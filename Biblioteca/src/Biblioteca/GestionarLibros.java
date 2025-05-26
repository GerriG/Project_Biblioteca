package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestionarLibros extends JFrame {

    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    private JTextField campoBusqueda;
    private JButton botonBuscar, botonAgregar, botonEliminar, botonActualizar, botonEditar;
    private JLabel mensajeCentral;
    private JScrollPane scrollPane;

    public GestionarLibros() {
    setTitle("📚 Gestión de Libros");
    setSize(900, 500);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());

    // Fondo blanco para toda la ventana
    getContentPane().setBackground(Color.WHITE); // Fondo blanco

    // Asegúrate de que el JFrame tenga un fondo blanco
    setBackground(Color.WHITE); // Establecer el fondo del JFrame a blanco

    UIManager.put("Button.font", new Font("Noto Color Emoji", Font.PLAIN, 14));

    // Panel superior
    JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
    panelSuperior.setBackground(new Color(0, 123, 255)); // Fondo azul

    campoBusqueda = new JTextField(30);
    campoBusqueda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 10, 5, 10)
    ));
    botonBuscar = new JButton("🔍 Buscar");
    botonActualizar = new JButton("🔄 Actualizar");

    estiloBoton(botonBuscar);
    estiloBoton(botonActualizar);
    JLabel labelBuscar = new JLabel("Buscar título:");
    labelBuscar.setForeground(Color.WHITE); // Establece el color del texto a blanco
    labelBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece la fuente en negrita
    panelSuperior.add(labelBuscar);
    panelSuperior.add(campoBusqueda);
    panelSuperior.add(botonBuscar);
    panelSuperior.add(botonActualizar);

    // Modelo y tabla
    modeloTabla = new DefaultTableModel(new Object[]{"ID", "Título", "Autor", "Año", "Estado", "Stock"}, 0);
    tablaLibros = new JTable(modeloTabla);
    tablaLibros.setRowHeight(25);
    tablaLibros.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    tablaLibros.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
    tablaLibros.getTableHeader().setBackground(new Color(100, 149, 237));
    tablaLibros.getTableHeader().setForeground(Color.WHITE);
    tablaLibros.setBackground(Color.WHITE); // Asegura el fondo blanco para la tabla
    centrarContenidoTabla();

    scrollPane = new JScrollPane(tablaLibros);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    scrollPane.getViewport().setBackground(Color.WHITE); // Fondo blanco del scroll pane
    scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
        }
    });

    // Panel inferior
    JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
    panelInferior.setBackground(new Color(135, 206, 235)); // Celeste

    botonAgregar = new JButton("➕ Agregar libro");
    botonEliminar = new JButton("🗑️ Eliminar libro");
    botonEditar = new JButton("✏️ Editar libro");

    estiloBoton(botonAgregar);
    estiloBoton(botonEliminar);
    estiloBoton(botonEditar);

    panelInferior.add(botonAgregar);
    panelInferior.add(botonEliminar);
    panelInferior.add(botonEditar);

    // Mensaje central
    mensajeCentral = new JLabel("", SwingConstants.CENTER);
    mensajeCentral.setFont(new Font("Segoe UI", Font.BOLD, 16));

    // Agregar a la ventana
    add(panelSuperior, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(panelInferior, BorderLayout.SOUTH);

    // Acciones
    cargarLibros();
    botonActualizar.addActionListener(e -> cargarLibros());
    botonBuscar.addActionListener(e -> buscarLibro(campoBusqueda.getText().trim()));

    botonAgregar.addActionListener(e -> {
        new AgregarLibro(this);
        cargarLibros();
    });

    botonEliminar.addActionListener(e -> {
        int fila = tablaLibros.getSelectedRow();
        if (fila != -1) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            EliminarLibro.eliminar(this, id);
            cargarLibros();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un libro para eliminar.");
        }
    });

    botonEditar.addActionListener(e -> {
        int fila = tablaLibros.getSelectedRow();
        if (fila != -1) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            String titulo = (String) modeloTabla.getValueAt(fila, 1);
            String autor = (String) modeloTabla.getValueAt(fila, 2);
            int anio = (int) modeloTabla.getValueAt(fila, 3);
            int stock = (int) modeloTabla.getValueAt(fila, 5);
            new EditarLibro(this, id, titulo, autor, anio, stock);
            cargarLibros();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un libro para editar.");
        }
    });

    tablaLibros.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount() == 2) {
                int fila = tablaLibros.getSelectedRow();
                if (fila != -1) {
                    int id = (int) modeloTabla.getValueAt(fila, 0);
                    String titulo = (String) modeloTabla.getValueAt(fila, 1);
                    String autor = (String) modeloTabla.getValueAt(fila, 2);
                    int anio = (int) modeloTabla.getValueAt(fila, 3);
                    int stock = (int) modeloTabla.getValueAt(fila, 5);
                    new EditarLibro(GestionarLibros.this, id, titulo, autor, anio, stock);
                    cargarLibros();
                }
            }
        }
    });

    setVisible(true);
}

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

    private void cargarLibros() {
        modeloTabla.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Libros";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            boolean hayLibros = false;

            while (rs.next()) {
                hayLibros = true;
                String estado = rs.getBoolean("disponible") ? "Disponible" : "Agotado";

                modeloTabla.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getInt("anio"),
                        estado,
                        rs.getInt("stock")
                });
            }

            mostrarMensajeSiTablaVacia(!hayLibros, "📭 No hay ningún libro registrado. ¡Agrega un nuevo libro!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar libros:\n" + e.getMessage());
        }
    }

    private void buscarLibro(String titulo) {
        BuscarLibro.buscarPorTitulo(titulo, modeloTabla);
        boolean sinResultados = modeloTabla.getRowCount() == 0;
        mostrarMensajeSiTablaVacia(sinResultados, "🔍 No se encontró el libro que estás buscando");
    }

    private void mostrarMensajeSiTablaVacia(boolean mostrar, String mensaje) {
        if (mostrar) {
            remove(scrollPane);
            mensajeCentral.setText(mensaje);
            mensajeCentral.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); // Cambia la fuente a Segoe UI Emoji
            add(mensajeCentral, BorderLayout.CENTER);
        } else {
            remove(mensajeCentral);
            add(scrollPane, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }


    private void centrarContenidoTabla() {
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
            tablaLibros.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }
    }

//    Testo interno
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(GestionarLibros::new);
//    }
}