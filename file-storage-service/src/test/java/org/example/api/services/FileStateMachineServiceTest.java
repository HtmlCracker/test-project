package org.example.api.services;

import org.example.api.statemachine.state.download.DownloadFileEvent;
import org.example.api.statemachine.state.download.DownloadFileState;
import org.example.api.statemachine.state.upload.UploadFileEvent;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultExtendedState;

import java.util.HashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStateMachineServiceTest {
    @InjectMocks
    FileStateMachineService fileStateMachineService;

    @Mock
    private StateMachineFactory<UploadFileState, UploadFileEvent> uploadStateMachineFactory;

    @Mock
    private StateMachine<UploadFileState, UploadFileEvent> uploadStateMachine;

    @Mock
    StateMachineFactory<DownloadFileState, DownloadFileEvent> downloadStateMachineFactory;

    @Mock
    private StateMachine<DownloadFileState, DownloadFileEvent> downloadStateMachine;

    @Mock
    private StateMachineAccessor<UploadFileState, UploadFileEvent> accessor;

    @Mock
    StateMachineAccess<UploadFileState, UploadFileEvent> stateMachineAccess;

    @Test
    void resortingUploadState_shouldProcessFromStoredToStored() {
        when(uploadStateMachineFactory.getStateMachine()).thenReturn(uploadStateMachine);
        ExtendedState extendedState = new DefaultExtendedState(new HashMap<>());
        when(uploadStateMachine.getExtendedState()).thenReturn(extendedState);
        when(uploadStateMachine.getStateMachineAccessor()).thenReturn(accessor);

        doAnswer(invocation -> {
            Consumer<StateMachineAccess<UploadFileState, UploadFileEvent>> action = invocation.getArgument(0);
            action.accept(stateMachineAccess);
            return null;
        }).when(accessor).doWithAllRegions(any());

        fileStateMachineService.resortingUploadState("file123", UploadFileState.ENCRYPTED);

        InOrder inOrder = inOrder(uploadStateMachine);
        inOrder.verify(uploadStateMachine).start();
        inOrder.verify(uploadStateMachine).stop();

        assertEquals("file123", extendedState.getVariables().get("fileId"));
    }

    @Test
    void resortingUploadState_shouldProcessFromEncryptedToStored() {
        when(uploadStateMachineFactory.getStateMachine()).thenReturn(uploadStateMachine);
        ExtendedState extendedState = new DefaultExtendedState(new HashMap<>());
        when(uploadStateMachine.getExtendedState()).thenReturn(extendedState);
        when(uploadStateMachine.getStateMachineAccessor()).thenReturn(accessor);

        doAnswer(invocation -> {
            Consumer<StateMachineAccess<UploadFileState, UploadFileEvent>> action = invocation.getArgument(0);
            action.accept(stateMachineAccess);
            return null;
        }).when(accessor).doWithAllRegions(any());

        when(uploadStateMachine.sendEvent(UploadFileEvent.STORE)).thenReturn(true);

        fileStateMachineService.resortingUploadState("file123", UploadFileState.ENCRYPTED);

        InOrder inOrder = inOrder(uploadStateMachine);
        inOrder.verify(uploadStateMachine).start();
        inOrder.verify(uploadStateMachine).sendEvent(UploadFileEvent.STORE);
        inOrder.verify(uploadStateMachine).stop();

        assertEquals("file123", extendedState.getVariables().get("fileId"));
    }

    @Test
    void resortingUploadState_shouldProcessFromCompressedToStored() {
        when(uploadStateMachineFactory.getStateMachine()).thenReturn(uploadStateMachine);
        ExtendedState extendedState = new DefaultExtendedState(new HashMap<>());
        when(uploadStateMachine.getExtendedState()).thenReturn(extendedState);
        when(uploadStateMachine.getStateMachineAccessor()).thenReturn(accessor);

        doAnswer(invocation -> {
            Consumer<StateMachineAccess<UploadFileState, UploadFileEvent>> action = invocation.getArgument(0);
            action.accept(stateMachineAccess);
            return null;
        }).when(accessor).doWithAllRegions(any());

        when(uploadStateMachine.sendEvent(UploadFileEvent.ENCRYPT)).thenReturn(true);
        when(uploadStateMachine.sendEvent(UploadFileEvent.STORE)).thenReturn(true);

        fileStateMachineService.resortingUploadState("file123", UploadFileState.COMPRESSED);

        InOrder inOrder = inOrder(uploadStateMachine);
        inOrder.verify(uploadStateMachine).start();
        inOrder.verify(uploadStateMachine).sendEvent(UploadFileEvent.ENCRYPT);
        inOrder.verify(uploadStateMachine).sendEvent(UploadFileEvent.STORE);
        inOrder.verify(uploadStateMachine).stop();

        assertEquals("file123", extendedState.getVariables().get("fileId"));
    }

    @Test
    void resortingUploadState_shouldProcessFromUploadedToStored() {
        when(uploadStateMachineFactory.getStateMachine()).thenReturn(uploadStateMachine);
        ExtendedState extendedState = new DefaultExtendedState(new HashMap<>());
        when(uploadStateMachine.getExtendedState()).thenReturn(extendedState);
        when(uploadStateMachine.getStateMachineAccessor()).thenReturn(accessor);

        doAnswer(invocation -> {
            Consumer<StateMachineAccess<UploadFileState, UploadFileEvent>> action = invocation.getArgument(0);
            action.accept(stateMachineAccess);
            return null;
        }).when(accessor).doWithAllRegions(any());

        when(uploadStateMachine.sendEvent(UploadFileEvent.COMPRESS)).thenReturn(true);
        when(uploadStateMachine.sendEvent(UploadFileEvent.ENCRYPT)).thenReturn(true);
        when(uploadStateMachine.sendEvent(UploadFileEvent.STORE)).thenReturn(true);

        fileStateMachineService.resortingUploadState("file123", UploadFileState.UPLOADED);

        InOrder inOrder = inOrder(uploadStateMachine);
        inOrder.verify(uploadStateMachine).start();
        inOrder.verify(uploadStateMachine).sendEvent(UploadFileEvent.COMPRESS);
        inOrder.verify(uploadStateMachine).sendEvent(UploadFileEvent.ENCRYPT);
        inOrder.verify(uploadStateMachine).sendEvent(UploadFileEvent.STORE);
        inOrder.verify(uploadStateMachine).stop();

        assertEquals("file123", extendedState.getVariables().get("fileId"));
    }

    @Test
    void resortingUploadState_shouldSkipIfAlreadyStored() {
        String fileId = "storedFileId";
        UploadFileState currentState = UploadFileState.STORED;

        ExtendedState extendedState = new DefaultExtendedState(new HashMap<>());

        when(uploadStateMachineFactory.getStateMachine()).thenReturn(uploadStateMachine);
        when(uploadStateMachine.getExtendedState()).thenReturn(extendedState);

        fileStateMachineService.resortingUploadState(fileId, currentState);

        verify(uploadStateMachine).start();
        verify(uploadStateMachine).stop();
        verify(uploadStateMachine, never()).sendEvent(any(UploadFileEvent.class));
    }


    @Test
    void uploadFile_shouldSendEventsInOrder() {
        ExtendedState extendedState = new DefaultExtendedState(new HashMap<>());
        when(uploadStateMachine.getExtendedState()).thenReturn(extendedState);
        when(uploadStateMachineFactory.getStateMachine()).thenReturn(uploadStateMachine);
        when(uploadStateMachine.sendEvent(any(UploadFileEvent.class))).thenReturn(true);

        fileStateMachineService.uploadFile("testFileId");

        InOrder inOrder = inOrder(uploadStateMachine);
        inOrder.verify(uploadStateMachine).start();
        inOrder.verify(uploadStateMachine, times(3)).sendEvent(any(UploadFileEvent.class));
        inOrder.verify(uploadStateMachine).stop();
    }

    @Test
    void downloadFile_shouldSendEventsInOrder() {
        String path = "test/path.path";
        String encryptionKey = "test";
        ExtendedState extendedState = new DefaultExtendedState(new HashMap<>());

        when(downloadStateMachineFactory.getStateMachine()).thenReturn(downloadStateMachine);
        when(downloadStateMachine.getExtendedState()).thenReturn(extendedState);

        fileStateMachineService.getFile(path, encryptionKey);

        verify(downloadStateMachine).start();
        verify(downloadStateMachine, atLeastOnce()).getExtendedState();
    }
}