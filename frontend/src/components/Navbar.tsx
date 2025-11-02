import { AppBar, Toolbar, Box } from '@mui/material';
import { useRef, useEffect } from 'react';
import { AnimatedTile } from './AnimatedTile';
import { useNavigation } from '../contexts/NavigationContext';
import type { ViewType } from '../contexts/NavigationContext';
import { TILES } from '../constants/tiles';

export const Navbar = () => {
  const { navItems, clearNavItems, setCurrentView, setNavbarAnimationsComplete } = useNavigation();
  const completedAnimations = useRef(new Set<string>());

  // Reset animations complete when nav items change
  useEffect(() => {
    completedAnimations.current.clear();
    setNavbarAnimationsComplete(false);
  }, [navItems.length, setNavbarAnimationsComplete]);

  const handleTileAnimationComplete = (tileId: string) => {
    completedAnimations.current.add(tileId);

    // Check if all tiles have completed their animations
    if (completedAnimations.current.size === TILES.length) {
      setNavbarAnimationsComplete(true);
    }
  };

  const handleTileClick = (tile: typeof TILES[0]) => {
    // Map path to view
    const viewMap: Record<string, ViewType> = {
      '/': 'landing',
      '/search': 'search',
      '/deluge': 'deluge',
    };

    if (tile.path === '/') {
      // ZOE clicked - clear navbar and go to landing simultaneously
      clearNavItems();
      setCurrentView('landing');
    } else {
      // Other tile clicked - just change view
      setCurrentView(viewMap[tile.path]);
    }
  };

  const shouldShow = navItems.length > 0;

  return (
    <AppBar
      position="fixed"
      sx={{
        backgroundColor: 'rgba(0, 0, 0, 0)',
        boxShadow: 'none',
        pointerEvents: shouldShow ? 'auto' : 'none',
      }}
    >
      <Toolbar sx={{ gap: 2, py: 1 }}>
        {shouldShow && TILES.map((tile) => (
          <Box key={tile.id} sx={{ display: 'flex' }}>
            <AnimatedTile
              id={tile.id}
              title={tile.title}
              color={tile.color}
              borderRadius={tile.borderRadius}
              onClick={() => handleTileClick(tile)}
              isNavbar={true}
              onLayoutAnimationComplete={() => handleTileAnimationComplete(tile.id)}
            />
          </Box>
        ))}
      </Toolbar>
    </AppBar>
  );
};
