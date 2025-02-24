package pro.handler.file.services;


import pro.handler.file.config.FileStorageConfig;
import pro.handler.file.exceptions.FileStorageException;
import pro.handler.file.exceptions.MyFileNotFoundException;
import pro.handler.file.vo.v1.UploadFileResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FileStorageService {
    private final Path storageBaseLocation;
    private final Path UPLOAD_STANDARD_PATH;
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.storageBaseLocation = Path.of(fileStorageConfig.getUploadDir());
        this.UPLOAD_STANDARD_PATH = Path.of(fileStorageConfig.getUploadStandardDir());

        this.fileStorageLocation = storageBaseLocation
                .resolve(UPLOAD_STANDARD_PATH)
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

        String fileName = this.storeFile(file,subDirs);

        String downloadPath = (subDirs == null || subDirs.length == 0)?
                UPLOAD_STANDARD_PATH.resolve(fileName).toString():
                UPLOAD_STANDARD_PATH.resolve(Paths.get("",subDirs)).resolve(fileName).toString();

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(downloadPath)
                .toUriString();
        return new UploadFileResponseVO(fileName, fileDownloadUri, downloadPath, file.getContentType(), file.getSize());
    }

    public List<UploadFileResponseVO> uploadMultipleFiles(MultipartFile[] files, String... subDirs){
        return Arrays.stream(files)
                .map(file -> uploadFile(file,subDirs))
                .collect(Collectors.toList());
    }

    public String deleteFile(String fileName, String fileTargetLocation) {

        try {
            Path filePath = storageBaseLocation.resolve(fileTargetLocation);
            Files.deleteIfExists(filePath);
            return "File " + fileName + " deleted successfully.";
        } catch (IOException e) {
            throw new MyFileNotFoundException("File " + fileName + "not found. ");
        }
    }

    public List<String> deleteMultipleFiles(UploadFileResponseVO[] files){
        return Arrays.stream(files)
                .map(file -> deleteFile(file.getFileName(),file.getFileTargetLocation()))
                .collect(Collectors.toList());
    }
}
