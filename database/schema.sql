CREATE TABLE users (
id            INT AUTO_INCREMENT PRIMARY KEY,
email         VARCHAR(255) NOT NULL,
username      VARCHAR(30)  NOT NULL,
password_hash VARCHAR(255) NOT NULL,
avatar_url    VARCHAR(255),
created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
email_verified BOOLEAN NOT NULL DEFAULT FALSE,
pending_email VARCHAR(255) NULL,
CONSTRAINT uq_users_email    UNIQUE (email),
CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE email_codes (
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
purpose ENUM('verify_email', 'reset_password', 'change_email') NOT NULL,
code_hash VARCHAR(255) NOT NULL,
attempts INT NOT NULL DEFAULT 0,
expires_at DATETIME NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_email_codes_user_id_purpose UNIQUE (user_id, purpose),
CONSTRAINT fk_email_codes_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE songs (
id                INT AUTO_INCREMENT PRIMARY KEY,
isrc              VARCHAR(12),
title             VARCHAR(255) NOT NULL,
artist_name       VARCHAR(255) NOT NULL,
cover_url         VARCHAR(500),
genre             VARCHAR(50),
deezer_track_id   BIGINT,
spotify_track_id  VARCHAR(500),
created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_songs_isrc            UNIQUE (isrc),
CONSTRAINT uq_songs_deezer_track_id UNIQUE (deezer_track_id)
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

CREATE TABLE liked_songs (
id         INT AUTO_INCREMENT PRIMARY KEY,
user_id    INT NOT NULL,
song_id    INT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT uq_liked_songs_user_song UNIQUE (user_id, song_id),
CONSTRAINT fk_liked_songs_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
CONSTRAINT fk_liked_songs_song_id FOREIGN KEY (song_id) REFERENCES songs(id)
);