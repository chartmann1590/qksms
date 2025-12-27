import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch } from '../../store';
import { logout } from '../../store/slices/authSlice';
import { apiClient } from '../../services/api';
import type { SyncStatus } from '../../types';
import { AiSettings } from './AiSettings';
import './Settings.css';

export const Settings: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [isWiping, setIsWiping] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [syncStatus, setSyncStatus] = useState<SyncStatus | null>(null);
  const [isLoadingSync, setIsLoadingSync] = useState(true);

  useEffect(() => {
    fetchSyncStatus();
  }, []);

  const fetchSyncStatus = async () => {
    try {
      const data = await apiClient.getSyncStatus();
      setSyncStatus(data);
    } catch (error) {
      console.error('Error fetching sync status:', error);
    } finally {
      setIsLoadingSync(false);
    }
  };

  const formatDate = (dateString: string | null): string => {
    if (!dateString) return 'Never';
    try {
      const date = new Date(dateString);
      return date.toLocaleString();
    } catch (error) {
      return 'Invalid date';
    }
  };

  const handleLogout = async () => {
    await dispatch(logout());
    navigate('/login');
  };

  const handleWipeDatabase = async () => {
    setShowConfirmDialog(false);
    setIsWiping(true);

    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/settings/wipe-database', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        alert('Database wiped successfully! You can now perform a full sync from your phone.');
        // Refresh sync status to show reset state
        await fetchSyncStatus();
      } else {
        const error = await response.json();
        alert(`Failed to wipe database: ${error.error || 'Unknown error'}`);
      }
    } catch (error) {
      console.error('Error wiping database:', error);
      alert('Failed to wipe database. Please try again.');
    } finally {
      setIsWiping(false);
    }
  };

  return (
    <div className="settings-container">
      <div className="settings-header">
        <button className="back-button" onClick={() => navigate('/')}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h1>Settings</h1>
      </div>

      <div className="settings-content">
        <section className="settings-section">
          <h2>Sync Log</h2>
          {isLoadingSync ? (
            <div className="settings-item">
              <p>Loading sync information...</p>
            </div>
          ) : (
            <>
              <div className="settings-item">
                <div className="settings-item-content">
                  <h3>Last Full Sync</h3>
                  <p>{formatDate(syncStatus?.lastFullSync || null)}</p>
                </div>
              </div>
              <div className="settings-item">
                <div className="settings-item-content">
                  <h3>Last Incremental Sync</h3>
                  <p>{formatDate(syncStatus?.lastIncrementalSync || null)}</p>
                </div>
              </div>
              <div className="settings-item">
                <div className="settings-item-content">
                  <h3>Sync Status</h3>
                  <p>
                    {syncStatus?.syncInProgress ? (
                      <span className="sync-status-active">Sync in progress...</span>
                    ) : (
                      <span className="sync-status-idle">Idle</span>
                    )}
                  </p>
                </div>
              </div>
              <div className="settings-item">
                <div className="settings-item-content">
                  <h3>Synced Data</h3>
                  <p>
                    {syncStatus?.conversationCount || 0} conversations, {syncStatus?.messageCount || 0} messages
                  </p>
                </div>
              </div>
            </>
          )}
        </section>

        <section className="settings-section">
          <h2>Database Management</h2>
          <div className="settings-item">
            <div className="settings-item-content">
              <h3>Wipe Database</h3>
              <p>
                Delete all conversations and messages from the web server.
                This will allow you to perform a fresh full sync from your phone.
              </p>
              <p className="warning-text">
                <strong>Warning:</strong> This action cannot be undone! All message data will be permanently deleted from the server.
              </p>
            </div>
            <button
              className="danger-button"
              onClick={() => setShowConfirmDialog(true)}
              disabled={isWiping}
            >
              {isWiping ? 'Wiping...' : 'Wipe Database'}
            </button>
          </div>
        </section>

        <AiSettings />

        <section className="settings-section">
          <h2>Account</h2>
          <div className="settings-item">
            <div className="settings-item-content">
              <h3>Logout</h3>
              <p>Sign out of your account</p>
            </div>
            <button className="secondary-button" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </section>
      </div>

      {showConfirmDialog && (
        <div className="modal-overlay" onClick={() => setShowConfirmDialog(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Confirm Database Wipe</h2>
            <p>
              Are you sure you want to delete all conversations and messages from the server?
            </p>
            <p className="warning-text">
              <strong>This action cannot be undone!</strong>
            </p>
            <div className="modal-buttons">
              <button
                className="secondary-button"
                onClick={() => setShowConfirmDialog(false)}
              >
                Cancel
              </button>
              <button
                className="danger-button"
                onClick={handleWipeDatabase}
              >
                Yes, Wipe Database
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
