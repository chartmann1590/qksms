import { v4 as uuidv4 } from 'uuid';
import { AppDataSource } from '../config/database';
import { User } from '../models/User';
import { Conversation } from '../models/Conversation';
import { Recipient } from '../models/Recipient';
import { Message } from '../models/Message';
import { Attachment } from '../models/Attachment';
import { SyncState } from '../models/SyncState';
import { QueuedMessage } from '../models/QueuedMessage';
import { getWebSocketService } from './websocket.service';

export interface RecipientDto {
  address: string;
  contactName?: string;
}

export interface ConversationDto {
  id: string;
  recipients: RecipientDto[];
  name?: string;
  archived: boolean;
  blocked: boolean;
  pinned: boolean;
}

export interface MessageDto {
  id: string;
  threadId: string;
  address: string;
  body?: string;
  type: 'sms' | 'mms';
  date: string;
  dateSent?: string;
  read: boolean;
  seen: boolean;
  isMe: boolean;
  attachments?: AttachmentDto[];
}

export interface AttachmentDto {
  type: string;
  data: string;
  uploadId?: string;
}

export interface MessageUpdateDto {
  id: string;
  read: boolean;
  seen: boolean;
  timestamp: number;
}

export class SyncService {
  private userRepository = AppDataSource.getRepository(User);
  private conversationRepository = AppDataSource.getRepository(Conversation);
  private recipientRepository = AppDataSource.getRepository(Recipient);
  private messageRepository = AppDataSource.getRepository(Message);
  private attachmentRepository = AppDataSource.getRepository(Attachment);
  private syncStateRepository = AppDataSource.getRepository(SyncState);
  private queuedMessageRepository = AppDataSource.getRepository(QueuedMessage);

  async initialSync(
    userId: string,
    conversations: ConversationDto[],
    messages: MessageDto[],
    batchNumber: number,
    totalBatches: number
  ): Promise<{ syncToken: string; processedCount: number }> {
    // Get or create sync state
    let syncState = await this.syncStateRepository.findOne({ where: { userId } });
    if (!syncState) {
      syncState = this.syncStateRepository.create({
        userId,
        syncToken: uuidv4(),
      });
    }

    // Mark sync in progress
    syncState.syncInProgress = true;
    await this.syncStateRepository.save(syncState);

    try {
      // Process conversations (only in first batch)
      if (batchNumber === 1 && conversations.length > 0) {
        for (const convDto of conversations) {
          await this.saveConversation(userId, convDto);
        }
        syncState.totalConversations = conversations.length;
        // Reset message count on first batch of full sync
        syncState.totalMessages = 0;
      }

      // Process messages
      let processedCount = 0;
      for (const msgDto of messages) {
        const wasNew = await this.saveMessage(userId, msgDto);
        if (wasNew) processedCount++;
      }

      // Update sync state (only count new messages to avoid inflating the count)
      syncState.totalMessages += processedCount;

      // If this is the last batch, mark sync complete
      if (batchNumber === totalBatches) {
        syncState.syncInProgress = false;
        syncState.lastFullSync = new Date();
        syncState.syncToken = uuidv4(); // Generate new token for next sync
      }

      await this.syncStateRepository.save(syncState);

      return {
        syncToken: syncState.syncToken!,
        processedCount: processedCount,
      };
    } catch (error) {
      syncState.syncInProgress = false;
      await this.syncStateRepository.save(syncState);
      throw error;
    }
  }

  async incrementalSync(
    userId: string,
    syncToken: string,
    newMessages: MessageDto[],
    updatedMessages: MessageUpdateDto[],
    deletedMessageIds: string[]
  ): Promise<{ newSyncToken: string; webUpdates: any }> {
    // Verify sync token
    const syncState = await this.syncStateRepository.findOne({ where: { userId } });
    if (!syncState || syncState.syncToken !== syncToken) {
      throw new Error('Invalid sync token');
    }

    // Process new messages
    for (const msgDto of newMessages) {
      await this.saveMessage(userId, msgDto);
    }

    // Process message updates
    for (const update of updatedMessages) {
      const message = await this.messageRepository.findOne({
        where: { id: update.id, userId },
      });
      if (message) {
        message.read = update.read;
        message.seen = update.seen;
        message.updatedAt = new Date();
        await this.messageRepository.save(message);
      }
    }

    // Process deletes (mark as deleted, don't actually delete)
    // In the future, could implement soft delete

    // Update sync state
    syncState.lastIncrementalSync = new Date();
    syncState.totalMessages += newMessages.length;
    const newSyncToken = uuidv4();
    syncState.syncToken = newSyncToken;
    await this.syncStateRepository.save(syncState);

    // TODO: Get updates made from web (read status changes, etc.)
    const webUpdates = {
      readMessages: [],
      seenMessages: [],
    };

    return { newSyncToken, webUpdates };
  }

