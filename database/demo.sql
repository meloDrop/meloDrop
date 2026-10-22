-- Demo data for development and tests only: never load it in production.
-- MySQL runs it after schema.sql, only when the database volume is empty:
-- the first `docker compose up`, or the first one after `docker compose down -v`.
--
-- Every account has a confirmed email and the password Password1!
-- Log in to the app with "lito": no drop of the day, so the whole feed is to discover.
-- Dates are counted from the loading time (UTC_TIMESTAMP), so the drops of the day
-- stay online for the next hours, whatever the day of the reset.

SET NAMES utf8mb4;

-- Argon2 hash of "Password1!", made once with hash_password()
SET @password_hash = '$argon2id$v=19$m=65536,t=3,p=4$23vxyL1/OB+mNxxD7XHaGg$UR/XDOSzE7QxfuZCUAgaIQAARPypk3fNjC5yvxYwjkc';


-- ======================================================================
-- Accounts
-- ======================================================================

INSERT INTO users (id, email, username, password_hash, email_verified, created_at) VALUES
(1, 'litobernier@gmail.com', 'litoxam', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 45 DAY),
(2, 'elliot@example.com', 'elliot', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 60 DAY),
(3, 'sam@example.com', 'sam', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 52 DAY),
(4, 'nina@example.com', 'nina', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 40 DAY),
(5, 'jules@example.com', 'jules', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 38 DAY),
(6, 'ines@example.com', 'ines', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 35 DAY),
(7, 'tom@example.com', 'tom', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 33 DAY),
(8, 'lea@example.com', 'lea', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 30 DAY),
(9, 'hugo@example.com', 'hugo', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 28 DAY),
(10, 'chloe@example.com', 'chloe', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 26 DAY),
(11, 'lucas@example.com', 'lucas', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 24 DAY),
(12, 'emma@example.com', 'emma', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 22 DAY),
(13, 'noah@example.com', 'noah', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 20 DAY),
(14, 'jade@example.com', 'jade', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 18 DAY),
(15, 'louis@example.com', 'louis', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 16 DAY),
(16, 'manon@example.com', 'manon', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 14 DAY),
(17, 'adam@example.com', 'adam', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 12 DAY),
(18, 'camille@example.com', 'camille', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 11 DAY),
(19, 'theo@example.com', 'theo', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 10 DAY),
(20, 'sarah@example.com', 'sarah', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 9 DAY),
(21, 'paul@example.com', 'paul', @password_hash, TRUE, UTC_TIMESTAMP() - INTERVAL 8 DAY);


-- ======================================================================
-- Songs: real Deezer data, so the covers, previews and genres work.
-- La Vie en Rose has no genre on Deezer: it tests a card without a chip.
-- ======================================================================

INSERT INTO songs (id, isrc, title, artist_name, cover_url, genre, deezer_track_id) VALUES
(1, 'USWB10300474', 'Numb', 'Linkin Park',
 'https://cdn-images.dzcdn.net/images/cover/44df4f6fb2534768f4924365c103d0f7/500x500-000000-80-0-0.jpg', 'Alternative', 677232),
(2, 'USQX91300809', 'Get Lucky (Radio Edit - feat. Pharrell Williams and Nile Rodgers)', 'Daft Punk',
 'https://cdn-images.dzcdn.net/images/cover/bc49adb87758e0c8c4e508a9c5cce85d/500x500-000000-80-0-0.jpg', 'Electro', 66609426),
(3, 'FRUM70902424', 'Alors on danse (Radio Edit)', 'Stromae',
 'https://cdn-images.dzcdn.net/images/cover/43bd78a4753df33da9efc2207c4286ee/500x500-000000-80-0-0.jpg', 'Dance', 6297555),
(4, 'USUM71703085', 'HUMBLE.', 'Kendrick Lamar',
 'https://cdn-images.dzcdn.net/images/cover/7ce6b8452fae425557067db6e6a1cad5/500x500-000000-80-0-0.jpg', 'Rap/Hip Hop', 350171311),
