package com.github.saydov.documents.enums;

public enum DocumentStatus {

    DRAFT(null) {
        @Override
        public DocumentStatus next() {
            return SUBMITTED;
        }
    },

    SUBMITTED(DocumentAction.SUBMIT) {
        @Override
        public DocumentStatus next() {
            return APPROVED;
        }
    },

    APPROVED(DocumentAction.APPROVE) {
        @Override
        public DocumentStatus next() {
            return null;
        }
    };

    private final DocumentAction action;

    DocumentStatus(DocumentAction action) {
        this.action = action;
    }

    public DocumentAction getAction() {
        return action;
    }

    public abstract DocumentStatus next();

    public boolean canTransitionTo(DocumentStatus target) {
        return next() == target;
    }
}
