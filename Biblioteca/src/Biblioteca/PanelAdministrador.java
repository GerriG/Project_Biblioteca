package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class PanelAdministrador extends JFrame {

    //Objetos de ventana
    private JLabel lblAvatar, lblNombre, lblDatos;
    private JButton btnGestionarUsuarios, btnGenerarReportes, btnGestionarPersonal;
    private String correoUsuario;
    private Image fondo;

    //Asignar una imagen de forndo a la ventana y cargar sus UI.
    public PanelAdministrador(String correoUsuario) {
        this.correoUsuario = correoUsuario;
        fondo = new ImageIcon(getClass().getResource("/Biblioteca/Wallpaper/Admin.png")).getImage();
        initUI();
        cargarDatosAdministrador(correoUsuario);
    }

    //Configurar UI de ventana
    private void initUI() {
        setTitle("Panel de Administrador");
        setSize(527, 700); // Tamaño ajustado
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelPrincipal = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        //Configurar Layout de ventana
        panelPrincipal.setLayout(new GridBagLayout());
        panelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));
        panelPrincipal.setOpaque(false);

        RoundedPanel panelContenido = new RoundedPanel(new Color(255, 255, 255, 204), 30);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBorder(new EmptyBorder(30, 67, 30, 67)); // top, left, bottom, right
        panelContenido.setOpaque(false);
        panelContenido.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra el panel en su contenedor

        // Avatar
        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(150, 150));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblAvatar);

        // Nombre
        lblNombre = new JLabel("Bienvenido, Administrador");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblNombre.setForeground(Color.BLACK);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelContenido.add(lblNombre);

        // Datos
        lblDatos = new JLabel();
        lblDatos.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDatos.setForeground(Color.BLACK);
        lblDatos.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDatos.setHorizontalAlignment(SwingConstants.CENTER);
        lblDatos.setBorder(new EmptyBorder(0, 0, 20, 0));
        panelContenido.add(lblDatos);

        // Botones
        btnGestionarUsuarios = new JButton("👥 Gestionar Usuarios");
        btnGenerarReportes = new JButton("📊 Generar Reportes");
        btnGestionarPersonal = new JButton("🛠 Gestionar Personal");

        estiloBoton(btnGestionarUsuarios);
        estiloBoton(btnGenerarReportes);
        estiloBoton(btnGestionarPersonal);

        btnGestionarUsuarios.addActionListener(e -> new GestionarUsuarios().setVisible(true));
        btnGenerarReportes.addActionListener(e -> new GenerarReportes().setVisible(true));
        btnGestionarPersonal.addActionListener(e -> new GestionarPersonal().setVisible(true));

        panelContenido.add(btnGestionarUsuarios);
        panelContenido.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContenido.add(btnGenerarReportes);
        panelContenido.add(Box.createRigidArea(new Dimension(0, 10)));
        panelContenido.add(btnGestionarPersonal);

        panelPrincipal.add(panelContenido);
        setContentPane(panelPrincipal);
    }

    //Cargar los datos del administrador que ha ingresado
    private void cargarDatosAdministrador(String correo) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT Nombre, Apellido, Nacionalidad, Sexo, R.NombreRol "
                + "FROM Usuarios U INNER JOIN Roles R ON U.RolID = R.ID WHERE U.Correo = ?")) {

            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("Nombre");
                String apellido = rs.getString("Apellido");
                String nacionalidad = rs.getString("Nacionalidad");
                String sexo = rs.getString("Sexo");
                String rol = rs.getString("NombreRol");

                // Cargar avatar por sexo
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

    //Formatear botones
    private void estiloBoton(JButton boton) {
        boton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        boton.setFocusPainted(false);
        boton.setBackground(new Color(40, 167, 69));
        boton.setForeground(Color.WHITE);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
