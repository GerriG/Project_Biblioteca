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

public class ConsultarMultas extends JFrame {

    //Configurar objetos de la ventana
    private JTable tablaMultas;
    private DefaultTableModel modeloTabla;
    private JScrollPane scrollPane;
    private JLabel mensajeCentral;
    private String usuario;

    public ConsultarMultas(String usuario) {
        this.usuario = usuario;

        //Configurar ventana
        setTitle("💰 Consultar Mora");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel superior
        JPanel panelTitulo = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.setBackground(new Color(0, 120, 215));
        panelTitulo.setBorder(new EmptyBorder(12, 20, 12, 20));

        //Asignar fuente de titulo
        JLabel lblTitulo = new JLabel("💰 Consultar Mora");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 18));
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // Modelo y tabla
        modeloTabla = new DefaultTableModel(new String[]{
            "Título", "Código de Copia", "Días de Retraso", "Monto ($)", "Fecha", "Estado"
        }, 0);

        tablaMultas = new JTable(modeloTabla);
        tablaMultas.setRowHeight(25);
        tablaMultas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaMultas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaMultas.getTableHeader().setBackground(new Color(100, 149, 237));
        tablaMultas.getTableHeader().setForeground(Color.WHITE);
        tablaMultas.setBackground(Color.WHITE);
        centrarContenidoTabla();

        // ScrollPane
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

        JButton btnCerrar = new JButton("🚪 Cerrar");
        estiloBoton(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

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

        cargarMultas();
        setVisible(true);
    }

    //Metodo para guardar las multas en la BD
    private void cargarMultas() {
        modeloTabla.setRowCount(0);
        try (Connection conn = Biblioteca.DatabaseConnection.getConnection()) {
            CallableStatement stmt = conn.prepareCall("EXEC sp_MultasPorUsuario ?");
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();

            boolean hayMultas = false;
            while (rs.next()) {
                hayMultas = true;
                String titulo = rs.getString("Titulo");
                String codigoCopia = rs.getString("CodigoCopia");
                int diasRetraso = rs.getInt("DiasRetraso");
                double monto = rs.getDouble("Monto");
                Date fecha = rs.getDate("FechaMulta");
                String estado = rs.getString("Estado");

                modeloTabla.addRow(new Object[]{
                    titulo, codigoCopia, diasRetraso, String.format("%.2f", monto), fecha.toString(), estado
                });
            }

            mostrarMensajeSiTablaVacia(!hayMultas, "✅ No tienes mora pendiente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar multas:\n" + ex.getMessage());
        }
    }

    //Mnesaje que se mostrara en caso de que el usuario no tenga multas
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

    //Centrar el mensaje en la ventana
    private void centrarContenidoTabla() {
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
            tablaMultas.getColumnModel().getColumn(i).setCellRenderer(centrado);
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
//        SwingUtilities.invokeLater(() -> new ConsultarMultas("carlos.morales@correo.com").setVisible(true));
//    }
}
