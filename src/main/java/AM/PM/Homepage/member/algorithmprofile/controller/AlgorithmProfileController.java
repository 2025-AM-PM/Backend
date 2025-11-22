package AM.PM.Homepage.member.algorithmprofile.controller;

import AM.PM.Homepage.member.algorithmprofile.response.AlgorithmProfileResponse;
import AM.PM.Homepage.member.algorithmprofile.service.AlgorithmProfileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students/tiers")
public class AlgorithmProfileController {

    private final AlgorithmProfileService algorithmProfileService;

    @GetMapping
    public ResponseEntity<List<AlgorithmProfileResponse>> getTopTiers() {
        List<AlgorithmProfileResponse> response = algorithmProfileService.getTopTiers();
        return ResponseEntity.ok(response);
    }
}
