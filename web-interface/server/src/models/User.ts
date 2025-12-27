import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, OneToMany, OneToOne } from 'typeorm';
import { Conversation } from './Conversation';
import { Message } from './Message';
import { SyncState } from './SyncState';
import { QueuedMessage } from './QueuedMessage';
import { RefreshToken } from './RefreshToken';
import { UserSettings } from './UserSettings';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid')
  id!: string;

  @Column({ unique: true })
  username!: string;

  @Column({ name: 'password_hash' })
  passwordHash!: string;

  @Column({ name: 'device_id', unique: true })
  deviceId!: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;

  @Column({ name: 'last_login', type: 'timestamp', nullable: true })
  lastLogin?: Date;

  @OneToMany(() => Conversation, conversation => conversation.user)
  conversations!: Conversation[];

  @OneToMany(() => Message, message => message.user)
  messages!: Message[];

  @OneToOne(() => SyncState, syncState => syncState.user)
  syncState!: SyncState;

  @OneToMany(() => QueuedMessage, queuedMessage => queuedMessage.user)
  queuedMessages!: QueuedMessage[];

  @OneToMany(() => RefreshToken, refreshToken => refreshToken.user)
  refreshTokens!: RefreshToken[];

  @OneToOne(() => UserSettings, userSettings => userSettings.user)
  userSettings!: UserSettings;
}
