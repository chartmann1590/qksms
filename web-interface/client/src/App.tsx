import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from './store';
import { useWebSocket } from './hooks/useWebSocket';
import { Login } from './components/Login/Login';
import { ConversationList } from './components/ConversationList/ConversationList';
import { MessageThread } from './components/MessageThread/MessageThread';
import { Settings } from './components/Settings/Settings';
import './App.css';

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAppSelector((state) => state.auth);
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

const MainLayout: React.FC = () => {
  // Initialize WebSocket connection
  useWebSocket();

  return (
    <div className="main-layout">
      <aside className="sidebar">
        <ConversationList />
      </aside>
      <main className="content">
        <MessageThread />
      </main>
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <MainLayout />
            </PrivateRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <PrivateRoute>
              <Settings />
            </PrivateRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};
