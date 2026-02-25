package com.desarrolloweb.inventario.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.desarrolloweb.inventario.model.entity.Producto;

public interface ProductoDAO extends JpaRepository<Producto, Long>{
    
}
