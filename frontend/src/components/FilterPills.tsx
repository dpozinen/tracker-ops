import { Box, Button, Tooltip } from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { motion } from 'framer-motion';

export interface FilterOption {
  id: string;
  label: string;
  field: string;
  value: string | number;
  operator: string;
  icon: any;
  iconColor: string;
}

interface FilterPillsProps {
  filterOptions: FilterOption[];
  activeFilters: Set<string>;
  filterExpanded: boolean;
  onFilterToggle: (filterId: string) => void;
  onFilterExpandedChange: (expanded: boolean) => void;
  colors: {
    container: string;
    selected: string;
    hover: string;
  };
}

export const FilterPills = ({
  filterOptions,
  activeFilters,
  filterExpanded,
  onFilterToggle,
  onFilterExpandedChange,
  colors,
}: FilterPillsProps) => {
  const handleFilterClick = (filterId: string) => {
    onFilterToggle(filterId);
    // Auto-collapse after selection
    onFilterExpandedChange(false);
  };

  // Create a pill array that includes both filter options and the anchor "Filters" pill
  const allPills = [
    ...filterOptions.map(option => ({ ...option, isAnchor: false })),
    { id: 'anchor', label: 'Filters', isAnchor: true } as const
  ];

  return (
    <Box
      sx={{
        display: 'inline-flex',
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
          gap: filterExpanded ? '4px' : '0',
          transformOrigin: 'right center',
          overflow: 'visible',
        }}
      >
        {allPills.map((pill, index) => {
          const isAnchor = pill.isAnchor;
          const isActive = !isAnchor && activeFilters.has(pill.id);
          // Anchor never becomes absolute, all others hide when collapsed
          const shouldBeAbsolute = !filterExpanded && !isAnchor;

          return (
            <motion.div
              key={pill.id}
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
                zIndex: isAnchor ? 10 : index,
              }}
            >
              {isAnchor ? (
                <Button
                  size="small"
                  disableRipple
                  onClick={() => onFilterExpandedChange(!filterExpanded)}
                  sx={{
                    minWidth: '85px',
                    px: 2,
                    py: 0.5,
                    borderRadius: 100,
                    textTransform: 'none',
                    fontWeight: activeFilters.size > 0 ? 600 : 400,
                    backgroundColor: activeFilters.size > 0 ? colors.selected : 'transparent',
                    color: activeFilters.size > 0 ? '#000' : 'text.primary',
                    whiteSpace: 'nowrap',
                    '&:hover': {
                      backgroundColor: activeFilters.size > 0 ? colors.hover : 'action.hover',
                    }
                  }}
                >
                  Filters{activeFilters.size > 0 && ` (${activeFilters.size})`}
                </Button>
              ) : (
                <Tooltip title={pill.label}>
                  <Button
                    size="small"
                    disableRipple
                    onClick={() => handleFilterClick(pill.id)}
                    sx={{
                      minWidth: 'auto',
                      px: 2,
                      py: 0.5,
                      borderRadius: 100,
                      textTransform: 'none',
                      backgroundColor: isActive ? colors.selected : 'transparent',
                      whiteSpace: 'nowrap',
                      pointerEvents: filterExpanded ? 'auto' : 'none',
                      opacity: isActive ? 1 : 0.6,
                      '&:hover': {
                        backgroundColor: isActive ? colors.hover : 'action.hover',
                        opacity: 1,
                      }
                    }}
                  >
                    <FontAwesomeIcon
                      icon={pill.icon}
                      style={{ color: pill.iconColor }}
                    />
                  </Button>
                </Tooltip>
              )}
            </motion.div>
          );
        })}
      </motion.div>
    </Box>
  );
};
