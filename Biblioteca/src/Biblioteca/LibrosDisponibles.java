package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class LibrosDisponibles extends JFrame {

    //Objetos de ventana
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollPane;
    private JLabel mensajeCentral;
    private String usuario;

    //Configurar ventana
    public LibrosDisponibles(String usuario) {
        this.usuario = usuario;

        setTitle("📚 Libros Disponibles");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel superior
        JPanel panelTitulo = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.setBackground(new Color(0, 120, 215));
        panelTitulo.setBorder(new EmptyBorder(12, 20, 12, 20));

        //Asignar fuente a titulos
        JLabel lblTitulo = new JLabel("📚 Libros Disponibles");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 18));
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // Modelo y tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "Disponible"}, 0);
        tablaLibros = new JTable(modeloTabla);
        tablaLibros.setRowHeight(25);
        tablaLibros.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaLibros.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaLibros.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaLibros.getTableHeader().setForeground(Color.WHITE);
        tablaLibros.setBackground(Color.WHITE);
        centrarContenidoTabla();

        // ScrollPane
        scrollPane = new JScrollPane(tablaLibros);
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

        JButton btnPrestar = new JButton("📥 Solicitar Préstamo");
        estiloBoton(btnPrestar);
        btnPrestar.addActionListener(e -> realizarPrestamo());

        JButton btnCerrar = new JButton("🚪 Cerrar");
        estiloBoton(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        panelInferior.add(btnPrestar);
        panelInferior.add(btnCerrar);

        // Mensaje central
        mensajeCentral = new JLabel("", SwingConstants.CENTER);
        mensajeCentral.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Redirección al cerrar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                new PanelUsuario(usuario).setVisible(true);
            }
        });

        //Llamar metodo para cargar libros
        cargarLibros();
        setVisible(true);
    }

    //Metodo para realizar prestamos y registrarlos en la BD
    private void realizarPrestamo() {
        int filaSeleccionada = tablaLibros.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro primero.");
            return;
        }

        int idLibro = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String titulo = (String) modeloTabla.getValueAt(filaSeleccionada, 1);

        try (Connection conn = Biblioteca.DatabaseConnection.getConnection()) {

            String sqlUsuario = "SELECT Nombre, Sexo FROM Usuarios WHERE Correo = ?";
            String nombre = "", sexo = "";

            try (PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario)) {
                stmtUsuario.setString(1, usuario);
                ResultSet rs = stmtUsuario.executeQuery();
                if (rs.next()) {
                    nombre = rs.getString("Nombre");
                    sexo = rs.getString("Sexo");
                }
            }

            String sqlCodigo = "SELECT TOP 1 CodigoCopia FROM Inventario WHERE LibroId = ? AND Estado = 'Disponible'";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCodigo)) {
                stmt.setInt(1, idLibro);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String codigo = rs.getString("CodigoCopia");
                    String saludo = sexo.equals("Femenino") ? "Estimada" : "Estimado";
                    JOptionPane.showMessageDialog(this,
                            saludo + " " + nombre + ", acérquese a un secretario y proporcione el siguiente código de préstamo:\n"
                            + "📘 *" + titulo + "*\n📄 Código: " + codigo,
                            "Solicitud de Préstamo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Este libro no tiene copias disponibles actualmente.");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar disponibilidad:\n" + ex.getMessage());
        }
    }

    //Metodo para cargar libros de la BD
    private void cargarLibros() {
        modeloTabla.setRowCount(0);
        try (Connection conn = Biblioteca.DatabaseConnection.getConnection()) {
            String sql = "EXEC sp_LibrosDisponibles";
            try (CallableStatement stmt = conn.prepareCall(sql); ResultSet rs = stmt.executeQuery()) {

                boolean hayLibros = false;

                while (rs.next()) {
                    hayLibros = true;
                    int idLibro = rs.getInt("Id");
                    String titulo = rs.getString("Titulo");
                    String autor = rs.getString("Autor");
                    int stock = rs.getInt("Stock");

                    // Llamar al SP que obtiene las copias disponibles
                    String disponibilidad;
                    int disponibles = 0;

                    try (PreparedStatement spDisponibles = conn.prepareStatement("EXEC sp_CopiasDisponiblesPorLibro ?")) {
                        spDisponibles.setInt(1, idLibro);
                        ResultSet rsDisp = spDisponibles.executeQuery();

                        while (rsDisp.next()) {
                            disponibles++;
                        }

                        if (disponibles > 0) {
                            disponibilidad = "Si (" + disponibles + "/" + stock + ")";
                        } else {
                            disponibilidad = "No";
                        }
                    }

                    modeloTabla.addRow(new Object[]{idLibro, titulo, autor, disponibilidad});
                }

                mostrarMensajeSiTablaVacia(!hayLibros, "📭 No hay libros disponibles en este momento.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar libros:\n" + ex.getMessage());
        }
    }

    //Mensaje en caso de que la tabla este vacia
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

    //Centrar el contenido de la tabla
    private void centrarContenidoTabla() {
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
            tablaLibros.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }
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

    // Para pruebas
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new LibrosDisponibles("lucia.perez@correo.comia").setVisible(true));
//    }
}
