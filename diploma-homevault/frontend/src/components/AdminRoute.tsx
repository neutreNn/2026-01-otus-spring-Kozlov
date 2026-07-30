import { Navigate, Outlet } from 'react-router-dom';
import { selectIsAdmin } from '../features/auth/authSlice';
import { useAppSelector } from '../app/hooks';

export function AdminRoute() {
  const isAdmin = useAppSelector(selectIsAdmin);

  if (!isAdmin) {
    return <Navigate to="/files" replace />;
  }

  return <Outlet />;
}
