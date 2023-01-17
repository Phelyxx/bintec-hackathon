package com.reto.codigoton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author fesar
 */
public class OptionsMenu {


    private static final SingletonConnection consg = null;

    private static void connect() throws SQLException {
        /*
        * Conexión a la base de datos mediante MariaDB
         */
        final String url = "jdbc:mariadb://localhost:3306";
        final String driver = "org.mariadb.jdbc.Driver";
        final String bd = "evalart_reto";
        final String user = "root";
        final String pass = "";

        try {
            consg.getConnection(url, driver, bd, user, pass);
            System.out.println("-> Connecting");

        } catch (SQLException sqlex) {
            System.out.println("-> Database error "
                    + sqlex.getMessage() + "\n");
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        /*
        * El archivo de entrada se encuentra en la carpeta data del proyecto 
        * y se puede seleccionar otro digitando el nombre.
         */
        String filename = System.getProperty("user.dir") + "\\data\\";
        String strmenu = "Digite el archivo de entrada";
        filename += JOptionPane.showInputDialog(strmenu);
        JOptionPane.showMessageDialog(null, getClientsForDinner(filename));
    }

    private static String getClientsForDinner(String filename) throws SQLException, IOException {
        Statement smt = null;
        ResultSet rs = null;
        String res = "";
        try {
            /*
            * Conexión Singleton y consulta SQL
            * La consulta une el id de los clientes en la tabla account con cada cliente
             */
            connect();
            smt = SingletonConnection.getConnection().createStatement();
            System.out.println("--> Start of query ....");
            String query = "select * from client join account on account.client_id = client.id";
            rs = smt.executeQuery(query);
            System.out.println("--> End of query ....");
            double total = 0;
            List<Client> clients = new ArrayList<>();
            while (rs.next()) {
                String code = rs.getString("client.code");
                int male = rs.getInt("client.male");
                int type = rs.getInt("client.type");
                String location = rs.getString("location");
                String company = rs.getString("company");
                int encrypt = rs.getInt("client.encrypt");
                Double balance = rs.getDouble("account.balance");
                /*
                * Para sumar los balances de todas las cuentas de un cliente
                * y dejar un sólo código de cliente, se compara si el siguiente
                * cliente tiene el mismo código, en caso de que sea así suma 
                * sus balances, de lo contrario crea un nuevo objeto de tipo
                * cliente que incluye el total del balance y los otros detalles
                * del cliente.
                 */
                if (!rs.isLast()) {
                    rs.next();
                }
                String codeNext = rs.getString("client.code");
                if (code.equals(codeNext) == true) {
                    total += balance;
                } else {
                    boolean isMale = false;
                    boolean encripted = false;
                    if (male == 1) {
                        isMale = true;
                    }
                    if (encrypt == 1) {
                        encripted = true;
                    }

                    Client cliente = new Client(code, isMale, type, location, company, encripted, total + balance);
                    clients.add(cliente);
                    total = 0;
                }
                if (!rs.isLast()) {
                    rs.previous();
                }
            }
            /*
            * Se itera sobre todas las mesas para obtener sus parámetros (que son
            * extraídos del archivo de entrada) Este algoritmo también sirve
            * cuando llegan menos de 6 mesas por parámetro.
             */
            int mesas = obtenerMesas(filename);
            int i = 1;
            List<Object> pGeneral = obtenerParametros("<General>", filename);
            res += "<General>" + "\n";
            res += searchClients(clients, (int) pGeneral.get(0), (String) pGeneral.get(1),
                    (int) pGeneral.get(2), (int) pGeneral.get(3)) + "\n\n";

            while (i < mesas + 1) {
                List<Object> pMesa = obtenerParametros("<Mesa " + i + ">", filename);
                res += "<Mesa " + i + ">" + "\n";
                /*
                * Se buscan los clientes de acuerdo a los filtros y el resultado
                * se recibe como una cadena.
                 */
                String search = searchClients(clients, (int) pMesa.get(0), (String) pMesa.get(1),
                        (int) pMesa.get(2), (int) pMesa.get(3));
                if (search.isBlank()) {
                    res += "CANCELADA" + "\n\n";
                } else {
                    res += search + "\n\n";
                }
                i += 1;

            }

            System.out.println(res);

        } catch (SQLException sqlex) {
            JOptionPane.showMessageDialog(null, "***> Error al insertar en la base de  datos. "
                    + sqlex.getMessage() + "\n");
            System.out.println("***> Error al consultar la base de  datos. "
                    + sqlex.getMessage() + "\n");
        } finally {
            consg.close(rs);

            consg.close(smt);
        }
        return res;
    }

    private static int obtenerMesas(String filename) throws IOException {
        int mayor = 0;
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            int i = 0;
            /*
            * Busca por el archivo de entrada cuál es el mayor número existente
            * para una mesa
             */
            while (i < lines.size()) {
                String line = lines.get(i);
                if (line.length() > 4 && line.substring(0, 5).equals("<Mesa")) {
                    int numMesa = Integer.parseInt(line.substring(6, 7));
                    if (numMesa > mayor) {
                        mayor = numMesa;
                    }
                }
                i += 1;
            }

        } catch (IOException e) {
            System.out.println(e);
        }
        return mayor;
    }

