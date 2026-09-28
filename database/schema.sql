CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  username      VARCHAR(30)  NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE songs (
  id                INT AUTO_INCREMENT PRIMARY KEY,
  deezer_track_id   INT UNIQUE,
  isrc              VARCHAR(12) UNIQUE,
  title             VARCHAR(255) NOT NULL,
  artist            VARCHAR(255) NOT NULL,
  album             VARCHAR(255),
  cover_url         VARCHAR(500),
);

CREATE TABLE genres (
  id    INT AUTO_INCREMENT PRIMARY KEY,
  slug  VARCHAR(50) NOT NULL UNIQUE,
  label VARCHAR(50) NOT NULL
);

CREATE TABLE streaming_accounts (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  user_id          INT NOT NULL,
  provider         ENUM('spotify', 'deezer') NOT NULL,
  provider_user_id VARCHAR(255) NOT NULL,
  access_token     TEXT NOT NULL,
  refresh_token    TEXT,
  expires_at       DATETIME,
  UNIQUE (user_id, provider),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE drops (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,
  song_id    INT NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (song_id) REFERENCES songs(id)
);

CREATE TABLE drop_genres (
  drop_id  INT NOT NULL,
  genre_id INT NOT NULL,
  PRIMARY KEY (drop_id, genre_id),
  FOREIGN KEY (drop_id) REFERENCES drops(id) ON DELETE CASCADE,
  FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE saves (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,
  drop_id    INT NOT NULL,
  provider   ENUM('spotify', 'deezer') NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, drop_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (drop_id) REFERENCES drops(id) ON DELETE CASCADE
);

CREATE TABLE skips (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,
  drop_id    INT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, drop_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (drop_id) REFERENCES drops(id) ON DELETE CASCADE
);

CREATE TABLE reports (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT NOT NULL,
  drop_id    INT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, drop_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (drop_id) REFERENCES drops(id) ON DELETE CASCADE
);

CREATE TABLE preview_urls (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  song_id    INT NOT NULL UNIQUE,
  provider   ENUM('deezer', 'spotify') NOT NULL,
  url        VARCHAR(500) NOT NULL,
  fetched_at DATETIME NOT NULL,
  FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE
);