import React from 'react';
import type { Attachment } from '../../types';
import './AttachmentView.css';

interface AttachmentViewProps {
  attachment: Attachment;
}

export const AttachmentView: React.FC<AttachmentViewProps> = ({ attachment }) => {
  const apiUrl = import.meta.env.VITE_SERVER_URL || 'http://localhost:3000';
  const accessToken = localStorage.getItem('accessToken');

  const downloadUrl = `${apiUrl}/api/attachments/${attachment.id}?token=${accessToken}`;
  const thumbnailUrl = attachment.hasThumbnail
    ? `${apiUrl}/api/attachments/${attachment.id}/thumbnail?token=${accessToken}`
    : null;

  const isImage = attachment.mimeType.startsWith('image/');
  const isVideo = attachment.mimeType.startsWith('video/');
  const isAudio = attachment.mimeType.startsWith('audio/');

  const formatFileSize = (bytes?: number): string => {
    if (!bytes) return 'Unknown size';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const getFileTypeLabel = (): string => {
    if (isImage) return 'Image';
    if (isVideo) return 'Video';
    if (isAudio) return 'Audio';
    if (attachment.mimeType === 'application/pdf') return 'PDF';
    if (attachment.mimeType.startsWith('text/')) return 'Text';
    return 'File';
  };

  // Render image with thumbnail
  if (isImage) {
    return (
      <div className="attachment-view attachment-image">
        <a href={downloadUrl} target="_blank" rel="noopener noreferrer">
          <img
            src={thumbnailUrl || downloadUrl}
            alt="Attachment"
            className="attachment-thumbnail"
            loading="lazy"
          />
        </a>
      </div>
    );
  }

  // Render video
  if (isVideo) {
    return (
      <div className="attachment-view attachment-video">
        <video controls className="attachment-video-player" preload="metadata">
          <source src={downloadUrl} type={attachment.mimeType} />
          Your browser does not support video playback.
        </video>
        <div className="attachment-info">
          {formatFileSize(attachment.fileSize)}
        </div>
      </div>
    );
  }

  // Render audio
  if (isAudio) {
    return (
      <div className="attachment-view attachment-audio">
        <audio controls className="attachment-audio-player" preload="metadata">
          <source src={downloadUrl} type={attachment.mimeType} />
          Your browser does not support audio playback.
        </audio>
        <div className="attachment-info">
          {formatFileSize(attachment.fileSize)}
        </div>
      </div>
    );
  }

  // Render generic file
  return (
    <div className="attachment-view attachment-file">
      <a href={downloadUrl} download className="attachment-download">
        <div className="attachment-icon">📎</div>
        <div className="attachment-details">
          <div className="attachment-type">{getFileTypeLabel()}</div>
          <div className="attachment-size">{formatFileSize(attachment.fileSize)}</div>
        </div>
        <div className="attachment-action">Download</div>
      </a>
    </div>
  );
};
