package repository;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class ProductsServiceRepository {
    String url = "jdbc:postgresql://localhost:5432/productdatabase";
    String user = "postgres";
    String password = "postgres";

    public Connection connectDatabase(){
        try (Connection connection = DriverManager.getConnection(url, user, password)){
            System.out.println("Connected to Postgres database");
            return connection;
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            return null;
        }
    }

    public int executeQuery(Connection connection, String query){
        try (Statement statement = connection.createStatement()) {
            int result = statement.executeUpdate(query);
            System.out.println("Query executed successfully: " + query);
            return result;
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
            return -1;
        }
    }

}
