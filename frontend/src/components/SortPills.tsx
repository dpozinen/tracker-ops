import { Box, Button, IconButton, Tooltip, Typography } from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowDownShortWide, faArrowDownWideShort } from '@fortawesome/free-solid-svg-icons';
import { motion } from 'framer-motion';

export interface SortOption<T extends string = string> {
  value: T;
  label: string;
}

interface SortPillsProps<T extends string = string> {
  sortOptions: SortOption<T>[];
  sortField: T;
  sortOrder: 'ASC' | 'DESC';
  sortExpanded: boolean;
  onSortFieldChange: (field: T) => void;
  onToggleSortOrder: () => void;
  onSortExpandedChange: (expanded: boolean) => void;
  resultCount: number;
  colors: {
    container: string;
    selected: string;
    hover: string;
  };
  filterPills?: React.ReactNode;
  filterExpanded?: boolean;
}

export const SortPills = <T extends string = string>({
  sortOptions,
  sortField,
  sortOrder,
  sortExpanded,
  onSortFieldChange,
  onToggleSortOrder,
  onSortExpandedChange,
  resultCount,
  colors,
  filterPills,
  filterExpanded = false,
}: SortPillsProps<T>) => {
  const anyExpanded = sortExpanded || filterExpanded;
  return (
    <Box sx={{
      display: 'flex',
      justifyContent: { xs: 'flex-end', md: 'space-between' },
      alignItems: 'center',
      gap: 2,
      overflowX: 'hidden',
      position: 'relative',
      width: '100%'
    }}>
      {/* Mobile: Animated result count */}
      <Box sx={{ display: { xs: 'block', md: 'none' } }}>
        <motion.div
          animate={{
            opacity: anyExpanded ? 0 : 1,
            x: anyExpanded ? -100 : 0,
            y: '-50%'
          }}
          transition={{
            duration: 0.4,
            ease: [0.4, 0, 0.2, 1]
          }}
          style={{
            position: 'absolute',
            left: 0,
            top: '50%',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
        >
          <Box sx={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'rgba(0, 0, 0, 0.08)',
            borderRadius: 100,
            px: 2,
            py: 0.5,
            minWidth: '40px',
          }}>
            <Typography variant="body1" sx={{ fontWeight: 600 }}>
              {resultCount}
            </Typography>
          </Box>
        </motion.div>
      </Box>

      {/* Desktop: Static result count */}
      <Box sx={{
        display: { xs: 'none', md: 'inline-flex' },
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(0, 0, 0, 0.08)',
        borderRadius: 100,
        px: 2,
        py: 0.5,
        minWidth: '40px',
      }}>
        <Typography variant="body1" sx={{ fontWeight: 600 }}>
          {resultCount}
        </Typography>
      </Box>

      <Box sx={{
        display: 'flex',
        gap: 2,
        alignItems: 'center',
        flexWrap: 'nowrap',
        width: { xs: anyExpanded ? '100%' : 'auto', md: 'auto' },
        justifyContent: { xs: 'flex-end', md: 'flex-start' }
      }}>
        <Box sx={{ display: { xs: sortExpanded ? 'none' : 'flex', md: 'flex' } }}>
          {filterPills}
        </Box>

        <Box
          sx={{
            display: { xs: filterExpanded ? 'none' : 'inline-flex', md: 'inline-flex' },
            justifyContent: 'flex-end',
          }}
        >
          <motion.div
            layout
            transition={{
              layout: {
                duration: 0.2,
                ease: [0.4, 0, 0.2, 1]
              }
            }}
            style={{
              display: 'flex',
              position: 'relative',
              backgroundColor: colors.container,
              borderRadius: 100,
              border: '1px solid rgba(0, 0, 0, 0.12)',
              padding: '2px',
              alignItems: 'center',
              gap: sortExpanded ? '4px' : '0',
              transformOrigin: 'right center',
              overflow: 'visible',
            }}
          >
            {sortOptions.map((option, index) => {
              const isSelected = option.value === sortField;
              const shouldBeAbsolute = !sortExpanded && !isSelected;

              return (
                <motion.div
                  key={option.value}
                  layout
                  animate={{
                    opacity: shouldBeAbsolute ? 0 : 1,
                  }}
                  transition={{
                    layout: {
                      duration: 0.2,
                      ease: [0.4, 0, 0.2, 1]
                    },
                    opacity: {
                      duration: 0.15,
                      ease: [0.4, 0, 0.2, 1]
                    }
                  }}
                  style={{
                    position: shouldBeAbsolute ? 'absolute' : 'relative',
                    right: shouldBeAbsolute ? '4px' : 'auto',
                    zIndex: isSelected ? 10 : index,
                  }}
                >
                  <Button
                    size="small"
                    onClick={() => isSelected ? onSortExpandedChange(!sortExpanded) : onSortFieldChange(option.value)}
                    sx={{
                      minWidth: 'auto',
                      px: 2,
                      py: 0.5,
                      borderRadius: 100,
                      textTransform: 'none',
                      fontWeight: isSelected ? 600 : 400,
                      backgroundColor: isSelected ? colors.selected : 'transparent',
                      color: isSelected ? '#000' : 'text.primary',
                      whiteSpace: 'nowrap',
                      pointerEvents: sortExpanded || isSelected ? 'auto' : 'none',
                      '&:hover': {
                        backgroundColor: isSelected ? colors.hover : 'action.hover',
                      }
                    }}
                  >
                    {option.label}
                  </Button>
                </motion.div>
              );
            })}

            {/* Sort order icon - always visible, static on the right */}
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                position: 'relative',
                zIndex: 100,
              }}
            >
              <Tooltip title={`Sort ${sortOrder === 'ASC' ? 'Descending' : 'Ascending'}`}>
                <IconButton
                  onClick={onToggleSortOrder}
                  size="small"
                  sx={{
                    width: 32,
                    height: 32,
                  }}
                >
                  <FontAwesomeIcon
                    icon={sortOrder === 'ASC' ? faArrowDownShortWide : faArrowDownWideShort}
                    style={{ fontSize: '14px' }}
                  />
                </IconButton>
              </Tooltip>
            </Box>
          </motion.div>
        </Box>
      </Box>
    </Box>
  );
};