(5, 'GBUM70603730', 'Rehab', 'Amy Winehouse',
 'https://cdn-images.dzcdn.net/images/cover/5772b495f0dcdf660d0fc88c4c38a3fa/500x500-000000-80-0-0.jpg', 'R&B', 2176852),
(6, 'USIR27800259', 'Is This Love', 'Bob Marley & The Wailers',
 'https://cdn-images.dzcdn.net/images/cover/6367879e2e914efb1a5e546c7492ca0f/500x500-000000-80-0-0.jpg', 'Reggae', 2293791),
(7, 'USSM15900113', 'So What (feat. John Coltrane, Cannonball Adderley & Bill Evans)', 'Miles Davis',
 'https://cdn-images.dzcdn.net/images/cover/fb9fbd93db667e24d4f5dee781e83f7d/500x500-000000-80-0-0.jpg', 'Jazz', 2711778),
(8, 'USGF19942501', 'Smells Like Teen Spirit', 'Nirvana',
 'https://cdn-images.dzcdn.net/images/cover/f0282817b697279e56df13909962a54a/500x500-000000-80-0-0.jpg', 'Rock', 13791930),
(9, 'GBUM71029604', 'Bohemian Rhapsody', 'Queen',
 'https://cdn-images.dzcdn.net/images/cover/0b56f4eee05fa1ea753c5654b2cdb70c/500x500-000000-80-0-0.jpg', 'Rock', 4091937401),
(10, 'USSM19902991', 'Billie Jean', 'Michael Jackson',
 'https://cdn-images.dzcdn.net/images/cover/a0ad67d1beb761f2cb9f8b60e5bcf07a/500x500-000000-80-0-0.jpg', 'Pop', 4603408),
(11, 'GBBKS1000335', 'Rolling in the Deep', 'Adele',
 'https://cdn-images.dzcdn.net/images/cover/dc1ce848d830ecc93521be5a78350364/500x500-000000-80-0-0.jpg', 'Pop', 8086126),
(12, 'QMKHM1900001', 'Enter Sandman (Remastered 2021)', 'Metallica',
 'https://cdn-images.dzcdn.net/images/cover/4f2093c9d25852c8f1937ae5a47b99a6/500x500-000000-80-0-0.jpg', 'Rock', 1483825212),
(13, 'USUM71900764', 'bad guy', 'Billie Eilish',
 'https://cdn-images.dzcdn.net/images/cover/6630083f454d48eadb6a9b53f035d734/500x500-000000-80-0-0.jpg', 'Alternative', 655095912),
(14, 'USUG11904206', 'Blinding Lights', 'The Weeknd',
 'https://cdn-images.dzcdn.net/images/cover/fd00ebd6d30d7253f813dba3bb1c66a9/500x500-000000-80-0-0.jpg', 'R&B', 908604612),
(15, 'BE8HB1800006', 'Balance ton quoi', 'Angèle',
 'https://cdn-images.dzcdn.net/images/cover/4a2360324af313f73b56fd1f7faaac88/500x500-000000-80-0-0.jpg', 'Pop', 546875572),
(16, 'FR8JU1900010', 'Au DD', 'PNL',
 'https://cdn-images.dzcdn.net/images/cover/ff5caf314549e1cff1960c5b2acfd384/500x500-000000-80-0-0.jpg', 'Rap/Hip Hop', 653159322),
(17, 'USWB10400046', 'Dreams (2004 Remaster)', 'Fleetwood Mac',
 'https://cdn-images.dzcdn.net/images/cover/9732751ce91d786dcf30069853697078/500x500-000000-80-0-0.jpg', 'Rock', 63480987),
(18, 'USIR10211559', 'Lose Yourself', 'Eminem',
 'https://cdn-images.dzcdn.net/images/cover/e2b36a9fda865cb2e9ed1476b6291a7d/500x500-000000-80-0-0.jpg', 'Rap/Hip Hop', 1109731),
(19, 'GBAHT1901299', 'Levitating', 'Dua Lipa',
 'https://cdn-images.dzcdn.net/images/cover/f8364f090ba04f1b19b381ec0390f3e4/500x500-000000-80-0-0.jpg', 'Pop', 1124841682),
