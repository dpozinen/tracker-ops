# Development Notes for Future Sessions

This document contains architectural decisions, lessons learned, and implementation patterns for the ZOE frontend project.

## Quality Checks (REQUIRED for changes >5 lines)

**IMPORTANT**: Run these checks after every frontend change larger than 5 lines:

### 1. TypeScript Errors & Unused Imports
```bash
npm run build 2>&1 | grep -E "(TS6133|error TS)"
```
Should return empty (no errors).

### 2. Deprecation Warnings
Check for deprecated MUI props (especially important with MUI v7):
```bash
grep -r "InputProps\|FormHelperTextProps\|InputLabelProps" src/ --include="*.tsx" --include="*.ts"
```
Or check type definitions:
```bash
grep -B5 "@deprecated" node_modules/@mui/material --include="*.d.ts"
```

### 3. Build Success
```bash
npm run build 2>&1 | tail -15
```
Should show "✓ built in X.XXs" with no errors.

### 4. Optional: ESLint
```bash
npm run lint
```

### Known Deprecations Fixed (Nov 2025)
- ✅ `onKeyPress` → `onKeyDown` (React 18+ deprecation)
- ✅ `InputProps` → `slotProps.input` (MUI v7 deprecation)
- ✅ Unused imports removed (Card, CardContent, Divider)

## Recent Progress (Nov 2025)

### Completed Features

#### 1. URL Routing & Navigation (Nov 3, 2025)
- ✅ Implemented client-side URL routing using browser History API (no React Router dependency)
- ✅ Landing page always shows first, then animates to target page
- ✅ URL paths: `/` (landing), `/search` (tracker search), `/deluge` (torrent manager)
- ✅ Browser back/forward button support
- ✅ Direct URL access with smooth transitions

#### 2. SearchLayout Component Architecture (Nov 3, 2025)
- ✅ Created shared `SearchLayout` component for consistent page structure
- ✅ Fixed position outer container (no scrolling)
- ✅ Only results area scrolls internally
- ✅ Responsive mobile spacing (0.5 rem margins on all sides for xs breakpoint)
- ✅ Framer Motion animations (slide + fade, 0.3s duration)
- ✅ Theme-based backgrounds for different pages (green for search, blue for deluge)

#### 3. Animation System (Nov 3, 2025)
- ✅ Smooth page transitions using AnimatePresence
- ✅ Consistent slide-down animation (both enter and exit use `y: 20`)
- ✅ Fade effect combined with slide (opacity 0 → 1 → 0)
- ✅ Standard easing: `cubic-bezier(0.4, 0, 0.2, 1)`
- ✅ 0.3s duration for balanced feel

#### 4. UI Polish (Nov 3, 2025)
- ✅ Fixed MUI Tooltip warnings (wrapped disabled buttons in `<span>`)
- ✅ Optimized mobile layout with proper padding and margins
- ✅ Rounded corners on mobile (borderRadius: 2)
- ✅ Equal spacing on all sides for mobile

