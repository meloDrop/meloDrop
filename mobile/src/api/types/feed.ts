import type { UserBrief } from './user';

// GET /feed: one card of the Discover feed (one card per song, ADR-0021)
export type FeedItem = {
    id: number; // first drop of the song: used for /save, /skip and /pick
    song_id: number; // meloDrop id of the song: used for the preview
    title: string;
    artist: string;
    cover_url: string | null;
    genre: string | null;
    author_id: number;
    author_username: string;
    saves_count: number;
    is_followed: boolean;
    droppers: UserBrief[] | null;
};