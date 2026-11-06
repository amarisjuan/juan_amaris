package com.juanamaris.inmobiliaria.model;

public class FiltroCompanero {
    private String ciudad;
    private Long universidad;
    private TipoBusquedaCompanero tipo;
    /** Mi género: oculta a quienes buscan a alguien de otro género. */
    private Genero soy;
    private Long presupuestoMax;

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public Long getUniversidad() { return universidad; }
    public void setUniversidad(Long universidad) { this.universidad = universidad; }
    public TipoBusquedaCompanero getTipo() { return tipo; }
    public void setTipo(TipoBusquedaCompanero tipo) { this.tipo = tipo; }
    public Genero getSoy() { return soy; }
    public void setSoy(Genero soy) { this.soy = soy; }
    public Long getPresupuestoMax() { return presupuestoMax; }
    public void setPresupuestoMax(Long presupuestoMax) { this.presupuestoMax = presupuestoMax; }
}
