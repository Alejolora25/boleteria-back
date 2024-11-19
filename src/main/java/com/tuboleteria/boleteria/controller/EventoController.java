package com.tuboleteria.boleteria.controller;

import com.tuboleteria.boleteria.model.Evento;
import com.tuboleteria.boleteria.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @GetMapping
    public List<Evento> obtenerTodosLosEventos() {
        return eventoService.obtenerTodosLosEventos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evento> obtenerEventoPorId(@PathVariable Long id) {
        Optional<Evento> evento = eventoService.obtenerEventoPorId(id);
        return evento.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<Evento> buscarEventosPorNombre(@RequestParam String nombre) {
        return eventoService.buscarEventosPorNombre(nombre);
    }

    @PostMapping
    public Evento crearEvento(@RequestBody Evento evento) {
        return eventoService.crearEvento(evento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEvento(@PathVariable Long id, @RequestBody Evento evento) {
        try {
            Evento eventoActualizado = eventoService.actualizarEvento(id, evento);
            return ResponseEntity.ok(eventoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEvento(@PathVariable Long id) {
        try {
            eventoService.eliminarEvento(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

}
