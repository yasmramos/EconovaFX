# 📊 EconoNova FX - Sistema Contable Profesional

[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=java)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-blue.svg?logo=javafx)](https://openjfx.io/)
[![Ebean ORM](https://img.shields.io/badge/Ebean-17.11.0-green.svg)](https://ebean.io/)
[![H2 Database](https://img.shields.io/badge/H2-2.2.224-red.svg)](https://h2database.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg?logo=apache-maven)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/tests-385%20passing-brightgreen.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/yasmramos/econovafx)

Sistema contable moderno y profesional desarrollado con **JavaFX 17** y **Ebean ORM 17**, diseñado para cumplir con la normativa contable cubana (Resolución 340/2004).

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías-utilizadas)
- [Requisitos](#-requisitos-previos)
- [Instalación](#-instalación-y-ejecución)
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

### Arquitectura Multi-Tenant y Cumplimiento Normativo
- ✅ Arquitectura modular por paquetes (`com.econovafx.modules.*`)
- ✅ Diseño preparado para multi-tenant (empresas múltiples)
- ✅ Cumplimiento de la Resolución 340/2004 (normativa contable cubana)
- ✅ Exportación a formatos oficiales (PDF, Excel)
- ✅ Auditoría completa de todas las operaciones

---

## 🚀 Tecnologías Utilizadas

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **Java** | 17 LTS | Lenguaje de programación |
| **JavaFX** | 17 | Interfaz gráfica de usuario moderna |
| **Ebean ORM** | 17.11.0 | Mapeo objeto-relacional de alto rendimiento |
| **H2 Database** | 2.2.224 | Base de datos embebida para desarrollo/testing |
| **PostgreSQL** | 42.7.3 | Base de datos de producción |
| **Maven** | 3.9+ | Gestión de dependencias y build |
| **Logback** | 1.4.14 | Framework de logging SLF4J |
| **Avaje Inject** | 12.6 | Inyección de dependencias ligera |
| **JUnit 5** | 5.11.0 | Testing framework |
| **AssertJ** | 3.25.x | Assertions fluents para tests |
| **Apache PDFBox** | 2.0.29 | Exportación a PDF |
| **Apache POI** | 5.2.5 | Exportación a Excel |
| **Ikonli** | 12.4.0 | Iconos JavaFX (Material Design) |
| **jBCrypt** | 0.4 | Hashing de contraseñas |

---

## 📋 Requisitos Previos

- **Java JDK 17** o superior ([descargar](https://adoptium.net/))
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

## 📦 Módulos Implementados

| Módulo | Estado | Descripción |
|--------|--------|-------------|
| **Contabilidad (Accounting)** | ✅ Completado | Plan de cuentas, transacciones, períodos contables |
| **Activos (Assets)** | ✅ Completado | Gestión de activos corrientes |
| **Banco (Bank)** | ✅ Completado | Cuentas bancarias y conciliación bancaria |
| **Facturación (Billing)** | ✅ Completado | Emisión y gestión de facturas |
| **Caja (Cash)** | ✅ Completado | Gestión de efectivo y arqueo de caja |
| **Core** | ✅ Completado | Configuración, empresa, utilidades |
| **Activos Fijos (Fixed Assets)** | ✅ Completado | Depreciación y gestión de activos fijos |
| **Inventario (Inventory)** | ✅ Completado | Almacenes, items y control de stock |
| **Cuentas por Pagar (Payables)** | ✅ Completado | Gestión de obligaciones a proveedores |
| **Nómina (Payroll)** | ✅ Completado | Gestión de salarios y empleados |
| **Cuentas por Cobrar (Receivables)** | ✅ Completado | Gestión de créditos a clientes |
| **Reportes (Reporting)** | ✅ Completado | Balances, estados financieros y consolidación |
| **Seguridad (Security)** | ✅ Completado | Usuarios, roles y permisos |
| **Presupuestos** | ⏳ Pendiente | Control presupuestario |

---

## 🧪 Testing

El proyecto cuenta con **385 tests automatizados** que cubren:

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
Tests ejecutados: 385
Pasados: 385 (100%)
Fallos: 0
Errores: 0
```

---

## 📚 Documentación

La documentación detallada se encuentra en el directorio [`docs/`](docs/):

- [Guía de Usuario](docs/USER_GUIDE.md) - Manual para usuarios finales
- [Arquitectura](docs/ARCHITECTURE.md) - Diseño técnico y patrones
- [Especificación Fase 1](docs/PHASE-1-SPECIFICATION.md) - Especificación funcional
- [Análisis GAP Resolución 340/2004](docs/RESOLUTION-340-2004-GAP-ANALYSIS.md) - Análisis de cumplimiento
- [Análisis Detallado Resolución 340/2004](docs/RESOLUTION_340_2004_DETAILED_ANALYSIS.md) - Normativa contable cubana
- [Roadmap Resolución 340/2004](docs/ROADMAP-RESOLUCION-340-2004-CUBA.md) - Hoja de ruta de implementación
- [Docker](docs/DOCKER.md) - Guía de despliegue con Docker
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

1. Crear clase modelo en `src/main/java/com/econovafx/modules/<módulo>/model/` extendiendo `BaseEntity`
2. Anotar con `@Entity`, `@Table(name = "tabla")`
3. Definir campos con anotaciones JPA/Ebean
4. Crear repositorio en `src/main/java/com/econovafx/modules/<módulo>/repository/`
5. Crear servicio en `src/main/java/com/econovafx/modules/<módulo>/service/`
6. Crear validador en `src/main/java/com/econovafx/modules/<módulo>/validation/` (si aplica)
7. Crear controlador UI en `src/main/java/com/econovafx/modules/<módulo>/ui/controller/` (si aplica)
8. Agregar tests en `src/test/java/com/econovafx/modules/<módulo>/`

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
