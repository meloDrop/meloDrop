"""All the database models, imported together so they know each other."""

from app.models.drop import Drop
from app.models.save import Save
from app.models.skip import Skip
from app.models.song import Song
from app.models.user import User
from app.models.liked_song import LikedSong
from app.models.email_code import EmailCode
from app.models.podium_song import PodiumSong
from app.models.labeled_song import LabeledSong
from app.models.badge import Badge
from app.models.user_badge import UserBadge
from app.models.follow import Follow
