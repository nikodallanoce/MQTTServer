import org.bson.types.ObjectId;

public class Customer {

    private final ObjectId customerID;
    private String name;

    public int getChatID() {
        return chatID;
    }

    private int chatID;

    public Customer(ObjectId customerID, String name, int chatID) {
        this.customerID = customerID;
        this.name = name;
        this.chatID = chatID;
    }

}
