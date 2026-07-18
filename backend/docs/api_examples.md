# Ejemplos de uso de la API — Fressco

Base URL: `http://localhost:8000/api/v1`

---

## 1. Autenticación

### Registrar cliente
```bash
curl -X POST http://localhost:8000/api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "María García",
    "email": "maria@example.com",
    "password": "mipassword123",
    "telefono": "987654321"
  }'
```

**Respuesta:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "rol": "cliente",
  "nombre": "María García",
  "id": 3
}
```

### Login (cliente o admin)
```bash
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@fressco.com",
    "password": "admin123"
  }'
```

---

## 2. Catálogo (público)

### Listar lotes disponibles con descuentos
```bash
curl http://localhost:8000/api/v1/lotes-disponibles
```

**Respuesta:**
```json
[
  {
    "id": 4,
    "producto_id": 3,
    "cantidad": 20,
    "fecha_vencimiento": "2025-01-05",
    "precio_lote": "22.00",
    "precio_con_descuento": "13.20",
    "porcentaje_descuento": "40.00",
    "dias_para_vencer": 4,
    "tiene_descuento_manual": false,
    "nombre_producto": "Pechuga de Pollo Fresca",
    "nombre_tienda": "Supermercado Fressco Demo",
    "categoria": "Carnes"
  }
]
```

### Buscar productos por texto
```bash
curl "http://localhost:8000/api/v1/productos?busqueda=yogurt&categoria=Lácteos"
```

### Solo lotes con descuento activo
```bash
curl "http://localhost:8000/api/v1/lotes-disponibles?solo_con_descuento=true"
```

---

## 3. Carrito (cliente autenticado)

> Reemplaza `TOKEN` con el JWT obtenido en el login.

### Ver carrito
```bash
curl http://localhost:8000/api/v1/carrito \
  -H "Authorization: Bearer TOKEN"
```

### Agregar item al carrito
```bash
curl -X POST http://localhost:8000/api/v1/carrito/items \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"lote_id": 4, "cantidad": 2}'
```

**Respuesta:**
```json
{
  "id": 1,
  "lote_id": 4,
  "cantidad": 2,
  "precio_aplicado": "13.20",
  "nombre_producto": "Pechuga de Pollo Fresca",
  "subtotal": "26.40",
  "agregado_en": "2025-01-01T12:00:00Z"
}
```

### Actualizar cantidad
```bash
curl -X PUT http://localhost:8000/api/v1/carrito/items/1 \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cantidad": 5}'
```

### Eliminar item
```bash
curl -X DELETE http://localhost:8000/api/v1/carrito/items/1 \
  -H "Authorization: Bearer TOKEN"
```

---

## 4. Pedidos (cliente)

### Confirmar compra desde el carrito
```bash
curl -X POST http://localhost:8000/api/v1/pedidos \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"notas": "Sin cebolla por favor"}'
```

**Respuesta:**
```json
{
  "id": 1,
  "cliente_id": 1,
  "tienda_id": 1,
  "estado": "confirmado",
  "total": "26.40",
  "fecha_pedido": "2025-01-01T12:05:00Z",
  "items": [
    {
      "id": 1,
      "lote_id": 4,
      "cantidad": 2,
      "precio_unitario_aplicado": "13.20",
      "subtotal": "26.40",
      "nombre_producto": "Pechuga de Pollo Fresca"
    }
  ]
}
```

### Historial de pedidos
```bash
curl http://localhost:8000/api/v1/pedidos \
  -H "Authorization: Bearer TOKEN"
```

### Cancelar pedido
```bash
curl -X PUT http://localhost:8000/api/v1/pedidos/1/cancelar \
  -H "Authorization: Bearer TOKEN"
```

---

## 5. Admin — Gestión de lotes y descuentos

> Usar token de admin (email: admin@fressco.com)

### Crear lote
```bash
curl -X POST http://localhost:8000/api/v1/admin/lotes \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "producto_id": 1,
    "cantidad": 100,
    "fecha_ingreso": "2025-01-01",
    "fecha_vencimiento": "2025-01-15",
    "precio_lote": 8.50
  }'
```

### Sobreescribir descuento manualmente
```bash
curl -X PUT http://localhost:8000/api/v1/admin/lotes/1/descuento \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "porcentaje": 35,
    "fecha_inicio": "2025-01-01",
    "fecha_fin": "2025-01-10",
    "descripcion": "Descuento especial por liquidación"
  }'
```

### Ver historial de descuentos de un lote
```bash
curl http://localhost:8000/api/v1/admin/lotes/1/historial-descuentos \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## 6. Admin — Reportes

### Productos más vendidos
```bash
curl "http://localhost:8000/api/v1/admin/reportes/productos-mas-vendidos?limite=5" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Lotes próximos a vencer (próximos 7 días)
```bash
curl "http://localhost:8000/api/v1/admin/reportes/lotes-proximos-a-vencer?dias=7" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Ventas por período
```bash
curl "http://localhost:8000/api/v1/admin/reportes/ventas-por-periodo?fecha_desde=2025-01-01&fecha_hasta=2025-01-31" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

**Respuesta:**
```json
{
  "periodo": {
    "desde": "2025-01-01",
    "hasta": "2025-01-31"
  },
  "total_pedidos": 42,
  "ingresos_totales": 1850.70,
  "ticket_promedio": 44.06
}
```

---

## 7. Gestión de clientes (admin)

### Suspender un cliente
```bash
curl -X PUT http://localhost:8000/api/v1/admin/clientes/2 \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado": "suspendido"}'
```

### Reactivar un cliente
```bash
curl -X PUT http://localhost:8000/api/v1/admin/clientes/2 \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado": "activo"}'
```

---

## Códigos de error estándar

| HTTP | code | Descripción |
|------|------|-------------|
| 400 | `BAD_REQUEST` | Datos inválidos en la solicitud |
| 400 | `LOTE_VENCIDO` | El lote está vencido o no disponible |
| 400 | `CARRITO_VACIO` | El carrito está vacío al intentar checkout |
| 401 | `UNAUTHORIZED` | Token ausente, expirado o inválido |
| 403 | `FORBIDDEN` | Sin permisos para el recurso |
| 404 | `NOT_FOUND` | Recurso no encontrado |
| 409 | `CONFLICT` | Conflicto (ej: email ya registrado) |
| 409 | `STOCK_INSUFICIENTE` | No hay suficiente stock disponible |
| 422 | `VALIDATION_ERROR` | Error de validación Pydantic |
| 500 | `INTERNAL_ERROR` | Error interno del servidor |
