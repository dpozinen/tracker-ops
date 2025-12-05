import { useState, useEffect, useRef, useCallback } from 'react';
import { Box, TextField, IconButton, Tooltip, InputAdornment, CircularProgress, Grid, Alert, Typography } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import CloseIcon from '@mui/icons-material/Close';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowDown, faArrowUp, faPause, faPlus, faMagnet } from '@fortawesome/free-solid-svg-icons';
import { SearchLayout } from '../components/SearchLayout';
import { TorrentCard } from '../components/TorrentCard';
import { SortPills } from '../components/SortPills';
import { FilterPills } from '../components/FilterPills';
import type { FilterOption } from '../components/FilterPills';
import { MagnetDialog } from '../components/MagnetDialog';
import { useDelugeWebSocketManager } from '../hooks/useDelugeWebSocketManager';
import { useNavigation } from '../contexts/NavigationContext';
import type { DelugeTorrent } from '../types/api';

type SortField = 'name' | 'size' | 'date' | 'uploaded';
type SortOrder = 'ASC' | 'DESC';

// Map frontend sort fields to backend By enum values
const SORT_FIELD_TO_BACKEND: Record<SortField, string> = {
  name: 'NAME',
  size: 'SIZE',
  date: 'DATE',
  uploaded: 'UPLOADED',
};

// Predefined filter options matching production implementation
const FILTER_OPTIONS: FilterOption[] = [
  { id: 'downloading', label: 'Downloading', field: 'STATE', value: 'Downloading', operator: 'IS', icon: faArrowDown, iconColor: '#f44336' },
  { id: 'seeding', label: 'Seeding', field: 'STATE', value: 'Seeding', operator: 'IS', icon: faArrowUp, iconColor: '#fbc02d' },
  { id: 'uploading', label: 'Uploading', field: 'UPLOAD_SPEED', value: 0.0, operator: 'GREATER', icon: faArrowUp, iconColor: '#4caf50' },
  { id: 'paused', label: 'Paused', field: 'STATE', value: 'Paused', operator: 'IS', icon: faPause, iconColor: '#ff9800' },
  { id: 'positive', label: 'Positive', field: 'RATIO', value: 1.0, operator: 'GREATER', icon: faPlus, iconColor: '#2196f3' },
];

