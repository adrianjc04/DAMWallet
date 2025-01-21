package model.config;

import java.io.File;
import model.config.configurables.LastFileConfigurator;

/**
 *
 * @author DAM2
 */
public enum Config {
    LastFile("config" + File.separator + "ultimaRuta.txt", "Archivo de base de datos (.db)", "db", new LastFileConfigurator());

    private Config(String ruta, String descripcionExtensionArchivo, String extensionArchivo, Configurable configurador) {
        this.RUTA = ruta;
        this.DESCRIPCION_EXTENSION_ARCHIVO = descripcionExtensionArchivo;
        this.EXTENSION_ARCHIVO = extensionArchivo;
        this.CONFIGURADOR = configurador;
        try {
            new File(ruta).getParentFile().mkdirs();
        } catch (NullPointerException e) {
        }
    }

    public final String RUTA;
    public final String DESCRIPCION_EXTENSION_ARCHIVO;
    public final String EXTENSION_ARCHIVO;
    public final Configurable CONFIGURADOR;
}
