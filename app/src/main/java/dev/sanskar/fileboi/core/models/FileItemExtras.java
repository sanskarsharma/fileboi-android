package dev.sanskar.fileboi.core.models;

public class FileItemExtras {

    private String thumbnailUrl;

    public FileItemExtras(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
