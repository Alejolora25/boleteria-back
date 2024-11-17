package com.tuboleteria.boleteria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleteria.boleteria.model.Boleta;
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
    public ResponseEntity<List<Boleta>> crearBoletas(@RequestBody Map<String, Object> payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Boleta boleta = objectMapper.convertValue(payload.get("boleta"), Boleta.class);
            int cantidad = (int) payload.get("cantidad");
            List<Boleta> boletas = boletaService.crearBoletas(boleta, cantidad);
            return ResponseEntity.status(HttpStatus.CREATED).body(boletas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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

    // Endpoint para obtener boletas paginadas
    @GetMapping("/paginadas")
    public Page<Boleta> obtenerBoletasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return boletaService.obtenerBoletasPaginadas(pageable);
    }

    // Endpoint para obtener boletas por estado con paginación
    @GetMapping("/estado/paginadas")
    public Page<Boleta> obtenerBoletasPorEstadoPaginadas(
            @RequestParam String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return boletaService.obtenerBoletasPorEstadoPaginadas(estado, pageable);
    }

}
