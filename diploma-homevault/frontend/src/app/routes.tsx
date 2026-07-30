import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminRoute } from '../components/AdminRoute';
import { AppLayout } from '../components/AppLayout';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { AdminStatsPage } from '../pages/AdminStatsPage';
import { AdminUsersPage } from '../pages/AdminUsersPage';
import { AuditPage } from '../pages/AuditPage';
import { FilesPage } from '../pages/FilesPage';
import { LoginPage } from '../pages/LoginPage';
import { NotesPage } from '../pages/NotesPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { ProfilePage } from '../pages/ProfilePage';
import { RegisterPage } from '../pages/RegisterPage';
import { SharesPage } from '../pages/SharesPage';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/files" replace />} />
          <Route path="/files" element={<FilesPage />} />
          <Route path="/notes" element={<NotesPage />} />
          <Route path="/shares" element={<SharesPage />} />
          <Route path="/audit" element={<AuditPage />} />
          <Route path="/profile" element={<ProfilePage />} />

          <Route element={<AdminRoute />}>
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/stats" element={<AdminStatsPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
