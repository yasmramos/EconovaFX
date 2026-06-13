package com.econovafx.service;

import com.econovafx.domain.*;
import com.econovafx.repository.MovimientoEfectivoRepository;
import io.avaje.inject.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Cash Movement operations
 */
@Component
public class MovimientoEfectivoService {

    private static final Logger logger = LoggerFactory.getLogger(MovimientoEfectivoService.class);

    private final MovimientoEfectivoRepository repository;
    private final TransactionService transactionService;
    private final CuentaBancariaService cuentaBancariaService;
    private final CajaService cajaService;

    public MovimientoEfectivoService(
            MovimientoEfectivoRepository repository,
            TransactionService transactionService,
            CuentaBancariaService cuentaBancariaService,
            CajaService cajaService) {
        this.repository = repository;
        this.transactionService = transactionService;
        this.cuentaBancariaService = cuentaBancariaService;
        this.cajaService = cajaService;
    }

    public List<MovimientoEfectivo> findAll() {
        return repository.findAll();
    }

    public Optional<MovimientoEfectivo> findById(Long id) {
        return repository.findById(id);
    }

    public MovimientoEfectivo save(MovimientoEfectivo movimiento) {
        // Validation: amount must be positive
        if (movimiento.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Validation: either bank account or cash box must be set
        if (movimiento.getCuentaBancaria() == null && movimiento.getCaja() == null) {
            throw new IllegalArgumentException("Either bank account or cash box must be specified");
        }

        // Validation: accounting account is required
        if (movimiento.getCuentaContrapartida() == null) {
            throw new IllegalArgumentException("Counterpart accounting account is required");
        }

        // Generate consecutive number if not set
        if (movimiento.getNumeroComprobante() == null || movimiento.getNumeroComprobante().trim().isEmpty()) {
            movimiento.setNumeroComprobante(generateNumeroComprobante());
        }

        logger.info("Saving cash movement: {}", movimiento.getNumeroComprobante());
        return repository.save(movimiento);
    }

    public MovimientoEfectivo postear(Long id, User usuario) {
        Optional<MovimientoEfectivo> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Cash movement not found with id: " + id);
        }

        MovimientoEfectivo movimiento = optional.get();

        if (movimiento.getEstaAnulado()) {
            throw new IllegalStateException("Cannot post a cancelled movement");
        }

        if (movimiento.getEsPosteado()) {
            throw new IllegalStateException("Movement already posted");
        }

        // Create accounting transaction
        Transaction comprobante = crearComprobanteContable(movimiento, usuario);
        movimiento.setComprobanteContable(comprobante);
        movimiento.setEsPosteado(true);
        movimiento.setFechaPosteo(LocalDateTime.now());
        movimiento.setUsuarioPosteo(usuario);

        // Update balance
        actualizarSaldo(movimiento);

        logger.info("Posting cash movement: {}", movimiento.getNumeroComprobante());
        return repository.save(movimiento);
    }

    public MovimientoEfectivo anular(Long id, User usuario, String motivo) {
        Optional<MovimientoEfectivo> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Cash movement not found with id: " + id);
        }

        MovimientoEfectivo movimiento = optional.get();

        if (movimiento.getEstaAnulado()) {
            throw new IllegalStateException("Movement already cancelled");
        }

        if (!movimiento.getEsPosteado()) {
            throw new IllegalStateException("Cannot cancel a movement that has not been posted");
        }

        movimiento.setEstaAnulado(true);
        movimiento.setFechaAnulacion(LocalDateTime.now());
        movimiento.setUsuarioAnulacion(usuario);
        movimiento.setMotivoAnulacion(motivo);

        // Reverse balance
        revertirSaldo(movimiento);

