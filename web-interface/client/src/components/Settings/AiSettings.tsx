import React, { useState, useEffect } from 'react';
import { apiClient } from '../../services/api';
import type { AiSettings as AiSettingsType, OllamaModel, TestConnectionResponse } from '../../types';
import './AiSettings.css';

export const AiSettings: React.FC = () => {
  const [settings, setSettings] = useState<AiSettingsType>({
    ollamaApiUrl: 'http://localhost:11434',
    ollamaModel: null,
    aiPersona: null,
    aiReplyEnabled: false,
  });
  const [availableModels, setAvailableModels] = useState<OllamaModel[]>([]);
  const [connectionStatus, setConnectionStatus] = useState<'unknown' | 'testing' | 'connected' | 'failed'>('unknown');
  const [connectionError, setConnectionError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isTesting, setIsTesting] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      setIsLoading(true);
      const data = await apiClient.getAiSettings();
      setSettings(data);
      
      // If there's a saved model and URL, automatically fetch models to show the dropdown
      if (data.ollamaModel && data.ollamaApiUrl) {
        try {
          const response: TestConnectionResponse = await apiClient.testOllamaConnection(data.ollamaApiUrl);
          if (response.success && response.models) {
            setAvailableModels(response.models);
            setConnectionStatus('connected');
          }
        } catch (error) {
          // Silently fail - user can test connection manually
          console.log('Could not auto-fetch models:', error);
        }
      }
    } catch (error) {
      console.error('Error loading AI settings:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTestConnection = async () => {
    const urlToTest = settings.ollamaApiUrl.trim();
    if (!urlToTest) {
      setConnectionError('Please enter an Ollama server URL');
      setConnectionStatus('failed');
      return;
    }

    setIsTesting(true);
    setConnectionStatus('testing');
    setConnectionError(null);

    try {
      const response: TestConnectionResponse = await apiClient.testOllamaConnection(urlToTest);
      
      if (response.success && response.models) {
        setAvailableModels(response.models);
        setConnectionStatus('connected');
        setConnectionError(null);
      } else {
        setConnectionStatus('failed');
        setConnectionError(response.error || 'Connection failed');
      }
    } catch (error: any) {
      setConnectionStatus('failed');
      setConnectionError(error.response?.data?.error || error.message || 'Connection failed');
    } finally {
      setIsTesting(false);
    }
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await apiClient.updateAiSettings(settings);
      alert('Settings saved successfully!');
    } catch (error: any) {
      console.error('Error saving settings:', error);
      alert(`Failed to save settings: ${error.response?.data?.error || error.message || 'Unknown error'}`);
    } finally {
      setIsSaving(false);
    }
  };

  const getConnectionStatusText = () => {
    switch (connectionStatus) {
      case 'testing':
        return 'Testing...';
      case 'connected':
        return `✓ Connected (${availableModels.length} models found)`;
      case 'failed':
        return '✗ Connection failed';
      default:
        return 'Not tested';
    }
  };

  const getConnectionStatusClass = () => {
    switch (connectionStatus) {
      case 'testing':
        return 'connection-status-testing';
      case 'connected':
        return 'connection-status-connected';
      case 'failed':
        return 'connection-status-failed';
      default:
        return 'connection-status-unknown';
    }
  };

  if (isLoading) {
    return (
      <section className="settings-section">
        <h2>AI Auto-Reply</h2>
        <div className="settings-item">
          <p>Loading settings...</p>
        </div>
      </section>
    );
  }

  return (
    <section className="settings-section">
      <h2>AI Auto-Reply</h2>

      <div className="ai-settings-content">
        {/* Enable/Disable Toggle */}
        <div className="settings-item">
          <div className="settings-item-content">
            <h3>Enable AI Replies</h3>
            <p>Enable AI-powered smart reply suggestions for messages</p>
          </div>
          <label className="toggle-switch">
            <input
              type="checkbox"
              checked={settings.aiReplyEnabled}
              onChange={(e) => setSettings({ ...settings, aiReplyEnabled: e.target.checked })}
            />
            <span className="toggle-slider"></span>
          </label>
        </div>

        {/* Ollama Server URL */}
        <div className="settings-item">
          <div className="settings-item-content">
            <h3>Ollama Server URL</h3>
            <p>Enter the URL of your Ollama server (e.g., http://localhost:11434)</p>
          </div>
          <input
            type="text"
            className="ai-input"
            value={settings.ollamaApiUrl}
            onChange={(e) => {
              setSettings({ ...settings, ollamaApiUrl: e.target.value });
              setConnectionStatus('unknown');
              setConnectionError(null);
            }}
            placeholder="http://localhost:11434"
          />
        </div>

        {/* Test Connection */}
        <div className="settings-item">
          <div className="settings-item-content">
            <h3>Connection Status</h3>
            <p className={getConnectionStatusClass()}>{getConnectionStatusText()}</p>
            {connectionError && (
              <p className="error-text">{connectionError}</p>
            )}
          </div>
          <button
            className="secondary-button"
            onClick={handleTestConnection}
            disabled={isTesting || !settings.ollamaApiUrl.trim()}
          >
            {isTesting ? 'Testing...' : 'Test Connection'}
          </button>
        </div>

        {/* Model Selection */}
        {(availableModels.length > 0 || settings.ollamaModel) && (
          <div className="settings-item">
            <div className="settings-item-content">
              <h3>Select Model</h3>
              <p>
                {settings.ollamaModel 
                  ? `Current model: ${settings.ollamaModel}` 
                  : 'Choose an AI model to use for generating replies'}
              </p>
            </div>
            <select
              className="ai-select"
              value={settings.ollamaModel || ''}
              onChange={(e) => setSettings({ ...settings, ollamaModel: e.target.value || null })}
            >
              <option value="">-- Select a model --</option>
              {availableModels.length > 0 ? (
                availableModels.map((model) => (
                  <option key={model.name} value={model.name}>
                    {model.name} ({(model.size / 1024 / 1024 / 1024).toFixed(2)} GB)
                  </option>
                ))
              ) : settings.ollamaModel ? (
                <option value={settings.ollamaModel}>{settings.ollamaModel}</option>
              ) : null}
            </select>
          </div>
        )}

        {/* Persona */}
        <div className="settings-item settings-item-full">
          <div className="settings-item-content">
            <h3>Persona (Optional)</h3>
            <p>Optional persona description to guide AI responses (e.g., "You are a friendly and professional assistant")</p>
          </div>
          <textarea
            className="ai-textarea"
            value={settings.aiPersona || ''}
            onChange={(e) => setSettings({ ...settings, aiPersona: e.target.value || null })}
            placeholder="Enter persona description (optional)"
            rows={4}
          />
        </div>

        {/* Save Button */}
        <div className="settings-item">
          <div className="settings-item-content">
            <h3>Save Settings</h3>
            <p>Save your AI configuration</p>
          </div>
          <button
            className="primary-button"
            onClick={handleSave}
            disabled={isSaving}
          >
            {isSaving ? 'Saving...' : 'Save Settings'}
          </button>
        </div>
      </div>
    </section>
  );
};

