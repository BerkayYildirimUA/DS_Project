package nintendods.ds_project.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NSAPIService {
    private String ip;
    private int port;
    public NSAPIService(String ip, int port) {
        setIp(ip);
        setPort(port);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String getBaseUrl () {
        return "http://" + ip + ":" + port;
    }

    public String executeErrorDelete(String path) {
        HttpURLConnection connection = null;

        try {
            // Create connection
            System.out.println("URL");
            URL url = new URL(getBaseUrl() + path);

            System.out.println("Connection");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");

            /*
                java.net.MalformedURLException: no protocol: 172.30.0.5:8089/nodes/2223/error
                    at java.base/java.net.URL.<init>(URL.java:772)
                    at java.base/java.net.URL.<init>(URL.java:654)
                    at java.base/java.net.URL.<init>(URL.java:590)
                    at nintendods.ds_project.service.NSAPIService.executeErrorDelete(NSAPIService.java:36)
                    at nintendods.ds_project.Client.main(Client.java:133)
            */

            //  connection.setRequestProperty("Content-Length", Integer.toString(json.getBytes().length));
            //  connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            System.out.println("Request: " + connection.toString());
            System.out.println("Header: " + connection.getHeaderFields().toString());

            // Send request
            DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
            // wr.writeBytes(json);
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            System.out.println(response);
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
