# ZOE Design System

## Branding
**ZOE** - Your personal NAS console

## Design Philosophy
Minimalist, clean, and modern with **Material Design 3** principles. Warm cream backgrounds, tonal surfaces instead of shadows, extra-large rounded corners, and expressive typography.

## Material Design 3 Principles Applied

### Core MD3 Features
- **Tonal Surfaces**: Elevation through color instead of shadows
- **Large Corner Radius**: 28-32px for modern, friendly feel
- **State Layers**: Interactive overlays on hover/press (8% opacity for hover, 12% for press)
- **Standard Easing**: `cubic-bezier(0.4, 0, 0.2, 1)` for all transitions
- **No Box Shadows**: Clean, flat aesthetic with depth from color
- **Expressive Typography**: Large, bold titles with proper hierarchy

## Color Palette

### Light Theme
- **Background**: Warm cream (`#faf8f5` - soft, inviting off-white)
- **Surface**: Pure white for elevation (`#ffffff`)
- **Tiles**: Tonal surfaces with state layers
  - **Deluge Tile**: Pale blue (`#a8d5f5`)
  - **Search Tile**: Pale mint green (`#6fd7a0`)
- **Text**:
  - Primary: MD3 standard (`rgba(0, 0, 0, 0.87)`)
  - Secondary: MD3 variant (`rgba(0, 0, 0, 0.60)`)

### Dark Theme (Future)
- **Background**: MD3 dark surface (`#1a1c1e`)
- **Surface**: MD3 dark surface variant (`#2b2d30`)
- **Text**:
  - Primary: MD3 on-dark (`rgba(255, 255, 255, 0.87)`)
  - Secondary: MD3 variant (`rgba(255, 255, 255, 0.60)`)

## Typography
- **Font Family**:
  - **ZOE branding**: Space Grotesk (700 weight, 10rem)
  - **UI elements**: Work Sans
- **Style**: Clean, modern, readable
  - Semi-bold (600) for h1/h2 headings
  - Regular (400) for tile text and body
  - Medium (500) for buttons and subheadings
  - Slight negative letter-spacing (-0.01em) for h1/h2
  - Normal spacing for all other text
- **Aesthetic**: Bold, clear, friendly, easy to read

## Component Styling

### Tiles (NavTile)
- **Dimensions**: 260px height, full width in grid
- **Layout**: 2 tiles side-by-side on desktop, stacked on mobile
- **Border Radius**: 32px (MD3 extra-large)
- **Elevation**: No shadows - tonal surfaces only
- **Surface Tint**: Subtle white gradient overlay (15% opacity)
- **State Layers**:
  - Hover: 8% black overlay with 2px lift
  - Press: 12% black overlay with no lift
- **Transitions**: 300ms with MD3 standard easing
- **Text**:
  - Size: 2.75rem
  - Weight: 400 (regular)
  - Color: MD3 on-surface (`rgba(0, 0, 0, 0.87)`)

### ZOE Title
- **Font**: Space Grotesk
- **Size**: 10rem (160px)
- **Weight**: 700 (bold)
- **Letter-spacing**: -0.02em
- **Position**: Upper portion of screen (pt: 12)

### Spacing
- Use 8px base unit for consistent spacing
- Generous padding/margins for breathing room
- Container max-width: lg (1280px)
- Large spacing between ZOE and tiles (mb: 12)

## Interaction Design
- **Hover**: MD3 state layer (8% opacity) + 2px lift
- **Press**: MD3 state layer (12% opacity) + return to baseline
- **Transitions**: 300ms cubic-bezier(0.4, 0, 0.2, 1)
- **Focus**: Clear focus indicators for accessibility
- **Overall**: Soft, responsive, material feel

## Current Implementation
- Landing page with "ZOE" in Space Grotesk (10rem, bold)
- Two horizontal tiles: "Deluge" (pale blue) and "Search" (pale mint green)
- Tiles use MD3 tonal surfaces with state layers
- No box shadows - elevation through color
- Extra-large rounded corners (32px on tiles, 28px default)
- Warm cream background
- Mobile responsive

## Material Design 3 Specifications
- **Border Radius**: 28px (default), 32px (tiles), 20px (buttons)
- **State Layers**:
  - Hover: `rgba(0, 0, 0, 0.08)`
  - Pressed: `rgba(0, 0, 0, 0.12)`
- **Transitions**: `0.3s cubic-bezier(0.4, 0, 0.2, 1)`
- **Text Opacity**:
  - Primary: 87%
  - Secondary: 60%
