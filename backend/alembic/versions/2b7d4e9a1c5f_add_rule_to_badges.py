"""add rule to badges

Revision ID: 2b7d4e9a1c5f
Revises: 8e1f6c3a2b9d
Create Date: 2026-11-05 12:40:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '2b7d4e9a1c5f'
down_revision: Union[str, Sequence[str], None] = '8e1f6c3a2b9d'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    op.add_column('badges', sa.Column('rule', sa.String(length=30), nullable=True))
    op.add_column('badges', sa.Column('threshold', sa.Integer(), nullable=True))


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_column('badges', 'threshold')
    op.drop_column('badges', 'rule')
