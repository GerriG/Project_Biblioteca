package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MantenimientoUsuario extends JDialog {

    private JTextField txtNombre, txtApellido, txtNacionalidad, txtCorreo;
    private JPasswordField txtContrasenia;
    private JComboBox<String> cmbSexo;
    private JButton btnGuardar, btnCancelar;
    private Integer idUsuario;
    private boolean modoEliminar;
    private String nombreCompletoUsuario = "";

    public MantenimientoUsuario(JFrame parent, Integer id) {
        this(parent, id, false);
    }

    public MantenimientoUsuario(JFrame parent, Integer id, boolean eliminar) {
        super(parent, true);
        this.idUsuario = id;
        this.modoEliminar = eliminar;

        if (modoEliminar) {
            cargarDatos(); // cargar nombreCompletoUsuario
            int confirm = JOptionPane.showConfirmDialog(parent,
                    "¿Seguro que deseas eliminar a " + nombreCompletoUsuario + "?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                eliminarUsuario();
            }

            dispose(); // cerrar el JDialog después de eliminar o cancelar
            return;
        }

        construirFormulario(parent);
    }

    private void construirFormulario(JFrame parent) {
        setTitle(idUsuario == null ? "Agregar Usuario" : "Editar Usuario");
        setSize(500, 460);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(0, 123, 255));
        JLabel lblTitulo = new JLabel(idUsuario == null ? "📝 Agregar Usuario" : "✏️ Editar Usuario");
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
        cmbSexo = new JComboBox<>(new String[]{"Masculino", "Femenino"});
        txtCorreo = new JTextField(25);
        txtContrasenia = new JPasswordField(25);

        int fila = 0;
        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtNombre, gbc); fila++;

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtApellido, gbc); fila++;

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Nacionalidad:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtNacionalidad, gbc); fila++;

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Sexo:"), gbc);
        gbc.gridx = 1; panelCentro.add(cmbSexo, gbc); fila++;

        gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Correo:"), gbc);
        gbc.gridx = 1; panelCentro.add(txtCorreo, gbc); fila++;

        if (idUsuario == null) {
            gbc.gridx = 0; gbc.gridy = fila; panelCentro.add(new JLabel("Contraseña:"), gbc);
            gbc.gridx = 1; panelCentro.add(txtContrasenia, gbc); fila++;
        }

        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        btnGuardar = new JButton("💾 Guardar");
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
        }

        btnGuardar.addActionListener(e -> {
            if (idUsuario == null) {
                agregarUsuario();
            } else {
                editarUsuario();
            }
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
             CallableStatement stmt = conn.prepareCall("{call sp_ObtenerUsuarioPorId(?)}")) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nombre = rs.getString("Nombre");
                String apellido = rs.getString("Apellido");
                nombreCompletoUsuario = nombre + " " + apellido;

                if (!modoEliminar) {
                    txtNombre.setText(nombre);
                    txtApellido.setText(apellido);
                    txtNacionalidad.setText(rs.getString("Nacionalidad"));
                    cmbSexo.setSelectedItem(rs.getString("Sexo"));
                    txtCorreo.setText(rs.getString("Correo"));
                }
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar datos: " + ex.getMessage());
        }
    }

    private void agregarUsuario() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String nacionalidad = txtNacionalidad.getText().trim();
        String sexo = (String) cmbSexo.getSelectedItem();
        String correo = txtCorreo.getText().trim();
        String contrasenia = new String(txtContrasenia.getPassword()).trim();

        if (nombre.isEmpty() || apellido.isEmpty() || nacionalidad.isEmpty() || correo.isEmpty() || contrasenia.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{call sp_InsertarUsuario(?, ?, ?, ?, ?, ?, ?)}")) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, nacionalidad);
            stmt.setString(4, sexo);
            stmt.setString(5, correo);
            stmt.setString(6, contrasenia);
            stmt.setInt(7, 2);
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

        if (nombre.isEmpty() || apellido.isEmpty() || nacionalidad.isEmpty() || correo.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{call sp_ActualizarUsuario(?, ?, ?, ?, ?)}")) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, nombre);
            stmt.setString(3, apellido);
            stmt.setString(4, nacionalidad);
            stmt.setString(5, correo);
            stmt.executeUpdate();
            mostrarMensaje("Usuario actualizado correctamente.");
            dispose();
        } catch (SQLException e) {
            mostrarError("Error al editar usuario: " + e.getMessage());
        }
    }

    private void eliminarUsuario() {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{call sp_EliminarUsuario(?)}")) {
            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
            mostrarMensaje("Usuario eliminado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al eliminar usuario: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}