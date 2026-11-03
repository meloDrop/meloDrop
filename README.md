# meloDrop

> One song a day, picked by someone, not by an algorithm.

Android music discovery app. Each user drops one song a day; others discover it by swiping and can add it to their Deezer library.

The app is currently built for a French-speaking audience (French interface only).

End-of-first-year project — Holberton School.

⚠️ **Work in progress.**

## Stack

- **Mobile**: Kotlin, Jetpack Compose
- **Backend**: Python, FastAPI, SQLAlchemy
- **Database**: MySQL
- **Environment**: Docker Compose

## Run the backend

```bash
cd backend
cp .env.example .env   # then fill in the variables
docker compose up --build
```

- API: http://localhost:8000
- Docs: http://localhost:8000/docs

## Author

Maxime Bernier