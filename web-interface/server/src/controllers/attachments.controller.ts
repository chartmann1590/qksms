import { Response } from 'express';
import { v4 as uuidv4 } from 'uuid';
import path from 'path';
import { AppDataSource } from '../config/database';
import { Attachment } from '../models/Attachment';
import { AttachmentService } from '../services/attachment.service';
import { AuthRequest } from '../middleware/auth.middleware';
import { ATTACHMENTS_DIR, THUMBNAILS_DIR } from '../config/upload';

export class AttachmentsController {
  private attachmentRepository = AppDataSource.getRepository(Attachment);
  private attachmentService = new AttachmentService();

  /**
   * Upload attachment file
   * POST /api/attachments/upload
   */
  async uploadAttachment(req: AuthRequest, res: Response): Promise<void> {
    try {
      if (!req.file) {
        res.status(400).json({ error: 'No file uploaded' });
        return;
      }

      const { messageId } = req.body;
      const file = req.file;
      const uploadId = uuidv4();

      // Generate thumbnail for images
      const thumbnailFilename = await this.attachmentService.generateThumbnail(
        file.path,
        file.mimetype
      );

      // Create attachment record (without messageId for now - will be set when message is synced)
      const attachmentData: Partial<Attachment> = {
        uploadId,
        mimeType: file.mimetype,
        filePath: file.filename, // Store relative path
        fileSize: file.size,
        thumbnailPath: thumbnailFilename || undefined,
      };

      if (messageId) {
        attachmentData.messageId = messageId;
      }

      const attachment = this.attachmentRepository.create(attachmentData);

      await this.attachmentRepository.save(attachment);

      res.json({
        uploadId,
        attachmentId: attachment.id,
        mimeType: file.mimetype,
        fileSize: file.size,
        hasThumbnail: !!thumbnailFilename,
      });
    } catch (error) {
      console.error('Upload attachment error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  /**
   * Download attachment file
   * GET /api/attachments/:id
   */
  async downloadAttachment(req: AuthRequest, res: Response): Promise<void> {
    try {
      const attachmentId = parseInt(req.params.id);

      const attachment = await this.attachmentRepository.findOne({
        where: { id: attachmentId },
        relations: ['message'],
      });

      if (!attachment) {
        res.status(404).json({ error: 'Attachment not found' });
        return;
      }

      // Verify user owns this attachment
      if (attachment.message && attachment.message.userId !== req.userId) {
        res.status(403).json({ error: 'Access denied' });
        return;
      }

      const filePath = path.join(ATTACHMENTS_DIR, attachment.filePath);

      // Validate file exists
      if (!this.attachmentService.validateFileAccess(filePath)) {
        res.status(404).json({ error: 'File not found on server' });
        return;
      }

      // Set appropriate headers
      res.setHeader('Content-Type', attachment.mimeType);
      res.setHeader('Content-Disposition', `inline; filename="${attachment.filePath}"`);

      if (attachment.fileSize) {
        res.setHeader('Content-Length', attachment.fileSize.toString());
      }

      // Stream file to response
      res.sendFile(filePath);
    } catch (error) {
      console.error('Download attachment error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  /**
   * Download attachment thumbnail
   * GET /api/attachments/:id/thumbnail
   */
  async downloadThumbnail(req: AuthRequest, res: Response): Promise<void> {
    try {
      const attachmentId = parseInt(req.params.id);

      const attachment = await this.attachmentRepository.findOne({
        where: { id: attachmentId },
        relations: ['message'],
      });

      if (!attachment) {
        res.status(404).json({ error: 'Attachment not found' });
        return;
      }

      // Verify user owns this attachment
      if (attachment.message && attachment.message.userId !== req.userId) {
        res.status(403).json({ error: 'Access denied' });
        return;
      }

      if (!attachment.thumbnailPath) {
        res.status(404).json({ error: 'Thumbnail not available' });
        return;
      }

      const thumbnailPath = path.join(THUMBNAILS_DIR, attachment.thumbnailPath);

      // Validate file exists
      if (!this.attachmentService.validateFileAccess(thumbnailPath)) {
        res.status(404).json({ error: 'Thumbnail not found on server' });
        return;
      }

      // Set appropriate headers
      res.setHeader('Content-Type', 'image/jpeg');
      res.setHeader('Content-Disposition', `inline; filename="thumb_${attachment.filePath}"`);

      // Stream file to response
      res.sendFile(thumbnailPath);
    } catch (error) {
      console.error('Download thumbnail error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }

  /**
   * Get attachment metadata by upload ID
   * GET /api/attachments/by-upload/:uploadId
   */
  async getByUploadId(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { uploadId } = req.params;

      const attachment = await this.attachmentRepository.findOne({
        where: { uploadId },
      });

      if (!attachment) {
        res.status(404).json({ error: 'Attachment not found' });
        return;
      }

      res.json({
        id: attachment.id,
        uploadId: attachment.uploadId,
        messageId: attachment.messageId,
        mimeType: attachment.mimeType,
        fileSize: attachment.fileSize,
        hasThumbnail: !!attachment.thumbnailPath,
        createdAt: attachment.createdAt,
      });
    } catch (error) {
      console.error('Get attachment by upload ID error:', error);
      res.status(500).json({ error: (error as Error).message });
    }
  }
}
