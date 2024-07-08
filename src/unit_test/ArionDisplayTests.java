package unit_test;

import arion.*;
import callback.*;
import exception.GuideDisplayException;

import java.util.*; // Random, ArrayList, Arrays
import java.io.*;

public class ArionDisplayTests {
    static int DEFAULT_WIDTH = 800;
    static int DEFAULT_HEIGHT = 600;

    static Test[] tests = generateTests();

    public static ArionDisplay generateDefaultDisplay(String title) {
        return new ArionDisplay(title, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    private static Test[] generateTests() {
        ArrayList<Flashcard> testFlashcards = generateTestFlashcards();

        Runnable mainScreenAddCallback = () -> System.out.println("AddCallback Run!");
        Runnable mainScreenStudyCallback = () -> System.out.println("StudyCallback Run!");
        AddCallback addCallback = (String[] fields) -> System.out
                .println("AddCallback ~ Fields: " + Arrays.toString(fields));
        EditCallback editCallback = (int index, String[] fields) -> {
            System.out.println("Edit Callback ~ Index: " + index + ", Fields: " + Arrays.toString(fields));
        };
        DeleteCallback deleteCallback = (int[] indices) -> {
            System.out.println("Delete Callback ~ Indices: " + Arrays.toString(indices));
        };
        Flashcard testFlashcard = new Flashcard("front", "back");
        ReviewCallback reviewCallback = (boolean success) -> System.out
                .println("Running review callback with success=" + success + ".");
        SortCallback sortCallback = (Flashcard.Field field, boolean reversed) -> {
            System.out.println("Running sort callback with field=" + field + ", reversed=" + reversed + ".");
        };

        Test[] tests = new Test[] {
                new DisplayConstructorTest("100x100 Resolution", 100, 100),
                new DisplayConstructorTest("-100x-100 Resolution", -100, -100),
                new DisplayConstructorTest("0x0 Resolution", 0, 0),
                new DisplayConstructorTest("", 100, 100),
                new DisplayConstructorTest(null, 100, 100),
                new DisplayConstructorTest("", 1, 1),
                new MenuBarTest("Regular Case", 3, 2),
                new MenuBarTest("Empty Menus", 0, 0),
                new MenuBarTest("Incorrectly Sized Menus", 1, 2, 3, 2, 3)
                        .withException("java.lang.IllegalArgumentException"),
                new MenuBarTest("Null Parameters", null, null, null).withException("java.lang.NullPointerException"),
                new MainScreenTest("Regular Case", mainScreenAddCallback, mainScreenStudyCallback),
                new MainScreenTest("Empty Callbacks", () -> {
                }, () -> {
                }),
                new MainScreenTest("Null Callbacks", null, null).withException("java.lang.NullPointerException"),
                new BrowseScreenTest("Regular Case", testFlashcards, editCallback, deleteCallback),
                new BrowseScreenTest("Empty Flashcards", new ArrayList<>(), editCallback, deleteCallback),
                new BrowseScreenTest("Empty Callbacks", testFlashcards, (int index, String[] fields) -> {
                }, (int[] indices) -> {
                }),
                new BrowseScreenTest("Null Parameters", null, null, null)
                        .withException("java.lang.NullPointerException"),
                new AddScreenTest("Regular Case", addCallback),
                new AddScreenTest("Empty Callback", (String[] fields) -> {
                }),
                new AddScreenTest("Null Callback", null).withException("java.lang.NullPointerException"),
                new StudyScreenTest("Regular Front Case", testFlashcard, true, reviewCallback),
                new StudyScreenTest("Regular Back Case", testFlashcard, false, reviewCallback),
                new StudyScreenTest("Null Flashcard", null, true, reviewCallback)
                        .withException("java.lang.NullPointerException"),
                new StudyScreenTest("Null Callback", testFlashcard, true, null)
                        .withException("java.lang.NullPointerException"),
                new StudyScreenTest("Null Parameters", null, true, null)
                        .withException("java.lang.NullPointerException"),
                new SortScreenTest("Regular Case", sortCallback),
                new SortScreenTest("Null Callback", null).withException("java.lang.NullPointerException"),
                new GuidePageTest("Regular Case", 0),
                new GuidePageTest("Small index", -1).withException("exception.GuideDisplayException"),
                new GuidePageTest("Large index", 100).withException("exception.GuideDisplayException"),
                new GuidePageTest("Missing Guide File", 0, "./test-files/guides/missing.xml")
                        .withException("exception.GuideDisplayException"),
                new GuidePageTest("Invalid Guide File", 0, "./test-files/guides/invalid.xml")
                        .withException("exception.GuideDisplayException"),
                new AboutScreenTest("Regular Case"),
                new AlertTest("Regular Case", "Alert!"),
                new AlertTest("Empty Message", ""),
                new AlertTest("Null Message", null).withException("java.lang.NullPointerException"),
                new ConfirmationWindowTest("Regular Case", "Message", "Title"),
                new ConfirmationWindowTest("Empty Message and Title", "", ""),
                new ConfirmationWindowTest("Null Message", null, "Title")
                        .withException("java.lang.NullPointerException"),
                new ConfirmationWindowTest("Null Title", "Message", null)
                        .withException("java.lang.NullPointerException"),
                new ConfirmationWindowTest("Null Parameters", null, null)
                        .withException("java.lang.NullPointerException"),
                new QuitTest("Regular Case"),
                new SuccessScreenTest("Regular Case"),
        };
        return tests;
    }

    private static ArrayList<Flashcard> generateTestFlashcards() {
        return ArionUtils.toArrayList(new Flashcard[] {
                new Flashcard("What is the capital of France?", "Paris"),
                new Flashcard("Who wrote \"To Kill a Mockingbird\"?", "Harper Lee"),
                new Flashcard("What is the chemical symbol for gold?", "Au"),
                new Flashcard("What year did World War II end?", "1945"),
                new Flashcard("Who painted the Mona Lisa?", "Leonardo da Vinci"),
                new Flashcard("What is the powerhouse of the cell?", "Mitochondria"),
                new Flashcard("What is the tallest mountain in the world?", "Mount Everest"),
                new Flashcard("Who invented the telephone?", "Alexander Graham Bell"),
                new Flashcard("What is the largest planet in our solar system?", "Jupiter"),
                new Flashcard("What is the chemical formula for water?", "H2O"),
                new Flashcard("What is the process of plants making their food called?", "Photosynthesis"),
                new Flashcard("Who discovered penicillin?", "Alexander Fleming"),
                new Flashcard("What is the speed of light in a vacuum?", "Approximately 299,792,458 meters per second"),
                new Flashcard("What is the longest river in the world?", "The Nile"),
                new Flashcard("What is the main component of the Earth's atmosphere?", "Nitrogen"),
                new Flashcard("Who developed the theory of relativity?", "Albert Einstein"),
                new Flashcard("What is the largest ocean on Earth?", "Pacific Ocean"),
                new Flashcard("Who wrote \"Romeo and Juliet\"?", "William Shakespeare"),
                new Flashcard("What is the capital of Japan?", "Tokyo"),
                new Flashcard("What is the chemical symbol for silver?", "Ag"),
                new Flashcard("What is the freezing point of water in Fahrenheit?", "32 degrees Fahrenheit"),
                new Flashcard("What is the largest mammal on Earth?", "Blue whale"),
                new Flashcard("Who was the first woman to win a Nobel Prize?", "Marie Curie"),
                new Flashcard("What is the largest desert in the world?", "Antarctica"),
                new Flashcard("What is the chemical formula for glucose?", "C6H12O6"),
        });
    }

    public static void main(String[] args) {
        Test.evaluateTests(tests);
    }
}

class DisplayConstructorTest extends Test {
    int width;
    int height;

    public DisplayConstructorTest(String testName, int width, int height) {
        super("Display Constructor Test", testName);
        this.width = width;
        this.height = height;
    }

    void execute() {
        new ArionDisplay(qualifiedName, width, height);
    }
}

class MenuBarTest extends Test {
    static String testType = "Display Menu Bar Test";

    String[] menuTitles;
    String[][] actions;
    Runnable[][] callbacks;

    public MenuBarTest(String testName, int menuCount, int actionCount) {
        super(testType, testName);

        this.menuTitles = generateTestMenus(menuCount);
        this.actions = generateTestActions(menuCount, actionCount);
        this.callbacks = generateTestCallbacks(menuCount, actionCount, this.actions);
    }

    public MenuBarTest(String testName, int menuCount, int actionWidth, int actionHeight, int callbackWidth,
            int callbackHeight) {
        super(testType, testName);

        this.menuTitles = generateTestMenus(menuCount);
        this.actions = generateTestActions(actionWidth, actionHeight);
        this.callbacks = generateTestCallbacks(callbackWidth, callbackHeight, this.actions);
    }

    public MenuBarTest(String testName, String[] menuTitles, String[][] actions, Runnable[][] callbacks) {
        super(testType, testName);

        this.menuTitles = menuTitles;
        this.actions = actions;
        this.callbacks = callbacks;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).displayMenuBar(menuTitles, actions, callbacks);
    }

    public static String[] generateTestMenus(int menuCount) {
        String[] menus = new String[menuCount];
        for (int i = 0; i < menuCount; i++) {
            Optional<String> randomStr = ArionUtils.generateRandomString();
            if (randomStr.isEmpty())
                menus[i] = "Menu " + i;
            else
                menus[i] = randomStr.get();
        }
        return menus;
    }

    public static String[][] generateTestActions(int menuCount, int actionCount) {
        String[][] actions = new String[menuCount][actionCount];
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++)
            for (int actionIdx = 0; actionIdx < actionCount; actionIdx++) {
                Optional<String> randomStr = ArionUtils.generateRandomString();
                if (randomStr.isEmpty())
                    actions[menuIdx][actionIdx] = "Action " + menuIdx + "," + actionIdx;
                else
                    actions[menuIdx][actionIdx] = randomStr.get();
            }

        return actions;
    }

