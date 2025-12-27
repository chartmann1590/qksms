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
import { SmartReplies } from './SmartReplies';
import { apiClient } from '../../services/api';
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
  const [showSmartReplies, setShowSmartReplies] = useState(false);
  const [smartReplySuggestions, setSmartReplySuggestions] = useState<string[]>([]);
  const [loadingSmartReplies, setLoadingSmartReplies] = useState(false);

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

  const handleGenerateSmartReplies = async () => {
    if (!selectedConversationId) return;

    setLoadingSmartReplies(true);
    setShowSmartReplies(true);
    setSmartReplySuggestions([]);

    try {
      const response = await apiClient.generateSmartReplies(selectedConversationId);
      if (response.success && response.suggestions) {
        setSmartReplySuggestions(response.suggestions);
      } else {
        setSmartReplySuggestions([]);
      }
    } catch (error: any) {
      console.error('Error generating smart replies:', error);
      alert(`Failed to generate smart replies: ${error.response?.data?.error || error.message || 'Unknown error'}`);
      setSmartReplySuggestions([]);
    } finally {
      setLoadingSmartReplies(false);
    }
  };

  const handleSelectSuggestion = (suggestion: string) => {
    setMessageText(suggestion);
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

      {showSmartReplies && (
        <SmartReplies
          suggestions={smartReplySuggestions}
          isLoading={loadingSmartReplies}
          onSelect={handleSelectSuggestion}
          onClose={() => setShowSmartReplies(false)}
        />
      )}

      <form className="message-thread-compose" onSubmit={handleSend}>
        <button
          type="button"
          className="compose-ai-button"
          onClick={handleGenerateSmartReplies}
          disabled={loadingSmartReplies || !selectedConversationId}
          title="Generate smart replies"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </button>
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
