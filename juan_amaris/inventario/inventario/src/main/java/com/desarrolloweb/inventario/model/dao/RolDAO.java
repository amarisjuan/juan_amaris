package com.desarrolloweb.inventario.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.desarrolloweb.inventario.model.entity.Rol;

public interface RolDAO extends JpaRepository<Rol, Long> {
    Rol findByNombre(String nombre);
}
