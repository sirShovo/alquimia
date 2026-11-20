package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.InterceptionRestMapper;
import application.adapters.in.rest.requests.FullInterceptionRequest;
import application.adapters.in.rest.responses.InterceptionResponse;
import application.domain.models.InterceptionResult;
import application.domain.ports.in.ProcessAccumulatedInterceptionUseCase;
import application.domain.ports.in.ProcessFullInterceptionUseCase;
import application.infrastructure.config.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lab location and message reconstruction: full processing (F1) and accumulated processing (F3).
 */
@RestController
@RequestMapping("/api/interceptions")
@RequiredArgsConstructor
public class InterceptionController {

    private final ProcessFullInterceptionUseCase processFullInterceptionUseCase;
    private final ProcessAccumulatedInterceptionUseCase processAccumulatedInterceptionUseCase;
    private final InterceptionRestMapper interceptionRestMapper;

    /** F1 - processes the information of the three devices received at once. */
    @PostMapping
    public ResponseEntity<ApiResponse<InterceptionResponse>> processFull(
            @Valid @RequestBody FullInterceptionRequest request) {
        InterceptionResult result = processFullInterceptionUseCase.process(interceptionRestMapper.toDomain(request));
        return ResponseEntity.ok(
                ApiResponse.ok("Interception processed successfully", interceptionRestMapper.toResponse(result)));
    }

    /** F3 - processes the readings registered individually. */
    @GetMapping("/accumulated")
    public ResponseEntity<ApiResponse<InterceptionResponse>> processAccumulated() {
        InterceptionResult result = processAccumulatedInterceptionUseCase.process();
        return ResponseEntity.ok(ApiResponse.ok(
                "Accumulated interception processed successfully", interceptionRestMapper.toResponse(result)));
    }
}
