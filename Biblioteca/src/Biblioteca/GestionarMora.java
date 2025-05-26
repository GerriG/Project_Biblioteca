package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestionarMora extends JFrame {

    private JTable tablaMultas;
    private DefaultTableModel modelo;
    private JScrollPane scrollPane;
    private JButton btnPagar;

    public GestionarMora() {
        setTitle("💰 Gestionar Mora");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel superior
        JPanel panelTitulo = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.setBackground(new Color(0, 120, 215));
        panelTitulo.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitulo = new JLabel("💰 Gestionar Mora");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 18));
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // Modelo y tabla
        modelo = new DefaultTableModel(new String[]{"ID", "Usuario", "Días Retraso", "Monto", "Fecha", "Estado"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaMultas = new JTable(modelo);
        tablaMultas.setRowHeight(25);
        tablaMultas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaMultas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaMultas.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaMultas.getTableHeader().setForeground(Color.WHITE);
        tablaMultas.setBackground(Color.WHITE);
        centrarContenidoTabla();

        // Scroll
        scrollPane = new JScrollPane(tablaMultas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
            }
        });

        // Panel inferior
        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));
        panelInferior.setBorder(new EmptyBorder(10, 20, 10, 20)); // ← margen adecuado

        btnPagar = new JButton("💰 Pago de Mora");        
        estiloBoton(btnPagar);
        btnPagar.setEnabled(false);

        btnPagar.addActionListener(e -> pagarMora());

        // Añadir botón dentro de panel auxiliar para mantener espaciado visual
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBoton.setOpaque(false);
        panelBoton.add(btnPagar);

        panelInferior.add(panelBoton);

        // Listener para habilitar botón
        tablaMultas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaMultas.getSelectedRow();
                if (fila >= 0) {
                    String estado = (String) tablaMultas.getValueAt(fila, 5);
                    btnPagar.setEnabled("Pendiente".equalsIgnoreCase(estado));
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        cargarMultas();
        setVisible(true);
    }

    private void cargarMultas() {
        modelo.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_ObtenerMultas}")) {

            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("IdMulta"),
                        rs.getString("NombreUsuario"),
                        rs.getInt("DiasRetraso"),
                        rs.getBigDecimal("Monto"),
                        rs.getDate("FechaMulta"),
                        rs.getString("Estado")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar multas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pagarMora() {
        int fila = tablaMultas.getSelectedRow();
        if (fila < 0) return;

        int idMulta = (int) tablaMultas.getValueAt(fila, 0);
        String usuario = tablaMultas.getValueAt(fila, 1).toString();
        int dias = (int) tablaMultas.getValueAt(fila, 2);
        double monto = Double.parseDouble(tablaMultas.getValueAt(fila, 3).toString());
        String fecha = tablaMultas.getValueAt(fila, 4).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Deseas registrar el pago de esta mora?", "Confirmar Pago", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cs = conn.prepareCall("{call sp_PagarMulta(?)}")) {

                cs.setInt(1, idMulta);
                cs.executeUpdate();

                JOptionPane.showMessageDialog(this, "¡Pago registrado exitosamente!");
                PDFMora.generarPDF(usuario, dias, monto, fecha);
                cargarMultas();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar pago: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void centrarContenidoTabla() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaMultas.getColumnCount(); i++) {
            tablaMultas.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void estiloBoton(JButton boton) {
        boton.setBackground(new Color(0, 120, 215));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Noto Color Emoji", Font.PLAIN, 14)); // Cambiado aquí
        boton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel crearPanelRedondeado(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> new GestionarMora());
    // }
}