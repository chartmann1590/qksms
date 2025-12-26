import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, CreateDateColumn } from 'typeorm';
import { Message } from './Message';

@Entity('attachments')
export class Attachment {
  @PrimaryGeneratedColumn('increment')
  id!: number;

  @Column({ name: 'message_id', type: 'bigint', nullable: true })
  messageId?: string;

  @Column({ name: 'upload_id', type: 'uuid', unique: true, nullable: true })
  uploadId?: string;

  @Column({ name: 'mime_type' })
  mimeType!: string;

  @Column({ name: 'file_path', type: 'text' })
  filePath!: string;

  @Column({ name: 'file_size', type: 'integer', nullable: true })
  fileSize?: number;

  @Column({ name: 'thumbnail_path', type: 'text', nullable: true })
  thumbnailPath?: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;

  @ManyToOne(() => Message, message => message.attachments, { onDelete: 'CASCADE' })
  message!: Message;
}
