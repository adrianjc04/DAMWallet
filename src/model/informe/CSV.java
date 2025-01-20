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
import java.util.List;
import model.Movimiento;

/**
 *
 * @author DAM2
 */
public class CSV extends Informe {

    public CSV(List<Movimiento> movimientos) {
        super(movimientos);
    }

    public void guardarDatosMensuales(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusMonths(1), "Último Mes");
    }

    public void guardarDatosAnuales(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusYears(1), "Último Año");
    }

    @Override
    public void guardarArchivo(File file, LocalDate limite, String titulo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Fecha,Concepto,Cantidad\n");

            for (Movimiento movimiento : movimientos) {
                if (movimiento.getFecha().isAfter(limite)) {
                    writer.write(movimiento.getFecha() + "," + movimiento.getConcepto() + "," + movimiento.getCantidad() + "\n");
                }
            }
        }
    }
}

