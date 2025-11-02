import { CssBaseline, Box } from '@mui/material';
import { AnimatePresence } from 'framer-motion';
import { NavigationProvider, useNavigation } from './contexts/NavigationContext';
import { Navbar } from './components/Navbar';
import { Landing } from './pages/Landing';
import { TrackerSearch } from './pages/TrackerSearch';
import { Deluge } from './pages/Deluge';

function AppContent() {
  const { currentView } = useNavigation();

  return (
    <>
      {/* Landing page background - always rendered */}
      <Box sx={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, zIndex: 0 }}>
        {/* Abstract geometric background */}
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            pointerEvents: 'none',
            overflow: 'hidden',
          }}
        >
          {/* Organic blob - pale yellow */}
          <Box
            sx={{
              position: 'absolute',
              top: { xs: '-10%', sm: '-5%' },
              right: { xs: '-10%', sm: '0%' },
              width: { xs: '60%', sm: '50%', md: '38%' },
              height: { xs: '40%', sm: '45%', md: '50%' },
              backgroundColor: '#fff9c4',
              borderRadius: '40% 60% 70% 30% / 40% 50% 60% 50%',
              opacity: 0.4,
            }}
          />
          {/* Organic blob - pale red */}
          <Box
            sx={{
              position: 'absolute',
              bottom: { xs: '-10%', sm: '-5%' },
              left: { xs: '-10%', sm: '0%' },
              width: { xs: '60%', sm: '50%', md: '38%' },
              height: { xs: '35%', sm: '40%', md: '45%' },
              backgroundColor: '#ffcdd2',
              borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%',
              opacity: 0.4,
            }}
          />
        </Box>
      </Box>

      <Box sx={{ position: 'relative', minHeight: '100vh' }}>
        {/* Navbar */}
        <Navbar />

        {/* Content layer */}
        <Box sx={{ position: 'relative', zIndex: 1 }}>
          {/* Landing always rendered for tile animations */}
          <Landing />

          {/* Pages conditionally rendered with AnimatePresence for slide animations */}
          <AnimatePresence mode="wait" initial={false}>
            {currentView === 'search' && <TrackerSearch key="search" />}
            {currentView === 'deluge' && <Deluge key="deluge" />}
          </AnimatePresence>
        </Box>
      </Box>
    </>
  );
}

function App() {
  return (
    <NavigationProvider>
      <CssBaseline />
      <AppContent />
    </NavigationProvider>
  );
}

export default App;
