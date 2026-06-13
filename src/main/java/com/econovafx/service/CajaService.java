package com.econovafx.service;

import com.econovafx.domain.Caja;
import com.econovafx.repository.CajaRepository;
import io.avaje.inject.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service for Cash Box operations
 */
@Component
public class CajaService {

    private static final Logger logger = LoggerFactory.getLogger(CajaService.class);

    private final CajaRepository repository;

    public CajaService(CajaRepository repository) {
        this.repository = repository;
    }

    public List<Caja> findAll() {
        return repository.findAll();
    }

    public List<Caja> findActivas() {
        return repository.findActivas();
    }

    public Optional<Caja> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Caja> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public Caja save(Caja caja) {
        // Validation: code must be unique
        if (caja.getCodigo() != null && !caja.getCodigo().trim().isEmpty()) {
            Optional<Caja> existing = repository.findByCodigo(caja.getCodigo());
            if (existing.isPresent() && !existing.get().getId().equals(caja.getId())) {
                throw new IllegalArgumentException("Cash box code already exists: " + caja.getCodigo());
            }
        }

        // Validation: accounting account is required
        if (caja.getCuentaContable() == null) {
            throw new IllegalArgumentException("Accounting account is required for cash box");
        }

        logger.info("Saving cash box: {}", caja.getCodigo());
        return repository.save(caja);
    }

    public void delete(Long id) {
        logger.info("Deleting cash box with id: {}", id);
        repository.delete(id);
    }

    public boolean existsByCodigo(String codigo) {
        return repository.findByCodigo(codigo).isPresent();
    }
}
