package Biblioteca;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class BuscarInventario {

    //Parametros para realizar busqueda de libros en inventario
    public static void buscar(String filtro, DefaultTableModel model) {
        try (Connection conn = Biblioteca.DatabaseConnection.getConnection()) {
            String sql = "{CALL sp_BuscarInventario(?)}";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, filtro);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Object[] row = new Object[7];
                    row[0] = rs.getInt("Id");
                    row[1] = rs.getString("CodigoCopia");
                    row[2] = rs.getDate("FechaAdquisicion");
                    row[3] = rs.getString("Estado");
                    row[4] = rs.getString("Titulo");
                    row[5] = rs.getString("Autor");
                    row[6] = rs.getInt("Anio");
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
