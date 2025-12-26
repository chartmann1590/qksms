import React, { useEffect, useRef, useState, FormEvent } from 'react';
import { format } from 'date-fns';
import { useAppDispatch, useAppSelector } from '../../store';
import {
  fetchMessages,
  sendMessage,
  markMessagesAsRead,
} from '../../store/slices/messagesSlice';
import { clearUnreadCount } from '../../store/slices/conversationsSlice';
import { AttachmentView } from './AttachmentView';
import type { Message } from '../../types';
import './MessageThread.css';

export const MessageThread: React.FC = () => {
  const dispatch = useAppDispatch();
  const { selectedConversationId, conversations } = useAppSelector(
    (state) => state.conversations
  );
  const { messagesByConversation, loading, sending } = useAppSelector((state) => state.messages);

  const [messageText, setMessageText] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const selectedConversation = conversations.find((c) => c.id === selectedConversationId);
  const messages = selectedConversationId ? messagesByConversation[selectedConversationId] || [] : [];

  // Load messages when conversation is selected
  useEffect(() => {
    if (selectedConversationId) {
      dispatch(fetchMessages({ conversationId: selectedConversationId, page: 1 }));
    }
  }, [selectedConversationId, dispatch]);

  // Auto-scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Mark messages as read when viewing
  useEffect(() => {
    if (selectedConversationId && messages.length > 0) {
      const unreadMessageIds = messages.filter((m) => !m.read && !m.isMe).map((m) => m.id);

      if (unreadMessageIds.length > 0) {
        dispatch(markMessagesAsRead({ conversationId: selectedConversationId, messageIds: unreadMessageIds }));
        dispatch(clearUnreadCount(selectedConversationId));
      }
    }
  }, [selectedConversationId, messages, dispatch]);

  const handleSend = async (e: FormEvent) => {
    e.preventDefault();

    if (!messageText.trim() || !selectedConversation) {
      return;
    }

    const addresses = selectedConversation.recipients.map((r) => r.address);

    await dispatch(
      sendMessage({
        conversationId: selectedConversationId!,
        addresses,
        body: messageText,
      })
    );

    setMessageText('');
  };

  const formatMessageTime = (dateString: string): string => {
    try {
      const date = new Date(parseInt(dateString));
      return format(date, 'MMM d, h:mm a');
    } catch {
      return '';
    }
  };

  const getConversationName = (): string => {
    if (!selectedConversation) return '';
    if (selectedConversation.name) return selectedConversation.name;
    if (selectedConversation.recipients.length === 0) return 'Unknown';
    if (selectedConversation.recipients.length === 1) {
      return selectedConversation.recipients[0].name || selectedConversation.recipients[0].address;
    }
    return selectedConversation.recipients
      .map((r) => r.name || r.address)
      .join(', ');
  };

  if (!selectedConversationId) {
    return (
      <div className="message-thread">
        <div className="message-thread-empty">
          <h3>No conversation selected</h3>
          <p>Select a conversation from the list to view messages</p>
        </div>
      </div>
    );
  }

  return (
    <div className="message-thread">
      <div className="message-thread-header">
        <div className="conversation-info">
          <h3>{getConversationName()}</h3>
          <div className="recipient-addresses">
            {selectedConversation?.recipients.map((r) => r.address).join(', ')}
          </div>
        </div>
      </div>

      <div className="message-thread-messages">
        {loading && messages.length === 0 ? (
          <div className="messages-loading">Loading messages...</div>
        ) : messages.length === 0 ? (
          <div className="messages-empty">
            <p>No messages in this conversation</p>
          </div>
        ) : (
          <>
            {messages.map((message: Message) => (
              <div
                key={message.id}
                className={`message ${message.isMe ? 'message-sent' : 'message-received'}`}
              >
                <div className="message-bubble">
                  {message.attachments && message.attachments.length > 0 && (
                    <div className="message-attachments">
                      {message.attachments.map((attachment) => (
                        <AttachmentView key={attachment.id} attachment={attachment} />
                      ))}
                    </div>
                  )}
                  {message.body && <div className="message-body">{message.body}</div>}
                  <div className="message-time">{formatMessageTime(message.date)}</div>
                </div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      <form className="message-thread-compose" onSubmit={handleSend}>
        <input
          type="text"
          value={messageText}
          onChange={(e) => setMessageText(e.target.value)}
          placeholder="Type a message..."
          disabled={sending}
          className="compose-input"
        />
        <button type="submit" disabled={sending || !messageText.trim()} className="compose-send">
          {sending ? 'Sending...' : 'Send'}
        </button>
      </form>
    </div>
  );
};
