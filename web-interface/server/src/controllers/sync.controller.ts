import { Request, Response } from 'express';
import { SyncService } from '../services/sync.service';
import { AuthRequest } from '../middleware/auth.middleware';

const syncService = new SyncService();

export class SyncController {
  async initialSync(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { conversations, messages, batchNumber, totalBatches } = req.body;

      if (!messages || !Array.isArray(messages)) {
        res.status(400).json({ error: 'Messages array required' });
        return;
      }

      if (typeof batchNumber !== 'number' || typeof totalBatches !== 'number') {
        res.status(400).json({ error: 'Batch information required' });
        return;
      }

      const result = await syncService.initialSync(
        req.userId!,
        conversations || [],
        messages,
        batchNumber,
        totalBatches
      );

      res.json({
        success: true,
        syncToken: result.syncToken,
        processedCount: result.processedCount,
      });
    } catch (error) {
      console.error('Initial sync error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  async incrementalSync(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { syncToken, newMessages, updatedMessages, deletedMessageIds } = req.body;

      if (!syncToken) {
        res.status(400).json({ error: 'Sync token required' });
        return;
      }

      const result = await syncService.incrementalSync(
        req.userId!,
        syncToken,
        newMessages || [],
        updatedMessages || [],
        deletedMessageIds || []
      );

      res.json({
        success: true,
        newSyncToken: result.newSyncToken,
        webUpdates: result.webUpdates,
      });
    } catch (error) {
      console.error('Incremental sync error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  async getSyncStatus(req: AuthRequest, res: Response): Promise<void> {
    try {
      const status = await syncService.getSyncStatus(req.userId!);
      res.json(status);
    } catch (error) {
      console.error('Get sync status error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  async getQueuedMessages(req: AuthRequest, res: Response): Promise<void> {
    try {
      const queuedMessages = await syncService.getQueuedMessages(req.userId!);

      res.json({
        queuedMessages: queuedMessages.map((msg) => ({
          id: msg.id,
          conversationId: msg.conversationId,
          addresses: msg.addresses,
          body: msg.body,
        })),
      });
    } catch (error) {
      console.error('Get queued messages error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  async confirmMessageSent(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { queueId, androidMessageId } = req.body;

      if (!queueId || !androidMessageId) {
        res.status(400).json({ error: 'Queue ID and Android message ID required' });
        return;
      }

      const success = await syncService.confirmMessageSent(
        req.userId!,
        queueId,
        androidMessageId
      );

      if (success) {
        res.json({ success: true });
      } else {
        res.status(404).json({ error: 'Queued message not found' });
      }
    } catch (error) {
      console.error('Confirm message sent error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }
}
