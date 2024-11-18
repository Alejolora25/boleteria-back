package com.tuboleteria.boleteria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleteria.boleteria.model.Boleta;
import com.tuboleteria.boleteria.model.Usuario;
import com.tuboleteria.boleteria.security.JwtUtil;
import com.tuboleteria.boleteria.service.BoletaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/boletas")
public class BoletaController {

    @Autowired
    private BoletaService boletaService;

    @Autowired
    private JwtUtil jwtUtil; // Inyección de JwtUtil

    @GetMapping
    public List<Boleta> obtenerTodasLasBoletas() {
        return boletaService.obtenerTodasLasBoletas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Boleta> obtenerBoletaPorId(@PathVariable Long id) {
        Optional<Boleta> boleta = boletaService.obtenerBoletaPorId(id);
        return boleta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/evento/{eventoId}")
    public List<Boleta> obtenerBoletasPorEvento(@PathVariable Long eventoId) {
        return boletaService.obtenerBoletasPorEvento(eventoId);
    }

    @GetMapping("/vendedor/{vendedorId}")
    public List<Boleta> obtenerBoletasPorVendedor(@PathVariable Long vendedorId) {
        return boletaService.obtenerBoletasPorVendedor(vendedorId);
    }

    @GetMapping("/estado")
    public List<Boleta> obtenerBoletasPorEstado(@RequestParam String estado) {
        return boletaService.obtenerBoletasPorEstado(estado);
    }

    @PostMapping
    public ResponseEntity<List<Boleta>> crearBoletas(
            @RequestHeader("Authorization") String token, 
            @RequestBody Map<String, Object> payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Boleta boleta = objectMapper.convertValue(payload.get("boleta"), Boleta.class);
            int cantidad = (int) payload.get("cantidad");

            // Extraer el ID del usuario desde el token
            String jwt = token.replace("Bearer ", "");
            Long vendedorId = jwtUtil.extractUserId(jwt); // Usa JwtUtil para extraer el ID

            // Establecer el vendedor en la boleta
            boleta.setVendedor(Usuario.builder().id(vendedorId).build());

            List<Boleta> boletas = boletaService.crearBoletas(boleta, cantidad);
            return ResponseEntity.status(HttpStatus.CREATED).body(boletas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarBoleta(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id, 
            @RequestBody Boleta boleta) {
        try {
            // Extraer el ID del usuario desde el token
            String jwt = token.replace("Bearer ", "");
            Long vendedorId = jwtUtil.extractUserId(jwt);

            // Establecer el vendedor en la boleta
            boleta.setVendedor(Usuario.builder().id(vendedorId).build());

            Boleta boletaActualizada = boletaService.actualizarBoleta(id, boleta);
            return ResponseEntity.ok(boletaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBoleta(@PathVariable Long id) {
        boletaService.eliminarBoleta(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/usar")
    public ResponseEntity<Boleta> usarBoleta(@PathVariable Long id) {
        try {
            Boleta boleta = boletaService.actualizarEstadoUsado(id);
            return ResponseEntity.ok(boleta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    // Endpoint para paginación inicial
    @GetMapping("/paginadas")
    public Page<Boleta> obtenerBoletasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return boletaService.obtenerBoletasPaginadas(pageable);
    }

    // Endpoint para filtrar boletas
    @GetMapping("/filtrar")
    public Page<Boleta> filtrarBoletas(
            @RequestParam String campo,
            @RequestParam String valor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);

        switch (campo.toLowerCase()) {
            case "nombre":
                return boletaService.buscarBoletasPorNombre(valor, pageable);
            case "identificacion":
                return boletaService.buscarBoletasPorIdentificacion(valor, pageable);
            case "correo":
                return boletaService.buscarBoletasPorCorreo(valor, pageable);
            case "telefono":
                return boletaService.buscarBoletasPorCelular(valor, pageable);
            default:
                throw new IllegalArgumentException("Campo de búsqueda no válido");
        }
    }

}
