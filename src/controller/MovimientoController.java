package controller;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.*;
import model.Movimiento;
import model.MovimientoDAO;
import observer.BalanceObserver;
import view.MovimientoView;

public class MovimientoController {

    private MovimientoView view;
    private String currentFilter = "Total";
    private boolean inHelpMode = false;
    private int helpStep = 0;

    private Stack<Movimiento> deletedMovimientos = new Stack<>();
    private List<BalanceObserver> observers = new ArrayList<>();

    public MovimientoController(MovimientoView view) {
        this.view = view;
        this.view.setController(this);

        this.view.addAddButtonListener(new AddButtonListener());
        this.view.addHelpButtonListener(new HelpButtonListener());
        this.view.addFilterLabelListener(new MesLabelListener(), new AñoLabelListener(), new TotalLabelListener());
        this.view.addContinueButtonListener(new ContinueButtonListener());

        // Agregar Key Bindings
        this.view.addHelpKeyBinding(new HelpKeyAction());
        this.view.addTabKeyBinding(new TabKeyAction());
        this.view.addCtrlNKeyBinding(new CtrlNKeyAction());
        this.view.addCtrlZKeyBinding(new CtrlZKeyAction());

        loadData(); // Carga inicial de datos

        // Forzar notificación del balance al iniciar
        double initialBalance = calculateTotalBalance();
        notifyBalanceChange(initialBalance);
    }

    private double calculateTotalBalance() {
        String selectQuery = "SELECT * FROM MOVIMIENTO";
        Movimiento[] movimientos = MovimientoDAO.leerMovimientos(selectQuery);

        double totalBalance = 0.0;
        for (Movimiento movimiento : movimientos) {
            totalBalance += movimiento.getCantidad();
        }
        return totalBalance;
    }

    public void addObserver(BalanceObserver observer) {
        observers.add(observer); // Añadir un observador
    }

    public void removeObserver(BalanceObserver observer) {
        observers.remove(observer); // Eliminar un observador
    }

    private void notifyBalanceChange(double balance) {
        for (BalanceObserver observer : observers) {
            observer.onBalanceChange(balance); // Notificar a cada observador del cambio
        }
    }

    private void loadData() {
        if (inHelpMode) {
            return;
        }

        String selectQuery = "SELECT * FROM MOVIMIENTO";
        switch (currentFilter) {
            case "Mes":
                selectQuery += " WHERE FECHA >= (strftime('%s', 'now', '-30 days') * 1000)";
                break;
            case "Año":
                selectQuery += " WHERE FECHA >= (strftime('%s', 'now', '-365 days') * 1000)";
                break;
            default:
                break;
        }
        selectQuery += " ORDER BY FECHA DESC";

        Movimiento[] movimientos = MovimientoDAO.leerMovimientos(selectQuery);

        double totalBalance = 0.0;
        for (Movimiento movimiento : movimientos) {
            totalBalance += movimiento.getCantidad();
        }

        notifyBalanceChange(totalBalance);
        view.setBalance(totalBalance);

        // Cambiar color del banner y filtros si el balance es negativo
        if (totalBalance < 0) {
            view.updateBannerAndFiltersColor("#d63429"); // Rojo
        } else {
            view.updateBannerAndFiltersColor("#123EAF"); // Azul original
        }

        view.setMovements(movimientos);
    }

