"""All the database models, imported together so they know each other."""

from app.models.drop import Drop
from app.models.drop_genre import DropGenre
from app.models.genre import Genre
from app.models.genre_report import GenreReport
from app.models.platform_link import PlatformLink
from app.models.save import Save
from app.models.skip import Skip
from app.models.song import Song
from app.models.user import User