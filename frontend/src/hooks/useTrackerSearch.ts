import { useState, useCallback, useEffect } from "react";
import type { Torrents, ApiError } from "../types/api";
import { Tracker } from "../types/api";
import { searchTorrents, selectTorrent } from "../api/trackerApi";

interface UseTrackerSearchState {
  results: Torrents;
  isLoading: boolean;
  error: ApiError | null;
}

interface UseTrackerSearchReturn extends UseTrackerSearchState {
  search: (tracker: Tracker, keywords: string) => Promise<void>;
  selectResult: (tracker: Tracker, keywords: string, index: number) => Promise<void>;
  clearResults: () => void;
  clearError: () => void;
}

/**
 * Custom hook for tracker search functionality
 * Handles loading states, errors, and result management
 * Persists results in sessionStorage to maintain state across tab switches
 */
export const useTrackerSearch = (): UseTrackerSearchReturn => {
  // Load initial state from sessionStorage if available
  const loadInitialState = (): UseTrackerSearchState => {
    try {
      const saved = sessionStorage.getItem('trackerSearchResults');
      if (saved) {
        const parsed = JSON.parse(saved);
        return {
          results: parsed.results || [],
          isLoading: false,
          error: null,
        };
      }
    } catch (e) {
      console.error('Failed to load saved search results:', e);
    }
    return {
      results: [],
      isLoading: false,
      error: null,
    };
  };

  const [state, setState] = useState<UseTrackerSearchState>(loadInitialState());

  // Persist results to sessionStorage whenever they change
  useEffect(() => {
    try {
      sessionStorage.setItem('trackerSearchResults', JSON.stringify({
        results: state.results,
      }));
    } catch (e) {
      console.error('Failed to save search results:', e);
    }
  }, [state.results]);

  const search = useCallback(async (tracker: Tracker, keywords: string) => {
    if (!keywords.trim()) {
      setState(prev => ({
        ...prev,
        error: { message: "Please enter search keywords" },
      }));
      return;
    }

    setState(prev => ({ ...prev, isLoading: true, error: null }));

    try {
      const results = await searchTorrents({ tracker, keywords });
      setState({ results, isLoading: false, error: null });
    } catch (error) {
      setState({
        results: [],
        isLoading: false,
        error: error as ApiError,
      });
    }
  }, []);

  const selectResult = useCallback(
    async (tracker: Tracker, keywords: string, index: number) => {
      setState(prev => ({ ...prev, isLoading: true, error: null }));

      try {
        const selectedTorrent = await selectTorrent(tracker, keywords, index);
        // You can handle the selected torrent here (e.g., copy magnet link)
        console.log("Selected torrent:", selectedTorrent);
        setState(prev => ({ ...prev, isLoading: false }));
      } catch (error) {
        setState(prev => ({
          ...prev,
          isLoading: false,
          error: error as ApiError,
        }));
      }
    },
    []
  );

  const clearResults = useCallback(() => {
    setState(prev => ({ ...prev, results: [] }));
    // Also clear from sessionStorage
    try {
      sessionStorage.removeItem('trackerSearchResults');
    } catch (e) {
      console.error('Failed to clear saved search results:', e);
    }
  }, []);

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
  }, []);

  return {
    ...state,
    search,
    selectResult,
    clearResults,
    clearError,
  };
};
