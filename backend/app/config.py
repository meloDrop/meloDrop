from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    DB_USER: str = Field(default="root", alias="MYSQL_USER")
    DB_PASSWORD: str = Field(alias="MYSQL_ROOT_PASSWORD")
    DB_HOST: str = Field(default="localhost", alias="MYSQL_HOST")
    DB_PORT: int = Field(default=3306, alias="MYSQL_PORT")
    DB_NAME: str = Field(alias="MYSQL_DATABASE")

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
        # Force reading the .env file in UTF-8 to prevent encoding errors across different OS
        env_file_encoding="utf-8",
        # Ignore unused .env variables not defined in this class
        extra="ignore"
    )


settings = Settings()