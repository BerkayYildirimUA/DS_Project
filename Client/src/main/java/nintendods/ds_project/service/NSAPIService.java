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
            
            //  connection.setRequestProperty("Content-Length", Integer.toString(json.getBytes().length));
            //  connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            System.out.println("Request: " + connection.toString());
            System.out.println("Header: " + connection.getHeaderFields().toString());


            /*
                Request: sun.net.www.protocol.http.HttpURLConnection:http://172.30.0.5:8089/nodes/32651/error
                Header: {null=[HTTP/1.1 404], Keep-Alive=[timeout=60], Connection=[keep-alive], Content-Length=[68], Date=[Mon, 29 Apr 2024 08:39:20 GMT], Content-Type=[text/plain;charset=UTF-8]}
                java.net.ProtocolException: Cannot write output after reading input.
                    at java.base/sun.net.www.protocol.http.HttpURLConnection.getOutputStream0(HttpURLConnection.java:1442)
                    at java.base/sun.net.www.protocol.http.HttpURLConnection.getOutputStream(HttpURLConnection.java:1417)
                    at nintendods.ds_project.service.NSAPIService.executeErrorDelete(NSAPIService.java:62)
                    at nintendods.ds_project.Client.main(Client.java:133)
                            */


            int responseCode = connection.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                System.out.println(response);
                return response.toString();
            } else {
                System.out.println("GET request did not work.");
                return "";
            }
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
