package unit_test;

import arion.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import exception.*;

public class ArionTests {
    static Test[] tests = generateTests();

    static Test[] generateTests() {

        String[] defaultAddFields = new String[] { "Front", "Back" };
        Flashcard defaultAddFlashcard = new Flashcard(defaultAddFields[0], defaultAddFields[1]);

        Flashcard[] emptyArray = new Flashcard[0];

        String[] defaultEditFields = new String[] { "Front", "Back", "March 28, 2021", "5 days" };
        Flashcard defaultEditFlashcard;
        try {
            defaultEditFlashcard = Flashcard.fromStringArray(defaultEditFields);
        } catch (DateFormatException | IntervalFormatException e) {
            e.printStackTrace();
            return new Test[] {};
        }

        Flashcard[] initialArray = new Flashcard[5];
        for (int i = 0; i < 5; i++) {
            Optional<Flashcard> flashcardOption = ArionUtils.generateRandomFlashcard();
            if (flashcardOption.isPresent()) {
                initialArray[i] = flashcardOption.get();
            } else {
                String strIdx = String.valueOf(i);
                initialArray[i] = new Flashcard("Front" + strIdx, "Back" + strIdx);
            }
        }

        int[] defaultIndices = { 1, 2, 3 };
        Flashcard[] expectedDeleteArray = new Flashcard[] { initialArray[0], initialArray[4] };

        int defaultIndex = 1;
        Flashcard[] expectedEditArray = ArionUtils.cloneArray(initialArray);
        expectedEditArray[defaultIndex] = defaultEditFlashcard;

        Flashcard[] expectedAddArray = new Flashcard[initialArray.length + 1];
        for (int i = 0; i < initialArray.length; i++) {
            expectedAddArray[i] = initialArray[i];
        }
        expectedAddArray[expectedAddArray.length - 1] = defaultAddFlashcard;

        Flashcard[] sortArray = {
                new Flashcard("Alice", "Part C", LocalDate.now().minusDays(3), 20),
                new Flashcard("Charlie", "Part A", LocalDate.now().minusDays(2), 2),
                new Flashcard("Bob", "Part D", LocalDate.now().minusDays(1), 38),
                new Flashcard("David", "Part E", LocalDate.now(), 10),
        };
        Flashcard[] frontSorted = { sortArray[0], sortArray[2], sortArray[1], sortArray[3] };
        Flashcard[] backSorted = { sortArray[1], sortArray[0], sortArray[2], sortArray[3] };
        Flashcard[] dateSorted = { sortArray[0], sortArray[1], sortArray[2], sortArray[3] };
        Flashcard[] intervalSorted = { sortArray[1], sortArray[3], sortArray[0], sortArray[2] };

        Test[] tests = {
                new ArionConstructorTest("Headless", true),
                new ArionConstructorTest("Head", false),

                new DeleteTest("Regular Case", defaultIndices, initialArray, expectedDeleteArray),
                new DeleteTest("No Indices with Flashcards", new int[] {}, initialArray, initialArray),
                new DeleteTest("No Indices without Flashcards", new int[] {}, emptyArray, emptyArray),
                new DeleteTest("Out of Order Indices", new int[] { 2, 3, 1 }, initialArray, initialArray)
                        .withException("java.lang.IllegalArgumentException"),
                new DeleteTest("Out of Bounds Indices", new int[] { 0, 99 }, initialArray, initialArray)
                        .withException("java.lang.IllegalArgumentException"),
                new DeleteTest("Negative Index", new int[] { -1 }, initialArray, initialArray)
                        .withException("java.lang.IllegalArgumentException"),
                new DeleteTest("Null Indices", null, initialArray, initialArray)
                        .withException("java.lang.NullPointerException"),

                new EditTest("Regular Case", initialArray, expectedEditArray, defaultIndex, defaultEditFields),
                new EditTest("Illegal Date", initialArray, initialArray, defaultIndex,
                        new String[] { "Front", "Back", "February 29, 2019", "11 days" }),
                new EditTest("Illegal Interval", initialArray, initialArray, defaultIndex,
                        new String[] { "Front", "Back", "February 28, 2019", "11days" }),
                new EditTest("Null Array", initialArray, null, defaultIndex, null)
                        .withException("java.lang.NullPointerException"),
                new EditTest("Null Fields", initialArray, null, defaultIndex, new String[] { null, null, null, null })
                        .withException("java.lang.NullPointerException"),
                new EditTest("Extra Field", initialArray, null, defaultIndex,
                        new String[] { "Front", "Back", "February 28, 2019", "11 days", "Extra Field" })
                        .withException("java.lang.IllegalArgumentException"),
                new EditTest("Missing Field", initialArray, null, defaultIndex,
                        new String[] { "Front", "Back", "February 28, 2019" })
                        .withException("java.lang.IllegalArgumentException"),
                new EditTest("Out of Bounds Index", initialArray, null, 99, defaultEditFields)
                        .withException("java.lang.IllegalArgumentException"),
                new EditTest("Negative Index", initialArray, null, -1, defaultEditFields)
                        .withException("java.lang.IllegalArgumentException"),

                new AddTest("Regular Case", defaultAddFields, initialArray, expectedAddArray),
                new AddTest("Initially Empty List", defaultAddFields, emptyArray,
                        new Flashcard[] { defaultAddFlashcard }),
                new AddTest("Null Array", null, initialArray, null)
                        .withException("java.lang.NullPointerException"),
                new AddTest("Extra Field", new String[] { "Front", "Back", "Extra" }, initialArray, null)
                        .withException("java.lang.IllegalArgumentException"),
                new AddTest("Missing Field", new String[] { "Front" }, initialArray, null)
                        .withException("java.lang.IllegalArgumentException"),

                new StudyTest("Regular Case", initialArray),
                new StudyTest("Empty Flashcards", emptyArray),
                new StudyTest("Null Array", null)
                        .withException("java.lang.NullPointerException"),
                new StudyTest("Null Flashcards", new Flashcard[] { null, null })
                        .withException("java.lang.NullPointerException"),

                new SortTest("Front Sort Forwards", sortArray, frontSorted, Flashcard.Field.FRONT, false),
                new SortTest("Front Sort Backwards", sortArray, ArionUtils.reversedArray(frontSorted),
                        Flashcard.Field.FRONT, true),
                new SortTest("Back Sort Forwards", sortArray, backSorted, Flashcard.Field.BACK, false),
                new SortTest("Back Sort Backwards", sortArray, ArionUtils.reversedArray(backSorted),
                        Flashcard.Field.BACK, true),
                new SortTest("Date Sort Forwards", sortArray, dateSorted, Flashcard.Field.REVIEW_DATE, false),
                new SortTest("Date Sort Backwards", sortArray, ArionUtils.reversedArray(dateSorted),
                        Flashcard.Field.REVIEW_DATE, true),
                new SortTest("Interval Sort Forwards", sortArray, intervalSorted, Flashcard.Field.REVIEW_INTERVAL,
                        false),
                new SortTest("Interval Sort Backwards", sortArray, ArionUtils.reversedArray(intervalSorted),
                        Flashcard.Field.REVIEW_INTERVAL, true),
                new SortTest("Null Field", sortArray, null, null, true)
                        .withException("java.lang.NullPointerException"),

                // public DisplayExceptionTest(String testName, Exception exception) {
                // public DisplayExceptionTest(String testName, String message, Exception
                // exception) {
                new DisplayExceptionTest("No Title Regular Case", new NullPointerException("message")),
                new DisplayExceptionTest("No Title Null Exception", null)
                        .withException("java.lang.NullPointerException"),

                new DisplayExceptionTest("Title Regular Case", "title", new NullPointerException("message")),
                new DisplayExceptionTest("Title Null Exception", "title", null)
                        .withException("java.lang.NullPointerException"),
                new DisplayExceptionTest("Title Null Title", null, new NullPointerException("message"))
                        .withException("java.lang.NullPointerException"),
                new DisplayExceptionTest("Title Null Parameters", null, null)
                        .withException("java.lang.NullPointerException"),

                new QuitTest("Regular Case"),
        };
        ArrayList<Test> testList = ArionUtils.toArrayList(tests);
        for (IOTestData testData : IOTestData.testData) {
            testList.add(new ArionIOTest(testData));
        }

        return testList.toArray(new Test[0]);
    }

