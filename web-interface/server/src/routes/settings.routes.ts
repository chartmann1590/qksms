import { Router } from 'express';
import { SettingsController } from '../controllers/settings.controller';
import { authenticate } from '../middleware/auth.middleware';
import { asyncHandler } from '../middleware/error.middleware';

const router = Router();
const settingsController = new SettingsController();

router.post(
  '/wipe-database',
  authenticate,
  asyncHandler((req, res) => settingsController.wipeDatabase(req, res))
);

export default router;
