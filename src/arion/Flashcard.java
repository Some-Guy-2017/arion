package arion;
import java.time.*;

public class Flashcard
{
    public enum Field
    {
        FRONT,
        BACK,
        REVIEW_DATE,
        REVIEW_INTERVAL,
    }
    public final static int FIELD_COUNT = 4;
    public final static String[] fieldNames = {"Front", "Back", "Review Date", "Review Interval"};
    
    String front;
    String back;
    Instant reviewDate;
    Duration reviewInterval;

    public Flashcard(String front, String back, Instant reviewDate, Duration reviewInterval)
    {
        this.front = front;
        this.back = back;
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;
    }
    
    public Flashcard(String front, String back)
    {
        setDefaultFieldValues();
        this.front = front;
        this.back = back;
    }


    public String[] toStringArray()
    {
        return new String[] {
            front,
            back,
            reviewDate.toString(),
            reviewInterval.toString(),
        };
    }

    public static Flashcard fromStringArray(String[] arr)
    {
        if (arr.length != 4) return null;
            
        String front = arr[0];
        String back = arr[1];
        Instant reviewDate = Instant.parse(arr[2]);
        Duration reviewInterval = Duration.parse(arr[3]);

        return new Flashcard(front, back, reviewDate, reviewInterval);
    }
    
    private void setDefaultFieldValues()
    {
        front = "";
        back = "";
        reviewDate = Instant.now();
        reviewInterval = Duration.ofDays(1);
    }
}
