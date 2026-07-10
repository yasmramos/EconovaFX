package com.econovafx.modules.fixedassets.repository;

import com.econovafx.modules.fixedassets.model.FixedAsset;
import com.econovafx.modules.fixedassets.model.FixedAsset.AssetStatus;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FixedAsset entities.
 * Maneja operaciones de acceso a datos para activos fijos.
 */
@Component
public class FixedAssetRepository {

    private static final Logger logger = LoggerFactory.getLogger(FixedAssetRepository.class);

    private final Database database;

    @Inject
    public FixedAssetRepository(Database database) {
        this.database = database;
    }

    public Optional<FixedAsset> findById(Long id) {
        return Optional.ofNullable(database.find(FixedAsset.class, id));
    }

    public Optional<FixedAsset> findByCode(String code) {
        return Optional.ofNullable(database.find(FixedAsset.class)
                .where().eq("code", code).findOne());
    }

    public List<FixedAsset> findAll() {
        return database.find(FixedAsset.class)
                .orderBy().asc("code").findList();
    }

    public List<FixedAsset> findByStatus(AssetStatus status) {
        return database.find(FixedAsset.class)
                .where().eq("status", status)
                .orderBy().asc("code").findList();
    }

    public List<FixedAsset> findByCategoryId(Long categoryId) {
        return database.find(FixedAsset.class)
                .where().eq("category.id", categoryId)
                .orderBy().asc("code").findList();
    }

    public List<FixedAsset> findActiveAssets() {
        return database.find(FixedAsset.class)
                .where().eq("status", AssetStatus.ACTIVE)
                .orderBy().asc("code").findList();
    }

    public FixedAsset save(FixedAsset asset) {
        database.save(asset);
        logger.debug("FixedAsset saved: {}", asset.getCode());
        return asset;
    }

    public void update(FixedAsset asset) {
        database.update(asset);
        logger.debug("FixedAsset updated: {}", asset.getCode());
    }

    public void delete(FixedAsset asset) {
        database.delete(asset);
        logger.debug("FixedAsset deleted: {}", asset.getCode());
    }

    public void deleteById(Long id) {
        database.delete(FixedAsset.class, id);
        logger.debug("FixedAsset deleted by ID: {}", id);
    }

    public boolean existsByCode(String code) {
        return database.find(FixedAsset.class)
                .where().eq("code", code).exists();
    }

    public boolean existsByCodeExclude(String code, Long excludeId) {
        var query = database.find(FixedAsset.class)
                .where().eq("code", code);
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        return query.exists();
    }

    public boolean existsById(Long id) {
        return database.find(FixedAsset.class, id) != null;
    }

    public long count() {
        return database.find(FixedAsset.class).findCount();
    }

    public long countByStatus(AssetStatus status) {
        return database.find(FixedAsset.class)
                .where().eq("status", status).findCount();
    }

    public List<FixedAsset> searchByName(String searchTerm) {
        return database.find(FixedAsset.class)
                .where().ilike("name", "%" + searchTerm + "%")
                .orderBy().asc("code").findList();
    }

    public List<FixedAsset> findAssetsForDepreciation() {
        return database.find(FixedAsset.class)
                .where()
                .eq("status", AssetStatus.ACTIVE)
                .gt("acquisitionCost", 0)
                .findList();
    }
}
