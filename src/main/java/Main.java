import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class Main {
    public static void main(String[] args) {
        final String host = "37c7a072139e48e380bb5e3df6662706.s1.eu.hivemq.cloud";
        final String username = "server";
        final String password = "Server00";

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

    private static void sendMessageToChat() {
        // Create your bot passing the token received from @BotFather
        TelegramBot bot = new TelegramBot("1794376012:AAFqfMrJD-axHouu8feNxbaixDgP9i4M7LI");
        SendResponse response = bot.execute(new SendMessage(-549095250, "Buongiorno un cazzo"));
    }
}
