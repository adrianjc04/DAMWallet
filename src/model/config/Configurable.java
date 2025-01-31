
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
 *
 * @author DAM2
 */

public interface Configurable {
    
    public static String[] leer(Config config){
        ArrayList<String> lineas = new ArrayList<>();
        try (BufferedReader archivo = new BufferedReader(new FileReader(config.RUTA))) {
            String linea;
            System.out.println("Contenido actual en "+config.RUTA+":");
            while((linea = archivo.readLine()) != null) {
                System.out.println(linea);
                lineas.add(linea);
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo "+config.RUTA);
        }
        
        return lineas.toArray(String[]::new);
    }
    
    public static boolean reescribir(Config config, String... args){
        boolean cambiado = true;
        try (BufferedWriter archivo = new BufferedWriter(new FileWriter(config.RUTA))) {
            for (int i = 0; i < args.length; i++) {
                archivo.write(args[i]+"\n");
            }
        } catch (IOException e) {
            System.out.println("Error al reescribir el contenido en "+config.RUTA);
            cambiado = false;
        }
        return cambiado;
    }
    
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
    
    public abstract boolean esValido(String... args);
    
    public abstract boolean esValidoActual() throws FileNotFoundException;
    
    public abstract boolean reescribirActual(String... args);
    
    public abstract String seleccionarArchivoActual();
}
