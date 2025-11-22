import { Container, Box } from '@mui/material';
import Grid from '@mui/material/Grid';
import { AnimatedTile } from '../components/AnimatedTile';
import { useNavigation } from '../contexts/NavigationContext';
import type { ViewType } from '../contexts/NavigationContext';
import { TILES } from '../constants/tiles';

export const Landing = () => {
  const { addNavItem, navItems, setCurrentView, currentView } = useNavigation();

  const handleTileClick = (clickedTile: typeof TILES[0]) => {
    // Add all tiles to navbar
    TILES.forEach(tile => {
      addNavItem({
        title: tile.title,
        path: tile.path,
        color: tile.color,
        borderRadius: tile.borderRadius,
      });
    });

    // Map path to view
    const viewMap: Record<string, ViewType> = {
      '/': 'landing',
      '/search': 'search',
      '/deluge': 'deluge',
    };

    // Change view
    setCurrentView(viewMap[clickedTile.path]);
  };

  const zoe = TILES.find(t => t.id === 'zoe');
  const otherTiles = TILES.filter(t => t.id !== 'zoe');

  const isLandingView = currentView === 'landing';
  const showTiles = navItems.length === 0;

  return (
    <Container
      maxWidth="lg"
      sx={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        zIndex: isLandingView ? 1 : -1,
        pointerEvents: showTiles ? 'auto' : 'none',
      }}
    >
      <Box sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        gap: { xs: 6, sm: 8, md: 12 },
      }}>
        {/* ZOE - centered above */}
        {showTiles && zoe && (
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <AnimatedTile
              id={zoe.id}
              title={zoe.title}
              color={zoe.color}
              borderRadius={zoe.borderRadius}
              onClick={() => handleTileClick(zoe)}
              isNavbar={false}
            />
          </Box>
        )}

        {/* Other tiles - in a row below */}
        {showTiles && (
          <Grid container spacing={3} justifyContent="center" sx={{ px: { xs: 2, sm: 0 } }}>
            {otherTiles.map((tile) => (
              <Grid
                size={{ xs: 6, sm: 6, md: "auto" }}
                key={tile.id}
                sx={{ display: 'flex', justifyContent: 'center' }}
              >
                <Box sx={{
                  width: { xs: '140px', sm: '200px', md: '280px' },
                  height: { xs: '140px', sm: '200px', md: '280px' },
                }}>
                  <AnimatedTile
                    id={tile.id}
                    title={tile.title}
                    color={tile.color}
                    borderRadius={tile.borderRadius}
                    onClick={() => handleTileClick(tile)}
                    isNavbar={false}
                  />
                </Box>
              </Grid>
            ))}
          </Grid>
        )}
      </Box>
    </Container>
  );
};
