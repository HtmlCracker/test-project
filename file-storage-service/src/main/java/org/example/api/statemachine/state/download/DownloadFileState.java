package org.example.api.statemachine.state.download;

public enum DownloadFileState {
    STORED,
    PREPARED,
    DECRYPTED,
    DECOMPRESSED,
    READY
}
