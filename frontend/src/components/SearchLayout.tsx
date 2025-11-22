import { Paper, Box, Container } from '@mui/material';
import { motion } from 'framer-motion';
import { type ReactNode } from 'react';

/**
 * Theme configuration for SearchLayout
 * Allows different background colors for different pages (Search vs Deluge)
 */
export interface SearchLayoutTheme {
  background: string;    // Background color for search bar
  buttonColor: string;   // Accent color for buttons (currently unused)
}

interface SearchLayoutProps {
  theme: SearchLayoutTheme;
  searchBarContent: ReactNode;      // Search bar UI to be rendered
  resultsHeaderContent?: ReactNode; // Optional results header (sort controls, count, etc.)
  children: ReactNode;              // Results content area (scrollable)
  onAnimationComplete?: () => void; // Optional callback when page transition completes
}

/**
 * SearchLayout - Shared layout component for search pages (TrackerSearch, Deluge)
 *
 * Architecture:
 * - Fixed outer container (no page scrolling)
 * - Responsive padding for mobile/tablet/desktop
 * - Framer Motion animations for page transitions
 * - Only results area scrolls, search bar stays visible
 *
 * Layout Structure:
 * Box (fixed) → Container (padding) → motion.div (animation) → Paper (content)
 *   → Paper (search bar, flexShrink: 0)
 *   → Box (results, overflow: auto)
 *
 * IMPORTANT: motion.div MUST be inside Container for animations to work correctly
 * with the fixed positioning. See .claude/CLAUDE.md for detailed explanation.
 */
export const SearchLayout = ({ theme, searchBarContent, resultsHeaderContent, children, onAnimationComplete }: SearchLayoutProps) => {
  return (
    // Outer fixed container - prevents full-page scrolling
    <Box sx={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      pt: 10,                          // Top padding for navbar
      pb: { xs: 0.5, md: 2 },         // Bottom padding (smaller on mobile)
      overflow: 'hidden'               // Critical: no scrolling at this level
    }}>
      {/* Container with responsive padding */}
      <Container maxWidth="xl" sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        px: { xs: 0.5, sm: 3, md: 3 }  // 0.5 on mobile, 3 on tablet/desktop
      }}>
        {/* Motion wrapper for page transition animations
            CRITICAL: This must be inside Container to work with fixed positioning */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}    // Start: transparent, shifted down
          animate={{ opacity: 1, y: 0 }}     // End: visible, normal position
          exit={{ opacity: 0, y: 20 }}       // Exit: transparent, shift down (same direction!)
          transition={{
            duration: 0.3,                   // 300ms transition
            ease: [0.4, 0, 0.2, 1]          // Material Design 3 standard easing
          }}
          onAnimationComplete={onAnimationComplete}  // Notify parent when animation completes
          style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}
        >
          {/* Main content Paper */}
          <Paper
            elevation={0}
            sx={{
              p: { xs: 2, md: 4 },         // Responsive internal padding
              flex: 1,
              minHeight: 0,                // CRITICAL: allows flex child to shrink for scrolling
              display: 'flex',
              flexDirection: 'column',
              borderRadius: { xs: 2, md: 3 },  // Smaller radius on mobile
              border: '1px solid',
              borderColor: 'divider',
              backgroundColor: 'background.paper',
              overflow: 'hidden',          // Clip content to rounded corners
            }}
          >
            {/* Search bar area - stays visible at top */}
            <Paper
              elevation={0}
              sx={{
                p: { xs: 1.5, md: 3 },
                mb: { xs: 2, md: 3 },
                borderRadius: { xs: 2, md: 3 },
                backgroundColor: theme.background,  // Themed background (green/blue)
                border: '1px solid',
                borderColor: 'divider',
                flexShrink: 0,             // CRITICAL: prevents this from shrinking
              }}
            >
              {searchBarContent}
            </Paper>

            {/* Optional results header - stays visible, doesn't scroll */}
            {resultsHeaderContent && (
              <Box sx={{
                mb: { xs: 2, md: 3 },
                flexShrink: 0,             // CRITICAL: prevents this from shrinking
              }}>
                {resultsHeaderContent}
              </Box>
            )}

            {/* Results area - only this scrolls */}
            <Box sx={{
              flex: 1,                     // Takes remaining space
              overflow: 'auto',            // CRITICAL: enables scrolling for results
              minHeight: 0,                // CRITICAL: allows flex child to shrink below content size
              pb: 2,                       // Bottom padding for aesthetics
              // Hide scrollbar while keeping scroll functionality
              '&::-webkit-scrollbar': {
                display: 'none'
              },
              scrollbarWidth: 'none',      // Firefox
              msOverflowStyle: 'none',     // IE/Edge
            }}>
              {children}
            </Box>
          </Paper>
        </motion.div>
      </Container>
    </Box>
  );
};