  async getSyncStatus(userId: string) {
    const syncState = await this.syncStateRepository.findOne({ where: { userId } });

    if (!syncState) {
      return {
        lastSyncTime: null,
        lastFullSync: null,
        lastIncrementalSync: null,
        messageCount: 0,
        conversationCount: 0,
        syncInProgress: false,
      };
    }

    return {
      lastSyncTime: syncState.lastFullSync?.toISOString() || syncState.lastIncrementalSync?.toISOString() || null,
      lastFullSync: syncState.lastFullSync?.toISOString() || null,
      lastIncrementalSync: syncState.lastIncrementalSync?.toISOString() || null,
      messageCount: syncState.totalMessages,
      conversationCount: syncState.totalConversations,
      syncInProgress: syncState.syncInProgress,
    };
  }

  async getQueuedMessages(userId: string) {
    const queuedMessages = await this.queuedMessageRepository.find({
      where: { userId, pickedUp: false, sent: false },
      order: { createdAt: 'ASC' },
    });

    // Mark as picked up
    for (const msg of queuedMessages) {
      msg.pickedUp = true;
      await this.queuedMessageRepository.save(msg);
    }

    return queuedMessages;
  }

  async confirmMessageSent(
    userId: string,
    queueId: string,
    androidMessageId: string
  ): Promise<boolean> {
    const queuedMessage = await this.queuedMessageRepository.findOne({
      where: { id: queueId, userId },
    });

    if (!queuedMessage) {
      return false;
    }

    queuedMessage.sent = true;
    queuedMessage.androidMessageId = androidMessageId;
    await this.queuedMessageRepository.save(queuedMessage);

    // Find the actual message that was created by Android
    const message = await this.messageRepository.findOne({
      where: { id: androidMessageId, userId },
    });

    // Broadcast message sent confirmation to web clients
    if (message) {
      try {
        const wsService = getWebSocketService();
        wsService.broadcastMessageSent(userId, queueId, androidMessageId, message);
      } catch (error) {
        console.error('Error broadcasting message sent confirmation:', error);
        // Don't throw - confirmation should succeed even if WebSocket broadcast fails
      }
    }

    return true;
  }

  private async saveConversation(userId: string, convDto: ConversationDto) {
    let conversation = await this.conversationRepository.findOne({
      where: { id: convDto.id, userId },
    });

    if (!conversation) {
      conversation = this.conversationRepository.create({
        id: convDto.id,
        userId,
        name: convDto.name,
        archived: convDto.archived,
        blocked: convDto.blocked,
        pinned: convDto.pinned,
      });
    } else {
      // Update existing conversation
      conversation.name = convDto.name;
      conversation.archived = convDto.archived;
      conversation.blocked = convDto.blocked;
      conversation.pinned = convDto.pinned;
    }

    await this.conversationRepository.save(conversation);

    // Save recipients
    for (const recipientDto of convDto.recipients) {
      const existingRecipient = await this.recipientRepository.findOne({
        where: { conversationId: convDto.id, address: recipientDto.address },
      });

      if (!existingRecipient) {
        const recipient = this.recipientRepository.create({
          id: `${convDto.id}_${recipientDto.address}`, // Generate unique ID
          conversationId: convDto.id,
          address: recipientDto.address,
          contactName: recipientDto.contactName,
        });
        await this.recipientRepository.save(recipient);
      } else {
        // Update contact name if it's changed
        existingRecipient.contactName = recipientDto.contactName;
        await this.recipientRepository.save(existingRecipient);
      }
    }
  }

