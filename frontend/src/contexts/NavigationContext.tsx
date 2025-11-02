import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { TILES } from '../constants/tiles';

export type ViewType = 'landing' | 'search' | 'deluge';

export interface NavigationItem {
  title: string;
  path: string;
  color: string;
  borderRadius: string;
}

interface NavigationContextType {
  navItems: NavigationItem[];               // Items shown in navbar
  addNavItem: (item: NavigationItem) => void;
  clearNavItems: () => void;
  currentView: ViewType;                    // Current page being displayed
  setCurrentView: (view: ViewType) => void; // Navigate to a view (also updates URL)
  navbarAnimationsComplete: boolean;        // Whether navbar tile morphing animations are done
  setNavbarAnimationsComplete: (complete: boolean) => void;
}

const NavigationContext = createContext<NavigationContextType | undefined>(undefined);

/**
 * Convert URL path to ViewType
 * Used when syncing URL to state (e.g., direct URL access, back/forward buttons)
 */
const pathToView = (path: string): ViewType => {
  switch (path) {
    case '/search':
      return 'search';
    case '/deluge':
      return 'deluge';
    default:
      return 'landing';
  }
};

/**
 * Convert ViewType to URL path
 * Used when updating URL to match current state
 */
const viewToPath = (view: ViewType): string => {
  switch (view) {
    case 'search':
      return '/search';
    case 'deluge':
      return '/deluge';
    case 'landing':
    default:
      return '/';
  }
};

/**
 * NavigationProvider - Manages app navigation and URL routing
 *
 * Architecture:
 * - State is source of truth (currentView)
 * - URL reflects state (updated via History API)
 * - Browser back/forward syncs URL → state
 *
 * Key Features:
 * - Direct URL access (e.g., /search) shows landing first, then transitions
 * - Browser back/forward buttons work correctly
 * - No React Router dependency (simpler for 3-page app)
 * - Smooth transitions using requestAnimationFrame
 */
export const NavigationProvider = ({ children }: { children: ReactNode }) => {
  // Always start with landing view for smooth tile animations
  const [navItems, setNavItems] = useState<NavigationItem[]>([]);
  const [currentView, setCurrentViewState] = useState<ViewType>('landing');
  const [navbarAnimationsComplete, setNavbarAnimationsComplete] = useState(false);

  /**
   * Add item to navbar (prevents duplicates)
   * Called when user navigates to a page
   */
  const addNavItem = (item: NavigationItem) => {
    setNavItems((prev) => {
      // Don't add duplicates
      if (prev.some((navItem) => navItem.path === item.path)) {
        return prev;
      }
      return [...prev, item];
    });
  };

  /**
   * Clear all navbar items
   * Used when returning to landing page
   */
  const clearNavItems = () => {
    setNavItems([]);
  };

  /**
   * Navigate to a view AND update URL
   * This is the public API - use this instead of setCurrentViewState
   */
  const setCurrentView = (view: ViewType) => {
    setCurrentViewState(view);
    const path = viewToPath(view);
    // Update URL without page reload (pushState)
    window.history.pushState({}, '', path);
  };

  /**
   * Handle direct URL access (e.g., user types /search in address bar)
   * Strategy: Show landing first, then transition to target view
   * This enables smooth tile-to-navbar animations
   */
  useEffect(() => {
    const targetView = pathToView(window.location.pathname);

    if (targetView !== 'landing') {
      // Use requestAnimationFrame for smooth transition
      requestAnimationFrame(() => {
        // Populate navbar with all tiles
        TILES.forEach(tile => {
          addNavItem({
            title: tile.title,
            path: tile.path,
            color: tile.color,
            borderRadius: tile.borderRadius,
          });
        });

        // Transition to target view
        setCurrentViewState(targetView);
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only run once on mount

  /**
   * Listen to browser back/forward buttons (popstate event)
   * Syncs URL changes → state changes
   */
  useEffect(() => {
    const handlePopState = () => {
      setCurrentViewState(pathToView(window.location.pathname));
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  return (
    <NavigationContext.Provider value={{
      navItems,
      addNavItem,
      clearNavItems,
      currentView,
      setCurrentView,
      navbarAnimationsComplete,
      setNavbarAnimationsComplete
    }}>
      {children}
    </NavigationContext.Provider>
  );
};

export const useNavigation = () => {
  const context = useContext(NavigationContext);
  if (!context) {
    throw new Error('useNavigation must be used within NavigationProvider');
  }
  return context;
};
