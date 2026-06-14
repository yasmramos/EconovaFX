# Cumplimiento Resolución 340/2004 - Ministerio de Finanzas y Precios de Cuba

## Documento de Análisis e Implementación

**Gaceta Oficial No. 2 de 19 de enero de 2005**  
**Resolución No. 340/04**

---

## I. ESTADO ACTUAL DEL SISTEMA ECONOVAFX

### 1.1 Módulos Existentes

| Módulo | Estado | Cumplimiento |
|--------|--------|--------------|
| Contabilidad General | ✅ Implementado | Parcial |
| Terceros (Cobros/Pagos) | ✅ Implementado | Parcial |
| Comprobantes | ✅ Implementado | Parcial |
| Períodos Contables | ✅ Implementado | Parcial |
| Dashboard | ✅ Implementado | Completo |
| Usuarios | ✅ Implementado | Completo |

### 1.2 Módulos Pendientes de Implementación

| Módulo | Prioridad | Complejidad |
|--------|-----------|-------------|
| Efectivo (Caja y Banco) | Alta | Media |
| Inventarios | Alta | Alta |
| Activos Fijos Tangibles | Media | Alta |
| Nóminas | Media | Alta |
| Facturación | Alta | Media |
| Estados Financieros | Alta | Media |
| Consolidación | Baja | Alta |

---

## II. REQUISITOS GENERALES - CUMPLIMIENTO

### 2.1 Integridad

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Intercambio automático entre módulos | ⚠️ Parcial | Implementado entre Comprobantes y Contabilidad, pendiente para otros módulos |
| Ejecución condicionada a resultados de otros módulos | ⚠️ Parcial | Cierre de períodos implementado, falta validación cruzada completa |
| Funcionamiento mono/multiusuario | ❌ Pendiente | Requiere implementación de gestión de sesiones concurrentes |

### 2.2 Validaciones

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Validación de campos según rango y tipo | ✅ Implementado | Validaciones básicas en entidades del dominio |
| Validación de partida doble | ✅ Implementado | Método `isBalanced()` en Transaction.java |

### 2.3 Trazas y Seguridad

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Traza de procesos utilizados | ⚠️ Parcial | Logback implementado, falta auditoría específica contable |
| Traza de salvas y restauraciones | ❌ Pendiente | Requiere módulo de backup/restore con logging |
| Importar/exportar bases de datos | ⚠️ Parcial | ExportService existe, falta importación completa |

### 2.4 Instalación

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Instalación general o por módulos | ❌ Pendiente | Actualmente instalación monolítica |
| Instalación condicionada a módulos asociados | ❌ Pendiente | Requiere refactorización a arquitectura modular |
| Múltiples empresas bajo un mismo fondo informativo | ❌ Pendiente | Requiere implementación de multi-tenancy |

### 2.5 Documentación Obligatoria

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Manual de Usuario | ❌ Pendiente | Requiere documentación completa |
| Manual de Explotación | ❌ Pendiente | Requiere documentación técnica |
| Actualización permanente | ❌ Pendiente | Requiere proceso de versionado |

### 2.6 Reportes y Pantallas

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Reportes por pantalla, impresora o diferido | ⚠️ Parcial | PDFBox y POI disponibles, falta implementación completa |
| Campo número de documento primario en captación | ⚠️ Parcial | Implementado en algunos formularios |
| Reimpresión y selección de rango de páginas | ❌ Pendiente | Requiere implementación |
| Metadatos en reportes (fecha, páginas, título, entidad) | ❌ Pendiente | Requiere estandarización de reportes |

### 2.7 Mensajes y Ayuda

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Mensajes de error claros y precisos | ⚠️ Parcial | ModernDialog implementado, falta estandarización |
| Ayuda en Línea por proceso | ❌ Pendiente | Requiere sistema de ayuda contextual |

