package edu.yu.cs.com3800.stage1;

import edu.yu.cs.com3800.SimpleServer;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

public class Stage1Test {
    private String clazz = """
            public class Clazz{
                public Clazz(){
                        
                }
                public String run(){
                    return "success!";
                }
            }
            """;
    @Test
    public void TestClientAndServer() throws IOException {
        SimpleServer server = new SimpleServerImpl(9000);
        server.start();
        Client client = new ClientImpl("localhost", 9000);
        client.sendCompileAndRunRequest(clazz);
        Client.Response r = client.getResponse();
        System.out.println("Expected response:");
        System.out.println("[success!]");
        System.out.println("Actual response:");
        System.out.println("[" + r.getBody() + "]");
        assertEquals(200, r.getCode());
        assertEquals("success!", r.getBody());
        server.stop();
    }
    @Test
    public void TestServerDoesntWork() throws IOException {
        clazz = """
            public class Clazz{
                public Clazz(int i){
                        
                }
                public String run(){
                    return "success!";
                }
            }
            """;
        SimpleServer server = new SimpleServerImpl(9000);
        server.start();
        Client client = new ClientImpl("localhost", 9000);
        client.sendCompileAndRunRequest(clazz);
        Client.Response r = client.getResponse();
        System.out.println("Expected response:");
        System.out.println("""
                [Could not create and run instance of class
                               at edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:156)
                               at edu.yu.cs.com3800.JavaRunner.compileAndRun(JavaRunner.java:70)
                               at edu.yu.cs.com3800.stage1.SimpleServerImpl$MyHandler.handle(SimpleServerImpl.java:63)
                               at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                               at jdk.httpserver/sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:82)
                               at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:98)
                               at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:733)
                               at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                               at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:700)
                               at jdk.httpserver/sun.net.httpserver.ServerImpl$DefaultExecutor.execute(ServerImpl.java:159)
                               at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.handle(ServerImpl.java:447)
                               at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.run(ServerImpl.java:413)
                               at java.base/java.lang.Thread.run(Thread.java:833)
                           Caused by: java.lang.NoSuchMethodException: Clazz.<init>()
                               at java.base/java.lang.Class.getConstructor0(Class.java:3585)
                               at java.base/java.lang.Class.getDeclaredConstructor(Class.java:2754)
                               at edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:148)
                               ... 12 more]
                    """);
        System.out.println("Actual response:");
        System.out.println("[" + r.getBody() + "]");
        assertEquals(400, r.getCode());
        assertEquals("""
                Could not create and run instance of class
                java.lang.ReflectiveOperationException: Could not create and run instance of class
                	at edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:156)
                	at edu.yu.cs.com3800.JavaRunner.compileAndRun(JavaRunner.java:70)
                	at edu.yu.cs.com3800.stage1.SimpleServerImpl$MyHandler.handle(SimpleServerImpl.java:63)
                	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                	at jdk.httpserver/sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:82)
                	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:98)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:733)
                	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:700)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$DefaultExecutor.execute(ServerImpl.java:159)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.handle(ServerImpl.java:447)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.run(ServerImpl.java:413)
                	at java.base/java.lang.Thread.run(Thread.java:833)
                Caused by: java.lang.NoSuchMethodException: Clazz.<init>()
                	at java.base/java.lang.Class.getConstructor0(Class.java:3585)
                	at java.base/java.lang.Class.getDeclaredConstructor(Class.java:2754)
                	at edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:148)
                	... 12 more
                    """, r.getBody());
        server.stop();
    }
    @Test
    public void TestNonJDKImport() throws IOException {
        clazz = """
            import org.apache.commons.net.ftp.FTPClient;
            public class Clazz{
                public Clazz(){
                        
                }
                public String run(){
                    return "success!";
                }
            }
            """;
        SimpleServer server = new SimpleServerImpl(9000);
        server.start();
        Client client = new ClientImpl("localhost", 9000);
        client.sendCompileAndRunRequest(clazz);
        Client.Response r = client.getResponse();
        System.out.println("Expected response:");
        System.out.println("""
                [Code did not compile:
                  Error on line 1, column 34 in string:///Clazz.java
                  
                  java.lang.IllegalArgumentException: Code did not compile:
                  Error on line 1, column 34 in string:///Clazz.java
                  
                    at edu.yu.cs.com3800.JavaRunner.compileFromString(JavaRunner.java:91)
                    at edu.yu.cs.com3800.JavaRunner.compileAndRun(JavaRunner.java:68)
                    at edu.yu.cs.com3800.stage1.SimpleServerImpl$MyHandler.handle(SimpleServerImpl.java:63)
                    at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                    at jdk.httpserver/sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:82)
                    at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:98)
                    at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:733)
                    at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                    at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:700)
                    at jdk.httpserver/sun.net.httpserver.ServerImpl$DefaultExecutor.execute(ServerImpl.java:159)
                    at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.handle(ServerImpl.java:447)
                    at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.run(ServerImpl.java:413)
                    at java.base/java.lang.Thread.run(Thread.java:833)]
                    """);
        System.out.println("Actual response:");
        System.out.println("[" + r.getBody() + "]");
        assertEquals(400, r.getCode());
        assertEquals("""
Code did not compile:
Error on line 1, column 34 in string:///Clazz.java

java.lang.IllegalArgumentException: Code did not compile:
Error on line 1, column 34 in string:///Clazz.java

	at edu.yu.cs.com3800.JavaRunner.compileFromString(JavaRunner.java:91)
	at edu.yu.cs.com3800.JavaRunner.compileAndRun(JavaRunner.java:68)
	at edu.yu.cs.com3800.stage1.SimpleServerImpl$MyHandler.handle(SimpleServerImpl.java:63)
	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
	at jdk.httpserver/sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:82)
	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:98)
	at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:733)
	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
	at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:700)
	at jdk.httpserver/sun.net.httpserver.ServerImpl$DefaultExecutor.execute(ServerImpl.java:159)
	at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.handle(ServerImpl.java:447)
	at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.run(ServerImpl.java:413)
	at java.base/java.lang.Thread.run(Thread.java:833)
                    """, r.getBody());
        server.stop();
    }
    @Test
    public void TestClientAndServerTwice() throws IOException {
        SimpleServer server = new SimpleServerImpl(9000);
        server.start();
        Client client = new ClientImpl("localhost", 9000);
        client.sendCompileAndRunRequest(clazz);
        Client.Response r = client.getResponse();
        assertEquals(200, r.getCode());
        assertEquals("success!", r.getBody());
        String c = """
            public class Clazz{
                public Clazz(){
                        
                }
                public String run(){
                    return "success x2!";
                }
            }
            """;
        client.sendCompileAndRunRequest(c);
        Client.Response r2 = client.getResponse();
        assertEquals(200, r2.getCode());
        assertEquals("success x2!", r2.getBody());
        System.out.println("Expected response:");
        System.out.println("[success x2!]");
        System.out.println("Actual response:");
        System.out.println("[" + r2.getBody() + "]");

        server.stop();
    }
    @Test
    public void TestClientAndServerBadThenGood() throws IOException {
        SimpleServer server = new SimpleServerImpl(9000);
        server.start();
        Client client = new ClientImpl("localhost", 9000);
        client.sendCompileAndRunRequest(clazz);
        Client.Response r = client.getResponse();
        assertEquals(200, r.getCode());
        assertEquals("success!", r.getBody());
        String c = """
            public class Clazz{
                public Clazz(int i){
                        
                }
                public String run(){
                    return "success!";
                }
            }
            """;
        client.sendCompileAndRunRequest(c);
        Client.Response r2 = client.getResponse();
        assertEquals(400, r2.getCode());
        assertEquals("""
                Could not create and run instance of class
                java.lang.ReflectiveOperationException: Could not create and run instance of class
                	at edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:156)
                	at edu.yu.cs.com3800.JavaRunner.compileAndRun(JavaRunner.java:70)
                	at edu.yu.cs.com3800.stage1.SimpleServerImpl$MyHandler.handle(SimpleServerImpl.java:63)
                	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                	at jdk.httpserver/sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:82)
                	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:98)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:733)
                	at jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:700)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$DefaultExecutor.execute(ServerImpl.java:159)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.handle(ServerImpl.java:447)
                	at jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.run(ServerImpl.java:413)
                	at java.base/java.lang.Thread.run(Thread.java:833)
                Caused by: java.lang.NoSuchMethodException: Clazz.<init>()
                	at java.base/java.lang.Class.getConstructor0(Class.java:3585)
                	at java.base/java.lang.Class.getDeclaredConstructor(Class.java:2754)
                	at edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:148)
                	... 12 more
                	""", r2.getBody());
        System.out.println("Expected response:");
        System.out.println("[Could not create and run instance of class\n" +
                "java.lang.ReflectiveOperationException: Could not create and run instance of class\n" +
                "\tat edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:156)\n" +
                "\tat edu.yu.cs.com3800.JavaRunner.compileAndRun(JavaRunner.java:70)\n" +
                "\tat edu.yu.cs.com3800.stage1.SimpleServerImpl$MyHandler.handle(SimpleServerImpl.java:63)\n" +
                "\tat jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)\n" +
                "\tat jdk.httpserver/sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:82)\n" +
                "\tat jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:98)\n" +
                "\tat jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:733)\n" +
                "\tat jdk.httpserver/com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:95)\n" +
                "\tat jdk.httpserver/sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:700)\n" +
                "\tat jdk.httpserver/sun.net.httpserver.ServerImpl$DefaultExecutor.execute(ServerImpl.java:159)\n" +
                "\tat jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.handle(ServerImpl.java:447)\n" +
                "\tat jdk.httpserver/sun.net.httpserver.ServerImpl$Dispatcher.run(ServerImpl.java:413)\n" +
                "\tat java.base/java.lang.Thread.run(Thread.java:833)\n" +
                "Caused by: java.lang.NoSuchMethodException: Clazz.<init>()\n" +
                "\tat java.base/java.lang.Class.getConstructor0(Class.java:3585)\n" +
                "\tat java.base/java.lang.Class.getDeclaredConstructor(Class.java:2754)\n" +
                "\tat edu.yu.cs.com3800.JavaRunner.runClass(JavaRunner.java:148)\n" +
                "\t... 12 more]");
        System.out.println("Actual response:");
        System.out.println("[" + r2.getBody() + "]");

        server.stop();
    }

}
