import { Card, CardContent, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useNavigation } from '../contexts/NavigationContext';

interface NavTileProps {
  title: string;
  path: string;
  color?: string;
  borderRadius?: string;
}

export const NavTile = ({ title, path, color = '#6fd7a0', borderRadius = '32px' }: NavTileProps) => {
  const navigate = useNavigate();
  const { addNavItem } = useNavigation();

  const handleClick = () => {
    addNavItem({ title, path, color, borderRadius });
    // Small delay for the animation to start before navigation
    setTimeout(() => {
      navigate(path);
    }, 100);
  };

  return (
    <Card
      onClick={handleClick}
      sx={{
        cursor: 'pointer',
        aspectRatio: '1', // MD3 Square: equal width and height
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        textAlign: 'center',
        backgroundColor: color,
        borderRadius: borderRadius, // Customizable border radius
        border: 'none',
        boxShadow: 'none', // MD3: No shadows, elevation through color
        position: 'relative',
        overflow: 'hidden',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', // MD3 standard easing
        // MD3: Subtle surface tint
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 100%)',
          pointerEvents: 'none',
          transition: 'opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        },
        // MD3: State layer on hover (tonal overlay)
        '&::after': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.08)', // MD3 hover state layer
          opacity: 0,
          pointerEvents: 'none',
          transition: 'opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        },
        '&:hover': {
          transform: 'translateY(-2px)', // Subtle lift
          '&::after': {
            opacity: 1, // Show state layer on hover
          },
        },
        '&:active': {
          transform: 'translateY(0)', // Press down effect
          '&::after': {
            background: 'rgba(0, 0, 0, 0.12)', // Pressed state
          },
        },
      }}
    >
      <CardContent sx={{ position: 'relative', zIndex: 1 }}>
        <Typography
          variant="h3"
          component="h2"
          sx={{
            fontSize: '2.75rem',
            fontWeight: 400,
            color: 'rgba(0, 0, 0, 0.75)', // Slightly lighter than MD3 standard
          }}
        >
          {title}
        </Typography>
      </CardContent>
    </Card>
  );
};
