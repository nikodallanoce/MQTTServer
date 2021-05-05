import org.bson.types.ObjectId;

public class Installation {

    private ObjectId installationID;
    private Device device;
    private Sensor sensor;
    private Topic topic;
    private String GPIOPin;

    public Installation(ObjectId installationID, Device device, Sensor sensor, Topic topic, String GPIOPin) {
        this.installationID = installationID;
        this.device = device;
        this.sensor = sensor;
        this.topic = topic;
        this.GPIOPin = GPIOPin;
    }
}