- **Shadows**: None (MD3 uses tonal elevation instead)

## Layout Patterns

### Fixed Container + Scrollable Content
**Used in**: TrackerSearch, Deluge pages via SearchLayout component

**Structure**:
```
Box (position: fixed, overflow: hidden)
  → Container (responsive padding)
    → motion.div (Framer Motion animations)
      → Paper (main content area)
        → Paper (search bar, flexShrink: 0)
        → Box (results area, overflow: auto)
```

**Key Properties**:
- Outer Box: `position: fixed`, `top/left/right/bottom: 0`, `overflow: hidden`
- Container: `px: { xs: 0.5, sm: 3, md: 3 }` for responsive spacing
- motion.div: Enables page transitions, must be inside Container
- Search Paper: `flexShrink: 0` keeps it visible
- Results Box: `overflow: auto` makes only this area scroll

**Benefits**:
- Search bar always visible
- No full-page scrolling
- Smooth animations
- Mobile-optimized spacing

### Page Themes
Different pages use themed backgrounds to distinguish functionality:

| Page | Background | Button Color |
|------|-----------|--------------|
| Search | `#d4f7e6` (pale mint) | `#8fd9b8` (mint) |
| Deluge | `#d4ebfc` (pale blue) | `#8fc5e8` (blue) |

### Responsive Spacing
**Mobile (xs: 0-600px)**:
- Container padding: `px: 0.5`, `pt: 10`, `pb: 0.5`
- Paper padding: `p: 2`
- Border radius: `2` (16px)
- Button size: `44px` (touch-friendly)
- Gaps: `gap: 1` (8px)

**Tablet (sm: 600-900px)**:
- Container padding: `px: 3`
- Paper padding: `p: 3`
- Border radius: `3` (24px)

**Desktop (md: 900px+)**:
- Container padding: `px: 3`, `pb: 2`
- Paper padding: `p: 4`
- Border radius: `3` (24px)
- Button size: `56px`
- Gaps: `gap: 2` (16px)

## Animation System

### Page Transitions
**Duration**: 0.3s
**Easing**: `cubic-bezier(0.4, 0, 0.2, 1)` (MD3 standard)
**Type**: Slide + fade

```tsx
initial={{ opacity: 0, y: 20 }}
animate={{ opacity: 1, y: 0 }}
exit={{ opacity: 0, y: 20 }}
```

**Important**: Both enter and exit slide DOWN (y: 20) for visual consistency

### AnimatePresence Configuration
```tsx
<AnimatePresence mode="wait" initial={false}>
  {currentView === 'search' && <TrackerSearch key="search" />}
  {currentView === 'deluge' && <Deluge key="deluge" />}
</AnimatePresence>
```

- `mode="wait"`: Exit animation completes before enter begins
- `initial={false}`: No animation on first mount
- `key`: Required for AnimatePresence to track components

### Navigation Flow
1. User clicks tile or navbar item
2. Landing page remains mounted (enables tile morphing)
3. Previous page exits with fade + slide down
4. New page enters with fade + slide down
5. URL updates to match current view

## Component Patterns

### SearchLayout Usage
```tsx
<SearchLayout
  theme={{
    background: '#d4f7e6',    // Search bar background
    buttonColor: '#8fd9b8'    // Button accent color
  }}
  searchBarContent={
    <Box sx={{ display: 'flex', gap: 2 }}>
      {/* Search UI */}
    </Box>
  }
>
  {/* Results content */}
</SearchLayout>
```

### Disabled Button with Tooltip
Always wrap disabled buttons in `<span>` to avoid MUI warnings:

```tsx
<Tooltip title="Search">
  <span>
    <IconButton disabled={isLoading}>
      <SearchIcon />
    </IconButton>
  </span>
</Tooltip>
```

## Accessibility Considerations
- Focus indicators on all interactive elements
- Proper heading hierarchy (h1 → h2 → h3)
- ARIA labels on icon-only buttons
- Touch targets minimum 44px on mobile
- Keyboard navigation support
- Screen reader friendly animations (prefers-reduced-motion)

## Future Considerations
- Theme toggle (light/dark mode switch)
- Additional service tiles as features expand
- Accessibility: WCAG AA compliance minimum (in progress)
- FAB (Floating Action Button) for quick actions
- Bottom navigation bar for mobile
- Snackbars and dialogs with MD3 styling
- Loading skeletons for better perceived performance
