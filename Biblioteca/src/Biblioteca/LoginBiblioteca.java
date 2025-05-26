package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginBiblioteca extends JFrame {

    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnSalir, btnRegistrar;
    private JLabel lblOlvidaste;

    public LoginBiblioteca() {
        initUI();
    }

    private void initUI() {
        setTitle("📚 Biblioteca Login");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.WHITE);

        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(0, 123, 255));

        JLabel lblTitulo = new JLabel("📚 Biblioteca Login");
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);

        JPanel panelCentro = crearPanelRedondeado(new GridBagLayout());
        panelCentro.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsuario = new JLabel("Correo:");
        lblUsuario.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        panelCentro.add(lblUsuario, gbc);

        txtCorreo = new JTextField(25);
        txtCorreo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 10, 5, 10)));
        gbc.gridx = 1; gbc.gridy = 0;
        panelCentro.add(txtCorreo, gbc);

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1;
        panelCentro.add(lblPassword, gbc);

        txtPassword = new JPasswordField(25);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 10, 5, 10)));
        gbc.gridx = 1; gbc.gridy = 1;
        panelCentro.add(txtPassword, gbc);

        lblOlvidaste = new JLabel("<html><u>¿Olvidaste tu contraseña?</u></html>");
        lblOlvidaste.setForeground(Color.BLUE);
        lblOlvidaste.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblOlvidaste.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 2;
        panelCentro.add(lblOlvidaste, gbc);

        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        btnLogin = new JButton("🔑 Iniciar sesión");
        btnRegistrar = new JButton("📝 Registrarse");
        btnSalir = new JButton("❌ Salir");

        estiloBoton(btnLogin);
        estiloBoton(btnRegistrar);
        estiloBoton(btnSalir);

        panelInferior.add(btnLogin);
        panelInferior.add(btnRegistrar);
        panelInferior.add(btnSalir);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> {
            String correo = txtCorreo.getText().trim();
            String contraseña = new String(txtPassword.getPassword()).trim();

            if (correo.isEmpty() || contraseña.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            AutentificarUsuario auth = new AutentificarUsuario();
            if (auth.validarUsuario(correo, contraseña)) {
                String rol = obtenerRolUsuario(correo);
                if (rol != null) {
                    if (rol.equalsIgnoreCase("Secretario")) {
                        JOptionPane.showMessageDialog(this, "Inicio de sesión exitoso.");
                        new PanelSecretario(correo).setVisible(true);
                        dispose();
                    } else if (rol.equalsIgnoreCase("Administrador")) {
                        JOptionPane.showMessageDialog(this, "Inicio de sesión exitoso.");
                        new PanelAdministrador(correo).setVisible(true);
                        dispose();
                    } else if (rol.equalsIgnoreCase("Bibliotecario")) {
                        JOptionPane.showMessageDialog(this, "Inicio de sesión exitoso.");
                        new PanelBibliotecario(correo).setVisible(true);
                        dispose();
                    } else if (rol.equalsIgnoreCase("Usuario")) {
                        JOptionPane.showMessageDialog(this, "Inicio de sesión exitoso.");
                        new PanelUsuario(correo).setVisible(true);
                        dispose();                    
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Inicio de sesión exitoso.\nRol '" + rol + "' aún no está implementado.",
                                "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRegistrar.addActionListener(e -> {
            new Registrar().setVisible(true);
            dispose();
        });

        btnSalir.addActionListener(e -> System.exit(0));

        lblOlvidaste.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new RecuperarContrasena().setVisible(true);
                dispose();
            }
        });
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
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

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

    private String obtenerRolUsuario(String correo) {
        String rol = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT R.NombreRol FROM Usuarios U INNER JOIN Roles R ON U.RolID = R.ID WHERE U.Correo = ?")) {

            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                rol = rs.getString("NombreRol");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el rol del usuario:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return rol;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginBiblioteca().setVisible(true));
    }
} 