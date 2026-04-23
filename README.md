# EconoNova FX - Sistema Contable

Sistema contable moderno desarrollado con **JavaFX** y **Ebean ORM**.

## 🚀 Tecnologías Utilizadas

- **JavaFX 23.0.2** - Interfaz gráfica de usuario
- **Ebean ORM 17.2.0** - Mapeo objeto-relacional
- **H2 Database 2.3.232** - Base de datos embebida
- **Maven** - Gestión de dependencias
- **Logback** - Logging

## 📋 Requisitos Previos

- Java 21 o superior
- Maven 3.8+

## 🛠️ Instalación y Ejecución

1. Compila el proyecto:
```bash
mvn clean compile
```

2. Ejecuta la aplicación:
```bash
mvn javafx:run
```

## 📁 Estructura del Proyecto

```
econovafx/
├── src/main/java/com/econovafx/
│   ├── config/              # Configuración
│   │   ├── AppContext.java  # Contexto de la aplicación
│   │   └── DatabaseConfig.java
│   ├── domain/              # Entidades
│   │   ├── Account.java
│   │   ├── AccountType.java
│   │   ├── BaseEntity.java
│   │   ├── Transaction.java
│   │   ├── TransactionEntry.java
│   │   └── User.java
│   ├── repository/          # Repositorios
│   │   ├── AccountRepository.java
│   │   ├── TransactionRepository.java
│   │   └── UserRepository.java
│   ├── service/             # Servicios
│   │   ├── AccountService.java
│   │   ├── TransactionService.java
│   │   └── UserService.java
│   ├── ui/
│   │   └── controller/      # Controladores JavaFX
│   └── App.java             # Clase principal
├── src/main/resources/
│   ├── fxml/                # Vistas FXML
│   ├── styles/              # Estilos CSS
│   └── ebean.properties     # Configuración Ebean
└── pom.xml
```

## 🔑 Características Principales

### Gestión de Cuentas Contables
- Plan de cuentas jerárquico
- Clasificación por tipo (Activo, Pasivo, Patrimonio, Ingreso, Gasto)
- Búsqueda y filtrado de cuentas
- Saldo en tiempo real

### Gestión de Transacciones
- Registro de transacciones con partida doble
- Validación de cuadre (Débito = Crédito)
- Publicación de transacciones
- Reversión de transacciones
- Filtrado por fechas

### Dashboard
- Resumen de activos, pasivos y patrimonio
- Transacciones recientes
- Estadísticas generales

## 💾 Base de Datos

La aplicación utiliza **H2 Database** embebida. Los datos se almacenan en:
```
./db/econovadb
```

La base de datos se crea automáticamente al iniciar la aplicación.

## 🎨 Personalización

Los estilos CSS se encuentran en `src/main/resources/styles/main.css`.

## 📦 Generar JAR

```bash
mvn clean package
```

## 🐛 Solución de Problemas

### Error: "Database connection failed"
Verifica que la carpeta `./db` tenga permisos de escritura.

### Error: "FXML load failed"
Revisa que los archivos FXML estén en `src/main/resources/fxml/`

---

**EconoNova FX** - Sistema Contable Moderno ⭐
