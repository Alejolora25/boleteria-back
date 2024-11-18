package com.tuboleteria.boleteria.repository;

import com.tuboleteria.boleteria.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    List<Boleta> findByEventoId(Long eventoId);

    List<Boleta> findByVendedorId(Long vendedorId);

    List<Boleta> findByEstado(String estado);

    // Método para paginación
    Page<Boleta> findAll(Pageable pageable);

    // Ejemplo de filtro por estado con paginación
    Page<Boleta> findByEstado(String estado, Pageable pageable);

    // Nuevos métodos
    Page<Boleta> findByNombreCompradorContainingIgnoreCase(String nombreComprador, Pageable pageable);
    Page<Boleta> findByIdentificacionCompradorContainingIgnoreCase(String identificacionComprador, Pageable pageable);
    Page<Boleta> findByCorreoCompradorContainingIgnoreCase(String correoComprador, Pageable pageable);
    Page<Boleta> findByCelularContainingIgnoreCase(String celular, Pageable pageable);

}

