import { Router } from 'express';
import { SyncController } from '../controllers/sync.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();
const syncController = new SyncController();

// All sync routes require authentication
router.post('/initial', authenticate, (req, res) => syncController.initialSync(req, res));
router.post('/incremental', authenticate, (req, res) => syncController.incrementalSync(req, res));
router.get('/status', authenticate, (req, res) => syncController.getSyncStatus(req, res));
router.get('/queue', authenticate, (req, res) => syncController.getQueuedMessages(req, res));
router.post('/confirm', authenticate, (req, res) => syncController.confirmMessageSent(req, res));

export default router;
