import type { Torrents, SearchParams, ApiError } from "../types/api";
import { Tracker } from "../types/api";
import { getMockDataForTracker } from "../mocks/torrents.mock";

// Configuration
const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === "true";
const MOCK_DELAY_MS = parseInt(import.meta.env.VITE_MOCK_DELAY || "800");

// Build API base URL dynamically like Deluge WebSocket does
const getApiBaseUrl = (): string => {
  // In development with Vite proxy, or in production served from same origin
  return `${window.location.protocol}//${window.location.host}/api`;
};

// Helper function to simulate network delay
const delay = (ms: number): Promise<void> =>
  new Promise(resolve => setTimeout(resolve, ms));

/**
 * Search torrents using the backend API or mock data
 */
export const searchTorrents = async (params: SearchParams): Promise<Torrents> => {
  const { tracker, keywords } = params;

  // Mock mode
  if (USE_MOCK_DATA) {
    console.log(`[MOCK] Searching ${tracker} for "${keywords}"`);
    await delay(MOCK_DELAY_MS);

    const mockData = getMockDataForTracker(tracker, keywords);
    console.log(`[MOCK] Returned ${mockData.length} results`);

    return mockData;
  }

  // Real API mode
  try {
    const encodedKeywords = encodeURIComponent(keywords);
    const url = `${getApiBaseUrl()}/search/${tracker}/${encodedKeywords}`;

    console.log(`[API] Fetching: ${url}`);

    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: Torrents = await response.json();
    console.log(`[API] Returned ${data.length} results`);

    return data;
  } catch (error) {
    console.error("[API] Search failed:", error);
    throw {
      message: error instanceof Error ? error.message : "Failed to search torrents",
      status: error instanceof Response ? error.status : undefined,
    } as ApiError;
  }
};

/**
 * Get torrent details by selecting from search results
 */
export const selectTorrent = async (
  tracker: Tracker,
  keywords: string,
  index: number
): Promise<Torrents[0]> => {
  // Mock mode
  if (USE_MOCK_DATA) {
    console.log(`[MOCK] Selecting torrent at index ${index}`);
    await delay(MOCK_DELAY_MS / 2);

    const mockData = getMockDataForTracker(tracker, keywords);

    if (index < 0 || index >= mockData.length) {
      throw {
        message: `Invalid index: ${index}`,
        status: 400,
      } as ApiError;
    }

    return mockData[index];
  }

  // Real API mode
  try {
    const encodedKeywords = encodeURIComponent(keywords);
    const url = `${getApiBaseUrl()}/search/${tracker}/${encodedKeywords}/select/${index}`;

    console.log(`[API] Fetching: ${url}`);

    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: Torrents[0] = await response.json();
    console.log(`[API] Selected torrent:`, data.name);

    return data;
  } catch (error) {
    console.error("[API] Select failed:", error);
    throw {
      message: error instanceof Error ? error.message : "Failed to select torrent",
      status: error instanceof Response ? error.status : undefined,
    } as ApiError;
  }
};

/**
 * Get current API configuration (useful for debugging)
 */
export const getApiConfig = () => ({
  baseUrl: getApiBaseUrl(),
  useMockData: USE_MOCK_DATA,
  mockDelay: MOCK_DELAY_MS,
});
