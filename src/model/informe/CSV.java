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
 * La clase CSV es responsable de generar y guardar archivos CSV que contienen los movimientos 
 * financieros en función de un período específico (mensual o anual). Los datos se almacenan 
 * en formato CSV, con las columnas de fecha, concepto y cantidad.
 * 
 * Esta clase permite guardar los datos de los movimientos financieros de los últimos 
 * meses o años en un archivo CSV, lo que facilita la exportación y el análisis posterior de los datos.
 * 
 * @author DAM2
 */
public class CSV {

    private List<Movimiento> movimientos;

    /**
     * Constructor que inicializa la lista de movimientos que se utilizará para generar 
     * los archivos CSV.
     * 
     * @param movimientos la lista de movimientos financieros a exportar.
     */
    public CSV(List<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    /**
     * Guarda los movimientos correspondientes al último mes en un archivo CSV.
     * Llama al método `guardarArchivo` con los parámetros adecuados para el último mes.
     * 
     * @param file el archivo donde se guardarán los datos.
     * @throws IOException si ocurre un error al escribir el archivo.
     */
    public void guardarDatosMensuales(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusMonths(1), "Último Mes");
    }

    /**
     * Guarda los movimientos correspondientes al último año en un archivo CSV.
     * Llama al método `guardarArchivo` con los parámetros adecuados para el último año.
     * 
     * @param file el archivo donde se guardarán los datos.
     * @throws IOException si ocurre un error al escribir el archivo.
     */
    public void guardarDatosAnuales(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusYears(1), "Último Año");
    }

    /**
     * Guarda los movimientos en un archivo CSV, aplicando un límite de fecha (mensual o anual) 
     * y utilizando un título específico para la exportación.
     * 
     * @param file el archivo donde se guardarán los datos.
     * @param limite la fecha límite para filtrar los movimientos a exportar.
     * @param titulo el título que se usará en el archivo CSV.
     * @throws IOException si ocurre un error al escribir el archivo.
     */
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
