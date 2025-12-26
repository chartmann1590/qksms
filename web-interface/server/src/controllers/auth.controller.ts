import { Request, Response } from 'express';
import { AuthService } from '../services/auth.service';
import { AuthRequest } from '../middleware/auth.middleware';

const authService = new AuthService();

export class AuthController {
  async register(req: Request, res: Response): Promise<void> {
    try {
      const { username, password, deviceId } = req.body;

      if (!username || !password || !deviceId) {
        res.status(400).json({ error: 'Missing required fields' });
        return;
      }

      if (password.length < 12) {
        res.status(400).json({ error: 'Password must be at least 12 characters' });
        return;
      }

      const result = await authService.register({ username, password, deviceId });
      res.status(201).json({ success: true, ...result });
    } catch (error) {
      res.status(400).json({ error: (error as Error).message });
    }
  }

  async login(req: Request, res: Response): Promise<void> {
    try {
      const { username, password, deviceId } = req.body;

      if (!username || !password || !deviceId) {
        res.status(400).json({ error: 'Missing required fields' });
        return;
      }

      // Detect if request is from a web browser (not mobile app)
      const userAgent = req.headers['user-agent'] || '';
      const isWebBrowser = !userAgent.includes('okhttp') && 
                          (userAgent.includes('Mozilla') || userAgent.includes('Chrome') || 
                           userAgent.includes('Safari') || userAgent.includes('Firefox') ||
                           userAgent.includes('Edge'));

      const { user, tokens } = await authService.login({ username, password, deviceId }, isWebBrowser);

      // Return different response format based on client type
      // Android app expects: { success, accessToken, refreshToken }
      // Web interface expects: { success, user, tokens: { accessToken, refreshToken } }
      if (isWebBrowser) {
        res.json({
          success: true,
          user: {
            id: user.id,
            username: user.username,
            deviceId: user.deviceId,
          },
          tokens: {
            accessToken: tokens.accessToken,
            refreshToken: tokens.refreshToken,
          },
        });
      } else {
        // Android app format (backward compatible)
        res.json({
          success: true,
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
        });
      }
    } catch (error) {
      res.status(401).json({ error: (error as Error).message });
    }
  }

  async refresh(req: Request, res: Response): Promise<void> {
    try {
      const { refreshToken } = req.body;

      if (!refreshToken) {
        res.status(400).json({ error: 'Refresh token required' });
        return;
      }

      const tokens = await authService.refreshAccessToken(refreshToken);

      res.json({
        success: true,
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
      });
    } catch (error) {
      res.status(401).json({ error: (error as Error).message });
    }
  }

  async logout(req: AuthRequest, res: Response): Promise<void> {
    try {
      const { refreshToken } = req.body;

      if (refreshToken) {
        await authService.logout(refreshToken);
      }

      res.json({ success: true });
    } catch (error) {
      res.status(400).json({ error: (error as Error).message });
    }
  }
}
