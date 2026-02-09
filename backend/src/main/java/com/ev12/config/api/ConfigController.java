package com.ev12.config.api;

import com.ev12.config.model.ConfigRequest;
import com.ev12.config.model.ConfigResponse;
import com.ev12.config.service.ConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @PostMapping("/send-config")
    public ResponseEntity<ConfigResponse> sendConfig(@Valid @RequestBody ConfigRequest request) {
        ConfigResponse response = configService.sendConfiguration(request);
        return ResponseEntity.ok(response);
    }
}
