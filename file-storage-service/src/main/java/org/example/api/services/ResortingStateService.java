package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.entities.FileInfoEntity;
import org.example.api.repositories.FileInfoRepository;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResortingStateService {
    FileInfoRepository fileInfoRepository;
    FileStateMachineService fileStateMachineService;

    public void resortStates() {
        List<FileInfoEntity> entities = getEntitiesWitResortState();
        System.out.println("SIZE: " + entities.size());
        for (FileInfoEntity entity : entities) {
            fileStateMachineService.resortingUploadState(
                    entity.getId().toString(), entity.getFileState()
            );
        }
    }

    private List<FileInfoEntity> getEntitiesWitResortState() {
        List<FileInfoEntity> entities = fileInfoRepository.findByFileStateIn(
                List.of(UploadFileState.UPLOADED,
                        UploadFileState.COMPRESSED,
                        UploadFileState.ENCRYPTED)
        );
        System.out.println("Monkey: " + entities.size());
        return entities;
    }
}