(20, 'USKYW0700945', 'La Vie en Rose', 'Edith Piaf',
 'https://cdn-images.dzcdn.net/images/cover/6e6dde2c332dcb75e76f302f19de636c/500x500-000000-80-0-0.jpg', NULL, 73693857);


-- ======================================================================
-- Drops of the day: one per account except lito, all still online.
-- created_at + 24 hours = expires_at, like in services/drops.py
-- ======================================================================

INSERT INTO drops (id, user_id, song_id, created_at, expires_at, rand_key) VALUES
(1, 2, 1, UTC_TIMESTAMP() - INTERVAL 3 HOUR, UTC_TIMESTAMP() + INTERVAL 21 HOUR, RAND()),  -- elliot: Linkin Park - Numb
(2, 3, 2, UTC_TIMESTAMP() - INTERVAL 5 HOUR, UTC_TIMESTAMP() + INTERVAL 19 HOUR, RAND()),  -- sam: Daft Punk - Get Lucky (Radio Edit - feat. Pharrell Williams and Nile Rodgers)
(3, 4, 3, UTC_TIMESTAMP() - INTERVAL 8 HOUR, UTC_TIMESTAMP() + INTERVAL 16 HOUR, RAND()),  -- nina: Stromae - Alors on danse (Radio Edit)
(4, 5, 4, UTC_TIMESTAMP() - INTERVAL 2 HOUR, UTC_TIMESTAMP() + INTERVAL 22 HOUR, RAND()),  -- jules: Kendrick Lamar - HUMBLE.
(5, 6, 5, UTC_TIMESTAMP() - INTERVAL 12 HOUR, UTC_TIMESTAMP() + INTERVAL 12 HOUR, RAND()),  -- ines: Amy Winehouse - Rehab
(6, 7, 6, UTC_TIMESTAMP() - INTERVAL 15 HOUR, UTC_TIMESTAMP() + INTERVAL 9 HOUR, RAND()),  -- tom: Bob Marley & The Wailers - Is This Love
(7, 8, 7, UTC_TIMESTAMP() - INTERVAL 1 HOUR, UTC_TIMESTAMP() + INTERVAL 23 HOUR, RAND()),  -- lea: Miles Davis - So What (feat. John Coltrane, Cannonball Adderley & Bill Evans)
(8, 9, 8, UTC_TIMESTAMP() - INTERVAL 6 HOUR, UTC_TIMESTAMP() + INTERVAL 18 HOUR, RAND()),  -- hugo: Nirvana - Smells Like Teen Spirit
(9, 10, 9, UTC_TIMESTAMP() - INTERVAL 9 HOUR, UTC_TIMESTAMP() + INTERVAL 15 HOUR, RAND()),  -- chloe: Queen - Bohemian Rhapsody
(10, 11, 10, UTC_TIMESTAMP() - INTERVAL 4 HOUR, UTC_TIMESTAMP() + INTERVAL 20 HOUR, RAND()),  -- lucas: Michael Jackson - Billie Jean
(11, 12, 11, UTC_TIMESTAMP() - INTERVAL 18 HOUR, UTC_TIMESTAMP() + INTERVAL 6 HOUR, RAND()),  -- emma: Adele - Rolling in the Deep
(12, 13, 12, UTC_TIMESTAMP() - INTERVAL 7 HOUR, UTC_TIMESTAMP() + INTERVAL 17 HOUR, RAND()),  -- noah: Metallica - Enter Sandman (Remastered 2021)
(13, 14, 13, UTC_TIMESTAMP() - INTERVAL 10 HOUR, UTC_TIMESTAMP() + INTERVAL 14 HOUR, RAND()),  -- jade: Billie Eilish - bad guy
(14, 15, 14, UTC_TIMESTAMP() - INTERVAL 11 HOUR, UTC_TIMESTAMP() + INTERVAL 13 HOUR, RAND()),  -- louis: The Weeknd - Blinding Lights
(15, 16, 15, UTC_TIMESTAMP() - INTERVAL 2 HOUR, UTC_TIMESTAMP() + INTERVAL 22 HOUR, RAND()),  -- manon: Angèle - Balance ton quoi
(16, 17, 16, UTC_TIMESTAMP() - INTERVAL 14 HOUR, UTC_TIMESTAMP() + INTERVAL 10 HOUR, RAND()),  -- adam: PNL - Au DD
(17, 18, 17, UTC_TIMESTAMP() - INTERVAL 20 HOUR, UTC_TIMESTAMP() + INTERVAL 4 HOUR, RAND()),  -- camille: Fleetwood Mac - Dreams (2004 Remaster)
(18, 19, 18, UTC_TIMESTAMP() - INTERVAL 3 HOUR, UTC_TIMESTAMP() + INTERVAL 21 HOUR, RAND()),  -- theo: Eminem - Lose Yourself
(19, 20, 19, UTC_TIMESTAMP() - INTERVAL 6 HOUR, UTC_TIMESTAMP() + INTERVAL 18 HOUR, RAND()),  -- sarah: Dua Lipa - Levitating
(20, 21, 20, UTC_TIMESTAMP() - INTERVAL 22 HOUR, UTC_TIMESTAMP() + INTERVAL 2 HOUR, RAND());  -- paul: Edith Piaf - La Vie en Rose


