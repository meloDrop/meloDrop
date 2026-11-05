"""add follows table

Revision ID: 9b4e7d2a6c1f
Revises: 3f8a5c2e9b7d
Create Date: 2026-11-05 05:30:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '9b4e7d2a6c1f'
down_revision: Union[str, Sequence[str], None] = '3f8a5c2e9b7d'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    op.create_table('follows',
    sa.Column('id', sa.Integer(), autoincrement=True, nullable=False),
    sa.Column('follower_id', sa.Integer(), nullable=False),
    sa.Column('followed_id', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(), server_default=sa.text('now()'), nullable=True),
    sa.ForeignKeyConstraint(['followed_id'], ['users.id'], name='fk_follows_followed_id', ondelete='CASCADE'),
    sa.ForeignKeyConstraint(['follower_id'], ['users.id'], name='fk_follows_follower_id', ondelete='CASCADE'),
    sa.PrimaryKeyConstraint('id'),
    sa.UniqueConstraint('follower_id', 'followed_id', name='uq_follows_follower_followed')
    )


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_table('follows')
