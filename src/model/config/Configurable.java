package model.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * La interfaz Configurable define los métodos necesarios para la configuración de archivos 
 * en la aplicación. Incluye métodos para leer y escribir configuraciones, seleccionar archivos 
 * mediante un cuadro de diálogo, y verificar la validez de las configuraciones.
 * 
 * Además, ofrece métodos concretos para gestionar configuraciones específicas de la aplicación 
 * a través de la lectura, reescritura y selección de archivos de configuración.
 * 
 * @author DAM2
 */
public interface Configurable {

    /**
     * Lee el contenido de un archivo de configuración especificado por la configuración dada.
     * Cada línea del archivo es leída y almacenada en un arreglo de cadenas.
     * 
     * @param config la configuración que contiene la ruta del archivo a leer.
     * @return un arreglo de cadenas que contiene cada línea del archivo de configuración.
     */
    public static String[] leer(Config config){
        ArrayList<String> lineas = new ArrayList<>();
        try (BufferedReader archivo = new BufferedReader(new FileReader(config.RUTA))) {
            String linea;
            System.out.println("Contenido actual en " + config.RUTA + ":");
            while((linea = archivo.readLine()) != null) {
                System.out.println(linea);
                lineas.add(linea);
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo " + config.RUTA);
        }
        
        return lineas.toArray(String[]::new);
    }

    /**
     * Reescribe el archivo de configuración con las nuevas líneas especificadas en los argumentos.
     * 
     * @param config la configuración que contiene la ruta del archivo a reescribir.
     * @param args las líneas de texto a escribir en el archivo.
     * @return true si el archivo fue reescrito correctamente, false si ocurrió un error.
     */
    public static boolean reescribir(Config config, String... args){
        boolean cambiado = true;
        try (BufferedWriter archivo = new BufferedWriter(new FileWriter(config.RUTA))) {
            for (int i = 0; i < args.length; i++) {
                archivo.write(args[i] + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al reescribir el contenido en " + config.RUTA);
            cambiado = false;
        }
        return cambiado;
    }

    /**
     * Abre un cuadro de diálogo de selección de archivo, permitiendo al usuario elegir un archivo 
     * con una extensión específica. El cuadro de diálogo filtra los archivos para mostrar solo aquellos 
     * que coinciden con la extensión proporcionada.
     * 
     * @param descripcion la descripción del tipo de archivo (por ejemplo, "Archivo de base de datos").
     * @param extension la extensión de archivo que se desea filtrar (por ejemplo, "db").
     * @return la ruta del archivo seleccionado o null si el usuario cancela la operación.
     */
    public static String seleccionarArchivo(String descripcion, String extension) {
        String ruta = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(descripcion, extension));
        
        int resultado = fileChooser.showOpenDialog(null);
        File archivo = fileChooser.getSelectedFile();
        if (resultado == JFileChooser.APPROVE_OPTION && !archivo.isDirectory()) {
            ruta = archivo.getAbsolutePath();
        }
        
        return ruta;
    };

    /**
     * Método abstracto para verificar la validez de los argumentos proporcionados.
     * 
     * @param args los argumentos a verificar.
     * @return true si los argumentos son válidos, false si no lo son.
     */
    public abstract boolean esValido(String... args);

    /**
     * Método abstracto para verificar la validez del archivo de configuración actual.
     * 
     * @return true si el archivo de configuración actual es válido, false si no lo es.
     * @throws FileNotFoundException si el archivo de configuración no se encuentra.
     */
    public abstract boolean esValidoActual() throws FileNotFoundException;

    /**
     * Método abstracto para reescribir el archivo de configuración actual con los nuevos argumentos.
     * 
     * @param args las nuevas líneas que reemplazarán el contenido del archivo de configuración.
     * @return true si el archivo de configuración fue reescrito correctamente, false si ocurrió un error.
     */
    public abstract boolean reescribirActual(String... args);

    /**
     * Método abstracto para seleccionar un archivo de configuración actual mediante un cuadro de diálogo.
     * 
     * @return la ruta del archivo seleccionado o null si el usuario cancela la operación.
     */
    public abstract String seleccionarArchivoActual();
}