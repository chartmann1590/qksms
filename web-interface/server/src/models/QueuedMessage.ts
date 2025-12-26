import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, CreateDateColumn } from 'typeorm';
import { User } from './User';

@Entity('queued_messages')
export class QueuedMessage {
  @PrimaryGeneratedColumn('uuid')
  id!: string;

  @Column({ name: 'user_id', type: 'uuid' })
  userId!: string;

  @Column({ name: 'conversation_id', type: 'bigint', nullable: true })
  conversationId?: string;

  @Column({ name: 'addresses', type: 'text', array: true })
  addresses!: string[];

  @Column({ type: 'text' })
  body!: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;

  @Column({ name: 'picked_up', default: false })
  pickedUp!: boolean;

  @Column({ default: false })
  sent!: boolean;

  @Column({ name: 'android_message_id', type: 'bigint', nullable: true })
  androidMessageId?: string;

  @ManyToOne(() => User, user => user.queuedMessages, { onDelete: 'CASCADE' })
  user!: User;
}
