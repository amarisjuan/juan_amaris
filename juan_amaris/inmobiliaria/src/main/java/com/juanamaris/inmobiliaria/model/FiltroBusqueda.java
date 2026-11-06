package com.juanamaris.inmobiliaria.model;

/** Parámetros que llegan del formulario de búsqueda. Todos son opcionales. */
public class FiltroBusqueda {
    private String ciudad;
    private String texto;
    private Operacion operacion;
    private TipoInmueble tipo;
    private Long precioMin;
    private Long precioMax;
    private Integer habitacionesMin;
    private Integer estrato;
    private String orden = "recientes";
    private Long universidad;
    private Integer maxMinutos;
    private Boolean amoblado;
    private Boolean serviciosIncluidos;
    /** "Busco vivienda para": excluye los inmuebles que no aceptan ese género. */
    private Genero para;
    private Boolean mascotas;

    public Genero getPara() { return para; }
    public void setPara(Genero para) { this.para = para; }
    public Boolean getMascotas() { return mascotas; }
    public void setMascotas(Boolean mascotas) { this.mascotas = mascotas; }

    public Long getUniversidad() { return universidad; }
    public void setUniversidad(Long universidad) { this.universidad = universidad; }
    public Integer getMaxMinutos() { return maxMinutos; }
    public void setMaxMinutos(Integer maxMinutos) { this.maxMinutos = maxMinutos; }
    public Boolean getAmoblado() { return amoblado; }
    public void setAmoblado(Boolean amoblado) { this.amoblado = amoblado; }
    public Boolean getServiciosIncluidos() { return serviciosIncluidos; }
    public void setServiciosIncluidos(Boolean serviciosIncluidos) { this.serviciosIncluidos = serviciosIncluidos; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public Operacion getOperacion() { return operacion; }
    public void setOperacion(Operacion operacion) { this.operacion = operacion; }
    public TipoInmueble getTipo() { return tipo; }
    public void setTipo(TipoInmueble tipo) { this.tipo = tipo; }
    public Long getPrecioMin() { return precioMin; }
    public void setPrecioMin(Long precioMin) { this.precioMin = precioMin; }
    public Long getPrecioMax() { return precioMax; }
    public void setPrecioMax(Long precioMax) { this.precioMax = precioMax; }
    public Integer getHabitacionesMin() { return habitacionesMin; }
    public void setHabitacionesMin(Integer habitacionesMin) { this.habitacionesMin = habitacionesMin; }
    public Integer getEstrato() { return estrato; }
    public void setEstrato(Integer estrato) { this.estrato = estrato; }
    public String getOrden() { return orden; }
    public void setOrden(String orden) { this.orden = orden; }
}
