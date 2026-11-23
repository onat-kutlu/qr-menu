import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../lib/auth-store';

export function ProtectedRoute() {
  const session = useAuthStore((state) => state.session);
  const location = useLocation();

  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
