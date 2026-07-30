import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#177e89',
      dark: '#0f5f68',
      light: '#dff3f5',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#4056a1',
      dark: '#2f3f7a',
      light: '#e6eafd',
    },
    background: {
      default: '#f6f8fb',
      paper: '#ffffff',
    },
    text: {
      primary: '#17202a',
      secondary: '#5b6775',
    },
    divider: '#dce3ea',
    error: {
      main: '#c2413d',
    },
    warning: {
      main: '#b7791f',
    },
    success: {
      main: '#2f855a',
    },
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily:
      'Inter, Roboto, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    h1: {
      fontSize: '2rem',
      lineHeight: 1.2,
      fontWeight: 700,
      letterSpacing: 0,
    },
    h2: {
      fontSize: '1.5rem',
      lineHeight: 1.25,
      fontWeight: 700,
      letterSpacing: 0,
    },
    h3: {
      fontSize: '1.25rem',
      lineHeight: 1.3,
      fontWeight: 700,
      letterSpacing: 0,
    },
    body1: {
      lineHeight: 1.55,
      letterSpacing: 0,
    },
    body2: {
      lineHeight: 1.45,
      letterSpacing: 0,
    },
    button: {
      textTransform: 'none',
      fontWeight: 700,
      letterSpacing: 0,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          minHeight: 40,
          boxShadow: 'none',
        },
      },
      defaultProps: {
        disableElevation: true,
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        size: 'small',
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          minHeight: 44,
        },
      },
    },
  },
});