export const Deluge = () => {
  const [keywords, setKeywords] = useState('');
  const [showCards, setShowCards] = useState(false);
  const [canvasAnimationComplete, setCanvasAnimationComplete] = useState(false);
  const [displayCount, setDisplayCount] = useState(20);
  const [sortField, setSortField] = useState<SortField>('name');
  const [sortOrder, setSortOrder] = useState<SortOrder>('DESC');
  const [sortExpanded, setSortExpanded] = useState(false);
  const [activeFilters, setActiveFilters] = useState<Set<string>>(new Set());
  const [filterExpanded, setFilterExpanded] = useState(false);
  const [magnetDialogOpen, setMagnetDialogOpen] = useState(false);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const { navbarAnimationsComplete } = useNavigation();
  const [initializedFromUrl, setInitializedFromUrl] = useState(false);

  // Connect to WebSocket for real-time torrent data (via Vite proxy with correct Origin)
  const wsUrl = window.location.protocol === 'https:'
    ? `wss://${window.location.host}/api/stream`
    : `ws://${window.location.host}/api/stream`;

  const { torrents, isConnected, isLoading, error, hasReceivedData, sendSortMutation, reverseSortOrder, addFilter, clearFilter, addSearch } = useDelugeWebSocketManager({
    url: wsUrl,
    autoConnect: true,
    reconnectInterval: 3000,   // Wait 3 seconds between reconnection attempts
    maxReconnectAttempts: 10,  // Try up to 10 times before giving up
  });

  // Initialize from URL query parameters on mount
  useEffect(() => {
    if (initializedFromUrl || !hasReceivedData) return;

    const params = new URLSearchParams(window.location.search);
    const sortParam = params.get('sort');
    const filterParam = params.get('filter');

    // Apply sort from URL (using frontend field names like 'name', 'uploaded')
    if (sortParam) {
      const sorts = sortParam.split(',');
      sorts.forEach(sortSpec => {
        const [field, order] = sortSpec.split(':');
        const backendField = SORT_FIELD_TO_BACKEND[field as SortField];

        if (backendField) {
          const validOrder = (order === 'ASC' || order === 'DESC') ? order : 'ASC';
          setSortField(field as SortField);
          setSortOrder(validOrder);
          sendSortMutation(backendField, validOrder);
        }
      });
    }

    // Apply filters from URL (using filter IDs)
    if (filterParam) {
      const filterIds = filterParam.split(',');
      const newActiveFilters = new Set<string>();

      filterIds.forEach(filterId => {
        const filter = FILTER_OPTIONS.find(f => f.id === filterId);

        if (filter) {
          newActiveFilters.add(filter.id);
          addFilter(filter.field, filter.value, [filter.operator]);
        }
      });

      setActiveFilters(newActiveFilters);
    }

    setInitializedFromUrl(true);
  }, [hasReceivedData, initializedFromUrl, addFilter, sendSortMutation]);

  // Show cards only after ALL animations complete AND torrents are available
  useEffect(() => {
    if (torrents.length > 0 && canvasAnimationComplete && navbarAnimationsComplete) {
      setShowCards(true);
    } else {
      setShowCards(false);
    }
  }, [torrents.length, canvasAnimationComplete, navbarAnimationsComplete]);

  // Callback when SearchLayout animation completes
  const handleCanvasAnimationComplete = () => {
    setCanvasAnimationComplete(true);
  };

  // Backend handles filtering via WebSocket mutations
  const allTorrents = torrents;

  // Limit displayed torrents to current displayCount
  const displayedTorrents = allTorrents.slice(0, displayCount);

  // Calculate trigger index (5 items before the end of displayed items)
  const triggerIndex = Math.max(0, displayCount - 6);

  // Callback ref that sets up observer when element is mounted
  const triggerElementRef = useCallback((element: HTMLDivElement | null) => {
    console.log('🎯 Callback ref called:', {
      element,
      displayCount,
      triggerIndex,
      totalTorrents: allTorrents.length
    });

    // Clean up previous observer
    if (observerRef.current) {
      observerRef.current.disconnect();
      console.log('🧹 Disconnected previous observer');
    }

    // Don't set up observer if no element or no more to load
    if (!element || displayCount >= allTorrents.length) {
      console.log('❌ Skipping observer - no element or all loaded');
      return;
    }

    // Create and attach observer
    observerRef.current = new IntersectionObserver(
      (entries) => {
        console.log('👁️ Observer triggered!', entries[0].isIntersecting);
        if (entries[0].isIntersecting) {
          console.log('✅ Loading more items!');
          setDisplayCount(prev => Math.min(prev + 20, allTorrents.length));
        }
      },
      { threshold: 0.1, rootMargin: '100px' }
    );

    observerRef.current.observe(element);
    console.log('✅ Observer attached to element at index', triggerIndex);
  }, [displayCount, triggerIndex, allTorrents.length]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handleSearch = async () => {
    const trimmed = keywords.trim();
    // Clear previous searches first by sending empty search (triggers Clear.AllSearches())
    addSearch('');

    // Wait 100ms before adding new search
    await new Promise(resolve => setTimeout(resolve, 100));

    // Then add new search if not empty
    if (trimmed) {
      addSearch(trimmed);
    }

    // Reset to showing first 20 items when search changes
    setDisplayCount(20);
  };

  const handleClearSearch = () => {
    setKeywords('');
    // Clear all searches on backend by sending empty search (triggers Clear.AllSearches())
    addSearch('');
    setDisplayCount(20);
  };

  const handlePauseResume = async (torrent: DelugeTorrent) => {
    const isPaused = torrent.state === 'Paused';
    const action = isPaused ? 'resume' : 'pause';

    try {
      const response = await fetch(`/api/deluge/${action}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          torrentId: torrent.id,
        }),
      });

      if (!response.ok) {
        throw new Error(`Failed to ${action} torrent: ${response.statusText}`);
      }

      console.log(`Torrent ${action}d successfully:`, torrent.id);
      // The WebSocket will automatically update the torrent list
    } catch (error) {
      console.error(`Error ${action}ing torrent:`, error);
      alert(`Failed to ${action} torrent: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  };

  const handleRemove = async (torrent: DelugeTorrent) => {
    if (!confirm(`Are you sure you want to remove "${torrent.name}"?\n\nThis will only remove it from Deluge, not delete the files.`)) {
      return;
    }

    try {
      const response = await fetch('/api/deluge/remove', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          torrentId: torrent.id,
          removeData: false, // Don't delete files by default
        }),
      });

      if (!response.ok) {
        throw new Error(`Failed to remove torrent: ${response.statusText}`);
      }

      console.log('Torrent removed successfully:', torrent.id);
      // The WebSocket will automatically update the torrent list
    } catch (error) {
      console.error('Error removing torrent:', error);
      alert(`Failed to remove torrent: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  };

  // Sort options configuration
  const sortOptions: { value: SortField; label: string }[] = [
    { value: 'name', label: 'Name' },
    { value: 'size', label: 'Size' },
    { value: 'date', label: 'Date' },
    { value: 'uploaded', label: 'Uploaded' },
  ];

  // Toggle sort order and send to backend
  const toggleSortOrder = () => {
    const newOrder = sortOrder === 'ASC' ? 'DESC' : 'ASC';
    setSortOrder(newOrder);
    const backendField = SORT_FIELD_TO_BACKEND[sortField];
    reverseSortOrder(backendField, sortOrder);
  };

  // Handle sort field change and send to backend
  const handleSortFieldChange = (field: SortField) => {
    // Clear previous sort before applying new one
    const previousBackendField = SORT_FIELD_TO_BACKEND[sortField];

    setSortField(field);
    setSortExpanded(false);
    // Reset to showing first 20 items when sort changes
    setDisplayCount(20);

    const backendField = SORT_FIELD_TO_BACKEND[field];
    sendSortMutation(backendField, sortOrder, previousBackendField);
  };

  // Handle filter toggle and send to backend
  const handleFilterToggle = (filterId: string) => {
    const filter = FILTER_OPTIONS.find(f => f.id === filterId);
    if (!filter) return;

    const newActiveFilters = new Set(activeFilters);

    if (activeFilters.has(filterId)) {
      // Remove filter
      newActiveFilters.delete(filterId);
      clearFilter(filter.field);
    } else {
      // Add filter
      newActiveFilters.add(filterId);
      addFilter(filter.field, filter.value, [filter.operator]);
    }

    setActiveFilters(newActiveFilters);
    // Reset to showing first 20 items when filters change
    setDisplayCount(20);
  };

  return (
    <>
      <SearchLayout
        theme={{
          background: '#d4ebfc',
          buttonColor: '#8fc5e8'
        }}
        onAnimationComplete={handleCanvasAnimationComplete}
        searchBarContent={
          <Box sx={{
            display: 'flex',
            gap: { xs: 1, md: 2 },
            alignItems: 'center',
          }}>
            <Tooltip title="Add Magnets">
              <IconButton
                onClick={() => setMagnetDialogOpen(true)}
                sx={{
                  height: { xs: 44, md: 56 },
                  width: { xs: 44, md: 56 },
                  flexShrink: 0,
                  backgroundColor: '#8fc5e8',
                  border: '1px solid',
                  borderColor: 'divider',
                  transition: 'all 0.2s',
                  '&:hover': {
                    backgroundColor: '#7ab5d8',
                    borderColor: '#2196f3',
                  },
                  '&:active': {
                    borderColor: '#2196f3',
                  }
                }}
              >
                <FontAwesomeIcon icon={faMagnet} style={{ fontSize: '18px' }} />
              </IconButton>
            </Tooltip>

            <TextField
              fullWidth
              placeholder="Search"
              value={keywords}
              onChange={(e) => setKeywords(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isLoading}
              sx={{
                '& .MuiOutlinedInput-root': {
                  '&.Mui-focused fieldset': {
                    borderColor: '#2196f3',
                  },
                },
              }}
              slotProps={{
                input: {
                  endAdornment: keywords && (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={handleClearSearch}
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
                    backgroundColor: '#8fc5e8',
                    border: '1px solid',
                    borderColor: 'divider',
                    '&:hover': {
                      backgroundColor: '#7ab5d8',
                      borderColor: 'primary.main',
                    },
                    '&.Mui-disabled': {
                      backgroundColor: '#c0ddf0',
                    }
                  }}
                >
                  {<SearchIcon sx={{ fontSize: { xs: 20, md: 24 } }} />}
                </IconButton>
              </span>
            </Tooltip>
          </Box>
        }
        resultsHeaderContent={
          <SortPills
            sortOptions={sortOptions}
            sortField={sortField}
            sortOrder={sortOrder}
            sortExpanded={sortExpanded}
            onSortFieldChange={handleSortFieldChange}
            onToggleSortOrder={toggleSortOrder}
            onSortExpandedChange={setSortExpanded}
            resultCount={torrents.length}
            filterExpanded={filterExpanded}
            colors={{
              container: '#d4ebfc',
              selected: '#8fc5e8',
              hover: '#7ab5d8',
            }}
            filterPills={
              <FilterPills
                filterOptions={FILTER_OPTIONS}
                activeFilters={activeFilters}
                filterExpanded={filterExpanded}
                onFilterToggle={handleFilterToggle}
                onFilterExpandedChange={setFilterExpanded}
                colors={{
                  container: '#d4ebfc',
                  selected: '#8fc5e8',
                  hover: '#7ab5d8',
                }}
              />
            }
          />
        }
      >
        {/* Connection Status & Error Messages */}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {!isConnected && !isLoading && !error && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Not connected to Deluge server
          </Alert>
        )}

        {/* Loading indicator - show while mounting or initial loading */}
        {(!hasReceivedData || isLoading) && !error && (
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', py: 8 }}>
            <CircularProgress size={60} color="secondary" />
          </Box>
        )}

        {/* Torrent cards with slide-up animation */}
        {showCards && !isLoading && displayedTorrents.length > 0 && (
          <Grid container spacing={2}>
            {displayedTorrents.map((torrent, index) => (
              <Grid
                key={torrent.id}
                size={{ xs: 12, sm: 6, md: 4 }}
                ref={index === triggerIndex ? triggerElementRef : null}
                sx={{
                  animation: index < 20 ? 'slideUp 0.4s ease-out' : 'none',
                  animationDelay: index < 20 ? `${index * 0.05}s` : '0s',
                  animationFillMode: 'backwards',
                  '@keyframes slideUp': {
                    '0%': {
                      opacity: 0,
                      transform: 'translateY(20px)',
                    },
                    '100%': {
                      opacity: 1,
                      transform: 'translateY(0)',
                    },
                  },
                }}
              >
                <TorrentCard
                  torrent={torrent}
                  onPauseResume={handlePauseResume}
                  onRemove={handleRemove}
                />
              </Grid>
            ))}
          </Grid>
        )}

        {/* No results message - only show after we've received data and there are 0 torrents */}
        {hasReceivedData && !isLoading && isConnected && torrents.length === 0 && (
          <Box sx={{ textAlign: 'center', py: 8 }}>
            <Typography variant="h6" color="text.secondary">
              {keywords.trim() ? `No torrents match "${keywords.trim()}"` : 'No torrents available'}
            </Typography>
          </Box>
        )}
      </SearchLayout>

      {/* Magnet Dialog */}
      <MagnetDialog
        open={magnetDialogOpen}
        onClose={() => setMagnetDialogOpen(false)}
      />
    </>
  );
};
