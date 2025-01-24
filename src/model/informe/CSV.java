/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.informe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.Movimiento;

/**
 *
 * @author DAM2
 */
public class CSV {

    private List<Movimiento> movimientos;

    public CSV(List<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    public void guardarDatosMensuales(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusMonths(1), "Último Mes");
    }

    public void guardarDatosAnuales(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusYears(1), "Último Año");
    }

    public void guardarArchivo(File file, LocalDate limite, String titulo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Escribir encabezado con formato adecuado
            writer.write("Fecha,Concepto,Cantidad\n");

            // Formato de fecha para hacerlo más legible
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Iterar sobre los movimientos y escribir solo los que son posteriores al límite
            for (Movimiento movimiento : movimientos) {
                if (movimiento.getFecha().isAfter(limite)) {
                    String formattedDate = movimiento.getFecha().format(dateFormatter);
                    writer.write(String.format("%s,%s,%.2f\n", formattedDate, movimiento.getConcepto(), movimiento.getCantidad()));
                }
            }
        }
    }
}