### 2.8 Otros Requisitos Técnicos

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Reindexación automática y opcional | ❌ Pendiente | H2 no requiere reindexación manual |
| Reportes básicos nominalizados | ⚠️ Parcial | Algunos reportes existentes |
| Soporte multimoneda | ❌ Pendiente | Requiere implementación según Normas Contables Cubanas |
| Consolidación de estados financieros | ❌ Pendiente | Requiere módulo específico |
| Sistema operativo soportado | ✅ Compatible | Java multiplataforma |

---

## III. MÓDULO DE CONTABILIDAD - ANÁLISIS DETALLADO

### 3.1 Procesos Fundamentales

#### Apertura de Saldos

| Requisito | Estado | Clase/Componente | Observaciones |
|-----------|--------|------------------|---------------|
| Apertura desde Balance de Comprobación | ⚠️ Parcial | AccountingPeriodService | Falta implementación completa |
| Cierre condicionado a cuadre del balance | ⚠️ Parcial | AccountingPeriod.java | Validación básica existente |

#### Operaciones

| Requisito | Estado | Clase/Componente | Observaciones |
|-----------|--------|------------------|---------------|
| Pantalla de captación de partidas | ✅ Implementado | ComprobanteFormController.java | Formulario funcional |
| Cuadre automático del comprobante | ✅ Implementado | Transaction.isBalanced() | Validación en tiempo real |
| Borrado solo si no traspasado al Mayor | ⚠️ Parcial | TransactionService.java | Validación por implementar |
| Imposibilidad de borrar cuentas con movimientos | ❌ Pendiente | AccountService.java | Requiere validación |
| Traspaso al Mayor por opción | ⚠️ Parcial | Transaction.isPosted | Campo existe, falta proceso |
| Información sobre comprobantes | ⚠️ Parcial | ComprobantesController.java | Visualización parcial |
| Visualización de 3 ejercicios anteriores | ❌ Pendiente | ComprobantesController.java | Requiere filtro por período |

#### Posteo (Traspaso al Mayor)

| Requisito | Estado | Clase/Componente | Observaciones |
|-----------|--------|------------------|---------------|
| Número consecutivo automático | ⚠️ Parcial | Transaction.number | Generación básica |
| Comprobantes cuadrados | ✅ Implementado | Transaction.isBalanced() | Validación existente |
| Fecha del período vigente | ⚠️ Parcial | AccountingPeriodService | Validación por reforzar |
| Registro impreso/visual antes de postear | ❌ Pendiente | - | Requiere vista previa |
| Inclusión en Fichero Histórico | ⚠️ Parcial | Transaction.isPosted | Marcador existe |

### 3.2 Informes Obligatorios

| Informe | Estado | Prioridad | Observaciones |
|---------|--------|-----------|---------------|
| Edición de comprobantes | ⚠️ Parcial | Alta | Formato PDF pendiente |
| Consultas al Mayor | ❌ Pendiente | Alta | Requiere implementación |
| Listado Fichero Histórico | ❌ Pendiente | Alta | Detalle por cuenta |
| Balance de Comprobación | ❌ Pendiente | Alta | Cuentas y subcuentas |
| Fichero Histórico de Comprobantes | ❌ Pendiente | Media | Listado completo |

### 3.3 Cierres

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Cierre mensual con validaciones | ⚠️ Parcial | AccountingPeriodService tiene lógica básica |
| Cierre anual con requisitos | ❌ Pendiente | Requiere cierre de cuentas nominales |
| Imposibilidad de reapertura | ⚠️ Parcial | PeriodStatus.LOCKED existe |

### 3.4 Estados Financieros

| Requisito | Estado | Observaciones |
|-----------|--------|---------------|
| Modelos según normas cubanas | ❌ Pendiente | Requiere configuración específica |
| Traslado de saldos desde Contabilidad | ❌ Pendiente | Proceso automático por implementar |
| Tecleo de datos no registrados | ❌ Pendiente | Interfaz de ajuste manual |
| Totales y subtotales automáticos | ❌ Pendiente | Motor de cálculos |

---

## IV. MÓDULO DE EFECTIVO (CAJA Y BANCO) - PLAN DE IMPLEMENTACIÓN

