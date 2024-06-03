package unit_test;

import arion.*;
import callback.*;

import java.util.*; // Random, ArrayList, Arrays
import java.io.*;

public class ArionDisplayTests {
    static Test[] tests;
    
    static final int randomWordCount = 2;
    static Random rand = new Random();
    static ArrayList<String> nounList = new ArrayList<>();
    static String nounFilepath = "./top-1000-nouns.txt";

    static int DEFAULT_WIDTH = 800;
    static int DEFAULT_HEIGHT = 600;

    public static void evaluate() {
        parseNouns();
        Runnable mainScreenAddCallback = () -> System.out.println("AddCallback Run!");
        Runnable mainScreenStudyCallback = () -> System.out.println("StudyCallback Run!");
        AddCallback addCallback = (String[] fields) -> System.out.println("AddCallback ~ Fields: " + Arrays.toString(fields));
        EditCallback editCallback = (int index, String[] fields) ->
            System.out.println("Edit Callback ~ Index: " + index + ", Fields: " + Arrays.toString(fields));
        DeleteCallback deleteCallback = (int[] indices) ->
            System.out.println("Delete Callback ~ Indices: " + Arrays.toString(indices));
        ArrayList<Flashcard> testFlashcards = generateTestFlashcards();
        
        tests = new Test[] {
            new ConstructorTest("100x100 Resolution", 100, 100),
            new ConstructorTest("-100x-100 Resolution", -100, -100),
            new ConstructorTest("0x0 Resolution", 0, 0),
            new ConstructorTest("", 100, 100),
            new ConstructorTest(null, 100, 100),
            new MenuBarTest("Regular Case", 3, 2),
            new MenuBarTest("Empty Menus", 0, 0),
            new MenuBarTest("Incorrectly Sized Menus", 1, 2, 3, 2, 3).withException("IllegalArgumentException"),
            new MenuBarTest("Null Parameters", null, null, null).withException("NullPointerException"),
            new MainScreenTest("Regular Case", mainScreenAddCallback, mainScreenStudyCallback),
            new MainScreenTest("Empty Callbacks", () -> {}, () -> {}),
            new MainScreenTest("Null Callbacks", null, null).withException("NullPointerException"),
            new BrowseScreenTest("Regular Case", testFlashcards, editCallback, deleteCallback),
            new BrowseScreenTest("Empty Flashcards", new ArrayList<>(), editCallback, deleteCallback),
            new BrowseScreenTest("Empty Callbacks", testFlashcards, (int index, String[] fields) -> {}, (int[] indices) -> {}),
            new BrowseScreenTest("Null Parameters", null, null, null).withException("NullPointerException"),
            new AddScreenTest("Regular Case", addCallback),
            new AddScreenTest("Empty Callback", (String[] fields) -> {}),
            new AddScreenTest("Null Callback", null),
            new DisplayMessageTest("Regular Case", "Message", "Title"),
            new DisplayMessageTest("Empty Message and Title", "", ""),
            new DisplayMessageTest("Null Parameters", null, null).withException("NullPointerException"),
            new DisplayWarningTest("Regular Case", "Message"),
            new DisplayWarningTest("Empty Message", ""),
            new DisplayWarningTest("Null Message", null).withException("NullPointerException"),
        };
        for (Test test : tests) test.evaluate();
    }

    static String generateRandomString() {
        StringBuilder builder = new StringBuilder();

        int lastIdx = -1;
        for (int i = 0; i < randomWordCount; i++) {
            int randomIdx;
            do {
                randomIdx = rand.nextInt(nounList.size());
            } while (randomIdx == lastIdx);

            String noun = nounList.get(randomIdx);
            noun = noun.substring(0, 1).toUpperCase() + noun.substring(1).toLowerCase();
            builder.append(noun);
            lastIdx = randomIdx;
        }

        return builder.toString();
    }

