# Fressco

Fressco es una solución tecnológica que conecta clientes y administradores
para reducir el desperdicio de alimentos, permitiendo la publicación y
adquisición de productos próximos a vencer o excedentes de comida a precios
accesibles.

Proyecto final — Desarrollo Avanzado de Nuevas Plataformas (DANP).

## Estructura del repositorio
danp-fressco/
├── backend/       → API REST y lógica de negocio
├── app-android/   → App cliente (Android Studio)
├── web-admin/     → Panel web para el administrador
├── bd/            → Scripts SQL y diagrama del modelo relacional
└── docs/          → Documentación general del proyecto

Cada carpeta principal tiene su propio README con instrucciones específicas.

## Arquitectura general

- **App Android (clientes)** y **Web (administrador)** consumen un único
  **backend** vía API REST.
- El backend es el único componente con acceso directo a la **base de datos
  relacional**.
- Despliegue en la nube (backend + base de datos en el mismo proveedor).

## Módulos funcionales

**App Clientes**
Iniciar sesión · Registrar cuenta · Pantalla principal · Productos ·
Carrito de compra · Formulario de pedido · Mis pedidos · Ofertas especiales

**Web Administrador**
Productos · Usuarios · Ofertas · Pedidos

## Flujo de trabajo (ramas)

- `main` → versión estable / entregable
- `develop` → integración de avances
- `feature/<modulo>-<funcionalidad>` → ramas de trabajo individuales
  (ej. `feature/app-login`, `feature/web-productos`, `feature/backend-pedidos`)

## Equipo

- (Agregar integrantes y roles)

## Estado del proyecto

🚧 En desarrollo.
EOF
