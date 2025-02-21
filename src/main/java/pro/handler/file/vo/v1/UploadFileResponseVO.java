package pro.handler.file.vo.v1;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class UploadFileResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;

    private String fileName;
    private String fileDownloadUri;
    private String fileTargetLocation;
    private String fileType;
    private Long fileSize;

    public UploadFileResponseVO() {
    }

    public UploadFileResponseVO(String fileName, String fileDownloadUri, String fileTargetLocation, String fileType, Long fileSize) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileTargetLocation = fileTargetLocation;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

}
