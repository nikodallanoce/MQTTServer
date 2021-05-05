import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.push;

// ...
public class MongoConnectionV2 {
    private final ConnectionString connString = new ConnectionString(
            "mongodb+srv://nikodallanoce:pieroangela@clustertest.zbdu9.mongodb.net/w=majority"
    );
    MongoDatabase db;

    public MongoDatabase getDb() {
        return db;
    }

    public MongoConnectionV2(String database) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        this.db = mongoClient.getDatabase(database);
    }

    public void insertRecordWithControls(ObjectId topicID, List<Number> val) {
        var today = LocalDate.now().plusDays(1);
        var now = LocalTime.now().toEpochSecond(today, ZoneOffset.of("Z"));

        Document tempDoc = new Document().append("val", val.get(0)).append("time", new BsonTimestamp(now));
        Document humDoc = new Document().append("val", val.get(1)).append("time", new BsonTimestamp(now));

        if (db.getCollection("Records").countDocuments(and(eq("topicID", topicID), eq("date", today))) > 0) {
            Bson update = combine(push("temp", tempDoc), push("hum", humDoc));

            db.getCollection("Records")
                    .updateOne(and(eq("topicID", topicID), eq("date", today)), update);

        } else {
            db.getCollection("Records").insertOne(new Document()
                    .append("topicID", topicID)
                    .append("date", today)
                    .append("temp", Collections.singletonList(tempDoc))
                    .append("hum", Collections.singletonList(humDoc)
                    ));
        }

        //retrived.forEach(System.out::println);

    }

    /*public List<Topic> getTopics() {
        var topics = db.getCollection("Topics").find();
        List<Object> values = new LinkedList<>();
        List<Topic> topicList = new LinkedList<>();
        topics.forEach(topicDoc -> {
            topicDoc.forEach((f, v) -> {
                values.add(v);
            });
            Customer c = getCustomerById((ObjectId) values.get(1));
            topicList.add(new Topic((ObjectId) values.get(0), c, (String) values.get(3), (int) values.remove(4), null));
            values.clear();
        });
        return topicList;
    }*/

    public List<Topic> getTopics() {
        var topics = db.getCollection("Topics").find();
        List<Topic> topicList = new LinkedList<>();
        Map<String, Object> fv = new HashMap<>();
        topics.forEach(topicDoc -> {
            topicDoc.forEach(fv::put);
            Customer c = getCustomerById((ObjectId) fv.get("customerID"));
            //topicList.add(new Topic((ObjectId) fv.get("_id"), c, (String) fv.get("name"), (int) fv.get("samplingInterval")));
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

        MongoConnectionV2 database = new MongoConnectionV2("Mqttemp");
        Document d = new Document().append("nome", "maestro");
        //database.getCollection("Topics").insertOne(d);
        Document filter = new Document("val", new Document("$lte", 12));
        var r = filter.toJson();
        //var ok = database.getCollection("Topics").find(filter);

    }
}