### 4.1 Entidades Requeridas

```java
// Nuevas entidades a crear:
- CashAccount (Cuenta de Caja)
- BankAccount (Cuenta Bancaria)
- BankStatement (Extracto Bancario)
- BankReconciliation (Conciliación Bancaria)
- CashFlow (Flujo de Caja)
```

### 4.2 Funcionalidades Clave

| Funcionalidad | Descripción | Prioridad |
|--------------|-------------|-----------|
| Asociación cuentas contables-bancarias | Mapeo entre Account y BankAccount | Alta |
| Conciliación bancaria | Métodos: saldo final, saldo ajustado, partidas pendientes | Alta |
| Flujos de caja | Análisis directo e indirecto | Media |
| Comprobantes de efectivo | Generación automática y traspaso a Contabilidad | Alta |

### 4.3 Estructura Propuesta

```
src/main/java/com/econovafx/domain/cash/
├── CashAccount.java
├── BankAccount.java
├── BankStatement.java
├── BankReconciliation.java
└── CashFlowEntry.java

src/main/java/com/econovafx/service/cash/
├── CashAccountService.java
├── BankAccountService.java
├── BankReconciliationService.java
└── CashFlowService.java

src/main/java/com/econovafx/ui/controller/cash/
├── CashAccountsController.java
├── BankAccountsController.java
├── BankReconciliationController.java
└── CashFlowController.java
```

---

## V. MÓDULO DE INVENTARIOS - PLAN DE IMPLEMENTACIÓN

### 5.1 Entidades Requeridas

```java
// Nuevas entidades a crear:
- Warehouse (Almacén)
- Product (Producto)
- InventoryMovement (Movimiento de Inventario)
- InventoryCount (Conteo de Inventario)
- InventoryValuation (Valoración de Inventarios)
```

### 5.2 Datos Obligatorios

#### Almacenes
- Código
- Descripción
- Estatus (abierto/cerrado)

#### Productos
- Código
- Descripción
- Unidad de medida
- Precio unitario
- Existencia
- Fecha de apertura
- Ubicación (sección, estante, casilla)
- Cuenta-subcuenta de inventario

### 5.3 Métodos de Valoración

| Método | Descripción | Implementación |
|--------|-------------|----------------|
| Promedio Ponderado | Precio promedio móvil | Prioridad Alta |
| PEPS (FIFO) | Primero en entrar, primero en salir | Prioridad Media |
| Identificación Específica | Por lote/serie | Prioridad Baja |

### 5.4 Estructura Propuesta

```
src/main/java/com/econovafx/domain/inventory/
├── Warehouse.java
├── Product.java
├── InventoryMovement.java
├── MovementType.java (ENTRADA, SALIDA, TRANSFERENCIA, AJUSTE)
├── InventoryCount.java
└── InventoryValuation.java

src/main/java/com/econovafx/service/inventory/
├── WarehouseService.java
├── ProductService.java
├── InventoryMovementService.java
├── InventoryCountService.java
└── InventoryValuationService.java
```

---

## VI. MÓDULO DE COBROS Y PAGOS - EXTENSIÓN

### 6.1 Mejoras Requeridas sobre ThirdParty Actual

| Funcionalidad | Estado Actual | Mejora Requerida |
|--------------|---------------|------------------|
| Facturas de clientes | ❌ No implementado | Entity Invoice con estado de cobro |
| Facturas de proveedores | ❌ No implementado | Entity Bill con estado de pago |
| Análisis por edades | ❌ No implementado | Reporte aging de cartera |
| Submayores por deudor/acreedor | ❌ No implementado | Consultas específicas |

### 6.2 Entidades Adicionales

```java
// Nuevas entidades:
- Invoice (Factura de Venta)
- Bill (Factura de Compra)
- Payment (Pago)
- Receipt (Cobro)
- AgingReport (Reporte de Antigüedad)
```

---

## VII. MÓDULO DE FACTURACIÓN - PLAN DE IMPLEMENTACIÓN

### 7.1 Requisitos de Factura

