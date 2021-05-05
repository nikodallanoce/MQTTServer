import org.bson.types.ObjectId;

public class Customer {

    private final ObjectId customerID;
    private String name;
    private int chatID;

    public ObjectId getCustomerID() {
        return customerID;
    }

    public int getChatID() {
        return chatID;
    }


    public Customer(ObjectId customerID, String name, int chatID) {
        this.customerID = customerID;
        this.name = name;
        this.chatID = chatID;
    }

}
