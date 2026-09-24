package com.bananas.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bananas.auth.dto.GlobalMessageResponseDTO;
import com.bananas.auth.dto.LoginRequestDTO;
import com.bananas.auth.dto.LoginResponseDTO;
import com.bananas.auth.dto.RegisterRequestDTO;
import com.bananas.auth.dto.UserDTO;
import com.bananas.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Expone /auth/register, /auth/login y /auth/refreshToken.
 * Con el context-path configurado (/api/v1) queda:
 *  POST /api/v1/auth/register
 *  POST /api/v1/auth/login
 *  POST /api/v1/auth/refreshToken
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<GlobalMessageResponseDTO<UserDTO>> register(@RequestBody RegisterRequestDTO request) {
        try {
            GlobalMessageResponseDTO<UserDTO> response = authService.register(request);
            HttpStatus status = response.getData() != null ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalMessageResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        try {
            GlobalMessageResponseDTO<LoginResponseDTO> response = authService.login(request);
            HttpStatus status = response.getData() != null ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<GlobalMessageResponseDTO<LoginResponseDTO>> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        GlobalMessageResponseDTO<LoginResponseDTO> response = new GlobalMessageResponseDTO<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setMessage("Token is invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            response = authService.refreshToken(token);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("Token is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
