
package model.config;

import java.io.File;

/**
 *
 * @author DAM2
 */
public enum Config {
    LastFile("ultimaRuta.txt", new Configurable() {
        @Override
        public boolean esValido(String... args) {
            File archivo = new File(args[0]);
            return !args[0].isEmpty() || archivo;
        }

        @Override
        public boolean esValidoActual() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    });

    private Config(String nombre, Configurable configurador) {
        this.fileName = nombre;
        this.configurador = configurador;
    }
    
    public static final String NAMESPACE = "config"+File.separator;
    public final String fileName;
    public final Configurable configurador;
    
}
