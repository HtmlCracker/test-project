package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.statemachine.state.download.DownloadFileEvent;
import org.example.api.statemachine.state.download.DownloadFileState;
import org.example.api.statemachine.state.upload.UploadFileEvent;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileStateMachineService {
    @Autowired
    StateMachineFactory<UploadFileState, UploadFileEvent> uploadStateMachineFactory;
    @Autowired
    StateMachineFactory<DownloadFileState, DownloadFileEvent> downloadStateMachineFactory;

    public void resortingUploadState(String fileId, UploadFileState currentState) {
        StateMachine<UploadFileState, UploadFileEvent> stateMachine =
                uploadStateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("fileId", fileId);
        stateMachine.start();

        try {
            if (currentState == UploadFileState.STORED) {
                return;
            }

            stateMachine.getStateMachineAccessor()
                    .doWithAllRegions(access -> {
                        ExtendedState extendedState = new DefaultExtendedState();
                        extendedState.getVariables().put("fileId", fileId);

                        access.resetStateMachine(new DefaultStateMachineContext<>(
                                currentState,
                                null,
                                null,
                                extendedState
                        ));
                    });

            UploadFileState state = currentState;
            while (state != null && state != UploadFileState.STORED) {
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
        stateMachine.sendEvent(UploadFileEvent.COMPRESS);
        stateMachine.sendEvent(UploadFileEvent.ENCRYPT);
        stateMachine.sendEvent(UploadFileEvent.STORE);
        stateMachine.stop();
    }

    public String getFile(String filePath) {
        StateMachine<DownloadFileState, DownloadFileEvent> stateMachine =
                downloadStateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("filePath", filePath);

        stateMachine.start();

        return (String) stateMachine.getExtendedState().getVariables().get("filePath");
    }
}
