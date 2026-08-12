package com.econovafx.modules.fixedassets.controller;

import com.econovafx.modules.fixedassets.model.FixedAsset;
import com.econovafx.modules.fixedassets.model.FixedAssetCategory;
import com.econovafx.modules.fixedassets.model.DepreciationRecord;
import com.econovafx.modules.fixedassets.service.DepreciationService;
import com.econovafx.modules.fixedassets.repository.FixedAssetRepository;
import com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Fixed Assets management.
 * Handles CRUD operations and depreciation calculations.
 * Compliant with Cuba ONAT Resolution 340-2004
 */
@Component
public class FixedAssetController {

    @Inject
    DepreciationService depreciationService;
    
    @Inject
    FixedAssetRepository fixedAssetRepository;
    
    @Inject
    DepreciationRecordRepository depreciationRecordRepository;

    /**
     * Get all fixed assets
     */
    public List<FixedAsset> getAllAssets() {
        return fixedAssetRepository.findAll();
    }

    /**
     * Get fixed asset by ID
     */
    public Optional<FixedAsset> getAssetById(Long id) {
        return fixedAssetRepository.findById(id);
    }

    /**
     * Create new fixed asset
     */
    public FixedAsset createAsset(FixedAsset asset) {
        if (asset.getAcquisitionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Acquisition date cannot be in the future");
        }
        
        if (asset.getResidualValue() != null && 
            asset.getResidualValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Residual value must be non-negative");
        }
        
        return fixedAssetRepository.save(asset);
    }

    /**
     * Update fixed asset
     */
    public FixedAsset updateAsset(Long id, FixedAsset assetDetails) {
        FixedAsset asset = fixedAssetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
        
        if (!asset.isActive()) {
            throw new IllegalStateException("Cannot update inactive asset");
        }
        
        asset.setName(assetDetails.getName());
        asset.setDescription(assetDetails.getDescription());
        asset.setOriginalValue(assetDetails.getOriginalValue());
        asset.setResidualValue(assetDetails.getResidualValue());
        asset.setUsefulLifeYears(assetDetails.getUsefulLifeYears());
        asset.setDepreciationMethod(assetDetails.getDepreciationMethod());
        asset.setAcquisitionDate(assetDetails.getAcquisitionDate());
        asset.setCategory(assetDetails.getCategory());
        
        return fixedAssetRepository.save(asset);
    }

    /**
     * Delete (deactivate) fixed asset
     */
    public void deleteAsset(Long id) {
        FixedAsset asset = fixedAssetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
        
        if (asset.getCurrentDepreciation().compareTo(asset.getOriginalValue()) >= 0) {
            asset.setActive(false);
            fixedAssetRepository.save(asset);
        } else {
            throw new IllegalStateException("Cannot delete asset that is not fully depreciated");
        }
    }

    /**
     * Calculate depreciation for an asset
     */
    public DepreciationRecord calculateDepreciation(Long assetId, LocalDate periodDate) {
        FixedAsset asset = fixedAssetRepository.findById(assetId)
            .orElseThrow(() -> new RuntimeException("Asset not found with id: " + assetId));
        
        return depreciationService.calculateMonthlyDepreciation(asset);
    }

    /**
     * Process monthly depreciation for all assets
     */
    public List<DepreciationRecord> processMonthlyDepreciation(LocalDate periodDate) {
        return depreciationService.processMonthlyDepreciation(
            periodDate.getYear(), 
            periodDate.getMonthValue(), 
            "system"
        );
    }

    /**
     * Get depreciation records for an asset
     */
    public List<DepreciationRecord> getDepreciationRecords(Long assetId) {
        FixedAsset asset = fixedAssetRepository.findById(assetId)
            .orElseThrow(() -> new RuntimeException("Asset not found with id: " + assetId));
        
        return depreciationRecordRepository.findByAssetOrderByPeriodDateDesc(asset);
    }

    /**
     * Revaluate an asset
     */
    public FixedAsset revaluateAsset(Long assetId, BigDecimal newValue, String reason) {
        FixedAsset asset = fixedAssetRepository.findById(assetId)
            .orElseThrow(() -> new RuntimeException("Asset not found with id: " + assetId));
        
        if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New value must be positive");
        }
        
        asset.setOriginalValue(newValue);
        asset.setRevaluationReason(reason);
        asset.setLastRevaluationDate(LocalDate.now());
        
        return fixedAssetRepository.save(asset);
    }

    /**
     * Get all fixed asset categories
     */
    public List<FixedAssetCategory> getAllCategories() {
        return fixedAssetRepository.findAllCategories();
    }

    /**
     * Create new category
     */
    public FixedAssetCategory createCategory(FixedAssetCategory category) {
        return fixedAssetRepository.save(category);
    }
}
