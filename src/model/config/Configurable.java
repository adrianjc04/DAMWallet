
package model.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author DAM2
 */

public interface Configurable {
    
    public static String[] leer(String ruta){
        ArrayList<String> lineas = new ArrayList<>();
        try (BufferedReader archivo = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while((linea = archivo.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo "+ruta);
        }
        
        return lineas.toArray(String[]::new);
    }
    
    public abstract boolean esValido(String... args);
    
    public abstract boolean esValidoActual();
}
