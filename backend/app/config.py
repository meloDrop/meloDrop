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

    # Sending emails with the Brevo SMTP relay (development and production)
    SMTP_HOST: str = "smtp-relay.brevo.com"
    SMTP_PORT: int = 587

    # SMTP login and key generated in Brevo, kept in .env only
    SMTP_USER: str
    SMTP_PASSWORD: str

    # Must be a sender address verified in Brevo
    MAIL_FROM: str

    # Codes sent by email (ADR-0015)
    EMAIL_CODE_EXPIRE_MINUTES: int = 15
    EMAIL_CODE_MAX_ATTEMPTS: int = 5


    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()