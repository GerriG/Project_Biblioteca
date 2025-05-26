package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class GenerarReportes extends JFrame {

    private JTable tablaReportes;
    private DefaultTableModel modeloTabla;

    public GenerarReportes() {
        setTitle("📊 Reportes de Biblioteca");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ----------------- Panel Superior -----------------
        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(0, 123, 255));
        JLabel titulo = new JLabel("📊 Reporte de Préstamos Actuales");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 20));
        panelSuperior.add(titulo);
        add(panelSuperior, BorderLayout.NORTH);

        // ----------------- Tabla con Reporte -----------------
        modeloTabla = new DefaultTableModel(new Object[]{
                "Usuario", "Correo", "Libro", "Código Copia", "Prestado", "Devolución Programada", "Estado Devolución"
        }, 0);

        tablaReportes = new JTable(modeloTabla);
        tablaReportes.setRowHeight(25);
        tablaReportes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaReportes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaReportes.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaReportes.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablaReportes);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ----------------- Panel Inferior con Botón Exportar -----------------
        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        JButton btnExportar = new JButton("📄 Exportar a PDF");
        btnExportar.setBackground(new Color(40, 167, 69));
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setFont(new Font("Noto Color Emoji", Font.BOLD, 14));
        btnExportar.setFocusPainted(false);
        btnExportar.setPreferredSize(new Dimension(180, 35));
        btnExportar.addActionListener((ActionEvent e) -> {
            ReportePDF.generarDesdeTabla(tablaReportes);
        });

        panelInferior.add(btnExportar);
        add(panelInferior, BorderLayout.SOUTH);

        // ----------------- Cargar datos -----------------
        cargarReportePrestamos();
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
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        return panel;
    }

    private void cargarReportePrestamos() {
        modeloTabla.setRowCount(0);
        String query =
                "SELECT U.Nombre + ' ' + U.Apellido AS Usuario, U.Correo, " +
                "L.Titulo, P.CodigoCopia, P.FechaHoraPrestamo, P.FechaDevolucion, " +
                "D.FechaRealDevolucion " +
                "FROM Prestamos P " +
                "INNER JOIN Usuarios U ON P.UsuarioId = U.Id " +
                "INNER JOIN Libros L ON P.LibroId = L.Id " +
                "INNER JOIN Inventario I ON P.CodigoCopia = I.CodigoCopia " +
                "LEFT JOIN Devoluciones D ON P.Id = D.PrestamoId";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Timestamp fechaPrestamo = rs.getTimestamp("FechaHoraPrestamo");
                Timestamp fechaDevProgramada = rs.getTimestamp("FechaDevolucion");
                Timestamp fechaDevReal = rs.getTimestamp("FechaRealDevolucion");

                String estadoEntrega;
                if (fechaDevReal == null) {
                    estadoEntrega = "Pendiente";
                } else if (fechaDevReal.before(fechaDevProgramada) || fechaDevReal.equals(fechaDevProgramada)) {
                    estadoEntrega = "Entregado";
                } else {
                    estadoEntrega = "Entrega tardía";
                }

                modeloTabla.addRow(new Object[]{
                        rs.getString("Usuario"),
                        rs.getString("Correo"),
                        rs.getString("Titulo"),
                        rs.getString("CodigoCopia"),
                        fechaPrestamo,
                        fechaDevProgramada,
                        estadoEntrega
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

//    Testeo interno
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(GenerarReportes::new);
//    }
}
