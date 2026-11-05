"""add is_pick to saves

Revision ID: 5d2e8b4f1a3c
Revises: 9b4e7d2a6c1f
Create Date: 2026-11-06 09:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '5d2e8b4f1a3c'
down_revision: Union[str, Sequence[str], None] = '9b4e7d2a6c1f'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    op.add_column('saves', sa.Column('is_pick', sa.Boolean(), server_default=sa.text('0'), nullable=False))
    # Old saves: a save is a pick when the song is a pick in the user's "Mes likes".
    # A like turned into a pick later in "Mes likes" also counts: the base can't tell them apart
    op.execute("""
        UPDATE saves AS sv
        JOIN drops AS d ON d.id = sv.drop_id
        JOIN liked_songs AS l ON l.user_id = sv.user_id AND l.song_id = d.song_id
        SET sv.is_pick = 1
        WHERE l.is_pick = 1
    """)


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_column('saves', 'is_pick')
