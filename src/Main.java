
import javax.swing.*;
import controller.MovimientoController;
import model.MovimientoDAO;
import view.MovimientoView;

public class Main {

    public static void main(String[] args) {
        // Crear la base de datos si no existe
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

    }
}
