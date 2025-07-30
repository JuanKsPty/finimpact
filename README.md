# 💼 FinImpact – Sistema de Seguimiento de Iniciativas e Impactos Financieros

## 🧠 Descripción General

**FinImpact** es una aplicación de escritorio estilo SPA (Single Page Application) desarrollada en JavaFX con conexión a SQL Server. Está diseñada para que empresas u organizaciones puedan registrar, monitorear y analizar el impacto financiero de sus iniciativas estratégicas, permitiendo comparar valores planeados, estimados y reales a lo largo del tiempo.

La aplicación se enfoca en consolidar en un solo entorno:
- El control de iniciativas empresariales
- El registro detallado de impactos financieros
- La visualización de indicadores clave y reportes

---

## 🧭 Arquitectura y Navegación

- **Tipo de aplicación:** JavaFX SPA (una sola ventana principal con pestañas)
- **Base de datos:** SQL Server (`finimpact`)
- **Navegación:** `TabPane` con pestañas dinámicas
- **Pestañas principales:**
  - Dashboard
  - Iniciativas
  - Impactos

---

## 🔐 Gestión de Acceso por Rol

| Rol       | Permisos                                                                 |
|------------|--------------------------------------------------------------------------|
| **Admin**  | Acceso total: puede ver, crear, modificar y eliminar usuarios, iniciativas e impactos |
| **Analista** | Puede crear/editar iniciativas e impactos asociadas a su usuario        |
| **Viewer**  | Solo puede visualizar datos (iniciativas, impactos, KPIs, reportes)     |

---

## 📌 Funcionalidades por Sección

### 🏠 **Dashboard**
- KPIs financieros:
  - Total de iniciativas activas
  - Suma de impactos planeados y reales del mes actual
  - Diferencia acumulada
- Gráficos dinámicos:
  - Distribución de impactos por tipo
  - Comparación planeado vs real

---

### 📁 **Iniciativas**
- CRUD de iniciativas
- Filtros por estado, tipo, riesgo y usuario owner
- Campos: nombre, descripción, fechas, tipo, estado, riesgo, owner

---

### 💰 **Impactos**
- Registro detallado de impactos financieros
- Tipos: Maquinaria, Generación, Optimización, Transformación
- Atributos: Planeado, Estimado, Real
- Multiplicador: 1 o -1 según impacto positivo o negativo
- Filtros por tipo, fecha, atributo, iniciativa
- Sección estilo CRUD + tabla dinámica

---

### 🔐 **Login**
- Autenticación por correo electrónico y contraseña
- Control de acceso según rol (`admin`, `analista`, `viewer`)
- Usuarios inactivos no pueden iniciar sesión

---

## 🗃️ Estructura de Base de Datos (SQL Server)

### 📄 Tabla: `usuarios`

```sql
CREATE TABLE usuarios (
    id_usuario INT PRIMARY KEY IDENTITY(1,1),
    nombre_completo NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    rol NVARCHAR(20) CHECK (rol IN ('admin', 'analista', 'viewer')) NOT NULL,
    estado BIT NOT NULL -- 1 = activo, 0 = inactivo
);
```

---

### 📄 Tabla: `iniciativas`

```sql
CREATE TABLE iniciativas (
    id_iniciativa INT PRIMARY KEY IDENTITY(1,1),
    nombre NVARCHAR(100) NOT NULL,
    descripcion NVARCHAR(500),
    fecha_inicio DATE,
    fecha_fin DATE,
    tipo NVARCHAR(30),
    estado NVARCHAR(20) CHECK (estado IN ('planeado', 'en curso', 'finalizado', 'cancelado')) NOT NULL,
    riesgo NVARCHAR(20) CHECK (riesgo IN ('alto', 'medio', 'bajo')) NOT NULL,
    id_owner INT NOT NULL,
    fecha_registro DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (id_owner) REFERENCES usuarios(id_usuario)
);
```

---

### 📄 Tabla: `impactos`

```sql
CREATE TABLE impactos (
    id_impacto INT PRIMARY KEY IDENTITY(1,1),
    id_iniciativa INT NOT NULL,
    fecha_creacion DATE NOT NULL,
    tipo_impacto NVARCHAR(30) CHECK (tipo_impacto IN ('Maquinaria', 'Generación', 'Optimización', 'Transformación')) NOT NULL,
    multiplicador INT CHECK (multiplicador IN (1, -1)) NOT NULL,
    atributo_impacto NVARCHAR(20) CHECK (atributo_impacto IN ('Planeado', 'Estimado', 'Real')) NOT NULL,
    fecha_impacto DATE NOT NULL,
    impacto DECIMAL(18, 2) NOT NULL,

    FOREIGN KEY (id_iniciativa) REFERENCES iniciativas(id_iniciativa)
);
```

---

## 🧱 Tecnologías Utilizadas

- Java 17+
- JavaFX (SPA con TabPane)
- SQL Server
- JDBC
- (Opcional: JFoenix o ControlsFX para mejorar UI)

---

