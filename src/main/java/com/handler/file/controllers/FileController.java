package com.handler.file.controllers;


import com.handler.file.services.FileStorageService;
import com.handler.file.vo.v1.UploadFileResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/file/v1")
public class FileController {
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponseVO uploadFile(@RequestParam("file")MultipartFile file){
        return fileStorageService.uploadFile(file,"tech","item1");
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponseVO> uploadMultipleFiles(@RequestParam("files")MultipartFile[] files){
        return fileStorageService.uploadMultipleFiles(files,"tech","item1");
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestBody UploadFileResponseVO file) {

        return fileStorageService.deleteFile(StringUtils.cleanPath(file.getFileName()),file.getFileTargetLocation());
    }

    @DeleteMapping("/deleteMultipleFile")
    public List<String> deleteMultipleFile(@RequestBody UploadFileResponseVO[] files) {

        return fileStorageService.deleteMultipleFiles(files);
    }
}
