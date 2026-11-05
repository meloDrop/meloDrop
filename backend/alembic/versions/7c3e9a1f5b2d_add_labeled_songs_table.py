"""add labeled_songs table

Revision ID: 7c3e9a1f5b2d
Revises: d6b0c1a96c23
Create Date: 2026-11-05 04:15:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '7c3e9a1f5b2d'
down_revision: Union[str, Sequence[str], None] = 'd6b0c1a96c23'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    op.create_table('labeled_songs',
    sa.Column('id', sa.Integer(), autoincrement=True, nullable=False),
    sa.Column('user_id', sa.Integer(), nullable=False),
    sa.Column('song_id', sa.Integer(), nullable=False),
    sa.Column('label', sa.String(length=30), nullable=False),
    sa.Column('created_at', sa.DateTime(), server_default=sa.text('now()'), nullable=True),
    sa.ForeignKeyConstraint(['song_id'], ['songs.id'], name='fk_labeled_songs_song_id'),
    sa.ForeignKeyConstraint(['user_id'], ['users.id'], name='fk_labeled_songs_user_id', ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id'),
    sa.UniqueConstraint('user_id', 'label', name='uq_labeled_songs_user_label')
    )


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_table('labeled_songs')