    public static ArionDisplay generateDefaultDisplay(String title) {
        return new ArionDisplay(title, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private static void parseNouns() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(nounFilepath));
            nounList = new ArrayList<>(reader.lines().toList());
        } catch (FileNotFoundException e) {
            System.out.println("Could not find " + nounFilepath);
            e.printStackTrace();
            return;
        }
    }

    private static ArrayList<Flashcard> generateTestFlashcards() {
        return new ArrayList<Flashcard>(Arrays.asList(new Flashcard[] {
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
        }));
    }

    public static void main(String[] args) {
        evaluate();
    }
}

class ConstructorTest extends Test {
    int width;
    int height;

    public ConstructorTest(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    void execute() {
        new ArionDisplay(name, width, height);
    }
}

class MenuBarTest extends Test {
    String[] menuTitles;
    String[][] actions;
    Runnable[][] callbacks;

    public MenuBarTest(String name, int menuCount, int actionCount) {
        this.name = name;
        this.menuTitles = generateTestMenus(menuCount);
        this.actions = generateTestActions(menuCount, actionCount);
        this.callbacks = generateTestCallbacks(menuCount, actionCount, this.actions);
    }
    
    public MenuBarTest(String name, int menuCount, int actionWidth, int actionHeight, int callbackWidth, int callbackHeight) {
        this.name = name;
        this.menuTitles = generateTestMenus(menuCount);
        this.actions = generateTestActions(actionWidth, actionHeight);
        this.callbacks = generateTestCallbacks(callbackWidth, callbackHeight, this.actions);
    }

    public MenuBarTest(String name, String[] menuTitles, String[][] actions, Runnable[][] callbacks) {
        this.name = name;
        this.menuTitles = menuTitles;
        this.actions = actions;
        this.callbacks = callbacks;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(name).displayMenuBar(menuTitles, actions, callbacks);
    }

    public static String[] generateTestMenus(int menuCount) {
        String[] menus = new String[menuCount];
        for (int i = 0; i < menuCount; i++)
            menus[i] = ArionDisplayTests.generateRandomString();
        return menus;
    }

    public static String[][] generateTestActions(int menuCount, int actionCount) {
        String[][] actions = new String[menuCount][actionCount];
        for (int menuIdx = 0; menuIdx < menuCount; menuIdx++)
            for (int actionIdx = 0; actionIdx < actionCount; actionIdx++) {
                actions[menuIdx][actionIdx] = ArionDisplayTests.generateRandomString();
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

    public MainScreenTest(String name, Runnable addCallback, Runnable studyCallback) {
        this.name = name;
        this.addCallback = addCallback;
        this.studyCallback = studyCallback;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(name).displayMainScreen(addCallback, studyCallback);
    }
}

class BrowseScreenTest extends Test {
    ArrayList<Flashcard> flashcards;
    EditCallback editCallback;
    DeleteCallback deleteCallback;

    public BrowseScreenTest(String name, ArrayList<Flashcard> flashcards, EditCallback editCallback, DeleteCallback deleteCallback) {
        this.name = name;
        this.flashcards = flashcards;
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(name).displayBrowseScreen(flashcards, editCallback, deleteCallback);
    }
}

class AddScreenTest extends Test {
    AddCallback addCallback;

    public AddScreenTest(String name, AddCallback addCallback) {
        this.name = name;
        this.addCallback = addCallback;
    }
    
    void execute() {
        ArionDisplayTests.generateDefaultDisplay(name).displayAddScreen(addCallback);
    }
}
    
class DisplayMessageTest extends Test {
    String message;
    String title;

    public DisplayMessageTest(String name, String message, String title) {
        this.name = name;
        this.message = message;
        this.title = title;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(name).displayMessage(message, title);
    }
}

class DisplayWarningTest extends Test {
    String message;

    public DisplayWarningTest(String name, String message) {
        this.name = name;
        this.message = message;
    }

    void execute() {
        ArionDisplayTests.generateDefaultDisplay(name).displayWarningMessage(message);
    }
}
