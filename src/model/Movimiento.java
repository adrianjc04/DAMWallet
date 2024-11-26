package model;

import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author DAM2
 */
public class Movimiento {

	//Atributos:
	private long id;
	private String concepto;
	private double cantidad;
	private LocalDate fecha;

	//Constructores:
	public Movimiento(long id, String concepto, double cantidad, LocalDate fecha) {
		this.id = id;
		this.concepto = concepto;
		this.cantidad = cantidad;
		this.fecha = fecha;
	}//Fin Constructor
	//Métodos:
	
	public Date fechaToDate() {
		return new GregorianCalendar(getFecha().getYear(), getFecha().getMonth().getValue() - 1, getFecha().getDayOfMonth()).getTime();
	}//Fin Función

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the concepto
	 */
	public String getConcepto() {
		return concepto;
	}

	/**
	 * @param concepto the concepto to set
	 */
	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	/**
	 * @return the cantidad
	 */
	public double getCantidad() {
		return cantidad;
	}

	/**
	 * @param cantidad the cantidad to set
	 */
	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	/**
	 * @return the fecha
	 */
	public LocalDate getFecha() {
		return fecha;
	}

	/**
	 * @param fecha the fecha to set
	 */
	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}
}
