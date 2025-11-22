export interface TileDefinition {
  id: string;
  title: string;
  path: string;
  color: string;
  borderRadius: string;
}

export const TILES: TileDefinition[] = [
  {
    id: 'zoe',
    title: 'ZOE',
    path: '/',
    color: 'transparent',
    borderRadius: '50px'  // Pill shape
  },
  {
    id: 'deluge',
    title: 'Deluge',
    path: '/deluge',
    color: '#d4ebfc',
    borderRadius: '20%'  // Proportional rounded square
  },
  {
    id: 'search',
    title: 'Search',
    path: '/search',
    color: '#b8f0d4',
    borderRadius: '50%'  // Perfect circle
  },
];
