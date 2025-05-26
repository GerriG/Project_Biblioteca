package biblioteca;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Date;

public class HistorialUsuario extends JFrame {

    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;
    private String usuario;

    public HistorialUsuario(String usuario) {
        this.usuario = usuario;

        setTitle("Historial de Préstamos");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        modeloTabla = new DefaultTableModel(new String[]{
            "Título", "Fecha Préstamo", "Fecha Devolución", "Estado"
        }, 0);

        tablaHistorial = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        JPanel panelInferior = new JPanel();
        panelInferior.add(btnCerrar);
        add(panelInferior, BorderLayout.SOUTH);

        cargarHistorial();
    }

    private void cargarHistorial() {
        try (Connection conn = Biblioteca.DatabaseConnection.getConnection()) {
            String sql = "EXEC sp_HistorialPrestamosUsuario ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, usuario);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String estado;
                    Date fechaDevolucion = rs.getDate("FechaDevolucion");
                    Date fechaEntrega = rs.getDate("FechaEntrega");

                    if (fechaEntrega == null) {
                        estado = "Pendiente";
                    } else if (fechaEntrega.after(fechaDevolucion)) {
                        estado = "Entrega tardía";
                    } else {
                        estado = "Entregado";
                    }

                    modeloTabla.addRow(new Object[]{
                        rs.getString("Titulo"),
                        rs.getDate("FechaPrestamo"),
                        fechaDevolucion,
                        estado
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar historial: " + ex.getMessage());
        }
    }
}