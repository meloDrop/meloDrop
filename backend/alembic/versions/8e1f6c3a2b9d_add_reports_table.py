"""add reports table

Revision ID: 8e1f6c3a2b9d
Revises: 5d2e8b4f1a3c
Create Date: 2026-11-06 09:10:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '8e1f6c3a2b9d'
down_revision: Union[str, Sequence[str], None] = '5d2e8b4f1a3c'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    op.create_table('reports',
    sa.Column('id', sa.Integer(), autoincrement=True, nullable=False),
    sa.Column('reporter_id', sa.Integer(), nullable=True),
    sa.Column('reported_id', sa.Integer(), nullable=True),
    sa.Column('reported_username', sa.String(length=30), nullable=False),
    sa.Column('reason', sa.String(length=20), nullable=False),
    sa.Column('message', sa.String(length=500), nullable=True),
    sa.Column('status', sa.String(length=20), server_default='open', nullable=False),
    sa.Column('note', sa.String(length=300), nullable=True),
    sa.Column('created_at', sa.DateTime(), server_default=sa.text('now()'), nullable=True),
    sa.Column('closed_at', sa.DateTime(), nullable=True),
    sa.ForeignKeyConstraint(['reported_id'], ['users.id'], name='fk_reports_reported_id', ondelete='SET NULL'),
    sa.ForeignKeyConstraint(['reporter_id'], ['users.id'], name='fk_reports_reporter_id', ondelete='SET NULL'),
    sa.PrimaryKeyConstraint('id')
    )


def downgrade() -> None:
    """Downgrade schema."""
    op.drop_table('reports')
