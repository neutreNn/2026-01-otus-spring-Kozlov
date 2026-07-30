import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { selectIsAuthenticated } from '../features/auth/authSlice';
import { useAppSelector } from '../app/hooks';

export function ProtectedRoute() {
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
}
