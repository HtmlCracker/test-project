package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.statemachine.state.download.DownloadFileEvent;
import org.example.api.statemachine.state.download.DownloadFileState;
import org.example.api.statemachine.state.upload.UploadFileEvent;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStateMachineService {
    StateMachineFactory<UploadFileState, UploadFileEvent> uploadStateMachineFactory;
    StateMachineFactory<DownloadFileState, DownloadFileEvent> downloadStateMachineFactory;

    public void resortingUploadState(String fileId, UploadFileState currentState) {
        StateMachine<UploadFileState, UploadFileEvent> stateMachine =
                uploadStateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("fileId", fileId);
        stateMachine.start();

        try {
            UploadFileState state = currentState;
            while (state != null && state.isBefore(UploadFileState.STORED)) {
                UploadFileEvent event = getEventForState(state);
                stateMachine.sendEvent(event);
                state = state.next();
            }
        } finally {
            stateMachine.stop();
        }
    }

    private UploadFileEvent getEventForState(UploadFileState state) {
        switch (state) {
            case UPLOADED: return UploadFileEvent.COMPRESS;
            case COMPRESSED: return UploadFileEvent.ENCRYPT;
            case ENCRYPTED: return UploadFileEvent.STORE;
            default: throw new IllegalStateException();
        }
    }

    public void uploadFile(String fileId) {
        StateMachine<UploadFileState, UploadFileEvent> stateMachine =
                uploadStateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("fileId", fileId);

        stateMachine.start();
    }

    public String getFile(String filePath) {
        StateMachine<DownloadFileState, DownloadFileEvent> stateMachine =
                downloadStateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("filePath", filePath);

        stateMachine.start();

        return (String) stateMachine.getExtendedState().getVariables().get("filePath");
    }
}