        logger.info("Cancelling cash movement: {}", movimiento.getNumeroComprobante());
        return repository.save(movimiento);
    }

    public List<MovimientoEfectivo> findByCuentaBancaria(Long cuentaBancariaId) {
        return repository.findByCuentaBancaria(cuentaBancariaId);
    }

    public List<MovimientoEfectivo> findByCaja(Long cajaId) {
        return repository.findByCaja(cajaId);
    }

    public List<MovimientoEfectivo> findNoPosteados() {
        return repository.findNoPosteados();
    }

    public List<MovimientoEfectivo> findByCuentaBancariaAndFechaBetween(
            Long cuentaBancariaId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findByCuentaBancariaAndFechaBetween(cuentaBancariaId, fechaInicio, fechaFin);
    }

    private String generateNumeroComprobante() {
        // Simple consecutive number generation
        // In production, this should be more sophisticated
        long count = repository.findAll().size() + 1;
        return String.format("EF-%08d", count);
    }

    private Transaction crearComprobanteContable(MovimientoEfectivo movimiento, User usuario) {
        Transaction comprobante = new Transaction();
        comprobante.setNumber(movimiento.getNumeroComprobante());
        comprobante.setDate(movimiento.getFecha());
        comprobante.setType("EFECTIVO");
        comprobante.setDescription(movimiento.getDescripcion());
        comprobante.setReference(movimiento.getNumeroComprobante());
        comprobante.setCreatedBy(usuario);
        comprobante.setIsPosted(false);

        Account cuentaEfectivo;
        if (movimiento.getCuentaBancaria() != null) {
            cuentaEfectivo = movimiento.getCuentaBancaria().getCuentaContable();
        } else {
            cuentaEfectivo = movimiento.getCaja().getCuentaContable();
        }

        // Create entries based on movement type
        if ("INCOME".equals(movimiento.getTipoMovimiento())) {
            // Debit: Cash/Bank account
            TransactionEntry entryDebito = new TransactionEntry();
            entryDebito.setAccount(cuentaEfectivo);
            entryDebito.setDebitAmount(movimiento.getImporte());
            entryDebito.setCreditAmount(BigDecimal.ZERO);
            entryDebito.setDescription("Ingreso: " + movimiento.getDescripcion());
            comprobante.addEntry(entryDebito);

            // Credit: Counterpart account
            TransactionEntry entryCredito = new TransactionEntry();
            entryCredito.setAccount(movimiento.getCuentaContrapartida());
            entryCredito.setDebitAmount(BigDecimal.ZERO);
            entryCredito.setCreditAmount(movimiento.getImporte());
            entryCredito.setDescription("Ingreso: " + movimiento.getDescripcion());
            comprobante.addEntry(entryCredito);
        } else if ("EXPENSE".equals(movimiento.getTipoMovimiento())) {
            // Debit: Counterpart account
            TransactionEntry entryDebito = new TransactionEntry();
            entryDebito.setAccount(movimiento.getCuentaContrapartida());
            entryDebito.setDebitAmount(movimiento.getImporte());
            entryDebito.setCreditAmount(BigDecimal.ZERO);
            entryDebito.setDescription("Egreso: " + movimiento.getDescripcion());
            comprobante.addEntry(entryDebito);

            // Credit: Cash/Bank account
            TransactionEntry entryCredito = new TransactionEntry();
            entryCredito.setAccount(cuentaEfectivo);
            entryCredito.setDebitAmount(BigDecimal.ZERO);
            entryCredito.setCreditAmount(movimiento.getImporte());
            entryCredito.setDescription("Egreso: " + movimiento.getDescripcion());
            comprobante.addEntry(entryCredito);
        }

        // Post the transaction
        return transactionService.post(comprobante.getId(), usuario);
    }

    private void actualizarSaldo(MovimientoEfectivo movimiento) {
        BigDecimal importe = movimiento.getImporte();

        if (movimiento.getCuentaBancaria() != null) {
            CuentaBancaria cuenta = movimiento.getCuentaBancaria();
            BigDecimal saldoActual = cuenta.getSaldoActual() != null ? cuenta.getSaldoActual() : BigDecimal.ZERO;

            if ("INCOME".equals(movimiento.getTipoMovimiento())) {
                cuenta.setSaldoActual(saldoActual.add(importe));
            } else if ("EXPENSE".equals(movimiento.getTipoMovimiento())) {
                cuenta.setSaldoActual(saldoActual.subtract(importe));
            }

            cuentaBancariaService.save(cuenta);
        } else if (movimiento.getCaja() != null) {
            Caja caja = movimiento.getCaja();
            BigDecimal saldoActual = caja.getSaldoActual() != null ? caja.getSaldoActual() : BigDecimal.ZERO;

            if ("INCOME".equals(movimiento.getTipoMovimiento())) {
                caja.setSaldoActual(saldoActual.add(importe));
            } else if ("EXPENSE".equals(movimiento.getTipoMovimiento())) {
                caja.setSaldoActual(saldoActual.subtract(importe));
            }

            cajaService.save(caja);
        }
    }

    private void revertirSaldo(MovimientoEfectivo movimiento) {
        BigDecimal importe = movimiento.getImporte();

        if (movimiento.getCuentaBancaria() != null) {
            CuentaBancaria cuenta = movimiento.getCuentaBancaria();
            BigDecimal saldoActual = cuenta.getSaldoActual() != null ? cuenta.getSaldoActual() : BigDecimal.ZERO;

            if ("INCOME".equals(movimiento.getTipoMovimiento())) {
                cuenta.setSaldoActual(saldoActual.subtract(importe));
            } else if ("EXPENSE".equals(movimiento.getTipoMovimiento())) {
                cuenta.setSaldoActual(saldoActual.add(importe));
            }

            cuentaBancariaService.save(cuenta);
        } else if (movimiento.getCaja() != null) {
            Caja caja = movimiento.getCaja();
            BigDecimal saldoActual = caja.getSaldoActual() != null ? caja.getSaldoActual() : BigDecimal.ZERO;

            if ("INCOME".equals(movimiento.getTipoMovimiento())) {
                caja.setSaldoActual(saldoActual.subtract(importe));
            } else if ("EXPENSE".equals(movimiento.getTipoMovimiento())) {
                caja.setSaldoActual(saldoActual.add(importe));
            }

            cajaService.save(caja);
        }
    }
}
