package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class GestionarPersonal extends JFrame {

    private JTable tablaPersonal;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollPane;
    private JButton btnAgregar, btnEditar, btnEliminar, btnActualizar;

    public GestionarPersonal() {
        setTitle("👥 Gestión de Personal");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBackground(new Color(0, 123, 255));

        JLabel titulo = new JLabel("👥 Personal Activo (Administrador, Secretario, Bibliotecario)");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 16));
        panelSuperior.add(titulo);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Apellido", "Nacionalidad", "Correo", "Rol"}, 0);
        tablaPersonal = new JTable(modeloTabla);
        tablaPersonal.setRowHeight(25);
        tablaPersonal.setFont(new Font("Noto Color Emoji", Font.PLAIN, 13));
        tablaPersonal.getTableHeader().setFont(new Font("Noto Color Emoji", Font.BOLD, 14));
        tablaPersonal.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaPersonal.getTableHeader().setForeground(Color.WHITE);
        tablaPersonal.setBackground(Color.WHITE);
        centrarContenidoTabla();

        scrollPane = new JScrollPane(tablaPersonal);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
            }
        });

        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 250));

        btnAgregar = new JButton("➕ Agregar");
        btnEditar = new JButton("✏️ Editar");
        btnEliminar = new JButton("🗑️ Eliminar");
        btnActualizar = new JButton("🔄 Actualizar");

        estiloBoton(btnAgregar);
        estiloBoton(btnEditar);
        estiloBoton(btnEliminar);
        estiloBoton(btnActualizar);

        panelInferior.add(btnAgregar);
        panelInferior.add(btnEditar);
        panelInferior.add(btnEliminar);
        panelInferior.add(btnActualizar);

        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        cargarPersonal();

        btnActualizar.addActionListener(e -> cargarPersonal());

        btnAgregar.addActionListener(e -> {
            new MantenimientoPersonal(this, null).setVisible(true);
        });

        btnEditar.addActionListener(e -> {
            int fila = tablaPersonal.getSelectedRow();
            if (fila != -1) {
                int id = (int) modeloTabla.getValueAt(fila, 0);
                new MantenimientoPersonal(this, id).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario para editar.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tablaPersonal.getSelectedRow();
            if (fila != -1) {
                int id = (int) modeloTabla.getValueAt(fila, 0);
                new MantenimientoPersonal(this, id, true).setVisible(true); // true = modo eliminar
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario para eliminar.");
            }
        });

        setVisible(true);
    }

    public void cargarPersonal() {
        modeloTabla.setRowCount(0);
        String query = "SELECT U.Id, U.Nombre, U.Apellido, U.Nacionalidad, U.Correo, R.NombreRol " +
                       "FROM Usuarios U INNER JOIN Roles R ON U.RolId = R.Id " +
                       "WHERE R.NombreRol IN ('Administrador', 'Secretario', 'Bibliotecario') " +
                       "ORDER BY R.NombreRol, U.Apellido";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                        rs.getInt("Id"),
                        rs.getString("Nombre"),
                        rs.getString("Apellido"),
                        rs.getString("Nacionalidad"),
                        rs.getString("Correo"),
                        rs.getString("NombreRol")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el personal:\n" + e.getMessage());
        }
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
        boton.setFont(new Font("Noto Color Emoji", Font.PLAIN, 13));
        boton.setFocusPainted(false);
        boton.setBackground(Color.WHITE);
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
            tablaPersonal.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GestionarPersonal::new);
    }
}