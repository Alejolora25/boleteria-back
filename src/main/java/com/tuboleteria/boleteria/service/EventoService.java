package com.tuboleteria.boleteria.service;

import com.tuboleteria.boleteria.model.Evento;
import com.tuboleteria.boleteria.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    public List<Evento> obtenerTodosLosEventos() {
        return eventoRepository.findAll();
    }

    public Optional<Evento> obtenerEventoPorId(Long id) {
        return eventoRepository.findById(id);
    }

    public List<Evento> buscarEventosPorNombre(String nombre) {
        return eventoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Evento crearEvento(Evento evento) {
        return eventoRepository.save(evento);
    }

    public Evento actualizarEvento(Long id, Evento evento) {
        Evento eventoExistente = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + id));
    
        // Actualizar los campos necesarios
        eventoExistente.setNombre(evento.getNombre());
        eventoExistente.setDescripcion(evento.getDescripcion());
        eventoExistente.setFecha(evento.getFecha());
        eventoExistente.setLugar(evento.getLugar());
        eventoExistente.setCapacidad(evento.getCapacidad());
    
        return eventoRepository.save(eventoExistente);
    }
    

    public void eliminarEvento(Long id) {
        eventoRepository.deleteById(id);
    }
}
