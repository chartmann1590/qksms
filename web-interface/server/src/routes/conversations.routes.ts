import { Router } from 'express';
import { ConversationsController } from '../controllers/conversations.controller';
import { MessagesController } from '../controllers/messages.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();
const conversationsController = new ConversationsController();
const messagesController = new MessagesController();

// All conversation routes require authentication
router.get('/', authenticate, (req, res) => conversationsController.getConversations(req, res));
router.get('/:id', authenticate, (req, res) => conversationsController.getConversation(req, res));
router.get('/:conversationId/messages', authenticate, (req, res) => messagesController.getMessages(req, res));

export default router;
