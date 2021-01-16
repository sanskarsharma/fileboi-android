package dev.sanskar.fileboi.core.services;

public class FileboiAPI {

    public static final String SERVICE_BASE_URL = "https://fileboi-rwac7a5nuq-de.a.run.app/fileboi";
    public static final String FILES_URL = SERVICE_BASE_URL + "/files";

    // FIXME : remove these upload/download URL methods after refactoring api code. also remove this class itself after refactoring to use retrofit services
    public static String getFileUploadURL(String fileId) {
        return FILES_URL + "/" + fileId + "/upload_url";
    }

    public static String getFileDownloadURL(String fileId) {
        return FILES_URL + "/" + fileId + "/download_url";
    }

    public static String getFileResourceURL(String fileId) {
        return FILES_URL + "/" + fileId;
    }

}
