package nobility.proxy.components;

import com.google.gson.Gson;
import javafx.util.Pair;
import nobility.proxy.components.entities.ProxyAnonymity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Manages and makes a request to the API server
 */
public class RequestAPI {

    private final UserSettings settings;
    private final String api_url = "http://api.proxychecker.co/";

    /**
     * The response given by the API server
     */
    public static class Response {

        public String ip;
        public String country;
        public ProxyAnonymity anonymity;

    }

    /**
     * Create a new RequestAPI object to make API requests
     * @param settings - UserSettings
     */
    public RequestAPI(UserSettings settings) {
        this.settings = settings;
    }

    /**
     * Takes a json string and parses it using the Response subclass.
     * @param json - String to parse
     * @return Response
     */
    public Response getResponse(String json) {
        return new Gson().fromJson(json, Response.class);
    }

    /**
     * Takes a HttpURLConnection object and reads the web response onto a String
     * @param connection - HttpURLConnection that has been connected
     * @return Response
     * @throws IOException err
     */
    public Response getResponse(HttpURLConnection connection) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        return new Gson().fromJson(sb.toString(), Response.class);
    }

    /**
     * Given a proxy makes an attempt to connect to the API server, storing and returning
     * the connection and the response time in a javafx.util.Pair
     * @param proxy - The Proxy to use when attempting to connect to the API service
     * @return null if unable to connect or javafx.util.Pair
     */
    public Pair<HttpURLConnection, Long> connect(Proxy proxy) {
        try {
            HttpURLConnection connection;
            if(proxy == null) {
                connection = (HttpURLConnection) new URL(this.api_url).openConnection();
            } else {
                connection = (HttpURLConnection) new URL(this.get_query_url(settings.getIp())).openConnection(proxy);
            }
            connection.setRequestProperty("User-Agent", "Proxy Checker v." + "1.1.2" +
                    " - (proxychecker.co) : " + System.getProperty("os.name") +
                    " v." + System.getProperty("os.version"));
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(settings.getTimeout());
            connection.setReadTimeout(settings.getTimeout());

            long startTime = System.currentTimeMillis();
            connection.connect();
            long endTime = System.currentTimeMillis();
            return new Pair<>(connection, (endTime - startTime));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get the API url to Query based on option ip parameter.
     * @param ip - The current users IP address
     * @return String - API url to query
     */
    private String get_query_url(String ip) {
        if (ip == null) {
            return api_url;
        } else {
            return api_url + "?ip=" + ip;
        }
    }
}
