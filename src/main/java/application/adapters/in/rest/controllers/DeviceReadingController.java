package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.DeviceReadingRestMapper;
import application.adapters.in.rest.requests.DeviceReadingRequest;
import application.adapters.in.rest.requests.PartialDeviceReadingRequest;
import application.adapters.in.rest.responses.DeviceReadingResponse;
import application.domain.models.DeviceReading;
import application.domain.ports.in.ConsultDeviceReadingsUseCase;
import application.domain.ports.in.DeleteDeviceReadingUseCase;
import application.domain.ports.in.RegisterDeviceReadingUseCase;
import application.domain.ports.in.ReplaceDeviceReadingUseCase;
import application.domain.ports.in.UpdateDeviceReadingPartiallyUseCase;
import application.infrastructure.config.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lifecycle of the readings of each surveillance device (F2, F3, F4, F5, F6).
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceReadingController {

    private final RegisterDeviceReadingUseCase registerDeviceReadingUseCase;
    private final ConsultDeviceReadingsUseCase consultDeviceReadingsUseCase;
    private final ReplaceDeviceReadingUseCase replaceDeviceReadingUseCase;
    private final UpdateDeviceReadingPartiallyUseCase updateDeviceReadingPartiallyUseCase;
    private final DeleteDeviceReadingUseCase deleteDeviceReadingUseCase;
    private final DeviceReadingRestMapper deviceReadingRestMapper;

    /** F2 - registers the reading of one device. */
    @PostMapping("/{deviceName}/readings")
    public ResponseEntity<ApiResponse<DeviceReadingResponse>> register(
            @PathVariable String deviceName, @Valid @RequestBody DeviceReadingRequest request) {
        DeviceReading registered = registerDeviceReadingUseCase.register(
                deviceReadingRestMapper.toDomain(deviceName, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Device reading registered successfully", deviceReadingRestMapper.toResponse(registered)));
    }

    /** F3 - lists every registered reading. */
    @GetMapping("/readings")
    public ResponseEntity<ApiResponse<List<DeviceReadingResponse>>> consultAll() {
        List<DeviceReading> readings = consultDeviceReadingsUseCase.consultAll();
        return ResponseEntity.ok(
                ApiResponse.ok("Device readings retrieved successfully", deviceReadingRestMapper.toResponses(readings)));
    }

    /** F3 - retrieves the reading of one device. */
    @GetMapping("/{deviceName}/readings")
    public ResponseEntity<ApiResponse<DeviceReadingResponse>> consult(@PathVariable String deviceName) {
        DeviceReading reading = consultDeviceReadingsUseCase.consult(deviceReadingRestMapper.toDevice(deviceName));
        return ResponseEntity.ok(
                ApiResponse.ok("Device reading retrieved successfully", deviceReadingRestMapper.toResponse(reading)));
    }

    /** F4 - fully replaces the reading of one device. */
    @PutMapping("/{deviceName}/readings")
    public ResponseEntity<ApiResponse<DeviceReadingResponse>> replace(
            @PathVariable String deviceName, @Valid @RequestBody DeviceReadingRequest request) {
        DeviceReading replaced = replaceDeviceReadingUseCase.replace(
                deviceReadingRestMapper.toDomain(deviceName, request));
        return ResponseEntity.ok(
                ApiResponse.ok("Device reading replaced successfully", deviceReadingRestMapper.toResponse(replaced)));
    }

    /** F5 - partially updates the reading of one device. */
    @PatchMapping("/{deviceName}/readings")
    public ResponseEntity<ApiResponse<DeviceReadingResponse>> updatePartially(
            @PathVariable String deviceName, @Valid @RequestBody PartialDeviceReadingRequest request) {
        DeviceReading updated = updateDeviceReadingPartiallyUseCase.update(
                deviceReadingRestMapper.toDevice(deviceName),
                deviceReadingRestMapper.toDistance(request.distance()),
                deviceReadingRestMapper.toFragment(request.message()));
        return ResponseEntity.ok(
                ApiResponse.ok("Device reading updated successfully", deviceReadingRestMapper.toResponse(updated)));
    }

    /** F6 - deletes the reading of one device. */
    @DeleteMapping("/{deviceName}/readings")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String deviceName) {
        deleteDeviceReadingUseCase.delete(deviceReadingRestMapper.toDevice(deviceName));
        return ResponseEntity.ok(ApiResponse.ok("Device reading deleted successfully", null));
    }
}
