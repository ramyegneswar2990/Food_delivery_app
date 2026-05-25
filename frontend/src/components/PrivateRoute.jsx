import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();

  // Show nothing while auth state is being restored
  if (loading) {
    return (
      <div className="spinner-overlay" style={{ minHeight: '100vh' }}>
        <div className="spinner" aria-label="Loading…" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
