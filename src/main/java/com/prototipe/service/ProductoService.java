package com.prototipe.service;



import com.prototipe.model.Producto;
import com.prototipe.repository.ProductoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProductoService {

    @Inject
    ProductoRepository productoRepository;

    public List<Producto> obtenerTodosProductos() {
        return productoRepository.findActivos();
    }

    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto crearProducto(Producto producto) {
        // ✅ Asegurar que el ID sea null para nueva entidad
        producto.idProducto = null;

        // Validar código único
        if (productoRepository.findByCodigo(producto.codigo).isPresent()) {
            throw new RuntimeException("El código de producto ya existe");
        }

        productoRepository.persist(producto);
        return producto;
    }

    @Transactional
    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        Producto producto = productoRepository.findById(id);
        if (producto != null) {
            producto.nombre = productoActualizado.nombre;
            producto.descripcion = productoActualizado.descripcion;
            producto.precioVenta = productoActualizado.precioVenta;
            producto.costo = productoActualizado.costo;
            producto.existencia = productoActualizado.existencia;
            producto.categoria = productoActualizado.categoria;
        }
        return producto;
    }

    @Transactional
    public void desactivarProducto(Long id) {
        Producto producto = productoRepository.findById(id);
        if (producto != null) {
            producto.activo = "N";
        }
    }
}
