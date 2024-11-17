package com.tuboleteria.boleteria.controller;

import com.tuboleteria.boleteria.model.LoginRequest;
import com.tuboleteria.boleteria.model.Usuario;
import com.tuboleteria.boleteria.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public Authentication getCurrentUser(Authentication authentication) {
        return authentication; // Devuelve el usuario autenticado
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Buscar usuario por correo
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorCorreo(loginRequest.getCorreo());
        
        // Verificar si el usuario existe y si la contraseña coincide
        if (usuarioOpt.isPresent() &&
            passwordEncoder.matches(loginRequest.getContraseña(), usuarioOpt.get().getContraseña())) {
            
            // Crear un token ficticio para pruebas (en un caso real, generar JWT)
            String token = "fake-jwt-token"; 
            
            // Devolver respuesta con el token
            return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "Inicio de sesión exitoso."
            ));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "message", "Correo o contraseña incorrectos."
        ));
    }
}
