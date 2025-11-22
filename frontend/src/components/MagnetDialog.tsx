import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  IconButton,
  Box,
  CircularProgress,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';

interface MagnetDialogProps {
  open: boolean;
  onClose: () => void;
}

const MAX_MAGNETS = 10;

export const MagnetDialog = ({ open, onClose }: MagnetDialogProps) => {
  const [magnetLinks, setMagnetLinks] = useState<string[]>(['']);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleAddField = () => {
    if (magnetLinks.length < MAX_MAGNETS) {
      setMagnetLinks([...magnetLinks, '']);
    }
  };

  const handleRemoveField = (index: number) => {
    if (magnetLinks.length > 1) {
      const newLinks = magnetLinks.filter((_, i) => i !== index);
      setMagnetLinks(newLinks);
    }
  };

  const handleChange = (index: number, value: string) => {
    const newLinks = [...magnetLinks];
    newLinks[index] = value;
    setMagnetLinks(newLinks);
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);

    // Filter out empty magnets
    const validMagnets = magnetLinks.filter(link => link.trim());

    try {
      // Send all magnets in parallel
      await Promise.all(
        validMagnets.map(magnet =>
          fetch('/api/deluge', {
            method: 'POST',
            headers: {
              'Content-Type': 'text/plain',
            },
            body: magnet.trim(),
          })
        )
      );

      // Reset form and close dialog on success
      setMagnetLinks(['']);
      onClose();
    } catch (error) {
      console.error('Failed to add magnets:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setMagnetLinks(['']);
      onClose();
    }
  };

  const hasValidMagnets = magnetLinks.some(link => link.trim());

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          backgroundColor: '#d4ebfc',
          borderRadius: 2,
        }
      }}
    >
      <DialogTitle sx={{ fontWeight: 600, color: '#000' }}>
        Add Magnets
      </DialogTitle>

      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          {magnetLinks.map((link, index) => (
            <Box key={index} sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
              <TextField
                fullWidth
                placeholder={`Magnet link ${index + 1}`}
                value={link}
                onChange={(e) => handleChange(index, e.target.value)}
                disabled={isSubmitting}
                sx={{
                  '& .MuiOutlinedInput-root': {
                    backgroundColor: '#fff',
                    '&.Mui-focused fieldset': {
                      borderColor: '#2196f3',
                    },
                  },
                }}
              />

              {magnetLinks.length > 1 && (
                <IconButton
                  onClick={() => handleRemoveField(index)}
                  disabled={isSubmitting}
                  size="small"
                  sx={{
                    color: '#f44336',
                    '&:hover': {
                      backgroundColor: 'rgba(244, 67, 54, 0.08)',
                    },
                  }}
                >
                  <DeleteIcon />
                </IconButton>
              )}
            </Box>
          ))}

          {magnetLinks.length < MAX_MAGNETS && (
            <Button
              startIcon={<AddIcon />}
              onClick={handleAddField}
              disabled={isSubmitting}
              sx={{
                alignSelf: 'flex-start',
                textTransform: 'none',
                color: '#2196f3',
                '&:hover': {
                  backgroundColor: 'rgba(33, 150, 243, 0.08)',
                },
              }}
            >
              Add another magnet
            </Button>
          )}
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button
          onClick={handleClose}
          disabled={isSubmitting}
          sx={{
            textTransform: 'none',
            color: '#000',
          }}
        >
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          disabled={!hasValidMagnets || isSubmitting}
          variant="contained"
          sx={{
            textTransform: 'none',
            backgroundColor: '#8fc5e8',
            color: '#000',
            fontWeight: 600,
            minWidth: 100,
            '&:hover': {
              backgroundColor: '#7ab5d8',
            },
            '&.Mui-disabled': {
              backgroundColor: '#c0ddf0',
              color: 'rgba(0, 0, 0, 0.38)',
            },
          }}
        >
          {isSubmitting ? (
            <CircularProgress size={24} sx={{ color: '#000' }} />
          ) : (
            'Submit'
          )}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
