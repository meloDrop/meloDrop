# meloDrop
<img width="1200" height="630" alt="og-image" src="https://github.com/user-attachments/assets/2d4e7fad-537f-4176-bc3d-d9e5f9c7bb7f" />

> One song a day, picked by someone, not by an algorithm.

meloDrop is an Android app for music discovery. Each user drops one song a day. The other users listen to a 30-second preview, then swipe to keep the song or skip it.

The interface is in French. The code and the docs are in English.

This is my end-of-first-year project at Holberton School. The app is in early access on Google Play. Website: [melodrop-app.com](https://melodrop-app.com)

## What you can do in the app

- Drop one song a day. It stays online for 24 hours.
- Swipe the drops of the other users: right to like, left to skip.
- Find your liked songs in "Mes likes".
- Get back a song skipped by mistake in "Mes skips".
- Visit profiles and search users by @username.

<table>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/de34bafb-f7f0-4e55-bcc3-f96795f77a5b" width="230" alt="Discover screen"><br>Discover</td>
    <td align="center"><img src="https://github.com/user-attachments/assets/e06861ba-5dac-4694-889e-61d6f9f19290" width="230" alt="Drop screen"><br>Drop</td>
    <td align="center"><img src="https://github.com/user-attachments/assets/94ca96eb-51f9-469d-a2d5-19944963c002" width="230" alt="Profile screen"><br>Profile</td>
  </tr>
</table>


## Built with

- Android app: Kotlin, Jetpack Compose, Retrofit
- Backend: Python, FastAPI, SQLAlchemy, Alembic
- Database: MySQL
- Music data: Deezer public API

## Run the project locally

### What you need

- Git
- Docker Desktop
- Android Studio
- An Android phone with USB debugging turned on, or an Android emulator

### 1. Clone the project

```bash
git clone https://github.com/meloDrop/meloDrop.git
cd meloDrop
```

### 2. Create the `.env` file

The backend reads its settings in `backend/.env`. Start from the example file:

```bash
cd backend
cp .env.example .env
```

Open `.env` and change these three lines:

```env
DB_USER=root
DB_PASSWORD=choose_a_password
JWT_SECRET=paste_a_random_key_here
```

To create a random key for `JWT_SECRET`:

```bash
python3 -c "import secrets; print(secrets.token_hex(32))"
```

Keep the other lines as they are.

The email lines (`SMTP_*` and `MAIL_FROM`) are only needed to send real emails with Brevo. Without them, the app runs, but you cannot confirm a new account. Use the demo accounts instead (step 4).

### 3. Start the backend

Still in the `backend` folder:

```bash
docker compose up -d --build
```

This starts two containers: the database (`db`) and the API (`api`). The first start can take a minute. The API creates the tables by itself.

Check that both containers are ready:

```bash
docker compose ps
```

The `STATUS` column must show `healthy` for both.

Then open http://localhost:8000/health in your browser. You should see `{"status":"ok"}`.

The API docs (Swagger) are at http://localhost:8000/docs.

### 4.  Load the demo data (optional)

Without demo data, the app is empty. The demo file adds users, songs and drops of the day.

Still in the `backend` folder:

```bash
docker compose cp ../database/demo.sql db:/demo.sql
docker compose exec db sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql -uroot $MYSQL_DATABASE < /demo.sql'
```

Load it only once, on an empty database.

All the demo accounts use the password `Password1!`. Log in with `litoxam`: this account has no drop of the day, so all the drops of the feed are left to discover.

The drops of the day last 24 hours from the time you load the file. After that, the feed is empty. To get new drops, reset the database (see [Useful commands](#useful-commands)).

### 5. Run the Android app

1. Open the `meloDrop` folder (the root of the project) in Android Studio.
2. Wait for the Gradle sync to finish.
3. Plug in your phone, or start an emulator.
4. Link port 8000 of your phone to port 8000 of your computer:

   ```bash
   adb reverse tcp:8000 tcp:8000
   ```

5. Click Run in Android Studio.

The debug version of the app calls the API on `http://localhost:8000`. The `adb reverse` command lets the phone reach your computer at this address. Run it again each time you plug in the phone.

`adb` comes with Android Studio. You can find it in the `platform-tools` folder of the Android SDK.

## Useful commands

Run them in the `backend` folder.

```bash
# Show the API logs (Ctrl + C to quit)
docker compose logs -f api

# Stop the backend
docker compose down

# Reset the database: this deletes all the data
docker compose down -v
docker compose up -d --build
```

After a reset, load the demo data again (step 4).

## Common problems

- If the `db` container does not start, port 3306 may already be used by a MySQL installed on your computer. Stop this MySQL, then start the backend again.
- If the app shows a network error, check that the backend is running, then run `adb reverse tcp:8000 tcp:8000` again.
- If you cannot log in with a new account, the confirmation email was not sent (no Brevo keys). You can log in with a demo account, or confirm your account by hand. In the `backend` folder, open MySQL:

  ```bash
  docker compose exec db sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql -uroot $MYSQL_DATABASE'
  ```

  Then run this line, with your username, and type `exit` to quit:

  ```sql
  UPDATE users SET email_verified = TRUE WHERE username = 'your_username';
  ```

## Project structure

```
meloDrop/
├── app/         Android app
├── backend/     API, Docker files and database migrations
├── database/    demo data
├── docs/adr/    architecture decisions (ADR)
└── landing/     website melodrop-app.com
```
The changes of each version are listed in `CHANGELOG.md` (French version: `CHANGELOGFR.md`).

## Author

Maxime Bernier, Holberton School
