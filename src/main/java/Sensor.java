import org.bson.types.ObjectId;

public class Sensor {

    private final ObjectId sensorID;
    private String model;

    public Sensor(ObjectId sensorID, String model) {
        this.sensorID = sensorID;
        this.model = model;
    }
}
