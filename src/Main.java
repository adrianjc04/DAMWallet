
import javax.swing.*;
import controller.MovimientoController;
import java.io.FileNotFoundException;
import model.MovimientoDAO;
import model.config.Config;
import model.config.Configurable;
import view.MovimientoView;

public class Main {

    public static void main(String[] args) {
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        Configurable lastFileConfigurator = Config.LastFile.CONFIGURADOR;
        if (args.length > 1 || (args.length == 1 && !lastFileConfigurator.esValido(args[0]))) {
            System.out.println("DAMWallet - Uso: DAMWallet <Ruta archivo base de datos (.db)>");
            System.exit(1);
        } else if (args.length == 1 && !lastFileConfigurator.esValido(args[0])) {
            lastFileConfigurator.reescribirActual(args[0]);
        } else {
            try {
                if (!lastFileConfigurator.esValidoActual()) {
                    String ruta = lastFileConfigurator.seleccionarArchivoActual();
                    if (ruta != null) {
                        System.out.println("ruta seleccionada: "+ruta);
                        lastFileConfigurator.reescribirActual(ruta);
                        MovimientoDAO.rutaBBDD = ruta;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("No habia archivo de configuracion " + Config.LastFile.RUTA);
            }
        }
        try {
            if (lastFileConfigurator.esValidoActual()) {
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
                System.out.println("La configuracion en "+Config.LastFile.RUTA+" no es valida");
            }
        } catch (FileNotFoundException ex) {
            System.out.println("El archivo "+Config.LastFile.RUTA+" no tiene informacion valida.");
        }
    }
}
