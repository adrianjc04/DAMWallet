package model;

import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * La clase Movimiento representa un movimiento financiero que incluye un concepto, 
 * una cantidad y una fecha asociada. Los movimientos pueden ser ingresos o gastos 
 * y están definidos por estos atributos.
 * 
 * Además, esta clase proporciona métodos para acceder y modificar los valores de los 
 * atributos y convertir la fecha a un objeto Date compatible con las bibliotecas antiguas.
 * 
 * @author DAM2
 */
public class Movimiento {

    // Atributos:
    private long id;
    private String concepto;
    private double cantidad;
    private LocalDate fecha;

    // Constructores:

    /**
     * Constructor para crear un objeto Movimiento con los valores específicos 
     * de id, concepto, cantidad y fecha.
     * 
     * @param id el identificador único del movimiento.
     * @param concepto el concepto que describe el movimiento.
     * @param cantidad la cantidad asociada al movimiento (positiva o negativa).
     * @param fecha la fecha en la que ocurrió el movimiento.
     */
    public Movimiento(long id, String concepto, double cantidad, LocalDate fecha) {
        this.id = id;
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    // Métodos:

    /**
     * Convierte la fecha de este movimiento de LocalDate a un objeto Date 
     * de la biblioteca java.util. Esto es útil para trabajar con APIs más antiguas 
     * que no aceptan LocalDate.
     * 
     * @return un objeto Date que representa la misma fecha que el LocalDate de este movimiento.
     */
    public Date fechaToDate() {
        return new GregorianCalendar(getFecha().getYear(), getFecha().getMonth().getValue() - 1, getFecha().getDayOfMonth()).getTime();
    }

    /**
     * Obtiene el identificador único de este movimiento.
     * 
     * @return el id del movimiento.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el identificador único para este movimiento.
     * 
     * @param id el id del movimiento.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene el concepto asociado a este movimiento.
     * 
     * @return el concepto del movimiento.
     */
    public String getConcepto() {
        return concepto;
    }

    /**
     * Establece el concepto para este movimiento.
     * 
     * @param concepto el concepto del movimiento.
     */
    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    /**
     * Obtiene la cantidad asociada a este movimiento.
     * 
     * @return la cantidad del movimiento.
     */
    public double getCantidad() {
        return cantidad;
    }

    /**
     * Establece la cantidad para este movimiento.
     * 
     * @param cantidad la cantidad del movimiento (puede ser positiva o negativa).
     */
    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Obtiene la fecha en la que ocurrió este movimiento.
     * 
     * @return la fecha del movimiento.
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha para este movimiento.
     * 
     * @param fecha la fecha en la que ocurrió el movimiento.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}