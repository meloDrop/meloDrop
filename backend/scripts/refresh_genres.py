"""One-off script: refresh the genre of every song already in the database."""

import time

from app.database import SessionLocal
from app.exceptions import DeezerError, TrackNotFoundError
from app.models.song import Song
from app.providers.deezer import DeezerProvider
from app.services.drops import get_album_genre

# Deezer allows about 50 calls every 5 seconds
PAUSE = 0.2


def main():
    db = SessionLocal()
    provider = DeezerProvider()
    updated = 0
    try:
        for song in db.query(Song).all():
            try:
                track = provider.get_track(str(song.deezer_track_id))
            except (TrackNotFoundError, DeezerError):
                print(f"skip {song.id}: not found on Deezer")
                continue

            genre = get_album_genre(track["album"]["id"])
            if genre and genre != song.genre:
                print(f"{song.id}: {song.genre} -> {genre}")
                song.genre = genre
                updated += 1
            time.sleep(PAUSE)

        db.commit()
        print(f"{updated} song(s) updated")
    finally:
        db.close()


if __name__ == "__main__":
    main()