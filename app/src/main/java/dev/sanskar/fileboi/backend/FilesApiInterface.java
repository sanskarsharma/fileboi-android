package dev.sanskar.fileboi.backend;

import java.util.List;

import dev.sanskar.fileboi.core.models.FileEntry;
import dev.sanskar.fileboi.core.schema.FileURLResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FilesApiInterface {

    public static final String SERVICE_BASE_URL = "https://fileboi-rwac7a5nuq-de.a.run.app/fileboi/";

    @POST("files")
    Call<FileEntry> createFile(@Header("Authorization") String authHeader, @Body FileEntry file);

    @GET("files")
    Call<List<FileEntry>> getFiles(@Header("Authorization") String authHeader);

    @GET("files/{file_id}/upload_url")
    Call<FileURLResponse> getFileUploadUrl(@Header("Authorization") String authHeader, @Path("file_id") String fileId);

    @GET("files/{file_id}/download_url")
    Call<FileURLResponse> getFileDownloadUrl(@Header("Authorization") String authHeader, @Path("file_id") String fileId);

    @DELETE("files/{file_id}")
    Call<FileEntry> deleteFile(@Header("Authorization") String authHeader, @Path("file_id") String fileId);
}
