# 📊 EconoNova FX - Sistema Contable Profesional

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=java)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg?logo=javafx)](https://openjfx.io/)
[![Ebean ORM](https://img.shields.io/badge/Ebean-17.11.0-green.svg)](https://ebean.io/)
[![H2 Database](https://img.shields.io/badge/H2-2.3.232-red.svg)](https://h2database.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg?logo=apache-maven)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/tests-268%20passing-brightgreen.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Sistema contable moderno y profesional desarrollado con **JavaFX 21** y **Ebean ORM 17**, diseñado para cumplir con la normativa contable cubana (Resolución 340/2004).

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías-utilizadas)
- [Requisitos](#-requisitos-previos)
- [Instalación](#-instalación-y-ejecución)
- [Estructura](#-estructura-del-proyecto)
- [Módulos](#-módulos-implementados)
- [Testing](#-testing)
- [Documentación](#-documentación)
- [Desarrollo](#-guía-de-desarrollo)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## ✨ Características

### Contabilidad General
- ✅ Plan de cuentas configurable (activo, pasivo, patrimonio, ingresos, gastos)
- ✅ Gestión de tipos de cuenta (detalle, titular, consolidación)
- ✅ Validación de partida doble automática
- ✅ Períodos contables con control de apertura/cierre
- ✅ Transacciones balanceadas con validación en tiempo real

### Gestión de Transacciones
- ✅ Registro de comprobantes contables
- ✅ Asientos con múltiples entradas (debe/haber)
- ✅ Numeración automática de transacciones
- ✅ Estados: Borrador, Validado, Contabilizado, Anulado
- ✅ Auditoría completa (quién, cuándo, qué)

### Control de Períodos
- ✅ Apertura y cierre de períodos mensuales/anuales
- ✅ Bloqueo de períodos cerrados
- ✅ Validación de fechas en transacciones
- ✅ Período actual activo por defecto

### Usuarios y Seguridad
- ✅ Roles: Administrador, Contador, Auditor, Visualizador
- ✅ Permisos granulares por módulo
- ✅ Autenticación local (preparado para LDAP/AD)
- ✅ Bitácora de actividades

### Terceros y Contactos
- ✅ Clientes, proveedores, empleados
- ✅ Clasificación por tipo de tercero
- ✅ Datos fiscales completos
- ✅ Historial de transacciones por tercero

### Dashboard e Informes
- ✅ Panel principal con KPIs contables
- ✅ Balances de comprobación
- ✅ Estados financieros básicos
- ✅ Reportes exportables (PDF, Excel, CSV)

### Tipos de Cambio
- ✅ Gestión de tasas de cambio activas
- ✅ Histórico de tipos de cambio
- ✅ Conversión automática en transacciones multicurrency

---

## 🚀 Tecnologías Utilizadas

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **Java** | 21 LTS | Lenguaje de programación |
| **JavaFX** | 21 | Interfaz gráfica de usuario moderna |
| **Ebean ORM** | 17.11.0 | Mapeo objeto-relacional de alto rendimiento |
| **H2 Database** | 2.3.232 | Base de datos embebida para desarrollo/testing |
| **Maven** | 3.9+ | Gestión de dependencias y build |
| **Logback** | 1.5.x | Framework de logging SLF4J |
| **Avaje Inject** | 9.0 | Inyección de dependencias ligera |
| **JUnit 5** | 5.10.x | Testing framework |
| **AssertJ** | 3.25.x | Assertions fluents para tests |

---

## 📋 Requisitos Previos

- **Java JDK 21** o superior ([descargar](https://adoptium.net/))
- **Maven 3.9+** ([instalar](https://maven.apache.org/download.cgi))
- **Git** para clonar el repositorio

Verifica tu instalación:
```bash
java --version
mvn --version
git --version
```

---

## 🛠️ Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone https://github.com/yasmramos/EconovaFX.git
cd EconovaFX
```

### 2. Compilar el Proyecto

```bash
mvn clean compile
```

### 3. Ejecutar Tests (Opcional pero recomendado)

```bash
mvn test
```

### 4. Ejecutar la Aplicación

```bash
mvn javafx:run
```

### 5. Primer Inicio

Al iniciar por primera vez:
- Se crea automáticamente la base de datos H2 en `target/econovafx.db`
- Se genera un período contable para el año actual
- Usuario por defecto: `admin` (sin contraseña en modo desarrollo)

---

## 📁 Estructura del Proyecto

```
econovafx/
├── src/
│   ├── main/
│   │   ├── java/com/econovafx/
│   │   │   ├── config/              # Configuración de la aplicación
│   │   │   │   ├── AppContext.java  # Contexto y dependencias
│   │   │   │   └── DatabaseConfig.java  # Configuración Ebean + H2
│   │   │   ├── domain/              # Entidades del dominio
│   │   │   │   ├── BaseEntity.java  # Clase base con auditoría
│   │   │   │   ├── Account.java     # Cuenta contable
│   │   │   │   ├── AccountType.java # Tipo de cuenta
│   │   │   │   ├── AccountingPeriod.java  # Período contable
│   │   │   │   ├── Transaction.java       # Transacción/comprobante
│   │   │   │   ├── TransactionEntry.java  # Entrada de transacción
│   │   │   │   ├── User.java        # Usuario del sistema
│   │   │   │   ├── ThirdParty.java  # Terceros (clientes/proveedores)
│   │   │   │   ├── Warehouse.java   # Almacenes
│   │   │   │   └── InventoryItem.java   # Items de inventario
│   │   │   ├── repository/          # Repositorios (patrón Repository)
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── ...
│   │   │   ├── service/             # Servicios de negocio
│   │   │   │   ├── AccountingService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ValidationService.java
│   │   │   │   └── ...
│   │   │   ├── controller/          # Controladores JavaFX
│   │   │   │   ├── MainController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── ...
│   │   │   ├── view/                # Vistas FXML
│   │   │   │   ├── MainView.fxml
│   │   │   │   ├── DashboardView.fxml
│   │   │   │   └── ...
│   │   │   └── EconoNovaApp.java    # Punto de entrada
│   │   └── resources/
│   │       ├── application.yml      # Configuración de la app
│   │       ├── ebean.properties     # Configuración de Ebean
│   │       ├── logback.xml          # Configuración de logs
│   │       └── views/               # Archivos FXML
│   └── test/java/com/econovafx/     # Tests unitarios y de integración
├── docs/                            # Documentación adicional
├── pom.xml                          # Configuración Maven
└── README.md                        # Este archivo
```

---

## 📦 Módulos Implementados

| Módulo | Estado | Descripción |
|--------|--------|-------------|
| **Contabilidad** | ✅ Completado | Plan de cuentas, transacciones, períodos |
| **Usuarios** | ✅ Completado | Gestión de usuarios y roles |
| **Terceros** | ✅ Completado | Clientes, proveedores, empleados |
| **Inventario** | ✅ Completado | Almacenes e items |
| **Dashboard** | ✅ Completado | Panel principal con KPIs |
| **Tipos de Cambio** | ✅ Completado | Gestión de tasas de cambio |
| **Reportes** | 🔄 En desarrollo | Balances, estados financieros |
| **Presupuestos** | ⏳ Pendiente | Control presupuestario |
| **Activos Fijos** | ⏳ Pendiente | Depreciación y gestión |
| **Nómina** | ⏳ Pendiente | Gestión de salarios |

---

## 🧪 Testing

El proyecto cuenta con **268 tests automatizados** que cubren:

- ✅ Tests unitarios de servicios y validadores
- ✅ Tests de integración con base de datos H2
- ✅ Tests de repositorios
- ✅ Validaciones de negocio (partida doble, períodos, etc.)

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=AccountingValidatorTest
mvn test -Dtest=TransactionServiceTest

# Con reporte de cobertura (requiere plugin jacoco)
mvn test jacoco:report
```

### Estado Actual
```
Tests ejecutados: 268
Pasados: 268 (100%)
Fallos: 0
Errores: 0
```

---

## 📚 Documentación

La documentación detallada se encuentra en el directorio [`docs/`](docs/):

- [Guía de Usuario](docs/USER_GUIDE.md) - Manual para usuarios finales
- [Arquitectura](docs/ARCHITECTURE.md) - Diseño técnico y patrones
- [API Reference](docs/API.md) - Documentación de servicios
- [Normativa 340/2004](docs/NORMATIVA_340_2004.md) - Cumplimiento legal
- [Changelog](CHANGELOG.md) - Historial de cambios

---

## 👩‍💻 Guía de Desarrollo

### Configurar IDE

#### IntelliJ IDEA
1. File → Open → Seleccionar `pom.xml`
2. Esperar a que Maven importe dependencias
3. Ejecutar: `mvn javafx:run` desde Maven panel

#### Eclipse
1. File → Import → Existing Maven Projects
2. Seleccionar directorio raíz
3. Ejecutar: Run As → Maven Build... → `javafx:run`

### Comandos Maven Útiles

```bash
# Limpieza y compilación
mvn clean compile

# Ejecutar tests
mvn test

# Empaquetar JAR
mvn package

# Instalar en repositorio local
mvn install

# Ejecutar aplicación
mvn javafx:run

# Generar sitio de documentación
mvn site

# Ver árbol de dependencias
mvn dependency:tree

# Actualizar dependencias
mvn versions:display-dependency-updates
```

### Convenciones de Código

- **Naming**: CamelCase para clases, snake_case para BD
- **Entidades**: Heredan de `BaseEntity` (id, createdAt, updatedAt)
- **Repositorios**: Interfaz + implementación opcional
- **Servicios**: Lógica de negocio, transaccionalidad
- **Controladores**: Solo UI, delegan a servicios
- **Tests**: Nombre descriptivo, Given-When-Then

### Agregar Nueva Entidad

1. Crear clase en `domain/` extendiendo `BaseEntity`
2. Anotar con `@Entity`, `@Table(name = "tabla")`
3. Definir campos con anotaciones JPA/Ebean
4. Crear repositorio en `repository/`
5. Crear servicio en `service/`
6. Agregar tests en `test/`

---

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Sigue estos pasos:

1. **Fork** el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'feat: agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un **Pull Request**

### Convenciones de Commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` Nueva funcionalidad
- `fix:` Corrección de bug
- `docs:` Cambios en documentación
- `style:` Formato, faltantes, etc.
- `refactor:` Refactorización
- `test:` Agregar/modificar tests
- `chore:` Mantenimiento, dependencias

### Código de Conducta

- Sé respetuoso y constructivo
- Documenta tus cambios
- Escribe tests para nuevas funcionalidades
- Sigue las convenciones del proyecto

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Ver [LICENSE](LICENSE) para más detalles.

```
Copyright (c) 2024 Yasmín Ramos

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📞 Contacto

- **Autor**: Yasmany Ramos García
- **Email**: yasmramos95@gmail.com
- **GitHub**: [@yasmramos](https://github.com/yasmramos)
- **Proyecto**: [EconoNova FX](https://github.com/yasmramos/EconovaFX)

---

## 🙏 Agradecimientos

- [Ebean ORM](https://ebean.io/) - Por su excelente framework ORM
- [OpenJFX](https://openjfx.io/) - Por JavaFX moderno y potente
- [Comunidad Java Cuba](https://twitter.com/search?q=java%20cuba) - Por el apoyo continuo
- [Resolución 340/2004](https://www.gacetaoficial.cu/) - Normativa contable cubana

---

<div align="center">

**¿Te gusta este proyecto?** ¡Dale una ⭐️ en GitHub!

Hecho con ❤️ para la comunidad contable cubana

</div>
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
