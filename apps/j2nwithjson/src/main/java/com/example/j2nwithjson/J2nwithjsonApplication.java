package com.example.j2nwithjson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class J2nwithjsonApplication {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int version = 1; // 1: Get, 2: Post, 3: Put, 4: Delete

        if (version == 1) {         // Get
            String msgGet = getRESTbyGET("http://localhost:3000/contacts/2");
            //System.out.println(msgGet);
            JSONParser jsonParser = new JSONParser();

            try {
                JSONObject result = (JSONObject)jsonParser.parse(msgGet);
                Iterator<String> keys = result.keySet().iterator();
                for (int i = 0; i < result.keySet().size(); i++) {
                    String key = keys.next();
                    JSONObject result2 = (JSONObject)result.get(key);
                    System.out.println(key + ":" + result2.get("name"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else if (version == 2) {    // Post
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "뽀로로");
            jsonObject.put("email", "pporo@qers.com");
            jsonObject.put("phone", "0100987");

            String msgPost = sendRESTbyPOST("http://localhost:3000/contacts/", jsonObject.toJSONString());
            System.out.println(msgPost);
        }
        else if (version == 3) {    // Put
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "뽀로로2");
            jsonObject.put("email", "pporo2@qers.com");
            jsonObject.put("phone", "0100987");

            String key = "7";

            String msgPut = sendRESTbyPUT("http://localhost:3000/contacts/" + key, jsonObject.toJSONString());
            System.out.println(msgPut);
        }
        else {
            String key = "7";

            String msgDelete = sendRESTbyDELETE("http://localhost:3000/contacts/" + key);
            System.out.println(msgDelete);
        }
    }

    // Get
    public static String getRESTbyGET(String sendUrl) throws IllegalStateException {
        String inputLine = null;
        StringBuilder outResult = new StringBuilder();

        try{
            System.out.println(("REST API Start of GET"));
            URL url = new URL(sendUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 리턴된 결과 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            while ((inputLine = in.readLine()) != null) {
                outResult.append(inputLine);
            }

            conn.disconnect();
            System.out.println(("REST API End of GET"));
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return outResult.toString();
    }

    // Post
    public static String sendRESTbyPOST(String sendUrl, String jsonValue) throws IllegalStateException {
        String inputLine = null;
        StringBuilder outResult = new StringBuilder();

        try{
            System.out.println(("REST API Start of POST"));
            URL url = new URL(sendUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            OutputStream os = conn.getOutputStream();
            os.write(jsonValue.getBytes(StandardCharsets.UTF_8));
            os.flush();

            System.out.println(os);

            // 리턴된 결과 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            while ((inputLine = in.readLine()) != null) {
                outResult.append(inputLine);
            }

            conn.disconnect();
            System.out.println(("REST API End of POST"));
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return outResult.toString();
    }

    // PUT
    public static String sendRESTbyPUT(String sendUrl, String jsonValue) throws IllegalStateException {
        String inputLine = null;
        StringBuilder outResult = new StringBuilder();

        try{
            System.out.println(("REST API Start of PUT"));
            URL url = new URL(sendUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            OutputStream os = conn.getOutputStream();
            os.write(jsonValue.getBytes(StandardCharsets.UTF_8));
            os.flush();

            System.out.println(os);

            // 리턴된 결과 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            while ((inputLine = in.readLine()) != null) {
                outResult.append(inputLine);
            }

            conn.disconnect();
            System.out.println(("REST API End of PUT"));
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return outResult.toString();
    }

    // DELETE
    public static String sendRESTbyDELETE(String sendUrl) throws IllegalStateException {
        String inputLine = null;
        StringBuilder outResult = new StringBuilder();

        try{
            System.out.println(("REST API Start of PUT"));
            URL url = new URL(sendUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            OutputStream os = conn.getOutputStream();
            //os.write(jsonValue.getBytes(StandardCharsets.UTF_8));
            os.flush();

            System.out.println(os);

            // 리턴된 결과 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            while ((inputLine = in.readLine()) != null) {
                outResult.append(inputLine);
            }

            conn.disconnect();
            System.out.println(("REST API End of PUT"));
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return outResult.toString();
    }

}
