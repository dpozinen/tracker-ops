# Tracker Ops Frontend

Modern React frontend for the Tracker Ops torrent proxy service.

## Tech Stack

- **React 19** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool & dev server
- **Material-UI 7** - Component library
- **Framer Motion** - Animation library
- **React Router 7** - Routing

## Getting Started

### Installation

```bash
npm install
```

### Development Modes

The frontend supports two development modes: **Mock Data Mode** and **Real API Mode**.

#### Mock Data Mode (Default)

Use this mode to develop the frontend without running the backend server.

```bash
npm run dev
```

This will:
- Start the dev server on `http://localhost:3000`
- Use **mock data** instead of real API calls
- Simulate network latency (800ms delay by default)
- Display a "Development Mode" indicator in the UI

**Benefits:**
- No backend required
- Fast iteration on UI
- Consistent test data
- Easy to test edge cases
- Works offline

#### Real API Mode

Use this mode to test with the actual backend.

1. Start the backend server first (port 8133)
2. Update environment configuration:

```bash
# Edit .env.development
VITE_USE_MOCK_DATA=false
```

3. Start the dev server:

```bash
npm run dev
```

The app will now make real API calls to `http://localhost:8133`.

### Environment Variables

Environment variables are located in:
- `.env.development` - Used in development mode (`npm run dev`)
- `.env.production` - Used in production builds (`npm run build`)
- `.env.example` - Template file

**Available Variables:**

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_USE_MOCK_DATA` | Enable mock data mode | `true` |
| `VITE_MOCK_DELAY` | Simulated network delay (ms) | `800` |
| `VITE_API_BASE_URL` | Backend API URL | `http://localhost:8133` |

**Example .env.development:**

```env
VITE_USE_MOCK_DATA=true
VITE_MOCK_DELAY=800
VITE_API_BASE_URL=http://localhost:8133
```

### Build for Production

```bash
npm run build
```

This creates an optimized production build in the `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## Project Structure

```
frontend/
├── .claude/                  # Documentation for AI assistants
│   └── CLAUDE.md             # Lessons learned, patterns, debugging tips
├── src/
│   ├── api/                  # API client layer
│   │   └── trackerApi.ts     # Tracker API with mock/real switching
│   ├── components/           # Reusable UI components
│   │   ├── AnimatedTile.tsx
│   │   ├── NavTile.tsx
│   │   ├── Navbar.tsx
│   │   └── SearchLayout.tsx  # Shared layout for search pages
│   ├── constants/            # App constants
│   │   └── tiles.ts          # Tile configurations
│   ├── contexts/             # React contexts
│   │   └── NavigationContext.tsx  # URL routing & navigation state
│   ├── hooks/                # Custom React hooks
│   │   └── useTrackerSearch.ts
│   ├── mocks/                # Mock data for development
│   │   └── torrents.mock.ts
│   ├── pages/                # Page components
│   │   ├── Landing.tsx       # Landing page with animated tiles
│   │   ├── TrackerSearch.tsx # Tracker search with recent searches
│   │   └── Deluge.tsx        # Deluge manager (coming soon)
│   ├── theme/                # MUI theme configuration
│   │   └── theme.ts
│   ├── types/                # TypeScript type definitions
│   │   └── api.ts
│   ├── App.tsx               # Root component with AnimatePresence
│   ├── main.tsx              # Entry point
│   └── vite-env.d.ts         # Vite environment types
├── .env.development          # Development environment config
├── .env.production           # Production environment config
├── .env.example              # Environment template
├── DESIGN.md                 # Design system & component patterns
├── package.json
├── vite.config.ts
└── tsconfig.json
```

## Features

### Navigation & Routing
- **Client-side URL routing** - No React Router dependency, uses History API
- **Deep linking** - Direct URL access to any page (/, /search, /deluge)
- **Browser integration** - Back/forward buttons work correctly
- **Smooth transitions** - Landing page → page animations with Framer Motion
- **URL state sync** - Browser URL always reflects current view

### Landing Page
- **Animated background** - Organic blob shapes in pale yellow/red
- **Interactive tiles** - Deluge (blue) and Search (green) tiles
- **Smooth morphing** - Tile-to-navbar animation on navigation
- **Always rendered** - Enables smooth tile animations

### Tracker Search
- **Multi-tracker support** - Search 1337x.to, TorrentGalaxy, or Trunk (local)
- **Recent searches** - Stores last 5 searches in localStorage
- **One-click re-search** - Click recent search to run again
- **Modern card-based UI** - Clean, Material Design 3 aesthetic
- **Advanced sorting** - Sort by seeds, leeches, size, date, or name
- **Sort order toggle** - Ascending or descending with visual indicator
- **Copy to clipboard** - One-click copy of torrent links
- **Download integration** - Open torrents directly in new tab
- **Themed background** - Pale mint green matching design language
- **Fully responsive** - Mobile-optimized with proper spacing
- **Fixed layout** - Search bar stays visible, only results scroll
- **Loading states** - Spinner animations during search
- **Error handling** - User-friendly error messages
- **Empty states** - Helpful prompts and recent searches display
- **Snackbar notifications** - Visual feedback for user actions
- **Mock data support** - Full offline development capability

### Deluge Manager (Coming Soon)
- **Themed background** - Pale blue matching design language
- **Real-time updates** - WebSocket connection for live torrent status
- **Torrent filtering** - Search and filter active torrents
- **Coming soon placeholder** - UI structure in place

### Navbar
- **Fixed top navigation** - Always visible across all pages
- **Morphing animation** - Smooth transition from landing tiles
- **Active indicators** - Shows current page
- **ZOE branding** - Consistent branding across app

## Development Guide

### Using Mock Data

The mock data system automatically returns different results based on keywords:

- Keywords containing **"movie"** → Movie torrents
- Keywords containing **"series"** or **"s0"** → TV show torrents
- Keywords containing **"game"** → Game torrents
- Keywords containing **"software"** → Software torrents
- Keywords containing **"music"** → Music torrents
- Other keywords → Mixed results

**Try these searches in dev mode:**
- `movie` - Returns 5 movie torrents
- `series` - Returns 3 TV show torrents
- `game` - Returns 2 game torrents
- `software` - Returns 2 software torrents
- `music` - Returns 2 music torrents

### Adding New Mock Data

Edit `src/mocks/torrents.mock.ts` to add more mock scenarios:

```typescript
export const mockCustomTorrents: Torrents = [
  {
    link: "https://...",
    name: "Custom Torrent Name",
    size: "1.5 GB",
    seeds: 100,
    leeches: 20,
    date: "2024-11-02",
    contributor: "Uploader",
  },
];
```

### Creating API Hooks

Use the `useTrackerSearch` hook in your components:

```typescript
import { useTrackerSearch } from '../hooks/useTrackerSearch';
import { Tracker } from '../types/api';

