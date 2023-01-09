package edu.yu.cs.com3800.stage1;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.yu.cs.com3800.JavaRunner;
import edu.yu.cs.com3800.SimpleServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SimpleServerImpl implements SimpleServer {
    private int port;
    private HttpServer server;
    private static final Logger LOGGER = Logger.getLogger("MyLog");
    private String path = "C:/stage1/";
    FileHandler fh;
    Path p = Paths.get("src/test/java/edu/yu/cs/com3800/stage1",LOGGER.getName().substring(LOGGER.getName().lastIndexOf('.') + 1) + ".log");

    public SimpleServerImpl(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/compileandrun", new MyHandler());
        this.port = port;
        try {
            // This block configure the logger with handler and formatter
            if (!Files.exists(p.getParent())) {
                Files.createDirectory(p.getParent());
            }
            fh = new FileHandler(p + "MyLog.log", true);
            LOGGER.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            LOGGER.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }
    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String rs = "potato";
            Headers headers = t.getRequestHeaders();
            OutputStream os;

            if (!headers.getFirst("Content-Type").contains("text/x-java-source")){
                LOGGER.severe("Content-Type is not of type text/x-java-source");
                t.sendResponseHeaders(400, rs.length());
                os = t.getResponseBody();
                os.write(rs.getBytes());
                os.close();
                return;
            }
            JavaRunner jr = new JavaRunner();
            InputStream is = t.getRequestBody();
            try {
                rs =jr.compileAndRun(new ByteArrayInputStream(is.readAllBytes()));
            } catch (Exception e) {
                rs = e.getMessage();
                rs +="\n";
                ByteArrayOutputStream osa = new ByteArrayOutputStream();
                PrintStream s = new PrintStream(osa);
                e.printStackTrace(s);
                rs += osa.toString(StandardCharsets.UTF_8);
                LOGGER.severe("Exception thrown while compiling and running");
                t.sendResponseHeaders(400, rs.length());
                os = t.getResponseBody();
                os.write(rs.getBytes());
                os.close();
                return;
            }
            LOGGER.info("successful server experience, hope you enjoyed!");
            t.sendResponseHeaders(200, rs.length());
            os = t.getResponseBody();
            os.write(rs.getBytes());
            os.close();
        }
    }
    @Override
    public void start() {
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }
    public static void main(String[] args) {
        int port = 9000;
        if(args.length >0) {
            port = Integer.parseInt(args[0]);
        }
        SimpleServer myserver = null;
        try {
            myserver = new SimpleServerImpl(port);
            myserver.start();
        } catch(Exception e) {
            System.err.println(e.getMessage());
            myserver.stop();
        }
    }
}
