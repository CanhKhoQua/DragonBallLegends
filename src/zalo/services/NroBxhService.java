package zalo.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NroBxhService {
    

    
    private static NroBxhService instance;
    private String nroServerUrl;
    
    private NroBxhService() {
        String nroHost = getProperty("nro.host", "localhost");
        int nroPort = Integer.parseInt(getProperty("nro.port", "8889"));
        this.nroServerUrl = "http://" + nroHost + ":" + nroPort + "/nro/bxh";
    }
    
    private String getProperty(String key, String defaultValue) {
        try {
            java.io.File propsFile = new java.io.File("settings/zalo.properties");
            if (propsFile.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(propsFile)) {
                    props.load(fis);
                    return props.getProperty(key, defaultValue);
                }
            }
        } catch (Exception e) {
        }
        return defaultValue;
    }
    
    public static NroBxhService gI() {
        if (instance == null) {
            instance = new NroBxhService();
        }
        return instance;
    }
    
    public JSONObject getBxh(String method, int limit) {
        try {
            String urlStr = nroServerUrl + "?method=" + method + "&limit=" + limit;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            
            StringBuilder response = new StringBuilder();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
            
            conn.disconnect();
            
            if (responseCode != 200) {
                return null;
            }
            
            if (response.length() == 0) {
                return null;
            }
            
            JSONParser parser = new JSONParser();
            JSONObject result = (JSONObject) parser.parse(response.toString());
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}

