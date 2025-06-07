package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class MantenimientoInventario extends JDialog {

    //Objetos de ventana
    private JTextField txtCodigo;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbLibros;
    private JDateChooser dateFechaAdquisicion;
    private Integer idInventario;
    private boolean modoEliminar;
    private String codigoLibro = "";
    private String estadoActual = "";
    private Map<String, Integer> mapLibros = new HashMap<>();

    //Metodo para ser clase hija de la ventana GestionarInventario
    public MantenimientoInventario(JFrame parent, Integer id) {
        this(parent, id, false);
    }

    //Configurar acciones de ventana
    public MantenimientoInventario(JFrame parent, Integer id, boolean eliminar) {
        super(parent, true);
        this.idInventario = id;
        this.modoEliminar = eliminar;

        if (modoEliminar) {
            cargarDatos(); // Cargar datos para confirmar estado
            if ("Prestado".equalsIgnoreCase(estadoActual)) {
                mostrarError("No se puede eliminar una copia con estado 'Prestado'.");
                dispose();
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(parent,
                    "¿Seguro que deseas eliminar la copia con código \"" + codigoLibro + "\"?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                eliminarInventario();
            }
            dispose();
            return;
        }

        construirFormulario(parent);
    }

    //Crear formulario para mantenimiento
    private void construirFormulario(JFrame parent) {
        setTitle(idInventario == null ? "Agregar Inventario" : "Editar Inventario");
        setSize(500, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel panelSuperior = crearPanelRedondeado(new FlowLayout(FlowLayout.CENTER));
        panelSuperior.setBackground(new Color(0, 123, 255));
        JLabel lblTitulo = new JLabel(idInventario == null ? "➕ Agregar Copia" : "✏️ Editar Copia");
        lblTitulo.setFont(new Font("Noto Color Emoji", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);

        JPanel panelCentro = crearPanelRedondeado(new GridBagLayout());
        panelCentro.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCodigo = new JTextField(25);
        cmbEstado = new JComboBox<>(new String[]{"Disponible", "Dañado", "En reparación"});
        cmbLibros = new JComboBox<>();
        dateFechaAdquisicion = new JDateChooser();
        dateFechaAdquisicion.setDateFormatString("yyyy-MM-dd");
        dateFechaAdquisicion.setDate(new Date());

        cargarLibros();

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentro.add(new JLabel("Libro:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(cmbLibros, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentro.add(new JLabel("Código de Copia:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(txtCodigo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentro.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(cmbEstado, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentro.add(new JLabel("Fecha de Adquisición:"), gbc);
        gbc.gridx = 1;
        panelCentro.add(dateFechaAdquisicion, gbc);

        JPanel panelInferior = crearPanelRedondeado(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(135, 206, 235));

        JButton btnGuardar = new JButton("💾 Guardar");
        JButton btnCancelar = new JButton("❌ Cancelar");

        estiloBoton(btnGuardar);
        estiloBoton(btnCancelar);

        panelInferior.add(btnGuardar);
        panelInferior.add(btnCancelar);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        if (idInventario != null) {
            cargarDatos();
            if ("Prestado".equalsIgnoreCase(estadoActual)) {
                mostrarError("No se puede editar una copia con estado 'Prestado'.");
                dispose();
                return;
            }
        }

        btnGuardar.addActionListener(e -> {
            if (idInventario == null) {
                agregarInventario();
            } else {
                editarInventario();
            }
        });

        btnCancelar.addActionListener(e -> dispose());
    }

    //Cargar libros de BD
    private void cargarLibros() {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT id, titulo FROM Libros")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                cmbLibros.addItem(titulo);
                mapLibros.put(titulo, id);
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar libros: " + ex.getMessage());
        }
    }

    //Crear titulos redondeados
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

    //Cargar datos de la BD mediante SP
    private void cargarDatos() {
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_ObtenerInventarioPorId(?)}")) {
            stmt.setInt(1, idInventario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                codigoLibro = rs.getString("CodigoCopia");
                estadoActual = rs.getString("Estado");

                if (!modoEliminar) {
                    txtCodigo.setText(codigoLibro);
                    cmbEstado.setSelectedItem(estadoActual);
                    Date fecha = rs.getDate("FechaAdquisicion");
                    dateFechaAdquisicion.setDate(fecha);

                    int libroId = rs.getInt("LibroId");
                    for (Map.Entry<String, Integer> entry : mapLibros.entrySet()) {
                        if (entry.getValue().equals(libroId)) {
                            cmbLibros.setSelectedItem(entry.getKey());
                            break;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar datos: " + ex.getMessage());
        }
    }

    //Cargar una nueva copia de libro al inventario
    private void agregarInventario() {
        String codigo = txtCodigo.getText().trim();
        String estado = (String) cmbEstado.getSelectedItem();
        String tituloSeleccionado = (String) cmbLibros.getSelectedItem();
        Integer libroId = mapLibros.get(tituloSeleccionado);
        Date fecha = dateFechaAdquisicion.getDate();

        if (codigo.isEmpty()) {
            mostrarError("El código de copia no puede estar vacío.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_InsertarInventario(?, ?, ?, ?)}")) {
            stmt.setInt(1, libroId);
            stmt.setString(2, codigo);
            stmt.setString(3, estado);
            stmt.setDate(4, new java.sql.Date(fecha.getTime()));
            stmt.executeUpdate();
            mostrarMensaje("Copia agregada correctamente.");
            dispose();
        } catch (SQLException e) {
            mostrarError("Error al agregar copia: " + e.getMessage());
        }
    }

    //Editar un linro del inventario en la BD
    private void editarInventario() {
        String codigo = txtCodigo.getText().trim();
        String estado = (String) cmbEstado.getSelectedItem();
        String tituloSeleccionado = (String) cmbLibros.getSelectedItem();
        Integer libroId = mapLibros.get(tituloSeleccionado);
        Date fecha = dateFechaAdquisicion.getDate();

        if (codigo.isEmpty()) {
            mostrarError("El código de copia no puede estar vacío.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_ActualizarInventario(?, ?, ?, ?, ?)}")) {
            stmt.setInt(1, idInventario);
            stmt.setInt(2, libroId);
            stmt.setString(3, codigo);
            stmt.setString(4, estado);
            stmt.setDate(5, new java.sql.Date(fecha.getTime()));
            stmt.executeUpdate();
            mostrarMensaje("Copia actualizada correctamente.");
            dispose();
        } catch (SQLException e) {
            mostrarError("Error al editar copia: " + e.getMessage());
        }
    }

    //Eliminar una copia de libro de inventario en la BD
    private void eliminarInventario() {
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_EliminarInventario(?)}")) {
            stmt.setInt(1, idInventario);
            stmt.executeUpdate();
            mostrarMensaje("Copia eliminada correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al eliminar copia: " + e.getMessage());
        }
    }

    //Mostrar errores
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //Mostrar mensajes
    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
