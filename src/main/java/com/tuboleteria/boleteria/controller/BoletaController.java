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

import com.tuboleteria.boleteria.service.MailService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;


import jakarta.mail.MessagingException;

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

    @Autowired
    private MailService mailService;

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
    public ResponseEntity<?>crearBoletas(
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
        } catch (jakarta.validation.ConstraintViolationException e) {
            // Manejo de errores de validación
            String errorMessage = e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                    .orElse("Error de validación");
            return ResponseEntity.badRequest().body(errorMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la solicitud");
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

    @PostMapping("/{id}/enviar")
    public ResponseEntity<?> enviarBoleta(@PathVariable Long id) {
        try {
            Boleta boleta = boletaService.obtenerBoletaPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada"));

            ByteArrayOutputStream pdfStream = generarPdfBoleta(boleta);

            String destinatario = boleta.getCorreoComprador();
            String asunto = "Tu Boleta para el evento " + boleta.getEvento().getNombre();
            String mensaje = "<p>Hola " + boleta.getNombreComprador() + ",</p>" +
                            "<p>Gracias por tu compra. Adjunto encontrarás tu boleta para el evento " + boleta.getEvento().getNombre() + ".</p>";

            mailService.enviarBoleta(destinatario, asunto, mensaje, pdfStream);

            // Devuelve una respuesta JSON explícita
            return ResponseEntity.ok(Map.of("mensaje", "Correo enviado con éxito."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al enviar el correo: " + e.getMessage()));
        }
    }


    private ByteArrayOutputStream generarPdfBoleta(Boleta boleta) {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        try {
            // Crear documento PDF
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, pdfStream);
    
            document.open();
            document.addTitle("Boleta");
            document.add(new com.lowagie.text.Paragraph("Detalle de la Boleta"));
            document.add(new com.lowagie.text.Paragraph("Nombre: " + boleta.getNombreComprador()));
            document.add(new com.lowagie.text.Paragraph("Identificación: " + boleta.getIdentificacionComprador()));
            document.add(new com.lowagie.text.Paragraph("Correo: " + boleta.getCorreoComprador()));
            document.add(new com.lowagie.text.Paragraph("Evento: " + boleta.getEvento().getNombre()));
            document.add(new com.lowagie.text.Paragraph("Tipo: " + boleta.getTipo()));
            document.add(new com.lowagie.text.Paragraph("Fecha de Compra: " + boleta.getFechaCompra()));
    
            // Generar imagen QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                boleta.getCodigoQr(), BarcodeFormat.QR_CODE, 300, 300);
    
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    
            // Convertir imagen QR a byte array
            ByteArrayOutputStream qrOutputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", qrOutputStream);
    
            // Agregar QR al PDF
            com.lowagie.text.Image qrPdfImage = com.lowagie.text.Image.getInstance(qrOutputStream.toByteArray());
            document.add(qrPdfImage);
    
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
        return pdfStream;
    }
    
}
