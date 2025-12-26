import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authLimiter } from '../middleware/ratelimit.middleware';
import { authenticate } from '../middleware/auth.middleware';
import { validate, schemas } from '../middleware/validation.middleware';
import { asyncHandler } from '../middleware/error.middleware';

const router = Router();
const authController = new AuthController();

router.post(
  '/register',
  authLimiter,
  validate(schemas.register),
  asyncHandler((req, res) => authController.register(req, res))
);

router.post(
  '/login',
  authLimiter,
  validate(schemas.login),
  asyncHandler((req, res) => authController.login(req, res))
);

router.post('/refresh', asyncHandler((req, res) => authController.refresh(req, res)));

router.post('/logout', authenticate, asyncHandler((req, res) => authController.logout(req, res)));

export default router;
