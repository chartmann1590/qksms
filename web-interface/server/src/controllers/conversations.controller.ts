import { Response } from 'express';
import { AppDataSource } from '../config/database';
import { Conversation } from '../models/Conversation';
import { Recipient } from '../models/Recipient';
import { Message } from '../models/Message';
import { AuthRequest } from '../middleware/auth.middleware';

export class ConversationsController {
  private conversationRepository = AppDataSource.getRepository(Conversation);
  private recipientRepository = AppDataSource.getRepository(Recipient);
  private messageRepository = AppDataSource.getRepository(Message);

  async getConversations(req: AuthRequest, res: Response): Promise<void> {
    try {
      const page = parseInt(req.query.page as string) || 1;
      const limit = parseInt(req.query.limit as string) || 1000; // Increased from 50 to 1000
      const skip = (page - 1) * limit;
      const search = req.query.search as string;

      let queryBuilder = this.conversationRepository
        .createQueryBuilder('conversation')
        .where('conversation.userId = :userId', { userId: req.userId! })
        .orderBy('conversation.lastMessageDate', 'DESC')
        .skip(skip)
        .take(limit);

      // Add search filter if provided
      if (search && search.trim()) {
        queryBuilder = queryBuilder.andWhere(
          '(conversation.name ILIKE :search OR EXISTS (SELECT 1 FROM recipients r WHERE r.conversation_id = conversation.id AND (r.address ILIKE :search OR r.contact_name ILIKE :search)) OR EXISTS (SELECT 1 FROM messages m WHERE m.conversation_id = conversation.id AND m.user_id = :userId AND m.body ILIKE :search))',
          { search: `%${search.trim()}%`, userId: req.userId! }
        );
      }

      const [conversations, total] = await queryBuilder.getManyAndCount();

      // Load recipients for each conversation
      const conversationsWithRecipients = await Promise.all(
        conversations.map(async (conv) => {
          const recipients = await this.recipientRepository.find({
            where: { conversationId: conv.id },
          });

          // Get last message
          const lastMessage = await this.messageRepository
            .createQueryBuilder('message')
            .leftJoinAndSelect('message.attachments', 'attachments')
            .where('message.conversationId = :convId', { convId: conv.id })
            .andWhere('message.userId = :userId', { userId: req.userId! })
            .orderBy('message.date', 'DESC')
            .getOne();

          // Count unread messages
          const unreadCount = await this.messageRepository.count({
            where: {
              conversationId: conv.id,
              userId: req.userId!,
              read: false,
              isMe: false,
            },
          });

          return {
            id: conv.id,
            name: conv.name,
            recipients: recipients.map((r) => ({
              id: r.id,
              address: r.address,
              name: r.contactName, // Include contact name
            })),
            lastMessage: lastMessage
              ? {
                  id: lastMessage.id,
                  body: this.getMessagePreview(lastMessage),
                  date: lastMessage.date,
                }
              : undefined,
            lastMessageDate: conv.lastMessageDate,
            archived: conv.archived,
            blocked: conv.blocked,
            pinned: conv.pinned,
            unreadCount,
          };
        })
      );

      res.json({
        conversations: conversationsWithRecipients,
        total,
        page,
        limit,
      });
    } catch (error) {
      console.error('Get conversations error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  private getMessagePreview(message: any): string {
    // If message has body text, return it
    if (message.body && message.body.trim()) {
      return message.body;
    }

    // If no body but has attachments, show attachment type
    if (message.attachments && message.attachments.length > 0) {
      const attachment = message.attachments[0];
      const mimeType = attachment.mimeType || '';

      if (mimeType.startsWith('image/')) {
        return message.attachments.length > 1 ? `${message.attachments.length} photos` : '📷 Photo';
      } else if (mimeType.startsWith('video/')) {
        return message.attachments.length > 1 ? `${message.attachments.length} videos` : '🎥 Video';
      } else if (mimeType.startsWith('audio/')) {
        return '🎵 Audio';
      } else {
        return message.attachments.length > 1 ? `${message.attachments.length} attachments` : '📎 Attachment';
      }
    }

    // Fallback for truly empty messages (shouldn't happen)
    return '';
  }

  async getConversation(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { id } = req.params;

      const conversation = await this.conversationRepository.findOne({
        where: { id, userId: req.userId! },
      });

      if (!conversation) {
        res.status(404).json({ error: 'Conversation not found' });
        return;
      }

      const recipients = await this.recipientRepository.find({
        where: { conversationId: id },
      });

      res.json({
        id: conversation.id,
        name: conversation.name,
        recipients: recipients.map((r) => ({
          id: r.id,
          address: r.address,
          name: r.contactName, // Include contact name
        })),
        archived: conversation.archived,
        blocked: conversation.blocked,
        pinned: conversation.pinned,
      });
    } catch (error) {
      console.error('Get conversation error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }
}
