# Fressco API — Backend

API REST del sistema **Fressco**, plataforma que conecta clientes (app Android) con tiendas que ofrecen lotes de productos próximos a vencer con descuentos especiales.

## Stack Técnico

| Componente | Tecnología |
|---|---|
| Framework | FastAPI 0.115 |
| ORM | SQLAlchemy 2 (Mapped/mapped_column) |
| Migraciones | Alembic |
| BD principal | PostgreSQL 16 |
| BD desarrollo | SQLite (opcional) |
| Validación | Pydantic v2 |
| Auth | JWT (PyJWT) + bcrypt (Passlib) |
| Configuración | pydantic-settings |

---

## Estructura del proyecto

```
backend/
├── app/
│   ├── main.py                    # FastAPI app principal
│   ├── api/v1/                    # Routers por dominio
│   │   ├── auth.py
│   │   ├── admin_tiendas.py
│   │   ├── admin_productos.py
│   │   ├── admin_lotes.py
│   │   ├── admin_clientes.py
│   │   ├── admin_pedidos.py
│   │   ├── admin_reportes.py
│   │   ├── catalogo.py
│   │   ├── carrito.py
│   │   ├── pedidos.py
│   │   └── perfil.py
│   ├── models/                    # Modelos SQLAlchemy
│   ├── schemas/                   # Schemas Pydantic v2
│   ├── services/                  # Lógica de negocio
│   └── core/                      # Config, security, deps, exceptions
├── alembic/                       # Migraciones
├── docs/                          # DER + ejemplos de API
├── tools/
│   └── seed.py                    # Datos de ejemplo
├── data/                          # BD SQLite local (ignorado en git)
├── docker-compose.yml
├── .env.example
├── alembic.ini
└── requirements.txt
```

---

## Instalación y configuración

### 1. Clonar y crear entorno virtual

```bash
# Desde la raíz del repositorio
cd backend

# Crear entorno virtual
python -m venv .venv

# Activar (Windows)
.venv\Scripts\activate

# Activar (Linux/macOS)
source .venv/bin/activate
```

### 2. Instalar dependencias

```bash
pip install -r requirements.txt
```

### 3. Configurar variables de entorno

```bash
# Copiar el ejemplo
cp .env.example .env

# Editar .env con tu editor preferido
# Mínimo necesario: SECRET_KEY y DATABASE_URL
```

---

## Levantar la base de datos con Docker

> Requiere Docker Desktop instalado y corriendo.

```bash
# Levanta PostgreSQL + pgAdmin en segundo plano
docker-compose up -d

# Verificar que el contenedor esté sano
docker-compose ps
```

| Servicio | URL | Credenciales |
|---|---|---|
| PostgreSQL | `localhost:5432` | fressco / fressco123 |
| pgAdmin | `http://localhost:5050` | admin@fressco.com / admin123 |

---

## Migraciones con Alembic

```bash
# Aplicar todas las migraciones (crea las tablas)
alembic upgrade head

# Ver estado actual de las migraciones
alembic current

# Ver historial de migraciones
alembic history

# Generar nueva migración automáticamente (tras modificar modelos)
alembic revision --autogenerate -m "descripcion_del_cambio"

# Revertir la última migración
alembic downgrade -1
```

---

## Cargar datos de ejemplo (seed)

```bash
python tools/seed.py
```

Esto crea:
- **1 tienda** demo
- **1 administrador**: `admin@fressco.com` / `admin123`
- **2 clientes**: `cliente1@fressco.com` / `cliente123`
- **5 productos** (Lácteos, Panadería, Carnes, Verduras)
- **8 lotes** con fechas variadas (para probar todos los niveles de descuento)
- **1 descuento manual** del 50% en un lote de Queso Gouda

---

## Levantar el servidor

```bash
# Modo desarrollo con recarga automática
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Producción
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

### Documentación interactiva
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc
- **OpenAPI JSON**: http://localhost:8000/openapi.json
- **Health check**: http://localhost:8000/health

---

## Modo desarrollo con SQLite (sin Docker)

Para desarrollo rápido sin necesidad de Docker:

```bash
# En .env, cambiar DATABASE_URL a:
DATABASE_URL=sqlite:///./data/fressco.db

# Crear tablas y correr seed (seed llama a create_all automáticamente)
python tools/seed.py

# Levantar servidor
uvicorn app.main:app --reload
```

---

## Endpoints principales

### Autenticación
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/registro` | Pública | Registro de cliente |
| POST | `/api/v1/auth/login` | Pública | Login (devuelve JWT) |

