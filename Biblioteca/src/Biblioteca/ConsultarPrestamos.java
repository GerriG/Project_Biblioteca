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
    private JButton botonActualizar;
    private JLabel mensajeCentral;
    private JScrollPane scrollPane;

    public ConsultarPrestamos() {
        setTitle("📄 Consultar Préstamos");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.WHITE);
        setBackground(Color.WHITE);

        // Panel superior
        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBackground(new Color(0, 123, 255));

        botonActualizar = new JButton("🔄 Actualizar");
        estiloBoton(botonActualizar);
        JLabel labelTitulo = new JLabel("Historial de Préstamos:");
        labelTitulo.setForeground(Color.WHITE);
        labelTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelSuperior.add(labelTitulo);
        panelSuperior.add(botonActualizar);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Título", "Código Copia", "Prestado Por", "Fecha Préstamo", "Fecha Devolución"}, 0);
        tablaPrestamos = new JTable(modeloTabla);
        tablaPrestamos.setRowHeight(25);
        tablaPrestamos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaPrestamos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaPrestamos.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaPrestamos.getTableHeader().setForeground(Color.WHITE);
        tablaPrestamos.setBackground(Color.WHITE);
        centrarContenidoTabla();

        scrollPane = new JScrollPane(tablaPrestamos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
            }
        });

        // Mensaje central
        mensajeCentral = new JLabel("", SwingConstants.CENTER);
        mensajeCentral.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Agregar componentes
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Acciones
        cargarPrestamos();
        botonActualizar.addActionListener(e -> cargarPrestamos());

        setVisible(true);
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

    private void cargarPrestamos() {
        modeloTabla.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT P.Id, L.titulo, P.CodigoCopia, U.Nombre + ' ' + U.Apellido AS Usuario,
                       P.FechaHoraPrestamo, P.FechaDevolucion
                FROM Prestamos P
                INNER JOIN Libros L ON P.LibroId = L.id
                INNER JOIN Usuarios U ON P.UsuarioId = U.Id
                ORDER BY P.FechaHoraPrestamo DESC
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            boolean hayDatos = false;
            while (rs.next()) {
                hayDatos = true;
                modeloTabla.addRow(new Object[]{
                        rs.getInt("Id"),
                        rs.getString("titulo"),
                        rs.getString("CodigoCopia"),
                        rs.getString("Usuario"),
                        rs.getTimestamp("FechaHoraPrestamo"),
                        rs.getTimestamp("FechaDevolucion")
                });
            }

            mostrarMensajeSiTablaVacia(!hayDatos, "📭 No hay préstamos registrados todavía.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar préstamos:\n" + e.getMessage());
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