    public static void main(String[] args) {
        Test.evaluateTests(tests);
    }
}

class ArionConstructorTest extends Test {
    boolean headless;

    public ArionConstructorTest(String testName, boolean headless) {
        super("Arion Constructor Test", testName);
        this.headless = headless;
    }

    void execute() {
        Arion arion = new Arion(headless);
    }
}

class ArionIOTest extends Test {
    IOTestData testData;
    boolean isException;

    public ArionIOTest(IOTestData testData) {
        super("Arion IO Test", testData.testName);
        this.testData = testData.clone();

        exceptionOption = Optional.empty();
        isException = false;
        if (testData.exceptionOption.isPresent()) {
            isException = true;

            // if the exception is unchecked (extends RuntimeException), then allow it;
            // otherwise, it will get checked by Arion.
            Class runtimeClass;
            String exception = testData.exceptionOption.get();
            try {
                runtimeClass = Class.forName(exception);
            } catch (ClassNotFoundException e) {
                System.out.println("Exception " + exception + " is invalid.");
                return;
            }

            Class[] supers = getSuperClasses(runtimeClass);
            for (Class superClass : supers) {
                if (superClass.getName().equals("java.lang.RuntimeException")) {
                    exceptionOption = testData.exceptionOption;
                }
            }
        }
    }

