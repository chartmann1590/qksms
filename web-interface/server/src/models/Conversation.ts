import { Entity, PrimaryColumn, Column, ManyToOne, OneToMany, CreateDateColumn, UpdateDateColumn, Index } from 'typeorm';
import { User } from './User';
import { Recipient } from './Recipient';
import { Message } from './Message';

@Entity('conversations')
@Index(['userId', 'archived'])
@Index(['userId', 'pinned'])
export class Conversation {
  @PrimaryColumn({ type: 'bigint' })
  id!: string;

  @Column({ name: 'user_id', type: 'uuid' })
  userId!: string;

  @Column({ nullable: true })
  name?: string;

  @Column({ default: false })
  archived!: boolean;

  @Column({ default: false })
  blocked!: boolean;

  @Column({ default: false })
  pinned!: boolean;

  @Column({ name: 'last_message_date', type: 'bigint', nullable: true })
  lastMessageDate?: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt!: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt!: Date;

  @ManyToOne(() => User, user => user.conversations, { onDelete: 'CASCADE' })
  user!: User;

  @OneToMany(() => Recipient, recipient => recipient.conversation, { cascade: true })
  recipients!: Recipient[];

  @OneToMany(() => Message, message => message.conversation)
  messages!: Message[];
}
