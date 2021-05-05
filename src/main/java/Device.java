import org.bson.types.ObjectId;

public class Device {

    private final ObjectId deviceID;
    private Customer customer;
    private String model;

    public Device(ObjectId deviceID, Customer customer, String model) {
        this.deviceID = deviceID;
        this.customer = customer;
        this.model = model;
    }
}
