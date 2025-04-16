package Biblioteca;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AgregarLibro extends JDialog {
    private JTextField campoTitulo, campoAutor, campoAnio, campoStock;

    public AgregarLibro(JFrame parent) {
        super(parent, "Agregar nuevo libro", true);
        setLayout(new GridLayout(6, 2, 10, 10));
        setSize(400, 300);
        setLocationRelativeTo(parent);

        campoTitulo = new JTextField();
        campoAutor = new JTextField();
        campoAnio = new JTextField();
        campoStock = new JTextField();

        add(new JLabel("Título:"));
        add(campoTitulo);
        add(new JLabel("Autor:"));
        add(campoAutor);
        add(new JLabel("Año:"));
        add(campoAnio);
        add(new JLabel("Stock:"));
        add(campoStock);
        add(new JLabel());

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardarLibro());
        add(botonGuardar);

        setVisible(true);
    }

    private void guardarLibro() {
        String titulo = campoTitulo.getText().trim();
        String autor = campoAutor.getText().trim();
        int anio;
        int stock;

        try {
            anio = Integer.parseInt(campoAnio.getText().trim());
            stock = Integer.parseInt(campoStock.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Año y stock deben ser números enteros.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Libros (titulo, autor, anio, disponible, stock) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, titulo);
            stmt.setString(2, autor);
            stmt.setInt(3, anio);
            stmt.setBoolean(4, true);
            stmt.setInt(5, stock);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Libro agregado correctamente");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar libro: " + ex.getMessage());
        }
    }
}