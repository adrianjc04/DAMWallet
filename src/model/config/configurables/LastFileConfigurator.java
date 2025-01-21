package model.config.configurables;

import java.io.File;
import model.config.Config;
import model.config.Configurable;

/**
 *
 * @author DAM2
 */
public class LastFileConfigurator implements Configurable {

    @Override
    public boolean esValido(String... args) {
        boolean esValido;
        try {
            File archivo = new File(args[0]);
            esValido = args[0] != null && !args[0].isEmpty() && archivo.exists();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("La configuracion propuesta para "+Config.LastFile.RUTA+" no es valida.");
            esValido = false;
        }
        return esValido;
    }

    @Override
    public boolean esValidoActual() {
        return esValido(Configurable.leer(Config.LastFile));
    }

    @Override
    public boolean reescribirActual(String... args) {
        return Configurable.reescribir(Config.LastFile, args);
    }

    @Override
    public String seleccionarArchivoActual() {
        return Configurable.seleccionarArchivo(Config.LastFile.DESCRIPCION_EXTENSION_ARCHIVO, Config.LastFile.EXTENSION_ARCHIVO);
    }
}
