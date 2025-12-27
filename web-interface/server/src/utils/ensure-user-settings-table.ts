import { AppDataSource } from '../config/database';
import { UserSettings } from '../models/UserSettings';

/**
 * Ensure the user_settings table exists
 * This is a workaround for production environments where synchronize is disabled
 */
export async function ensureUserSettingsTable(): Promise<void> {
  try {
    const queryRunner = AppDataSource.createQueryRunner();
    
    // Check if table exists
    const tableExists = await queryRunner.hasTable('user_settings');
    
    if (!tableExists) {
      console.log('Creating user_settings table...');
      
      await queryRunner.query(`
        CREATE TABLE IF NOT EXISTS user_settings (
          user_id UUID PRIMARY KEY,
          ollama_api_url VARCHAR(500) DEFAULT 'http://localhost:11434',
          ollama_model VARCHAR(200),
          ai_persona TEXT,
          ai_reply_enabled BOOLEAN DEFAULT false,
          CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        )
      `);
      
      console.log('✓ user_settings table created');
    }
    
    await queryRunner.release();
  } catch (error) {
    console.error('Error ensuring user_settings table:', error);
    // Don't throw - let the app continue, TypeORM will handle it
  }
}

