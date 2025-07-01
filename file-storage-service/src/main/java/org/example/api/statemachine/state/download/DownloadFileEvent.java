package org.example.api.statemachine.state.download;

public enum DownloadFileEvent {
    PREPARE,
    DECRYPT,
    DECOMPRESS,
    DELIVER
}