| Requisito | Implementación |
|-----------|----------------|
| Numeración consecutiva automática | Secuenciador por tipo de factura |
| Condiciones de pago | Contado, crédito, combinado |
| Descuentos | Porcentaje, monto fijo, por volumen |
| Plazos de cobro | Días crédito, vencimiento |
| Fechas de vencimiento | Cálculo automático |

### 7.2 Controles

| Control | Descripción |
|---------|-------------|
| Afectación a inventario y clientes | Integración con módulos |
| No modificación de facturas emitidas | Cancelación y nueva emisión |
| Órdenes de pedido | Workflow completo |
| Anulación de pedidos | Con trazabilidad |

---

## VIII. MÓDULO DE ACTIVOS FIJOS TANGIBLES - PLAN DE IMPLEMENTACIÓN

### 8.1 Entidades Requeridas

```java
// Nuevas entidades:
- FixedAsset (Activo Fijo)
- AssetCategory (Categoría de Activo)
- AssetMovement (Movimiento de Activo)
- DepreciationMethod (Método de Depreciación)
- DepreciationEntry (Asiento de Depreciación)
- MaintenanceRecord (Registro de Mantenimiento)
```

### 8.2 Datos del Activo Fijo

| Grupo de Datos | Campos |
|---------------|--------|
| Referencia | Número inventario, nombre, descripción |
| Control | Fecha alta, vida útil, valor residual |
| Cuantitativos | Costo original, depreciación acumulada, valor neto |
| Características técnicas | Marca, modelo, serie, especificaciones |
| Depreciaciones | Método, tasa, frecuencia |

### 8.3 Tipos de Movimientos

- Altas
- Bajas
- Traslados
- Revalorizaciones
- Reparaciones que incrementan valor
- Alquileres
- Envío a reparar

### 8.4 Métodos de Depreciación

| Método | Descripción | Normativa Cuba |
|--------|-------------|----------------|
| Línea Recta | Depreciación constante | Principal |
| Saldo Decreciente | Acelerada | Opcional |
| Unidades de Producción | Según uso | Específico |

---

## IX. MÓDULO DE NÓMINAS - PLAN DE IMPLEMENTACIÓN

### 9.1 Entidades Requeridas

```java
// Nuevas entidades:
- Employee (Trabajador)
- SalaryStructure (Estructura Salarial)
- Payroll (Nómina)
- PayrollEntry (Detalle de Nómina)
- Deduction (Descuento)
- Retention (Retención)
- Vacation (Vacaciones)
- SocialSecurity (Seguridad Social)
```

### 9.2 Cálculos Automáticos Requeridos

| Concepto | Porcentaje/Base | Normativa |
|----------|-----------------|-----------|
| 9.09% (Vacaciones) | 9.09% del salario | Ley cubana |
| Impuesto Fuerza de Trabajo | Según escala | ONAT |
| Contribución Seguridad Social | 5% trabajador | MTSS |
| Retenciones registradas | Variables | Configurables |

### 9.3 Tipos de Nómina

- Sueldos (mensual)
- Jornales (diario/semanal)
- Vacaciones
- Subsidios
- Décimo tercer mes (si aplica)

---

## X. MATRIZ DE CUMPLIMIENTO GENERAL

### Leyenda
- ✅ Completo: Implementado y funcional
- ⚠️ Parcial: Implementado parcialmente o requiere ajustes
- ❌ Pendiente: No implementado
- 📋 Planificado: En plan de desarrollo

| Módulo | Requisitos Generales | Contabilidad | Efectivo | Inventarios | Cobros/Pagos | Facturación | Activos Fijos | Nóminas |
|--------|---------------------|--------------|----------|-------------|--------------|-------------|---------------|---------|
| Integridad | ⚠️ | ⚠️ | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ |
| Validaciones | ✅ | ✅ | ❌ | ❌ | ⚠️ | ❌ | ❌ | ❌ |
| Trazas | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Reportes | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Cierres | ⚠️ | ⚠️ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Estados Financieros | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## XI. PLAN DE TRABAJO PRIORIZADO

