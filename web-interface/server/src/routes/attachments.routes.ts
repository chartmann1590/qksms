import { Router } from 'express';
import { AttachmentsController } from '../controllers/attachments.controller';
import { authenticate } from '../middleware/auth.middleware';
import { upload } from '../config/upload';

const router = Router();
const controller = new AttachmentsController();

// All routes require authentication
router.use(authenticate);

// Upload attachment
router.post('/upload', upload.single('file'), (req, res) =>
  controller.uploadAttachment(req, res)
);

// Download attachment
router.get('/:id', (req, res) =>
  controller.downloadAttachment(req, res)
);

// Download thumbnail
router.get('/:id/thumbnail', (req, res) =>
  controller.downloadThumbnail(req, res)
);

// Get attachment by upload ID
router.get('/by-upload/:uploadId', (req, res) =>
  controller.getByUploadId(req, res)
);

export default router;
