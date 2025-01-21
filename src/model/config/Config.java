package model.config;

import java.io.File;

/**
 *
 * @author DAM2
 */
public enum Config {
    LastFile("config" + File.separator + "ultimaRuta.txt",new Configurable() {
        @Override
        public boolean esValido(String... args) {
            File archivo = new File(args[0]);
            return args[0] != null && !args[0].isEmpty() && !archivo.exists();
        }

        @Override
        public boolean esValidoActual(String nombre) {
            return esValido(Configurable.leer(Config.LastFile));
        }
    });

    private Config(String ruta, Configurable configurador) {
        this.ruta = ruta;
        this.configurador = configurador;
    }
    
    public final String ruta;
    public final Configurable configurador;
    
}
