package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.LoginDTO;
import com.tecmilenio.mapsconect.dto.RegistroDTO;
import com.tecmilenio.mapsconect.dto.TokenDTO;
import com.tecmilenio.mapsconect.dto.UsuarioDTO;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public TokenDTO registrar(RegistroDTO registroDTO) {
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .email(registroDTO.getEmail())
                .nombre(registroDTO.getNombre())
                .apellido(registroDTO.getApellido())
                .contrasena(passwordEncoder.encode(registroDTO.getContrasena()))
                .rol(Usuario.Rol.ESTUDIANTE)
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        String token = jwtTokenProvider.generateToken(usuarioGuardado.getEmail());

        return TokenDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiresIn(86400L)
                .usuario(mapearADTO(usuarioGuardado))
                .build();
    }

    public TokenDTO login(LoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(loginDTO.getContrasena(), usuario.getContrasena())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtTokenProvider.generateToken(usuario.getEmail());

        return TokenDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiresIn(86400L)
                .usuario(mapearADTO(usuario))
                .build();
    }

    public UsuarioDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return mapearADTO(usuario);
    }

    private UsuarioDTO mapearADTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol().toString())
                .activo(usuario.getActivo())
                .build();
    }

}
