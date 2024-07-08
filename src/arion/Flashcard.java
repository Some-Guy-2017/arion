package arion;

import exception.*;

import java.time.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.time.temporal.ChronoUnit;
import java.time.format.*;
import java.time.temporal.TemporalAccessor;

/*
 * The Flashcard class represents one flashcard. It contains the flashcard's front, back, review date, and review interval.
 * It contains Flashcard specific methods, such as checking whether it is due, and updating the review interval after studying.
 */

public class Flashcard {

    /*
     * The Field enum represents one of the four fields of a flashcard.
     */
    public enum Field {
        FRONT("Front"),
        BACK("Back"),
        REVIEW_DATE("Review Date"),
        REVIEW_INTERVAL("Review Interval");

        String title;

        /*
         * The constructor assigns its title to the passed title.
         *
         * Input: title of the field.
         * Output: new Field enum.
         */
        Field(String title) {
            this.title = title;
        }

        /*
         * toString converts this field into its string representation.
         * 
         * Input: no input.
         * Output: field as a String.
         */
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

    /*
     * The constructor sets the internal fields of Flashcard to the values passed in
     * the constructor.
     * This signature accepts the review date as a LocalDate, and the review
     * interval as
     * a number of days, as a long.
     *
     * Input: front, back, review date, and review interval of the flashcard.
     * Output: new Flashcard class.
     */
    public Flashcard(String front, String back, LocalDate reviewDate, long reviewInterval) {
        if (front == null || back == null || reviewDate == null) {
            throw new NullPointerException("Attempted to construct flashcard with null fields.");
        }

        this.front = front;
        this.back = back;
        this.reviewDate = reviewDate;
        this.reviewInterval = reviewInterval;
        if (reviewInterval <= 0) {
            throw new IllegalArgumentException("Attempted to construct flashcard with invalid review interval.");
        }
    }

    /*
     * This constructor signature represents the review date as an epoch date,
     * and the review interval as a long representing the number of days until
     * review.
     *
     * Input: flashcard front, back, epoch day, and review interval days.
     * Output: new Flashcard class.
     */
    public Flashcard(String front, String back, long epochDay, long days) {
        this(front, back, LocalDate.ofEpochDay(epochDay), days);
    }

    /*
     * This constructor signature does not accept a review date or interval;
     * instead, it sets them to the default values of today and one day.
     * 
     * Input: flashcard front and back.
     * Output: new Flashcard class.
     */
    public Flashcard(String front, String back) {
        this(front, back, LocalDate.now(), 1);
    }

    /*
     * This constructor signature represents the review date and interval as
     * Strings.
     * This is primarily useful when flashcards from user input.
     * It may throw a DateFormatException or an IntervalFormatException, since the
     * String representation
     * of the date and review interval can be incorrect.
     */
    public Flashcard(String front, String back, String reviewDate, String reviewInterval)
            throws DateFormatException, IntervalFormatException {
        this(front, back, parseDateString(reviewDate), parseIntervalString(reviewInterval));
    }

    /*
     * toStringArray converts the flashcard into a String array representation,
     * where each field
     * is converted to a String then inserted into the array.
     * 
     * Input: no input.
     * Output: String array representing this Flashcard.
     */
    public String[] toStringArray() {
        return new String[] {
                front,
                back,
                dateFormatter.format(reviewDate),
                formatInterval(reviewInterval),
        };
    }

    /*
     * fromStringArray converts a String array representing a Flashcard into a
     * Flashcard.
     * The array contains either a front and a back, or all four fields represented as Strings.
     *
     * Input: String array to convert.
     * Output: flashcard.
     */
    public static Flashcard fromStringArray(String[] array) throws IntervalFormatException, DateFormatException {
        if (array == null) {
            throw new NullPointerException("Received null array");
        }

        if (array.length == 2) {
            return new Flashcard(array[0], array[1]);
        } else if (array.length == 4) {
            return new Flashcard(array[0], array[1], array[2], array[3]);
        }
        
        throw new IllegalArgumentException("Array has to be of length two or four.");
    }

    /*
     * updateReviewDate updates the review date and interval of this flashcard
     * according to whether the user failed or succeeded in recalling the back
     * information.
     * If successful, the review interval gets 1.6 times longer, and the review date
     * is set to
     * the current date plus the interval.
     * Otherwise, the interval is reset to one day and the review date is today.
     *
     * Input: whether the user successfully reviewed the flashcard.
     * Output: no return value, modifies the review date and interval.
     */
    public void updateReview(boolean success) {
        if (success) {
            reviewDate = LocalDate.now().plusDays(reviewInterval);
            reviewInterval = (long) (reviewInterval * 1.6) + 1;
        } else {
            reviewDate = LocalDate.now();
            reviewInterval = 1;
        }
    }

    /*
     * isDue checks whether this flashcard is due.
     *
     * Input: no input.
     * Output: boolean representing whether this flashcard is due.
     */
    public boolean isDue() {
        return !reviewDate.isAfter(LocalDate.now());
    }

    /*
     * toString represents this flashcard as a String.
     *
     * Input: no input.
     * Output: String representing this flashcard.
     */
    @Override
    public String toString() {
        String[] stringFields = toStringArray();
        return "Flashcard(front: \"" + stringFields[0] + "\""
                + ", back: \"" + stringFields[1] + "\""
                + ", reviewDate: \"" + stringFields[2] + "\""
                + ", reviewInterval: \"" + stringFields[3] + "\")";
    }

    /*
     * equals checks whether the input object is equal to this flashcard.
     *
     * Input: other object to compare against.
     * Output: boolean representing whether the other object equals this flashcard.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Flashcard)) {
            return false;
        }

        Flashcard otherFlashcard = (Flashcard) other;
        return front.equals(otherFlashcard.front)
                && back.equals(otherFlashcard.back)
                && reviewDate.equals(otherFlashcard.reviewDate)
                && reviewInterval == otherFlashcard.reviewInterval;
    }

    /*
     * compareTo compares this flashcard to another one. The comparison is true if
     * this flashcard
     * is in the correct order compared to the other, given the desired field and
     * whether the comparison
     * is reversed.
     *
     * Input: other flashcard to compare to, field to compare by, and whether to
     * reverse the comparison.
     * Output: whether this flashcard is correctly sorted compared to the other
     * flashcard.
     */
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

    /*
     * formatInterval formats the interval, in number of days, as a String.
     *
     * Input: review interval, a long that represents number of days.
     * Output: String representing the interval.
     */
    private static String formatInterval(long interval) {
        StringBuilder builder = new StringBuilder(interval + " ");

        if (interval == 1) {
            builder.append("day");
        } else {
            builder.append("days");
        }

        return builder.toString();
    }

    /*
     * parseDateString parses the given String as date.
     *
     * Input: String representation of a date.
     * Output: LocalDate converted from the String.
     */
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

    /*
     * parseIntervalString parses the given String as a review interval.
     *
     * Input: String to parse.
     * Output: review interval in days.
     */
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
        if (dayCount <= 0) {
            throw new IntervalFormatException("Cannot parse interval with day count less than 0.");
        }

        return dayCount;
    }

    /*
     * generateFieldTitles statically generates a list of field titles from the FIELDS array.
     *
     * Input: no input.
     * Output: String array of titles of all flashcard fields.
     */
    private static String[] generateFieldTitles() {
        String[] titles = new String[FIELD_COUNT];
        for (int i = 0; i < FIELDS.length; i++) {
            titles[i] = FIELDS[i].title;
        }
        return titles;
    }
}
