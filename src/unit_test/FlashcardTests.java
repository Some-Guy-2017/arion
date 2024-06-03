package unit_test;

import arion.*;
import java.time.*;

public class FlashcardTests {
    /*
    static BasicConstructorParams[] basicConstructorParams = new BasicConstructorParams[] {
        new BasicConstructorParams("regular case", "Front", "Back", LocalDate.now(), 5),
        new BasicConstructorParams("empty parameters", "", "", LocalDate.now(), 0),
        new BasicConstructorParams("null parameters", null, null, null, null, "NullPointerException"),
        new BasicConstructorParams("negative duration", "Front", "Back", LocalDate.now(), Duration.ofDays(-3), "IllegalArgumentException"),
    };
    
    public static void testConstructors() {
        for (BasicConstructorParams params : basicConstructorParams) {
            if (params.exception == null) new Flashcard(params.front, params.back, params.reviewDate, params.reviewInterval);
            else {
                boolean success = Test.expectUnhandledException(
                        () -> new Flashcard(params.front, params.back, params.reviewDate, params.reviewInterval),
                        params.exception);
                if (!success) System.out.println("An error occurred while executing " + params.name + ".");
            }
            Test.printSuccessMessage("basic constructor", params.name);
        }
    }

    public static void main(String[] args) {
        testConstructors();
    }
    */
}

class BasicConstructorParams {
    /*
    String name;
    String front;
    String back;
    LocalDate reviewDate;
    Duration reviewInterval;
    String exception;

    public BasicConstructorParams(String name, String front, String back, LocalDate reviewDate, Duration reviewInterval, String exception) {
        this.name = name;
        this.front = front;
        this.back = back;
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;
        this.exception = exception;
    }
    
    public BasicConstructorParams(String name, String front, String back, LocalDate reviewDate, int intervalDays, String exception) {
        this(name, front, back, reviewDate, Duration.ofDays(intervalDays), exception);
    }
    
    public BasicConstructorParams(String name, String front, String back, LocalDate reviewDate, Duration reviewInterval) {
        this(name, front, back, reviewDate, reviewInterval, null);
    }
    
    public BasicConstructorParams(String name, String front, String back, LocalDate reviewDate, int intervalDays) {
        this(name, front, back, reviewDate, Duration.ofDays(intervalDays), null);
    }
}

class StringParams {
    String front;
    String back;
    String reviewInterval;
    String reviewDate;
    String exception;
    */
}
