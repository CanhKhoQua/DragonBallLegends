package zalo.services;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NroMaintenanceService {
    

    
    private static NroMaintenanceService instance;
    private String nroServerUrl;
    
    private NroMaintenanceService() {
        String nroHost = getProperty("nro.host", "localhost");
        int nroPort = Integer.parseInt(getProperty("nro.port", "8889"));
        this.nroServerUrl = "http://" + nroHost + ":" + nroPort + "/nro/baotri";
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
    
    public static NroMaintenanceService gI() {
        if (instance == null) {
            instance = new NroMaintenanceService();
        }
        return instance;
    }
    
    public boolean startMaintenance(int minutes) {
        try {
            String json = "{\"minutes\":" + minutes + "}";
            
            URL url = new URL(nroServerUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
}

