import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../store';
import { websocketClient } from '../services/websocket';
import { addMessage } from '../store/slices/messagesSlice';
import { updateConversation, incrementUnreadCount } from '../store/slices/conversationsSlice';
import type { Message } from '../types';

export function useWebSocket() {
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAppSelector((state) => state.auth);
  const { selectedConversationId } = useAppSelector((state) => state.conversations);
  const accessToken = localStorage.getItem('accessToken');

  // Request notification permission on mount
  useEffect(() => {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated && accessToken) {
      // Connect to WebSocket
      websocketClient.connect(accessToken);

      // Subscribe to events
      const unsubscribe = websocketClient.subscribe((event) => {
        switch (event.type) {
          case 'NEW_MESSAGE': {
            const message: Message = event.payload;
            dispatch(addMessage(message));

            // Increment unread count for conversation
            dispatch(incrementUnreadCount(message.conversationId));

            // Show notification if message is not from current conversation
            if (
              message.conversationId !== selectedConversationId &&
              'Notification' in window &&
              Notification.permission === 'granted'
            ) {
              new Notification('New Message', {
                body: message.body || 'You received a new message',
                icon: '/favicon.ico',
                tag: message.conversationId, // Group by conversation
              });
            }
            break;
          }

          case 'MESSAGE_SENT': {
            const { message } = event.payload;
            dispatch(addMessage(message));
            break;
          }

          case 'MESSAGE_STATUS_CHANGED': {
            const { messageIds, updates } = event.payload;
            // Update each message
            messageIds.forEach((id: string) => {
              // We don't have the full message, so we'll need to update via API
              // For now, just log it
              console.log('Message status changed:', id, updates);
            });
            break;
          }

          case 'CONVERSATION_UPDATED': {
            const conversation = event.payload;
            dispatch(updateConversation(conversation));
            break;
          }

          default:
            console.warn('Unknown WebSocket event type:', event.type);
        }
      });

      // Cleanup on unmount
      return () => {
        unsubscribe();
      };
    } else {
      // Disconnect if not authenticated
      websocketClient.disconnect();
    }
  }, [isAuthenticated, accessToken, dispatch]);
}
