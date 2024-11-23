package com.tuboleteria.boleteria.repository;

import com.tuboleteria.boleteria.model.Boleta;
import com.tuboleteria.boleteria.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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

    @Query("SELECT DISTINCT b.vendedor FROM Boleta b")
    List<Usuario> findDistinctVendedores();

    @Query("SELECT b FROM Boleta b WHERE LOWER(b.vendedor.nombre) LIKE LOWER(CONCAT('%', :nombreVendedor, '%')) AND LOWER(b.nombreComprador) LIKE LOWER(CONCAT('%', :valor, '%'))")
    Page<Boleta> findByVendedorAndNombreCompradorContainingIgnoreCase(@Param("nombreVendedor") String nombreVendedor, @Param("valor") String valor, Pageable pageable);

    @Query("SELECT b FROM Boleta b WHERE LOWER(b.vendedor.nombre) LIKE LOWER(CONCAT('%', :nombreVendedor, '%')) AND LOWER(b.identificacionComprador) LIKE LOWER(CONCAT('%', :valor, '%'))")
    Page<Boleta> findByVendedorAndIdentificacionCompradorContainingIgnoreCase(@Param("nombreVendedor") String nombreVendedor, @Param("valor") String valor, Pageable pageable);

    @Query("SELECT b FROM Boleta b WHERE LOWER(b.vendedor.nombre) LIKE LOWER(CONCAT('%', :nombreVendedor, '%')) AND LOWER(b.correoComprador) LIKE LOWER(CONCAT('%', :valor, '%'))")
    Page<Boleta> findByVendedorAndCorreoCompradorContainingIgnoreCase(@Param("nombreVendedor") String nombreVendedor, @Param("valor") String valor, Pageable pageable);

    @Query("SELECT b FROM Boleta b WHERE LOWER(b.vendedor.nombre) LIKE LOWER(CONCAT('%', :nombreVendedor, '%')) AND LOWER(b.celular) LIKE LOWER(CONCAT('%', :valor, '%'))")
    Page<Boleta> findByVendedorAndCelularContainingIgnoreCase(@Param("nombreVendedor") String nombreVendedor, @Param("valor") String valor, Pageable pageable);

    //ESTADISTICAS PARA HOME
    // Contar la cantidad total de boletas vendidas por estado
    @Query("SELECT COUNT(b) FROM Boleta b WHERE b.estado = :estado")
    Long countBoletasByEstado(@Param("estado") String estado);

    // Calcular los ingresos totales por estado
    @Query("SELECT SUM(b.valorTotal) FROM Boleta b WHERE b.estado = :estado")
    BigDecimal sumIngresosByEstado(@Param("estado") String estado);

    // Calcular los ingresos totales por tipo
    @Query("SELECT SUM(b.valorTotal) FROM Boleta b WHERE b.tipo = :tipo AND b.estado = 'Vendido'")
    BigDecimal sumIngresosByTipo(@Param("tipo") String tipo);

    // Contar la cantidad de boletas por tipo
    @Query("SELECT COUNT(b) FROM Boleta b WHERE b.tipo = :tipo AND b.estado = 'Vendido'")
    Long countBoletasByTipo(@Param("tipo") String tipo);

    // Obtener el cliente que más ha comprado
    @Query("SELECT b.nombreComprador, COUNT(b) as total FROM Boleta b GROUP BY b.nombreComprador ORDER BY total DESC")
    List<Object[]> findTopCliente();

    // Obtener la cantidad total de boletas vendidas por cada vendedor
    @Query("SELECT b.vendedor.nombre, COUNT(b) as total FROM Boleta b WHERE b.estado = 'Vendido' GROUP BY b.vendedor.nombre")
    List<Object[]> findBoletasVendidasPorVendedor();

    // Calcular los ingresos totales generados por cada vendedor
    @Query("SELECT b.vendedor.nombre, SUM(b.valorTotal) as total FROM Boleta b WHERE b.estado = 'Vendido' GROUP BY b.vendedor.nombre")
    List<Object[]> sumIngresosPorVendedor();

    // Obtener el porcentaje de ventas por método de pago
    @Query("SELECT b.metodoPago, COUNT(b) as total FROM Boleta b GROUP BY b.metodoPago")
    List<Object[]> findVentasPorMetodoPago();

    // Calcular la edad promedio de los compradores
    @Query("SELECT AVG(b.edad) FROM Boleta b")
    Double calcularEdadPromedio();

}