-- ======================================================================
-- Past drops, already expired: they fill the "derniers drops" of the profiles
-- (lito, elliot, sam, ines and jade).
-- ======================================================================

INSERT INTO drops (id, user_id, song_id, created_at, expires_at, rand_key) VALUES
(21, 1, 8, UTC_TIMESTAMP() - INTERVAL 2 DAY, UTC_TIMESTAMP() - INTERVAL 1 DAY, RAND()),  -- lito: Nirvana - Smells Like Teen Spirit
(22, 1, 18, UTC_TIMESTAMP() - INTERVAL 4 DAY, UTC_TIMESTAMP() - INTERVAL 3 DAY, RAND()),  -- lito: Eminem - Lose Yourself
(23, 1, 11, UTC_TIMESTAMP() - INTERVAL 6 DAY, UTC_TIMESTAMP() - INTERVAL 5 DAY, RAND()),  -- lito: Adele - Rolling in the Deep
(24, 1, 6, UTC_TIMESTAMP() - INTERVAL 9 DAY, UTC_TIMESTAMP() - INTERVAL 8 DAY, RAND()),  -- lito: Bob Marley & The Wailers - Is This Love
(25, 2, 12, UTC_TIMESTAMP() - INTERVAL 2 DAY, UTC_TIMESTAMP() - INTERVAL 1 DAY, RAND()),  -- elliot: Metallica - Enter Sandman (Remastered 2021)
(26, 2, 9, UTC_TIMESTAMP() - INTERVAL 3 DAY, UTC_TIMESTAMP() - INTERVAL 2 DAY, RAND()),  -- elliot: Queen - Bohemian Rhapsody
(27, 2, 17, UTC_TIMESTAMP() - INTERVAL 5 DAY, UTC_TIMESTAMP() - INTERVAL 4 DAY, RAND()),  -- elliot: Fleetwood Mac - Dreams (2004 Remaster)
(28, 2, 8, UTC_TIMESTAMP() - INTERVAL 7 DAY, UTC_TIMESTAMP() - INTERVAL 6 DAY, RAND()),  -- elliot: Nirvana - Smells Like Teen Spirit
(29, 2, 4, UTC_TIMESTAMP() - INTERVAL 10 DAY, UTC_TIMESTAMP() - INTERVAL 9 DAY, RAND()),  -- elliot: Kendrick Lamar - HUMBLE.
(30, 3, 14, UTC_TIMESTAMP() - INTERVAL 2 DAY, UTC_TIMESTAMP() - INTERVAL 1 DAY, RAND()),  -- sam: The Weeknd - Blinding Lights
(31, 3, 19, UTC_TIMESTAMP() - INTERVAL 4 DAY, UTC_TIMESTAMP() - INTERVAL 3 DAY, RAND()),  -- sam: Dua Lipa - Levitating
(32, 3, 3, UTC_TIMESTAMP() - INTERVAL 6 DAY, UTC_TIMESTAMP() - INTERVAL 5 DAY, RAND()),  -- sam: Stromae - Alors on danse (Radio Edit)
(33, 3, 10, UTC_TIMESTAMP() - INTERVAL 8 DAY, UTC_TIMESTAMP() - INTERVAL 7 DAY, RAND()),  -- sam: Michael Jackson - Billie Jean
(34, 6, 7, UTC_TIMESTAMP() - INTERVAL 3 DAY, UTC_TIMESTAMP() - INTERVAL 2 DAY, RAND()),  -- ines: Miles Davis - So What (feat. John Coltrane, Cannonball Adderley & Bill Evans)
(35, 6, 20, UTC_TIMESTAMP() - INTERVAL 5 DAY, UTC_TIMESTAMP() - INTERVAL 4 DAY, RAND()),  -- ines: Edith Piaf - La Vie en Rose
(36, 6, 14, UTC_TIMESTAMP() - INTERVAL 9 DAY, UTC_TIMESTAMP() - INTERVAL 8 DAY, RAND()),  -- ines: The Weeknd - Blinding Lights
(37, 14, 15, UTC_TIMESTAMP() - INTERVAL 2 DAY, UTC_TIMESTAMP() - INTERVAL 1 DAY, RAND()),  -- jade: Angèle - Balance ton quoi
(38, 14, 19, UTC_TIMESTAMP() - INTERVAL 5 DAY, UTC_TIMESTAMP() - INTERVAL 4 DAY, RAND()),  -- jade: Dua Lipa - Levitating
(39, 14, 11, UTC_TIMESTAMP() - INTERVAL 7 DAY, UTC_TIMESTAMP() - INTERVAL 6 DAY, RAND());  -- jade: Adele - Rolling in the Deep


