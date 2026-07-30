package zalo.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NroTradeService {
    

    
    private static NroTradeService instance;
    private String nroServerUrl;
    
    private NroTradeService() {
        loadConfig();
    }
    
    public static NroTradeService gI() {
        if (instance == null) {
            instance = new NroTradeService();
        }
        return instance;
    }
    
    private void loadConfig() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("settings/zalo.properties"));
            String nroHost = props.getProperty("nro.host", "localhost");
            int nroPort = Integer.parseInt(props.getProperty("nro.port", "8889"));
            nroServerUrl = "http://" + nroHost + ":" + nroPort + "/nro/trade";
        } catch (Exception e) {
            nroServerUrl = "http://localhost:8889/nro/trade";
        }
    }
    
    public List<Map<String, Object>> getTransactionsByUser(String username, int limit) {
        try {
            String urlStr = nroServerUrl + "?username=" + java.net.URLEncoder.encode(username, "UTF-8") + "&limit=" + limit;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return new ArrayList<>();
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
            JSONArray dataArray = (JSONArray) jsonResponse.get("data");
            
            List<Map<String, Object>> transactions = new ArrayList<>();
            if (dataArray != null) {
                for (Object obj : dataArray) {
                    JSONObject transObj = (JSONObject) obj;
                    Map<String, Object> trans = new HashMap<>();
                    trans.put("player1", transObj.get("player1"));
                    trans.put("player2", transObj.get("player2"));
                    trans.put("items1", transObj.get("items1"));
                    trans.put("items2", transObj.get("items2"));
                    trans.put("time", transObj.get("time"));
                    transactions.add(trans);
                }
            }
            
            return transactions;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

