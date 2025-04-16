package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.statemachine.enums.FileEvent;
import org.example.api.statemachine.enums.FileState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileProcessorService {
    StateMachineFactory<FileState, FileEvent> stateMachineFactory;

    public void processFile(String fileId) {
        StateMachine<FileState, FileEvent> stateMachine = stateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("fileId", fileId);

        stateMachine.start();
        stateMachine.sendEvent(FileEvent.COMPRESS);
        stateMachine.sendEvent(FileEvent.ENCRYPT);
        stateMachine.sendEvent(FileEvent.STORE);
        stateMachine.stop();
    }
}
