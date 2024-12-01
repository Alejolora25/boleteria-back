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
import org.springframework.core.io.ClassPathResource;


import com.tuboleteria.boleteria.service.MailService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;


import jakarta.mail.MessagingException;

import java.util.HashMap;
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

    @GetMapping("/filtrar-por-vendedor")
    public Page<Boleta> filtrarBoletasPorVendedorYCampo(
        @RequestParam(required = false) String nombreVendedor,
        @RequestParam(required = false) String campo,
        @RequestParam(required = false) String valor,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // Si no hay vendedor seleccionado, usar el filtro general
        if (nombreVendedor == null || nombreVendedor.isEmpty()) {
            return filtrarBoletas(campo, valor, page, size);
        }

        // Si hay vendedor, combinar filtros
        return boletaService.filtrarPorVendedorYCampo(nombreVendedor, campo, valor, pageable);
    }


    @GetMapping("/vendedores")
    public ResponseEntity<List<Usuario>> obtenerVendedores() {
        List<Usuario> vendedores = boletaService.obtenerVendedoresUnicos();
        return ResponseEntity.ok(vendedores);
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
            // Crear documento PDF en tamaño carta
            com.lowagie.text.Document document = new com.lowagie.text.Document(new com.lowagie.text.Rectangle(216, 279));
            com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(document, pdfStream);
    
            document.open();
    
            com.lowagie.text.pdf.PdfContentByte canvas = writer.getDirectContent();
    
            // Marco general
            canvas.setLineWidth(1.5f);
            canvas.rectangle(10, 10, 196, 259); // Dimensiones del marco
            canvas.stroke();
    
            // Título del evento
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(boleta.getEvento().getNombre(), titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            title.setSpacingBefore(15); // Espaciado desde el borde superior
            title.setSpacingAfter(5);   // Espaciado hacia la imagen
            document.add(title);
    
            // Imagen del evento
            ClassPathResource imgFile = new ClassPathResource("static/images/IMG_0872.PNG");
            com.lowagie.text.Image eventImage = com.lowagie.text.Image.getInstance(imgFile.getInputStream().readAllBytes());
            eventImage.scaleToFit(70, 90); // Ajustar tamaño
            eventImage.setAbsolutePosition(73, 165); // Posicionada más abajo para evitar el texto
            document.add(eventImage);
    
            // Marco interior del detalle de la boleta
            canvas.setLineWidth(0.5f);
            canvas.roundRectangle(15, 85, 186, 100, 5); // Ajustado para no superponerse con la imagen
            canvas.stroke();
    
            // Encabezado del detalle
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 6, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Paragraph header = new com.lowagie.text.Paragraph("Detalle de la Boleta", headerFont);
            header.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            header.setSpacingBefore(95); // Ajustado para estar dentro del marco de detalle
            document.add(header);
    
            // Información de la boleta
            int contentY = 95; // Posición inicial del contenido
            int lineSpacing = 8; // Espaciado entre líneas
    
            // Detalles de la boleta
            canvas.beginText();
            canvas.setFontAndSize(com.lowagie.text.pdf.BaseFont.createFont(), 5); // Tamaño de letra reducido
            canvas.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, "Nombre: " + boleta.getNombreComprador(), 20, contentY, 0);
            canvas.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, "Identificación: " + boleta.getIdentificacionComprador(), 20, contentY - lineSpacing, 0);
            canvas.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, "Correo: " + boleta.getCorreoComprador(), 20, contentY - 2 * lineSpacing, 0);
            canvas.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, "Celular: " + boleta.getCelular(), 20, contentY - 3 * lineSpacing, 0);
            canvas.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, "Edad: " + boleta.getEdad(), 20, contentY - 4 * lineSpacing, 0);
            canvas.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, "Tipo: " + boleta.getTipo(), 20, contentY - 5 * lineSpacing, 0);
            canvas.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, "Fecha de Compra: " + boleta.getFechaCompra(), 20, contentY - 6 * lineSpacing, 0);
            canvas.endText();
    
            // Generar imagen QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(boleta.getCodigoQr(), BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    
            ByteArrayOutputStream qrOutputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", qrOutputStream);
    
            // Agregar QR al PDF
            com.lowagie.text.Image qrPdfImage = com.lowagie.text.Image.getInstance(qrOutputStream.toByteArray());
            qrPdfImage.scaleToFit(50, 50);
            qrPdfImage.setAbsolutePosition(150, 95); // Ajustado para estar en el espacio correcto
            document.add(qrPdfImage);
    
            // Condiciones de uso
            canvas.setColorFill(java.awt.Color.BLACK);
            canvas.rectangle(15, 30, 186, 10); // Fondo negro ajustado
            canvas.fill();
    
            com.lowagie.text.Font conditionTitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 5, com.lowagie.text.Font.BOLD, java.awt.Color.WHITE);
            com.lowagie.text.Paragraph conditionTitle = new com.lowagie.text.Paragraph(
                    "ESTE ES SU TICKET DIGITAL PARA INGRESAR AL EVENTO - RECOMENDACIONES DE USO",
                    conditionTitleFont
            );
            conditionTitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            conditionTitle.setSpacingBefore(40); // Espaciado para no superponerse
            document.add(conditionTitle);
    
            com.lowagie.text.Font conditionFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 4);
            com.lowagie.text.Paragraph conditions = new com.lowagie.text.Paragraph(
                    "1. POR FAVOR IMPRIMA SOLO LOS TICKET DIGITAL RECIBIDOS.\n" +
                    "2. SI USTED HA COMPRADO VARIAS ENTRADAS, IMPRIMA CADA TICKET DIGITAL POR SEPARADO.\n" +
                    "3. EN CASO DE REIMPRIMIR O COPIAR UN TICKET DIGITAL, SOLO SE PERMITIRÁ EL INGRESO A UNO DE ELLOS.\n" +
                    "4. EL TICKET DIGITAL PUEDE SER IMPRESO A COLOR O EN BLANCO Y NEGRO, ASEGÚRESE QUE EL CÓDIGO QR SEA LEGIBLE.\n" +
                    "5. NO PUBLIQUE LOS TICKET DIGITAL NI EXPONGA EL CÓDIGO QR EN REDES SOCIALES.\n" +
                    "6. EL TICKET DIGITAL PUEDE SER PRESENTADO EN SU CELULAR O OTRO DISPOSITIVO.",
                    conditionFont
            );
            conditions.setSpacingBefore(5);
            document.add(conditions);
    
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
        return pdfStream;
    }
    
    
    


    //ESTADISTICAS PARA HOME
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalVendidas", boletaService.contarBoletasPorEstado("Vendido"));
        estadisticas.put("totalUsadas", boletaService.contarBoletasPorEstado("Usado"));
        estadisticas.put("ingresosTotales", boletaService.calcularIngresosPorEstado("Vendido"));
        estadisticas.put("ingresosVIP", boletaService.calcularIngresosPorTipo("VIP"));
        estadisticas.put("ingresosGeneral", boletaService.calcularIngresosPorTipo("General"));
        estadisticas.put("ingresosPalco", boletaService.calcularIngresosPorTipo("Palco"));
        estadisticas.put("boletasVIP", boletaService.contarBoletasPorTipo("VIP"));
        estadisticas.put("boletasGeneral", boletaService.contarBoletasPorTipo("General"));
        estadisticas.put("boletasPalco", boletaService.contarBoletasPorTipo("Palco"));
        estadisticas.put("topClientes", boletaService.obtenerTopClientes());
        estadisticas.put("boletasPorVendedor", boletaService.obtenerBoletasVendidasPorVendedor());
        estadisticas.put("ingresosPorVendedor", boletaService.calcularIngresosPorVendedor());
        estadisticas.put("porcentajeMetodoPago", boletaService.calcularPorcentajeVentasPorMetodoPago());
        estadisticas.put("edadPromedio", boletaService.calcularEdadPromedioCompradores());

        return ResponseEntity.ok(estadisticas);
    }
  
}
