package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

// Clase para panel con esquinas redondeadas y fondo semi-transparente
class RoundedPanel extends JPanel {
    private Color backgroundColor;
    private int cornerRadius = 30;

    public RoundedPanel(Color bgColor, int radius) {
        super();
        backgroundColor = bgColor;
        cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}

public class PanelSecretario extends JFrame {

    private JLabel lblAvatar, lblNombre, lblDatos;
    private JButton btnGestionarLibros, btnPrestamosDevoluciones;
    private String correoUsuario;
    private Image fondo;

    public PanelSecretario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
        fondo = new ImageIcon(getClass().getResource("/Biblioteca/Wallpaper/Fondo.jpg")).getImage();
        initUI();
        cargarDatosSecretaria(correoUsuario);
    }

    private void initUI() {
        setTitle("Panel de Secretaria");
        setSize(527, 700);  // Escalado desde 500x600
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelPrincipal = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panelPrincipal.setLayout(new GridBagLayout());
        panelPrincipal.setBorder(new EmptyBorder(23, 23, 23, 23));  // Escalado de 20
        panelPrincipal.setOpaque(false);

        // Panel de contenido (redondeado y semi-transparente)
        RoundedPanel panelContenido = new RoundedPanel(new Color(255, 255, 255, 204), 30);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBorder(new EmptyBorder(35, 35, 35, 35)); // Escalado de 30
        panelContenido.setOpaque(false);

        // Avatar
        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(158, 175)); // Escalado de 150x150
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblAvatar);

        // Nombre
        lblNombre = new JLabel("Bienvenida, Secretaria");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 21)); // Escalado de 20
        lblNombre.setForeground(Color.BLACK);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setBorder(new EmptyBorder(12, 0, 12, 0)); // Escalado de 10
        panelContenido.add(lblNombre);

        // Datos
        lblDatos = new JLabel();
        lblDatos.setFont(new Font("Segoe UI", Font.PLAIN, 17)); // Escalado de 15
        lblDatos.setForeground(Color.BLACK);
        lblDatos.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDatos.setHorizontalAlignment(SwingConstants.CENTER);
        lblDatos.setBorder(new EmptyBorder(0, 0, 23, 0)); // Escalado de 20
        panelContenido.add(lblDatos);

        // Botones
        btnGestionarLibros = new JButton("📚 Gestionar Libros");
        btnPrestamosDevoluciones = new JButton("📖 Préstamos y Devoluciones");

        estiloBoton(btnGestionarLibros);
        estiloBoton(btnPrestamosDevoluciones);

        btnGestionarLibros.addActionListener(e -> new GestionarLibros().setVisible(true));
        btnPrestamosDevoluciones.addActionListener(e -> new ConsultarPrestamos().setVisible(true));

        panelContenido.add(btnGestionarLibros);
        panelContenido.add(Box.createRigidArea(new Dimension(0, 12))); // Escalado de 10
        panelContenido.add(btnPrestamosDevoluciones);

        panelPrincipal.add(panelContenido);
        setContentPane(panelPrincipal);
    }


    private void cargarDatosSecretaria(String correo) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT Nombre, Apellido, Nacionalidad, Sexo, R.NombreRol FROM Usuarios U INNER JOIN Roles R ON U.RolID = R.ID WHERE U.Correo = ?")) {

            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("Nombre");
                String apellido = rs.getString("Apellido");
                String nacionalidad = rs.getString("Nacionalidad");
                String sexo = rs.getString("Sexo");
                String rol = rs.getString("NombreRol");

                // Avatar
                String avatarPath = "/Biblioteca/Avatares/" + (sexo.equalsIgnoreCase("Femenino") ? "Femenino.png" : "Masculino.png");
                ImageIcon avatarIcon = new ImageIcon(getClass().getResource(avatarPath));
                Image avatarImg = avatarIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblAvatar.setIcon(new ImageIcon(avatarImg));

                lblNombre.setText("¡Bienvenid" + (sexo.equalsIgnoreCase("Femenino") ? "a" : "o") + ", " + nombre + "!");

                lblDatos.setText("<html><div style='text-align: center;'>"
                        + "<b>" + nombre + " " + apellido + "</b><br>"
                        + "<b>Rol: " + rol + "</b><br>"
                        + "Nacionalidad: " + nacionalidad
                        + "</div></html>");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void estiloBoton(JButton boton) {
        boton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        boton.setFocusPainted(false);
        boton.setBackground(new Color(0, 123, 255));
        boton.setForeground(Color.WHITE);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}