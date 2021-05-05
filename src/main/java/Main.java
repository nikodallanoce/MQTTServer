public class Main {
    public static void main(String[] args) {
        final String host = "37c7a072139e48e380bb5e3df6662706.s1.eu.hivemq.cloud";
        final String username = "server";
        final String password = "Server00";

        MQTTServer server = new MQTTServer("server", host, new MongoConnection("Mqttempv2"));
        server.connect(username, password);
        server.subscribe("#");
        server.listen();
    }
}
