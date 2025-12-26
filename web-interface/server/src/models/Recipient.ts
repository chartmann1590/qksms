import { Entity, PrimaryColumn, Column, ManyToOne, CreateDateColumn, Index } from 'typeorm';
import { Conversation } from './Conversation';

@Entity('recipients')
@Index(['address'])
export class Recipient {
  @PrimaryColumn({ type: 'varchar', length: 255 })
  id!: string;

  @Column({ name: 'conversation_id', type: 'bigint' })
  conversationId!: string;

  @Column()
  address!: string;

  @Column({ name: 'contact_name', nullable: true })
  contactName?: string;

  @Column({ name: 'contact_photo_uri', type: 'text', nullable: true })
  contactPhotoUri?: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;

  @ManyToOne(() => Conversation, conversation => conversation.recipients, { onDelete: 'CASCADE' })
  conversation!: Conversation;
}
