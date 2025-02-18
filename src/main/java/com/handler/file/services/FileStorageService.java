package com.handler.file.services;


import com.handler.file.config.FileStorageConfig;
import com.handler.file.exceptions.FileStorageException;
import com.handler.file.vo.v1.UploadFileResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath()
                .normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new FileStorageException("Could not create the directory where the uploaded files",e);
        }
    }

    public String storeFile(MultipartFile file, String... subDirs){
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            if (fileName.contains("..")){
                throw new FileStorageException("Invalid name");
            }
            Path targetPath = (subDirs == null || subDirs.length == 0)?
                    this.fileStorageLocation :
                    this.fileStorageLocation.resolve(Paths.get("",subDirs));
            Files.createDirectories(targetPath);

            Path targetLocation = targetPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            throw new FileStorageException("Could not store file" + fileName, e);
        }
    }

    public UploadFileResponseVO uploadFile(MultipartFile file, String... subDirs){
        Path standardPath = Path.of("/files/");

        String fileName = this.storeFile(file,subDirs);

        String downloadPath = (subDirs == null || subDirs.length == 0)?
                standardPath.resolve(fileName).toString():
                standardPath.resolve(Paths.get("",subDirs)).resolve(fileName).toString();

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(downloadPath)
                .toUriString();
        return new UploadFileResponseVO(fileName, fileDownloadUri,file.getContentType(), file.getSize());
    }
}
