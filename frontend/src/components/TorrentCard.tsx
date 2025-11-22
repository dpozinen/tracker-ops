import { memo } from 'react';
import { Typography, Box, Chip, LinearProgress } from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPause, faPlay, faTrash, faArrowDown, faArrowUp, faClock, faArrowsUpDown } from '@fortawesome/free-solid-svg-icons';
import { ContentCard } from './ContentCard';
import type { DelugeTorrent } from '../types/api';

// Helper function to get state color based on torrent state
const getStateColor = (state: DelugeTorrent['state']) => {
  switch (state) {
    case 'Seeding':
      return 'success';
    case 'Downloading':
      return 'info';  // blue
    case 'Paused':
      return 'warning';
    case 'Error':
      return 'error';  // red
    case 'Checking':
      return 'default';  // gray
    case 'Queued':
      return 'default';
    default:
      return 'default';
  }
};

// Helper function to get progress bar color
const getProgressColor = (state: DelugeTorrent['state']) => {
  switch (state) {
    case 'Seeding':
      return 'success';
    case 'Downloading':
      return 'info';  // blue
    case 'Paused':
      return 'warning';
    case 'Error':
      return 'error';  // red
    case 'Checking':
      return 'inherit';  // gray
    default:
      return 'info';
  }
};

interface TorrentCardProps {
  torrent: DelugeTorrent;
  onPauseResume: (torrent: DelugeTorrent) => void;
  onRemove: (torrent: DelugeTorrent) => void;
}

/**
 * Memoized torrent card component
 * Only re-renders when torrent data actually changes
 */
export const TorrentCard = memo(({ torrent, onPauseResume, onRemove }: TorrentCardProps) => {
  return (
    <ContentCard accentColor="#8fc5e8" sx={{ height: '100%', minHeight: { xs: 'auto', md: 220 } }}>
      {/* Progress Bar with State */}
      <Box sx={{ mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          <Chip
            label={torrent.state}
            color={getStateColor(torrent.state)}
            size="small"
          />
          <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
            {torrent.progress}%
          </Typography>
        </Box>
        <LinearProgress
          variant="determinate"
          value={torrent.progress}
          color={getProgressColor(torrent.state)}
          sx={{
            height: 8,
            borderRadius: 1,
            ...(torrent.state === 'Checking' && {
              backgroundColor: 'rgba(0, 0, 0, 0.12)',
              '& .MuiLinearProgress-bar': {
                backgroundColor: 'rgba(0, 0, 0, 0.38)'
              }
            })
          }}
        />
      </Box>

      {/* Torrent Name */}
      <Typography
        variant="body1"
        sx={{
          mb: { xs: 2.5, md: 2 },
          fontWeight: 500,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          minHeight: '3.2em',
        }}
      >
        {torrent.name}
      </Typography>

      {/* Stats Row 1: Size, Ratio, Uploaded */}
      <Box sx={{
        display: 'flex',
        justifyContent: 'space-between',
        mb: 2,
        gap: 1
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, flex: 1 }}>
          <FontAwesomeIcon icon={faArrowDown} style={{ fontSize: '0.875rem', color: '#f44336' }} />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
            {torrent.size}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, flex: 1, justifyContent: 'center' }}>
          <FontAwesomeIcon icon={faArrowsUpDown} style={{ fontSize: '0.875rem', color: '#ff9800' }} />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
            {torrent.ratio}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, flex: 1, justifyContent: 'flex-end' }}>
          <FontAwesomeIcon icon={faArrowUp} style={{ fontSize: '0.875rem', color: '#4caf50' }} />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
            {torrent.uploaded}
          </Typography>
        </Box>
      </Box>

      {/* Stats Row 2: Download Speed, ETA, Upload Speed */}
      <Box sx={{
        display: 'flex',
        justifyContent: 'space-between',
        mb: 2,
        gap: 1
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, flex: 1 }}>
          <FontAwesomeIcon icon={faArrowDown} style={{ fontSize: '0.875rem' }} />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
            {torrent.downloadSpeed}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, flex: 1, justifyContent: 'center' }}>
          <FontAwesomeIcon icon={faClock} style={{ fontSize: '0.875rem' }} />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
            {torrent.eta}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, flex: 1, justifyContent: 'flex-end' }}>
          <FontAwesomeIcon icon={faArrowUp} style={{ fontSize: '0.875rem' }} />
          <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
            {torrent.uploadSpeed}
          </Typography>
        </Box>
      </Box>

      {/* Date */}
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', textAlign: 'center', mb: 2 }}>
        {torrent.date}
      </Typography>

      {/* Action Pills */}
      <Box sx={{
        display: 'flex',
        justifyContent: 'center',
        gap: 1,
        pt: 1,
        mt: 'auto'
      }}>
        {/* Pause/Resume Pill */}
        <Chip
          label={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.1 }}>
              <FontAwesomeIcon icon={torrent.state === 'Paused' ? faPlay : faPause} />
            </Box>
          }
          onClick={() => onPauseResume(torrent)}
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

        {/* Remove Pill */}
        <Chip
          label={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <FontAwesomeIcon icon={faTrash} />
            </Box>
          }
          onClick={() => onRemove(torrent)}
          variant="outlined"
          color="error"
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
  );
}, (prevProps, nextProps) => {
  // Custom comparison function - only re-render if torrent data changed
  return (
    prevProps.torrent.id === nextProps.torrent.id &&
    prevProps.torrent.state === nextProps.torrent.state &&
    prevProps.torrent.progress === nextProps.torrent.progress &&
    prevProps.torrent.downloadSpeed === nextProps.torrent.downloadSpeed &&
    prevProps.torrent.uploadSpeed === nextProps.torrent.uploadSpeed &&
    prevProps.torrent.eta === nextProps.torrent.eta
  );
});

TorrentCard.displayName = 'TorrentCard';
