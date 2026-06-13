package com.econovafx.service;

import com.econovafx.domain.*;
import com.econovafx.repository.ConciliacionBancariaRepository;
import io.avaje.inject.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Bank Reconciliation operations
 * Implements reconciliation according to Cuban Resolution 340/2004
 */
@Component
public class ConciliacionBancariaService {

    private static final Logger logger = LoggerFactory.getLogger(ConciliacionBancariaService.class);

    private final ConciliacionBancariaRepository repository;
    private final MovimientoEfectivoService movimientoEfectivoService;
    private final CuentaBancariaService cuentaBancariaService;

    public ConciliacionBancariaService(
            ConciliacionBancariaRepository repository,
            MovimientoEfectivoService movimientoEfectivoService,
            CuentaBancariaService cuentaBancariaService) {
        this.repository = repository;
        this.movimientoEfectivoService = movimientoEfectivoService;
        this.cuentaBancariaService = cuentaBancariaService;
    }

    public List<ConciliacionBancaria> findAll() {
        return repository.findAll();
    }

    public Optional<ConciliacionBancaria> findById(Long id) {
        return repository.findById(id);
    }

    public ConciliacionBancaria save(ConciliacionBancaria conciliacion) {
        // Validation: bank account is required
        if (conciliacion.getCuentaBancaria() == null) {
            throw new IllegalArgumentException("Bank account is required for reconciliation");
        }

        // Validation: date range is valid
        if (conciliacion.getFechaInicio().isAfter(conciliacion.getFechaFin())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Generate consecutive number if not set
        if (conciliacion.getNumeroConciliacion() == null || conciliacion.getNumeroConciliacion().trim().isEmpty()) {
            conciliacion.setNumeroConciliacion(generateNumeroConciliacion());
        }

        // Calculate difference
        calcularDiferencia(conciliacion);

        logger.info("Saving bank reconciliation: {}", conciliacion.getNumeroConciliacion());
        return repository.save(conciliacion);
    }

    public ConciliacionBancaria agregarPartidaSistema(Long conciliacionId, PartidaConciliacion partida) {
        Optional<ConciliacionBancaria> optional = repository.findById(conciliacionId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation not found with id: " + conciliacionId);
        }

        ConciliacionBancaria conciliacion = optional.get();

        if (conciliacion.getEstaCuadrada()) {
            throw new IllegalStateException("Cannot modify a completed reconciliation");
        }

        partida.setTipoPartida("SISTEMA");
        partida.setEstaConciliada(false);
        conciliacion.addPartida(partida);

        return save(conciliacion);
    }

    public ConciliacionBancaria agregarPartidaBanco(Long conciliacionId, PartidaConciliacion partida) {
        Optional<ConciliacionBancaria> optional = repository.findById(conciliacionId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation not found with id: " + conciliacionId);
        }

        ConciliacionBancaria conciliacion = optional.get();

        if (conciliacion.getEstaCuadrada()) {
            throw new IllegalStateException("Cannot modify a completed reconciliation");
        }

        partida.setTipoPartida("BANCO");
        partida.setEstaConciliada(false);
        conciliacion.addPartida(partida);

        return save(conciliacion);
    }

    public ConciliacionBancaria marcarPartidaConciliada(Long conciliacionId, Long partidaId) {
        Optional<ConciliacionBancaria> optional = repository.findById(conciliacionId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation not found with id: " + conciliacionId);
        }

        ConciliacionBancaria conciliacion = optional.get();

        if (conciliacion.getEstaCuadrada()) {
            throw new IllegalStateException("Cannot modify a completed reconciliation");
        }

        for (PartidaConciliacion partida : conciliacion.getPartidas()) {
            if (partida.getId().equals(partidaId)) {
                partida.setEstaConciliada(!partida.getEstaConciliada());
                break;
            }
        }

        // Recalculate balances and difference
        calcularSaldosFinales(conciliacion);
        calcularDiferencia(conciliacion);

        // Check if reconciliation is balanced
        if (conciliacion.isBalanced()) {
            conciliacion.setEstaCuadrada(true);
            logger.info("Reconciliation {} is now balanced", conciliacion.getNumeroConciliacion());
        }

        return save(conciliacion);
    }

    public ConciliacionBancaria postear(Long id, User usuario) {
        Optional<ConciliacionBancaria> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation not found with id: " + id);
        }

        ConciliacionBancaria conciliacion = optional.get();

        if (!conciliacion.isBalanced()) {
            throw new IllegalStateException("Cannot post an unbalanced reconciliation");
        }

        if (conciliacion.getEsPosteada()) {
            throw new IllegalStateException("Reconciliation already posted");
        }

        conciliacion.setEsPosteada(true);
        conciliacion.setFechaPosteo(LocalDateTime.now());
        conciliacion.setUsuarioPosteo(usuario);

        // Create accounting entries for reconciling items if needed
        crearAsientosAjuste(conciliacion, usuario);

        logger.info("Posting bank reconciliation: {}", conciliacion.getNumeroConciliacion());
        return repository.save(conciliacion);
    }

