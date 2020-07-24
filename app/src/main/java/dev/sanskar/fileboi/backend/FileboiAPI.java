package dev.sanskar.fileboi.backend;

public class FileboiAPI {

    public static final String SERVICE_BASE_URL = "https://fileboi-rwac7a5nuq-de.a.run.app/fileboi";
    public static final String FILES_URL = SERVICE_BASE_URL + "/files";

    public static String getFileUploadURL(String fileId) {
        return FILES_URL + "/" + fileId + "/upload_url";
    }

    public static String getFileDownloadURL(String fileId) {
        return FILES_URL + "/" + fileId + "/download_url";
    }

}