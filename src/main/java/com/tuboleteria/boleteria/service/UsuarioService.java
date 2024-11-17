package com.tuboleteria.boleteria.service;

import com.tuboleteria.boleteria.model.Usuario;
import com.tuboleteria.boleteria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public Usuario crearUsuario(Usuario usuario) {
        // Verificar si ya existe un usuario con el mismo correo
        Optional<Usuario> usuarioExistenteCorreo = usuarioRepository.findByCorreo(usuario.getCorreo());
        if (usuarioExistenteCorreo.isPresent()) {
            throw new IllegalArgumentException("El correo ya está en uso.");
        }
    
        // Verificar si ya existe un usuario con la misma identificación
        Optional<Usuario> usuarioExistenteIdentificacion = usuarioRepository.findByIdentificacion(usuario.getIdentificacion());
        if (usuarioExistenteIdentificacion.isPresent()) {
            throw new IllegalArgumentException("La identificación ya está en uso.");
        }

        // Asignar rol por defecto si no se especifica
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            usuario.setRoles(List.of("ROLE_USER"));
        }
    
        // Cifrar la contraseña antes de guardar
        usuario.setContraseña(passwordEncoder.encode(usuario.getContraseña()));
    
        // Guardar el nuevo usuario
        return usuarioRepository.save(usuario);
    }

    public boolean verificarContraseña(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
