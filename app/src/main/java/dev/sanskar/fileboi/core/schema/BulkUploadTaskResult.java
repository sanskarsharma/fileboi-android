package dev.sanskar.fileboi.core.schema;

import java.util.List;

public class BulkUploadTaskResult {
    private int notificationId ;
    private List<String> fileNames ;
    private List<String> successfulUploads ;
    private List<String> failedUploads ;

    public List<String> getSuccessfulUploads() {
        return successfulUploads;
    }

    public void setSuccessfulUploads(List<String> successfulUploads) {
        this.successfulUploads = successfulUploads;
    }

    public List<String> getFailedUploads() {
        return failedUploads;
    }

    public void setFailedUploads(List<String> failedUploads) {
        this.failedUploads = failedUploads;
    }

    private boolean success ;

    public BulkUploadTaskResult(int notificationId, List<String> fileNames, boolean success) {
        this.notificationId = notificationId;
        this.fileNames = fileNames;
        this.success = success;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
