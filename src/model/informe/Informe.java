package model.informe;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javafx.stage.FileChooser;
import model.Movimiento;

/**
 *
 * @author DAM2
 */
public abstract class Informe {

    protected List<Movimiento> movimientos;

    public Informe(List<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    public File seleccionarUbicacion(String titulo, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(titulo);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos " + extension.toUpperCase(), "*." + extension));
        return fileChooser.showSaveDialog(null);
    }

    public abstract void guardarArchivo(File file, LocalDate limite, String titulo) throws IOException;
}
