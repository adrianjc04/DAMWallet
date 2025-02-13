package model.config.configurables;

import java.io.File;
import model.config.Config;
import model.config.Configurable;

/**
 * La clase LastFileConfigurator implementa la interfaz Configurable para gestionar la 
 * configuración específica del último archivo utilizado en la aplicación. Esta clase 
 * permite verificar si la configuración es válida, reescribir el archivo de configuración 
 * y seleccionar un archivo mediante un cuadro de diálogo.
 * 
 * @author DAM2
 */
public class LastFileConfigurator implements Configurable {

    /**
     * Verifica si la configuración propuesta (en este caso, la ruta del archivo) es válida.
     * La configuración es válida si la ruta del archivo no es nula ni vacía y el archivo 
     * especificado existe en el sistema.
     * 
     * @param args los argumentos que contienen la ruta del archivo de configuración.
     * @return true si la ruta del archivo es válida, false si no lo es.
     */
    @Override
    public boolean esValido(String... args) {
        boolean esValido;
        try {
            File archivo = new File(args[0]);
            esValido = args[0] != null && !args[0].isEmpty() && archivo.exists();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("La configuración propuesta para " + Config.LastFile.RUTA + " no es válida.");
            esValido = false;
        }
        return esValido;
    }

    /**
     * Verifica si la configuración actual (el último archivo utilizado) es válida.
     * Llama al método `esValido` de la interfaz Configurable para validar la ruta del archivo.
     * 
     * @return true si la configuración actual es válida, false si no lo es.
     */
    @Override
    public boolean esValidoActual() {
        return esValido(Configurable.leer(Config.LastFile));
    }

    /**
     * Reescribe el archivo de configuración actual con los nuevos argumentos proporcionados.
     * Utiliza el método `reescribir` de la interfaz Configurable para actualizar el archivo 
     * con la nueva ruta del archivo.
     * 
     * @param args los nuevos argumentos (ruta del archivo) a escribir en el archivo de configuración.
     * @return true si el archivo fue reescrito correctamente, false si ocurrió un error.
     */
    @Override
    public boolean reescribirActual(String... args) {
        return Configurable.reescribir(Config.LastFile, args);
    }

    /**
     * Abre un cuadro de diálogo para seleccionar un archivo de configuración. 
     * Filtra los archivos para permitir solo aquellos que tengan la extensión definida 
     * en la configuración (por ejemplo, archivos de base de datos).
     * 
     * @return la ruta del archivo seleccionado, o null si el usuario cancela la operación.
     */
    @Override
    public String seleccionarArchivoActual() {
        return Configurable.seleccionarArchivo(Config.LastFile.DESCRIPCION_EXTENSION_ARCHIVO, Config.LastFile.EXTENSION_ARCHIVO);
    }
}
