package Biblioteca;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PanelUsuario extends JFrame {

    //Objetos de ventana
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;
    private String usuario;
    private String sexoUsuario;
    private String nombre;
    private Image fondo;
    private Image avatarImg;

    //Configurar ventana
    public PanelUsuario(String correoUsuario) {
        this.usuario = correoUsuario;
        this.sexoUsuario = obtenerSexoDesdeSP(correoUsuario);
        this.nombre = SP_ObtenerNombre(correoUsuario);

        // Cargar imágenes
        fondo = new ImageIcon(getClass().getResource("/Biblioteca/Wallpaper/Fondo.jpg")).getImage();
        String avatarFile = sexoUsuario.equalsIgnoreCase("Femenino") ? "Femenino.png" : "Masculino.png";
        avatarImg = new ImageIcon(getClass().getResource("/Biblioteca/Avatares/" + avatarFile)).getImage();

        //Iniciar UI
        initUI();
        cargarHistorial();
    }

    //UI de Panel Usuario
    private void initUI() {
        //Configurar ventana
        setTitle("Panel de Usuario");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Fondo
        JPanel contentPane = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // Panel superior
        JPanel panelSuperior = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                int avatarSize = 60;
                g2d.drawImage(avatarImg, 10, 10, avatarSize, avatarSize, this);
            }
        };
        panelSuperior.setBounds(50, 30, 780, 80);
        panelSuperior.setLayout(null);
        contentPane.add(panelSuperior);

        JLabel labelNombre = new JLabel("Bienvenido, " + nombre);
        labelNombre.setFont(new Font("Arial", Font.BOLD, 18));
        labelNombre.setBounds(80, 25, 400, 30);
        panelSuperior.add(labelNombre);

        // Tabla
        modeloTabla = new DefaultTableModel();
        modeloTabla.setColumnIdentifiers(new String[]{"Libro", "Código", "Fecha Préstamo", "Fecha Devolución", "Estado"});
        tablaHistorial = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setBounds(50, 130, 780, 300);
        contentPane.add(scrollPane);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 30));
        panelBotones.setBounds(0, 460, 900, 100);
        panelBotones.setBackground(new Color(135, 206, 235));
        contentPane.add(panelBotones);

        JButton btnLibros = crearBoton("Libros Disponibles");
        btnLibros.addActionListener(e -> {
            new LibrosDisponibles(usuario).setVisible(true);
            dispose();
        });

        JButton btnMultas = crearBoton("Consultar Mora");
        btnMultas.addActionListener(e -> {
            new ConsultarMultas(usuario).setVisible(true);
            dispose();
        });

        JButton btnCerrarSesion = crearBoton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> {
            new LoginBiblioteca().setVisible(true);
            dispose();
        });

        panelBotones.add(btnLibros);
        panelBotones.add(btnMultas);
        panelBotones.add(btnCerrarSesion);

        // Reescalado dinámico
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int height = getHeight();

                contentPane.setSize(width, height);
                contentPane.repaint();

                panelSuperior.setBounds((int) (width * 0.055), 30, (int) (width * 0.87), 80);
                scrollPane.setBounds((int) (width * 0.055), 130, (int) (width * 0.87), (int) (height * 0.55));
                panelBotones.setBounds(0, height - 125, width, 90);
                panelBotones.revalidate(); // Actualiza la posición de los botones
            }
        });
    }

    //Crear botones
    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(70, 130, 180));
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setPreferredSize(new Dimension(180, 40));
        boton.setBorder(BorderFactory.createEmptyBorder());
        return boton;
    }

    //Cargar el historial del usuario desde la BD mediante SP
    private void cargarHistorial() {
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_HistorialPrestamosUsuario(?)}")) {

            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();

            modeloTabla.setRowCount(0);
            while (rs.next()) {
                String libro = rs.getString("Libro");
                String codigo = rs.getString("CodigoCopia");
                String fechaPrestamo = rs.getString("FechaHoraPrestamo");
                String fechaEntrega = rs.getString("FechaDevolucion");
                String fechaReal = rs.getString("FechaRealDevolucion");
                String estado = calcularEstado(fechaEntrega, fechaReal);

                modeloTabla.addRow(new Object[]{libro, codigo, fechaPrestamo, fechaEntrega, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el historial", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Calcular el estado de entrega de libro
    private String calcularEstado(String fechaEntrega, String fechaReal) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (fechaReal == null) {
                Date entregaEsperada = sdf.parse(fechaEntrega);
                return new Date().after(entregaEsperada) ? "Entrega tardía" : "Pendiente";
            } else {
                return "Entregado";
            }
        } catch (Exception e) {
            return "Desconocido";
        }
    }

    //Obtener el sexo del usuario mediante SP
    private String obtenerSexoDesdeSP(String correo) {
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_ObtenerUsuarioPorCorreo(?)}")) {

            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Sexo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Masculino";
    }

    //Obtener el nombre del usuario mediante SP
    private String SP_ObtenerNombre(String correo) {
        try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall("{call sp_ObtenerUsuarioPorCorreo(?)}")) {

            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Nombre") + " " + rs.getString("Apellido");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Usuario";
    }

    //Testeo interno con credenciales ficticias
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new PanelUsuario("usuario_demo@correo.com").setVisible(true));
//    }
}
