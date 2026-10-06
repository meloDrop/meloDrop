CREATE TABLE users (
id            INT AUTO_INCREMENT PRIMARY KEY,
email         VARCHAR(255) NOT NULL,
username      VARCHAR(30)  NOT NULL,
password_hash VARCHAR(255) NOT NULL,
avatar_url    VARCHAR(255),
created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
CONSTRAINT uq_users_email    UNIQUE (email),
CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE songs (
id                INT AUTO_INCREMENT PRIMARY KEY,
isrc              VARCHAR(12),
title             VARCHAR(255) NOT NULL,
artist_name       VARCHAR(255) NOT NULL,
cover_url         VARCHAR(500),
deezer_track_id   BIGINT,
spotify_track_id  VARCHAR(500),
created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_songs_isrc            UNIQUE (isrc),
CONSTRAINT uq_songs_deezer_track_id UNIQUE (deezer_track_id)
);

CREATE TABLE genres (
id    INT AUTO_INCREMENT PRIMARY KEY,
label VARCHAR(50) NOT NULL,
CONSTRAINT uq_genres_label UNIQUE (label)
);

CREATE TABLE platform_links (
id               INT AUTO_INCREMENT PRIMARY KEY,
user_id          INT NOT NULL,
provider         ENUM('spotify', 'deezer') NOT NULL,
provider_user_id VARCHAR(255) NOT NULL,
access_token     TEXT NOT NULL,
refresh_token    TEXT,
expires_at       DATETIME,
deezer_playlist_id BIGINT,
spotify_playlist_id VARCHAR(64),
CONSTRAINT uq_platform_links_user_provider UNIQUE (user_id, provider),
CONSTRAINT uq_platform_links_provider_account UNIQUE (provider, provider_user_id),
CONSTRAINT fk_platform_links_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE drops (
id         INT AUTO_INCREMENT PRIMARY KEY,
user_id    INT NOT NULL,
song_id    INT NOT NULL,
expires_at DATETIME NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
rand_key   FLOAT NOT NULL,
INDEX idx_drops_rand_key (rand_key),
CONSTRAINT fk_drops_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
CONSTRAINT fk_drops_song_id FOREIGN KEY (song_id) REFERENCES songs(id)
);

CREATE TABLE drop_genres (
id       INT AUTO_INCREMENT PRIMARY KEY,
drop_id  INT NOT NULL,
genre_id INT NOT NULL,
CONSTRAINT uq_drop_genres_drop_genre UNIQUE (drop_id, genre_id),
CONSTRAINT fk_drop_genres_drop_id  FOREIGN KEY (drop_id)  REFERENCES drops(id) ON DELETE CASCADE,
CONSTRAINT fk_drop_genres_genre_id FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE saves (
id         INT AUTO_INCREMENT PRIMARY KEY,
user_id    INT NOT NULL,
drop_id    INT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_saves_user_drop UNIQUE (user_id, drop_id),
CONSTRAINT fk_saves_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
CONSTRAINT fk_saves_drop_id FOREIGN KEY (drop_id) REFERENCES drops(id) ON DELETE CASCADE
);

CREATE TABLE skips (
id         INT AUTO_INCREMENT PRIMARY KEY,
user_id    INT NOT NULL,
drop_id    INT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_skips_user_drop UNIQUE (user_id, drop_id),
CONSTRAINT fk_skips_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
CONSTRAINT fk_skips_drop_id FOREIGN KEY (drop_id) REFERENCES drops(id) ON DELETE CASCADE
);

CREATE TABLE genre_reports (
id         INT AUTO_INCREMENT PRIMARY KEY,
user_id    INT NOT NULL,
drop_id    INT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_genre_reports_user_drop UNIQUE (user_id, drop_id),
CONSTRAINT fk_genre_reports_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
CONSTRAINT fk_genre_reports_drop_id FOREIGN KEY (drop_id) REFERENCES drops(id) ON DELETE CASCADE
);

CREATE TABLE playlists (
id         INT AUTO_INCREMENT PRIMARY KEY,
user_id    INT NOT NULL,
name       VARCHAR(50) NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_playlists_user_name UNIQUE (user_id, name),
CONSTRAINT fk_playlists_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
