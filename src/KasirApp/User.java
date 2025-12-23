package KasirApp;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;

public class User {

    public static int uid = -1;

    // Konfigurasi Odoo
    static final String URL_ODOO = "http://localhost:8069";
    static final String DB = "db_coffe_shop";
    public static final String PASSWORD = "admin";

    public boolean login(String username, String password) {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(URL_ODOO + "/xmlrpc/2/common"));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            Object uidObj = client.execute(
                "authenticate",
                new Object[]{DB, username, password, java.util.Collections.emptyMap()}
            );

            if (uidObj instanceof Integer) {
                uid = (int) uidObj;
                return uid > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}