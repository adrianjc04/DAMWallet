
import javax.swing.*;
import controller.MovimientoController;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.MovimientoDAO;
import view.MovimientoView;

public class Main {

    public static final String RUTA_ULTIMO_ARCHIVO = "config" + File.separator + "ultimaRuta.txt";

    public static void main(String[] args) {
        String ultimaRuta = null;
        BufferedReader ultimaRutaTXT = null;
        try {
            ultimaRutaTXT = new BufferedReader(new FileReader(RUTA_ULTIMO_ARCHIVO));
            try {
                ultimaRuta = ultimaRutaTXT.readLine();
                if (ultimaRuta == null || ultimaRuta.trim().isEmpty() || new File(ultimaRuta).isDirectory()) {
                    ultimaRuta = null;
                } else {
                    args = new String[]{ultimaRuta};
                }
            } catch (IOException ex) {
                System.out.println("Error al leer la ultima ruta en " + RUTA_ULTIMO_ARCHIVO);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("No habia último archivo seleccionado");
        } finally {
            if (ultimaRutaTXT != null) {
                try {
                    ultimaRutaTXT.close();
                } catch (IOException ex) {
                    System.out.println("Error al cerrar el archivo " + RUTA_ULTIMO_ARCHIVO);
                }
            }
        }
        if (args.length > 1) {
            System.out.println("DAMWallet - Uso: DAMWallet <rutaBBDD>");
            System.exit(1);
        }
        if (args.length == 0 && ultimaRuta == null) {
            JFileChooser selector = new JFileChooser();
            selector.setFileFilter(new FileNameExtensionFilter("Archivos de base de datos (.db)", "db"));
            int resultado = selector.showOpenDialog(null);

            if (resultado == JFileChooser.APPROVE_OPTION) {
                String rutaSeleccionada = selector.getSelectedFile().getAbsolutePath();
                args = new String[]{rutaSeleccionada};

                try {
                    new File(RUTA_ULTIMO_ARCHIVO).getParentFile().mkdirs();
                } catch (NullPointerException e) {
                }

                BufferedWriter archivo = null;

                try {
                    archivo = new BufferedWriter(new FileWriter(RUTA_ULTIMO_ARCHIVO));
                    archivo.write(rutaSeleccionada);
                } catch (IOException e) {
                    System.out.println("Error al guardar la ruta del último archivo seleccionado " + RUTA_ULTIMO_ARCHIVO);
                } finally {
                    if (archivo != null) {
                        try {
                            archivo.close();
                        } catch (IOException e) {
                            System.out.println("Error al cerrar el archivo " + RUTA_ULTIMO_ARCHIVO);
                        }
                    }
                }
            }
        }
        if (args.length == 1) {
            // Crear la base de datos si no existe
            MovimientoDAO.rutaBBDD = args[0]
                    .replaceAll("\\\\", "\\" + File.separator)
                    .replaceAll("/", "\\" + File.separator);
            if (!MovimientoDAO.crearBaseDeDatos()) {
                JOptionPane.showMessageDialog(null, "Error al crear o conectar con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Iniciar la interfaz gráfica en el hilo de despacho de eventos
            SwingUtilities.invokeLater(() -> {
                MovimientoView view = new MovimientoView();
                MovimientoController controller = new MovimientoController(view);

                controller.addObserver(view); // Registrar la vista como observadora
                view.setVisible(true);
            });
        } else {
            System.out.println("DAMWallet - Uso: DAMWallet <rutaBBDD>");
            System.exit(1);
        }

    }
}