    public static Runnable[][] generateTestCallbacks(int menuCount, int actionCount, String[][] actions) {
        Runnable[][] callbacks = new Runnable[menuCount][actionCount];
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++)
            for (int actionIdx = 0; actionIdx < actionCount; actionIdx++) {
                String action = actions[menuIdx][actionIdx];

                // declare them final for the anonymous class
                final int finalMenuIdx = menuIdx;
                final int finalActionIdx = actionIdx;

                callbacks[menuIdx][actionIdx] = () -> {
                    System.out.println("Action " + action + " called. This is action " + finalMenuIdx + ":"
                            + finalActionIdx + ".");
                };
            }

        return callbacks;

    }
}

class MainScreenTest extends Test {
    Runnable addCallback;
    Runnable studyCallback;

    public MainScreenTest(String testName, Runnable addCallback, Runnable studyCallback) {
        super("Display Main Screen Test", testName);
        this.addCallback = addCallback;
        this.studyCallback = studyCallback;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).displayMainScreen(addCallback, studyCallback);
    }
}

class BrowseScreenTest extends Test {
    ArrayList<Flashcard> flashcards;
    EditCallback editCallback;
    DeleteCallback deleteCallback;

    public BrowseScreenTest(String testName, ArrayList<Flashcard> flashcards, EditCallback editCallback,
            DeleteCallback deleteCallback) {
        super("Display Browse Screen Test", testName);
        this.flashcards = flashcards;
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).displayBrowseScreen(flashcards, editCallback,
                deleteCallback);
    }
}

