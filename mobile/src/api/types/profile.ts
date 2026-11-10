// A drop shown on a profile
export type ProfileDrop = {
    id: number;
    song_id: number;
    title: string;
    artist: string;
    cover_url: string | null;
    genre: string | null;
    dropped_at: string;
    saves_count: number;
    picks_count: number;
    views_count: number;
};

// A song on the podium of the Mixtape
export type PodiumSong = {
    place: number; // 1, 2 or 3
    song_id: number;
    deezer_track_id: number;
    title: string;
    artist: string;
    cover_url: string | null;
};

// A song of the Mixtape under a label, like "cry"
export type LabeledSong = {
    label: string;
    song_id: number;
    deezer_track_id: number;
    title: string;
    artist: string;
    cover_url: string | null;
};

// GET /users/{user_id}/profile: same route for my profile and the others (ADR-0018)
export type Profile = {
    id: number;
    username: string;
    display_name: string | null;
    created_at: string;
    avatar_color: string;
    avatar_style: string;
    stats: {
        drops_count: number;
        likes_received: number;
        likes_count: number;
    };
    current_drop: ProfileDrop | null;
    recent_drops: ProfileDrop[];
    podium: PodiumSong[];
    labeled_songs: LabeledSong[];
    followers_count: number;
    following_count: number;
    is_followed: boolean;
    follows_you: boolean;
    is_me: boolean;
};