package Biblioteca;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class AgregarLibro extends JDialog {
    private JTextField campoTitulo, campoAutor, campoAnio, campoStock;
    private JButton botonGuardar;

    public AgregarLibro(JFrame parent) {
        super(parent, "📘 Agregar nuevo libro", true);
        setSize(500, 420);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior (título)
        JPanel panelTitulo = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.setBackground(new Color(0, 120, 215));
        panelTitulo.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitulo = new JLabel("📘 Agregar nuevo libro");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 18));
        panelTitulo.add(lblTitulo, BorderLayout.WEST);
        add(panelTitulo, BorderLayout.NORTH);

        // Panel central con campos
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(Color.WHITE);
        panelCampos.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fuente = new Font("Noto Color Emoji", Font.PLAIN, 14);

        campoTitulo = new JTextField();
        campoAutor = new JTextField();
        campoAnio = new JTextField();
        campoStock = new JTextField();

        // Campo Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panelCampos.add(crearLabel("📖 Título:", fuente), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panelCampos.add(campoTitulo, gbc);

        // Campo Autor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panelCampos.add(crearLabel("✍️ Autor:", fuente), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panelCampos.add(campoAutor, gbc);

        // Campo Año
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panelCampos.add(crearLabel("📅 Año:", fuente), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panelCampos.add(campoAnio, gbc);

        // Campo Stock
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panelCampos.add(crearLabel("📦 Stock:", fuente), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panelCampos.add(campoStock, gbc);

        add(panelCampos, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(new Color(135, 206, 235));

        botonGuardar = new JButton("💾 Guardar");
        estiloBoton(botonGuardar);
        botonGuardar.addActionListener(e -> guardarLibro());
        panelBotones.add(botonGuardar);

        JButton botonCancelar = new JButton("❌ Cancelar");
        estiloBoton(botonCancelar);
        botonCancelar.addActionListener(e -> dispose());
        panelBotones.add(botonCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JLabel crearLabel(String texto, Font fuente) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(fuente);
        return lbl;
    }

    private void estiloBoton(JButton boton) {
        boton.setFont(new Font("Noto Color Emoji", Font.PLAIN, 14));
        boton.setBackground(new Color(0, 120, 215));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(8, 20, 8, 20));
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

    private void guardarLibro() {
        String titulo = campoTitulo.getText().trim();
        String autor = campoAutor.getText().trim();
        int anio;
        int stock;

        try {
            anio = Integer.parseInt(campoAnio.getText().trim());
            stock = Integer.parseInt(campoStock.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ Año y stock deben ser números enteros.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Libros (titulo, autor, anio, disponible, stock) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, titulo);
            stmt.setString(2, autor);
            stmt.setInt(3, anio);
            stmt.setBoolean(4, true);
            stmt.setInt(5, stock);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Libro agregado correctamente.");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error al agregar libro: " + ex.getMessage());
        }
    }
}