package dev.jaqobb.namemc.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HTTPUtils {
    
    private HTTPUtils() {
        throw new UnsupportedOperationException("Cannot create instance of this class");
    }
    
    public static JSONArray getJSONArray(String urlString) throws IOException, ParseException {
        HttpURLConnection urlConnection = createJSONReadConnection(urlString);
        int responseCode = urlConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to get OK response from server: " + responseCode + " - " + urlConnection.getResponseMessage());
        }
        Object parsedResponse = JSONValue.parseWithException(readResponse(urlConnection));
        if (!(parsedResponse instanceof JSONArray)) {
            throw new RuntimeException("Parsed response is not a JSON array");
        }
        return (JSONArray) parsedResponse;
    }
    
    private static HttpURLConnection createJSONReadConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setConnectTimeout(5_000);
        urlConnection.setReadTimeout(5_000);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(false);
        return urlConnection;
    }
    
    private static String readResponse(HttpURLConnection urlConnection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            return responseBuilder.toString();
        }
    }
}
