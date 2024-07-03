package arion;

import exception.*;

import java.time.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.time.temporal.ChronoUnit;
import java.time.format.*;
import java.time.temporal.TemporalAccessor;

public class Flashcard {
    public enum Field {
        FRONT("Front"),
        BACK("Back"),
        REVIEW_DATE("Review Date"),
        REVIEW_INTERVAL("Review Interval");

        String title;

        Field(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public final static int FIELD_COUNT = 4;
    public final static Field[] FIELDS = { Field.FRONT, Field.BACK, Field.REVIEW_DATE, Field.REVIEW_INTERVAL };
    public static String[] FIELD_TITLES = generateFieldTitles();

    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public String front;
    public String back;
    public LocalDate reviewDate;
    public long reviewInterval;

    private static final double intervalMultiplicand = 1.6;

    public Flashcard(String front, String back, LocalDate reviewDate, long reviewInterval) {
        if (front == null || back == null || reviewDate == null)
            throw new NullPointerException("Attempted to construct flashcard with null fields.");

        this.front = front;
        this.back = back;
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;
        if (reviewInterval <= 0)
            throw new IllegalArgumentException("Attempted to construct flashcard with invalid review interval.");
    }

    public Flashcard(String front, String back, long epochDay, long days) {
        this(front, back, LocalDate.ofEpochDay(epochDay), days);
    }

    public Flashcard(String front, String back) {
        this(front, back, LocalDate.now(), 1);
    }

    public Flashcard(String front, String back, String reviewDate, String reviewInterval)
            throws DateFormatException, IntervalFormatException {
        this(front, back, parseDateString(reviewDate), parseIntervalString(reviewInterval));
    }

    public String[] toStringArray() {
        return new String[] {
                front,
                back,
                dateFormatter.format(reviewDate),
                formatInterval(reviewInterval),
        };
    }

    public static Flashcard fromStringArray(String[] arr) throws IntervalFormatException, DateFormatException {
        if (arr == null)
            throw new NullPointerException("Received null array");

        if (arr.length != 4)
            throw new IllegalArgumentException("Array has to be of length four.");

        return new Flashcard(arr[0], arr[1], arr[2], arr[3]);

    }

    public void updateReviewDate(boolean success) {
        if (success) {
            reviewDate = LocalDate.now().plusDays(reviewInterval);
            reviewInterval = (long) (reviewInterval * 1.6) + 1;
        } else {
            reviewDate = LocalDate.now();
            reviewInterval = 1;
        }
    }

    public boolean isDue() {
        return !reviewDate.isAfter(LocalDate.now());
    }

    @Override
    public String toString() {
        String[] stringFields = toStringArray();
        return "Flashcard(front: \"" + stringFields[0] + "\""
                + ", back: \"" + stringFields[1] + "\""
                + ", reviewDate: \"" + stringFields[2] + "\""
                + ", reviewInterval: \"" + stringFields[3] + "\")";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Flashcard))
            return false;

        Flashcard otherFlashcard = (Flashcard) other;
        return front.equals(otherFlashcard.front)
                && back.equals(otherFlashcard.back)
                && reviewDate.equals(otherFlashcard.reviewDate)
                && reviewInterval == otherFlashcard.reviewInterval;
    }

    public Flashcard clone() {
        return new Flashcard(
                front,
                back,
                reviewDate,
                reviewInterval);
    }

    public boolean compareTo(Flashcard other, Field field, boolean reversed) {
        boolean comparison;
        switch (field) {
            case FRONT:
                comparison = front.compareToIgnoreCase(other.front) <= 0;
                break;
            case BACK:
                comparison = back.compareToIgnoreCase(other.back) <= 0;
                break;
            case REVIEW_DATE:
                comparison = !reviewDate.isAfter(other.reviewDate);
                break;
            case REVIEW_INTERVAL:
                comparison = reviewInterval <= other.reviewInterval;
                break;
            default:
                comparison = true;
                break;
        }
        if (reversed)
            return !comparison;
        else
            return comparison;
    }

    private void setFieldValues(String front, String back, LocalDate reviewDate, long reviewInterval) {
    }

    private static String formatInterval(long interval) {
        StringBuilder builder = new StringBuilder(interval + " ");

        if (interval == 1)
            builder.append("day");
        else
            builder.append("days");

        return builder.toString();
    }

    private static LocalDate parseDateString(String str) throws DateFormatException {
        if (str == null) {
            throw new NullPointerException("Cannot parse date because it is null.");
        }
        try {
            return LocalDate.from(dateFormatter.parse(str));
        } catch (DateTimeParseException e) {
            throw new DateFormatException("Cannot construct flashcard because date is improperly formatted.");
        }
    }

    private static long parseIntervalString(String str) throws IntervalFormatException {
        if (str == null) {
            throw new NullPointerException("Cannot parse interval because string is null.");
        }

        str = str.toLowerCase().replaceAll(" days?", "").toLowerCase(); // remove "day" or "days"
        long dayCount = -1;

        try {
            dayCount = Long.valueOf(str).longValue();
        } catch (NumberFormatException e) {
            throw new IntervalFormatException("Cannot parse interval because it is not a valid number.");
        }
        if (dayCount <= 0)
            throw new IntervalFormatException("Cannot parse interval with day count less than 0.");

        return dayCount;
    }

    private static String[] generateFieldTitles() {
        String[] titles = new String[FIELD_COUNT];
        for (int i = 0; i < FIELDS.length; i++)
            titles[i] = FIELDS[i].title;
        return titles;
    }
}
