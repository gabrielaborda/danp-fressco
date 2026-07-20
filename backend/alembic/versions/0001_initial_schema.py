"""
Migración inicial: crea todas las tablas del esquema Fressco.

Revision ID: 0001
Revises:
Create Date: 2025-01-01 00:00:00.000000
"""
from alembic import op
import sqlalchemy as sa

# revision identifiers
revision = "0001"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    # ── tiendas ───────────────────────────────────────────────────────────────
    op.create_table(
        "tiendas",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("nombre", sa.String(120), nullable=False),
        sa.Column("direccion", sa.String(255), nullable=False),
        sa.Column("telefono", sa.String(30), nullable=True),
        sa.Column("email_contacto", sa.String(120), nullable=True),
        sa.Column("descripcion", sa.Text(), nullable=True),
        sa.Column("logo_url", sa.String(500), nullable=True),
        sa.Column("activa", sa.Boolean(), nullable=False, server_default="1"),
    )

    # ── administradores ───────────────────────────────────────────────────────
    op.create_table(
        "administradores",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("tienda_id", sa.Integer(), sa.ForeignKey("tiendas.id"), nullable=False),
        sa.Column("nombre", sa.String(120), nullable=False),
        sa.Column("email", sa.String(120), nullable=False),
        sa.Column("password_hash", sa.String(255), nullable=False),
        sa.Column("rol", sa.String(20), nullable=False, server_default="admin"),
        sa.Column("activo", sa.Boolean(), nullable=False, server_default="1"),
    )
    op.create_index("ix_administradores_email", "administradores", ["email"], unique=True)

    # ── clientes ──────────────────────────────────────────────────────────────
    op.create_table(
        "clientes",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("nombre", sa.String(120), nullable=False),
        sa.Column("email", sa.String(120), nullable=False),
        sa.Column("password_hash", sa.String(255), nullable=False),
        sa.Column("rol", sa.String(20), nullable=False, server_default="cliente"),
        sa.Column("telefono", sa.String(30), nullable=True),
        sa.Column(
            "fecha_registro",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
        ),
        sa.Column("estado", sa.String(20), nullable=False, server_default="activo"),
    )
    op.create_index("ix_clientes_email", "clientes", ["email"], unique=True)

    # ── productos ─────────────────────────────────────────────────────────────
    op.create_table(
        "productos",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("tienda_id", sa.Integer(), sa.ForeignKey("tiendas.id"), nullable=False),
        sa.Column("nombre", sa.String(200), nullable=False),
        sa.Column("descripcion", sa.Text(), nullable=True),
        sa.Column("categoria", sa.String(80), nullable=True),
        sa.Column("precio_base", sa.Numeric(10, 2), nullable=False),
        sa.Column("imagen_url", sa.String(500), nullable=True),
        sa.Column("activo", sa.Boolean(), nullable=False, server_default="1"),
    )
    op.create_index("ix_productos_categoria", "productos", ["categoria"])

    # ── lotes ─────────────────────────────────────────────────────────────────
    op.create_table(
        "lotes",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("producto_id", sa.Integer(), sa.ForeignKey("productos.id"), nullable=False),
        sa.Column("cantidad", sa.Integer(), nullable=False),
        sa.Column("cantidad_inicial", sa.Integer(), nullable=False),
        sa.Column("fecha_ingreso", sa.Date(), nullable=False),
        sa.Column("fecha_vencimiento", sa.Date(), nullable=False),
        sa.Column("precio_lote", sa.Numeric(10, 2), nullable=False),
        sa.Column("estado", sa.String(20), nullable=False, server_default="disponible"),
        sa.Column(
            "creado_en",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
        ),
    )
    op.create_index("ix_lotes_fecha_vencimiento", "lotes", ["fecha_vencimiento"])
    op.create_index("ix_lotes_estado", "lotes", ["estado"])

    # ── descuentos ────────────────────────────────────────────────────────────
    op.create_table(
        "descuentos",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("lote_id", sa.Integer(), sa.ForeignKey("lotes.id"), nullable=False),
        sa.Column("tipo", sa.String(30), nullable=False),
        sa.Column("porcentaje", sa.Numeric(5, 2), nullable=True),
        sa.Column("monto_fijo", sa.Numeric(10, 2), nullable=True),
        sa.Column("fecha_inicio", sa.Date(), nullable=False),
        sa.Column("fecha_fin", sa.Date(), nullable=False),
        sa.Column("activo", sa.Boolean(), nullable=False, server_default="1"),
        sa.Column("descripcion", sa.String(255), nullable=True),
        sa.Column(
            "creado_en",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
        ),
    )
    op.create_index("ix_descuentos_lote_id", "descuentos", ["lote_id"])

    # ── carrito_items ─────────────────────────────────────────────────────────
    op.create_table(
        "carrito_items",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("cliente_id", sa.Integer(), sa.ForeignKey("clientes.id"), nullable=False),
        sa.Column("lote_id", sa.Integer(), sa.ForeignKey("lotes.id"), nullable=False),
        sa.Column("cantidad", sa.Integer(), nullable=False),
        sa.Column("precio_aplicado", sa.Numeric(10, 2), nullable=False),
        sa.Column(
            "agregado_en",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
        ),
    )
    op.create_index("ix_carrito_items_cliente_id", "carrito_items", ["cliente_id"])

    # ── pedidos ───────────────────────────────────────────────────────────────
    op.create_table(
        "pedidos",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("cliente_id", sa.Integer(), sa.ForeignKey("clientes.id"), nullable=False),
        sa.Column("tienda_id", sa.Integer(), sa.ForeignKey("tiendas.id"), nullable=False),
        sa.Column(
            "fecha_pedido",
            sa.DateTime(timezone=True),
            server_default=sa.func.now(),
        ),
        sa.Column("estado", sa.String(20), nullable=False, server_default="pendiente"),
        sa.Column("total", sa.Numeric(12, 2), nullable=False),
        sa.Column("notas", sa.String(500), nullable=True),
    )
    op.create_index("ix_pedidos_cliente_id", "pedidos", ["cliente_id"])
    op.create_index("ix_pedidos_tienda_id", "pedidos", ["tienda_id"])
    op.create_index("ix_pedidos_estado", "pedidos", ["estado"])

    # ── pedido_items ──────────────────────────────────────────────────────────
    op.create_table(
        "pedido_items",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("pedido_id", sa.Integer(), sa.ForeignKey("pedidos.id"), nullable=False),
        sa.Column("lote_id", sa.Integer(), sa.ForeignKey("lotes.id"), nullable=False),
        sa.Column("cantidad", sa.Integer(), nullable=False),
        sa.Column("precio_unitario_aplicado", sa.Numeric(10, 2), nullable=False),
    )
    op.create_index("ix_pedido_items_pedido_id", "pedido_items", ["pedido_id"])


def downgrade() -> None:
    op.drop_table("pedido_items")
    op.drop_table("pedidos")
    op.drop_table("carrito_items")
    op.drop_table("descuentos")
    op.drop_table("lotes")
    op.drop_table("productos")
    op.drop_table("clientes")
    op.drop_table("administradores")
    op.drop_table("tiendas")
