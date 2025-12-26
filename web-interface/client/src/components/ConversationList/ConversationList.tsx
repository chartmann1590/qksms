import React, { useEffect, useState } from 'react';
import { formatDistanceToNow } from 'date-fns';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store';
import { fetchConversations, selectConversation } from '../../store/slices/conversationsSlice';
import { logout } from '../../store/slices/authSlice';
import type { Conversation } from '../../types';
import './ConversationList.css';

export const ConversationList: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { conversations, selectedConversationId, loading } = useAppSelector(
    (state) => state.conversations
  );
  const [searchQuery, setSearchQuery] = useState('');

  const handleLogout = async () => {
    await dispatch(logout());
    navigate('/login');
  };

  const handleSettings = () => {
    navigate('/settings');
  };

  useEffect(() => {
    dispatch(fetchConversations({ page: 1 }));
  }, [dispatch]);

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      dispatch(fetchConversations({ page: 1, search: searchQuery }));
    }, 300); // Debounce search

    return () => clearTimeout(timeoutId);
  }, [searchQuery, dispatch]);

  const handleSelectConversation = (conversationId: string) => {
    dispatch(selectConversation(conversationId));
  };

  const getConversationName = (conversation: Conversation): string => {
    if (conversation.name) {
      return conversation.name;
    }
    if (conversation.recipients.length === 0) {
      return 'Unknown';
    }
    if (conversation.recipients.length === 1) {
      return conversation.recipients[0].name || conversation.recipients[0].address;
    }
    return conversation.recipients
      .map((r) => r.name || r.address)
      .slice(0, 2)
      .join(', ') + (conversation.recipients.length > 2 ? ` +${conversation.recipients.length - 2}` : '');
  };

  const formatDate = (dateString?: string): string => {
    if (!dateString) return '';
    try {
      return formatDistanceToNow(new Date(parseInt(dateString)), { addSuffix: true });
    } catch {
      return '';
    }
  };

  if (loading && conversations.length === 0) {
    return (
      <div className="conversation-list">
        <div className="conversation-list-header">
          <h2>Messages</h2>
        </div>
        <div className="conversation-list-loading">Loading conversations...</div>
      </div>
    );
  }

  return (
    <div className="conversation-list">
      <div className="conversation-list-header">
        <div className="header-content">
          <div>
            <h2>Messages</h2>
            <div className="conversation-count">{conversations.length} conversations</div>
          </div>
          <div className="header-buttons">
            <button className="header-button" onClick={handleSettings} title="Settings">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="3"></circle>
                <path d="M12 1v6m0 6v6m9-9h-6m-6 0H3"></path>
                <path d="M19.4 15.4l-4.2-4.2m-6.4 0L4.6 15.4m10.6 4.2l-4.2-4.2m-6.4 0L4.6 8.6"></path>
              </svg>
            </button>
            <button className="header-button" onClick={handleLogout} title="Logout">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <div className="conversation-search">
        <input
          type="text"
          placeholder="Search conversations, contacts, or phone numbers..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="search-input"
        />
      </div>

      <div className="conversation-list-items">
        {conversations.length === 0 ? (
          <div className="conversation-list-empty">
            <p>No conversations yet</p>
            <p className="empty-subtitle">
              Messages will appear here when they're synced from your phone
            </p>
          </div>
        ) : (
          conversations.map((conversation) => (
            <div
              key={conversation.id}
              className={`conversation-item ${
                selectedConversationId === conversation.id ? 'selected' : ''
              } ${conversation.unreadCount > 0 ? 'unread' : ''}`}
              onClick={() => handleSelectConversation(conversation.id)}
            >
              <div className="conversation-avatar">
                {getConversationName(conversation).charAt(0).toUpperCase()}
              </div>

              <div className="conversation-content">
                <div className="conversation-header">
                  <div className="conversation-name">{getConversationName(conversation)}</div>
                  <div className="conversation-date">{formatDate(conversation.lastMessageDate)}</div>
                </div>

                <div className="conversation-preview">
                  <div className="conversation-last-message">
                    {conversation.lastMessage?.body || 'No messages'}
                  </div>
                  {conversation.unreadCount > 0 && (
                    <div className="conversation-unread-badge">{conversation.unreadCount}</div>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
