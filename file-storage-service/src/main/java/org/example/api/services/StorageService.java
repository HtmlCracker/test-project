package org.example.api.services;


import org.apache.commons.io.FilenameUtils;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileEntity;
import org.example.api.entities.FolderEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.factories.UploadFileResponseDtoFactory;
import org.example.api.repositories.FileRepository;
import org.example.api.repositories.FolderRepository;
import org.example.api.utils.compression.FileCompressor;
import org.example.api.utils.FileUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StorageService {
    FolderRepository folderRepository;
    FileRepository fileRepository;

    FolderService folderService;
    UploadFileResponseDtoFactory uploadFileResponseDtoFactory;

    FileCompressor fileCompressUtils = new FileCompressor();
    FileUtils fileUtils = FileUtils.of("/app/storage-files");

    /*public UploadFileResponseDto uploadFile(MultipartFile file, boolean isCompressed) {
        if (file.isEmpty())
            throw new BadRequestException("File can't be empty.");

        FolderEntity folderEntity = folderService.getLeastFilledFolder();

        FileEntity fileEntity = makeFileEntity(file, folderEntity);

        // TODO: Протестить
        if (!isCompressed) {
            byte[] dataBytes = fileCompressUtils.compressMultipartFile(file);
            long compressedFileSizeByte = dataBytes.length;
            fileEntity.setCompressedFileSizeByte(compressedFileSizeByte);
            loadFileInByteToStorage(fileEntity, dataBytes, Path.of(folderEntity.getPath()));
        } else {
            loadMultipartFileToStorage(fileEntity, file, Path.of(folderEntity.getPath()));
        }

        FileEntity savedFileEntity = saveFileEntity(fileEntity);
        FolderEntity updatedFolder = saveFolderEntity(bindFileToFolder(folderEntity, savedFileEntity));

        return uploadFileResponseDtoFactory.makeUploadFileResponseDto(savedFileEntity);
    }*/

    private FileEntity makeFileEntity(MultipartFile file, FolderEntity folderEntity) {
        String filename = UUID.randomUUID().toString();
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        return FileEntity.builder()
                .fileName(String.format("%s.%s", filename, extension))
                .originalFileName(file.getOriginalFilename())
                .compressedFileSizeByte(file.getSize())
                .originalFileSizeByte(file.getSize())
                .path(Paths.get(folderEntity.getPath(), filename).toString())
                .folder(folderEntity)
                .build();
    }

    private FileEntity saveFileEntity(FileEntity entity) {
        return fileRepository.save(entity);
    }

    private void deleteFileEntity(FileEntity entity) {
        fileRepository.delete(entity);
    }

    private FolderEntity bindFileToFolder(FolderEntity folder, FileEntity fileEntity) {
        int fileCount = folder.getFileCount()+1;
        folder.setFileCount(fileCount);

        Long usedStorageByte = folder.getUsedStorageByte() + fileEntity.getCompressedFileSizeByte();
        folder.setUsedStorageByte(usedStorageByte);

        folder.getFiles().add(fileEntity);

        return folder;
    }

    private FolderEntity saveFolderEntity(FolderEntity entity) {
        return folderRepository.save(entity);
    }

    private void loadFileInByteToStorage(FileEntity fileEntity, byte[] fileInByte, Path path) {
        String fileName = fileEntity.getFileName() + ".gz";
        fileUtils.saveFileInByteToPath(path, fileName, fileInByte);

        //Path compressedFilePath = Paths.get(path.toString(), fileName);
        //fileCompressUtils.testDecompressFile(compressedFilePath);
    }

    private void loadMultipartFileToStorage(FileEntity fileEntity, MultipartFile file, Path path) {
        String fileName = fileEntity.getFileName();

        fileUtils.saveMultipartFileToPath(path, fileName, file);
    }
//
//
//    public GetFileResponseDto getFile(UUID fileId) {
//        FileInfoEntity fileInfoEntity = fileInfoRepository.findById(fileId)
//                .orElseThrow(() -> new NotFoundException(String.format("File with id \"%s\" does not exist", fileId)));
//
//        String path = fileInfoEntity.getStorage().getId().toString();
//        String fileName = fileInfoEntity.getId().toString();
//
//        byte[] byteData = fileUtils.getFileByPath(path, fileName);
//
//        return GetFileResponseDto.builder()
//                .byteData(byteData)
//                .fileName(fileInfoEntity.getOriginalName())
//                .fileSize(fileInfoEntity.getFileSizeByte())
//                .build();
//    }
}
