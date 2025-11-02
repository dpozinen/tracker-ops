import {useEffect, useMemo, useState} from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Grid,
  IconButton,
  InputAdornment,
  Menu,
  MenuItem,
  Snackbar,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import SettingsIcon from '@mui/icons-material/Settings';
import HistoryIcon from '@mui/icons-material/History';
import CloseIcon from '@mui/icons-material/Close';
import {AnimatePresence, motion} from 'framer-motion';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMagnet, faCopy, faGears } from '@fortawesome/free-solid-svg-icons';
import {useTrackerSearch} from '../hooks/useTrackerSearch';
import type {Torrent} from '../types/api';
import {Tracker} from '../types/api';
import {SearchLayout} from '../components/SearchLayout';
import {ContentCard} from '../components/ContentCard';
import {SortPills} from '../components/SortPills';

type SortField = 'seeds' | 'date' | 'size' | 'name';
type SortOrder = 'asc' | 'desc';


export const TrackerSearch = () => {
  const { results, isLoading, error, search, clearError, clearResults } = useTrackerSearch();

  // Load persisted search state from sessionStorage
  const loadSearchState = () => {
    try {
      const saved = sessionStorage.getItem('trackerSearchState');
      if (saved) {
        const parsed = JSON.parse(saved);
        return {
          keywords: parsed.keywords || '',
          selectedTracker: parsed.selectedTracker || Tracker.ONE_THREE_THREE,
          sortField: parsed.sortField || 'seeds',
          sortOrder: parsed.sortOrder || 'desc',
        };
      }
    } catch (e) {
      console.error('Failed to load search state:', e);
    }
    return {
      keywords: '',
      selectedTracker: Tracker.ONE_THREE_THREE,
      sortField: 'seeds' as SortField,
      sortOrder: 'desc' as SortOrder,
    };
  };

  const initialState = loadSearchState();
  const [keywords, setKeywords] = useState(initialState.keywords);
  const [selectedTracker, setSelectedTracker] = useState<Tracker>(initialState.selectedTracker);
  const [sortField, setSortField] = useState<SortField>(initialState.sortField);
  const [sortOrder, setSortOrder] = useState<SortOrder>(initialState.sortOrder);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [settingsAnchorEl, setSettingsAnchorEl] = useState<null | HTMLElement>(null);
  const [recentSearches, setRecentSearches] = useState<string[]>([]);
  const [sortExpanded, setSortExpanded] = useState(false);

  const settingsOpen = Boolean(settingsAnchorEl);

  // Persist search state to sessionStorage whenever it changes
  useEffect(() => {
    try {
      sessionStorage.setItem('trackerSearchState', JSON.stringify({
        keywords,
        selectedTracker,
        sortField,
        sortOrder,
      }));
    } catch (e) {
      console.error('Failed to save search state:', e);
    }
  }, [keywords, selectedTracker, sortField, sortOrder]);

  // Load recent searches from localStorage
  useEffect(() => {
    const saved = localStorage.getItem('recentSearches');
    if (saved) {
      try {
        setRecentSearches(JSON.parse(saved));
      } catch (e) {
        console.error('Failed to load recent searches:', e);
      }
    }
  }, []);

  // Save search to recent searches
  const addToRecentSearches = (query: string) => {
    const trimmed = query.trim();
    if (!trimmed) return;

    const updated = [trimmed, ...recentSearches.filter(s => s !== trimmed)].slice(0, 5);
    setRecentSearches(updated);
    localStorage.setItem('recentSearches', JSON.stringify(updated));
  };

  // Clear all recent searches
  const clearRecentSearches = () => {
    setRecentSearches([]);
    localStorage.removeItem('recentSearches');
  };

  const handleSearch = () => {
    if (keywords.trim()) {
      addToRecentSearches(keywords);
      search(selectedTracker, keywords);
    }
  };

  const handleRecentSearchClick = (query: string) => {
    setKeywords(query);
    addToRecentSearches(query);
    search(selectedTracker, query);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handleCopyMagnet = async (torrent: Torrent) => {
    try {
      // For now, copy the link. In real implementation, this would get the magnet link
      await navigator.clipboard.writeText(torrent.link);
      setSnackbarMessage(`Copied: ${torrent.name}`);
      setSnackbarOpen(true);
    } catch (err) {
      setSnackbarMessage('Failed to copy to clipboard');
      setSnackbarOpen(true);
    }
  };

  const handleDownload = (torrent: Torrent) => {
    // Open the torrent link in a new tab (magnet link)
    window.open(torrent.link, '_blank');
  };

  const handleAddToDeluge = async (torrent: Torrent) => {
    try {
      // POST magnet link to Deluge endpoint
      const response = await fetch('http://localhost:8133/deluge', {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain',
        },
        body: torrent.link, // Send magnet link as plain text
      });

      if (response.ok) {
        setSnackbarMessage(`Added to Deluge: ${torrent.name}`);
      } else {
        setSnackbarMessage('Failed to add to Deluge');
      }
    } catch (err) {
      setSnackbarMessage('Error connecting to Deluge');
    }
    setSnackbarOpen(true);
  };

  // Helper function to parse size string to bytes for proper sorting
  const parseSizeToBytes = (size: string): number => {
    const units: { [key: string]: number } = {
      'B': 1,
      'KB': 1024,
      'MB': 1024 * 1024,
      'GB': 1024 * 1024 * 1024,
      'TB': 1024 * 1024 * 1024 * 1024,
    };

    const match = size.match(/^([\d.]+)\s*([A-Z]+)$/i);
    if (!match) return 0;

    const value = parseFloat(match[1]);
    const unit = match[2].toUpperCase();

    return value * (units[unit] || 1);
  };

  // Sorting logic
  const sortedResults = useMemo(() => {
    if (!results.length) return [];

    return [...results].sort((a, b) => {
      let aVal: any;
      let bVal: any;

      switch (sortField) {
        case 'seeds':
          aVal = a.seeds;
          bVal = b.seeds;
          break;
        case 'name':
          aVal = a.name.toLowerCase();
          bVal = b.name.toLowerCase();
          break;
        case 'size':
          // Convert size to bytes for proper sorting
          aVal = parseSizeToBytes(a.size);
          bVal = parseSizeToBytes(b.size);
          break;
        case 'date':
          aVal = new Date(a.date).getTime();
          bVal = new Date(b.date).getTime();
          break;
        default:
          return 0;
      }

      if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
      return 0;
    });
  }, [results, sortField, sortOrder]);

  const toggleSortOrder = () => {
    setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc');
  };

  const handleSortFieldChange = (field: SortField) => {
    // Apply sort immediately (don't wait for animation)
    setSortField(field);
    // Collapse the pill
    setSortExpanded(false);
  };

  const sortOptions: { value: SortField; label: string }[] = [
    { value: 'seeds', label: 'Seeds' },
    { value: 'size', label: 'Size' },
    { value: 'date', label: 'Date' },
    { value: 'name', label: 'Name' },
  ];

  return (
    <>
      <SearchLayout
        theme={{
          background: '#d4f7e6',
          buttonColor: '#8fd9b8'
        }}
        resultsHeaderContent={
          sortedResults.length > 0 ? (
            <SortPills
              sortOptions={sortOptions}
              sortField={sortField}
              sortOrder={sortOrder.toUpperCase() as 'ASC' | 'DESC'}
              sortExpanded={sortExpanded}
              onSortFieldChange={handleSortFieldChange}
              onToggleSortOrder={toggleSortOrder}
              onSortExpandedChange={setSortExpanded}
              resultCount={sortedResults.length}
              colors={{
                container: '#d9f0dd',
                selected: '#8fd9b8',
                hover: '#7bc9a8',
              }}
            />
          ) : undefined
        }
        searchBarContent={
          <Box sx={{
            display: 'flex',
            gap: { xs: 1, md: 2 },
            alignItems: 'center',
          }}>
            <Tooltip title="Settings">
              <span>
                <IconButton
                  onClick={(e) => setSettingsAnchorEl(e.currentTarget)}
                  disabled={isLoading}
                  sx={{
                    height: { xs: 44, md: 56 },
                    width: { xs: 44, md: 56 },
                    flexShrink: 0,
                    backgroundColor: '#8fd9b8',
                    border: '1px solid',
                    borderColor: 'divider',
                    '&:hover': {
                      backgroundColor: '#7acca5',
                      borderColor: 'primary.main',
                    }
                  }}
                >
                  <SettingsIcon sx={{ fontSize: { xs: 20, md: 24 } }} />
                </IconButton>
              </span>
            </Tooltip>

            <TextField
              fullWidth
              placeholder="Search"
              value={keywords}
              onChange={(e) => setKeywords(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isLoading}
              slotProps={{
                input: {
                  endAdornment: keywords && (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => {
                          setKeywords('');
                          clearError();
                          clearResults();
                        }}
                        edge="end"
                        size="small"
                      >
                        <CloseIcon />
                      </IconButton>
                    </InputAdornment>
                  )
                }
              }}
            />

            <Tooltip title="Search">
              <span>
                <IconButton
                  onClick={handleSearch}
                  disabled={isLoading || !keywords.trim()}
                  sx={{
                    height: { xs: 44, md: 56 },
                    width: { xs: 44, md: 56 },
                    flexShrink: 0,
                    backgroundColor: '#8fd9b8',
                    border: '1px solid',
                    borderColor: 'divider',
                    '&:hover': {
                      backgroundColor: '#7acca5',
                      borderColor: 'primary.main',
                    },
                    '&.Mui-disabled': {
                      backgroundColor: '#c0e6d6',
                    }
                  }}
                >
                  {isLoading ? <CircularProgress size={20} /> : <SearchIcon sx={{ fontSize: { xs: 20, md: 24 } }} />}
                </IconButton>
              </span>
            </Tooltip>
          </Box>
        }
      >

        {/* Error Display */}
        {error && (
          <Alert severity="error" onClose={clearError} sx={{ mb: 3, borderRadius: 3 }}>
            {error.message}
          </Alert>
        )}

          {/* Results Grid - Card Layout */}
          {sortedResults.length > 0 && (
            <Box sx={{
              overflow: 'hidden',
              position: 'relative'
            }}>
              <Grid container spacing={2}>
                  <AnimatePresence mode="popLayout">
                  {sortedResults.map((torrent, index) => (
                    <Grid key={`${sortField}-${sortOrder}-${index}`} size={{ xs: 12, sm: 6, md: 4 }}>
                      <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: 20 }}
                        transition={{
                          duration: 0.4,
                          ease: [0.4, 0, 0.2, 1]
                        }}
                        style={{ height: '100%' }}
                      >
                        <ContentCard accentColor="#8fd9b8" sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                      {/* Torrent Name */}
                      <Typography
                        variant="h6"
                        sx={{
                          fontWeight: 500,
                          mb: { xs: 2.5, md: 2 },
                          wordBreak: 'break-word',
                          fontSize: { xs: '1rem', md: '1.25rem' },
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          lineHeight: 1.4,
                          minHeight: { xs: 'calc(1rem * 1.4 * 2)', md: 'calc(1.25rem * 1.4 * 2)' }
                        }}
                      >
                        {torrent.name}
                      </Typography>

                      {/* Torrent Info Chips */}
                      <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        mb: 2
                      }}>
                        <Chip
                          label={torrent.size}
                          size="small"
                          sx={{
                            backgroundColor: 'rgba(0, 0, 0, 0.06)',
                            fontWeight: 500
                          }}
                        />
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Chip
                            label={`↑ ${torrent.seeds}`}
                            size="small"
                            color="success"
                            variant="outlined"
                          />
                          <Chip
                            label={`↓ ${torrent.leeches}`}
                            size="small"
                            color="warning"
                            variant="outlined"
                          />
                        </Box>
                      </Box>

                      {/* Metadata */}
                      <Box sx={{
                        mb: 2,
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        flex: 1
                      }}>
                        <Typography variant="caption" color="text.secondary">
                          {torrent.date}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          by {torrent.contributor}
                        </Typography>
                      </Box>

                      {/* Action Pills */}
                      <Box sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        gap: 1,
                        pt: 1,
                        mt: 'auto'
                      }}>
                        <Chip
                          label={<Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <FontAwesomeIcon icon={faMagnet} />
                          </Box>}
                          onClick={() => handleDownload(torrent)}
                          variant="outlined"
                          sx={{
                            height: '32px',
                            borderRadius: 100,
                            cursor: 'pointer',
                            fontSize: '0.875rem',
                            '& .MuiChip-label': {
                              px: 2,
                              py: 0.5,
                            },
                            '&:hover': {
                              backgroundColor: 'action.hover',
                            }
                          }}
                        />
                        <Chip
                          label={<Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <FontAwesomeIcon icon={faCopy} />
                            <FontAwesomeIcon icon={faMagnet} />
                          </Box>}
                          onClick={() => handleCopyMagnet(torrent)}
                          variant="outlined"
                          sx={{
                            height: '32px',
                            borderRadius: 100,
                            cursor: 'pointer',
                            fontSize: '0.875rem',
                            '& .MuiChip-label': {
                              px: 2,
                              py: 0.5,
                            },
                            '&:hover': {
                              backgroundColor: 'action.hover',
                            }
                          }}
                        />
                        <Chip
                          label={<Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <FontAwesomeIcon icon={faGears} />
                            <FontAwesomeIcon icon={faMagnet} />
                          </Box>}
                          onClick={() => handleAddToDeluge(torrent)}
                          variant="outlined"
                          sx={{
                            height: '32px',
                            borderRadius: 100,
                            cursor: 'pointer',
                            fontSize: '0.875rem',
                            '& .MuiChip-label': {
                              px: 2,
                              py: 0.5,
                            },
                            '&:hover': {
                              backgroundColor: 'action.hover',
                            }
                          }}
                        />
                      </Box>
                    </ContentCard>
                    </motion.div>
                  </Grid>
                  ))}
                  </AnimatePresence>
              </Grid>
            </Box>
          )}

          {/* Recent Searches */}
          <AnimatePresence mode="wait">
            {!isLoading && !error && results.length === 0 && !keywords && recentSearches.length > 0 && (
              <motion.div
                key="recent-searches"
                initial={{ opacity: 0, y: 50 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 50 }}
                transition={{
                  duration: 0.3,
                  ease: [0.4, 0, 0.2, 1]
                }}
              >
                <Box sx={{ px: 3, pt: 2, pb: 4 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <HistoryIcon sx={{ color: 'text.secondary' }} />
                      <Typography variant="h6" color="text.secondary">
                        Recent Searches
                      </Typography>
                    </Box>
                    <Button
                      size="small"
                      startIcon={<CloseIcon />}
                      onClick={clearRecentSearches}
                      sx={{ color: 'text.secondary' }}
                    >
                      Clear
                    </Button>
                  </Box>
                  <Stack spacing={1}>
                    {recentSearches.map((query, index) => (
                      <Chip
                        key={index}
                        label={query}
                        onClick={() => handleRecentSearchClick(query)}
                        icon={<SearchIcon />}
                        sx={{
                          justifyContent: 'flex-start',
                          px: 2,
                          py: 3,
                          fontSize: '1rem',
                          '&:hover': {
                            backgroundColor: 'action.hover',
                          }
                        }}
                      />
                    ))}
                  </Stack>
                </Box>
          </motion.div>
        )}
      </AnimatePresence>
      </SearchLayout>

      {/* Settings Menu */}
      <Menu
        anchorEl={settingsAnchorEl}
        open={settingsOpen}
        onClose={() => setSettingsAnchorEl(null)}
      >
        <MenuItem
          selected={selectedTracker === Tracker.ONE_THREE_THREE}
          onClick={() => {
            setSelectedTracker(Tracker.ONE_THREE_THREE);
            setSettingsAnchorEl(null);
          }}
        >
          1337x.to
        </MenuItem>
        <MenuItem
          selected={selectedTracker === Tracker.TORRENT_GALAXY}
          onClick={() => {
            setSelectedTracker(Tracker.TORRENT_GALAXY);
            setSettingsAnchorEl(null);
          }}
        >
          TorrentGalaxy
        </MenuItem>
        <MenuItem
          selected={selectedTracker === Tracker.TRUNK}
          onClick={() => {
            setSelectedTracker(Tracker.TRUNK);
            setSettingsAnchorEl(null);
          }}
        >
          Trunk (Local)
        </MenuItem>
      </Menu>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarOpen(false)}
        message={snackbarMessage}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </>
  );
};
