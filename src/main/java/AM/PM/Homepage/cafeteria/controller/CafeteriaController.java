package AM.PM.Homepage.cafeteria.controller;

import AM.PM.Homepage.cafeteria.response.CafeteriaResponse;
import AM.PM.Homepage.cafeteria.service.CafeteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cafeteria")
@RequiredArgsConstructor
public class CafeteriaController {

    private final CafeteriaService cafeteriaService;

    @GetMapping
    public ResponseEntity<List<CafeteriaResponse>> showCafeteriaMenu() throws IOException {
        return ResponseEntity.of(Optional.ofNullable(cafeteriaService.updateDailyCafeteriaMenus()));
    }

}
