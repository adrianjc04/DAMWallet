package view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import model.Movimiento;
import controller.MovimientoController;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import model.MovimientoDAO;
import model.config.Config;
import model.config.Configurable;
import observer.BalanceObserver;
import model.informe.CSV;
import model.informe.PDF;

public class MovimientoView extends JFrame implements BalanceObserver {

    final String RUTA_ULTIMO_ARCHIVO = "config" + File.separator + "ultimaRuta.txt";
    private Component[] atajos;
    private JPanel movementsPanel;
    private JLabel balanceLabel;
    private JLabel recentTransactionsLabel;
    private JLabel helpTextLabel1;
    private JLabel helpTextLabel2;
    private JLabel arrowLabel;
    private JButton addButton;
    private JButton helpButton;
    private JButton continueButton;
    private JPanel headerPanel;
    private JPanel bannerPanel;
    private JPanel filtersPanel;
    private JLabel mesLabel;
    private JLabel añoLabel;
    private JLabel totalLabel;
    private JPanel centerPanel;
    private JScrollPane scrollPane; // Declarar scrollPane como variable de instancia

    private boolean inHelpMode = false;
    private int currentHelpStep = 0;

    @Override
    public void onBalanceChange(double balance) {
        balanceLabel.setText(String.format("%.2f", balance));
        if (balance < 0) {
            bannerPanel.setBackground(Color.decode("#d63429")); // Rojo
            filtersPanel.setBackground(Color.decode("#d63429")); // Rojo
        } else {
            bannerPanel.setBackground(Color.decode("#123EAF")); // Azul original
            filtersPanel.setBackground(Color.decode("#123EAF")); // Azul original
        }
    }

    private static class ArrowPosition {

        int x;
        int y;

        ArrowPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private ArrowPosition[] arrowPositions;
    private ActionListener continueButtonListener;

    private MovimientoController controller;

    private int selectedMovementIndex = -1;
    private List<MovimientoPanel> movimientoPanels = new ArrayList<>();

    public void setController(MovimientoController controller) {
        this.controller = controller;
    }

