package AM.PM.Homepage.home.controller;

import AM.PM.Homepage.home.response.HomepageResponse;
import AM.PM.Homepage.home.service.HomepageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomepageController {

    private final HomepageService homepageService;

    @GetMapping
    public ResponseEntity<HomepageResponse> getAllRecentPosts() {
        HomepageResponse response = homepageService.getAllRecentPosts();
        return ResponseEntity.ok(response);
    }
}
