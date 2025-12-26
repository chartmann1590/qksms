import multer from 'multer';
import { v4 as uuidv4 } from 'uuid';
import path from 'path';
import fs from 'fs';

// Ensure upload directories exist
const UPLOAD_DIR = process.env.UPLOAD_DIR || './uploads';
const ATTACHMENTS_DIR = path.join(UPLOAD_DIR, 'attachments');
const THUMBNAILS_DIR = path.join(UPLOAD_DIR, 'thumbnails');

[UPLOAD_DIR, ATTACHMENTS_DIR, THUMBNAILS_DIR].forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
});

// Configure storage
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, ATTACHMENTS_DIR);
  },
  filename: (req, file, cb) => {
    const uniqueName = `${uuidv4()}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  },
});

// File filter - allow images, videos, audio, and documents
const fileFilter = (req: Express.Request, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
  const allowedMimes = [
    // Images
    'image/jpeg',
    'image/jpg',
    'image/png',
    'image/gif',
    'image/webp',
    'image/bmp',
    // Videos
    'video/mp4',
    'video/3gpp',
    'video/3gpp2',
    'video/quicktime',
    'video/x-msvideo',
    // Audio
    'audio/mpeg',
    'audio/mp3',
    'audio/mp4',
    'audio/3gpp',
    'audio/x-wav',
    'audio/ogg',
    // Documents
    'application/pdf',
    'text/plain',
    'text/x-vcard',
  ];

  if (allowedMimes.includes(file.mimetype)) {
    cb(null, true);
  } else {
    cb(new Error(`File type not allowed: ${file.mimetype}`));
  }
};

// Create multer instance
export const upload = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: parseInt(process.env.MAX_FILE_SIZE || '10485760'), // 10MB default
  },
});

export { ATTACHMENTS_DIR, THUMBNAILS_DIR };
