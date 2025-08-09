package org.example.api.services;

import org.example.api.entities.FileInfoEntity;
import org.example.api.repositories.FileInfoRepository;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResortingStateServiceTest {
    @InjectMocks
    ResortingStateService resortingStateService;

    @Mock
    FileInfoRepository fileInfoRepository;

    @Mock
    FileStateMachineService fileStateMachineService;

    @Test
    void resortStates_shouldProcessAllEntities() {
        UUID id1 = UUID.fromString("b1c9e1b7-98a1-4be4-86ba-bab9ac6f3135");
        UUID id2 = UUID.randomUUID();

        FileInfoEntity entity1 = FileInfoEntity.builder()
                .id(id1)
                .fileState(UploadFileState.UPLOADED)
                .build();

        FileInfoEntity entity2 = FileInfoEntity.builder()
                .id(id2)
                .fileState(UploadFileState.COMPRESSED)
                .build();

        List<FileInfoEntity> testEntities = List.of(entity1, entity2);

        when(fileInfoRepository.findByFileStateIn(anyCollection()))
                .thenReturn(testEntities);

        resortingStateService.resortStates();

        verify(fileStateMachineService).resortingUploadState(id1.toString(), UploadFileState.UPLOADED);
        verify(fileStateMachineService).resortingUploadState(id2.toString(), UploadFileState.COMPRESSED);
    }
}
