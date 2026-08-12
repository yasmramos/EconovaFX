package com.econovafx.modules.fixedassets.controller;

import com.econovafx.modules.fixedassets.model.FixedAsset;
import com.econovafx.modules.fixedassets.model.FixedAssetCategory;
import com.econovafx.modules.fixedassets.model.DepreciationRecord;
import com.econovafx.modules.fixedassets.service.DepreciationService;
import com.econovafx.modules.fixedassets.repository.FixedAssetRepository;
import com.econovafx.modules.fixedassets.repository.DepreciationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Fixed Assets management.
 * Handles CRUD operations and depreciation calculations.
 */
@RestController
@RequestMapping("/api/fixed-assets")
@CrossOrigin(origins = "*")
public class FixedAssetController {

    @Autowired
    private FixedAssetRepository fixedAssetRepository;

    @Autowired
    private DepreciationRecordRepository depreciationRecordRepository;

    @Autowired
    private DepreciationService depreciationService;

    // Fixed Asset endpoints

    @GetMapping
    public List<FixedAsset> getAllAssets() {
        return fixedAssetRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixedAsset> getAssetById(@PathVariable Long id) {
        Optional<FixedAsset> asset = fixedAssetRepository.findById(id);
        return asset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FixedAsset> createAsset(@RequestBody FixedAsset asset) {
        if (asset.getAcquisitionDate() == null || asset.getOriginalValue() == null 
            || asset.getOriginalValue().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        FixedAsset saved = fixedAssetRepository.save(asset);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FixedAsset> updateAsset(@PathVariable Long id, @RequestBody FixedAsset assetDetails) {
        Optional<FixedAsset> optionalAsset = fixedAssetRepository.findById(id);
        if (!optionalAsset.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        FixedAsset asset = optionalAsset.get();
        if (assetDetails.getDescription() != null) {
            asset.setDescription(assetDetails.getDescription());
        }
        if (assetDetails.getSerialNumber() != null) {
            asset.setSerialNumber(assetDetails.getSerialNumber());
        }
        if (assetDetails.getLocation() != null) {
            asset.setLocation(assetDetails.getLocation());
        }
        if (assetDetails.getResidualValue() != null) {
            asset.setResidualValue(assetDetails.getResidualValue());
        }
        
        FixedAsset updated = fixedAssetRepository.save(asset);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        if (!fixedAssetRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        fixedAssetRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/dispose")
    public ResponseEntity<FixedAsset> disposeAsset(@PathVariable Long id, 
                                                    @RequestParam String reason,
                                                    @RequestParam LocalDate disposalDate) {
        try {
            FixedAsset disposed = depreciationService.disposeAsset(id, reason, disposalDate);
            return ResponseEntity.ok(disposed);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/revalue")
    public ResponseEntity<FixedAsset> revalueAsset(@PathVariable Long id,
                                                    @RequestParam BigDecimal newValue,
                                                    @RequestParam LocalDate revaluationDate) {
        try {
            FixedAsset revalued = depreciationService.revalueAsset(id, newValue, revaluationDate);
            return ResponseEntity.ok(revalued);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Category endpoints

    @GetMapping("/categories")
    public List<FixedAssetCategory> getAllCategories() {
        return fixedAssetRepository.findAllCategories();
    }

    @PostMapping("/categories")
    public ResponseEntity<FixedAssetCategory> createCategory(@RequestBody FixedAssetCategory category) {
        if (category.getName() == null || category.getDepreciationRate() == null) {
            return ResponseEntity.badRequest().build();
        }
        FixedAssetCategory saved = fixedAssetRepository.saveCategory(category);
        return ResponseEntity.ok(saved);
    }

    // Depreciation endpoints

    @GetMapping("/{id}/depreciation")
    public List<DepreciationRecord> getDepreciationRecords(@PathVariable Long id) {
        return depreciationRecordRepository.findByAssetId(id);
    }

    @PostMapping("/{id}/depreciate")
    public ResponseEntity<DepreciationRecord> calculateDepreciation(@PathVariable Long id,
                                                                     @RequestParam LocalDate periodEnd) {
        try {
            DepreciationRecord record = depreciationService.calculateDepreciation(id, periodEnd);
            return ResponseEntity.ok(record);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/batch-depreciate")
    public ResponseEntity<List<DepreciationRecord>> batchDepreciate(@RequestParam LocalDate periodEnd) {
        List<DepreciationRecord> records = depreciationService.batchCalculateDepreciation(periodEnd);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/report/depreciation-schedule")
    public ResponseEntity<List<Object>> getDepreciationSchedule(@RequestParam Long assetId) {
        List<Object> schedule = depreciationService.generateDepreciationSchedule(assetId);
        return ResponseEntity.ok(schedule);
    }
}
