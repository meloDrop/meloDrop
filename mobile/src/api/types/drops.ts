// GET /catalog/search: a track found on Deezer
export type TrackResult = {
    deezer_track_id: number;
    title: string;
    artist: string;
    cover_url: string | null;
    preview_url: string | null;
};

// POST /drops: the drop just created
export type NewDrop = {
    song_id: number;
    title: string;
    artist: string;
    cover_url: string | null;
    genre: string | null;
    expires_at: string; // UTC, like "2026-11-09T20:35:00+00:00"
};

// GET /drops/me: the drop online today (the backend sends null if none)
export type MyDrop = {
    id: number;
    song_id: number;
    title: string;
    artist: string;
    cover_url: string | null;
    genre: string | null;
    expires_at: string;
    saves_count: number; // likes, picks included
    picks_count: number;
    views_count: number;
};