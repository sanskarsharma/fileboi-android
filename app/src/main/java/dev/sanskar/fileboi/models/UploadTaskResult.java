package dev.sanskar.fileboi.models;

public class UploadTaskResult {

    private int notificationId ;
    private String objectName ;
    private boolean success ;

    public UploadTaskResult(int notificationId, String objectName, boolean success) {
        this.notificationId = notificationId;
        this.objectName = objectName;
        this.success = success;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
