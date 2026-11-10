import type { UserBrief } from './user';

// GET /me/likes: one song of "Mes likes"
export type LikedSong = {
    song_id: number;
    title: string;
    artist: string;
    cover_url: string | null;
    liked_at: string;
    author_id: number | null; // null if the author deleted the account
    author_username: string | null;
    liked_late: boolean; // rescued from "Mes skips" after the drop expired
    is_pick: boolean;
    genre: string | null;
    droppers: UserBrief[] | null;
};