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
public class NodoBD {
  Connection connection;

  private void displaySQLErrors(SQLException e) {
    System.out.println("SQLException: " + e.getMessage());
    System.out.println("SQLState:     " + e.getSQLState());
    System.out.println("VendorError:  " + e.getErrorCode());
  }

  public NodoBD() {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
    } catch (Exception e) {
      System.err.println("Unable to find and load MySQL driver(NodeBD)");
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

  public ArrayList<Nodo> executeSQL() {
    try {
      Statement statement = connection.createStatement();
      ArrayList<Nodo> listaNodo = new ArrayList<Nodo>();
      ResultSet rs = statement.executeQuery("SELECT * FROM remotemonitor.nodos");
      ProcesoBD procesoBD = new ProcesoBD();

      while (rs.next()) {
        Nodo nodo = new Nodo();
        nodo.setId(rs.getString("id"));
        nodo.setIp(rs.getString("ip"));
        nodo.setCpu(rs.getString("cpu"));
        nodo.setMemTotal(rs.getString("ram_total"));
        nodo.setMemUsada(rs.getString("ram_usada"));
        nodo.setMemLibre(rs.getString("ram_libre"));
        
        procesoBD = new ProcesoBD();
        procesoBD.conectarBD();
        nodo.setListaProcesos(procesoBD.executeSQL(rs.getInt("id")));
       
        listaNodo.add(nodo);
      }

      rs.close();
      statement.close();
      connection.close();
      return listaNodo;
    } catch (SQLException e) {
      displaySQLErrors(e);
    }
    return null;
  }

}