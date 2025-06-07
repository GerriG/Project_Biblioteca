package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestionarInventario extends JFrame {

    //Objetos de ventana
    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;
    private JTextField campoBusqueda;
    private JButton botonBuscar, botonAgregar, botonEliminar, botonActualizar, botonEditar;
    private JLabel mensajeCentral;
    private JScrollPane scrollPane;

    //Configuracion de ventana
    public GestionarInventario() {
        setTitle("📦 Gestión de Inventario");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.WHITE);
        setBackground(Color.WHITE);

        UIManager.put("Button.font", new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBackground(new Color(0, 123, 255));

        //Crear botones
        campoBusqueda = new JTextField(30);
        campoBusqueda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5, 10, 5, 10)
        ));
        botonBuscar = new JButton("🔍 Buscar");
        botonActualizar = new JButton("🔄 Actualizar");

        estiloBoton(botonBuscar);
        estiloBoton(botonActualizar);
        JLabel labelBuscar = new JLabel("Buscar código libro:");
        labelBuscar.setForeground(Color.WHITE);
        labelBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelSuperior.add(labelBuscar);
        panelSuperior.add(campoBusqueda);
        panelSuperior.add(botonBuscar);
        panelSuperior.add(botonActualizar);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Código Libro", "Código Copia", "Estado"}, 0);
        tablaInventario = new JTable(modeloTabla);
        tablaInventario.setRowHeight(25);
        tablaInventario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaInventario.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaInventario.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaInventario.getTableHeader().setForeground(Color.WHITE);
        tablaInventario.setBackground(Color.WHITE);
        centrarContenidoTabla();

        scrollPane = new JScrollPane(tablaInventario);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
            }
        });

        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        botonAgregar = new JButton("➕ Agregar copia");
        botonEliminar = new JButton("🗑️ Eliminar copia");
        botonEditar = new JButton("✏️ Editar estado");

        estiloBoton(botonAgregar);
        estiloBoton(botonEliminar);
        estiloBoton(botonEditar);

        panelInferior.add(botonAgregar);
        panelInferior.add(botonEliminar);
        panelInferior.add(botonEditar);

        mensajeCentral = new JLabel("", SwingConstants.CENTER);
        mensajeCentral.setFont(new Font("Segoe UI", Font.BOLD, 16));

        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        cargarInventario();
        botonActualizar.addActionListener(e -> cargarInventario());
        botonBuscar.addActionListener(e -> buscarInventario(campoBusqueda.getText().trim()));

        botonAgregar.addActionListener(e -> {
            new MantenimientoInventario(this, null).setVisible(true);
            cargarInventario();
        });

        botonEliminar.addActionListener(e -> {
            int fila = tablaInventario.getSelectedRow();
            if (fila != -1) {
                String estado = modeloTabla.getValueAt(fila, 3).toString();
                if (estado.equalsIgnoreCase("Prestado")) {
                    mostrarDialogoAviso("No se puede eliminar una copia que está prestada.");
                    return;
                }
                int id = (int) modeloTabla.getValueAt(fila, 0);
                new MantenimientoInventario(this, id, true); // modo eliminar
                cargarInventario();
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona una copia para eliminar.");
            }
        });

        botonEditar.addActionListener(e -> {
            int fila = tablaInventario.getSelectedRow();
            if (fila != -1) {
                String estado = modeloTabla.getValueAt(fila, 3).toString();
                if (estado.equalsIgnoreCase("Prestado")) {
                    mostrarDialogoAviso("No se puede editar una copia que está prestada.");
                    return;
                }
                int id = (int) modeloTabla.getValueAt(fila, 0);
                new MantenimientoInventario(this, id).setVisible(true); // modo editar
                cargarInventario();
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona una copia para editar.");
            }
        });

        setVisible(true);
    }

    //Crear titulos redondeados
    private JPanel crearPanelRedondeado(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    //Formatear botones
    private void estiloBoton(JButton boton) {
        boton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        boton.setFocusPainted(false);
        boton.setBackground(Color.WHITE);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(6, 12, 6, 12)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    //Cargar el inventario en la BD
    private void cargarInventario() {
        modeloTabla.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL sp_ObtenerInventario}");
            ResultSet rs = stmt.executeQuery();
            boolean hayDatos = false;
            while (rs.next()) {
                hayDatos = true;
                modeloTabla.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("id_libro"),
                    rs.getString("codigo_copia"),
                    rs.getString("estado")
                });
            }
            mostrarMensajeSiTablaVacia(!hayDatos, "📭 No hay copias registradas.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar inventario:\n" + e.getMessage());
        }
    }

    //Realizar la busqueda en la BD
    private void buscarInventario(String filtro) {
        modeloTabla.setRowCount(0); // Limpiar la tabla antes de buscar
        BuscarInventario.buscar(filtro, modeloTabla);
        boolean sinResultados = modeloTabla.getRowCount() == 0;
        mostrarMensajeSiTablaVacia(sinResultados, "🔍 No se encontraron resultados para el filtro ingresado.");
    }

    //Mensaje en caso de que la tabla no posea datos
    private void mostrarMensajeSiTablaVacia(boolean mostrar, String mensaje) {
        if (mostrar) {
            remove(scrollPane);
            mensajeCentral.setText(mensaje);
            mensajeCentral.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            add(mensajeCentral, BorderLayout.CENTER);
        } else {
            remove(mensajeCentral);
            add(scrollPane, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    //Centrar el contenido de la tabla
    private void centrarContenidoTabla() {
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
            tablaInventario.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }
    }

    //Mostrar alertas
    private void mostrarDialogoAviso(String mensaje) {
        JOptionPane optionPane = new JOptionPane(mensaje, JOptionPane.WARNING_MESSAGE);
        JDialog dialog = optionPane.createDialog(this, "⚠️ Aviso");
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

//    Testo interno
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(GestionarInventario::new);
//    }
}