class AddScreenTest extends Test {
    AddCallback addCallback;

    public AddScreenTest(String testName, AddCallback addCallback) {
        super("Display Add Screen Test", testName);
        this.addCallback = addCallback;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).displayAddScreen(addCallback);
    }
}

class StudyScreenTest extends Test {
    Flashcard flashcard;
    boolean front;
    ReviewCallback reviewCallback;

    public StudyScreenTest(String testName, Flashcard flashcard, boolean front, ReviewCallback reviewCallback) {
        super("Display Study Screen Test", testName);
        this.flashcard = flashcard;
        this.front = front;
        this.reviewCallback = reviewCallback;
    }

    void execute() {
        ArionDisplay display = ArionDisplayTests.generateDefaultDisplay(qualifiedName);
        display.displayStudyScreen(flashcard, front, reviewCallback);
    }
}

class SortScreenTest extends Test {
    SortCallback callback;

    public SortScreenTest(String testName, SortCallback callback) {
        super("Display Sort Screen Test", testName);
        this.callback = callback;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).displaySortScreen(callback);
    }
}

class GuidePageTest extends Test {
    int pageNum;
    Optional<String> guideFileOption;

    public GuidePageTest(String testName, int pageNum) {
        this(testName, pageNum, Optional.empty());
    }

    public GuidePageTest(String testName, int pageNum, String guideFile) {
        this(testName, pageNum, Optional.of(guideFile));
    }

    private GuidePageTest(String testName, int pageNum, Optional<String> guideFileOption) {
        super("Display Guide Page Test", testName);
        this.pageNum = pageNum;
        this.guideFileOption = guideFileOption;
    }

    void execute() throws GuideDisplayException {
        ArionDisplay display = ArionDisplayTests.generateDefaultDisplay(qualifiedName);

        ArionDisplay._TestHooks testHooks = display._testHooks;
        if (guideFileOption.isPresent()) {
            testHooks.setGuideFilepath(guideFileOption.get());
        } else {
            testHooks.setGuideFilepath(display._testHooks.getGuideFilepath());
        }

        testHooks.displayGuidePageRoutine(pageNum);
    }
}

class AboutScreenTest extends Test {
    public AboutScreenTest(String testName) {
        super("Display About Screen Test", testName);
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).displayAboutScreen();
    }
}

class AlertTest extends Test {
    String message;

    public AlertTest(String testName, String message) {
        super("Display Alert Test", testName);
        this.message = message;
    }

    void execute() {
        ArionDisplay.alert(message);
    }
}

class ConfirmationWindowTest extends Test {
    String message;
    String title;

    public ConfirmationWindowTest(String testName, String message, String title) {
        super("Display Confirmation Window Test", testName);
        this.message = message;
        this.title = title;
    }

    void execute() {
        ArionDisplay display = ArionDisplayTests.generateDefaultDisplay(qualifiedName);
        boolean response = display.displayConfirmationWindow(message, title);
        System.out.println("Received response of " + response + ".");
    }
}

class QuitTest extends Test {

    public QuitTest(String testName) {
        super("Display Quit Test", testName);
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).quit();
    }
}

class SuccessScreenTest extends Test {

    public SuccessScreenTest(String testName) {
        super("Display Success Screen Test", testName);
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(qualifiedName).displaySuccessScreen();
    }
}