    void execute() throws FileNotFoundException, IOException {
        if (testData.readCase) {
            executeLoadCase();
        }
        if (testData.writeCase) {
            executeSaveCase();
        }
    }

    void executeLoadCase() throws FileNotFoundException {
        Arion arion = new Arion(true);
        arion._testHooks.setDatabaseFile(testData.filepath);
        boolean success = arion._testHooks.loadFlashcardRoutine();

        assert success == !isException : exceptionMessage();

        if (testData.flashcards != null)
            testData.validateFlashcards(arion._testHooks.getFlashcards());
    }

    void executeSaveCase() throws FileNotFoundException, IOException {
        Arion arion = new Arion(true);
        arion._testHooks.setDatabaseFile(IOTestData.WRITE_FILEPATH);
        arion._testHooks.setFlashcards(testData.flashcards);
        boolean success = arion._testHooks.saveFlashcardRoutine();

        assert success == !isException : exceptionMessage();

        if (testData.filepath != null) {
            testData.validateWrittenFile();
        }
    }

    private String exceptionMessage() {
        return isException ? "Expected to be an exception but was not" : "Expected no exception but received one.";
    }

    private static Class[] getSuperClasses(Class runtimeClass) {
        ArrayList<Class> supers = new ArrayList<>();

        Class node = runtimeClass;
        while (!node.getName().equals("java.lang.Object")) {
            supers.add(node);
            node = node.getSuperclass();
        }
        supers.add(node); // add last class
        return supers.toArray(new Class[0]);
    }
}

class DeleteTest extends Test {
    int[] indices;
    ArrayList<Flashcard> initialFlashcards;
    ArrayList<Flashcard> expectedFlashcards;

    public DeleteTest(String testName, int[] indices, Flashcard[] initialFlashcards,
            Flashcard[] expectedFlashcards) {
        super("Arion Delete Test", testName);
        this.indices = indices;
        this.initialFlashcards = ArionUtils.toArrayList(initialFlashcards);
        this.expectedFlashcards = ArionUtils.toArrayList(expectedFlashcards);
    }

    void execute() {
        Arion arion = new Arion(true);
        arion._testHooks.setFlashcards(initialFlashcards);
        arion.deleteFlashcards(indices);

        ArrayList<Flashcard> received = arion._testHooks.getFlashcards();
        Test.validateFlashcards(expectedFlashcards, received);
    }
}

