import sharp from 'sharp';
import path from 'path';
import fs from 'fs';
import { v4 as uuidv4 } from 'uuid';
import { THUMBNAILS_DIR } from '../config/upload';

export class AttachmentService {
  /**
   * Generate thumbnail for image attachments
   * @param filePath Original file path
   * @param mimeType File MIME type
   * @returns Thumbnail path or null if not an image
   */
  async generateThumbnail(filePath: string, mimeType: string): Promise<string | null> {
    // Only generate thumbnails for images
    if (!mimeType.startsWith('image/')) {
      return null;
    }

    try {
      const thumbnailFilename = `thumb_${uuidv4()}.jpg`;
      const thumbnailPath = path.join(THUMBNAILS_DIR, thumbnailFilename);

      await sharp(filePath)
        .resize(300, 300, {
          fit: 'inside',
          withoutEnlargement: true,
        })
        .jpeg({ quality: 80 })
        .toFile(thumbnailPath);

      return thumbnailFilename;
    } catch (error) {
      console.error('Error generating thumbnail:', error);
      return null;
    }
  }

  /**
   * Delete attachment and its thumbnail from filesystem
   * @param filePath Attachment file path
   * @param thumbnailPath Thumbnail file path (optional)
   */
  async deleteAttachment(filePath: string, thumbnailPath?: string): Promise<void> {
    try {
      if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
      }

      if (thumbnailPath && fs.existsSync(thumbnailPath)) {
        fs.unlinkSync(thumbnailPath);
      }
    } catch (error) {
      console.error('Error deleting attachment files:', error);
    }
  }

  /**
   * Validate file exists and is accessible
   * @param filePath File path to validate
   * @returns true if file exists and is readable
   */
  validateFileAccess(filePath: string): boolean {
    try {
      return fs.existsSync(filePath) && fs.statSync(filePath).isFile();
    } catch {
      return false;
    }
  }

  /**
   * Get file size in bytes
   * @param filePath File path
   * @returns File size or null if error
   */
  getFileSize(filePath: string): number | null {
    try {
      return fs.statSync(filePath).size;
    } catch {
      return null;
    }
  }
}
