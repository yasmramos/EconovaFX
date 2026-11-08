package com.econovafx.modules.accounting.repository;

import com.econovafx.modules.accounting.model.DocumentSequence;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository for the persistent consecutive-number counters.
 */
@Component
public class DocumentSequenceRepository {

    private static final Logger logger = LoggerFactory.getLogger(DocumentSequenceRepository.class);

    private final Database database;

    @Inject
    public DocumentSequenceRepository(Database database) {
        this.database = database;
    }

    public DocumentSequence findByKey(String sequenceKey) {
        return database.find(DocumentSequence.class)
                .where().eq("sequenceKey", sequenceKey)
                .findOne();
    }

    public DocumentSequence save(DocumentSequence sequence) {
        database.save(sequence);
        logger.debug("Document sequence saved: {} = {}", sequence.getSequenceKey(), sequence.getLastValue());
        return sequence;
    }

    public void update(DocumentSequence sequence) {
        database.update(sequence);
        logger.debug("Document sequence updated: {} = {}", sequence.getSequenceKey(), sequence.getLastValue());
    }
}
