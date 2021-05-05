import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.push;

// ...
public class MongoConnection {
    private final ConnectionString connString = new ConnectionString(
            "mongodb+srv://nikodallanoce:pieroangela@clustertest.zbdu9.mongodb.net/w=majority"
    );
    MongoDatabase db;

    public MongoDatabase getDb() {
        return db;
    }

    public MongoConnection(String database) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        this.db = mongoClient.getDatabase(database);
    }

    public DBOperation insertRecordWithControls(ObjectId topicID, Map<String, Number> field_val) {
        var today = LocalDate.now();
        var now = LocalTime.now().toEpochSecond(today, ZoneOffset.of("Z"));
        Document tempDoc = new Document().append("val", field_val.get("temp")).append("time", new BsonTimestamp(now));
        Document humDoc = new Document().append("val", field_val.get("hum")).append("time", new BsonTimestamp(now));
        var dbOp = DBOperation.VOID;
        if (db.getCollection("Records").countDocuments(and(eq("topicID", topicID), eq("date", today))) > 0) {
            Bson update = combine(push("temp", tempDoc), push("hum", humDoc));
            db.getCollection("Records")
                    .updateOne(and(eq("topicID", topicID), eq("date", today)), update);
            dbOp = DBOperation.UPDATE;
        } else {
            db.getCollection("Records").insertOne(new Document()
                    .append("topicID", topicID)
                    .append("date", today)
                    .append("temp", Collections.singletonList(tempDoc))
                    .append("hum", Collections.singletonList(humDoc)
                    ));
            dbOp = DBOperation.INSERT;
        }
        return dbOp;
    }

    public DBOperation insertAlertWithTimeControl(Topic t, String message, int offsetMin) {
        DBOperation dbOp = DBOperation.VOID;
        var alerts = db.getCollection("Alerts");
        var filter = new Document("topicID", new Document("$eq", t.getTopicID()));
        if (alerts.countDocuments(filter) == 0) {
            Document messArray = new Document().append("text", message).append("dateTime", LocalDateTime.now());
            Document d = new Document()
                    .append("topicID", t.getTopicID())
                    .append("message", Collections.singletonList(messArray));
            alerts.insertOne(d);
            dbOp = DBOperation.INSERT;
        } else {
            FindIterable<Document> ris = alerts.find(filter);
            for (var doc : ris) {
                List<Document> alert = (ArrayList<Document>) doc.get("message");
                Date la = (Date) alert.get(alert.size() - 1).get("dateTime");
                LocalDateTime lastAlert = LocalDateTime.ofInstant(la.toInstant(), ZoneOffset.of("Z"));
                if (lastAlert.plusMinutes(offsetMin).isBefore(LocalDateTime.now())) {
                    Bson update = push("message", new Document().append("text", message).append("dateTime", LocalDateTime.now()));
                    alerts.updateOne(eq("topicID", t.getTopicID()), update);
                    dbOp = DBOperation.UPDATE;
                }
            }
        }
        return dbOp;
    }

    public List<Topic> getTopics() {
        var topics = db.getCollection("Topics").find();
        List<Topic> topicList = new LinkedList<>();
        Map<String, Object> fv = new HashMap<>();
        topics.forEach(topicDoc -> {
            topicDoc.forEach(fv::put);
            Customer c = getCustomerById((ObjectId) fv.get("customerID"));
            topicList.add(new Topic((ObjectId) fv.get("_id"), c, (String) fv.get("name"), (int) fv.get("samplingInterval"), (Integer) fv.get("triggerCond")));
            fv.clear();
        });
        return topicList;
    }

    public Customer getCustomerById(ObjectId id) {

        Document filter = new Document("_id", new Document("$eq", id));
        var customers = db.getCollection("Customers").find(filter);
        List<Object> values = new LinkedList<>();
        customers.forEach(customerDoc -> customerDoc.forEach((s, o) -> values.add(o)));
        return new Customer((ObjectId) values.get(0), (String) values.get(1), (int) values.get(2));
    }


    public static void main(String[] args) {

        MongoConnection database = new MongoConnection("Mqttemp");
        Document d = new Document().append("nome", "maestro");
        //database.getCollection("Topics").insertOne(d);
        Document filter = new Document("val", new Document("$lte", 12));
        var r = filter.toJson();
        //var ok = database.getCollection("Topics").find(filter);

    }
}
