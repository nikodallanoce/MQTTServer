import org.bson.types.ObjectId;

import java.util.Date;

public class Record {
    private ObjectId recordID;
    private Date date;
    private float sample[];
    private Topic topic;

    public Record(ObjectId recordID, Date date, float[] sample, Topic topic) {
        this.recordID = recordID;
        this.date = date;
        this.sample = sample;
        this.topic = topic;
    }


}
