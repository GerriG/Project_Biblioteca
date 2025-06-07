package Biblioteca;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class BuscarLibro {

    //Metodo y parametros para buscar libros por sus datos.
    public static void buscarPorTitulo(String titulo, DefaultTableModel model) {
        try (Connection conn = Biblioteca.DatabaseConnection.getConnection()) {
            String sql = "SELECT id_libro, titulo, autor, anio, estado, stock FROM libros WHERE titulo LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + titulo + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Object[] row = new Object[6];
                    row[0] = rs.getInt("id_libro");
                    row[1] = rs.getString("titulo");
                    row[2] = rs.getString("autor");
                    row[3] = rs.getInt("anio");
                    row[4] = rs.getString("estado");
                    row[5] = rs.getInt("stock");
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