const MyComponent = () => {
  const { results, isLoading, error, search } = useTrackerSearch();

  const handleSearch = () => {
    search(Tracker.ONE_THREE_THREE, "my keywords");
  };

  return (
    <div>
      <button onClick={handleSearch}>Search</button>
      {isLoading && <p>Loading...</p>}
      {error && <p>Error: {error.message}</p>}
      {results.map(torrent => <div key={torrent.link}>{torrent.name}</div>)}
    </div>
  );
};
```

### Checking Current Mode

You can check which mode is active:

```typescript
import { getApiConfig } from '../api/trackerApi';

const apiConfig = getApiConfig();
console.log('Using mock data:', apiConfig.useMockData);
console.log('Base URL:', apiConfig.baseUrl);
console.log('Mock delay:', apiConfig.mockDelay);
```

## API Reference

### Tracker Search

**Endpoint:** `GET /search/{tracker}/{keywords}`

**Parameters:**
- `tracker`: `"one-three-three"` | `"torrent-galaxy"` | `"trunk"`
- `keywords`: Search terms (URL encoded)

**Response:**
```typescript
[
  {
    link: string,
    name: string,
    size: string,
    seeds: number,
    leeches: number,
    date: string,
    contributor: string,
  }
]
```

### Select Torrent

**Endpoint:** `GET /search/{tracker}/{keywords}/select/{index}`

**Parameters:**
- `tracker`: Tracker name
- `keywords`: Original search terms
- `index`: Result index (0-based)

**Response:** Single torrent object with magnet link

## API Proxy

The Vite dev server proxies API requests to the Spring Boot backend:
- API: `http://localhost:8133`
- WebSocket: `ws://localhost:8133`

Requests to `/search`, `/deluge`, and `/ws` are automatically proxied.

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server (mock mode by default) |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |

## Troubleshooting

### Mock data not working

1. Check `.env.development` file exists
2. Verify `VITE_USE_MOCK_DATA=true`
3. Restart the dev server (Vite needs restart for env changes)

### Real API calls failing

1. Ensure backend is running on port 8133
2. Check `VITE_USE_MOCK_DATA=false` in `.env.development`
3. Verify `VITE_API_BASE_URL` points to correct backend URL
4. Check browser console for CORS errors

### Environment changes not applying

Environment variables are embedded at build time. You must:
1. Stop the dev server
2. Update `.env.development`
3. Restart the dev server with `npm run dev`

## Customization

Edit `src/theme/theme.ts` to customize:
- Colors and palette
- Typography
- Border radius
- Component defaults
- Dark/light mode

## Next Steps

### High Priority
- [ ] Implement torrent selection and magnet link handling
- [ ] Complete Deluge page with real-time torrent management
- [ ] Add dark mode toggle
- [ ] Add unit tests for NavigationContext and SearchLayout
- [ ] Add error boundaries for better error handling

### Medium Priority
- [ ] Add Storybook for component documentation
- [ ] Implement keyboard shortcuts (e.g., "/" for search)
- [ ] Add focus management for accessibility
- [ ] Optimize animations for lower-end devices
- [ ] Add loading skeletons for better perceived performance

### Low Priority
- [ ] Add page transition sound effects (optional)
- [ ] Add haptic feedback on mobile (optional)
- [ ] Implement gesture navigation (swipe to go back)

## Documentation

- **README.md** - Getting started, features, API reference
- **DESIGN.md** - Design system, typography, colors, component styling
- **.claude/CLAUDE.md** - Implementation notes, lessons learned, debugging tips