-- ======================================================================
-- Likes. Like in services/swipes.py, each like writes one row in saves
-- (the like received by the author) and one row in liked_songs
-- (the song in the user's "Mes likes" list), with the same date (ADR-0013).
-- lito has no like on the drops of the day, so his feed is full.
-- ======================================================================

INSERT INTO saves (user_id, drop_id, created_at) VALUES
(3, 1, UTC_TIMESTAMP() - INTERVAL 50 MINUTE),  -- sam likes the drop of elliot
(4, 1, UTC_TIMESTAMP() - INTERVAL 40 MINUTE),  -- nina likes the drop of elliot
(9, 1, UTC_TIMESTAMP() - INTERVAL 30 MINUTE),  -- hugo likes the drop of elliot
(10, 1, UTC_TIMESTAMP() - INTERVAL 20 MINUTE),  -- chloe likes the drop of elliot
(5, 2, UTC_TIMESTAMP() - INTERVAL 45 MINUTE),  -- jules likes the drop of sam
(11, 2, UTC_TIMESTAMP() - INTERVAL 25 MINUTE),  -- lucas likes the drop of sam
(2, 5, UTC_TIMESTAMP() - INTERVAL 35 MINUTE),  -- elliot likes the drop of ines
(12, 13, UTC_TIMESTAMP() - INTERVAL 15 MINUTE),  -- emma likes the drop of jade
(13, 13, UTC_TIMESTAMP() - INTERVAL 10 MINUTE),  -- noah likes the drop of jade
(20, 14, UTC_TIMESTAMP() - INTERVAL 5 MINUTE),  -- sarah likes the drop of louis
(2, 21, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 2 HOUR),  -- elliot likes the drop of lito
(3, 21, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 3 HOUR),  -- sam likes the drop of lito
(4, 21, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 5 HOUR),  -- nina likes the drop of lito
(5, 22, UTC_TIMESTAMP() - INTERVAL 4 DAY + INTERVAL 1 HOUR),  -- jules likes the drop of lito
(9, 22, UTC_TIMESTAMP() - INTERVAL 4 DAY + INTERVAL 4 HOUR),  -- hugo likes the drop of lito
(12, 23, UTC_TIMESTAMP() - INTERVAL 6 DAY + INTERVAL 6 HOUR),  -- emma likes the drop of lito
(13, 25, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 2 HOUR),  -- noah likes the drop of elliot
(9, 25, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 3 HOUR),  -- hugo likes the drop of elliot
(1, 25, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 5 HOUR),  -- lito likes the drop of elliot
(10, 26, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 1 HOUR),  -- chloe likes the drop of elliot
(1, 26, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 2 HOUR),  -- lito likes the drop of elliot
(3, 26, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 6 HOUR),  -- sam likes the drop of elliot
(18, 27, UTC_TIMESTAMP() - INTERVAL 5 DAY + INTERVAL 3 HOUR),  -- camille likes the drop of elliot
(8, 28, UTC_TIMESTAMP() - INTERVAL 7 DAY + INTERVAL 2 HOUR),  -- lea likes the drop of elliot
(17, 29, UTC_TIMESTAMP() - INTERVAL 10 DAY + INTERVAL 4 HOUR),  -- adam likes the drop of elliot
(1, 30, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 3 HOUR),  -- lito likes the drop of sam
(16, 30, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 7 HOUR),  -- manon likes the drop of sam
(12, 31, UTC_TIMESTAMP() - INTERVAL 4 DAY + INTERVAL 2 HOUR),  -- emma likes the drop of sam
(7, 32, UTC_TIMESTAMP() - INTERVAL 6 DAY + INTERVAL 5 HOUR),  -- tom likes the drop of sam
(21, 33, UTC_TIMESTAMP() - INTERVAL 8 DAY + INTERVAL 1 HOUR),  -- paul likes the drop of sam
(1, 34, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 4 HOUR),  -- lito likes the drop of ines
(19, 34, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 6 HOUR),  -- theo likes the drop of ines
(18, 35, UTC_TIMESTAMP() - INTERVAL 5 DAY + INTERVAL 2 HOUR),  -- camille likes the drop of ines
(2, 36, UTC_TIMESTAMP() - INTERVAL 9 DAY + INTERVAL 3 HOUR),  -- elliot likes the drop of ines
(13, 37, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 2 HOUR),  -- noah likes the drop of jade
(10, 38, UTC_TIMESTAMP() - INTERVAL 5 DAY + INTERVAL 4 HOUR),  -- chloe likes the drop of jade
(15, 39, UTC_TIMESTAMP() - INTERVAL 7 DAY + INTERVAL 1 HOUR);  -- louis likes the drop of jade

INSERT INTO liked_songs (user_id, song_id, created_at) VALUES
(3, 1, UTC_TIMESTAMP() - INTERVAL 50 MINUTE),  -- sam: Linkin Park - Numb
(4, 1, UTC_TIMESTAMP() - INTERVAL 40 MINUTE),  -- nina: Linkin Park - Numb
(9, 1, UTC_TIMESTAMP() - INTERVAL 30 MINUTE),  -- hugo: Linkin Park - Numb
(10, 1, UTC_TIMESTAMP() - INTERVAL 20 MINUTE),  -- chloe: Linkin Park - Numb
(5, 2, UTC_TIMESTAMP() - INTERVAL 45 MINUTE),  -- jules: Daft Punk - Get Lucky (Radio Edit - feat. Pharrell Williams and Nile Rodgers)
(11, 2, UTC_TIMESTAMP() - INTERVAL 25 MINUTE),  -- lucas: Daft Punk - Get Lucky (Radio Edit - feat. Pharrell Williams and Nile Rodgers)
(2, 5, UTC_TIMESTAMP() - INTERVAL 35 MINUTE),  -- elliot: Amy Winehouse - Rehab
(12, 13, UTC_TIMESTAMP() - INTERVAL 15 MINUTE),  -- emma: Billie Eilish - bad guy
(13, 13, UTC_TIMESTAMP() - INTERVAL 10 MINUTE),  -- noah: Billie Eilish - bad guy
(20, 14, UTC_TIMESTAMP() - INTERVAL 5 MINUTE),  -- sarah: The Weeknd - Blinding Lights
(2, 8, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 2 HOUR),  -- elliot: Nirvana - Smells Like Teen Spirit
(3, 8, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 3 HOUR),  -- sam: Nirvana - Smells Like Teen Spirit
(4, 8, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 5 HOUR),  -- nina: Nirvana - Smells Like Teen Spirit
(5, 18, UTC_TIMESTAMP() - INTERVAL 4 DAY + INTERVAL 1 HOUR),  -- jules: Eminem - Lose Yourself
(9, 18, UTC_TIMESTAMP() - INTERVAL 4 DAY + INTERVAL 4 HOUR),  -- hugo: Eminem - Lose Yourself
(12, 11, UTC_TIMESTAMP() - INTERVAL 6 DAY + INTERVAL 6 HOUR),  -- emma: Adele - Rolling in the Deep
(13, 12, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 2 HOUR),  -- noah: Metallica - Enter Sandman (Remastered 2021)
(9, 12, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 3 HOUR),  -- hugo: Metallica - Enter Sandman (Remastered 2021)
(1, 12, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 5 HOUR),  -- lito: Metallica - Enter Sandman (Remastered 2021)
(10, 9, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 1 HOUR),  -- chloe: Queen - Bohemian Rhapsody
(1, 9, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 2 HOUR),  -- lito: Queen - Bohemian Rhapsody
(3, 9, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 6 HOUR),  -- sam: Queen - Bohemian Rhapsody
(18, 17, UTC_TIMESTAMP() - INTERVAL 5 DAY + INTERVAL 3 HOUR),  -- camille: Fleetwood Mac - Dreams (2004 Remaster)
(8, 8, UTC_TIMESTAMP() - INTERVAL 7 DAY + INTERVAL 2 HOUR),  -- lea: Nirvana - Smells Like Teen Spirit
(17, 4, UTC_TIMESTAMP() - INTERVAL 10 DAY + INTERVAL 4 HOUR),  -- adam: Kendrick Lamar - HUMBLE.
(1, 14, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 3 HOUR),  -- lito: The Weeknd - Blinding Lights
(16, 14, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 7 HOUR),  -- manon: The Weeknd - Blinding Lights
(12, 19, UTC_TIMESTAMP() - INTERVAL 4 DAY + INTERVAL 2 HOUR),  -- emma: Dua Lipa - Levitating
(7, 3, UTC_TIMESTAMP() - INTERVAL 6 DAY + INTERVAL 5 HOUR),  -- tom: Stromae - Alors on danse (Radio Edit)
(21, 10, UTC_TIMESTAMP() - INTERVAL 8 DAY + INTERVAL 1 HOUR),  -- paul: Michael Jackson - Billie Jean
(1, 7, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 4 HOUR),  -- lito: Miles Davis - So What (feat. John Coltrane, Cannonball Adderley & Bill Evans)
(19, 7, UTC_TIMESTAMP() - INTERVAL 3 DAY + INTERVAL 6 HOUR),  -- theo: Miles Davis - So What (feat. John Coltrane, Cannonball Adderley & Bill Evans)
(18, 20, UTC_TIMESTAMP() - INTERVAL 5 DAY + INTERVAL 2 HOUR),  -- camille: Edith Piaf - La Vie en Rose
(2, 14, UTC_TIMESTAMP() - INTERVAL 9 DAY + INTERVAL 3 HOUR),  -- elliot: The Weeknd - Blinding Lights
(13, 15, UTC_TIMESTAMP() - INTERVAL 2 DAY + INTERVAL 2 HOUR),  -- noah: Angèle - Balance ton quoi
(10, 19, UTC_TIMESTAMP() - INTERVAL 5 DAY + INTERVAL 4 HOUR),  -- chloe: Dua Lipa - Levitating
(15, 11, UTC_TIMESTAMP() - INTERVAL 7 DAY + INTERVAL 1 HOUR);  -- louis: Adele - Rolling in the Deep