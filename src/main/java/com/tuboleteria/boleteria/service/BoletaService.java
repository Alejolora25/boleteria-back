package com.tuboleteria.boleteria.service;

import com.tuboleteria.boleteria.model.Boleta;
import com.tuboleteria.boleteria.repository.BoletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BoletaService {

    @Autowired
    private BoletaRepository boletaRepository;

    public List<Boleta> obtenerTodasLasBoletas() {
        return boletaRepository.findAll();
    }

    public Optional<Boleta> obtenerBoletaPorId(Long id) {
        return boletaRepository.findById(id);
    }

    public List<Boleta> obtenerBoletasPorEvento(Long eventoId) {
        return boletaRepository.findByEventoId(eventoId);
    }

    public List<Boleta> obtenerBoletasPorVendedor(Long vendedorId) {
        return boletaRepository.findByVendedorId(vendedorId);
    }

    public List<Boleta> obtenerBoletasPorEstado(String estado) {
        return boletaRepository.findByEstado(estado);
    }

    public List<Boleta> crearBoletas(Boleta boleta, int cantidad) {
        List<Boleta> boletas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Boleta nuevaBoleta = Boleta.builder()
                    .nombreComprador(boleta.getNombreComprador())
                    .identificacionComprador(boleta.getIdentificacionComprador())
                    .correoComprador(boleta.getCorreoComprador())
                    .celular(boleta.getCelular())
                    .edad(boleta.getEdad())
                    .valorTotal(boleta.getValorTotal())
                    .metodoPago(boleta.getMetodoPago())
                    .estado("Vendido")
                    .numeroTransaccion(boleta.getNumeroTransaccion())
                    .codigoQr(generateQrCode(boleta))
                    .etapaVenta(boleta.getEtapaVenta())
                    .fechaCompra(LocalDateTime.now())
                    .tipo(boleta.getTipo())
                    .vendedor(boleta.getVendedor())
                    .evento(boleta.getEvento())
                    .build();

            boletas.add(nuevaBoleta);
        }
        return boletaRepository.saveAll(boletas);
    }

    public Boleta actualizarEstadoUsado(Long id) {
        Optional<Boleta> boletaOpt = boletaRepository.findById(id);
        if (boletaOpt.isPresent()) {
            Boleta boleta = boletaOpt.get();
            if (!"Usado".equals(boleta.getEstado())) {
                boleta.setEstado("Usado");
                return boletaRepository.save(boleta);
            } else {
                throw new IllegalStateException("La boleta ya está en estado 'Usado'.");
            }
        } else {
            throw new IllegalArgumentException("Boleta no encontrada con ID: " + id);
        }
    }

    public void eliminarBoleta(Long id) {
        boletaRepository.deleteById(id);
    }

    private String generateQrCode(Boleta boleta) {
        // Crear un mapa con los datos necesarios para el QR
        var qrData = new java.util.HashMap<String, Object>();
        qrData.put("id", UUID.randomUUID().toString());
        qrData.put("tipo", boleta.getTipo());
        qrData.put("nombre", boleta.getNombreComprador().trim().replaceAll("\\s+", "_"));
        qrData.put("identificacion", boleta.getIdentificacionComprador());
    
        // Convertir el mapa a JSON
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(qrData); // Devuelve el JSON como String
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el contenido del QR", e);
        }
    }

    // Método para obtener boletas paginadas
    public Page<Boleta> obtenerBoletasPaginadas(Pageable pageable) {
        return boletaRepository.findAll(pageable);
    }

    // Método para obtener boletas por estado con paginación
    public Page<Boleta> obtenerBoletasPorEstadoPaginadas(String estado, Pageable pageable) {
        return boletaRepository.findByEstado(estado, pageable);
    }
}
