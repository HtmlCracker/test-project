package org.example.api.statemachine.state.upload;

public enum UploadFileState {
    UPLOADED,
    COMPRESSED,
    ENCRYPTED,
    STORED;

    private static final UploadFileState[] VALUES = values();

    public UploadFileState next() {
        if (this == STORED) {
            return null;
        }
        return VALUES[this.ordinal() + 1];
    }
}