    private static List<Object> obtenerParametros(String mesa, String filename) throws IOException {
        /*
        *   Busca la línea que contenga el nombre de la mesa, si la encuentra
        *   busca las 4 líneas siguientes, las cuales pueden contener 1 o más
        *   filtros de entrada.
         */
        int tc = 0;
        String ug = "0";
        int ri = 0;
        int rf = 2000000000;
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            int i = 0;
            while (i < lines.size()) {
                String line = lines.get(i);
                if (line.equals(mesa)) {
                    int j = 0;
                    boolean encontrado = true;
                    while (j < 4 && encontrado == true) {
                        if (i + 1 + j < lines.size()) {
                            String nextLine = lines.get(i + 1 + j);
                            String parameter = nextLine.substring(0, 3);
                            if (parameter.equals("TC:")) {
                                tc = Integer.parseInt(nextLine.substring(3, nextLine.length()).strip());
                            } else if (parameter.equals("UG:")) {
                                ug = nextLine.substring(3, nextLine.length()).strip();
                            } else if (parameter.equals("RI:")) {
                                ri = Integer.parseInt(nextLine.substring(3, nextLine.length()).strip());
                            } else if (parameter.equals("RF:")) {
                                rf = Integer.parseInt(nextLine.substring(3, nextLine.length()).strip());
                            } else {
                                encontrado = false;
                            }
                        }
                        j += 1;
                    }
                }
                i += 1;
            }
            int l = 0;
        } catch (IOException e) {
            System.out.println(e);
        }
        List<Object> parameters = Arrays.asList(tc, ug, ri, rf);
        return parameters;
    }

    private static String searchClients(List<Client> clientes, int tc, String ug, int ri, int rf) throws IOException {
        /*
        *   Filtra los clientes en la lista de acuerdo a los parámetros, en caso
        *   de que un parámetro sea igual a 0 significa que en el archivo de
        *   entrada no existe para esa mesa, por lo que no se tiene encuenta
         */
        List<Client> filtered = new ArrayList<>();
        String codes = "";
        for (Client cliente : clientes) {
            int type = cliente.getType();
            String location = cliente.getLocation();
            Double balance = cliente.getBalance();
            if ((type == tc || tc == 0) && (location.equals(ug) || ug.equals("0"))
                    && (balance > ri) && (balance < rf)) {
                filtered.add(cliente);
            }
        }
        String strClients = "";

        if (filtered.size() >= 4) {
            // Ordenar de acuerdo al balance
            Collections.sort(filtered, Comparator.comparingDouble(Client::getBalance).reversed());
            List<String> companies = new ArrayList<>();
            List<Client> filterCompanies = new ArrayList<>();
            List<Client> maleClients = new ArrayList<>();
            List<Client> femaleClients = new ArrayList<>();
            for (Client cliente : filtered) {
                String company = cliente.getCompany();
                // Asegurarse de que no exista un cliente de una compañía repetida
                if (!companies.contains(company)) {
                    // Separar los clientes por sexo
                    if (cliente.isMale()) {
                        maleClients.add(cliente);
                    } else {
                        femaleClients.add(cliente);
                    }
                    if (cliente.isEncrypt()) {
                        cliente.setCode(decryptCode(cliente.getCode()).replace("\"", ""));
                    }
                    companies.add(cliente.getCompany());
                    filterCompanies.add(cliente);
                }
            }
            int i = 0;
            /*
            *   Unir máximo 4 clientes de cada sexo en una nueva lista de forma 
            *   proporcional.
             */
            List<Client> filter = new ArrayList<>();
            while (i < 4 && i < maleClients.size() && i < femaleClients.size()) {
                filter.add(maleClients.get(i));
                filter.add(femaleClients.get(i));
                i += 1;
            }
            // Ordenar de forma ascendente de acuerdo a su código
            Collections.sort(filter, Comparator.comparing(Client::getCode));
            // Ordenar de mayor a menor balance.
            Collections.sort(filter, Comparator.comparingDouble(Client::getBalance).reversed());
            strClients = "";
            int j = 0;
            // Por cada cliente obtener su código separado por ','
            while (j < filter.size()) {
                Client cliente = filter.get(j);
                codes += cliente.getCode() + ',';
                j += 1;
            }
            codes = codes.substring(0, codes.length() - 1);
        }
        return codes;
    }

    public static String decryptCode(String code) throws IOException {
        /*
        *   Realiza una solicitud HTTP GET al webservice y retorna la 
        *   respuesta (el código desencriptado)
        */
        String decrypted = "";
        URL urlGetRequeest = new URL("https://test.evalartapp.com/extapiquest/code_decrypt/" + code);
        String readLine = null;
        HttpURLConnection conection = (HttpURLConnection) urlGetRequeest.openConnection();
        conection.setRequestMethod("GET");
        int responseCode = conection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }
            in.close();
            decrypted = response.toString();
        } else {
            System.out.println("-> Get error");
        }
        return decrypted;
    }
}
