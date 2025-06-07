package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RecuperarContrasena extends JFrame {

    //Objetos de ventana
    private JTextField txtCorreo, txtNombre, txtApellido;
    private JButton btnVerificar;

    //Configurar ventana
    public RecuperarContrasena() {
        setTitle("🔐 Recuperar Contraseña");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel superior
        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(255, 165, 0));
        JLabel lblTitulo = new JLabel("🔐 Recuperar Contraseña");
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);

        // Panel central
        JPanel panelCentro = crearPanelRedondeado(new GridBagLayout());
        panelCentro.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fuenteCampos = new Font("Segoe UI", Font.PLAIN, 14);

        //Crear campos y botones
        txtCorreo = agregarCampo(panelCentro, "Correo:", gbc, 0, fuenteCampos);
        txtNombre = agregarCampo(panelCentro, "Nombre:", gbc, 1, fuenteCampos);
        txtApellido = agregarCampo(panelCentro, "Apellido:", gbc, 2, fuenteCampos);

        btnVerificar = new JButton("🔍 Verificar");
        estiloBoton(btnVerificar);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelCentro.add(btnVerificar, gbc);

        // Panel inferior
        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(255, 230, 180));
        JButton btnCancelar = new JButton("❌ Cancelar");
        estiloBoton(btnCancelar);
        panelInferior.add(btnCancelar);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Eventos
        btnVerificar.addActionListener(e -> verificarYMostrarNuevaVentana());
        btnCancelar.addActionListener(e -> volverAlLogin());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                volverAlLogin();
            }
        });
    }

    //Agregar campos de texto
    private JTextField agregarCampo(JPanel panel, String etiqueta, GridBagConstraints gbc, int fila, Font fuente) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(fuente);
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(lbl, gbc);

        JTextField txt = new JTextField(20);
        txt.setFont(fuente);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5, 10, 5, 10)));
        gbc.gridx = 1;
        panel.add(txt, gbc);

        return txt;
    }

    //Verificar los datos del usuario y proceder a solicitar nueva contraseña
    private void verificarYMostrarNuevaVentana() {
        String correo = txtCorreo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Sexo FROM Usuarios WHERE Correo = ? AND Nombre = ? AND Apellido = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, correo);
            stmt.setString(2, nombre);
            stmt.setString(3, apellido);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String sexo = rs.getString("Sexo");
                mostrarVentanaNuevaContrasena(nombre + " " + apellido, sexo, correo);
                dispose(); // cerrar ventana actual
            } else {
                JOptionPane.showMessageDialog(this, "❌ Los datos no coinciden con ningún usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //Ventana para solcitar nueva contraseña y procesar su cambio
    private void mostrarVentanaNuevaContrasena(String nombreCompleto, String sexo, String correo) {
        JFrame frameNueva = new JFrame("🔐 Recuperar Contraseña");
        frameNueva.setSize(500, 400);
        frameNueva.setLocationRelativeTo(null);
        frameNueva.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameNueva.setLayout(new BorderLayout());
        frameNueva.getContentPane().setBackground(Color.WHITE);

        // Panel superior
        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(255, 165, 0));
        JLabel lblTitulo = new JLabel("🔐 Recuperar Contraseña");
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);

        // Panel centro con mensaje y campos
        JPanel panelCentro = crearPanelRedondeado(new GridBagLayout());
        panelCentro.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblVerificado = new JLabel("✅ Identidad Verificada", JLabel.CENTER);
        lblVerificado.setFont(new Font("Noto Color Emoji", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelCentro.add(lblVerificado, gbc);

        String emoji = sexo.equalsIgnoreCase("Femenino") ? "👩" : "👨";
        JLabel lblNombre = new JLabel(emoji + " " + nombreCompleto, JLabel.CENTER);
        lblNombre.setFont(new Font("Noto Color Emoji", Font.PLAIN, 16));
        gbc.gridy = 1;
        panelCentro.add(lblNombre, gbc);

        JLabel lblNueva = new JLabel("Nueva contraseña:");
        lblNueva.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panelCentro.add(lblNueva, gbc);

        JPasswordField txtNueva = new JPasswordField(20);
        txtNueva.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNueva.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5, 10, 5, 10)));
        gbc.gridx = 1;
        panelCentro.add(txtNueva, gbc);

        JButton btnActualizar = new JButton("💾 Actualizar");
        estiloBoton(btnActualizar);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelCentro.add(btnActualizar, gbc);

        // Panel inferior
        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(255, 230, 180));
        JButton btnCancelar = new JButton("❌ Cancelar");
        estiloBoton(btnCancelar);
        panelInferior.add(btnCancelar);

        // Eventos
        btnActualizar.addActionListener(e -> {
            String nuevaContra = new String(txtNueva.getPassword()).trim();
            if (nuevaContra.isEmpty()) {
                JOptionPane.showMessageDialog(frameNueva, "⚠️ Ingrese la nueva contraseña.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Obtener la contraseña actual
                String select = "SELECT Contrasenia, Nombre, Apellido FROM Usuarios WHERE Correo = ?";
                PreparedStatement stmtSelect = conn.prepareStatement(select);
                stmtSelect.setString(1, correo);
                ResultSet rs = stmtSelect.executeQuery();

                if (rs.next()) {
                    String contraseniaActual = rs.getString("Contrasenia");
                    String nombre = rs.getString("Nombre");
                    String apellido = rs.getString("Apellido");

                    if (nuevaContra.equals(contraseniaActual)) {
                        JOptionPane.showMessageDialog(frameNueva, nombreCompleto + ", la contraseña ingresada actualmente ya está asociada a su cuenta.\n Por motivos de seguridad, debe elegir una diferente.", "Advertencia Contraseña En Uso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Actualizar contraseña
                    String update = "UPDATE Usuarios SET Contrasenia = ? WHERE Correo = ?";
                    PreparedStatement stmtUpdate = conn.prepareStatement(update);
                    stmtUpdate.setString(1, nuevaContra);
                    stmtUpdate.setString(2, correo);

                    if (stmtUpdate.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(frameNueva, "✅ Contraseña actualizada con éxito.");
                        frameNueva.dispose();
                        volverAlLogin();
                    } else {
                        JOptionPane.showMessageDialog(frameNueva, "❌ Error al actualizar la contraseña.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } else {
                    JOptionPane.showMessageDialog(frameNueva, "❌ No se pudo verificar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnCancelar.addActionListener(e -> {
            frameNueva.dispose();
            volverAlLogin();
        });

        frameNueva.add(panelSuperior, BorderLayout.NORTH);
        frameNueva.add(panelCentro, BorderLayout.CENTER);
        frameNueva.add(panelInferior, BorderLayout.SOUTH);
        frameNueva.setVisible(true);
    }

    //Volver al login
    private void volverAlLogin() {
        new LoginBiblioteca().setVisible(true);
        dispose();
    }

    //Titulo redondeado
    private JPanel crearPanelRedondeado(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
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
}
