# Diagrama Entidad-Relación — Fressco

```mermaid
erDiagram
    TIENDA {
        int id PK
        string nombre
        string direccion
        string telefono
        string email_contacto
        text descripcion
        string logo_url
        bool activa
    }

    ADMINISTRADOR {
        int id PK
        int tienda_id FK
        string nombre
        string email
        string password_hash
        string rol
        bool activo
    }

    CLIENTE {
        int id PK
        string nombre
        string email
        string password_hash
        string rol
        string telefono
        datetime fecha_registro
        string estado
    }

    PRODUCTO {
        int id PK
        int tienda_id FK
        string nombre
        text descripcion
        string categoria
        decimal precio_base
        string imagen_url
        bool activo
    }

    LOTE {
        int id PK
        int producto_id FK
        int cantidad
        int cantidad_inicial
        date fecha_ingreso
        date fecha_vencimiento
        decimal precio_lote
        string estado
        datetime creado_en
    }

    DESCUENTO {
        int id PK
        int lote_id FK
        string tipo
        decimal porcentaje
        decimal monto_fijo
        date fecha_inicio
        date fecha_fin
        bool activo
        string descripcion
        datetime creado_en
    }

    CARRITO_ITEM {
        int id PK
        int cliente_id FK
        int lote_id FK
        int cantidad
        decimal precio_aplicado
        datetime agregado_en
    }

    PEDIDO {
        int id PK
        int cliente_id FK
        int tienda_id FK
        datetime fecha_pedido
        string estado
        decimal total
        string notas
    }

    PEDIDO_ITEM {
        int id PK
        int pedido_id FK
        int lote_id FK
        int cantidad
        decimal precio_unitario_aplicado
    }

    TIENDA ||--o{ ADMINISTRADOR : "administra"
    TIENDA ||--o{ PRODUCTO : "vende"
    TIENDA ||--o{ PEDIDO : "recibe"
    PRODUCTO ||--o{ LOTE : "tiene"
    LOTE ||--o{ DESCUENTO : "tiene"
    LOTE ||--o{ CARRITO_ITEM : "en carrito"
    LOTE ||--o{ PEDIDO_ITEM : "en pedido"
    CLIENTE ||--o{ CARRITO_ITEM : "tiene"
    CLIENTE ||--o{ PEDIDO : "realiza"
    PEDIDO ||--o{ PEDIDO_ITEM : "contiene"
```

## Notas de diseño

### Tabla `descuentos`
- `tipo = "automatico_por_vencimiento"`: generado por la lógica escalonada del sistema
- `tipo = "manual_admin"`: creado explícitamente por el administrador
- Un descuento manual activo **siempre tiene precedencia** sobre el automático
- Solo puede existir un descuento manual activo por lote en un período dado

### Snapshot de precios
- `carrito_items.precio_aplicado`: precio con descuento al momento de agregar al carrito
- `pedido_items.precio_unitario_aplicado`: precio con descuento al momento de confirmar el pedido
- Ambos son **inmutables**: no se recalculan aunque cambie el descuento del lote

### Control de stock
- `lotes.cantidad`: stock actual disponible
- `lotes.cantidad_inicial`: referencia histórica (nunca cambia)
- La reserva de stock se hace con `SELECT FOR UPDATE` (atomicidad bajo concurrencia)
- Estado `agotado` se activa automáticamente cuando `cantidad = 0`