class EditTest extends Test {
    ArrayList<Flashcard> initialFlashcards;
    ArrayList<Flashcard> expectedFlashcards;
    int index;
    String[] fields;

    public EditTest(String testName, Flashcard[] initialFlashcards, Flashcard[] expectedFlashcards,
            int index, String[] fields) {
        super("Arion Edit Test", testName);
        this.initialFlashcards = ArionUtils.toArrayList(initialFlashcards);
        this.expectedFlashcards = ArionUtils.toArrayList(expectedFlashcards);
        this.index = index;
        this.fields = ArionUtils.cloneArray(fields);
    }

    void execute() {
        Arion arion = new Arion(true);
        arion._testHooks.setFlashcards(initialFlashcards);
        arion.editFlashcard(index, fields);

        ArrayList<Flashcard> received = arion._testHooks.getFlashcards();
        Test.validateFlashcards(expectedFlashcards, received);
    }
}

class AddTest extends Test {
    String[] fields;
    ArrayList<Flashcard> initialFlashcards;
    ArrayList<Flashcard> expectedFlashcards;

    public AddTest(String testName, String[] fields, Flashcard[] initialFlashcards,
            Flashcard[] expectedFlashcards) {
        super("Arion Add Test", testName);
        this.fields = ArionUtils.cloneArray(fields);
        this.initialFlashcards = ArionUtils.toArrayList(initialFlashcards);
        this.expectedFlashcards = ArionUtils.toArrayList(expectedFlashcards);
    }

    void execute() {
        Arion arion = new Arion(true);
        arion._testHooks.setFlashcards(initialFlashcards);
        arion.addFlashcard(fields);

        ArrayList<Flashcard> received = arion._testHooks.getFlashcards();
        Test.validateFlashcards(expectedFlashcards, received);
    }
}

class StudyTest extends Test {
    ArrayList<Flashcard> flashcards;

    public StudyTest(String testName, Flashcard[] flashcards) {
        super("Arion Study Test", testName);
        this.flashcards = ArionUtils.toArrayList(flashcards);
    }

    void execute() {
        Arion arion = new Arion(true);
        arion._testHooks.setFlashcards(flashcards);
        arion.studyFlashcards();
    }
}

class SortTest extends Test {
    ArrayList<Flashcard> initialFlashcards;
    ArrayList<Flashcard> expectedFlashcards;
    Flashcard.Field field;
    boolean reversed;

    public SortTest(String testName, Flashcard[] initialFlashcards, Flashcard[] expectedFlashcards,
            Flashcard.Field field, boolean reversed) {
        super("Arion Sort Test", testName);
        this.initialFlashcards = ArionUtils.toArrayList(initialFlashcards);
        this.expectedFlashcards = ArionUtils.toArrayList(expectedFlashcards);
        this.field = field;
        this.reversed = reversed;
    }

    void execute() {
        Arion arion = new Arion(true);
        arion._testHooks.setFlashcards(initialFlashcards);
        arion.sortFlashcards(field, reversed);

        ArrayList<Flashcard> received = arion._testHooks.getFlashcards();
        Test.validateFlashcards(expectedFlashcards, received);
    }
}

class DisplayExceptionTest extends Test {
    private final static String testType = "Arion Display Exception Test";

    String message;
    Exception exception;
    boolean isMessage;

    public DisplayExceptionTest(String testName, Exception exception) {
        super(testType, testName);
        this.message = null;
        isMessage = false;
        this.exception = exception;
    }

    public DisplayExceptionTest(String testName, String message, Exception exception) {
        super(testType, testName);
        this.message = message;
        isMessage = true;
        this.exception = exception;
    }

    void execute() {
        Arion arion = new Arion(true);
        if (isMessage) {
            arion.displayException(message, exception);
        } else {
            arion.displayException(exception);
        }
    }
}

class QuitTest extends Test {

    public QuitTest(String testName) {
        super("Quit Test", testName);
    }

    void execute() {
        Arion arion = new Arion(true);
        arion.quit();
    }
}
