import { Entity, PrimaryColumn, Column, ManyToOne, OneToMany, JoinColumn, CreateDateColumn, UpdateDateColumn, Index } from 'typeorm';
import { User } from './User';
import { Conversation } from './Conversation';
import { Attachment } from './Attachment';

@Entity('messages')
@Index(['userId', 'conversationId', 'date'])
@Index(['userId', 'date'])
@Index(['userId', 'read'])
export class Message {
  @PrimaryColumn({ type: 'bigint' })
  id!: string;

  @Column({ name: 'conversation_id', type: 'bigint' })
  conversationId!: string;

  @Column({ name: 'user_id', type: 'uuid' })
  userId!: string;

  @Column()
  address!: string;

  @Column({ type: 'text', nullable: true })
  body?: string;

  @Column({ type: 'varchar', length: 10 })
  type!: 'sms' | 'mms';

  @Column({ type: 'bigint' })
  date!: string;

  @Column({ name: 'date_sent', type: 'bigint', nullable: true })
  dateSent?: string;

  @Column({ default: false })
  read!: boolean;

  @Column({ default: false })
  seen!: boolean;

  @Column({ name: 'is_me', default: false })
  isMe!: boolean;

  @Column({ name: 'box_id', type: 'integer', nullable: true })
  boxId?: number;

  @Column({ name: 'delivery_status', type: 'integer', default: 0 })
  deliveryStatus!: number;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt!: Date;

  @ManyToOne(() => User, user => user.messages, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user!: User;

  @ManyToOne(() => Conversation, conversation => conversation.messages, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'conversation_id' })
  conversation!: Conversation;

  @OneToMany(() => Attachment, attachment => attachment.message, { cascade: true })
  attachments!: Attachment[];
}
