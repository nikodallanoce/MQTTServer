import org.bson.types.ObjectId;

public class Topic {

    private final ObjectId topicID;
    private Customer customer;
    private String name;
    private int samplingInterval;
    private Number trigger;


    public Customer getCustomer() {
        return customer;
    }

    public int getSamplingInterval() {
        return samplingInterval;
    }

    public Number getTrigger() {
        return trigger;
    }

    public ObjectId getTopicID() {
        return topicID;
    }

    public String getName() {
        return name;
    }

    public Topic(ObjectId topicID, Customer customer, String name, int samplingInterval, Number trigger) {
        this.topicID = topicID;
        this.customer = customer;
        this.name = name;
        this.samplingInterval = samplingInterval;
        this.trigger = trigger;
    }
}
