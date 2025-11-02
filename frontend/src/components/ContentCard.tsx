import { Paper, Box, type SxProps, type Theme } from '@mui/material';
import { type ReactNode } from 'react';

interface ContentCardProps {
  accentColor: string;     // Accent color for left border
  children?: ReactNode;    // Card content
  sx?: SxProps<Theme>;     // Additional sx props
}

/**
 * ContentCard - Reusable card component with accent color
 *
 * Features:
 * - Accent color border on the left edge
 * - Rounded corners matching main container
 * - Responsive sizing (1 col mobile, up to 3 cols desktop)
 * - Elevation through border instead of shadow
 *
 * Usage:
 * <ContentCard accentColor="#8fd9b8">
 *   <YourContent />
 * </ContentCard>
 */
export const ContentCard = ({ accentColor, children, sx }: ContentCardProps) => {
  return (
    <Paper
      elevation={0}
      sx={{
        position: 'relative',
        borderRadius: { xs: 1.5, md: 2.25 },      // Medium radius for cards
        border: '1px solid',
        borderColor: accentColor,                 // Use accent color for entire border
        backgroundColor: 'background.paper',
        overflow: 'hidden',
        transition: 'all 0.1s cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': {
          boxShadow: `inset 0 0 0 1px ${accentColor}`,  // Inner shadow simulates thicker border
        },
        ...sx,
      }}
    >
      {/* Card content */}
      <Box sx={{
        px: { xs: 3, md: 4 },                    // Responsive horizontal padding
        py: { xs: 3, md: 4 },                    // Responsive vertical padding
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}>
        {children}
      </Box>
    </Paper>
  );
};
