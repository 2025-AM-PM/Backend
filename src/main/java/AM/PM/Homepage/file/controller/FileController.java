package AM.PM.Homepage.file.controller;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final SignatureGenerator signatureGenerator;
    private final String storageServiceBaseUrl = "localhost:6736";

    /**
     * 프론트엔드에서 파일 업로드를 위한 Presigned URL을 생성하여 반환합니다.
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadUrlResponse> makePreSignedUrl(
            @AuthenticationPrincipal UserAuth userAuth // (인증 객체, 필요시 사용)
    ) {

        // 1. StorageService에 PUT 요청을 보낼 것임을 명시
        // (Interceptor가 "PUT" 메서드 기준으로 서명을 검증함)
        String httpMethod = "PUT";

        // 2. 파일 ID로 UUID 사용 (String)
        String fileId = UUID.randomUUID().toString();
        String userId = userAuth.getId().toString();
        String filePath = "/images/{userId}/posts/{fileId}/image.png";

        // 3. 만료 시간 설정 (예: 10분 후 만료)
        // System.currentTimeMillis() / 1000 : 현재 시간을 '초' 단위로 변경
        long expires = (System.currentTimeMillis() / 1000) + 600; // 10분

        // 4. 서명 생성
        String signature = signatureGenerator.generateSignature(httpMethod, fileId, expires);

        // 5. 프론트엔드가 요청할 최종 URL 조립
        // (예: http://storage.server.com/storage/{fileId}?expires=...&signature=...)
        String presignedUrl = UriComponentsBuilder
                .fromHttpUrl("http://localhost:6736") // StorageService 주소
                .path("/exhibits/images/{userId}/{fileId}/{filePath}")
                .queryParam("expires", expires)
                .queryParam("signature", signature)
                .buildAndExpand(fileId) // {fileId} 경로 변수 치환
                .toUriString();

        // 6. DTO 생성 및 반환
        UploadUrlResponse responseDto = new UploadUrlResponse(fileId, presignedUrl);

        return ResponseEntity.ok(responseDto);
    }

}
