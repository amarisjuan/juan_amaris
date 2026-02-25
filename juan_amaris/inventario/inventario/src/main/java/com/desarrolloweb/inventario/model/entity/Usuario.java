package com.desarrolloweb.inventario.model.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(length = 50, unique = true, nullable = false)
   private String username;

   @Column(length = 128, nullable = false)
   private String password;

   private boolean activo = true;

   @ManyToMany(fetch = FetchType.EAGER)
   @JoinTable(name = "usuarios_roles",
         joinColumns = @JoinColumn(name = "usuario_id"),
         inverseJoinColumns = @JoinColumn(name = "rol_id"))
   private List<Rol> roles;

   public Usuario() {
   }

   public Usuario(Long id, String username, String password, boolean activo, List<Rol> roles) {
      this.id = id;
      this.username = username;
      this.password = password;
      this.activo = activo;
      this.roles = roles;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public boolean isActivo() {
      return activo;
   }

   public void setActivo(boolean activo) {
      this.activo = activo;
   }

   public List<Rol> getRoles() {
      return roles;
   }

   public void setRoles(List<Rol> roles) {
      this.roles = roles;
   }

}



    

