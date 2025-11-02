import { createTheme } from '@mui/material/styles';

// Material Design 3 principles with custom branding
// Tonal surfaces, large corners, no shadows, expressive typography

export const lightTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#6fd7a0', // Pale mint green for search/primary actions
      light: '#8fe5b5',
      dark: '#4cc78a',
    },
    secondary: {
      main: '#a8d5f5', // Pale blue for deluge/secondary actions
      light: '#c0e3f9',
      dark: '#7bc3f0',
    },
    background: {
      default: '#faf8f5', // Warm cream background
      paper: '#ffffff',   // Pure white for elevated surfaces
    },
    text: {
      primary: 'rgba(0, 0, 0, 0.87)',   // MD3 standard on-surface
      secondary: 'rgba(0, 0, 0, 0.60)', // MD3 standard on-surface variant
    },
  },
  shape: {
    borderRadius: 28, // MD3: Extra large corner radius
  },
  typography: {
    fontFamily: '"Work Sans", "Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    fontWeightRegular: 400,
    fontWeightMedium: 500,
    fontWeightBold: 600,
    h1: {
      fontWeight: 600,
      letterSpacing: '-0.01em',
      lineHeight: 1.2,
    },
    h2: {
      fontWeight: 600,
      letterSpacing: '-0.01em',
      lineHeight: 1.3,
    },
    h3: {
      fontWeight: 400, // Regular for tile text
      letterSpacing: '0em',
    },
    h4: {
      fontWeight: 500,
      letterSpacing: '0em',
    },
    h5: {
      fontWeight: 500,
      letterSpacing: '0em',
    },
    h6: {
      fontWeight: 500,
      letterSpacing: '0em',
    },
    body1: {
      fontWeight: 400,
      lineHeight: 1.6,
      letterSpacing: '0em',
    },
    body2: {
      fontWeight: 400,
      lineHeight: 1.5,
      letterSpacing: '0em',
    },
    button: {
      fontWeight: 500,
      letterSpacing: '0.02em',
      textTransform: 'none',
    },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 28, // MD3 large corners
          boxShadow: 'none', // MD3: No shadows, use tonal surfaces
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', // MD3 standard easing
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 20, // MD3 full corner radius for buttons
          textTransform: 'none',
          fontWeight: 500,
          padding: '12px 24px',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        },
      },
    },
  },
});

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#6fd7a0',
      light: '#8fe5b5',
      dark: '#4cc78a',
    },
    secondary: {
      main: '#a8d5f5',
      light: '#c0e3f9',
      dark: '#7bc3f0',
    },
    background: {
      default: '#1a1c1e', // MD3 dark surface
      paper: '#2b2d30',   // MD3 dark surface variant
    },
    text: {
      primary: 'rgba(255, 255, 255, 0.87)',   // MD3 on-dark-surface
      secondary: 'rgba(255, 255, 255, 0.60)', // MD3 on-dark-surface variant
    },
  },
  shape: {
    borderRadius: 28,
  },
  typography: {
    fontFamily: '"Work Sans", "Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    fontWeightRegular: 400,
    fontWeightMedium: 500,
    fontWeightBold: 600,
    h1: {
      fontWeight: 600,
      letterSpacing: '-0.01em',
      lineHeight: 1.2,
    },
    h2: {
      fontWeight: 600,
      letterSpacing: '-0.01em',
      lineHeight: 1.3,
    },
    h3: {
      fontWeight: 400,
      letterSpacing: '0em',
    },
    h4: {
      fontWeight: 500,
      letterSpacing: '0em',
    },
    h5: {
      fontWeight: 500,
      letterSpacing: '0em',
    },
    h6: {
      fontWeight: 500,
      letterSpacing: '0em',
    },
    body1: {
      fontWeight: 400,
      lineHeight: 1.6,
      letterSpacing: '0em',
    },
    body2: {
      fontWeight: 400,
      lineHeight: 1.5,
      letterSpacing: '0em',
    },
    button: {
      fontWeight: 500,
      letterSpacing: '0.02em',
      textTransform: 'none',
    },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 28,
          boxShadow: 'none',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 20,
          textTransform: 'none',
          fontWeight: 500,
          padding: '12px 24px',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        },
      },
    },
  },
});

// Export light theme as default
export const theme = lightTheme;
