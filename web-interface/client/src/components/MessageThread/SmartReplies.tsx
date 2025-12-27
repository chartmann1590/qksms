import React from 'react';
import './SmartReplies.css';

interface SmartRepliesProps {
  suggestions: string[];
  isLoading: boolean;
  onSelect: (suggestion: string) => void;
  onClose: () => void;
}

export const SmartReplies: React.FC<SmartRepliesProps> = ({
  suggestions,
  isLoading,
  onSelect,
  onClose,
}) => {
  if (isLoading) {
    return (
      <div className="smart-replies-container">
        <div className="smart-replies-header">
          <h4>Generating Smart Replies...</h4>
          <button className="smart-replies-close" onClick={onClose}>×</button>
        </div>
        <div className="smart-replies-loading">
          <div className="loading-spinner"></div>
          <p>AI is thinking...</p>
        </div>
      </div>
    );
  }

  if (suggestions.length === 0) {
    return (
      <div className="smart-replies-container">
        <div className="smart-replies-header">
          <h4>Smart Replies</h4>
          <button className="smart-replies-close" onClick={onClose}>×</button>
        </div>
        <div className="smart-replies-empty">
          <p>No suggestions available</p>
        </div>
      </div>
    );
  }

  return (
    <div className="smart-replies-container">
      <div className="smart-replies-header">
        <h4>Smart Replies</h4>
        <button className="smart-replies-close" onClick={onClose}>×</button>
      </div>
      <div className="smart-replies-list">
        {suggestions.map((suggestion, index) => (
          <button
            key={index}
            className="smart-reply-item"
            onClick={() => {
              onSelect(suggestion);
              onClose();
            }}
          >
            {suggestion}
          </button>
        ))}
      </div>
    </div>
  );
};

