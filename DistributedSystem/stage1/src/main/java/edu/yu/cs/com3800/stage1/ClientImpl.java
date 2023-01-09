package edu.yu.cs.com3800.stage1;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClientImpl implements Client{
    private String hostName;
    private int hostPort;
    private HttpClient client = HttpClient.newHttpClient();
    private HttpResponse<String> response;;
    public ClientImpl(String hostName, int hostPort) throws MalformedURLException {
        this.hostName = hostName;
        this.hostPort = hostPort;
    }
    @Override
    public void sendCompileAndRunRequest(String src) throws IOException {

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(src))
                .uri(URI.create("http://" + hostName + ":" + hostPort + "/compileandrun"))
                .header("Content-Type", "text/x-java-source")
                .build();
        response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
    }

    @Override
    public Response getResponse() throws IOException {
        Response r = new Response(response.statusCode(), response.body());
        return r;
    }
}
