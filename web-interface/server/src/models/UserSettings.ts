import { Entity, PrimaryColumn, Column, OneToOne, JoinColumn } from 'typeorm';
import { User } from './User';

@Entity('user_settings')
export class UserSettings {
  @PrimaryColumn({ name: 'user_id', type: 'uuid' })
  userId!: string;

  @Column({ name: 'ollama_api_url', type: 'varchar', length: 500, default: 'http://localhost:11434' })
  ollamaApiUrl!: string;

  @Column({ name: 'ollama_model', type: 'varchar', length: 200, nullable: true })
  ollamaModel?: string;

  @Column({ name: 'ai_persona', type: 'text', nullable: true })
  aiPersona?: string;

  @Column({ name: 'ai_reply_enabled', type: 'boolean', default: false })
  aiReplyEnabled!: boolean;

  @OneToOne(() => User, user => user.userSettings, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user!: User;
}

