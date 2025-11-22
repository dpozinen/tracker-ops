import { Tracker, type Torrents, type DelugeTorrents } from "../types/api";

// Mock data for popular movie search
export const mockMovieTorrents: Torrents = [
  {
    link: "https://1337x.to/torrent/12345/Example-Movie-2024-1080p-BluRay",
    name: "Example Movie 2024 1080p BluRay x264",
    size: "2.1 GB",
    seeds: 847,
    leeches: 123,
    date: "2024-10-15",
    contributor: "MovieUploader",
  },
  {
    link: "https://1337x.to/torrent/12346/Example-Movie-2024-2160p-WEB-DL",
    name: "Example Movie 2024 2160p WEB-DL HDR x265",
    size: "8.5 GB",
    seeds: 423,
    leeches: 87,
    date: "2024-10-20",
    contributor: "4KMaster",
  },
  {
    link: "https://1337x.to/torrent/12347/Example-Movie-2024-720p-HDTV",
    name: "Example Movie 2024 720p HDTV x264",
    size: "950 MB",
    seeds: 1205,
    leeches: 234,
    date: "2024-10-10",
    contributor: "QuickRipper",
  },
  {
    link: "https://1337x.to/torrent/12348/Example-Movie-2024-REMUX",
    name: "Example Movie 2024 REMUX 2160p BluRay DTS-HD MA 7.1 x265",
    size: "45.2 GB",
    seeds: 89,
    leeches: 34,
    date: "2024-10-25",
    contributor: "RemuxTeam",
  },
  {
    link: "https://1337x.to/torrent/12349/Example-Movie-2024-1080p-HEVC",
    name: "Example Movie 2024 1080p BluRay HEVC x265",
    size: "1.4 GB",
    seeds: 656,
    leeches: 145,
    date: "2024-10-18",
    contributor: "HEVCGuru",
  },
  {
    link: "https://1337x.to/torrent/12350/Another-Movie-2024-1080p",
    name: "Another Movie 2024 1080p WEB-DL x264 DD5.1",
    size: "3.2 GB",
    seeds: 512,
    leeches: 98,
    date: "2024-10-12",
    contributor: "WebRipper",
  },
  {
    link: "https://1337x.to/torrent/12351/Classic-Film-1080p-Restored",
    name: "Classic Film 1080p BluRay Restored Edition x264",
    size: "4.5 GB",
    seeds: 234,
    leeches: 45,
    date: "2024-10-08",
    contributor: "ClassicRestore",
  },
  {
    link: "https://1337x.to/torrent/12352/Action-Thriller-2024-4K",
    name: "Action Thriller 2024 2160p UHD BluRay HDR x265",
    size: "12.8 GB",
    seeds: 389,
    leeches: 156,
    date: "2024-10-22",
    contributor: "UHDKing",
  },
  {
    link: "https://1337x.to/torrent/12353/Comedy-Special-2024",
    name: "Comedy Special 2024 1080p WEBRip x264 AAC",
    size: "1.9 GB",
    seeds: 678,
    leeches: 123,
    date: "2024-10-19",
    contributor: "ComedyFan",
  },
  {
    link: "https://1337x.to/torrent/12354/Documentary-Series-Complete",
    name: "Documentary Series Complete 720p HDTV x264",
    size: "8.4 GB",
    seeds: 145,
    leeches: 67,
    date: "2024-10-05",
    contributor: "DocsArchive",
  },
];

// Mock data for TV show search
export const mockTvShowTorrents: Torrents = [
  {
    link: "https://1337x.to/torrent/23456/Popular-Series-S05E01",
    name: "Popular Series S05E01 1080p WEB-DL DD5.1 H264",
    size: "1.8 GB",
    seeds: 2341,
    leeches: 567,
    date: "2024-11-01",
    contributor: "TVReleaser",
  },
  {
    link: "https://1337x.to/torrent/23457/Popular-Series-S05E01-720p",
    name: "Popular Series S05E01 720p HDTV x264",
    size: "650 MB",
    seeds: 1876,
    leeches: 423,
    date: "2024-11-01",
    contributor: "FastTV",
  },
  {
    link: "https://1337x.to/torrent/23458/Popular-Series-S05-Complete",
    name: "Popular Series Season 5 Complete 1080p WEB-DL x265",
    size: "12.4 GB",
    seeds: 345,
    leeches: 89,
    date: "2024-10-28",
    contributor: "SeasonPack",
  },
];

// Mock data for software search
export const mockSoftwareTorrents: Torrents = [
  {
    link: "https://1337x.to/torrent/34567/Example-Software-2024",
    name: "Example Software Pro 2024 v10.5.2 (x64) + Crack",
    size: "485 MB",
    seeds: 234,
    leeches: 67,
    date: "2024-10-22",
    contributor: "SoftwareCracker",
  },
  {
    link: "https://1337x.to/torrent/34568/Example-Software-Portable",
    name: "Example Software Portable 2024 v10.5.1",
    size: "320 MB",
    seeds: 156,
    leeches: 34,
    date: "2024-10-15",
    contributor: "PortableApps",
  },
];

// Mock data for game search
export const mockGameTorrents: Torrents = [
  {
    link: "https://1337x.to/torrent/45678/Awesome-Game-2024",
    name: "Awesome Game 2024 Deluxe Edition [PC] [Repack]",
    size: "35.2 GB",
    seeds: 1543,
    leeches: 678,
    date: "2024-10-30",
    contributor: "GameRepackers",
  },
  {
    link: "https://1337x.to/torrent/45679/Awesome-Game-Update",
    name: "Awesome Game 2024 Update v1.2.5 + DLC",
    size: "4.7 GB",
    seeds: 789,
    leeches: 234,
    date: "2024-11-01",
    contributor: "GameUpdates",
  },
];

