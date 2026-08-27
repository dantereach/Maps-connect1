package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.LoginDTO;
import com.tecmilenio.mapsconect.dto.RegistroDTO;
import com.tecmilenio.mapsconect.dto.TokenDTO;
import com.tecmilenio.mapsconect.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<TokenDTO>> registrar(@Valid @RequestBody RegistroDTO registroDTO) {
        TokenDTO token = usuarioService.registrar(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(token, "Usuario registrado correctamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        TokenDTO token = usuarioService.login(loginDTO);
        return ResponseEntity.ok(ApiResponse.success(token, "Sesión iniciada correctamente"));
    }

}