  private async saveMessage(userId: string, msgDto: MessageDto): Promise<boolean> {
    let message;
    let isNewMessage = false;

    try {
      message = await this.messageRepository.findOne({
        where: { id: msgDto.id, userId },
      });

      isNewMessage = !message;

      if (!message) {
        // Validate numeric fields before creating
        if (!msgDto.id || !/^\d+$/.test(msgDto.id)) {
          console.error(`Invalid message ID: "${msgDto.id}" (type: ${typeof msgDto.id})`);
          throw new Error(`Invalid message ID: "${msgDto.id}"`);
        }
        if (!msgDto.threadId || !/^\d+$/.test(msgDto.threadId)) {
          console.error(`Invalid threadId for message ${msgDto.id}: "${msgDto.threadId}" (type: ${typeof msgDto.threadId})`);
          throw new Error(`Invalid threadId for message ${msgDto.id}: "${msgDto.threadId}"`);
        }
        if (!msgDto.date || !/^-?\d+$/.test(msgDto.date)) {
          console.error(`Invalid date for message ${msgDto.id}: "${msgDto.date}" (type: ${typeof msgDto.date})`);
          throw new Error(`Invalid date for message ${msgDto.id}: "${msgDto.date}"`);
        }
        if (msgDto.dateSent && !/^-?\d+$/.test(msgDto.dateSent)) {
          console.error(`Invalid dateSent for message ${msgDto.id}: "${msgDto.dateSent}" (type: ${typeof msgDto.dateSent})`);
          throw new Error(`Invalid dateSent for message ${msgDto.id}: "${msgDto.dateSent}"`);
        }

        message = this.messageRepository.create({
          id: msgDto.id,
          conversationId: msgDto.threadId,
          userId,
          address: msgDto.address,
          body: msgDto.body,
          type: msgDto.type,
          date: msgDto.date,
          dateSent: msgDto.dateSent,
          read: msgDto.read,
          seen: msgDto.seen,
          isMe: msgDto.isMe,
        });
      } else {
        // Update existing message
        message.read = msgDto.read;
        message.seen = msgDto.seen;
        message.updatedAt = new Date();
      }

      await this.messageRepository.save(message);
    } catch (error) {
      console.error('Error saving message:', error);
      console.error('Message DTO:', JSON.stringify(msgDto, null, 2));
      throw error;
    }

    // Broadcast new message to connected web clients
    if (isNewMessage && !msgDto.isMe) {
      try {
        const wsService = getWebSocketService();
        wsService.broadcastNewMessage(userId, message);
      } catch (error) {
        console.error('Error broadcasting new message:', error);
        // Don't throw - sync should continue even if WebSocket broadcast fails
      }
    }

    // Update conversation's last message date
    const conversation = await this.conversationRepository.findOne({
      where: { id: msgDto.threadId, userId },
    });
    if (conversation) {
      const currentLastDate = conversation.lastMessageDate
        ? BigInt(conversation.lastMessageDate)
        : BigInt(0);
      const newDate = BigInt(msgDto.date);

      if (newDate > currentLastDate) {
        conversation.lastMessageDate = msgDto.date;
        await this.conversationRepository.save(conversation);
      }
    }

    // Save attachments if present
    if (msgDto.attachments && msgDto.attachments.length > 0) {
      await this.saveAttachments(userId, msgDto.id, msgDto.attachments);
    }

    return isNewMessage;
  }

  private async saveAttachments(userId: string, messageId: string, attachments: AttachmentDto[]) {
    const fs = require('fs').promises;
    const path = require('path');
    const { ATTACHMENTS_DIR } = require('../config/upload');
    const { AttachmentService } = require('./attachment.service');
    const attachmentService = new AttachmentService();

    for (const attachDto of attachments) {
      // Check if attachment already exists (avoid duplicates)
      const existing = attachDto.uploadId
        ? await this.attachmentRepository.findOne({
            where: { uploadId: attachDto.uploadId, messageId },
          })
        : null;

      if (existing) {
        continue; // Skip if already saved
      }

      // Handle two cases:
      // 1. uploadId provided - file was uploaded separately
      // 2. data provided - inline base64 data for small files

      if (attachDto.uploadId) {
        // Update existing attachment record with messageId
        const attachment = await this.attachmentRepository.findOne({
          where: { uploadId: attachDto.uploadId },
        });

        if (attachment) {
          attachment.messageId = messageId;
          await this.attachmentRepository.save(attachment);
        }
      } else if (attachDto.data) {
        // Save inline base64 data as file
        const filename = `${messageId}_${uuidv4()}${this.getExtensionFromMimeType(attachDto.type)}`;
        const filePath = path.join(ATTACHMENTS_DIR, filename);

        try {
          // Decode base64 and save to file
          const buffer = Buffer.from(attachDto.data, 'base64');
          await fs.writeFile(filePath, buffer);

          // Generate thumbnail if image
          const thumbnailFilename = await attachmentService.generateThumbnail(
            filePath,
            attachDto.type
          );

          // Create attachment record
          const attachment = this.attachmentRepository.create({
            messageId,
            uploadId: uuidv4(),
            mimeType: attachDto.type,
            filePath: filename, // Store relative path
            fileSize: buffer.length,
            thumbnailPath: thumbnailFilename,
          });

          await this.attachmentRepository.save(attachment);
        } catch (error) {
          console.error('Error saving inline attachment:', error);
          // Continue with other attachments
        }
      }
    }
  }

  private getExtensionFromMimeType(mimeType: string): string {
    const extensions: { [key: string]: string } = {
      'image/jpeg': '.jpg',
      'image/jpg': '.jpg',
      'image/png': '.png',
      'image/gif': '.gif',
      'image/webp': '.webp',
      'image/bmp': '.bmp',
      'video/mp4': '.mp4',
      'video/3gpp': '.3gp',
      'video/3gpp2': '.3g2',
      'video/quicktime': '.mov',
      'audio/mpeg': '.mp3',
      'audio/mp3': '.mp3',
      'audio/mp4': '.m4a',
      'audio/3gpp': '.3ga',
      'audio/x-wav': '.wav',
      'audio/ogg': '.ogg',
      'application/pdf': '.pdf',
      'text/plain': '.txt',
      'text/x-vcard': '.vcf',
    };

    return extensions[mimeType] || '';
  }
}
