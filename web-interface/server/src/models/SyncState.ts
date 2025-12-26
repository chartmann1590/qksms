import { Entity, PrimaryColumn, Column, OneToOne, JoinColumn } from 'typeorm';
import { User } from './User';

@Entity('sync_state')
export class SyncState {
  @PrimaryColumn({ name: 'user_id', type: 'uuid' })
  userId!: string;

  @Column({ name: 'sync_token', type: 'uuid', nullable: true })
  syncToken?: string;

  @Column({ name: 'last_full_sync', type: 'timestamp', nullable: true })
  lastFullSync?: Date;

  @Column({ name: 'last_incremental_sync', type: 'timestamp', nullable: true })
  lastIncrementalSync?: Date;

  @Column({ name: 'total_messages', type: 'integer', default: 0 })
  totalMessages!: number;

  @Column({ name: 'total_conversations', type: 'integer', default: 0 })
  totalConversations!: number;

  @Column({ name: 'sync_in_progress', default: false })
  syncInProgress!: boolean;

  @OneToOne(() => User, user => user.syncState, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user!: User;
}
