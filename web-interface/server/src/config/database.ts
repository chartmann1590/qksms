import { DataSource } from 'typeorm';
import { User } from '../models/User';
import { Conversation} from '../models/Conversation';
import { Recipient } from '../models/Recipient';
import { Message } from '../models/Message';
import { Attachment } from '../models/Attachment';
import { SyncState } from '../models/SyncState';
import { QueuedMessage } from '../models/QueuedMessage';
import { RefreshToken } from '../models/RefreshToken';

export const AppDataSource = new DataSource({
  type: 'postgres',
  url: process.env.DATABASE_URL,
  synchronize: process.env.NODE_ENV === 'development', // Auto-create tables in dev
  logging: process.env.NODE_ENV === 'development',
  entities: [User, Conversation, Recipient, Message, Attachment, SyncState, QueuedMessage, RefreshToken],
  migrations: [],
  subscribers: [],
});

export async function initializeDatabase(): Promise<void> {
  try {
    await AppDataSource.initialize();
    console.log('✓ Database connection established');
  } catch (error) {
    console.error('Database connection failed:', error);
    throw error;
  }
}
