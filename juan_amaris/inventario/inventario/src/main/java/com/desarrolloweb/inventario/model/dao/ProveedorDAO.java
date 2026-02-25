package com.desarrolloweb.inventario.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.desarrolloweb.inventario.model.entity.Proveedor;

public interface ProveedorDAO extends JpaRepository<Proveedor, Long>{
    
}
