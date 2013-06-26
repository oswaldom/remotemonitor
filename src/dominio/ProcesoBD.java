package dominio;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author oswaldomaestra
 */
public class ProcesoBD {
  Connection connection;

  private void displaySQLErrors(SQLException e) {
    System.out.println("SQLException: " + e.getMessage());
    System.out.println("SQLState:     " + e.getSQLState());
    System.out.println("VendorError:  " + e.getErrorCode());
  }

  public ProcesoBD() {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
    } catch (Exception e) {
      System.err.println("Unable to find and load MySQL driver(ProcessBD)");
      System.exit(1);
    }
  }

  public void conectarBD() {
    try {
      connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/remotemonitor?user=root&password=root");
    } catch (SQLException e) {
      displaySQLErrors(e);
    }
  }

  public ArrayList<Proceso> executeSQL(Integer id_nodo) {
    try {
      Statement statement = connection.createStatement();
      ArrayList<Proceso> listaProceso = new ArrayList<Proceso>();
      Proceso proceso = new Proceso();
      ResultSet rs = statement.executeQuery("SELECT * FROM remotemonitor.procesos WHERE nodo_id = " + id_nodo);
      
      while (rs.next()) {
        proceso = new Proceso();
        proceso.setId(rs.getString("id"));
        proceso.setPid(rs.getString("id_proceso"));
        proceso.setCpu((rs.getString("cpu")));
        proceso.setRam((rs.getString("ram")));
        proceso.setState(rs.getString("estado"));
        listaProceso.add(proceso);
      }

      rs.close();
      statement.close();
      connection.close();
      return listaProceso;
    } catch (SQLException e) {
      displaySQLErrors(e);
    }
    return null;
  }

}