package Biblioteca;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditarLibro extends JDialog {
    private JTextField campoTitulo, campoAutor, campoAnio, campoStock;
    private int libroId;

    public EditarLibro(JFrame parent, int libroId, String titulo, String autor, int anio, int stock) {
        super(parent, "Editar libro", true);
        this.libroId = libroId;

        setLayout(new GridLayout(6, 2, 10, 10));
        setSize(400, 300);
        setLocationRelativeTo(parent);

        campoTitulo = new JTextField(titulo);
        campoAutor = new JTextField(autor);
        campoAnio = new JTextField(String.valueOf(anio));
        campoStock = new JTextField(String.valueOf(stock));

        add(new JLabel("Título:"));
        add(campoTitulo);
        add(new JLabel("Autor:"));
        add(campoAutor);
        add(new JLabel("Año:"));
        add(campoAnio);
        add(new JLabel("Stock:"));
        add(campoStock);
        add(new JLabel());

        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(e -> actualizarLibro());
        add(botonActualizar);

        setVisible(true);
    }

    private void actualizarLibro() {
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
            String sql = "UPDATE Libros SET titulo = ?, autor = ?, anio = ?, stock = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, titulo);
            stmt.setString(2, autor);
            stmt.setInt(3, anio);
            stmt.setInt(4, stock);
            stmt.setInt(5, libroId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Libro actualizado correctamente");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage());
        }
    }
}