    public void deleteMovimiento(long id) {
        // Obtener el movimiento antes de eliminarlo
        Movimiento movimiento = MovimientoDAO.obtenerMovimientoPorId(id);
        if (movimiento != null) {
            boolean success = MovimientoDAO.borrarMovimiento(id);
            if (success) {
                // Añadir a la pila de eliminados
                deletedMovimientos.push(movimiento);
                loadData();
            } else {
                JOptionPane.showMessageDialog(view, "Error al eliminar el movimiento.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(view, "Movimiento no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void undoDeleteMovimiento() {
        if (!deletedMovimientos.isEmpty()) {
            Movimiento movimiento = deletedMovimientos.pop();
            boolean success = MovimientoDAO.escribirMovimiento(movimiento);
            if (success) {
                loadData();
                JOptionPane.showMessageDialog(view, "Movimiento restaurado exitosamente.");
            } else {
                JOptionPane.showMessageDialog(view, "Error al restaurar el movimiento.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método para cambiar al siguiente filtro
    public void cycleFilter() {
        if (!inHelpMode) {
            switch (currentFilter) {
                case "Total":
                    currentFilter = "Mes";
                    view.highlightSelectedFilter(view.getMesLabel(), view.getAñoLabel(), view.getTotalLabel());
                    break;
                case "Mes":
                    currentFilter = "Año";
                    view.highlightSelectedFilter(view.getAñoLabel(), view.getMesLabel(), view.getTotalLabel());
                    break;
                case "Año":
                    currentFilter = "Total";
                    view.highlightSelectedFilter(view.getTotalLabel(), view.getMesLabel(), view.getAñoLabel());
                    break;
                default:
                    currentFilter = "Mes";
                    view.highlightSelectedFilter(view.getMesLabel(), view.getAñoLabel(), view.getTotalLabel());
                    break;
            }
            loadData();
        }
    }

    // Método público para activar la ayuda desde la vista
    public void triggerHelp() {
        if (!inHelpMode) {
            startHelp();
        }
    }

    public void addMovimiento() {
        JTextField conceptoField = new JTextField();
        JTextField cantidadField = new JTextField();
        JTextField fechaField = new JTextField(LocalDate.now().toString());

        // Crear radio buttons
        JRadioButton ingresoRadioButton = new JRadioButton("Ingreso");
        JRadioButton gastoRadioButton = new JRadioButton("Gasto");

        // Crear un ButtonGroup para que sean mutuamente exclusivos
        ButtonGroup tipoGroup = new ButtonGroup();
        tipoGroup.add(ingresoRadioButton);
        tipoGroup.add(gastoRadioButton);

        // Seleccionar "Ingreso" por defecto
        ingresoRadioButton.setSelected(true);

        // Crear un panel para organizar los componentes
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Crear etiquetas y alinearlas a la izquierda
        JLabel conceptoLabel = new JLabel("Concepto:");
        conceptoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        conceptoField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(conceptoLabel);
        panel.add(conceptoField);

        JLabel tipoLabel = new JLabel("Tipo:");
        tipoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tipoLabel);

        JPanel tipoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tipoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ingresoRadioButton.setFocusTraversalKeysEnabled(false);
        gastoRadioButton.setFocusTraversalKeysEnabled(false);
        tipoPanel.add(ingresoRadioButton);
        tipoPanel.add(gastoRadioButton);
        panel.add(tipoPanel);

        JLabel cantidadLabel = new JLabel("Cantidad:");
        cantidadLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cantidadField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cantidadLabel);
        panel.add(cantidadField);

        // Evitar que se introduzcan números negativos en cantidadField
        cantidadField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Permitir sólo dígitos y el punto decimal
                if (!Character.isDigit(c) && c != '.') {
                    e.consume();
                }
            }
        });

        JLabel fechaLabel = new JLabel("Fecha (YYYY-MM-DD):");
        fechaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fechaField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fechaLabel);
        panel.add(fechaField);

        // Crear un JOptionPane personalizado
        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION
        );

        // Convertir JOptionPane a JDialog para mayor control
        JDialog dialog = optionPane.createDialog(view, "Agregar Movimiento");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Solicitar el foco en el campo "Concepto" cuando el diálogo se abre
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                conceptoField.requestFocusInWindow();
            }
        });

        // Lista de componentes para navegación con teclas
        List<Component> components = new ArrayList<>();
        components.add(conceptoField);
        components.add(null); // Placeholder para los radio buttons
        components.add(cantidadField);
        components.add(fechaField);

        // Agregar KeyListeners para navegación con flechas
        for (int i = 0; i < components.size(); i++) {
            Component comp = components.get(i);
            int index = i;

            if (comp != null) {
                comp.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        int keyCode = e.getKeyCode();
                        if (keyCode == KeyEvent.VK_UP) {
                            int prevIndex = (index - 1 + components.size()) % components.size();
                            if (prevIndex == 1) {
                                // Mover el foco al radio button seleccionado
                                if (ingresoRadioButton.isSelected()) {
                                    ingresoRadioButton.requestFocusInWindow();
                                } else {
                                    gastoRadioButton.requestFocusInWindow();
                                }
                            } else {
                                components.get(prevIndex).requestFocusInWindow();
                            }
                            e.consume();
                        } else if (keyCode == KeyEvent.VK_DOWN) {
                            int nextIndex = (index + 1) % components.size();
                            if (nextIndex == 1) {
                                // Mover el foco al radio button seleccionado
                                if (ingresoRadioButton.isSelected()) {
                                    ingresoRadioButton.requestFocusInWindow();
                                } else {
                                    gastoRadioButton.requestFocusInWindow();
                                }
                            } else {
                                components.get(nextIndex).requestFocusInWindow();
                            }
                            e.consume();
                        }
                    }
                });
            }
        }

        // Agregar KeyListeners a los radio buttons
        ingresoRadioButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) {
                    conceptoField.requestFocusInWindow();
                    e.consume();
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    cantidadField.requestFocusInWindow();
                    e.consume();
                } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                    gastoRadioButton.setSelected(true);
                    gastoRadioButton.requestFocusInWindow();
                    e.consume();
                }
            }
        });

        gastoRadioButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) {
                    conceptoField.requestFocusInWindow();
                    e.consume();
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    cantidadField.requestFocusInWindow();
                    e.consume();
                } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                    ingresoRadioButton.setSelected(true);
                    ingresoRadioButton.requestFocusInWindow();
                    e.consume();
                }
            }
        });

        dialog.setVisible(true);

        Object selectedValue = optionPane.getValue();
        int option = JOptionPane.CLOSED_OPTION;
        if (selectedValue != null && selectedValue instanceof Integer) {
            option = (Integer) selectedValue;
        }
        Movimiento movimiento = null;
        if (option == JOptionPane.OK_OPTION) {
            try {
                String concepto = conceptoField.getText().trim();
                String cantidadStr = cantidadField.getText().trim();
                if (cantidadStr.isEmpty()) {
                    throw new NumberFormatException("La cantidad no puede estar vacía.");
                }
                double cantidad = Double.parseDouble(cantidadStr);
                String dateString = fechaField.getText().trim();

                // Verificar si se seleccionó un tipo
                if (!ingresoRadioButton.isSelected() && !gastoRadioButton.isSelected()) {
                    JOptionPane.showMessageDialog(view, "Debe seleccionar un tipo (Ingreso o Gasto).", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Ajustar la cantidad según el tipo seleccionado
                if (gastoRadioButton.isSelected()) {
                    // Si es gasto, convertir a negativo
                    cantidad = -Math.abs(cantidad);
                } else {
                    // Si es ingreso, asegurar que sea positivo
                    cantidad = Math.abs(cantidad);
                }

                // Crear un DateTimeFormatter que acepte meses y días de uno o dos dígitos
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

                // Analizar la fecha con el formatter personalizado
                LocalDate fecha = LocalDate.parse(dateString, formatter);

                String tipoMovimiento = (gastoRadioButton.isSelected()) ? "Gasto" : "Ingreso";
                String formattedFecha = fecha.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                int confirmOption = view.showAddMovementConfirmDialog(concepto, tipoMovimiento, cantidad, formattedFecha);

                if (confirmOption != JOptionPane.YES_OPTION) {
                    return; // Salir si el usuario no confirma
                }

                movimiento = new Movimiento(0, concepto, cantidad, fecha);
                boolean success = MovimientoDAO.escribirMovimiento(movimiento);
                if (success) {
                    JOptionPane.showMessageDialog(view, "Movimiento agregado exitosamente.");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(view, "Error al agregar Movimiento.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(view, "La cantidad debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Entrada inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class AddButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!inHelpMode) {
                addMovimiento();
            }
        }
    }

    private class HelpButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            startHelp();
        }
    }

    // Acción para la tecla F1
    private class HelpKeyAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (inHelpMode) {
                // Si estamos en modo ayuda, avanzamos al siguiente paso
                nextHelpStep();
            } else {
                // Si no estamos en modo ayuda, iniciamos la guía
                triggerHelp();
            }
        }
    }

    // Acción para la tecla Tab
    private class TabKeyAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            cycleFilter();
        }
    }

    // Acción para la combinación Ctrl+N
    private class CtrlNKeyAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!inHelpMode) {
                addMovimiento();
            }
        }
    }

    // Acción para la combinación Ctrl+Z
    private class CtrlZKeyAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!inHelpMode) {
                undoDeleteMovimiento();
            }
        }
    }

    private void startHelp() {
        inHelpMode = true;
        helpStep = 1;
        view.setHelpStep(helpStep);
        view.enterHelpMode();

        String text = "Muestra tu balance correspondiente a lo ingresado menos lo gastado.";
        String[] splitText = splitTextInHalf(text);
        view.updateHelpText(splitText[0], splitText[1]);
        view.showArrowAtStep(helpStep);
    }

    private class ContinueButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            nextHelpStep();
        }
    }

    private void nextHelpStep() {
        helpStep++;
        view.setHelpStep(helpStep);
        String text;
        String[] splitText;
        switch (helpStep) {
            case 2:
                text = "Muestra los gastos/ingresos en los últimos 30 días y el balance correspondiente.";
                splitText = splitTextInHalf(text);
                view.updateHelpText(splitText[0], splitText[1]);
                view.showArrowAtStep(helpStep);
                break;
            case 3:
                text = "Muestra los gastos/ingresos en los últimos 365 días y el balance correspondiente.";
                splitText = splitTextInHalf(text);
                view.updateHelpText(splitText[0], splitText[1]);
                view.showArrowAtStep(helpStep);
                break;
            case 4:
                text = "Permite añadir un ingreso/gasto con su respectivo concepto y fecha.";
                splitText = splitTextInHalf(text);
                view.updateHelpText(splitText[0], splitText[1]);
                view.showArrowAtStep(helpStep);
                break;
            case 5:
                text = "Atajos de teclado:";
                splitText = splitTextInHalf(text);
                view.updateHelpText(splitText[0],
                        splitText[1],
                        "1.Pagina Principal:",
                        " ",
                        "	-Up/Down por primera vez:",
                        "	selecciona la primera fila.",
                        " ",
                        "	-Up/Down por segunda vez:",
                        "	selecciona el movimiento de",
                        "	arriba/abajo si lo hubiese.",
                        " ",
                        "	-Supr",
                        "	(mientras se selecciona una fila con up/down):",
                        "	pregunta si se desea eliminar el movimiento.",
                        " ",
                        "	-ctrl+n: abre la pestaña de nuevo movimiento.",
                        " ",
                        "	-F1: abre la sección de ayuda.",
                        " ",
                        "2.Cualquier modal:",
                        " ",
                        "	-Esc: sale del modal.",
                        " ",
                        "	-Enter: realiza la acción del proposito del",
                        "	modal si la respuesta fuese afirmativa.",
                        " ",
                        "	-up/down/left/right: permite moverse",
                        "	entre los elementos del modal.",
                        " ",
                        "	-Tab: permite moverse entre los elementos",
                        "	del modal en orden por defecto.",
                        " ",
                        "3.Agregar Movimiento:",
                        " ",
                        "	-left/right (si esta seleccionando el tipo):",
                        "	cambia el tipo entre ingreso y gasto.",
                        " ",
                        "4.Sección de ayuda:",
                        " ",
                        "	-F1: pasa al siguiente paso",
                        "	(o sale de la seccion de ayuda si es el ultimo).");
                view.getArrowLabel().setVisible(false);
                break;
            case 6:
                endHelp();
                break;
        }
    }

    private void endHelp() {
        inHelpMode = false;
        helpStep = 0;
        view.setHelpStep(helpStep);
        view.exitHelpMode();
        view.limpiarAtajos();
        loadData();
    }

    private String[] splitTextInHalf(String text) {
        int middle = text.length() / 2;
        int spaceIndex = text.lastIndexOf(" ", middle);
        if (spaceIndex == -1) {
            spaceIndex = text.indexOf(" ", middle);
        }
        if (spaceIndex == -1) {
            return new String[]{text, ""};
        } else {
            String firstPart = text.substring(0, spaceIndex).trim();
            String secondPart = text.substring(spaceIndex).trim();
            return new String[]{firstPart, secondPart};
        }
    }

    private class MesLabelListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!inHelpMode) {
                currentFilter = "Mes";
                loadData();
                view.highlightSelectedFilter(view.getMesLabel(), view.getAñoLabel(), view.getTotalLabel());
            }
        }
    }

    private class AñoLabelListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!inHelpMode) {
                currentFilter = "Año";
                loadData();
                view.highlightSelectedFilter(view.getAñoLabel(), view.getMesLabel(), view.getTotalLabel());
            }
        }
    }

    private class TotalLabelListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!inHelpMode) {
                currentFilter = "Total";
                loadData();
                view.highlightSelectedFilter(view.getTotalLabel(), view.getMesLabel(), view.getAñoLabel());
            }
        }
    }

}
