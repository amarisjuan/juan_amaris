package com.desarrolloweb.inventario.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.desarrolloweb.inventario.model.entity.Producto;
import com.desarrolloweb.inventario.service.InventarioServiceIface;

@Controller
@SessionAttributes("producto")
public class ProductoController {

    private final InventarioServiceIface inventarioService;

    public ProductoController(InventarioServiceIface inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/productonuevo")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String nuevoProducto(Model model) {
        model.addAttribute("titulo", "Nuevo producto");
        model.addAttribute("accion", "Agregar");
        model.addAttribute("producto", new Producto());
        model.addAttribute("proveedores", inventarioService.obtenerProveedores());
        return "producto/producto_form";
    }
    
    @GetMapping("/productos")
    public String mostrarProductos(Model model) {
        List<Producto> productos = inventarioService.obtenerProductos();
        model.addAttribute("titulo", "Listado de productos");
        model.addAttribute("productos", productos);
        return "producto/productos_listado";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String guardarProducto(@ModelAttribute Producto producto, Model model, SessionStatus status) {
        inventarioService.guardarProducto(producto);
        status.setComplete();
        return "redirect:/productos";
    }

}
