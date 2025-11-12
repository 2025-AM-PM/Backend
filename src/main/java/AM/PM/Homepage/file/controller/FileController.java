package AM.PM.Homepage.file.controller;

import AM.PM.Homepage.file.response.DownloadUrlResponse;
import AM.PM.Homepage.file.response.UploadUrlResponse;
import AM.PM.Homepage.file.util.SignatureGenerator;
import AM.PM.Homepage.notice.response.NoticeDetailResponse;
import AM.PM.Homepage.security.UserAuth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final SignatureGenerator signatureGenerator;
    private final String storageServiceBaseUrl = "http://localhost:6736";

    /**
     * 프론트엔드에서 파일 업로드를 위한 Presigned URL을 생성하여 반환합니다.
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadUrlResponse> makePreSignedUrl(
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        String httpMethod = "PUT";
        String fileId = UUID.randomUUID().toString();
        String userId = userAuth.getId().toString();

        // 1. (수정) 이 템플릿을 사용합니다.
        String filePathTemplate = "/images/{userId}/posts/{fileId}/image.png";

        long expires = (System.currentTimeMillis() / 1000) + 600; // 10분
        String signature = signatureGenerator.generateSignature(httpMethod, fileId, expires);

        String presignedUrl = UriComponentsBuilder
                .fromHttpUrl(storageServiceBaseUrl) // StorageService 주소

                // 2. (수정) path()에 기본 경로와 템플릿을 합칩니다.
                // 최종 경로는 "/exhibits/images/{userId}/posts/{fileId}/image.png"가 됩니다.
                .path("/storage/exhibits" + filePathTemplate)

                .queryParam("expires", expires)
                .queryParam("signature", signature)

                // 3. (수정) path에 사용된 {userId}, {fileId} 2개 변수를 순서대로 전달합니다.
                .buildAndExpand(userId, fileId)
                .toUriString();

        UploadUrlResponse responseDto = new UploadUrlResponse(fileId, presignedUrl);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/download")
    public ResponseEntity<DownloadUrlResponse> makeDownloadUrl(
            // React CustomImageRenderer가 fileId 파라미터에 partialPath를 담아 보냅니다.
            @RequestParam("fileId") String partialPath,
            @AuthenticationPrincipal UserAuth userAuth // 사용자 ID를 가져오기 위해 추가
    ) {
        // 1. (수정) partialPath에서 uuid 추출
        // "exhibits/images/{uuid}" 형식이라고 가정
        String uuid;
        try {
            // "exhibits/images/" (16자) 다음의 문자열을 uuid로 간주
            if (partialPath == null || !partialPath.startsWith("exhibits/images/") || partialPath.length() <= 16) {
                throw new IllegalArgumentException("잘못된 파일 경로 형식입니다: " + partialPath);
            }
            uuid = partialPath.substring(16);
            // UUID 형식 검증 (권장)
            UUID.fromString(uuid);
        } catch (Exception e) {
            // 로그를 남기고 400 Bad Request 반환
            System.err.println("UUID 추출 실패: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        // 2. (수정) userId와 fileName을 재구성
        String userId = userAuth.getId().toString();
        String fileName = "image.png"; // 업로드 시 사용한 고정값

        // 3. (수정) 스토리지 서버의 'handleRawFileDownload' 엔드포인트와 일치하는
        // *전체 경로*를 재조립합니다.
        String fullLogicalPath = String.format("/storage/exhibits/images/%s/%s/%s", userId, uuid, fileName);


        // 4. (수정) fullLogicalPath를 사용하여 원본 URL 생성
        String downloadUrl = UriComponentsBuilder
                .fromHttpUrl(storageServiceBaseUrl) // 예: "http://localhost:6736"
                .pathSegment(fullLogicalPath.split("/")) // 경로를 안전하게 분리하여 추가
                .build()
                .toUriString();

        // 5. 완성된 URL을 DTO에 담아 반환
        // 예: "http://localhost:6736/exhibits/images/user-id/uuid/image.png"
        DownloadUrlResponse response = new DownloadUrlResponse(downloadUrl);
        return ResponseEntity.ok(response);
    }
}
