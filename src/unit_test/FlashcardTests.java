package unit_test;

import arion.*;
import java.time.*;
import java.util.Optional;

import exception.*;

public class FlashcardTests {
    static Test[] tests;
    static {
        tests = new Test[] {
                new FlashcardConstructorTest("LocalDate Regular Case", LocalDate.now(), 5, "Front", "Back")
                        .localDate(LocalDate.now(), 5),
                new FlashcardConstructorTest("Empty Parameters", LocalDate.now(), 1, "", "")
                        .localDate(LocalDate.now(), 1),
                new FlashcardConstructorTest("Null Parameters", null, 0, null, null)
                        .localDate(null, 0)
                        .withException("java.lang.NullPointerException"),
                new FlashcardConstructorTest("Zero Duration", LocalDate.now(), -1, "Front", "Back")
                        .localDate(LocalDate.now(), 0)
                        .withException("java.lang.IllegalArgumentException"),
                new FlashcardConstructorTest("Negative Duration", LocalDate.now(), -2, "Front", "Back")
                        .localDate(LocalDate.now(), -2)
                        .withException("java.lang.IllegalArgumentException"),
                new FlashcardConstructorTest("Date Before Today", LocalDate.now().minusDays(2), 1, "Front", "Back")
                        .localDate(LocalDate.now().minusDays(2), 1),

                new FlashcardConstructorTest("Epoch Day Regular Case", LocalDate.ofEpochDay(0), 1, "Front", "Back")
                        .epochDay(0, 1),
                new FlashcardConstructorTest("Negative Epoch Day", LocalDate.ofEpochDay(-120), 1, "Front", "Back")
                        .epochDay(-120, 1),
                new FlashcardConstructorTest("Epoch Day Null Parameters", LocalDate.ofEpochDay(0), 1, null, null)
                        .epochDay(0, 1)
                        .withException("java.lang.NullPointerException"),

                new FlashcardConstructorTest("String String Regular Case", LocalDate.ofEpochDay(100), 1, "Front",
                        "Back")
                        .stringString("April 11, 1970", "1 day"),
                new FlashcardConstructorTest("Invalid Date", null, 1, "Front", "Back")
                        .stringString("February 29, 1971", "1 day")
                        .withException("exception.DateFormatException"),
                new FlashcardConstructorTest("Invalid Interval", null, 1, "Front", "Back")
                        .stringString("February 28, 1971", "Interval")
                        .withException("exception.IntervalFormatException"),
                new FlashcardConstructorTest("Leap Year", LocalDate.of(1972, 2, 29), 1, "Front", "Back")
                        .stringString("February 29, 1972", "1 day"),
                new FlashcardConstructorTest("String String Null Parameters", null, 0, "Front", "Back")
                        .stringString(null, null)
                        .withException("java.lang.NullPointerException"),
                new FlashcardConstructorTest("String String Null Date", null, 1, "Front", "Back")
                        .stringString(null, "1 day")
                        .withException("java.lang.NullPointerException"),
                new FlashcardConstructorTest("String String Null Interval", LocalDate.ofEpochDay(20000), 0, "Front",
                        "Back")
                        .stringString("October 4, 2024", null)
                        .withException("java.lang.NullPointerException"),

                new FlashcardConstructorTest("Default Fields Regular Case", LocalDate.now(), 1, "Front", "Back")
                        .defaultFields(),
                new FlashcardConstructorTest("Default Fields Null Fields", LocalDate.now(), 1, null, null)
                        .defaultFields()
                        .withException("java.lang.NullPointerException"),

                new ToStringArrayTest("Regular Case", "Front", "Back", LocalDate.ofEpochDay(20000), 10,
                        "October 4, 2024",
                        "10 days"),
                new FromStringArrayTest("Regular Case", new String[] {
                        "Front",
                        "Back",
                        "October 4, 2024",
                        "10 days",
                }, "Front", "Back", LocalDate.ofEpochDay(20000), 10),
                new FromStringArrayTest("Invalid Date", new String[] {
                        "Front",
                        "Back",
                        "Invalid Date",
                        "10 days",
                }, "Front", "Back", LocalDate.now(), 10)
                        .withException("exception.DateFormatException"),
                new FromStringArrayTest("Invalid Interval", new String[] {
                        "Front",
                        "Back",
                        "October 4, 2024",
                        "Interval",
                }, "Front", "Back", LocalDate.of(2024, 10, 4), 1)
                        .withException("exception.IntervalFormatException"),
                new FromStringArrayTest("Null Array", null, null, null, null, 0)
                        .withException("java.lang.NullPointerException"),
                new FromStringArrayTest("Null Values in Array", new String[] { null, null, null, null }, null, null,
                        null, 0)
                        .withException("java.lang.NullPointerException"),

                new UpdateReviewDateTest("Regular Case true", true, 5, LocalDate.now().plusDays(5), 9),
                new UpdateReviewDateTest("Regular Case false", false, 5, LocalDate.now(), 1),

                new DueTest("Is Due", LocalDate.now().minusDays(10), true),
                new DueTest("Not Due", LocalDate.now().plusDays(10), false),
                new DueTest("Due Today", LocalDate.now(), true),

                new ToStringTest("Regular Case", "Front", "Back", LocalDate.of(2024, 2, 2), 3,
                        "Flashcard(front: \"Front\", back: \"Back\", reviewDate: \"February 2, 2024\", reviewInterval: \"3 days\")"),
                new ToStringTest("Single Day", "Front", "Back", LocalDate.of(2024, 2, 2), 1,
                        "Flashcard(front: \"Front\", back: \"Back\", reviewDate: \"February 2, 2024\", reviewInterval: \"1 day\")"),

                new EqualsTest("Equal", new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Front", "Back", LocalDate.now(), 3), true),
                new EqualsTest("Different Front", new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("front", "Back", LocalDate.now(), 3), false),
                new EqualsTest("Different Back", new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Front", "back", LocalDate.now(), 3), false),
                new EqualsTest("Different Review Date", new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Front", "Back", LocalDate.now().minusDays(3), 3), false),
                new EqualsTest("Different Review Interval", new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Front", "Back", LocalDate.now(), 1), false),

                new CompareTest("Front Correct Order",
                        new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Great", "Back", LocalDate.now(), 3), Flashcard.Field.FRONT, false, true),
                new CompareTest("Front Correct Order Reversed",
                        new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Great", "Back", LocalDate.now(), 3), Flashcard.Field.FRONT, true, false),
                new CompareTest("Front Incorrect Order",
                        new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Bingo", "Back", LocalDate.now(), 3), Flashcard.Field.FRONT, false, false),
                new CompareTest("Front Same Cards",
                        new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Front", "Back", LocalDate.now(), 3), Flashcard.Field.FRONT, false, true),
                new CompareTest("Back Correct Order",
                        new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Front", "Bite", LocalDate.now(), 3), Flashcard.Field.BACK, false, true),
                new CompareTest("Back Incorrect Order",
                        new Flashcard("Front", "Back", LocalDate.now(), 3),
                        new Flashcard("Front", "Andy", LocalDate.now(), 3), Flashcard.Field.BACK, false, false),
                new CompareTest("Review Date Correct Order",
                        new Flashcard("Front", "Back", LocalDate.now().minusDays(1), 3),
                        new Flashcard("Front", "Andy", LocalDate.now().plusDays(1), 3), Flashcard.Field.REVIEW_DATE,
                        false, true),
                new CompareTest("Review Date Incorrect Order",
                        new Flashcard("Front", "Back", LocalDate.now().plusDays(1), 3),
                        new Flashcard("Front", "Back", LocalDate.now().minusDays(1), 3), Flashcard.Field.REVIEW_DATE,
                        false, false),
                new CompareTest("Review Interval Correct Order",
                        new Flashcard("Front", "Back", LocalDate.now(), 1),
                        new Flashcard("Front", "Back", LocalDate.now(), 2), Flashcard.Field.REVIEW_INTERVAL, false,
                        true),
                new CompareTest("Review Interval Incorrect Order",
                        new Flashcard("Front", "Back", LocalDate.now(), 2),
                        new Flashcard("Front", "Back", LocalDate.now(), 1), Flashcard.Field.REVIEW_INTERVAL, false,
                        false),
        };
    }

    public static void main(String[] args) {
        Test.evaluateTests(tests);
    }
}

class FlashcardConstructorTest extends Test {
    String front;
    String back;
    Object reviewDate;
    Object reviewInterval;

    LocalDate expectedDate;
    long expectedInterval;

    Optional<ConstructorType> constructorTypeOption = Optional.empty();

    enum ConstructorType {
        LOCAL_DATE,
        EPOCH_DAY,
        STRING_STRING,
        DEFAULT_FIELDS,
    }

    public FlashcardConstructorTest(String testName, LocalDate expectedDate, long expectedInteval, String front,
            String back) {
        super("Flashcard Constructor Test", testName);
        this.expectedDate = expectedDate;
        this.expectedInterval = expectedInteval;
        this.front = front;
        this.back = back;
    }

    public FlashcardConstructorTest localDate(LocalDate reviewDate, long reviewInterval) {
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;

        constructorTypeOption = Optional.of(ConstructorType.LOCAL_DATE);
        return this;
    }

    public FlashcardConstructorTest epochDay(long epochDay, long reviewInterval) {
        this.reviewDate = epochDay;
        this.reviewInterval = reviewInterval;
        constructorTypeOption = Optional.of(ConstructorType.EPOCH_DAY);
        return this;
    }

    public FlashcardConstructorTest stringString(String reviewDate, String reviewInterval) {
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;
        constructorTypeOption = Optional.of(ConstructorType.STRING_STRING);
        return this;
    }

    public FlashcardConstructorTest defaultFields() {
        constructorTypeOption = Optional.of(ConstructorType.DEFAULT_FIELDS);
        return this;
    }

    void execute() throws exception.DateFormatException, IntervalFormatException {
        if (constructorTypeOption.isEmpty()) {
            System.out.println("Cannot execute " + qualifiedName + " because constructor was not selected.");
            return;
        }

        ConstructorType constructorType = constructorTypeOption.get();
        Flashcard flashcard;

        switch (constructorType) {
            case LOCAL_DATE:
                flashcard = new Flashcard(front, back, (LocalDate) reviewDate, (long) reviewInterval);
                break;
            case EPOCH_DAY:
                flashcard = new Flashcard(front, back, (long) reviewDate, (long) reviewInterval);
                break;
            case STRING_STRING:
                flashcard = new Flashcard(front, back, (String) reviewDate, (String) reviewInterval);
                break;
            case DEFAULT_FIELDS:
                flashcard = new Flashcard(front, back);
                break;
            default:
                System.out.println("Executing unimplemented constructor type: " + constructorType + ".");
                return;
        }

        assert flashcard.reviewDate.equals(expectedDate)
                : "Expected date " + expectedDate + " but instead got " + flashcard.reviewDate;
        assert flashcard.reviewInterval == expectedInterval
                : "Expected interval " + expectedInterval + " but instead got " + flashcard.reviewInterval;
    }
}

class ToStringArrayTest extends Test {
    String front;
    String back;
    LocalDate reviewDate;
    long reviewInterval;

    String expectedDate;
    String expectedInterval;

    public ToStringArrayTest(String testName, String front, String back, LocalDate reviewDate, long reviewInterval,
            String expectedDate, String expectedInterval) {
        super("Flashcard To String Array Test", testName);
        this.front = front;
        this.back = back;
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;
        this.expectedDate = expectedDate;
        this.expectedInterval = expectedInterval;
    }

    void execute() {
        Flashcard flashcard = new Flashcard(front, back, reviewDate, reviewInterval);
        String[] arr = flashcard.toStringArray();

        assert arr[0].equals(front) : "Expected front " + front + ", but got " + arr[0] + ".";
        assert arr[1].equals(back) : "Expected back " + back + ", but got " + arr[1] + ".";
        assert arr[2].equals(expectedDate) : "Expected date " + expectedDate + ", but got " + arr[2] + ".";
        assert arr[3].equals(expectedInterval) : "Expected interval " + expectedInterval + ", but got " + arr[3] + ".";
    }
}

class FromStringArrayTest extends Test {
    String[] arr;
    String expectedFront;
    String expectedBack;
    LocalDate expectedDate;
    long expectedInterval;

    public FromStringArrayTest(String testName, String[] arr, String expectedFront, String expectedBack,
            LocalDate expectedDate, long expectedInterval) {
        super("Flashcard From String Array Test", testName);
        this.arr = arr;
        this.expectedFront = expectedFront;
        this.expectedBack = expectedBack;
        this.expectedDate = expectedDate;
        this.expectedInterval = expectedInterval;
    }

    void execute() throws DateFormatException, IntervalFormatException {
        Flashcard flashcard = Flashcard.fromStringArray(arr);
        assert expectedFront.equals(flashcard.front)
                : "Expected " + expectedFront + ", but received " + flashcard.front + ".";
        assert expectedBack.equals(flashcard.back)
                : "Expected " + expectedBack + ", but received " + flashcard.back + ".";
        assert expectedDate.equals(flashcard.reviewDate)
                : "Expected " + expectedDate + ", but received " + flashcard.reviewDate + ".";
        assert expectedInterval == flashcard.reviewInterval
                : "Expected " + expectedInterval + ", but received " + flashcard.reviewInterval + ".";
    }
}

class UpdateReviewDateTest extends Test {
    boolean success;
    long initialInterval;

    LocalDate expectedDate;
    long expectedInterval;

    public UpdateReviewDateTest(String testName, boolean success, long initialInterval, LocalDate expectedDate,
            long expectedInterval) {
        super("Flashcard Update Review Date Test", testName);
        this.success = success;
        this.initialInterval = initialInterval;
        this.expectedDate = expectedDate;
        this.expectedInterval = expectedInterval;
    }

    void execute() {
        Flashcard flashcard = new Flashcard("Front", "Back", LocalDate.now(), initialInterval);
        flashcard.updateReview(success);

        assert expectedDate.equals(flashcard.reviewDate)
                : "Expected " + expectedDate + " but received " + flashcard.reviewDate + ".";
        assert expectedInterval == flashcard.reviewInterval
                : "Expected " + expectedInterval + " but received " + flashcard.reviewInterval + ".";

    }
}

class DueTest extends Test {
    LocalDate date;
    boolean expectedReturn;

    public DueTest(String testName, LocalDate date, boolean expectedReturn) {
        super("Flashcard Due Test", testName);
        this.date = date;
        this.expectedReturn = expectedReturn;
    }

    void execute() {
        Flashcard flashcard = new Flashcard("Front", "Back", date, 1);
        boolean isDue = flashcard.isDue();
        assert expectedReturn == isDue : "Expected " + expectedReturn + ", but received " + isDue + ".";
    }
}

class ToStringTest extends Test {
    String front;
    String back;
    LocalDate reviewDate;
    long reviewInterval;
    String expectedString;

    public ToStringTest(String testName, String front, String back, LocalDate reviewDate, long reviewInterval,
            String expectedString) {
        super("Flashcard To String Test", testName);
        this.front = front;
        this.back = back;
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;
        this.expectedString = expectedString;
    }

    void execute() {
        Flashcard flashcard = new Flashcard(front, back, reviewDate, reviewInterval);
        String str = flashcard.toString();
        assert expectedString.equals(str) : "Expected " + expectedString + ", but received " + str + ".";
    }
}

class EqualsTest extends Test {
    Flashcard card1;
    Flashcard card2;
    boolean expectedReturn;

    public EqualsTest(String testName, Flashcard card1, Flashcard card2, boolean expectedReturn) {
        super("Flashcard Equals Test", testName);
        this.card1 = card1;
        this.card2 = card2;
        this.expectedReturn = expectedReturn;
    }

    void execute() {
        boolean same = card1.equals(card2);
        assert expectedReturn == same : "Expected " + expectedReturn + ", but received " + same + ".";
    }
}

class CompareTest extends Test {
    Flashcard card1;
    Flashcard card2;
    Flashcard.Field field;
    boolean reversed;

    boolean expectedReturn;

    public CompareTest(String testName, Flashcard card1, Flashcard card2, Flashcard.Field field, boolean reversed,
            boolean expectedReturn) {
        super("Flashcard Comparison Test", testName);
        this.card1 = card1;
        this.card2 = card2;
        this.field = field;
        this.reversed = reversed;
        this.expectedReturn = expectedReturn;
    }

    void execute() {
        boolean comparison = card1.compareTo(card2, field, reversed);
        assert expectedReturn == comparison : "Expected " + expectedReturn + ", but received " + comparison + ".";
    }
}
