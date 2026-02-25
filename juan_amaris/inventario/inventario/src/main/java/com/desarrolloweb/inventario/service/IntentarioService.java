package com.desarrolloweb.inventario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrolloweb.inventario.model.dao.ProductoDAO;
import com.desarrolloweb.inventario.model.dao.ProveedorDAO;
import com.desarrolloweb.inventario.model.entity.Producto;
import com.desarrolloweb.inventario.model.entity.Proveedor;

@Service
public class IntentarioService implements InventarioServiceIface{

    private final ProductoDAO productoDAO;
    private final ProveedorDAO proveedorDAO;

    public IntentarioService(ProductoDAO productoDAO, ProveedorDAO proveedorDAO) {
        this.productoDAO = productoDAO;
        this.proveedorDAO = proveedorDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> obtenerProveedores() {
        return proveedorDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor obtenerProveedorPorId(Long id) {
        return proveedorDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductos() {
        return productoDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Long id) {
        return productoDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void guardarProducto(Producto producto) {
        productoDAO.save(producto);
    }
    
}
