package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Registrar extends JFrame {

    //Objetos de ventana
    private JTextField txtNombre, txtApellido, txtNacionalidad, txtCorreo;
    private JPasswordField txtContrasena;
    private JComboBox<String> comboSexo;
    private JButton btnRegistrar, btnCancelar;

    //Configurar ventana
    public Registrar() {
        setTitle("📝 Registro de Usuario");
        setSize(500, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.WHITE);

        // Panel superior
        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(0, 123, 255));
        JLabel lblTitulo = new JLabel("📝 Registro de Usuario");
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

        txtNombre = agregarCampo(panelCentro, "Nombre:", gbc, 0, fuenteCampos);
        txtApellido = agregarCampo(panelCentro, "Apellido:", gbc, 1, fuenteCampos);
        txtNacionalidad = agregarCampo(panelCentro, "Nacionalidad:", gbc, 2, fuenteCampos);

        JLabel lblSexo = new JLabel("Sexo:");
        lblSexo.setFont(fuenteCampos);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentro.add(lblSexo, gbc);

        comboSexo = new JComboBox<>(new String[]{"Masculino", "Femenino"});
        comboSexo.setFont(fuenteCampos);
        comboSexo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelCentro.add(comboSexo, gbc);

        txtCorreo = agregarCampo(panelCentro, "Correo:", gbc, 4, fuenteCampos);
        txtContrasena = new JPasswordField(20);
        txtContrasena.setFont(fuenteCampos);
        txtContrasena.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5, 10, 5, 10)));
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(fuenteCampos);
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelCentro.add(lblPass, gbc);
        gbc.gridx = 1;
        gbc.gridy = 5;
        panelCentro.add(txtContrasena, gbc);

        // Panel inferior
        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        btnRegistrar = new JButton("✅ Registrar");
        btnCancelar = new JButton("❌ Cancelar");

        estiloBoton(btnRegistrar);
        estiloBoton(btnCancelar);

        panelInferior.add(btnRegistrar);
        panelInferior.add(btnCancelar);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Acciones
        btnRegistrar.addActionListener(e -> registrarUsuario());
        btnCancelar.addActionListener(e -> volverAlLogin());

        // Al cerrar ventana manualmente, volver al login
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
        gbc.gridy = fila;
        panel.add(txt, gbc);

        return txt;
    }
    
    //Registrar un nuevo usuario a la BD
    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String nacionalidad = txtNacionalidad.getText().trim();
        String sexo = comboSexo.getSelectedItem().toString();
        String correo = txtCorreo.getText().trim();
        String contrasena = new String(txtContrasena.getPassword()).trim();

        if (nombre.isEmpty() || apellido.isEmpty() || nacionalidad.isEmpty()
                || correo.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, completa todos los campos.", "⚠️ Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Usuarios (Nombre, Apellido, Nacionalidad, Sexo, Correo, Contrasenia) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, nacionalidad);
            stmt.setString(4, sexo);
            stmt.setString(5, correo);
            stmt.setString(6, contrasena);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "🎉 Registro exitoso.");
            volverAlLogin();

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "🚫 El correo ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error en el registro.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Volver al Login al finalizar
    private void volverAlLogin() {
        new LoginBiblioteca().setVisible(true);
        dispose();
    }

    //Crear titulo redondeado
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
