package com.prototipe.service;

import com.prototipe.exceptions.*;
import com.prototipe.utils.*;
import com.prototipe.model.Producto;
import com.prototipe.repository.ProductoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ProductoService {

    private static final Logger LOG = Logger.getLogger(ProductoService.class.getName());

    @Inject
    ProductoRepository productoRepository;

    public List<Producto> obtenerTodosProductos() {
        LOG.fine("📦 Obteniendo lista de productos activos");
        return productoRepository.findActivos();
    }

    public Producto obtenerProductoPorId(Long id) {
        ValidationUtils.validarNoNulo(id, "idProducto");
        ValidationUtils.validarPositivo(id, "idProducto");

        Producto producto = productoRepository.findById(id);
        if (producto == null) {
            throw new VentaException("Producto no encontrado: " + id, "PROD_002");
        }

        if (!"S".equals(producto.activo)) {
            LOG.warning("⚠️ Intento de acceso a producto inactivo - ID: " + id);
        }

        return producto;
    }

    @Transactional
    public Producto crearProducto(Producto productoRequest) {
        try {
            LOG.info("🆕 Creando nuevo producto");

            // Validaciones básicas
            ValidationUtils.validarNoNulo(productoRequest, "producto");
            ValidationUtils.validarTextoNoVacio(productoRequest.codigo, "codigo");
            ValidationUtils.validarTextoNoVacio(productoRequest.nombre, "nombre");
            BusinessRules.validarPrecioPositivo(productoRequest.precioVenta);

            // Validar código único
            if (productoRepository.findByCodigo(productoRequest.codigo).isPresent()) {
                throw new ValidationException(
                        "El código de producto ya existe",
                        "codigo",
                        productoRequest.codigo
                );
            }

            // Validar existencia no negativa
            if (productoRequest.existencia < 0) {
                throw new ValidationException(
                        "La existencia no puede ser negativa",
                        "existencia",
                        String.valueOf(productoRequest.existencia)
                );
            }

            // ✅ SOLUCIÓN: Crear NUEVA instancia en lugar de usar la recibida
            Producto producto = new Producto();
            producto.codigo = productoRequest.codigo;
            producto.nombre = productoRequest.nombre;
            producto.descripcion = productoRequest.descripcion;
            producto.precioVenta = productoRequest.precioVenta;
            producto.costo = productoRequest.costo;
            producto.existencia = productoRequest.existencia != null ? productoRequest.existencia : 0;
            producto.categoria = productoRequest.categoria;
            producto.activo = productoRequest.activo != null ? productoRequest.activo : "S";

            // ✅ Asegurar que el ID sea null para nueva entidad
            producto.idProducto = null;

            productoRepository.persist(producto);
            LOG.info("✅ Producto creado - ID: " + producto.idProducto + ", Código: " + producto.codigo);

            return producto;

        } catch (ValidationException e) {
            LOG.severe("❌ Error de validación en producto: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al crear producto: " + e.getMessage());
            throw new VentaException("Error interno al crear producto: " + e.getMessage(), "PROD_001");
        }
    }

    @Transactional
    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        try {
            ValidationUtils.validarNoNulo(id, "idProducto");
            ValidationUtils.validarPositivo(id, "idProducto");
            ValidationUtils.validarNoNulo(productoActualizado, "productoActualizado");

            Producto producto = productoRepository.findById(id);
            if (producto == null) {
                throw new VentaException("Producto no encontrado: " + id, "PROD_002");
            }

            LOG.info("✏️ Actualizando producto - ID: " + id);

            // Validar y actualizar campos
            if (productoActualizado.nombre != null) {
                ValidationUtils.validarTextoNoVacio(productoActualizado.nombre, "nombre");
                producto.nombre = productoActualizado.nombre;
            }

            if (productoActualizado.descripcion != null) {
                producto.descripcion = productoActualizado.descripcion;
            }

            if (productoActualizado.precioVenta != null) {
                BusinessRules.validarPrecioPositivo(productoActualizado.precioVenta);
                producto.precioVenta = productoActualizado.precioVenta;
            }

            if (productoActualizado.costo != null) {
                BusinessRules.validarPrecioPositivo(productoActualizado.costo);
                producto.costo = productoActualizado.costo;
            }

            if (productoActualizado.existencia != null) {
                if (productoActualizado.existencia < 0) {
                    throw new ValidationException(
                            "La existencia no puede ser negativa",
                            "existencia",
                            String.valueOf(productoActualizado.existencia)
                    );
                }
                producto.existencia = productoActualizado.existencia;
            }

            if (productoActualizado.categoria != null) {
                producto.categoria = productoActualizado.categoria;
            }

            LOG.info("✅ Producto actualizado - ID: " + id);
            return producto;

        } catch (ValidationException e) {
            LOG.severe("❌ Error de validación al actualizar producto: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al actualizar producto: " + e.getMessage());
            throw new VentaException("Error interno al actualizar producto", "PROD_003");
        }
    }

    @Transactional
    public void desactivarProducto(Long id) {
        try {
            ValidationUtils.validarNoNulo(id, "idProducto");
            ValidationUtils.validarPositivo(id, "idProducto");

            Producto producto = productoRepository.findById(id);
            if (producto != null) {
                // Validar que no tenga ventas activas pendientes
                /*
                boolean tieneVentasActivas = productoRepository.tieneVentasActivas(id);
                if (tieneVentasActivas) {
                    throw new VentaException(
                            "No se puede desactivar producto con ventas activas",
                            "PROD_004"
                    );
                }

                 */

                producto.activo = "N";
                LOG.info("🚫 Producto desactivado - ID: " + id + ", Código: " + producto.codigo);
            } else {
                LOG.warning("⚠️ Intento de desactivar producto no existente - ID: " + id);
            }

        } catch (VentaException e) {
            LOG.severe("❌ Error de negocio al desactivar producto: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al desactivar producto: " + e.getMessage());
            throw new VentaException("Error interno al desactivar producto", "PROD_005");
        }
    }
}