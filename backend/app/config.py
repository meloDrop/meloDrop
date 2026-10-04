"""Settings of the API, read from the .env file."""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """All the settings used by the API."""

    DB_USER: str = "root"
    DB_PASSWORD: str
    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_NAME: str

    JWT_SECRET: str
    JWT_ALGORITHM: str = "HS256"
    # user will have to reconnect every month, can be changed later
    JWT_EXPIRE_MINUTES: int = 60 * 24 * 30

    # 15 reports needed to notify the drop author in production
    REPORT_THRESHOLD_PROD: int = 15
    # threshold lowered to 3 during test phase
    REPORT_THRESHOLD_TEST: int = 3
    # expiration of the music preview URL in hours
    PREVIEW_URL_TTL_HOURS: int = 12

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()