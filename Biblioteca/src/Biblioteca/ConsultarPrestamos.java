package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ConsultarPrestamos extends JFrame {

    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;
    private JTextField campoBusqueda;
    private JLabel mensajeCentral;
    private JScrollPane scrollPane;

    public ConsultarPrestamos() {
        setTitle("📄 Consultar Préstamos");
        setSize(1000, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel superior (título y buscador)
        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBackground(new Color(0, 123, 255));

        JLabel titulo = new JLabel("📄 Consultar Préstamos");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 20));
        panelSuperior.add(titulo);

        JLabel labelBuscar = new JLabel(" 🔍 Usuario:");
        labelBuscar.setForeground(Color.WHITE);
        labelBuscar.setFont(new Font("Noto Color Emoji", Font.BOLD, 13));
        campoBusqueda = new JTextField(20);
        campoBusqueda.setFont(new Font("Noto Color Emoji", Font.PLAIN, 13));
        JButton botonBuscar = new JButton("Buscar");
        estiloBotonClaro(botonBuscar);

        panelSuperior.add(labelBuscar);
        panelSuperior.add(campoBusqueda);
        panelSuperior.add(botonBuscar);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{
                "ID", "Título", "Código Copia", "Prestado Por", "Fecha Préstamo", "Fecha Devolución", "Estado Devolución"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // evita edición directa
            }
        };

        tablaPrestamos = new JTable(modeloTabla);
        tablaPrestamos.setRowHeight(25);
        tablaPrestamos.setFont(new Font("Noto Color Emoji", Font.PLAIN, 13));
        tablaPrestamos.getTableHeader().setFont(new Font("Noto Color Emoji", Font.BOLD, 14));
        tablaPrestamos.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaPrestamos.getTableHeader().setForeground(Color.WHITE);
        tablaPrestamos.setBackground(Color.WHITE);
        centrarContenidoTabla();

        scrollPane = new JScrollPane(tablaPrestamos);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
            }
        });

        mensajeCentral = new JLabel("", SwingConstants.CENTER);
        mensajeCentral.setFont(new Font("Noto Color Emoji", Font.BOLD, 16));

        // Panel inferior con botones
        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        JButton botonActualizar = new JButton("🔄 Actualizar");
        JButton botonNuevo = new JButton("➕ Nuevo");
        JButton botonEditar = new JButton("✏️ Editar");
        JButton botonDevolver = new JButton("📥 Devolución");

        estiloBotonClaro(botonActualizar);
        estiloBotonClaro(botonNuevo);
        estiloBotonClaro(botonEditar);
        estiloBotonClaro(botonDevolver);

        panelInferior.add(botonActualizar);
        panelInferior.add(botonNuevo);
        panelInferior.add(botonEditar);
        panelInferior.add(botonDevolver);

        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Acciones
        botonBuscar.addActionListener(e -> buscarPrestamosPorUsuario());

        botonActualizar.addActionListener(e -> {
            campoBusqueda.setText("");
            modeloTabla.setRowCount(0);
            mensajeCentral.setText("");
            buscarPrestamosPorUsuario();
        });

        botonNuevo.addActionListener(e -> new ProcesarPrestamos(this, null, "nuevo"));

        botonEditar.addActionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila >= 0) {
                String estado = modeloTabla.getValueAt(fila, 6).toString();
                if (estado.equals("Pendiente")) {
                    int idPrestamo = (int) modeloTabla.getValueAt(fila, 0);
                    new ProcesarPrestamos(this, idPrestamo, "editar");
                } else {
                    JOptionPane.showMessageDialog(this, "Este préstamo ya fue devuelto. No se puede editar.", "Operación no permitida", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un préstamo para editar.");
            }
        });

        botonDevolver.addActionListener(e -> {
            int fila = tablaPrestamos.getSelectedRow();
            if (fila >= 0) {
                String estado = modeloTabla.getValueAt(fila, 6).toString();
                if (estado.equals("Pendiente")) {
                    int idPrestamo = (int) modeloTabla.getValueAt(fila, 0);
                    new ProcesarPrestamos(this, idPrestamo, "devolver");
                } else {
                    JOptionPane.showMessageDialog(this, "Este préstamo ya fue devuelto.", "Operación no permitida", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un préstamo para devolver.");
            }
        });

        buscarPrestamosPorUsuario(); // Cargar todos los préstamos al iniciar
        setVisible(true);
    }

    private void buscarPrestamosPorUsuario() {
        modeloTabla.setRowCount(0);
        String busqueda = campoBusqueda.getText().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            CallableStatement stmt;

            if (busqueda.isEmpty()) {
                // Mostrar todos los préstamos
                stmt = conn.prepareCall("{call sp_ObtenerTodosLosPrestamos()}");
            } else {
                // Buscar por usuario
                stmt = conn.prepareCall("{call sp_ObtenerPrestamosPorUsuario(?)}");
                stmt.setString(1, busqueda);
            }

            ResultSet rs = stmt.executeQuery();
            boolean hayDatos = false;

            while (rs.next()) {
                hayDatos = true;
                Timestamp fechaPrestamo = rs.getTimestamp("FechaHoraPrestamo");
                Timestamp fechaDevolucion = rs.getTimestamp("FechaDevolucion");
                Timestamp fechaReal = rs.getTimestamp("FechaRealDevolucion");

                String estado;
                if (fechaReal == null) {
                    estado = "Pendiente";
                } else if (!fechaReal.after(fechaDevolucion)) {
                    estado = "Entregado";
                } else {
                    estado = "Entrega tardía";
                }

                modeloTabla.addRow(new Object[]{
                    rs.getInt("Id"),
                    rs.getString("Titulo"),
                    rs.getString("CodigoCopia"),
                    rs.getString("Usuario"),
                    fechaPrestamo,
                    fechaDevolucion,
                    estado
                });
            }

            mostrarMensajeSiTablaVacia(!hayDatos,
                    busqueda.isEmpty() ? "📭 No hay préstamos registrados." : "📭 No se encontraron préstamos para ese usuario.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar préstamos:\n" + e.getMessage());
        }
    }


    private void mostrarMensajeSiTablaVacia(boolean mostrar, String mensaje) {
        if (mostrar) {
            remove(scrollPane);
            mensajeCentral.setText(mensaje);
            add(mensajeCentral, BorderLayout.CENTER);
        } else {
            remove(mensajeCentral);
            add(scrollPane, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private JPanel crearPanelRedondeado(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
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
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        return panel;
    }

    private void estiloBotonClaro(JButton boton) {
        boton.setFont(new Font("Noto Color Emoji", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBackground(Color.WHITE);
        boton.setForeground(Color.BLACK);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(6, 12, 6, 12)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void centrarContenidoTabla() {
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
            tablaPrestamos.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConsultarPrestamos::new);
    }
}