#### 5. Card System & Results Display (Nov 3, 2025)
- ✅ Created `ContentCard` component with accent color border
- ✅ Responsive grid layout: 1 col mobile, 2 tablet, 3 desktop
- ✅ Integrated with mock data system (useTrackerSearch hook)
- ✅ Search results display as cards instead of list view
- ✅ Different accent colors: green (#8fd9b8) for Search, blue (#8fc5e8) for Deluge
- ✅ Card displays: name, size, seeds, leeches, date, contributor, action buttons

#### 6. State Persistence (Nov 3, 2025)
- ✅ Search results persist across tab switches (sessionStorage)
- ✅ Search keywords, tracker, sort settings persist (sessionStorage)
- ✅ Recent searches persist across sessions (localStorage, max 5)
- ✅ Clear button properly clears both state and storage

#### 7. Code Quality & Deprecations (Nov 3, 2025)
- ✅ Fixed `onKeyPress` → `onKeyDown` (React 18+ deprecation)
- ✅ Fixed `InputProps` → `slotProps.input` (MUI v7 deprecation)
- ✅ Removed unused imports (Card, CardContent, Divider)
- ✅ Fixed `parseSizeToBytes` hoisting issue
- ✅ Established quality check workflow for future changes

## Architecture Decisions

### 1. No React Router Dependency
**Decision**: Use browser History API directly instead of React Router
**Reasoning**:
- Single-page application with only 3 views
- Simpler mental model - state is source of truth, URL reflects state
- Smaller bundle size
- More control over animations and transitions

**Implementation**: See `src/contexts/NavigationContext.tsx`

### 2. SearchLayout Component Pattern
**Decision**: Create shared layout component for search pages
**Pattern**: Composition with render props

```tsx
<SearchLayout
  theme={{ background: '#color', buttonColor: '#color' }}
  searchBarContent={<YourSearchBar />}
>
  <YourResults />
</SearchLayout>
```

**Benefits**:
- Consistent layout across search pages
- Centralized animation logic
- Easy to maintain responsive behavior
- Theme support for different pages

### 3. Fixed Container + Scrollable Content Pattern
**Decision**: Outer container is `position: fixed`, inner results area scrolls

**Structure**:
```
Box (position: fixed, overflow: hidden)
  → Container (responsive padding)
    → motion.div (animations)
      → Paper (main content)
        → Paper (search form, flexShrink: 0)
        → Box (results, overflow: auto)
```

**Why This Works**:
- Prevents full-page scrolling
- Search bar stays visible
- Only results scroll
- Smooth animations (no layout shift)
- Mobile-friendly

## Critical Lessons Learned

### Animation Issues

#### Problem: Animations stopped working
**Root Cause**: `motion.div` was placed outside the fixed container
**Solution**: Move `motion.div` INSIDE the Container, wrapping the Paper component
**Why**: Fixed positioning creates new stacking context; motion.div needs to be within it

#### Problem: Jittery animations on page transitions
**Root Causes**:
1. LayoutGroup causing unnecessary recalculations
2. Background re-rendering on each transition
3. `mode="wait"` causing sequential animations

**Solutions**:
1. Removed LayoutGroup - not needed for simple transitions
2. Changed background to `position: fixed` with `zIndex: 0`
3. Changed to `mode="wait"` with `initial={false}` for smoother transitions
4. Made both enter and exit slide in same direction (both `y: 20`, not `y: -20` and `y: 20`)

### Mobile Layout Issues

#### Problem: Card edges touching screen
**Root Cause**: Removed padding/margins trying to maximize space
**Solution**: Container `px: { xs: 0.5, sm: 3, md: 3 }` provides proper mobile spacing

#### Problem: Square corners on mobile
**Root Cause**: Set `borderRadius: 0` trying to maximize space
**Solution**: Keep `borderRadius: { xs: 2, md: 3 }` for consistent design

#### Problem: Unwanted scrolling on mobile
**Root Cause**: Outer container not properly constrained
**Solution**:
```tsx
<Box sx={{
  position: 'fixed',
  top: 0, left: 0, right: 0, bottom: 0,
  overflow: 'hidden'
}}>
```

### MUI Tooltip Warnings

#### Problem: "Disabled element does not fire events" warning
**Root Cause**: Tooltip wrapping disabled IconButton directly
**Solution**: Wrap disabled buttons in `<span>` element

```tsx
<Tooltip title="Search">
  <span>
    <IconButton disabled={isLoading || !keywords.trim()}>
      <SearchIcon />
    </IconButton>
  </span>
</Tooltip>
```

## Component Implementation Notes

### NavigationContext.tsx
- Manages app-wide navigation state
- Syncs `currentView` state with browser URL
- Handles browser back/forward buttons
- On direct URL access, shows landing first, then transitions
- Uses `requestAnimationFrame` for smooth initial transitions

**Key Functions**:
- `pathToView()`: Converts URL path to ViewType
- `viewToPath()`: Converts ViewType to URL path
- `setCurrentView()`: Updates state AND URL
- `handlePopState()`: Syncs state when user clicks back/forward

### SearchLayout.tsx
- Shared layout for search pages
- Accepts theme prop for custom backgrounds
- Manages fixed/scrollable layout
- Handles page animations
- Responsive spacing for mobile

**Props**:
```tsx
interface SearchLayoutProps {
  theme: {
    background: string;      // Search bar background
    buttonColor: string;     // Button color (unused currently)
  };
  searchBarContent: ReactNode;  // Search bar UI
  children: ReactNode;          // Results content
}
```

### App.tsx
- Fixed background (never re-renders)
- Always renders Landing page
- Conditionally renders Search/Deluge pages
- AnimatePresence manages transitions

**Important**: Landing is always rendered so tiles can animate to navbar

## Responsive Design Patterns

### Mobile-First Breakpoints
```tsx
sx={{
  xs: 'mobile',    // 0-600px
  sm: 'tablet',    // 600-900px
  md: 'desktop',   // 900px+
}}
```

### Standard Spacing Pattern
```tsx
// Outer container
pt: 10,                    // Top padding for navbar
pb: { xs: 0.5, md: 2 },   // Bottom padding

// Container
px: { xs: 0.5, sm: 3, md: 3 },  // Horizontal padding

// Paper
p: { xs: 2, md: 4 },      // Internal padding
```

### Responsive Components
```tsx
// Icon buttons
height: { xs: 44, md: 56 }  // Touch-friendly on mobile

// Border radius
borderRadius: { xs: 2, md: 3 }  // Slightly smaller on mobile

// Gaps
gap: { xs: 1, md: 2 }  // Tighter spacing on mobile
```

## Animation Configuration

### Standard Page Transition
```tsx
<motion.div
  initial={{ opacity: 0, y: 20 }}
  animate={{ opacity: 1, y: 0 }}
  exit={{ opacity: 0, y: 20 }}
  transition={{
    duration: 0.3,
    ease: [0.4, 0, 0.2, 1]
  }}
>
```

**Important**: Both enter and exit slide DOWN (y: 20) for consistency

### AnimatePresence Setup
```tsx
<AnimatePresence mode="wait" initial={false}>
  {currentView === 'search' && <TrackerSearch key="search" />}
  {currentView === 'deluge' && <Deluge key="deluge" />}
</AnimatePresence>
```

**Key Props**:
- `mode="wait"`: Wait for exit animation before entering
- `initial={false}`: Don't animate on mount
- `key`: Required for AnimatePresence to track components

## Common Pitfalls & Solutions

### ❌ DON'T: Place motion.div outside fixed container
```tsx
<motion.div>
  <Box sx={{ position: 'fixed' }}>
    {/* Content */}
  </Box>
</motion.div>
```

### ✅ DO: Place motion.div inside fixed container
```tsx
<Box sx={{ position: 'fixed' }}>
  <Container>
    <motion.div>
      {/* Content */}
    </motion.div>
  </Container>
</Box>
```

### ❌ DON'T: Use different slide directions for enter/exit
```tsx
initial={{ y: 20 }}
exit={{ y: -20 }}  // Jarring transition
```

### ✅ DO: Use same direction for smooth feel
```tsx
initial={{ y: 20 }}
exit={{ y: 20 }}  // Smooth, consistent
```

### ❌ DON'T: Set margins/padding to 0 on mobile
```tsx
px: { xs: 0, md: 3 }  // Edges touch screen
```

### ✅ DO: Keep small margins on mobile
```tsx
px: { xs: 0.5, sm: 3, md: 3 }  // Proper spacing
```

### ❌ DON'T: Wrap disabled buttons in Tooltip directly
```tsx
<Tooltip title="Click">
  <IconButton disabled={true}>...</IconButton>
</Tooltip>
```

### ✅ DO: Add span wrapper for disabled buttons
```tsx
<Tooltip title="Click">
  <span>
    <IconButton disabled={true}>...</IconButton>
  </span>
</Tooltip>
```

## File Organization

### Key Files for Navigation/Layout
- `src/contexts/NavigationContext.tsx` - URL routing and navigation state
- `src/components/SearchLayout.tsx` - Shared search page layout
- `src/App.tsx` - Root component with AnimatePresence
- `src/pages/Landing.tsx` - Landing page with tiles
- `src/pages/TrackerSearch.tsx` - Tracker search page
- `src/pages/Deluge.tsx` - Deluge management page

### Key Files for Styling
- `src/theme/theme.ts` - MUI theme configuration
- `DESIGN.md` - Design system documentation
- `src/constants/tiles.ts` - Tile configuration

## Testing the App

### URL Routes to Test
- `http://localhost:3000/` - Landing page
- `http://localhost:3000/search` - Tracker search (via landing)
- `http://localhost:3000/deluge` - Deluge manager (via landing)

### Things to Verify
- [ ] Direct URL access shows landing, then transitions
- [ ] Navbar navigation works smoothly
- [ ] Browser back/forward buttons work
- [ ] Animations are smooth (no jitter)
- [ ] Mobile spacing is correct (0.5 rem margins)
- [ ] Only results area scrolls, not whole page
- [ ] No console warnings

## Next Development Tasks

### High Priority
- [ ] Add unit tests for NavigationContext
- [ ] Add Storybook stories for SearchLayout
- [ ] Implement dark mode support
- [ ] Add error boundaries for better error handling

### Medium Priority
- [ ] Optimize animations for lower-end devices
- [ ] Add keyboard shortcuts for navigation
- [ ] Implement focus management for accessibility
- [ ] Add loading skeletons for better perceived performance

### Low Priority
- [ ] Add page transition sound effects (optional)
- [ ] Add haptic feedback on mobile (optional)
- [ ] Implement advanced gesture navigation (swipe to go back)

## Performance Notes

### Animation Performance
- Framer Motion uses GPU acceleration automatically
- `transform` and `opacity` are performant properties
- Avoid animating `width`, `height`, `top`, `left` directly

### Bundle Size
- No React Router = ~50KB saved
- Framer Motion = ~30KB (tree-shakeable)
- Total bundle: ~XXX KB (TODO: measure)

### Rendering Optimizations
- Landing page always mounted (prevents re-mount cost)
- Fixed background (no re-renders)
- AnimatePresence mode="wait" (one component at a time)

## Debugging Tips

### Animation Not Working?
1. Check if `motion.div` is inside fixed container
2. Verify AnimatePresence has `key` prop on children
3. Check if `initial={false}` is set on AnimatePresence
4. Inspect with React DevTools to see component tree

### Layout Issues on Mobile?
1. Check responsive sx props (xs, sm, md)
2. Verify container padding: `px: { xs: 0.5, sm: 3, md: 3 }`
3. Check border radius: `borderRadius: { xs: 2, md: 3 }`
4. Inspect with Chrome DevTools mobile emulation

### URL Not Syncing?
1. Verify `setCurrentView()` is being called (not `setCurrentViewState()`)
2. Check `handlePopState` listener is attached
3. Verify `pathToView()` and `viewToPath()` mappings
4. Check browser console for errors

## AI-Friendly Summary

**For Claude Code Sessions**: This React app uses:
- Custom URL routing (History API, no React Router)
- SearchLayout component for consistent page structure
- Fixed outer container + scrollable results pattern
- Framer Motion for animations (slide + fade, 0.3s)
- Mobile-first responsive design with MUI breakpoints
- Landing → page transition pattern (shows landing first)

**Critical Files**:
- NavigationContext.tsx (routing)
- SearchLayout.tsx (layout)
- App.tsx (structure)

**Common Tasks**:
- New page? Add to NavigationContext, wrap in SearchLayout
- Animation issue? Check motion.div is inside fixed container
- Mobile spacing? Use `px: { xs: 0.5, sm: 3, md: 3 }`
- Tooltip warning? Wrap disabled button in `<span>`
