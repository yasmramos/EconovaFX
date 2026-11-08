package com.econovafx.modules.accounting.model;

import com.econovafx.modules.core.model.BaseEntity;
import jakarta.persistence.*;

/**
 * Persistent, monotonic counter used to hand out consecutive document numbers.
 * Resolution 340/2004 forbids reusing or skipping numbers, so the counter is
 * stored independently of the documents themselves: deleting a voucher must not
 * free its number for reuse.
 */
@Entity
@Table(name = "document_sequences")
public class DocumentSequence extends BaseEntity {

    @Column(name = "sequence_key", nullable = false, unique = true, length = 64)
    private String sequenceKey;

    @Column(name = "last_value", nullable = false, columnDefinition = "BIGINT")
    private Long lastValue = 0L;

    public DocumentSequence() {
    }

    public DocumentSequence(String sequenceKey, Long lastValue) {
        this.sequenceKey = sequenceKey;
        this.lastValue = lastValue;
    }

    public String getSequenceKey() {
        return sequenceKey;
    }

    public void setSequenceKey(String sequenceKey) {
        this.sequenceKey = sequenceKey;
    }

    public Long getLastValue() {
        return lastValue;
    }

    public void setLastValue(Long lastValue) {
        this.lastValue = lastValue;
    }
}
