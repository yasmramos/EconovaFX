package com.econovafx.service;

import com.econovafx.domain.CuentaBancaria;
import com.econovafx.repository.CuentaBancariaRepository;
import io.avaje.inject.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service for Bank Account operations
 */
@Component
public class CuentaBancariaService {

    private static final Logger logger = LoggerFactory.getLogger(CuentaBancariaService.class);

    private final CuentaBancariaRepository repository;

    public CuentaBancariaService(CuentaBancariaRepository repository) {
        this.repository = repository;
    }

    public List<CuentaBancaria> findAll() {
        return repository.findAll();
    }

    public List<CuentaBancaria> findActivas() {
        return repository.findActivas();
    }

    public Optional<CuentaBancaria> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<CuentaBancaria> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public CuentaBancaria save(CuentaBancaria cuentaBancaria) {
        // Validation: code must be unique
        if (cuentaBancaria.getCodigo() != null && !cuentaBancaria.getCodigo().trim().isEmpty()) {
            Optional<CuentaBancaria> existing = repository.findByCodigo(cuentaBancaria.getCodigo());
            if (existing.isPresent() && !existing.get().getId().equals(cuentaBancaria.getId())) {
                throw new IllegalArgumentException("Bank account code already exists: " + cuentaBancaria.getCodigo());
            }
        }

        // Validation: accounting account is required
        if (cuentaBancaria.getCuentaContable() == null) {
            throw new IllegalArgumentException("Accounting account is required for bank account");
        }

        logger.info("Saving bank account: {}", cuentaBancaria.getCodigo());
        return repository.save(cuentaBancaria);
    }

    public void delete(Long id) {
        logger.info("Deleting bank account with id: {}", id);
        repository.delete(id);
    }

    public List<CuentaBancaria> findByMoneda(String moneda) {
        return repository.findByMoneda(moneda);
    }

    public boolean existsByCodigo(String codigo) {
        return repository.findByCodigo(codigo).isPresent();
    }
}