### Catálogo
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/v1/productos` | Pública | Listar productos con filtros |
| GET | `/api/v1/productos/{id}` | Pública | Detalle de producto |
| GET | `/api/v1/lotes-disponibles` | Pública | Lotes con descuento calculado |

### Carrito (cliente)
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/v1/carrito` | Cliente | Ver carrito |
| POST | `/api/v1/carrito/items` | Cliente | Agregar item |
| PUT | `/api/v1/carrito/items/{id}` | Cliente | Actualizar cantidad |
| DELETE | `/api/v1/carrito/items/{id}` | Cliente | Eliminar item |
| DELETE | `/api/v1/carrito` | Cliente | Vaciar carrito |

### Pedidos (cliente)
| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/v1/pedidos` | Cliente | Confirmar compra |
| GET | `/api/v1/pedidos` | Cliente | Historial |
| GET | `/api/v1/pedidos/{id}` | Cliente | Detalle |
| PUT | `/api/v1/pedidos/{id}/cancelar` | Cliente | Cancelar |

### Admin — Gestión
| Método | Endpoint | Descripción |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/v1/admin/tiendas` | CRUD tiendas |
| GET/POST/PUT/DELETE | `/api/v1/admin/productos` | CRUD productos |
| GET/POST/PUT/DELETE | `/api/v1/admin/lotes` | CRUD lotes |
| PUT | `/api/v1/admin/lotes/{id}/descuento` | Descuento manual |
| GET | `/api/v1/admin/lotes/{id}/historial-descuentos` | Historial |
| GET/PUT/DELETE | `/api/v1/admin/clientes` | Gestión clientes |
| GET | `/api/v1/admin/pedidos` | Ver todos los pedidos |
| PUT | `/api/v1/admin/pedidos/{id}/estado` | Cambiar estado |

### Admin — Reportes
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/admin/reportes/productos-mas-vendidos` | Top productos |
| GET | `/api/v1/admin/reportes/lotes-proximos-a-vencer` | Alertas de vencimiento |
| GET | `/api/v1/admin/reportes/ventas-por-periodo` | Resumen financiero |

---

## Lógica de descuentos automáticos

Los descuentos se calculan según los días hasta la fecha de vencimiento del lote.
La tabla de escalones es configurable desde `.env` con `DISCOUNT_TIERS`:

```
# Formato: "dias_limite:porcentaje" separados por coma
DISCOUNT_TIERS=2:60,5:40,10:20
```

| Días para vencer | Descuento |
|---|---|
| > 10 días | 0% |
| ≤ 10 días | 20% |
| ≤ 5 días | 40% |
| ≤ 2 días | 60% |

> **Prioridad**: Un descuento manual activo del admin **siempre prevalece** sobre el automático.

---

## Control de concurrencia en stock

Al confirmar un pedido (`POST /api/v1/pedidos`), el sistema:
1. Obtiene cada lote con `SELECT FOR UPDATE` (bloqueo a nivel de fila)
2. Valida stock disponible dentro de la transacción bloqueada
3. Descuenta stock atómicamente
4. Hace commit de toda la transacción (pedido + items + actualización de stock)

Esto garantiza que dos clientes comprando el mismo lote simultáneamente **nunca** puedan sobrepasar el stock disponible.

---

## Seguridad

- **JWT con expiración**: configurable con `ACCESS_TOKEN_EXPIRE_MINUTES`
- **Roles en el payload**: `rol: "admin" | "cliente"` — validado en cada endpoint
- **Contraseñas**: hash bcrypt con factor de costo por defecto de Passlib
- **Clientes suspendidos**: no pueden hacer login ni operar aunque tengan token válido

---

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `DATABASE_URL` | URL de conexión SQLAlchemy | `sqlite:///./data/fressco.db` |
| `SECRET_KEY` | Clave para firmar JWT | ⚠️ Cambiar en producción |
| `ALGORITHM` | Algoritmo JWT | `HS256` |
| `ACCESS_TOKEN_EXPIRE_MINUTES` | Expiración del token | `60` |
| `DISCOUNT_TIERS` | Escalones de descuento | `2:60,5:40,10:20` |
| `DEBUG` | Loguear SQL en consola | `false` |

---

## Ver más ejemplos

📖 [docs/api_examples.md](docs/api_examples.md) — Ejemplos completos con curl  
📊 [docs/DER.md](docs/DER.md) — Diagrama Entidad-Relación
