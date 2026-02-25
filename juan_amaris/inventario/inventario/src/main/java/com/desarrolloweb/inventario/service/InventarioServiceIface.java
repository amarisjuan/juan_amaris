package com.desarrolloweb.inventario.service;

import java.util.List;

import com.desarrolloweb.inventario.model.entity.Producto;
import com.desarrolloweb.inventario.model.entity.Proveedor;

public interface InventarioServiceIface {
    public List<Proveedor> obtenerProveedores();
    public Proveedor obtenerProveedorPorId(Long id);

    public List<Producto> obtenerProductos();
    public Producto obtenerProductoPorId(Long id);
    public void guardarProducto(Producto producto);
}
