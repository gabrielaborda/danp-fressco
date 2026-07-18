"""
tools/seed.py — Carga datos de ejemplo para desarrollo y testing.

Crea:
- 1 Tienda
- 1 Administrador (admin@fressco.com / admin123)
- 5 Productos con categorías distintas
- 8 Lotes con fechas de vencimiento variadas (para probar todos los niveles de descuento)
- 2 Clientes (cliente1@fressco.com / cliente123)

Uso:
    python tools/seed.py
"""
import os
import sys
from datetime import date, timedelta

# Agregar raíz del proyecto al path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.core.security import hash_password
from app.db.session import SessionLocal, engine
from app.models.base import Base
from app.models.tienda import Tienda
from app.models.administrador import Administrador
from app.models.cliente import Cliente
from app.models.producto import Producto
from app.models.lote import Lote
from app.models.descuento import Descuento


def crear_tablas():
    """Crea las tablas si no existen (útil para SQLite en desarrollo rápido)."""
    Base.metadata.create_all(bind=engine)
    print("✅ Tablas verificadas/creadas")


def seed():
    db = SessionLocal()
    hoy = date.today()

    try:
        # ── Limpiar datos anteriores (orden inverso por FK) ──────────────────
        for tabla in [Descuento, Lote, Producto, Administrador, Cliente, Tienda]:
            db.query(tabla).delete()
        db.commit()
        print("🗑️  Datos anteriores eliminados")

        # ── Tienda ────────────────────────────────────────────────────────────
        tienda = Tienda(
            nombre="Supermercado Fressco Demo",
            direccion="Av. Los Álamos 456, Miraflores, Lima",
            telefono="01-456-7890",
            email_contacto="contacto@fresscode.pe",
            descripcion="Supermercado líder en Lima con productos frescos y de calidad",
            logo_url="https://via.placeholder.com/200x200?text=Fressco",
            activa=True,
        )
        db.add(tienda)
        db.flush()
        print(f"🏪 Tienda creada: {tienda.nombre} (id={tienda.id})")

        # ── Administrador ─────────────────────────────────────────────────────
        admin = Administrador(
            tienda_id=tienda.id,
            nombre="Carlos Admin",
            email="admin@fressco.com",
            password_hash=hash_password("admin123"),
            rol="admin",
            activo=True,
        )
        db.add(admin)
        print(f"👤 Admin creado: {admin.email} / admin123")

        # ── Clientes ──────────────────────────────────────────────────────────
        cliente1 = Cliente(
            nombre="Ana Cliente",
            email="cliente1@fressco.com",
            password_hash=hash_password("cliente123"),
            telefono="987-654-321",
            estado="activo",
        )
        cliente2 = Cliente(
            nombre="Juan Pérez",
            email="cliente2@fressco.com",
            password_hash=hash_password("cliente123"),
            telefono="912-345-678",
            estado="activo",
        )
        db.add_all([cliente1, cliente2])
        print(f"👥 Clientes creados: cliente1@fressco.com, cliente2@fressco.com / cliente123")

        db.flush()

        # ── Productos ─────────────────────────────────────────────────────────
        productos_data = [
            {
                "nombre": "Yogurt Natural Bio",
                "descripcion": "Yogurt griego sin azúcar añadida, rico en probióticos",
                "categoria": "Lácteos",
                "precio_base": 8.50,
                "imagen_url": "https://via.placeholder.com/300x300?text=Yogurt",
            },
            {
                "nombre": "Pan Integral Artesanal",
                "descripcion": "Pan de masa madre con semillas de chía y linaza",
                "categoria": "Panadería",
                "precio_base": 12.00,
                "imagen_url": "https://via.placeholder.com/300x300?text=Pan",
            },
            {
                "nombre": "Pechuga de Pollo Fresca",
                "descripcion": "Pechuga sin antibióticos, crianza libre de jaula",
                "categoria": "Carnes",
                "precio_base": 22.00,
                "imagen_url": "https://via.placeholder.com/300x300?text=Pollo",
            },
            {
                "nombre": "Mix de Ensalada Primavera",
                "descripcion": "Lechuga, rúcula, espinaca y tomate cherry listos para consumir",
                "categoria": "Verduras",
                "precio_base": 6.90,
                "imagen_url": "https://via.placeholder.com/300x300?text=Ensalada",
            },
            {
                "nombre": "Queso Gouda Importado",
                "descripcion": "Queso holandés madurado 6 meses, cortado al momento",
                "categoria": "Lácteos",
                "precio_base": 35.00,
                "imagen_url": "https://via.placeholder.com/300x300?text=Queso",
            },
        ]

        productos = []
        for data in productos_data:
            p = Producto(tienda_id=tienda.id, **data)
            db.add(p)
            productos.append(p)

        db.flush()
        print(f"📦 {len(productos)} productos creados")

        # ── Lotes (con distintas fechas de vencimiento para probar descuentos) ─
        lotes_data = [
            # Sin descuento (> 10 días)
            {
                "producto_idx": 0,  # Yogurt
                "cantidad": 50,
                "fecha_ingreso": hoy - timedelta(days=5),
                "fecha_vencimiento": hoy + timedelta(days=20),
                "precio_lote": 8.50,
            },
            # Descuento 20% (≤ 10 días)
            {
                "producto_idx": 1,  # Pan
                "cantidad": 30,
                "fecha_ingreso": hoy - timedelta(days=2),
                "fecha_vencimiento": hoy + timedelta(days=8),
                "precio_lote": 12.00,
            },
            # Descuento 20% (≤ 10 días)
            {
                "producto_idx": 4,  # Queso
                "cantidad": 15,
                "fecha_ingreso": hoy - timedelta(days=10),
                "fecha_vencimiento": hoy + timedelta(days=9),
                "precio_lote": 35.00,
            },
            # Descuento 40% (≤ 5 días)
            {
                "producto_idx": 2,  # Pollo
                "cantidad": 20,
                "fecha_ingreso": hoy - timedelta(days=3),
                "fecha_vencimiento": hoy + timedelta(days=4),
                "precio_lote": 22.00,
            },
            # Descuento 40% (≤ 5 días)
            {
                "producto_idx": 3,  # Ensalada
                "cantidad": 40,
                "fecha_ingreso": hoy - timedelta(days=1),
                "fecha_vencimiento": hoy + timedelta(days=3),
                "precio_lote": 6.90,
            },
            # Descuento 60% (≤ 2 días)
            {
                "producto_idx": 0,  # Yogurt — lote urgente
                "cantidad": 10,
                "fecha_ingreso": hoy - timedelta(days=12),
                "fecha_vencimiento": hoy + timedelta(days=1),
                "precio_lote": 8.50,
            },
            # Descuento 60% (≤ 2 días)
            {
                "producto_idx": 1,  # Pan — lote urgente
                "cantidad": 8,
                "fecha_ingreso": hoy - timedelta(days=6),
                "fecha_vencimiento": hoy + timedelta(days=2),
                "precio_lote": 12.00,
            },
            # Lote con descuento MANUAL del admin (sobreescribe el automático)
            {
                "producto_idx": 4,  # Queso — con descuento manual del 50%
                "cantidad": 5,
                "fecha_ingreso": hoy - timedelta(days=20),
                "fecha_vencimiento": hoy + timedelta(days=7),
                "precio_lote": 35.00,
            },
        ]

        lotes = []
        for data in lotes_data:
            idx = data.pop("producto_idx")
            l = Lote(
                producto_id=productos[idx].id,
                cantidad_inicial=data["cantidad"],
                **data,
                estado="disponible",
            )
            db.add(l)
            lotes.append(l)

        db.flush()
        print(f"📦 {len(lotes)} lotes creados")

        # ── Descuento manual en el último lote ────────────────────────────────
        descuento_manual = Descuento(
            lote_id=lotes[-1].id,
            tipo="manual_admin",
            porcentaje=50,
            fecha_inicio=hoy,
            fecha_fin=hoy + timedelta(days=7),
            activo=True,
            descripcion="Oferta especial de temporada — 50% OFF",
        )
        db.add(descuento_manual)
        print("🏷️  Descuento manual 50% creado en lote de Queso Gouda")

        db.commit()
        print("\n✅ Seed completado exitosamente!")
        print("\n📋 Credenciales de acceso:")
        print("   Admin:    admin@fressco.com    / admin123")
        print("   Cliente:  cliente1@fressco.com  / cliente123")
        print("   Cliente:  cliente2@fressco.com  / cliente123")
        print("\n🌐 Swagger UI: http://localhost:8000/docs")

    except Exception as e:
        db.rollback()
        print(f"\n❌ Error durante el seed: {e}")
        raise
    finally:
        db.close()


if __name__ == "__main__":
    crear_tablas()
    seed()
