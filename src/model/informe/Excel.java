package model.informe;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import model.Movimiento;

/**
 *
 * @author DAM2
 */
class Excel extends Informe {

    public Excel(List<Movimiento> movimientos) {
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
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(titulo);

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Fecha");
        headerRow.createCell(1).setCellValue("Concepto");
        headerRow.createCell(2).setCellValue("Cantidad");

        int rowIndex = 1;
        for (Movimiento movimiento : movimientos) {
            if (movimiento.getFecha().isAfter(limite)) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(movimiento.getFecha().toString());
                row.createCell(1).setCellValue(movimiento.getConcepto());
                row.createCell(2).setCellValue(movimiento.getCantidad());
            }
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}