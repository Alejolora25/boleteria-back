package com.tuboleteria.boleteria.service;

import com.tuboleteria.boleteria.model.Boleta;
import com.tuboleteria.boleteria.model.Usuario;
import com.tuboleteria.boleteria.repository.BoletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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

    public Boleta actualizarBoleta(Long id, Boleta boleta) {
        Boleta boletaExistente = boletaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada con ID: " + id));
    
        // Actualizar los campos necesarios
        boletaExistente.setNombreComprador(boleta.getNombreComprador());
        boletaExistente.setIdentificacionComprador(boleta.getIdentificacionComprador());
        boletaExistente.setCorreoComprador(boleta.getCorreoComprador());
        boletaExistente.setCelular(boleta.getCelular());
        boletaExistente.setEdad(boleta.getEdad());
        boletaExistente.setValorTotal(boleta.getValorTotal());
        boletaExistente.setMetodoPago(boleta.getMetodoPago());
        boletaExistente.setEstado(boleta.getEstado());
        boletaExistente.setNumeroTransaccion(boleta.getNumeroTransaccion());
        boletaExistente.setTipo(boleta.getTipo());
        boletaExistente.setEtapaVenta(boleta.getEtapaVenta());
    
        return boletaRepository.save(boletaExistente);
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

    // Métodos de búsqueda con paginación
    public Page<Boleta> buscarBoletasPorNombre(String nombre, Pageable pageable) {
        return boletaRepository.findByNombreCompradorContainingIgnoreCase(nombre, pageable);
    }

    public Page<Boleta> buscarBoletasPorIdentificacion(String identificacion, Pageable pageable) {
        return boletaRepository.findByIdentificacionCompradorContainingIgnoreCase(identificacion, pageable);
    }

    public Page<Boleta> buscarBoletasPorCorreo(String correo, Pageable pageable) {
        return boletaRepository.findByCorreoCompradorContainingIgnoreCase(correo, pageable);
    }

    public Page<Boleta> buscarBoletasPorCelular(String celular, Pageable pageable) {
        return boletaRepository.findByCelularContainingIgnoreCase(celular, pageable);
    }

    public List<Usuario> obtenerVendedoresUnicos() {
        return boletaRepository.findDistinctVendedores();
    }

    public Page<Boleta> filtrarPorVendedorYCampo(String nombreVendedor, String campo, String valor, Pageable pageable) {
        switch (campo.toLowerCase()) {
            case "nombre":
                return boletaRepository.findByVendedorAndNombreCompradorContainingIgnoreCase(nombreVendedor, valor, pageable);
            case "identificacion":
                return boletaRepository.findByVendedorAndIdentificacionCompradorContainingIgnoreCase(nombreVendedor, valor, pageable);
            case "correo":
                return boletaRepository.findByVendedorAndCorreoCompradorContainingIgnoreCase(nombreVendedor, valor, pageable);
            case "telefono":
                return boletaRepository.findByVendedorAndCelularContainingIgnoreCase(nombreVendedor, valor, pageable);
            default:
                throw new IllegalArgumentException("Campo de búsqueda no válido");
        }
    }
    

    //ESTADISTICAS PARA HOME
    public Long contarBoletasPorEstado(String estado) {
    return boletaRepository.countBoletasByEstado(estado);
    }

    public BigDecimal calcularIngresosPorEstado(String estado) {
        return boletaRepository.sumIngresosByEstado(estado);
    }

    public BigDecimal calcularIngresosPorTipo(String tipo) {
        return boletaRepository.sumIngresosByTipo(tipo);
    }

    public Long contarBoletasPorTipo(String tipo) {
        return boletaRepository.countBoletasByTipo(tipo);
    }

    public List<Object[]> obtenerTopClientes() {
        return boletaRepository.findTopCliente();
    }

    public List<Object[]> obtenerBoletasVendidasPorVendedor() {
        return boletaRepository.findBoletasVendidasPorVendedor();
    }

    public List<Object[]> calcularIngresosPorVendedor() {
        return boletaRepository.sumIngresosPorVendedor();
    }

    public List<Object[]> calcularPorcentajeVentasPorMetodoPago() {
        return boletaRepository.findVentasPorMetodoPago();
    }

    public Double calcularEdadPromedioCompradores() {
        return boletaRepository.calcularEdadPromedio();
    }
  
}