// Mock data for music search
export const mockMusicTorrents: Torrents = [
  {
    link: "https://1337x.to/torrent/56789/Artist-Album-2024",
    name: "Artist Name - Album Title (2024) [FLAC]",
    size: "450 MB",
    seeds: 234,
    leeches: 45,
    date: "2024-10-20",
    contributor: "MusicLover",
  },
  {
    link: "https://1337x.to/torrent/56790/Artist-Album-MP3",
    name: "Artist Name - Album Title (2024) [MP3 320kbps]",
    size: "125 MB",
    seeds: 567,
    leeches: 89,
    date: "2024-10-20",
    contributor: "MP3Master",
  },
];

// Mock empty results
export const mockEmptyResults: Torrents = [];

// Mock data mapper based on keywords
export const getMockDataForKeywords = (_keywords: string): Torrents => {
  // Always return all movie torrents for testing scroll
  return mockMovieTorrents;
};

// Mock different tracker responses (slight variations)
export const getMockDataForTracker = (tracker: Tracker, keywords: string): Torrents => {
  const baseData = getMockDataForKeywords(keywords);

  switch (tracker) {
    case Tracker.ONE_THREE_THREE:
      return baseData;

    case Tracker.TORRENT_GALAXY:
      // Slightly different results for TorrentGalaxy
      return baseData.map(torrent => ({
        ...torrent,
        seeds: Math.floor(torrent.seeds * 0.8),
        leeches: Math.floor(torrent.leeches * 1.2),
        contributor: `TG_${torrent.contributor}`,
      }));

    case Tracker.TRUNK:
      // Trunk (local) - fewer results, no contributor
      return baseData.slice(0, 3).map(torrent => ({
        ...torrent,
        contributor: "Local DB",
        seeds: 0,
        leeches: 0,
      }));

    default:
      return baseData;
  }
};

// Mock data for Deluge torrents (active downloads/seeds)
export const mockDelugeTorrents: DelugeTorrents = [
  {
    id: "d1",
    name: "Ubuntu 24.04 LTS Desktop (64-bit)",
    size: "5.8 GB",
    state: "Seeding",
    progress: 100,
    ratio: "2.45",
    uploaded: "14.2 GB",
    downloadSpeed: "0 KiB/s",
    uploadSpeed: "1.2 MiB/s",
    eta: "∞",
    date: "2024-10-15",
  },
  {
    id: "d2",
    name: "Example Movie 2024 1080p BluRay x264",
    size: "2.1 GB",
    state: "Downloading",
    progress: 67,
    ratio: "0.15",
    uploaded: "315 MB",
    downloadSpeed: "3.5 MiB/s",
    uploadSpeed: "450 KiB/s",
    eta: "5m 23s",
    date: "2024-11-02",
  },
  {
    id: "d3",
    name: "Popular Series S05E01 1080p WEB-DL DD5.1 H264",
    size: "1.8 GB",
    state: "Paused",
    progress: 42,
    ratio: "0.02",
    uploaded: "35 MB",
    downloadSpeed: "0 KiB/s",
    uploadSpeed: "0 KiB/s",
    eta: "∞",
    date: "2024-11-01",
  },
  {
    id: "d4",
    name: "Awesome Game 2024 Deluxe Edition [PC] [Repack]",
    size: "35.2 GB",
    state: "Downloading",
    progress: 23,
    ratio: "0.08",
    uploaded: "2.8 GB",
    downloadSpeed: "8.2 MiB/s",
    uploadSpeed: "1.8 MiB/s",
    eta: "1h 47m",
    date: "2024-10-30",
  },
  {
    id: "d5",
    name: "Artist Name - Album Title (2024) [FLAC]",
    size: "450 MB",
    state: "Seeding",
    progress: 100,
    ratio: "5.67",
    uploaded: "2.6 GB",
    downloadSpeed: "0 KiB/s",
    uploadSpeed: "850 KiB/s",
    eta: "∞",
    date: "2024-10-20",
  },
  {
    id: "d6",
    name: "Documentary Series Complete 720p HDTV x264",
    size: "8.4 GB",
    state: "Checking",
    progress: 100,
    ratio: "0.00",
    uploaded: "0 B",
    downloadSpeed: "0 KiB/s",
    uploadSpeed: "0 KiB/s",
    eta: "∞",
    date: "2024-10-05",
  },
  {
    id: "d7",
    name: "Example Software Pro 2024 v10.5.2 (x64) + Crack",
    size: "485 MB",
    state: "Downloading",
    progress: 89,
    ratio: "0.12",
    uploaded: "58 MB",
    downloadSpeed: "2.1 MiB/s",
    uploadSpeed: "320 KiB/s",
    eta: "2m 15s",
    date: "2024-10-22",
  },
  {
    id: "d8",
    name: "Classic Film 1080p BluRay Restored Edition x264",
    size: "4.5 GB",
    state: "Seeding",
    progress: 100,
    ratio: "1.23",
    uploaded: "5.5 GB",
    downloadSpeed: "0 KiB/s",
    uploadSpeed: "650 KiB/s",
    eta: "∞",
    date: "2024-10-08",
  },
  {
    id: "d9",
    name: "Broken Download - Tracker Connection Failed",
    size: "12.3 GB",
    state: "Error",
    progress: 34,
    ratio: "0.00",
    uploaded: "0 B",
    downloadSpeed: "0 KiB/s",
    uploadSpeed: "0 KiB/s",
    eta: "∞",
    date: "2024-11-03",
  },
];
