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
        stateMachine.sendEvent(DownloadFileEvent.PREPARE);
        stateMachine.sendEvent(DownloadFileEvent.DECRYPT);
        stateMachine.sendEvent(DownloadFileEvent.DECOMPRESS);
        stateMachine.sendEvent(DownloadFileEvent.DELIVER);
        stateMachine.stop();

        return (String) stateMachine.getExtendedState().getVariables().get("filePath");
    }
}
