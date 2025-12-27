import { Response } from 'express';
import { AppDataSource } from '../config/database';
import { UserSettings } from '../models/UserSettings';
import { Message } from '../models/Message';
import { AuthRequest } from '../middleware/auth.middleware';
import { OllamaService } from '../services/ollama.service';

export class AiController {
  private userSettingsRepository = AppDataSource.getRepository(UserSettings);
  private messageRepository = AppDataSource.getRepository(Message);
  private ollamaService = new OllamaService();

  /**
   * Get current user's AI settings
   */
  async getSettings(req: AuthRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;

      let settings = await this.userSettingsRepository.findOne({
        where: { userId },
      });

      // Create default settings if they don't exist
      if (!settings) {
        settings = this.userSettingsRepository.create({
          userId,
          ollamaApiUrl: 'http://localhost:11434',
          ollamaModel: undefined,
          aiPersona: undefined,
          aiReplyEnabled: false,
        });
        await this.userSettingsRepository.save(settings);
      }

      res.json({
        ollamaApiUrl: settings.ollamaApiUrl,
        ollamaModel: settings.ollamaModel || null,
        aiPersona: settings.aiPersona || null,
        aiReplyEnabled: settings.aiReplyEnabled,
      });
    } catch (error) {
      console.error('Get AI settings error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  /**
   * Update AI settings
   */
  async updateSettings(req: AuthRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const { ollamaApiUrl, ollamaModel, aiPersona, aiReplyEnabled } = req.body;

      let settings = await this.userSettingsRepository.findOne({
        where: { userId },
      });

      if (!settings) {
        settings = this.userSettingsRepository.create({
          userId,
          ollamaApiUrl: ollamaApiUrl || 'http://localhost:11434',
          ollamaModel: ollamaModel || undefined,
          aiPersona: aiPersona || undefined,
          aiReplyEnabled: aiReplyEnabled || false,
        });
      } else {
        if (ollamaApiUrl !== undefined) settings.ollamaApiUrl = ollamaApiUrl;
        if (ollamaModel !== undefined) settings.ollamaModel = ollamaModel || undefined;
        if (aiPersona !== undefined) settings.aiPersona = aiPersona || undefined;
        if (aiReplyEnabled !== undefined) settings.aiReplyEnabled = aiReplyEnabled;
      }

      await this.userSettingsRepository.save(settings);

      res.json({
        success: true,
        settings: {
          ollamaApiUrl: settings.ollamaApiUrl,
          ollamaModel: settings.ollamaModel || null,
          aiPersona: settings.aiPersona || null,
          aiReplyEnabled: settings.aiReplyEnabled,
        },
      });
    } catch (error) {
      console.error('Update AI settings error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  /**
   * Test Ollama connection and fetch available models
   */
  async testConnection(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { url } = req.body;
      console.log('Test connection request body:', req.body);
      console.log('URL received:', url);

      if (!url || typeof url !== 'string') {
        res.status(400).json({ error: 'URL is required' });
        return;
      }

      console.log('Attempting to fetch models from:', url);
      const models = await this.ollamaService.getAvailableModels(url);

      res.json({
        success: true,
        models: models.map((model) => ({
          name: model.name,
          size: model.size,
          modifiedAt: model.modified_at,
        })),
      });
    } catch (error) {
      console.error('Test connection error:', error);
      res.status(500).json({
        success: false,
        error: (error as Error).message,
      });
    }
  }

  /**
   * Generate smart replies for a conversation
   */
  async generateSmartReplies(req: AuthRequest, res: Response): Promise<void> {
    try {
      const userId = req.userId!;
      const { conversationId } = req.body;

      if (!conversationId) {
        res.status(400).json({ error: 'Conversation ID is required' });
        return;
      }

      // Get user's AI settings
      const settings = await this.userSettingsRepository.findOne({
        where: { userId },
      });

      if (!settings || !settings.aiReplyEnabled) {
        res.status(400).json({ error: 'AI replies are not enabled' });
        return;
      }

      if (!settings.ollamaModel) {
        res.status(400).json({ error: 'No model selected' });
        return;
      }

      // Get recent messages from the conversation (last 10, then we'll filter to last 4 valid ones)
      // Order by date DESC to get newest first, then reverse to get chronological order
      const messages = await this.messageRepository.find({
        where: {
          conversationId,
          userId,
        },
        order: {
          date: 'DESC', // Newest first
        },
        take: 10,
      });
      
      // Reverse to get chronological order (oldest first) for context building
      messages.reverse();

      if (messages.length === 0) {
        res.status(400).json({ error: 'No messages found in conversation' });
        return;
      }

      // Generate smart replies using the Ollama service
      const suggestions = await this.ollamaService.generateReplySuggestions(
        settings.ollamaApiUrl,
        settings.ollamaModel,
        messages,
        settings.aiPersona || undefined
      );

      res.json({
        success: true,
        suggestions,
      });
    } catch (error) {
      console.error('Generate smart replies error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }
}

