"""add avatar cosmetics to users

Revision ID: c5a1e7d3f9b2
Revises: 2b7d4e9a1c5f
Create Date: 2026-11-11 12:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'c5a1e7d3f9b2'
down_revision: Union[str, Sequence[str], None] = '2b7d4e9a1c5f'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    # The defaults keep the avatars as they are today
    op.add_column('users', sa.Column('avatar_drop', sa.String(length=20), nullable=False, server_default='night'))
    op.add_column('users', sa.Column('avatar_deco', sa.String(length=20), nullable=False, server_default='plain'))
    op.add_column('users', sa.Column('avatar_face', sa.String(length=20), nullable=False, server_default='none'))
    op.add_column('users', sa.Column('avatar_aura', sa.String(length=20), nullable=False, server_default='none'))
    op.add_column('users', sa.Column('is_subscriber', sa.Boolean(), nullable=False, server_default=sa.text('0')))


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_column('users', 'is_subscriber')
    op.drop_column('users', 'avatar_aura')
    op.drop_column('users', 'avatar_face')
    op.drop_column('users', 'avatar_deco')
    op.drop_column('users', 'avatar_drop')