    public MovimientoView() {
        setTitle("DAMWallet");
        setSize(800, 600);
        setMinimumSize(new Dimension(670, 667));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (inHelpMode) {
                    recalculateArrowPositions();
                    showArrowAtStep(currentHelpStep);
                }
            }
        });

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        this.setIconImage(new ImageIcon("./imgs/LogoRecortado.png").getImage());

        // Deshabilitar las teclas de navegación de foco predeterminadas
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());

        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        bannerPanel = new JPanel();
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));

        Color bannerColor = Color.decode("#123EAF");
        bannerPanel.setBackground(bannerColor);

        JLabel titleLabel = new JLabel("Balance Total", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        balanceLabel = new JLabel("0.00", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");

        JMenuItem mItemAbrir = new JMenuItem("Abrir");
        JMenuItem mItemCerrar = new JMenuItem("Cerrar");
        JMenuItem mItemExportarPDF = new JMenuItem("Exportar a PDF");
        JMenuItem mItemExportarCSV = new JMenuItem("Exportar a CSV");

        mItemExportarPDF.addActionListener(l -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar como PDF");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo PDF (*.pdf)", "pdf"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Asegurar que el archivo tenga la extensión .pdf
                if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }

                // Obtener la lista de movimientos desde la base de datos
                Movimiento[] movimientosArray = MovimientoDAO.leerMovimientos("SELECT * FROM " + MovimientoDAO.NOMBRETABLA);
                List<Movimiento> listaMovimientos = Arrays.asList(movimientosArray);

                PDF pdf = new PDF(listaMovimientos, "imgs\\LogoRecortado.png");
                try {
                    pdf.guardarGraficoMensual(fileToSave);
                    JOptionPane.showMessageDialog(null, "Exportado correctamente a PDF.");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al exportar a PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Exportación cancelada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        mItemExportarCSV.addActionListener(l -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar como CSV");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Asegurar que el archivo tenga la extensión .csv
                if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
                }

                // Obtener la lista de movimientos desde la base de datos
                Movimiento[] movimientosArray = MovimientoDAO.leerMovimientos("SELECT * FROM " + MovimientoDAO.NOMBRETABLA);
                List<Movimiento> listaMovimientos = Arrays.asList(movimientosArray);

                CSV csv = new CSV(listaMovimientos);
                try {
                    csv.guardarDatosMensuales(fileToSave);
                    JOptionPane.showMessageDialog(null, "Exportado correctamente a CSV.");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error al exportar a CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Exportación cancelada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        mItemAbrir.addActionListener(l -> {
            Configurable lastFileConfigurator = Config.LastFile.CONFIGURADOR;
            String rutaSeleccionada = lastFileConfigurator.seleccionarArchivoActual();
            if (lastFileConfigurator.esValido(rutaSeleccionada)) {
                lastFileConfigurator.reescribirActual(rutaSeleccionada);
                System.out.println("Seleccionado " + rutaSeleccionada);
            }

            MovimientoDAO.rutaBBDD = rutaSeleccionada;
            if (MovimientoDAO.crearBaseDeDatos()) {
                System.out.println("Abriendo " + rutaSeleccionada);
                setMovements(MovimientoDAO.leerMovimientos("SELECT * FROM MOVIMIENTO;"));
                updateMovementSelection();
                SwingUtilities.invokeLater(() -> simularClic(totalLabel));
            } else {
                System.out.println("Error al abrir " + rutaSeleccionada);
            }
        });

        mItemCerrar.addActionListener(l -> {
            System.exit(0);
        });

        menuArchivo.add(mItemAbrir);
        menuArchivo.add(mItemCerrar);
        menuArchivo.addSeparator();
        menuArchivo.add(mItemExportarPDF);
        menuArchivo.add(mItemExportarCSV);

        JMenu menuVer = new JMenu("Ver");

        JCheckBoxMenuItem mItemAlwaysOnTop = new JCheckBoxMenuItem("Siempre en primer plano");

        mItemAlwaysOnTop.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this.setAlwaysOnTop(true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                this.setAlwaysOnTop(false);
            }
        });

        menuVer.add(mItemAlwaysOnTop);

        menuBar.add(menuArchivo);
        menuBar.add(menuVer);

        this.setJMenuBar(menuBar);

        bannerPanel.add(Box.createVerticalStrut(10));
        bannerPanel.add(titleLabel);
        bannerPanel.add(balanceLabel);
        bannerPanel.add(Box.createVerticalStrut(10));

        filtersPanel = new JPanel();
        filtersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        filtersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        filtersPanel.setBackground(bannerColor);

        mesLabel = new JLabel("MES");
        añoLabel = new JLabel("AÑO");
        totalLabel = new JLabel("TOTAL");

        Font filterFont = new Font("Arial", Font.BOLD, 14);
        mesLabel.setFont(filterFont);
        añoLabel.setFont(filterFont);
        totalLabel.setFont(filterFont);

        mesLabel.setForeground(Color.WHITE);
        añoLabel.setForeground(Color.WHITE);
        totalLabel.setForeground(Color.WHITE);

        mesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        añoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        totalLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mesLabel.setToolTipText("Filtro mes (Alternar: Tab)");
        añoLabel.setToolTipText("Filtro año (Alternar: Tab)");
        totalLabel.setToolTipText("Filtro total (Alternar: Tab)");

        filtersPanel.add(mesLabel);
        filtersPanel.add(añoLabel);
        filtersPanel.add(totalLabel);

        highlightSelectedFilter(totalLabel, mesLabel, añoLabel);

        headerPanel.add(bannerPanel);
        headerPanel.add(filtersPanel);

        add(headerPanel, BorderLayout.NORTH);

        recentTransactionsLabel = new JLabel("Transacciones recientes", SwingConstants.CENTER);
        recentTransactionsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        recentTransactionsLabel.setForeground(Color.decode("#4898F6"));
        recentTransactionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        helpTextLabel1 = new JLabel("", SwingConstants.CENTER);
        helpTextLabel1.setFont(new Font("Arial", Font.PLAIN, 14));
        helpTextLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);

        helpTextLabel2 = new JLabel("", SwingConstants.CENTER);
        helpTextLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
        helpTextLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon arrowIcon = new ImageIcon("imgs/arrow_icon_right.png");
        Image arrowImg = arrowIcon.getImage();
        Image scaledArrowImg = arrowImg.getScaledInstance(54, 54, Image.SCALE_SMOOTH);
        arrowIcon = new ImageIcon(scaledArrowImg);

        arrowLabel = new JLabel(arrowIcon);
        arrowLabel.setVisible(false);

        movementsPanel = new JPanel();
        movementsPanel.setLayout(new BoxLayout(movementsPanel, BoxLayout.Y_AXIS));
        movementsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.add(recentTransactionsLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(helpTextLabel1);
        centerPanel.add(helpTextLabel2);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(movementsPanel);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scrollPane = new JScrollPane(centerPanel); // Inicializar scrollPane como variable de instancia
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        ImageIcon helpIcon = new ImageIcon("imgs/question_mark_icon.png");
        Image helpImg = helpIcon.getImage();
        Image scaledHelpImg = helpImg.getScaledInstance(54, 54, Image.SCALE_SMOOTH);
        helpIcon = new ImageIcon(scaledHelpImg);

        helpButton = new JButton(helpIcon);
        helpButton.setPreferredSize(new Dimension(54, 54));
        helpButton.setContentAreaFilled(false);
        helpButton.setBorderPainted(false);
        helpButton.setFocusPainted(false);
        helpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        helpButton.setToolTipText("Ayuda (F1)");

        leftButtonPanel.add(helpButton);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        ImageIcon addIcon = new ImageIcon("imgs/icono_añadir.png");
        Image img = addIcon.getImage();
        Image scaledImg = img.getScaledInstance(54, 54, Image.SCALE_SMOOTH);
        addIcon = new ImageIcon(scaledImg);

        addButton = new JButton(addIcon);
        addButton.setPreferredSize(new Dimension(54, 54));
        addButton.setContentAreaFilled(false);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.setToolTipText("Nuevo Movimiento (ctrl + n)");

        rightButtonPanel.add(addButton);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        add(buttonPanel, BorderLayout.SOUTH);

        // Configurar Key Binding para F1, Tab, Ctrl+N, Ctrl+Z, Flechas y Supr
        setupKeyBindings();
    }

    public static void simularClic(JLabel label) {
        // Crear el evento MouseEvent que simula un clic
        MouseEvent event = new MouseEvent(label, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                0, label.getWidth() / 2, label.getHeight() / 2, 1, false);

        // Disparar el evento al JLabel
        label.dispatchEvent(event);
    }

    private void setupKeyBindings() {
        // Mapear la tecla F1 a la acción "showHelp"
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("F1"), "showHelp");
        actionMap.put("showHelp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller != null) {
                    controller.triggerHelp();
                }
            }
        });

        // Mapear la tecla Tab a la acción "cycleFilter"
        inputMap.put(KeyStroke.getKeyStroke("pressed TAB"), "cycleFilter");
        actionMap.put("cycleFilter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inHelpMode && controller != null) {
                    controller.cycleFilter();
                }
            }
        });

        // Mapear Ctrl+N a la acción "addMovimiento"
        inputMap.put(KeyStroke.getKeyStroke("ctrl N"), "addMovimiento");
        actionMap.put("addMovimiento", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inHelpMode && controller != null) {
                    controller.addMovimiento();
                }
            }
        });

        // Mapear Ctrl+Z a la acción "undoDeleteMovimiento"
        inputMap.put(KeyStroke.getKeyStroke("ctrl Z"), "undoDeleteMovimiento");
        actionMap.put("undoDeleteMovimiento", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inHelpMode && controller != null) {
                    controller.undoDeleteMovimiento();
                }
            }
        });

        // Mapear Flecha Arriba a la acción "moveSelectionUp"
        inputMap.put(KeyStroke.getKeyStroke("UP"), "moveSelectionUp");
        actionMap.put("moveSelectionUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inHelpMode) {
                    moveSelectionUp();
                }
            }
        });

        // Mapear Flecha Abajo a la acción "moveSelectionDown"
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "moveSelectionDown");
        actionMap.put("moveSelectionDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inHelpMode) {
                    moveSelectionDown();
                }
            }
        });

        // Mapear Supr a la acción "deleteSelectedMovement"
        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteSelectedMovement");
        actionMap.put("deleteSelectedMovement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inHelpMode) {
                    deleteSelectedMovement();
                }
            }
        });
    }

    public int showAddConfirmDialog(String message) {
        JButton yesButton = new JButton("Sí");
        JButton noButton = new JButton("No");

        yesButton.setFocusable(true);
        noButton.setFocusable(true);

        Object[] options = {yesButton, noButton};
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, null);
        JDialog dialog = pane.createDialog(this, "Confirmar Nuevo Movimiento");

        List<Component> focusOrder = Arrays.asList(yesButton, noButton);
        FocusTraversalPolicy policy = new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                int idx = (focusOrder.indexOf(aComponent) + 1) % focusOrder.size();
                return focusOrder.get(idx);
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                int idx = (focusOrder.indexOf(aComponent) - 1 + focusOrder.size()) % focusOrder.size();
                return focusOrder.get(idx);
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return focusOrder.get(0);
            }

            @Override
            public Component getLastComponent(Container aContainer) {
                return focusOrder.get(focusOrder.size() - 1);
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return focusOrder.get(0);
            }
        };
        dialog.setFocusTraversalPolicy(policy);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                yesButton.requestFocusInWindow();
            }
        });

        yesButton.addActionListener(e -> {
            pane.setValue(JOptionPane.YES_OPTION);
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            pane.setValue(JOptionPane.NO_OPTION);
            dialog.dispose();
        });

        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = dialog.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        actionMap.put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (yesButton.isFocusOwner()) {
                    yesButton.doClick();
                } else {
                    noButton.doClick();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        actionMap.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noButton.doClick();
            }
        });

        dialog.setVisible(true);

        Object selectedValue = pane.getValue();
        if (selectedValue instanceof Integer) {
            return (int) selectedValue;
        } else {
            return JOptionPane.CLOSED_OPTION;
        }
    }

    private void moveSelectionUp() {
        if (movimientoPanels.isEmpty()) {
            return;
        }
        if (selectedMovementIndex == -1) {
            // No hay selección, selecciona el primero
            selectedMovementIndex = movimientoPanels.size() - 1;
        } else if (selectedMovementIndex > 0) {
            selectedMovementIndex--;
        }
        updateMovementSelection();
    }

    private void moveSelectionDown() {
        if (movimientoPanels.isEmpty()) {
            return;
        }
        if (selectedMovementIndex == -1) {
            // No hay selección, selecciona el primero
            selectedMovementIndex = 0;
        } else if (selectedMovementIndex < movimientoPanels.size() - 1) {
            selectedMovementIndex++;
        }
        updateMovementSelection();
    }

    private void scrollToPanel(MovimientoPanel panel) {
        SwingUtilities.invokeLater(() -> {
            Rectangle rect = panel.getBounds();
            Point panelLocation = SwingUtilities.convertPoint(panel.getParent(), rect.getLocation(), scrollPane.getViewport().getView());

            // Obtener la posición Y del panel en relación al viewport
            int panelY = panelLocation.y;

            // Calcular la altura del header y otros componentes sobre el panel
            int headerHeight = headerPanel.getHeight() + recentTransactionsLabel.getHeight() + 20; // Ajusta el valor 20 según sea necesario

            // Obtener el rectángulo visible del viewport
            Rectangle viewRect = scrollPane.getViewport().getViewRect();

            if (panelY < viewRect.y + headerHeight) {
                // Si el panel está oculto debajo del header, ajustar la posición
                int newY = panelY - headerHeight;
                if (newY < 0) {
                    newY = 0;
                }
                scrollPane.getViewport().setViewPosition(new Point(0, newY));
            } else if (panelY + panel.getHeight() > viewRect.y + viewRect.height) {
                // Si el panel está fuera de la parte inferior del viewport
                int newY = panelY + panel.getHeight() - viewRect.height;
                scrollPane.getViewport().setViewPosition(new Point(0, newY));
            }
        });
    }

    private void updateMovementSelection() {
        for (int i = 0; i < movimientoPanels.size(); i++) {
            MovimientoPanel panel = movimientoPanels.get(i);
            if (i == selectedMovementIndex) {
                panel.setSelected(true);
                // Asegurar que el panel seleccionado sea visible
                scrollToPanel(panel);
            } else {
                panel.setSelected(false);
            }
        }
    }

    private void deleteSelectedMovement() {
        if (selectedMovementIndex >= 0 && selectedMovementIndex < movimientoPanels.size()) {
            MovimientoPanel selectedPanel = movimientoPanels.get(selectedMovementIndex);
            // Activar la acción de eliminar
            selectedPanel.getDeleteButton().doClick();
        }
    }

    public void setBalance(double balance) {
        balanceLabel.setText(String.format("%.2f", balance));
    }

    public void setMovements(Movimiento[] movimientos) {
        movementsPanel.removeAll();
        movimientoPanels.clear();
        selectedMovementIndex = -1;

        for (Movimiento movimiento : movimientos) {
            MovimientoPanel movimientoPanel = new MovimientoPanel(movimiento);

            movimientoPanel.getDeleteButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int option = showConfirmDialog("¿Estás seguro de que deseas eliminar este movimiento?");
                    if (option == JOptionPane.YES_OPTION) {
                        if (controller != null) {
                            controller.deleteMovimiento(movimientoPanel.getMovimientoId());
                        }
                    }
                }
            });

            movementsPanel.add(movimientoPanel);
            movimientoPanels.add(movimientoPanel);
        }

        movementsPanel.revalidate();
        movementsPanel.repaint();
    }

    private int showConfirmDialog(String message) {
        JButton yesButton = new JButton("Sí");
        JButton noButton = new JButton("No");

        // Hacer que los botones sean enfocables
        yesButton.setFocusable(true);
        noButton.setFocusable(true);

        // Crear el JOptionPane con botones personalizados
        Object[] options = {yesButton, noButton};
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, null);
        JDialog dialog = pane.createDialog(this, "Confirmar eliminación");

        // Definir una política de recorrido de foco personalizada
        List<Component> focusOrder = Arrays.asList(yesButton, noButton);
        FocusTraversalPolicy policy = new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                int idx = (focusOrder.indexOf(aComponent) + 1) % focusOrder.size();
                return focusOrder.get(idx);
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                int idx = (focusOrder.indexOf(aComponent) - 1 + focusOrder.size()) % focusOrder.size();
                return focusOrder.get(idx);
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return focusOrder.get(0);
            }

            @Override
            public Component getLastComponent(Container aContainer) {
                return focusOrder.get(focusOrder.size() - 1);
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return focusOrder.get(0);
            }
        };
        dialog.setFocusTraversalPolicy(policy);

        // Establecer el foco inicial en el botón "Sí"
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                yesButton.requestFocusInWindow();
            }
        });

        // Agregar ActionListeners a los botones
        yesButton.addActionListener(e -> {
            pane.setValue(JOptionPane.YES_OPTION);
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            pane.setValue(JOptionPane.NO_OPTION);
            dialog.dispose();
        });

        // Configurar Key Bindings para Enter y Escape
        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = dialog.getRootPane().getActionMap();

        // Acción para la tecla Enter
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        actionMap.put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Verificar qué botón tiene el foco y simular un clic
                if (yesButton.isFocusOwner()) {
                    yesButton.doClick();
                } else if (noButton.isFocusOwner()) {
                    noButton.doClick();
                } else {
                    // Si ningún botón tiene el foco, por defecto seleccionar "Sí"
                    yesButton.doClick();
                }
            }
        });

        // Acción para la tecla Escape
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        actionMap.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noButton.doClick();
            }
        });

        // Mostrar el diálogo
        dialog.setVisible(true);

        // Obtener el valor seleccionado
        Object selectedValue = pane.getValue();
        if (selectedValue instanceof Integer) {
            return (int) selectedValue;
        } else {
            return JOptionPane.CLOSED_OPTION;
        }
    }

    public int showAddMovementConfirmDialog(String concepto, String tipoMovimiento, double cantidad, String fecha) {
        JButton yesButton = new JButton("Sí");
        JButton noButton = new JButton("No");

        // Hacer que los botones sean enfocables
        yesButton.setFocusable(true);
        noButton.setFocusable(true);

        // Crear el mensaje de confirmación
        String message = String.format(
                "¿Seguro que quieres añadir el movimiento con los siguientes detalles?\n\n"
                + "Concepto: %s\n"
                + "Tipo: %s\n"
                + "Cantidad: %.2f\n"
                + "Fecha: %s",
                concepto, tipoMovimiento, cantidad, fecha
        );

        // Crear el JOptionPane con botones personalizados
        Object[] options = {yesButton, noButton};
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, null);
        JDialog dialog = pane.createDialog(this, "Confirmar Nuevo Movimiento");

        // Definir una política de recorrido de foco personalizada
        List<Component> focusOrder = Arrays.asList(yesButton, noButton);
        FocusTraversalPolicy policy = new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                int idx = (focusOrder.indexOf(aComponent) + 1) % focusOrder.size();
                return focusOrder.get(idx);
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                int idx = (focusOrder.indexOf(aComponent) - 1 + focusOrder.size()) % focusOrder.size();
                return focusOrder.get(idx);
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return focusOrder.get(0);
            }

            @Override
            public Component getLastComponent(Container aContainer) {
                return focusOrder.get(focusOrder.size() - 1);
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return focusOrder.get(0);
            }
        };
        dialog.setFocusTraversalPolicy(policy);

        // Establecer el foco inicial en el botón "Sí"
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                yesButton.requestFocusInWindow();
            }
        });

        // Agregar ActionListeners a los botones
        yesButton.addActionListener(e -> {
            pane.setValue(JOptionPane.YES_OPTION);
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            pane.setValue(JOptionPane.NO_OPTION);
            dialog.dispose();
        });

        // Configurar Key Bindings para Enter y Escape
        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = dialog.getRootPane().getActionMap();

        // Acción para la tecla Enter
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        actionMap.put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (yesButton.isFocusOwner()) {
                    yesButton.doClick();
                } else if (noButton.isFocusOwner()) {
                    noButton.doClick();
                } else {
                    yesButton.doClick();
                }
            }
        });

        // Acción para la tecla Escape
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        actionMap.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noButton.doClick();
            }
        });

        // Mostrar el diálogo
        dialog.setVisible(true);

        // Obtener el valor seleccionado
        Object selectedValue = pane.getValue();
        if (selectedValue instanceof Integer) {
            return (int) selectedValue;
        } else {
            return JOptionPane.CLOSED_OPTION;
        }
    }

    private class MovimientoPanel extends JPanel {

        private long movimientoId;
        private JButton deleteButton;
        private Color hoverBackgroundColor = Color.decode("#89bced");
        private Color hoverBorderColor = Color.decode("#183cac");
        private Color defaultBackgroundColor;
        private Border defaultBorder;
        private boolean isSelected = false;

        public MovimientoPanel(Movimiento movimiento) {
            this.movimientoId = movimiento.getId();
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setOpaque(true); // Hacer el panel opaco para que se vea el fondo

            // Guardar los valores por defecto
            defaultBackgroundColor = getBackground();
            defaultBorder = getBorder();

            deleteButton = new JButton("x");
            deleteButton.setToolTipText("Eliminar (Spr)");
            deleteButton.setVisible(false);
            deleteButton.setPreferredSize(new Dimension(45, 45)); // Mantener el tamaño solicitado
            deleteButton.setFont(new Font("Arial", Font.PLAIN, 16));
            deleteButton.setContentAreaFilled(false);
            deleteButton.setBorderPainted(false);
            deleteButton.setFocusPainted(false);
            deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            add(deleteButton, BorderLayout.WEST);

            JPanel centroPanel = new JPanel();
            centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.PAGE_AXIS));
            centroPanel.setOpaque(false); // Mantener transparente

            JLabel conceptoLabel = new JLabel(movimiento.getConcepto());
            conceptoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            conceptoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel fechaLabel = new JLabel(movimiento.getFecha().toString());
            fechaLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            fechaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            centroPanel.add(conceptoLabel);
            centroPanel.add(fechaLabel);

            JLabel cantidadLabel = new JLabel(String.format("%.2f", movimiento.getCantidad()));
            cantidadLabel.setFont(new Font("Arial", Font.BOLD, 16));

            add(centroPanel, BorderLayout.CENTER);
            add(cantidadLabel, BorderLayout.EAST);

            int maxWidth = 500;
            setMaximumSize(new Dimension(maxWidth, getPreferredSize().height));
            setAlignmentX(Component.CENTER_ALIGNMENT);

            // MouseAdapter para manejar eventos del ratón
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        deleteButton.setVisible(true);
                        setBackground(hoverBackgroundColor);
                        setBorder(BorderFactory.createLineBorder(hoverBorderColor, 2));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        deleteButton.setVisible(false);
                        setBackground(defaultBackgroundColor);
                        setBorder(defaultBorder);
                    }
                }
            };

            // Agregar el MouseAdapter a los componentes necesarios
            addMouseListener(mouseAdapter);
            centroPanel.addMouseListener(mouseAdapter);
            conceptoLabel.addMouseListener(mouseAdapter);
            fechaLabel.addMouseListener(mouseAdapter);
            cantidadLabel.addMouseListener(mouseAdapter);
            deleteButton.addMouseListener(mouseAdapter);
        }

        public long getMovimientoId() {
            return movimientoId;
        }

        public JButton getDeleteButton() {
            return deleteButton;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            if (selected) {
                deleteButton.setVisible(true);
                setBackground(hoverBackgroundColor);
                setBorder(BorderFactory.createLineBorder(hoverBorderColor, 2));
            } else {
                deleteButton.setVisible(false);
                setBackground(defaultBackgroundColor);
                setBorder(defaultBorder);
            }
        }
    }

    public void highlightSelectedFilter(JLabel selectedLabel, JLabel... otherLabels) {
        selectedLabel.setText("<html><u>" + selectedLabel.getText() + "</u></html>");
        for (JLabel label : otherLabels) {
            label.setText(label.getText().replace("<html><u>", "").replace("</u></html>", ""));
        }
    }

    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void addHelpButtonListener(ActionListener listener) {
        helpButton.addActionListener(listener);
    }

    public void addContinueButtonListener(ActionListener listener) {
        this.continueButtonListener = listener;
        if (continueButton != null) {
            continueButton.addActionListener(continueButtonListener);
        }
    }

    public void addFilterLabelListener(MouseListener mesListener, MouseListener añoListener, MouseListener totalListener) {
        mesLabel.addMouseListener(mesListener);
        añoLabel.addMouseListener(añoListener);
        totalLabel.addMouseListener(totalListener);
    }

    public JLabel getMesLabel() {
        return mesLabel;
    }

    public JLabel getAñoLabel() {
        return añoLabel;
    }

    public JLabel getTotalLabel() {
        return totalLabel;
    }

    public void setHelpStep(int step) {
        this.currentHelpStep = step;
    }

    public void enterHelpMode() {
        inHelpMode = true;
        currentHelpStep = 1;

        movementsPanel.setVisible(false);
        addButton.setEnabled(false);
        helpButton.setVisible(false);
        mesLabel.setEnabled(false);
        añoLabel.setEnabled(false);
        totalLabel.setEnabled(false);

        recentTransactionsLabel.setText("Guía");

        continueButton = new JButton("Continuar...");
        continueButton.setFont(new Font("Arial", Font.BOLD, 18));
        continueButton.setForeground(Color.decode("#4898F6"));
        continueButton.setContentAreaFilled(false);
        continueButton.setBorderPainted(false);
        continueButton.setFocusPainted(false);
        continueButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (continueButtonListener != null) {
            continueButton.addActionListener(continueButtonListener);
        }

        JPanel leftButtonPanel = (JPanel) ((BorderLayout) ((JPanel) getContentPane().getComponent(2)).getLayout())
                .getLayoutComponent(BorderLayout.WEST);
        leftButtonPanel.removeAll();
        leftButtonPanel.add(continueButton);
        leftButtonPanel.revalidate();
        leftButtonPanel.repaint();

        helpTextLabel1.setText("");
        helpTextLabel2.setText("");

        arrowPositions = new ArrowPosition[4];

        if (arrowLabel.getParent() != getLayeredPane()) {
            getLayeredPane().add(arrowLabel, JLayeredPane.POPUP_LAYER);
        }
        arrowLabel.setVisible(true);

        recalculateArrowPositions();
        showArrowAtStep(currentHelpStep);
    }

    private void recalculateArrowPositions() {
        SwingUtilities.invokeLater(() -> {
            try {
                Point frameLocation = getLocationOnScreen();

                int arrowWidth = arrowLabel.getIcon().getIconWidth();
                int arrowHeight = arrowLabel.getIcon().getIconHeight();

                Point balanceLocation = balanceLabel.getLocationOnScreen();
                int x1 = balanceLocation.x - frameLocation.x - arrowWidth - 10;
                int y1 = balanceLocation.y - frameLocation.y + (balanceLabel.getHeight() - arrowHeight) / 2 - 30;

                Point mesLocation = mesLabel.getLocationOnScreen();
                int x2 = mesLocation.x - frameLocation.x - arrowWidth - 10;
                int y2 = mesLocation.y - frameLocation.y + (mesLabel.getHeight() - arrowHeight) / 2 - 30;

                Point añoLocation = añoLabel.getLocationOnScreen();
                int x3 = añoLocation.x - frameLocation.x - arrowWidth - 10;
                int y3 = añoLocation.y - frameLocation.y + (añoLabel.getHeight() - arrowHeight) / 2 - 30;

                Point addButtonLocation = addButton.getLocationOnScreen();
                int x4 = addButtonLocation.x - frameLocation.x - arrowWidth - 10;
                int y4 = addButtonLocation.y - frameLocation.y + (addButton.getHeight() - arrowHeight) / 2 - 30;

                arrowPositions[0] = new ArrowPosition(x1, y1);
                arrowPositions[1] = new ArrowPosition(x2, y2);
                arrowPositions[2] = new ArrowPosition(x3, y3);
                arrowPositions[3] = new ArrowPosition(x4, y4);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private JTable table;
    private JScrollPane scrollPane2;
    private JPanel wrapperPanel = new JPanel();

    public void updateHelpText(String text1, String text2, Object... otrosComponentes) {
        helpTextLabel1.setText(text1);
        helpTextLabel2.setText(text2);

        atajos = new Component[otrosComponentes.length];

        // Determinar cuántos elementos son texto y cuántos son imágenes
        List<Object[]> dataList = new ArrayList<>();

        for (Object obj : otrosComponentes) {
            if (obj instanceof String) {
                dataList.add(new Object[]{obj}); // Agregar texto como fila
            } else if (obj instanceof JLabel) {
                JLabel imageLabel = (JLabel) obj;
                if (imageLabel.getIcon() != null) {
                    dataList.add(new Object[]{imageLabel}); // Agregar imagen como fila
                }
            }
        }

        // Convertir la lista a un array
        Object[][] data = dataList.toArray(new Object[0][1]);

        // Crear la tabla solo si hay datos
        if (data.length > 0) {
            String[] columnNames = {""}; // Sin encabezado visible
            table = new JTable(new AbstractTableModel() {
                @Override
                public int getRowCount() {
                    return data.length;
                }

                @Override
                public int getColumnCount() {
                    return 1;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    return data[rowIndex][0];
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return Object.class; // Permite mezclar texto e imágenes
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false; // Hace que las celdas no sean editables
                }
            });

            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setRowHeight(25);
            table.setTableHeader(null); // Oculta los encabezados de columna
            table.setShowGrid(false); // Oculta las líneas de la tabla
            table.setIntercellSpacing(new Dimension(0, 0)); // Elimina el espacio entre celdas
            table.setOpaque(false); // Hace que la tabla no tenga fondo
            table.setBackground(new Color(0, 0, 0, 0)); // Fondo completamente transparente
            table.setBorder(BorderFactory.createEmptyBorder()); // Elimina cualquier borde
            table.setShowVerticalLines(false); // Quita líneas verticales
            table.setShowHorizontalLines(false); // Quita líneas horizontales
            table.setFillsViewportHeight(true);

            // 🔹 Evitar selección de filas y columnas
            table.setRowSelectionAllowed(false);
            table.setColumnSelectionAllowed(false);
            table.setCellSelectionEnabled(false);

            // 🔹 Evitar que la tabla reciba el foco al hacer clic
            table.setFocusable(false);

            // 🔹 Renderizador personalizado para manejar texto e imágenes
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value instanceof JLabel) {
                        return (JLabel) value; // Mostrar imagen directamente
                    } else {
                        String text = value == null ? "" : value.toString();
                        JLabel cell = new JLabel(text);
                        cell.setOpaque(false); // Hace que la celda no tenga fondo

                        // 🔹 Aplicar negrita si el texto empieza con un número seguido de punto
                        if (text.matches("^[0-9]+\\..*")) {
                            cell.setFont(new Font("Arial", Font.BOLD, 14)); // Negrita
                        } else {
                            cell.setFont(new Font("Arial", Font.PLAIN, 14)); // Normal
                        }

                        return cell;
                    }
                }
            });

            // Crear un JScrollPane sin fondo, pero con scroll vertical dinámico
            scrollPane2 = new JScrollPane(table);
            scrollPane2.setBorder(BorderFactory.createEmptyBorder()); // Sin bordes
            scrollPane2.setViewportBorder(BorderFactory.createEmptyBorder()); // Quita cualquier borde del viewport
            scrollPane2.setOpaque(false); // Hace que el scrollpane sea transparente
            scrollPane2.getViewport().setOpaque(false); // Hace que el área del viewport sea transparente

            // 🔹 Ajustar la altura dinámicamente
            int tableHeight = table.getRowCount() * table.getRowHeight();
            scrollPane2.setPreferredSize(new Dimension(500, Math.min(300, tableHeight))); // Máximo 300px de alto

            // 🔹 Permitir scroll vertical si hay demasiados elementos
            scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Desactiva scroll horizontal
            scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Activar scroll vertical cuando sea necesario

            // 🔹 Aumentar la velocidad del scroll con el mousewheel
            scrollPane2.getVerticalScrollBar().setUnitIncrement(20);
        } else {
            table = null;
            scrollPane2 = null;
        }

        // Ajustar el panel sin limpiarlo (como en tu versión)
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Agregar textos al panel
        centerPanel.add(helpTextLabel1);
        centerPanel.add(helpTextLabel2);

        // Agregar la tabla solo si existe
        if (scrollPane2 != null) {
            wrapperPanel = new JPanel();
            wrapperPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Centra la tabla
            wrapperPanel.setOpaque(false); // Hace que el panel contenedor también sea transparente
            wrapperPanel.add(scrollPane2);
            centerPanel.add(wrapperPanel);
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    public void showArrowAtStep(int step) {
        SwingUtilities.invokeLater(() -> {
            int index = step - 1;
            if (index >= 0 && index < arrowPositions.length) {
                ArrowPosition pos = arrowPositions[index];
                if (pos != null) {
                    arrowLabel.setBounds(pos.x, pos.y, arrowLabel.getIcon().getIconWidth(),
                            arrowLabel.getIcon().getIconHeight());
                    arrowLabel.repaint();
                }
            }
        });
    }

    public void exitHelpMode() {
        inHelpMode = false;
        currentHelpStep = 0;

        if (continueButton != null && continueButtonListener != null) {
            continueButton.removeActionListener(continueButtonListener);
        }

        movementsPanel.setVisible(true);
        addButton.setEnabled(true);
        helpButton.setVisible(true);
        mesLabel.setEnabled(true);
        añoLabel.setEnabled(true);
        totalLabel.setEnabled(true);

        recentTransactionsLabel.setText("Transacciones recientes");

        helpTextLabel1.setText("");
        helpTextLabel2.setText("");

        arrowLabel.setVisible(false);
        getLayeredPane().remove(arrowLabel);

        JPanel leftButtonPanel = (JPanel) ((BorderLayout) ((JPanel) getContentPane().getComponent(2)).getLayout())
                .getLayoutComponent(BorderLayout.WEST);
        leftButtonPanel.removeAll();
        leftButtonPanel.add(helpButton);
        leftButtonPanel.revalidate();
        leftButtonPanel.repaint();
    }

    // Métodos para agregar Key Bindings desde el controlador
    public void addHelpKeyBinding(Action action) {
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("F1"), "showHelp");
        actionMap.put("showHelp", action);
    }

    public void addTabKeyBinding(Action action) {
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("pressed TAB"), "cycleFilter");
        actionMap.put("cycleFilter", action);
    }

    public void addCtrlNKeyBinding(Action action) {
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ctrl N"), "addMovimiento");
        actionMap.put("addMovimiento", action);
    }

    public void addCtrlZKeyBinding(Action action) {
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ctrl Z"), "undoDeleteMovimiento");
        actionMap.put("undoDeleteMovimiento", action);
    }

    public void updateBannerAndFiltersColor(String colorHex) {
        Color color = Color.decode(colorHex);
        bannerPanel.setBackground(color);
        filtersPanel.setBackground(color);
    }

    public JLabel getArrowLabel() {
        return arrowLabel;
    }

    public void limpiarAtajos() {
        for (int i = 0; i < atajos.length; i++) {
            try {
                centerPanel.remove(atajos[i]);
            } catch (NullPointerException e) {
            }
        }
        try {
            centerPanel.remove(wrapperPanel);
        } catch (NullPointerException e) {
        }
    }
}