### Fase 1 - Crítico (Mes 1-2)

1. **Completar Módulo de Contabilidad**
   - Balance de Comprobación completo
   - Libro Mayor con consultas
   - Fichero Histórico de Comprobantes
   - Cierre mensual robusto

2. **Módulo de Efectivo**
   - Cuentas de caja y banco
   - Conciliación bancaria
   - Comprobantes de efectivo

3. **Mejorar Cobros y Pagos**
   - Facturas pendientes
   - Análisis por edades
   - Submayores

### Fase 2 - Alto Impacto (Mes 3-4)

4. **Módulo de Inventarios**
   - Gestión de almacenes
   - Control de productos
   - Valoración promedio ponderado
   - Movimientos básicos

5. **Módulo de Facturación**
   - Emisión de facturas
   - Control de numeración
   - Integración con inventario y cobros

### Fase 3 - Completamiento (Mes 5-6)

6. **Estados Financieros**
   - Balance General
   - Estado de Resultados
   - Flujo de Efectivo

7. **Módulo de Activos Fijos**
   - Registro de activos
   - Depreciación línea recta
   - Movimientos básicos

8. **Módulo de Nóminas**
   - Registro de trabajadores
   - Cálculo de nómina básica
   - Retenciones principales

### Fase 4 - Avanzado (Mes 7+)

9. **Características Avanzadas**
   - Multi-empresa
   - Consolidación
   - Multimoneda
   - Help Desk integrado
   - Reportes avanzados

---

## XII. RECOMENDACIONES TÉCNICAS

### 12.1 Arquitectura

1. **Mantener arquitectura actual** basada en:
   - Entity-Service-Controller pattern
   - Ebean ORM para persistencia
   - JavaFX para UI

2. **Implementar patrón Repository** para mejor separación de responsabilidades

3. **Agregar capa de Dominio Rico** para reglas de negocio complejas

### 12.2 Base de Datos

1. **Migrar a PostgreSQL** para producción (mantener H2 para desarrollo/testing)

2. **Implementar esquemas separados** por empresa para multi-tenancy

3. **Agregar índices estratégicos** para reportes pesados

### 12.3 Seguridad

1. **Implementar RBAC** (Role-Based Access Control) completo

2. **Auditoría de todas las operaciones** contables

3. **Encriptación de datos sensibles**

### 12.4 Performance

1. **Paginación en todos los listados** grandes

2. **Cache de consultas frecuentes**

3. **Procesos batch para cierres**

---

## XIII. DOCUMENTACIÓN REQUERIDA

### 13.1 Manual de Usuario

Debe incluir:
- Introducción al sistema
- Instalación paso a paso
- Guía por módulo con capturas de pantalla
- Flujos de trabajo típicos
- Solución de problemas comunes
- Glosario de términos contables

### 13.2 Manual de Explotación

Debe incluir:
- Arquitectura del sistema
- Modelo de datos completo (DER)
- Interrelaciones entre módulos
- API de integración
- Procedimientos de backup/restore
- Guía de troubleshooting técnico

---

## XIV. CONCLUSIÓN

El sistema EconoNova FX actualmente cuenta con una base sólida para la gestión contable básica, pero requiere extensiones significativas para cumplir completamente con la Resolución 340/2004 del Ministerio de Finanzas y Precios de Cuba.

**Puntos Fuertes Actuales:**
- Arquitectura limpia y escalable
- Módulo de contabilidad funcional
- Validación de partida doble implementada
- Uso de tecnologías modernas

**Áreas Críticas a Desarrollar:**
- Módulos especializados (Inventarios, Activos Fijos, Nóminas)
- Estados financieros normativos
- Sistema de reportes completo
- Trazas y auditoría contable
- Documentación oficial

**Tiempo Estimado de Implementación Completa:** 6-8 meses con equipo de 2-3 desarrolladores

---

*Documento creado: Junio 2024*  
*Versión: 1.0*  
*Para: Equipo de Desarrollo EconoNova FX*
