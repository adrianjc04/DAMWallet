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
            return args[0] != null && !args[0].isEmpty() && !archivo.exists();
        }

        @Override
        public boolean esValidoActual(String nombre) {
            return esValido(Configurable.leer(NAMESPACE + nombre));
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
