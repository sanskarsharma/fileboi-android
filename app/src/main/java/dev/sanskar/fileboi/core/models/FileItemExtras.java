package dev.sanskar.fileboi.core.models;

public class FileItemExtras {

    private String thumbnailUrl;
    private String downloadUrl;
    private String uploadUrl;

    public FileItemExtras(String thumbnailUrl, String downloadUrl, String uploadUrl) {
        this.thumbnailUrl = thumbnailUrl;
        this.downloadUrl = downloadUrl;
        this.uploadUrl = uploadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
