package model.config;

import java.io.File;
import model.config.configurables.LastFileConfigurator;

/**
 * La clase Config es un enumerado que define configuraciones específicas para la aplicación. 
 * Cada valor en este enumerado representa una configuración, incluyendo la ruta del archivo, 
 * la descripción de la extensión del archivo, la extensión de archivo y un configurador específico 
 * para gestionar la configuración de ese valor.
 * 
 * Esta clase se utiliza para almacenar y gestionar configuraciones relacionadas con los archivos 
 * de la aplicación, como la ubicación del último archivo utilizado.
 * 
 * @author DAM2
 */
public enum Config {
    
    /**
     * Configuración para la última ruta de archivo utilizada, incluyendo la ruta del archivo, 
     * una descripción de la extensión y el configurador asociado.
     */
    LastFile("config" + File.separator + "ultimaRuta.txt", "Archivo de base de datos (.db)", "db", new LastFileConfigurator());

    // Atributos de configuración
    public final String RUTA;
    public final String DESCRIPCION_EXTENSION_ARCHIVO;
    public final String EXTENSION_ARCHIVO;
    public final Configurable CONFIGURADOR;

    /**
     * Constructor privado del enumerado Config. 
     * Se inicializan los atributos relacionados con la configuración, y se asegura que 
     * la carpeta que contiene el archivo de configuración exista, creando la carpeta si es necesario.
     * 
     * @param ruta la ruta del archivo de configuración.
     * @param descripcionExtensionArchivo una descripción de la extensión del archivo.
     * @param extensionArchivo la extensión del archivo (por ejemplo, "db").
     * @param configurador el objeto que se utilizará para configurar la aplicación con esta configuración.
     */
    private Config(String ruta, String descripcionExtensionArchivo, String extensionArchivo, Configurable configurador) {
        this.RUTA = ruta;
        this.DESCRIPCION_EXTENSION_ARCHIVO = descripcionExtensionArchivo;
        this.EXTENSION_ARCHIVO = extensionArchivo;
        this.CONFIGURADOR = configurador;
        
        // Asegurar que la carpeta del archivo exista
        try {
            new File(ruta).getParentFile().mkdirs();
        } catch (NullPointerException e) {
            // Si ocurre un error, no se interrumpe el proceso
        }
    }
}