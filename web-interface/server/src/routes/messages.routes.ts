import { Router } from 'express';
import { MessagesController } from '../controllers/messages.controller';
import { authenticate } from '../middleware/auth.middleware';
import { validate, schemas } from '../middleware/validation.middleware';
import { asyncHandler } from '../middleware/error.middleware';

const router = Router();
const messagesController = new MessagesController();

// All message routes require authentication
router.post(
  '/send',
  authenticate,
  validate(schemas.sendMessage),
  asyncHandler((req, res) => messagesController.sendMessage(req, res))
);

router.patch(
  '/status',
  authenticate,
  asyncHandler((req, res) => messagesController.updateMessageStatus(req, res))
);

export default router;
