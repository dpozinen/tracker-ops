export interface Torrent {
  link: string;
  name: string;
  size: string;
  seeds: number;
  leeches: number;
  date: string;
  contributor: string;
}

export type Torrents = Torrent[];

export const Tracker = {
  ONE_THREE_THREE: "one-three-three",
  TORRENT_GALAXY: "torrent-galaxy",
  TRUNK: "trunk",
} as const;

export type Tracker = (typeof Tracker)[keyof typeof Tracker];

export interface SearchParams {
  tracker: Tracker;
  keywords: string;
}

export interface SearchResponse {
  results: Torrents;
  tracker: Tracker;
  keywords: string;
}

export interface ApiError {
  message: string;
  status?: number;
}

export interface DelugeTorrent {
  id: string;
  name: string;
  size: string;
  state: 'Downloading' | 'Seeding' | 'Paused' | 'Error' | 'Checking' | 'Queued';
  progress: number;          // Percentage 0-100
  ratio: string;
  uploaded: string;
  downloadSpeed: string;
  uploadSpeed: string;
  eta: string;
  date: string;
}

export type DelugeTorrents = DelugeTorrent[];
