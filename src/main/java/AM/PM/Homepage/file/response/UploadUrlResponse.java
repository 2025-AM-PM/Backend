package AM.PM.Homepage.file.response;

public record UploadUrlResponse(
        String fileId,
        String presignedUrl
) {
}