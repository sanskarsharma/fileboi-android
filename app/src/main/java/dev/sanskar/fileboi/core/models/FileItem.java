package dev.sanskar.fileboi.core.models;

public class FileItem {
    private String id;
    private String name;
    private String createdAt;
    private FileMetadata fileMetadata;
    private FileItemExtras extras;

    public FileItemExtras getExtras() {
        return extras;
    }

    public void setExtras(FileItemExtras extras) {
        this.extras = extras;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", fileMetadata=" + fileMetadata +
                ", extras=" + extras +
                '}';
    }
}
