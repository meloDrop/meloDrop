// GET /songs/{song_id}/preview: a fresh link, it expires after about 15 min
export type Preview = {
    preview_url: string | null;
};