import 'dotenv/config';
import express, { Application } from 'express';
import { createServer } from 'http';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import { initializeDatabase } from './config/database';
import { initializeWebSocket } from './services/websocket.service';
import { errorHandler } from './middleware/error.middleware';
import { apiLimiter } from './middleware/ratelimit.middleware';
import { sanitizeBody } from './middleware/validation.middleware';
import { autoRegisterInitialUser } from "./utils/auto-register";
import authRoutes from './routes/auth.routes';
import syncRoutes from './routes/sync.routes';
import conversationsRoutes from './routes/conversations.routes';
import messagesRoutes from './routes/messages.routes';
import attachmentsRoutes from './routes/attachments.routes';
import settingsRoutes from './routes/settings.routes';

const app: Application = express();
const httpServer = createServer(app);
const PORT = process.env.PORT || 3000;

async function startServer(): Promise<void> {
  try {
    // Initialize database
    await initializeDatabase();

    // Auto-register initial user if configured
    await autoRegisterInitialUser();

    // Trust proxy (for nginx reverse proxy)
    // Only trust the first proxy (nginx) to prevent IP spoofing
    app.set('trust proxy', 1);

    // Middleware
    app.use(helmet()); // Security headers
    app.use(
      cors({
        origin: process.env.CLIENT_URL || 'http://localhost:8081',
        credentials: true,
        methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
        allowedHeaders: ['Content-Type', 'Authorization'],
      })
    ); // Enable CORS with restrictions
    app.use(morgan('combined')); // Logging
    app.use(express.json({ limit: '10mb' })); // Parse JSON bodies
    app.use(express.urlencoded({ extended: true, limit: '10mb' }));
    app.use(sanitizeBody); // Sanitize input

    // Rate limiting
    app.use('/api', apiLimiter);

    // Health check endpoint
    app.get('/health', (req, res) => {
      res.json({ status: 'ok', timestamp: new Date().toISOString() });
    });

    // Routes
    app.use('/api/auth', authRoutes);
    app.use('/api/sync', syncRoutes);
    app.use('/api/conversations', conversationsRoutes);
    app.use('/api/messages', messagesRoutes);
    app.use('/api/attachments', attachmentsRoutes);
    app.use('/api/settings', settingsRoutes);

    // 404 handler
    app.use((req, res) => {
      res.status(404).json({ error: 'Not found' });
    });

    // Error handling middleware (must be last)
    app.use(errorHandler);

    // Initialize WebSocket
    initializeWebSocket(httpServer);

    // Start server
    httpServer.listen(PORT, () => {
      console.log(`✓ Server running on port ${PORT}`);
      console.log(`✓ Environment: ${process.env.NODE_ENV || 'development'}`);
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
}

startServer();
