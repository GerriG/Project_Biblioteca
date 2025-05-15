package Biblioteca;

// Dependencias
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL de conexión a SQL Server usando localhost
    private static final String URL = "jdbc:sqlserver://localhost;databaseName=BibliotecaDB;integratedSecurity=true;encrypt=false";

    // Método para obtener la conexión
    public static Connection getConnection() {
        try {
            // Intentar establecer la conexión con SQL Server
            Connection connection = DriverManager.getConnection(URL);
            
            return connection;
        } catch (SQLException e) {
            // Si ocurre un error, se captura y se imprime el mensaje
            System.out.println("❌ Error al conectar a SQL Server: " + e.getMessage());
            e.printStackTrace(); // Opcional, para obtener más detalles del error
            return null;
        }
    }

    // Método para probar la conexión
    public static void testConnection() {
        Connection connection = getConnection();
        if (connection != null) {
            try {
                // Verificar si la conexión está viva
                if (!connection.isClosed()) {
                    System.out.println("✅ Conexión válida");
                    // Cerrar la conexión después de la prueba
                    connection.close();
                } else {
                    System.out.println("❌ La conexión está cerrada.");
                }
            } catch (SQLException e) {
                System.out.println("❌ Error al verificar la conexión: " + e.getMessage());
            }
        } else {
            System.out.println("❌ No se pudo establecer la conexión.");
        }
    }

//    // Método principal para probar la conexión
//    public static void main(String[] args) {
//        testConnection();
//    }
}