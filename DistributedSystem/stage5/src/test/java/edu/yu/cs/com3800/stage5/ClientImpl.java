package edu.yu.cs.com3800.stage5;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClientImpl{
    private String hostName;
    private int hostPort;
    //HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private HttpResponse<String> response;;
    public ClientImpl(String hostName, int hostPort) throws MalformedURLException {
        this.hostName = hostName;
        this.hostPort = hostPort;
    }
    class Response {
        private int code;
        private String body;

        public Response(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public int getCode() {
            return this.code;
        }

        public String getBody() {
            return this.body;
        }
    }

    public void sendCompileAndRunRequest(String src) throws IOException {

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(src))
                .uri(URI.create("http://" + hostName + ":" + hostPort + "/compileandrun"))
                .header("Content-Type", "text/x-java-source")
                .build();
        response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
    }
    public HttpURLConnection sendLeaderRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }


    public Response getResponse() throws IOException {
        Response r = new Response(response.statusCode(), response.body());
        return r;
    }
}
