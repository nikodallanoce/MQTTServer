public class Main {
    public static void main(String[] args) {
        final String host = System.getenv("mqttConn");
        final String username = System.getenv("mqttUsr");
        final String password = System.getenv("mqttPsw");

        try {
            System.out.println(System.getenv("tokenBot"));
            System.out.println(System.getenv("mongoConnection"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        MQTTServer server = new MQTTServer("server", host, new MongoConnection("Mqttempv2"));
        server.connect(username, password);
        server.subscribe("#");
        server.listen();
    }
}
