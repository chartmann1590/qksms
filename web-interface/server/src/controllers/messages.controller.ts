import { Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { AppDataSource } from '../config/database';
import { Message } from '../models/Message';
import { QueuedMessage } from '../models/QueuedMessage';
import { Conversation } from '../models/Conversation';
import { AuthRequest } from '../middleware/auth.middleware';

export class MessagesController {
  private messageRepository = AppDataSource.getRepository(Message);
  private queuedMessageRepository = AppDataSource.getRepository(QueuedMessage);
  private conversationRepository = AppDataSource.getRepository(Conversation);

  async getMessages(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { conversationId } = req.params;
      const page = parseInt(req.query.page as string) || 1;
      const limit = parseInt(req.query.limit as string) || 100;
      const skip = (page - 1) * limit;

      const [messages, total] = await this.messageRepository.findAndCount({
        where: { conversationId, userId: req.userId! },
        order: { date: 'DESC' },
        relations: ['attachments'],
        skip,
        take: limit,
      });

      // Reverse to show oldest first
      const messagesResponse = messages.reverse().map((msg) => ({
        id: msg.id,
        conversationId: msg.conversationId,
        address: msg.address,
        body: msg.body,
        type: msg.type,
        date: msg.date,
        dateSent: msg.dateSent,
        read: msg.read,
        seen: msg.seen,
        isMe: msg.isMe,
        attachments: msg.attachments?.map((att) => ({
          id: att.id,
          mimeType: att.mimeType,
          fileSize: att.fileSize,
          hasThumbnail: !!att.thumbnailPath,
        })),
      }));

      res.json({
        messages: messagesResponse,
        total,
        page,
        limit,
      });
    } catch (error) {
      console.error('Get messages error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  async sendMessage(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { conversationId, addresses, body } = req.body;

      if (!addresses || !Array.isArray(addresses) || addresses.length === 0) {
        res.status(400).json({ error: 'Addresses array required' });
        return;
      }

      if (!body || typeof body !== 'string') {
        res.status(400).json({ error: 'Message body required' });
        return;
      }

      // Create queued message for Android to pick up
      const queuedMessage = this.queuedMessageRepository.create({
        userId: req.userId!,
        conversationId: conversationId || undefined,
        addresses: addresses,
        body,
        pickedUp: false,
        sent: false,
      });

      await this.queuedMessageRepository.save(queuedMessage);

      res.json({
        queueId: queuedMessage.id,
        message: 'Message queued for sending',
      });
    } catch (error) {
      console.error('Send message error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  async updateMessageStatus(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { messageIds, read, seen } = req.body;

      if (!messageIds || !Array.isArray(messageIds)) {
        res.status(400).json({ error: 'Message IDs array required' });
        return;
      }

      // Update messages
      await this.messageRepository
        .createQueryBuilder()
        .update(Message)
        .set({
          ...(typeof read === 'boolean' ? { read } : {}),
          ...(typeof seen === 'boolean' ? { seen } : {}),
          updatedAt: new Date(),
        })
        .where('id IN (:...ids)', { ids: messageIds })
        .andWhere('userId = :userId', { userId: req.userId! })
        .execute();

      res.json({ success: true, updated: messageIds.length });
    } catch (error) {
      console.error('Update message status error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }
}
