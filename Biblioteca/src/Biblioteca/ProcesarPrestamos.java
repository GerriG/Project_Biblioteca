package Biblioteca;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class ProcesarPrestamos extends JDialog {

    //Objetos de ventana
    private JComboBox<String> comboLibros;
    private JComboBox<String> comboCopias;
    private JComboBox<String> comboUsuarios;
    private JButton btnGuardar;
    private int prestamoId = -1;
    private String modo;

    //Configurar ventana y botones
    public ProcesarPrestamos(JFrame parent, Integer id, String modo) {
        super(parent, true);
        this.modo = modo;
        if (id != null) {
            this.prestamoId = id;
        }

        setTitle(modo.equals("nuevo") ? "Nuevo Préstamo" : modo.equals("editar") ? "Editar Préstamo" : "Procesar Devolución");
        setSize(500, 320);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior (azul)
        JPanel panelTitulo = crearPanelRedondeado(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.setBackground(new Color(0, 120, 215));
        panelTitulo.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitulo = new JLabel((modo.equals("nuevo") ? "📚 Nuevo Préstamo" : modo.equals("editar") ? "✏️ Editar Préstamo" : "📥 Procesar Devolución"));
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCampos.add(crearLabel("📖 Libro:", fuente), gbc);
        gbc.gridx = 1;
        comboLibros = new JComboBox<>();
        comboLibros.setFont(fuente);
        panelCampos.add(comboLibros, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCampos.add(crearLabel("🔢 Copia:", fuente), gbc);
        gbc.gridx = 1;
        comboCopias = new JComboBox<>();
        comboCopias.setFont(fuente);
        panelCampos.add(comboCopias, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCampos.add(crearLabel("👤 Usuario:", fuente), gbc);
        gbc.gridx = 1;
        comboUsuarios = new JComboBox<>();
        comboUsuarios.setFont(fuente);
        panelCampos.add(comboUsuarios, gbc);

        add(panelCampos, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(new Color(135, 206, 235));

        btnGuardar = new JButton(modo.equals("devolver") ? "📥 Devolver" : "💾 Guardar");
        estiloBoton(btnGuardar);
        panelBotones.add(btnGuardar);

        JButton btnCancelar = new JButton("❌ Cancelar");
        estiloBoton(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        // Cargar datos y configurar lógica
        cargarDatos();

        if (modo.equals("editar") || modo.equals("devolver")) {
            cargarPrestamo(prestamoId);
            if (modo.equals("devolver")) {
                comboLibros.setEnabled(false);
                comboCopias.setEnabled(false);
                comboUsuarios.setEnabled(false);
            }
        }

        btnGuardar.addActionListener(e -> {
            switch (modo) {
                case "nuevo" ->
                    insertarPrestamo();
                case "editar" ->
                    actualizarPrestamo();
                case "devolver" ->
                    procesarDevolucion();
            }
        });

        setVisible(true);
    }

    //Crear etiquetas
    private JLabel crearLabel(String texto, Font fuente) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(fuente);
        return lbl;
    }

    //Formatear botones
    private void estiloBoton(JButton boton) {
        boton.setFont(new Font("Noto Color Emoji", Font.PLAIN, 14));
        boton.setBackground(new Color(0, 120, 215));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(8, 20, 8, 20));
    }

    //Cargar datos desde la BD
    private void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement psLibros = conn.prepareStatement("SELECT id, titulo FROM Libros WHERE disponible = 1");
            ResultSet rsLibros = psLibros.executeQuery();
            while (rsLibros.next()) {
                comboLibros.addItem(rsLibros.getInt("id") + " - " + rsLibros.getString("titulo"));
            }

            PreparedStatement psCopias = conn.prepareStatement("SELECT CodigoCopia FROM Inventario WHERE Estado = 'Disponible'");
            ResultSet rsCopias = psCopias.executeQuery();
            while (rsCopias.next()) {
                comboCopias.addItem(rsCopias.getString("CodigoCopia"));
            }

            PreparedStatement psUsuarios = conn.prepareStatement("SELECT Id, Nombre, Apellido FROM Usuarios");
            ResultSet rsUsuarios = psUsuarios.executeQuery();
            while (rsUsuarios.next()) {
                comboUsuarios.addItem(rsUsuarios.getInt("Id") + " - " + rsUsuarios.getString("Nombre") + " " + rsUsuarios.getString("Apellido"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error al cargar datos:\n" + e.getMessage());
        }
    }

    //Cargar prestamos
    private void cargarPrestamo(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT LibroId, CodigoCopia, UsuarioId FROM Prestamos WHERE Id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                seleccionarItemPorId(comboLibros, rs.getInt("LibroId"));
                seleccionarItemExacto(comboCopias, rs.getString("CodigoCopia"));
                seleccionarItemPorId(comboUsuarios, rs.getInt("UsuarioId"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error al cargar préstamo:\n" + e.getMessage());
        }
    }

    //Insertar prestamos
    private void insertarPrestamo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            CallableStatement cs = conn.prepareCall("{call sp_InsertarPrestamo(?, ?, ?)}");
            cs.setInt(1, obtenerIdSeleccionado(comboLibros));
            cs.setString(2, (String) comboCopias.getSelectedItem());
            cs.setInt(3, obtenerIdSeleccionado(comboUsuarios));
            cs.execute();
            JOptionPane.showMessageDialog(this, "✅ Préstamo registrado exitosamente.");
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error al insertar préstamo:\n" + e.getMessage());
        }
    }

    //Actualizar prestamos en la BD
    private void actualizarPrestamo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            CallableStatement cs = conn.prepareCall("{call sp_ActualizarPrestamo(?, ?, ?, ?)}");
            cs.setInt(1, prestamoId);
            cs.setInt(2, obtenerIdSeleccionado(comboLibros));
            cs.setString(3, (String) comboCopias.getSelectedItem());
            cs.setInt(4, obtenerIdSeleccionado(comboUsuarios));
            cs.execute();
            JOptionPane.showMessageDialog(this, "✅ Préstamo actualizado exitosamente.");
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error al actualizar préstamo:\n" + e.getMessage());
        }
    }

    //Procesar devoluciones de libros
    private void procesarDevolucion() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            CallableStatement cs = conn.prepareCall("{call sp_ProcesarDevolucion(?)}");
            cs.setInt(1, prestamoId);
            cs.execute();
            JOptionPane.showMessageDialog(this, "📦 Devolución procesada exitosamente.");
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Error al procesar devolución:\n" + e.getMessage());
        }
    }

    //Obtener el ID del prestamo seleccionado
    private int obtenerIdSeleccionado(JComboBox<String> combo) {
        String seleccionado = (String) combo.getSelectedItem();
        return Integer.parseInt(seleccionado.split(" - ")[0]);
    }

    //Seleccionar por ID
    private void seleccionarItemPorId(JComboBox<String> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).startsWith(id + " -")) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void seleccionarItemExacto(JComboBox<String> combo, String valor) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(valor)) {
                combo.setSelectedIndex(i);
                break;
            }
        }
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
}
