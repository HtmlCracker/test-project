package org.example.api.statemachine.state.upload;

public enum UploadFileEvent {
    COMPRESS,
    ENCRYPT,
    STORE,
    RETRY
}
