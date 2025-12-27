import { Router } from 'express';
import { AiController } from '../controllers/ai.controller';
import { authenticate } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/error.middleware';

const router = Router();
const aiController = new AiController();

router.get('/settings', authenticate, asyncHandler((req, res) => aiController.getSettings(req, res)));
router.put('/settings', authenticate, asyncHandler((req, res) => aiController.updateSettings(req, res)));
router.post('/test-connection', authenticate, asyncHandler((req, res) => aiController.testConnection(req, res)));
router.post('/generate-replies', authenticate, asyncHandler((req, res) => aiController.generateSmartReplies(req, res)));

export default router;

