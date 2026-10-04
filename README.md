# 📊 EconovaFX - Sistema Contable Profesional

[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-blue.svg?logo=javafx)](https://openjfx.io/)
[![Ebean ORM](https://img.shields.io/badge/Ebean-17.11.0-green.svg)](https://ebean.io/)
[![H2 Database](https://img.shields.io/badge/H2-2.2.224-red.svg)](https://h2database.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![GraalVM Native](https://img.shields.io/badge/GraalVM_Native-Image%20(CI)-purple.svg)](../../actions/workflows/native-image.yml)
[![Tests](https://img.shields.io/badge/tests-385%20passing-brightgreen.svg)]()
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg?logo=apache-maven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/yasmramos/econovafx)

Sistema contable moderno y profesional desarrollado con **JavaFX 17** y **Ebean ORM 17**, diseñado para cumplir con la normativa contable cubana (Resolución 340/2004). Incluye una interfaz de escritorio clásica (FXML) y una **interfaz web embebida** construida con SvelteKit servida en un `WebView` de JavaFX, además de compilación nativa experimental con GraalVM (GluonFX).

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías-utilizadas)
- [Requisitos](#-requisitos-previos)
- [Instalación](#-instalación-y-ejecución)
- [Módulos](#-módulos-implementados)
- [Testing](#-testing)
- [CI/CD](#-cicd)
- [Compilación Nativa](#-compilación-nativa-graalvm)
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
- ✅ Numeración automática de transacciones (secuencias por tipo de comprobante)
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
- ✅ Autenticación local con hashing bcrypt (preparado para LDAP/AD)
- ✅ Bitácora de actividades (auditoría)
- ✅ Aislamiento multi-tenant probado (por empresa/tenant)

### Terceros y Contactos
- ✅ Clientes, proveedores, empleados
- ✅ Clasificación por tipo de tercero
- ✅ Datos fiscales completos
- ✅ Historial de transacciones por tercero

### Dashboard e Informes
- ✅ Panel principal con KPIs contables
- ✅ Balances de comprobación
- ✅ Estados financieros básicos (Balance General, Estado de Resultados)
- ✅ Consolidación financiera multi-empresa (Res. 340/2004, norma II.18)
- ✅ Filtrado por rango de fechas y transacciones contabilizadas
- ✅ Reportes exportables (PDF con OpenHTMLtoPDF/PDFBox, Excel con Apache POI)

### Tipos de Cambio
- ✅ Gestión de tasas de cambio activas
- ✅ Histórico de tipos de cambio
- ✅ Conversión automática en transacciones multicurrency

### Arquitectura Multi-Tenant y Cumplimiento Normativo
- ✅ Arquitectura modular por paquetes (`com.econovafx.modules.*`)
- ✅ Diseño multi-tenant (empresas múltiples) con aislamiento verificado por tests
- ✅ Cumplimiento de la Resolución 340/2004 (normativa contable cubana)
- ✅ Exportación a formatos oficiales (PDF, Excel)
- ✅ Auditoría completa de todas las operaciones
- ✅ Migraciones de esquema gestionadas con Ebean Migration

### Interfaz Web Embebida
- ✅ UI moderna construida con **SvelteKit** + **Tailwind CSS**
- ✅ Renderizada dentro de la app mediante **JavaFX WebView**
- ✅ Servida por un servidor HTTP local en `http://127.0.0.1:<port>/`
- ✅ Generación de sitio estático (SSG) para carga rápida
- ✅ Navegación SPA (Single Page Application) con routing del lado del cliente
- ✅ Compatible con ES2017 para JavaFX 17 WebView
- ✅ Ver [web-ui/README.md](web-ui/README.md) para detalles de desarrollo

---

## 🚀 Tecnologías Utilizadas

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **Java** | 17 LTS (`maven.compiler.release=17`) | Lenguaje de programación; CI valida también JDK 21 y 25 |
| **JavaFX** | 17.0.2 | Interfaz gráfica de usuario (controles, FXML y WebView) |
| **SvelteKit** | ^2.5 (Vite ^5) | Framework web reactivo de la UI embebida |
| **Tailwind CSS** | ^3.4 | Framework CSS utility-first |
| **Ebean ORM** | 17.11.0 | Mapeo objeto-relacional con agente de enhancement en tiempo de ejecución |
| **Ebean Migration** | 14.2.0 | Migraciones de base de datos |
| **HikariCP** | 7.1.0 | Pool de conexiones |
| **H2 Database** | 2.2.224 | Base de datos embebida para desarrollo/testing |
| **PostgreSQL** | driver 42.7.3 | Base de datos de producción |
| **Maven** | 3.9+ | Gestión de dependencias y build |
| **Avaje Inject** | 12.6 | Inyección de dependencias ligera (con plugin AOP) |
| **Avaje Config** | 5.2 | Configuración externalizada |
| **Logback / SLF4J** | 1.4.14 / 2.0.9 | Framework de logging |
| **JUnit 5** | 5.11.0 | Testing framework |
| **AssertJ** | 3.25.x | Assertions fluidas para tests |
| **TestFX + Monocle** | 4.0.17 / 17.0.10 | Tests de UI headless |
| **JaCoCo** | 0.8.15 | Cobertura de código (integrada con CI) |
| **Apache PDFBox** | 2.0.29 | Exportación a PDF |
| **OpenHTMLtoPDF** | 1.0.10 | Plantillas HTML → PDF para reportes |
| **Apache POI** | 5.2.5 | Exportación a Excel |
| **Jsoup** | 1.17.2 | Parsing/saneado de HTML |
| **Ikonli** | 12.4.0 | Iconos JavaFX (Material Design 2) |
| **jBCrypt** | 0.4 | Hashing de contraseñas |
| **GraalVM + GluonFX Plugin** | native profile `-Pnative` | Compilación a imagen nativa (CI en Windows) |

---

## 📋 Requisitos Previos

- **Java JDK 17** o superior ([descargar](https://adoptium.net/)) — la app compila con `release=17`; se soporta ejecutarla sobre JDK 17, 21 o 25
- **Maven 3.9+** ([instalar](https://maven.apache.org/download.cgi))
- **Git** para clonar el repositorio
- **Node.js 18+** (solo si vas a compilar la interfaz web embebida)

Verifica tu instalación:
```bash
java --version
mvn --version
git --version
node --version   # opcional, para web-ui
```

---

## 🛠️ Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone https://github.com/yasmramos/EconovaFX.git
cd EconovaFX
```

### 2. Configurar Hooks de Commit (recomendado)

El repo incluye validación de Conventional Commits en `.githooks/pre-commit`:

```bash
git config core.hooksPath .githooks
```

### 3. Compilar el Proyecto

```bash
mvn clean compile
```

> **Requisito:** es necesario usar un JDK 21+ para compilar (recomendado JDK 25), ya que el compilador de Ebean requiere APIs disponibles desde Java 21. La aplicación resultante se ejecuta sobre JavaFX 17 / release 17.
>
> El agente de Ebean (`lib/ebean-agent-17.11.0.jar`) se descarga/copiará automáticamente a `lib/` durante el build vía `maven-dependency-plugin`.

### 4. Ejecutar Tests (Opcional pero recomendado)

```bash
mvn test
```

### 5. Compilar la Interfaz Web (Opcional pero recomendado)

```bash
cd web-ui
npm install
npm run build
cd ..
```

Esto compila la UI web con SvelteKit y copia el sitio estático a `src/main/resources/web/` para que JavaFX lo sirva en el `WebView`.

### 6. Ejecutar la Aplicación

```bash
mvn javafx:run
```

El plugin ya añade `-javaagent:lib/ebean-agent-17.11.0.jar` y los `--add-opens` necesarios. Alternativa equivalente: `mvn exec:java`.

Para generar un JAR autocontenido (fat jar):

```bash
mvn package -Pshade
java -jar target/econovafx-1.0.0.jar
```

### 7. Primer Inicio

Al iniciar por primera vez:
- Se crea automáticamente la base de datos H2 en `target/econovafx.db`
- Se generan las tablas vía DDL/migraciones de Ebean
- Se genera un período contable para el año actual
- Usuario por defecto: `admin` (sin contraseña en modo desarrollo)

---

## 📦 Módulos Implementados

Código actual: **15 módulos** con **239 clases Java** en `src/main/java/com/econovafx/modules/`.

| Módulo (paquete) | Estado | Descripción |
|--------|--------|-------------|
| **Contabilidad (`accounting`)** | ✅ Completado | Plan de cuentas, transacciones/partidas, períodos, validadores |
| **Core (`core`)** | ✅ Completado | Empresa, configuración, seguridad, auditoría, utilidades, UI shell |
| **Banco (`bank`)** | ✅ Completado | Cuentas bancarias y conciliación bancaria |
| **Facturación (`billing`)** | ✅ Completado | Emisión y gestión de facturas, numeración secuencial |
| **Caja (`cash`)** | ✅ Completado | Gestión de efectivo y arqueo de caja |
| **Activos Fijos (`fixedassets`)** | ✅ Completado | Depreciación y gestión de activos fijos |
| **Inventario (`inventory`)** | ✅ Completado | Almacenes, items y control de stock |
| **Cuentas por Pagar (`payables`)** | ✅ Completado | Gestión de obligaciones a proveedores |
| **Nómina (`payroll`)** | ✅ Completado | Gestión de salarios y empleados |
| **Cuentas por Cobrar (`receivables`)** | ✅ Completado | Gestión de créditos a clientes |
| **Reportes (`reporting`)** | ✅ Completado | Balances, estados financieros y consolidación multi-empresa |
| **Seguridad (`security`)** | 🔶 Parcial | Complemento de usuarios/roles (la base está en `core`) |
| **AFT (`aft`)** | 🔶 En desarrollo | Controlador UI de Activos Fijos Tangibles (integración con `fixedassets`) |
| **Costos (`costing`)** | 🔶 En desarrollo | Centros de costos y procesos (controlador UI inicial) |
| **Finanzas (`finance`)** | 🔶 En desarrollo | Operaciones financieras y flujo de caja (controlador UI inicial) |
| **Presupuestos** | ⏳ Pendiente | Control presupuestario |

---

## 🧪 Testing

El proyecto cuenta con **385 tests automatizados** (✅ 100 % de passing en CI) repartidos en 34 clases de test:

- ✅ Tests unitarios de servicios y validadores (partida doble, períodos, etc.)
- ✅ Tests de integración con base de datos H2
- ✅ Tests de repositorios
- ✅ Tests de aislamiento multi-tenant y seguridad (auth, bcrypt)
- ✅ Tests de arranque de la aplicación e inicialización de Ebean
- ✅ Tests de UI headless con TestFX + Monocle

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Suite completa con cobertura (igual que CI)
mvn clean verify

# Tests específicos
mvn test -Dtest=AccountingValidatorTest
mvn test -Dtest=TransactionServiceTest

# Reporte de cobertura JaCoCo (se genera con `verify`)
open target/site/jacoco/index.html
```

### Estado Actual
```
Tests ejecutados: 385
Pasados: 385 (100%)
Fallos: 0
Errores: 0
```

Umbrales de cobertura en CI (jacoco-report): 60 % global / 70 % en archivos modificados.

---

## 🔄 CI/CD

GitHub Actions valida el proyecto en dos pipelines:

| Workflow | Runner | Qué hace |
|----------|--------|----------|
| [`maven.yml`](.github/workflows/maven.yml) — *Java CI with Maven* | `ubuntu-26.04`, matriz JDK **17 / 21 / 25** (Temurin) | `mvn clean verify` + tests + cobertura JaCoCo; sube artefactos y comenta el coverage en PRs |
| [`native-image.yml`](.github/workflows/native-image.yml) — *Native Image Build (Windows)* | `windows-2025` con **GraalVM JDK 25** (`distribution: 'graalvm'`, componente `native-image`) | `mvn clean gluonfx:build -Pnative` — compila la imagen nativa de la app JavaFX |

---

## 🚀 Compilación Nativa (GraalVM)

El perfil `-Pnative` configura el **GluonFX Maven Plugin** para construir un ejecutable nativo de la aplicación:

```bash
# Requiere GraalVM (JDK 21+ recomendado, CI usa JDK 25) con Visual Studio Build Tools en Windows
export GRAALVM_HOME=/ruta/a/graalvm   # o dejarla detectar por JAVA_HOME
mvn clean gluonfx:build -Pnative
```

El ejecutable resultante se genera en `target/gluonfx/<target>/econovafx(.exe)`. Su nombre se controla con el parámetro `<name>` del plugin, parametrizado mediante la propiedad Maven `app.executable.name` (por defecto `econovafx`), en lugar de derivarse del `<name>` del proyecto Maven (`EconovaFX - Accounting System`), lo que evita rutas con espacios. Puedes cambiarlo puntualmente con `-Dapp.executable.name=otro-nombre`.

En CI esto ocurre automáticamente en el workflow *Native Image Build (Windows)* usando `graalvm/setup-graalvm@v1` con el esquema nuevo (`distribution: 'graalvm'` + `java-version: '25'`). Los reflect/config resources nativos viven en `src/main/resources/META-INF/native-image/`.

---

## 📚 Documentación

La documentación completa está indexada en [`docs/README.md`](docs/README.md):

### Documentación Principal
- [Índice de Documentación](docs/README.md) - Vista general de toda la documentación
- [Guía de Usuario](docs/USER_GUIDE.md) - Manual para usuarios finales
- [Arquitectura](docs/ARCHITECTURE.md) - Diseño técnico y estructura modular
- [Reportes Financieros](docs/FINANCIAL_REPORTING.md) - Consolidación multi-empresa y filtrado por fechas
- [Especificación Fase 1](docs/PHASE-1-SPECIFICATION.md) - Especificación funcional
- [Análisis GAP Resolución 340/2004](docs/RESOLUTION-340-2004-GAP-ANALYSIS.md) - Análisis de cumplimiento
- [Análisis Detallado Resolución 340/2004](docs/RESOLUTION_340_2004_DETAILED_ANALYSIS.md) - Evidencia de implementación
- [Roadmap Resolución 340/2004](docs/ROADMAP-RESOLUCION-340-2004-CUBA.md) - Hoja de ruta de implementación
- [Docker](docs/DOCKER.md) - Guía de despliegue con Docker

### Documentación del Proyecto
- [Changelog](CHANGELOG.md) - Historial de cambios por versión
- [Commit Guidelines](COMMIT_GUIDELINES.md) - Convenciones para mensajes de commit (validadas por pre-commit hook)

### Interfaz Web
- [Web UI README](web-ui/README.md) - Guía de desarrollo de la interfaz web embebida (SvelteKit + Tailwind CSS)

---

## 👩‍💻 Guía de Desarrollo

### Configurar IDE

#### IntelliJ IDEA
1. File → Open → Seleccionar `pom.xml`
2. Esperar a que Maven importe dependencias
3. Ejecutar: `mvn javafx:run` desde Maven panel

#### Eclipse / VS Code
1. Importar como proyecto Maven
2. Ejecutar: `mvn javafx:run` (el agente de Ebean y los `--add-opens` ya están configurados en el POM)

### Comandos Maven Útiles

```bash
# Limpieza y compilación (requiere JDK 21+ para el compilador de Ebean)
mvn clean compile

# Ejecutar tests
mvn test

# Build completo + cobertura (como CI)
mvn clean verify

# Empaquetar JAR estándar
mvn package

# Empaquetar fat JAR ejecutable
mvn package -Pshade

# Instalar en repositorio local
mvn install

# Ejecutar aplicación
mvn javafx:run

# Compilar imagen nativa (requiere GraalVM)
mvn clean gluonfx:build -Pnative

# Ver árbol de dependencias
mvn dependency:tree

# Actualizar dependencias
mvn versions:display-dependency-updates
```

### Convenciones de Código

- **Naming**: CamelCase para clases, snake_case para BD
- **Entidades**: Heredan de `BaseEntity` (id, createdAt, updatedAt); descubiertas por Ebean vía `ebean.packages` en `application.properties` (sin registro manual)
- **Repositorios**: Interfaz + implementación opcional
- **Servicios**: Lógica de negocio, transaccionalidad
- **Controladores**: Solo UI, delegan a servicios
- **DI**: Avaje Inject (`@Singleton`, generador de wiring en `process-sources`)
- **Tests**: Nombre descriptivo, Given-When-Then

### Agregar Nueva Entidad

1. Crear clase modelo en `src/main/java/com/econovafx/modules/<módulo>/model/` extendiendo `BaseEntity`
2. Anotar con `@Entity`, `@Table(name = "tabla")`
3. Definir campos con anotaciones JPA/Ebean (el paquete debe estar listado en `ebean.packages`)
4. Crear repositorio en `src/main/java/com/econovafx/modules/<módulo>/repository/`
5. Crear servicio en `src/main/java/com/econovafx/modules/<módulo>/service/`
6. Crear validador en `src/main/java/com/econovafx/modules/<módulo>/validation/` (si aplica)
7. Crear controlador UI en `src/main/java/com/econovafx/modules/<módulo>/ui/controller/` (si aplica)
8. Si el módulo es nuevo y tiene entidades, añadir el `--add-opens` correspondiente al `javafx-maven-plugin` en `pom.xml`
9. Agregar tests en `src/test/java/com/econovafx/modules/<módulo>/`

---

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Sigue estos pasos:

1. **Fork** el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'feat: agregar nueva funcionalidad'`) — el hook `pre-commit` valida el formato
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un **Pull Request** contra `develop`

### Convenciones de Commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/) **en inglés** (validado por el hook `commit-msg`):

```
<type>[opcional scope]: <descripción en inglés, imperativo, sin punto final>
```

- `feat:` Nueva funcionalidad
- `fix:` Corrección de bug
- `docs:` Cambios en documentación
- `style:` Formato, faltantes, etc.
- `refactor:` Refactorización
- `perf:` Mejora de rendimiento
- `test:` Agregar/modificar tests
- `build:` Build/sistema de dependencias (`pom.xml`)
- `ci:` Configuración de CI
- `chore:` Mantenimiento
- `revert:` Reversión de commits

Ejemplos: `feat(billing): add sequential invoice numbering`, `fix(accounting): prevent unbalanced journal entry`. Los cambios disruptivos usan `!` (p. ej. `feat(reporting)!: ...`).

> 📖 Política completa de contribución (incluye idioma obligatorio en inglés para código, comentarios y documentación): [CONTRIBUTING.md](CONTRIBUTING.md)

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
- **Proyecto**: [EconovaFX](https://github.com/yasmramos/EconovaFX)

---

## 🙏 Agradecimientos

- [Ebean ORM](https://ebean.io/) - Por su excelente framework ORM
- [OpenJFX](https://openjfx.io/) - Por JavaFX moderno y potente
- [Gluon](https://gluonhq.com/) - Por GluonFX y el soporte de imágenes nativas JavaFX
- [Comunidad Java Cuba](https://twitter.com/search?q=java%20cuba) - Por el apoyo continuo
- [Resolución 340/2004](https://www.gacetaoficial.cu/) - Normativa contable cubana

---

<div align="center">

**¿Te gusta este proyecto?** ¡Dale una ⭐️ en GitHub!

Hecho con ❤️ para la comunidad contable cubana

</div>