    public List<ConciliacionBancaria> findByCuentaBancaria(Long cuentaBancariaId) {
        return repository.findByCuentaBancaria(cuentaBancariaId);
    }

    public List<ConciliacionBancaria> findNoPosteadas() {
        return repository.findNoPosteadas();
    }

    public Optional<ConciliacionBancaria> findUltimaPorCuentaBancaria(Long cuentaBancariaId) {
        return repository.findUltimaPorCuentaBancaria(cuentaBancariaId);
    }

    public List<MovimientoEfectivo> getMovimientosNoConciliados(Long cuentaBancariaId, LocalDate fechaCorte) {
        // Get all movements up to the cutoff date
        List<MovimientoEfectivo> movimientos = movimientoEfectivoService.findByCuentaBancariaAndFechaBetween(
                cuentaBancariaId, LocalDate.of(2000, 1, 1), fechaCorte);

        // Filter out movements that are already in a reconciliation
        // This is a simplified implementation
        return movimientos;
    }

    private String generateNumeroConciliacion() {
        long count = repository.findAll().size() + 1;
        return String.format("CB-%08d", count);
    }

    private void calcularDiferencia(ConciliacionBancaria conciliacion) {
        BigDecimal saldoSistemaAjustado = conciliacion.getSaldoSegunSistemaFinal() != null ? 
                conciliacion.getSaldoSegunSistemaFinal() : BigDecimal.ZERO;
        BigDecimal saldoBancoAjustado = conciliacion.getSaldoSegunBancoFinal() != null ? 
                conciliacion.getSaldoSegunBancoFinal() : BigDecimal.ZERO;

        BigDecimal diferencia = saldoSistemaAjustado.subtract(saldoBancoAjustado);
        conciliacion.setDiferencia(diferencia);
    }

    private void calcularSaldosFinales(ConciliacionBancaria conciliacion) {
        BigDecimal totalPartidasSistema = BigDecimal.ZERO;
        BigDecimal totalPartidasBanco = BigDecimal.ZERO;

        for (PartidaConciliacion partida : conciliacion.getPartidas()) {
            if (partida.getEstaConciliada()) {
                if ("SISTEMA".equals(partida.getTipoPartida())) {
                    totalPartidasSistema = totalPartidasSistema.add(partida.getImporte());
                } else if ("BANCO".equals(partida.getTipoPartida())) {
                    totalPartidasBanco = totalPartidasBanco.add(partida.getImporte());
                }
            }
        }

        BigDecimal saldoSistemaFinal = conciliacion.getSaldoSegunSistemaInicial()
                .add(totalPartidasSistema);
        BigDecimal saldoBancoFinal = conciliacion.getSaldoSegunBancoInicial()
                .add(totalPartidasBanco);

        conciliacion.setSaldoSegunSistemaFinal(saldoSistemaFinal);
        conciliacion.setSaldoSegunBancoFinal(saldoBancoFinal);
    }

    private void crearAsientosAjuste(ConciliacionBancaria conciliacion, User usuario) {
        // Create accounting entries for bank charges, interest, etc.
        // This is a simplified implementation
        // In production, this would create actual Transaction entities

        for (PartidaConciliacion partida : conciliacion.getPartidas()) {
            if ("BANCO".equals(partida.getTipoPartida()) && partida.getEstaConciliada()) {
                // This is a bank-only item that needs to be recorded in the system
                // Create a cash movement for it
                logger.debug("Creating adjustment for bank item: {}", partida.getDescripcion());
            }
        }
    }
}
