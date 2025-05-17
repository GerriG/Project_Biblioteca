package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MantenimientoPersonal extends JDialog {

    private JTextField txtNombre, txtApellido, txtNacionalidad, txtCorreo;
    private JPasswordField txtContrasenia;
    private JComboBox<String> comboRol;
    private JButton btnGuardar, btnCancelar;
    private Integer idUsuario;
    private boolean modoEliminar;

    private final String[] rolesPermitidos = {"Administrador", "Secretario", "Bibliotecario"};

    public MantenimientoPersonal(JFrame parent, Integer id) {
        this(parent, id, false);
    }

    public MantenimientoPersonal(JFrame parent, Integer id, boolean eliminar) {
        super(parent, true);
        this.idUsuario = id;
        this.modoEliminar = eliminar;
        setTitle(eliminar ? "Eliminar Usuario" : (id == null ? "Agregar Usuario" : "Editar Usuario"));
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(0, 123, 255));
        JLabel lblTitulo = new JLabel(id == null ? "📝 Agregar Personal" : (modoEliminar ? "🗑️ Eliminar Personal" : "✏️ Editar Personal"));
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);

        JPanel panelCentro = crearPanelRedondeado(new GridBagLayout());
        panelCentro.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre = new JTextField(25);
        txtApellido = new JTextField(25);
        txtNacionalidad = new JTextField(25);
        txtCorreo = new JTextField(25);
        txtContrasenia = new JPasswordField(25);
        comboRol = new JComboBox<>(rolesPermitidos);

        int fila = 0;
        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtNombre, gbc); fila++;

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtApellido, gbc); fila++;

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Nacionalidad:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtNacionalidad, gbc); fila++;

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Correo:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtCorreo, gbc); fila++;

        if (idUsuario == null && !modoEliminar) {
            gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Contraseña:"), gbc);
            gbc.gridx = 1; panelCentro.add(txtContrasenia, gbc); fila++;
        }

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1; panelCentro.add(comboRol, gbc);

        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        btnGuardar = new JButton(modoEliminar ? "🗑️ Eliminar" : "💾 Guardar");
        btnCancelar = new JButton("❌ Cancelar");

        estiloBoton(btnGuardar);
        estiloBoton(btnCancelar);

        panelInferior.add(btnGuardar);
        panelInferior.add(btnCancelar);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        if (idUsuario != null) {
            cargarDatos();
            if (modoEliminar) deshabilitarCampos();
        }

        btnGuardar.addActionListener(e -> {
            if (modoEliminar) eliminarUsuario();
            else if (idUsuario == null) agregarUsuario();
            else editarUsuario();
        });

        btnCancelar.addActionListener(e -> dispose());
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

    private void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT Nombre, Apellido, Nacionalidad, Correo, RolId FROM Usuarios WHERE Id = ?")) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtNombre.setText(rs.getString("Nombre"));
                txtApellido.setText(rs.getString("Apellido"));
                txtNacionalidad.setText(rs.getString("Nacionalidad"));
                txtCorreo.setText(rs.getString("Correo"));
                comboRol.setSelectedItem(obtenerNombreRol(rs.getInt("RolId")));
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar datos: " + ex.getMessage());
        }
    }

    private String obtenerNombreRol(int rolId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT NombreRol FROM Roles WHERE Id = ?")) {
            stmt.setInt(1, rolId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("NombreRol");
        }
        return "";
    }

    private int obtenerIdRol(String nombreRol) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Roles WHERE NombreRol = ?")) {
            stmt.setString(1, nombreRol);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("Id");
        }
        return -1;
    }

    private void agregarUsuario() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String nacionalidad = txtNacionalidad.getText().trim();
        String correo = txtCorreo.getText().trim();
        String contrasenia = new String(txtContrasenia.getPassword()).trim();
        String rol = (String) comboRol.getSelectedItem();

        if (nombre.isEmpty() || apellido.isEmpty() || nacionalidad.isEmpty() || correo.isEmpty() || contrasenia.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Usuarios (Nombre, Apellido, Nacionalidad, Sexo, Correo, Contrasenia, RolId) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, nacionalidad);
            stmt.setString(4, "Masculino");
            stmt.setString(5, correo);
            stmt.setString(6, contrasenia);
            stmt.setInt(7, obtenerIdRol(rol));
            stmt.executeUpdate();
            mostrarMensaje("Usuario agregado correctamente.");
            dispose();
        } catch (SQLException e) {
            mostrarError("Error al agregar usuario: " + e.getMessage());
        }
    }

    private void editarUsuario() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String nacionalidad = txtNacionalidad.getText().trim();
        String correo = txtCorreo.getText().trim();
        String rol = (String) comboRol.getSelectedItem();

        if (nombre.isEmpty() || apellido.isEmpty() || nacionalidad.isEmpty() || correo.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE Usuarios SET Nombre=?, Apellido=?, Nacionalidad=?, Correo=?, RolId=? WHERE Id=?")) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, nacionalidad);
            stmt.setString(4, correo);
            stmt.setInt(5, obtenerIdRol(rol));
            stmt.setInt(6, idUsuario);
            stmt.executeUpdate();
            mostrarMensaje("Usuario actualizado correctamente.");
            dispose();
        } catch (SQLException e) {
            mostrarError("Error al editar usuario: " + e.getMessage());
        }
    }

    private void eliminarUsuario() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar este usuario?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Usuarios WHERE Id=?")) {
            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
            mostrarMensaje("Usuario eliminado correctamente.");
            dispose();
        } catch (SQLException e) {
            mostrarError("Error al eliminar usuario: " + e.getMessage());
        }
    }

    private void deshabilitarCampos() {
        txtNombre.setEnabled(false);
        txtApellido.setEnabled(false);
        txtNacionalidad.setEnabled(false);
        txtCorreo.setEnabled(false);
        comboRol.setEnabled(false);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
