import { Navigate, Outlet } from 'react-router-dom';
import { isPlatformAdmin, useAuthStore } from '../lib/auth-store';

export function PlatformRoute() {
  const session = useAuthStore((state) => state.session);
  if (!session) return <Navigate to="/login" replace />;
  if (!isPlatformAdmin(session.highestRole)) return <Navigate to="/dashboard/company" replace />;
  return <Outlet />;
}
