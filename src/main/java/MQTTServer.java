import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.*;
import java.util.concurrent.Semaphore;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;


public class MQTTServer {

    private final String identifier;
    private final Mqtt5BlockingClient client;
    private List<Pair<String, String>> topicPayload;
    private List<Pair<Topic, String>> alerts;
    private Semaphore[] subscrAndAlerts;
    private MongoConnection db;
    private TelegramBot bot;


    public MQTTServer(String identifier, String host, MongoConnection db) {
        this.identifier = identifier;
        this.client = createClient(host);
        this.topicPayload = new LinkedList<>();
        this.alerts = new LinkedList<>();
        this.subscrAndAlerts = new Semaphore[]{new Semaphore(0), new Semaphore(0)};
        this.db = db;
        this.bot = new TelegramBot("1794376012:AAFqfMrJD-axHouu8feNxbaixDgP9i4M7LI");
    }

    private Mqtt5BlockingClient createClient(String host) {
        return MqttClient.builder()
                .identifier(identifier)
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();
    }

    public void connect(String username, String password) {
        client.connectWith()
                .cleanStart(false)
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();
        System.out.println("Connected successfully");
    }

    public void listen() {
        upload().start();
        sendAlerts().start();
        client.toAsync().publishes(ALL, publish -> {
            String topic = publish.getTopic().toString();
            String payload = UTF_8.decode(publish.getPayload().orElseThrow()).toString();
            System.out.println("Received message: " + topic + " -> " + payload);
            if (!topic.contains("comunication")) {
                topicPayload.add(new Pair<>(topic.replaceFirst("customer/", ""), payload));
                subscrAndAlerts[0].release(1);
            }
        });
    }

    private Thread upload() {
        Runnable r = () -> {
            while (true) {
                try {
                    subscrAndAlerts[0].acquire(1);
                    Map<String, Topic> topics = new HashMap<>();
                    db.getTopics().forEach(topic -> topics.put(topic.getName(), topic));
                    Pair<String, String> tp = topicPayload.remove(0);
                    if (topics.containsKey(tp.getKey())) {
                        Topic topic = topics.get(tp.getKey());
                        Map<String, Number> field_val = parse(tp.getValue(), new String[]{"temp", "hum"});
                        threshControlQueueInsertion(topic, field_val, Comparator.comparing(Number::doubleValue));
                        db.insertRecordWithControls(topic.getTopicID(), field_val);
                        publish("comunication/" + topic.getName(), topic.getSamplingInterval() + "");
                    } else {
                        System.out.printf("Topic %s non nel DB", tp.getKey());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        return new Thread(r);
    }


    private Thread sendAlerts() {
        Runnable r = () -> {
            while (true) {
                try {
                    subscrAndAlerts[1].acquire(1);
                    Pair<Topic, String> topic_mess = alerts.remove(0);
                    DBOperation ris = db.insertAlertWithTimeControl(topic_mess.getKey(), topic_mess.getValue(), 1);
                    if (ris != DBOperation.VOID) {
                        Customer customerToAlert = db.getCustomerById(topic_mess.getKey().getCustomer().getCustomerID());
                        sendMessageToChat(customerToAlert.getChatID(), topic_mess.getValue());
                        System.out.println(topic_mess);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        return new Thread(r);
    }

    private void sendMessageToChat(int chatID, String message) {
        // Create your bot passing the token received from @BotFather
        SendResponse response = bot.execute(new SendMessage(chatID, message));
    }

    private void threshControlQueueInsertion(Topic t, Map<String, Number> field_val, Comparator<Number> comp) {
        var sampleValue = field_val.get("temp");
        if (comp.compare(sampleValue, t.getTrigger()) > 0) {
            String mess = "ATTENTION: Topic %s with value: %.2f exceeds the threshold: %.2f".formatted(t.getName(),
                    sampleValue.doubleValue(), t.getTrigger().doubleValue());
            alerts.add(new Pair<>(t, mess));
            subscrAndAlerts[1].release(1);
        }
    }

    private Map<String, Number> parse(String input, String[] typeOfSamples) {
        String[] data = input.split(";");
        Map<String, Number> res = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            res.put(typeOfSamples[i], Double.parseDouble(data[i]));
        }
        return res;
    }

    public void subscribe(String topic) {
        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
    }

    private void publish(String topic, String payload) {
        //publish a message to the topic
        client.publishWith()
                .topic(topic)
                .payload(UTF_8.encode(payload))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send();
    }
}


