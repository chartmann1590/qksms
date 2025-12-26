import { Response } from 'express';
import { AppDataSource } from '../config/database';
import { Conversation } from '../models/Conversation';
import { Recipient } from '../models/Recipient';
import { Message } from '../models/Message';
import { Attachment } from '../models/Attachment';
import { SyncState } from '../models/SyncState';
import { QueuedMessage } from '../models/QueuedMessage';
import { AuthRequest } from '../middleware/auth.middleware';
import * as fs from 'fs/promises';
import * as path from 'path';

export class SettingsController {
  private conversationRepository = AppDataSource.getRepository(Conversation);
  private recipientRepository = AppDataSource.getRepository(Recipient);
  private messageRepository = AppDataSource.getRepository(Message);
  private attachmentRepository = AppDataSource.getRepository(Attachment);
  private syncStateRepository = AppDataSource.getRepository(SyncState);
  private queuedMessageRepository = AppDataSource.getRepository(QueuedMessage);

  async wipeDatabase(req: AuthRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;

      console.log(`Wiping database for user ${userId}...`);

      // Delete all attachments files for this user
      const attachments = await this.attachmentRepository
        .createQueryBuilder('attachment')
        .innerJoin('attachment.message', 'message')
        .where('message.userId = :userId', { userId })
        .getMany();

      // Delete attachment files
      const { ATTACHMENTS_DIR } = require('../config/upload');
      for (const attachment of attachments) {
        try {
          if (attachment.filePath) {
            const filePath = path.join(ATTACHMENTS_DIR, attachment.filePath);
            await fs.unlink(filePath).catch(() => {}); // Ignore errors if file doesn't exist
          }
          if (attachment.thumbnailPath) {
            const thumbnailPath = path.join(ATTACHMENTS_DIR, attachment.thumbnailPath);
            await fs.unlink(thumbnailPath).catch(() => {});
          }
        } catch (error) {
          console.error('Error deleting attachment file:', error);
          // Continue with deletion even if file deletion fails
        }
      }

      // Delete database records (cascading will handle related records)
      await this.messageRepository.delete({ userId });
      await this.conversationRepository.delete({ userId });
      await this.queuedMessageRepository.delete({ userId });

      // Reset sync state
      const syncState = await this.syncStateRepository.findOne({ where: { userId } });
      if (syncState) {
        syncState.totalMessages = 0;
        syncState.totalConversations = 0;
        syncState.lastFullSync = undefined;
        syncState.lastIncrementalSync = undefined;
        syncState.syncInProgress = false;
        await this.syncStateRepository.save(syncState);
      }

      console.log(`Database wiped successfully for user ${userId}`);

      res.json({
        success: true,
        message: 'Database wiped successfully',
      });
    } catch (error) {
      console.error('Wipe database error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }
}
