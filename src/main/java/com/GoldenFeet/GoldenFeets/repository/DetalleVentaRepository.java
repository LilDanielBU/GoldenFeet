package com.GoldenFeet.GoldenFeets.repository;

import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Importación necesaria para @Transactional en la interfaz

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Método para eliminar todos los DetalleVenta asociados a una VARIANTE.
    // ESTA ES LA CORRECCIÓN CLAVE PARA ELIMINAR VARIANTES INDIVIDUALES.
    @Modifying
    @Transactional
    // Asume que la entidad DetalleVenta tiene un campo de relación llamado 'variante'.
    void deleteByVarianteId(Long varianteId);

    // Este método ya existía en tu código. Se mantiene, aunque es mejor usar la sintaxis de Spring Data JPA.
    // Si la entidad DetalleVenta tiene un campo de relación 'variante', y este a su vez
    // apunta a un 'producto', el DTO se usaría así: DELETE FROM DetalleVenta dv WHERE dv.variante.producto.id = :productoId
    // La anotación @Query está bien si necesitas consultas complejas.
    @Modifying
    @Query("DELETE FROM DetalleVenta dv WHERE dv.variante.producto.id = :productoId")
    void deleteByProducto_Id(@Param("productoId") Long productoId);

    // Nota: Es mejor usar deleteByVarianteId para la eliminación de variantes individuales,
    // ya que es el que se requiere para la lógica de la clave foránea entre Variante y DetalleVenta.
}