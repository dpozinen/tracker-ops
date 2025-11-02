import { motion } from 'framer-motion';
import { Box, Typography } from '@mui/material';

interface AnimatedTileProps {
  id: string;
  title: string;
  color: string;
  borderRadius: string;
  onClick?: () => void;
  isNavbar?: boolean;
  onLayoutAnimationComplete?: () => void;
}

export const AnimatedTile = ({ id, title, color, borderRadius, onClick, isNavbar = false, onLayoutAnimationComplete }: AnimatedTileProps) => {
  const isZoe = id === 'zoe';

  // Scale border radius proportionally when in navbar
  const getScaledBorderRadius = () => {
    if (!isNavbar) return borderRadius;

    // If it's a percentage, keep it as percentage (scales automatically)
    if (borderRadius.includes('%')) return borderRadius;

    // For pixel values, scale down proportionally (280px -> 60px ≈ 21.4%)
    const numericRadius = parseInt(borderRadius);
    const scaledRadius = Math.round(numericRadius * 0.214); // 60/280
    return `${scaledRadius}px`;
  };

  return (
    <motion.div
      layoutId={id}
      onClick={onClick}
      onLayoutAnimationComplete={onLayoutAnimationComplete}
      style={{
        width: isNavbar ? (isZoe ? 'auto' : '60px') : (isZoe ? 'auto' : '100%'),
        height: isNavbar ? (isZoe ? 'auto' : '60px') : (isZoe ? 'auto' : '100%'),
        backgroundColor: color,
        borderRadius: getScaledBorderRadius(),
        cursor: 'pointer',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        overflow: 'hidden',
        padding: isZoe ? (isNavbar ? '8px 16px' : '16px 32px') : 0,
      }}
      transition={{
        layout: {
          type: 'spring',
          stiffness: 200,
          damping: 25,
        },
        backgroundColor: { duration: 0 },
        borderRadius: { duration: 0 },
      }}
      whileHover={!isNavbar ? { scale: 0.98 } : {}}
      whileTap={{ scale: 0.95 }}
    >
      {/* Surface tint overlay - skip for ZOE */}
      {!isZoe && (
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 100%)',
            pointerEvents: 'none',
          }}
        />
      )}

      {/* Text */}
      <Typography
        sx={{
          fontSize: isZoe
            ? (isNavbar ? '1.5rem' : 'clamp(5rem, 15vw, 10rem)')
            : (isNavbar ? '0.875rem' : { xs: '1.5rem', sm: '2rem', md: '2.75rem' }),
          fontWeight: isZoe ? 700 : 400,
          color: 'rgba(0, 0, 0, 0.87)',
          textAlign: 'center',
          fontFamily: isZoe ? '"Space Grotesk", sans-serif' : '"Work Sans", sans-serif',
          letterSpacing: isZoe ? '-0.02em' : 'normal',
          position: 'relative',
          zIndex: 1,
        }}
      >
        {title}
      </Typography>
    </motion.div>
  );
};
