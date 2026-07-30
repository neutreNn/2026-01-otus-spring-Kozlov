import { useMemo, useState } from 'react';
import {
  AppBar,
  Avatar,
  Box,
  Button,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Stack,
  Toolbar,
  Typography,
  useMediaQuery,
} from '@mui/material';
import { alpha, useTheme } from '@mui/material/styles';
import { Link as RouterLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import {
  logout,
  selectCurrentUser,
  selectIsAdmin,
  selectRefreshToken,
} from '../features/auth/authSlice';
import { useLogoutMutation } from '../features/auth/authApi';
import {
  AuditIcon,
  FilesIcon,
  MenuIcon,
  NotesIcon,
  ProfileIcon,
  SharesIcon,
  StatsIcon,
  UsersIcon,
} from './icons';

const drawerWidth = 264;

const primaryNavigation = [
  { label: 'Files', path: '/files', icon: <FilesIcon /> },
  { label: 'Notes', path: '/notes', icon: <NotesIcon /> },
  { label: 'Shares', path: '/shares', icon: <SharesIcon /> },
  { label: 'Audit', path: '/audit', icon: <AuditIcon /> },
  { label: 'Profile', path: '/profile', icon: <ProfileIcon /> },
];

const adminNavigation = [
  { label: 'Admin users', path: '/admin/users', icon: <UsersIcon /> },
  { label: 'Admin stats', path: '/admin/stats', icon: <StatsIcon /> },
];

export function AppLayout() {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const currentUser = useAppSelector(selectCurrentUser);
  const refreshToken = useAppSelector(selectRefreshToken);
  const isAdmin = useAppSelector(selectIsAdmin);
  const [logoutRequest] = useLogoutMutation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [userMenuAnchor, setUserMenuAnchor] = useState<null | HTMLElement>(null);

  const navigation = useMemo(
    () => (isAdmin ? [...primaryNavigation, ...adminNavigation] : primaryNavigation),
    [isAdmin],
  );

  const handleLogout = async () => {
    setUserMenuAnchor(null);

    try {
      if (refreshToken) {
        await logoutRequest({ refreshToken }).unwrap();
      }
    } catch {
      // Logout must always clear local state even if the token was already invalidated.
    } finally {
      dispatch(logout());
      navigate('/login', { replace: true });
    }
  };

  const drawer = (
    <Stack sx={{ height: '100%' }}>
      <Toolbar sx={{ minHeight: { xs: 64, md: 72 }, px: 3 }}>
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <Box
            aria-hidden="true"
            sx={{
              width: 36,
              height: 36,
              borderRadius: 2,
              display: 'grid',
              placeItems: 'center',
              bgcolor: 'primary.main',
              color: 'primary.contrastText',
              fontWeight: 800,
              fontSize: 14,
            }}
          >
            HV
          </Box>
          <Box>
            <Typography variant="h3" component="p" sx={{ fontSize: 18 }}>
              HomeVault
            </Typography>
            <Typography variant="caption" color="text.secondary">
              self-hosted workspace
            </Typography>
          </Box>
        </Stack>
      </Toolbar>

      <Divider />

      <List sx={{ px: 1.5, py: 2 }}>
        {navigation.map((item) => {
          const selected =
            location.pathname === item.path || location.pathname.startsWith(`${item.path}/`);

          return (
            <ListItemButton
              key={item.path}
              component={RouterLink}
              to={item.path}
              selected={selected}
              onClick={() => setMobileOpen(false)}
              sx={{
                mb: 0.5,
                borderRadius: 2,
                color: selected ? 'primary.dark' : 'text.secondary',
                '&.Mui-selected': {
                  color: 'primary.dark',
                  bgcolor: alpha(theme.palette.primary.main, 0.1),
                },
                '&.Mui-selected:hover': {
                  bgcolor: alpha(theme.palette.primary.main, 0.14),
                },
              }}
            >
              <ListItemIcon
                sx={{
                  minWidth: 40,
                  color: 'inherit',
                }}
              >
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{
                  fontSize: 14,
                  fontWeight: selected ? 700 : 600,
                }}
              />
            </ListItemButton>
          );
        })}
      </List>

      <Box sx={{ flexGrow: 1 }} />

      <Box sx={{ p: 2 }}>
        <Box
          sx={{
            p: 1.5,
            border: 1,
            borderColor: 'divider',
            borderRadius: 2,
            bgcolor: 'background.paper',
          }}
        >
          <Typography variant="body2" fontWeight={700} noWrap>
            {currentUser?.displayName ?? 'User'}
          </Typography>
          <Typography variant="caption" color="text.secondary" noWrap>
            {currentUser?.email ?? 'user@example.com'}
          </Typography>
        </Box>
      </Box>
    </Stack>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{
          borderBottom: 1,
          borderColor: 'divider',
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
          bgcolor: alpha(theme.palette.background.paper, 0.94),
          backdropFilter: 'blur(12px)',
        }}
      >
        <Toolbar sx={{ minHeight: { xs: 64, md: 72 }, gap: 2 }}>
          {!isDesktop && (
            <IconButton
              color="inherit"
              edge="start"
              aria-label="Открыть меню"
              onClick={() => setMobileOpen(true)}
            >
              <MenuIcon />
            </IconButton>
          )}

          <Box sx={{ minWidth: 0, flexGrow: 1 }}>
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ display: { xs: 'none', sm: 'block' } }}
            >
              HomeVault
            </Typography>
            <Typography variant="h2" sx={{ fontSize: { xs: 20, sm: 24 } }} noWrap>
              {isDesktop ? 'Рабочее пространство' : 'HomeVault'}
            </Typography>
          </Box>

          <Button
            variant="text"
            color="inherit"
            onClick={(event) => setUserMenuAnchor(event.currentTarget)}
            startIcon={
              <Avatar
                sx={{
                  width: 32,
                  height: 32,
                  bgcolor: 'primary.light',
                  color: 'primary.dark',
                  fontSize: 13,
                  fontWeight: 800,
                }}
              >
                {(currentUser?.displayName ?? 'U').slice(0, 1).toUpperCase()}
              </Avatar>
            }
            sx={{ minWidth: 0, px: { xs: 1, sm: 1.5 } }}
          >
            <Box sx={{ display: { xs: 'none', sm: 'block' }, textAlign: 'left' }}>
              <Typography variant="body2" component="span" fontWeight={700} noWrap>
                {currentUser?.displayName ?? 'User'}
              </Typography>
            </Box>
          </Button>
          <Menu
            anchorEl={userMenuAnchor}
            open={Boolean(userMenuAnchor)}
            onClose={() => setUserMenuAnchor(null)}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
          >
            <MenuItem component={RouterLink} to="/profile" onClick={() => setUserMenuAnchor(null)}>
              Профиль
            </MenuItem>
            <MenuItem onClick={handleLogout}>Выйти</MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': {
              width: drawerWidth,
              boxSizing: 'border-box',
            },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          open
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': {
              width: drawerWidth,
              boxSizing: 'border-box',
              borderRightColor: 'divider',
            },
          }}
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          pt: { xs: 10, md: 11 },
          px: { xs: 2, sm: 3, lg: 4 },
          pb: 4,
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
}
