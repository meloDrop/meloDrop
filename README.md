# meloDrop

One song a day, picked by someone, not by an algorithm.

meloDrop is an Android app for music discovery. Every user can drop one song a day on their profile. The other users discover these drops one by one, listen to a 30-second preview, and swipe right to keep the song or left to pass.

The app is built for a French-speaking audience: the interface is in French, the code and the documentation are in English.

This is my end-of-first-year project at Holberton School.

Version 0.5.0 is available in early access (closed testing) on Google Play. More about the app on [melodrop-app.com](https://melodrop-app.com).

## Features

- Drop one song a day. A drop stays online for 24 hours from the time it was posted. Songs come from the Deezer catalog, and the genre is taken from the Deezer album.
- Discover the drops of the other users in a random order. Swipe right to like a song, left to skip it.
- Find your liked songs in "Mes likes", listen to them again or remove them.
- Rescue a song skipped by mistake from "Mes skips" (skips of the last 7 days).
- Visit a profile: drop of the day, latest drops and counters. "Mes drops" lists all your drops, and every song on a profile can be played.
- Search other users by @username.
- Customize your profile with a display name and an avatar.
- Manage your account: email check with a 6-digit code, forgotten password, email and password change, account deletion.
- Get a notification when your drop expires.

In this first version, the app does not connect to a streaming account: liked songs are kept in meloDrop.

An admin dashboard is served at `/admin`, for accounts with the admin role.

## Stack

- Android app: Kotlin, Jetpack Compose, Retrofit
- Backend: Python, FastAPI, SQLAlchemy, Alembic
- Database: MySQL 9.7
- Music data: Deezer public API (search, track details, previews)
- Emails: Brevo SMTP relay, Jinja2 templates
- Hosting: Scaleway server, Docker Compose, Nginx, Let's Encrypt

## Project structure

```
meloDrop/
├── app/            Android app
├── backend/        FastAPI API and Docker files
│   ├── app/        routers, services, schemas, models, providers
│   ├── alembic/    database migrations
│   └── scripts/    server scripts (daily database backup)
├── database/       demo data for development
└── landing/        landing page of melodrop-app.com
```

In the backend, every request follows the same path: the router receives the HTTP request, the service applies the business rules, the models read and write the database, and the providers call Deezer.

## Run the backend locally

You need Docker with Docker Compose.

```bash
cd backend
cp .env.example .env
```

Fill in `.env` before the first start:

- `DB_USER` must be `root` with this Docker Compose setup, and `DB_PASSWORD` is the MySQL root password.
- `JWT_SECRET` can be generated with `python3 -c "import secrets; print(secrets.token_hex(32))"`.
- The `SMTP_*` variables are your Brevo SMTP keys. They are needed to send the emails (account check, forgotten password).

Then start the database and the API:

```bash
docker compose up -d --build
```

At startup, the API container applies the Alembic migrations, then starts the API with auto-reload.

- API: http://localhost:8000
- Interactive docs (Swagger): http://localhost:8000/docs

To check that everything works:

```bash
curl -s http://localhost:8000/health | jq
```

The answer should be `{"status": "ok"}`.

### Demo data

`database/demo.sql` creates demo accounts and drops. All the accounts use the password `Password1!`. Log in with `lito`: this account has no drop of the day, so the whole feed is left to discover.

Load it once, on an empty database, after the first start:

```bash
docker compose exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' < ../database/demo.sql
```

Never load it in production.

## Run the Android app

1. Start the backend (see above).
2. Open the root folder of the project in Android Studio.
3. The debug build calls `http://localhost:8000/`. Forward this port to the phone or the emulator:

   ```bash
   adb reverse tcp:8000 tcp:8000
   ```

4. Run in Android Studio.

The release build calls the production API: `https://api.melodrop-app.com`.

## Production

The production server follows the `main` branch. `backend/docker-compose.prod.yml` runs three containers:

- MySQL, which is not reachable from the internet;
- the API;
- Nginx, which handles HTTPS with Let's Encrypt certificates and serves the landing page.

To deploy a new version, on the server, in the project folder:

```bash
git pull origin main
cd backend
docker compose -f docker-compose.prod.yml up -d --build
```

The Alembic migrations are applied automatically when the API starts. A cron job runs `backend/scripts/backup_db.sh` every night to back up the database.

## Git workflow

- `main`: released versions only. This is the branch deployed in production.
- `dev`: work branch. All the other branches start from it.
- `feat/`, `fix/`, `doc/`, `build/`: one branch per task, merged into `dev` through a pull request.

Commit messages start with `feat:`, `fix:` or `doc:`. Each release is tagged on `main` (for example `v0.5.0`) and listed in the GitHub Releases.

## What's next

- Link a streaming account (Spotify, Deezer) to export the liked songs to a playlist.
- Follow other users.

## Author

Maxime Bernier, Holberton School