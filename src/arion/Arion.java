package arion;

import callback.*;
import exception.*;

import java.util.*;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.io.*;

/*
 * Arion is a spaced repetition flashcard application.
 * It allows users to load, save, browse, edit, delete, sort, and study their flashcards.
 * Additionally, users can see a guide to better understand the program, and see an about page to get an overview of Arion.
 */

public class Arion {
    private final static String DATABASE_FILENAME = "./flashcards.txt";
    private Database database = new Database(DATABASE_FILENAME);

    private final static String LOG_FILEPATH = "./log.txt";
    private static Optional<PrintWriter> exceptionWriterOption = generateExceptionWriter();

    private Optional<ArionDisplay> displayOption;
    private boolean headless;

    private ArrayList<Flashcard> flashcards = new ArrayList<Flashcard>();
    private Queue<Flashcard> dueFlashcards = new LinkedList<>();

    private AddCallback addCallback = (String[] fields) -> addFlashcard(fields);
    private ReviewCallback reviewCallback = (boolean success) -> updateReviewedFlashcard(success);

    private final static double WINDOW_SCREEN_RATIO = 1.6;

    public class TestHooks {
        private Database origDatabase;
        private Arion parent;

        public TestHooks(Arion parent) {
            this.parent = parent;
        }

        public void setDatabaseFile(String databaseFilename) {
            origDatabase = database;
            database = new Database(databaseFilename);
        }

        public void restoreDatabase() {
            database = origDatabase;
        }

        public ArrayList<Flashcard> getFlashcards() {
            return flashcards;
        }

        public String getDatabaseFilename() {
            return DATABASE_FILENAME;
        }

        public void setFlashcards(ArrayList<Flashcard> flashcards) {
            parent.flashcards = flashcards;
        }

        public boolean loadFlashcardRoutine() {
            return parent.loadFlashcardRoutine();
        }

        public boolean saveFlashcardRoutine() {
            return parent.saveFlashcardRoutine();
        }
    }

    public TestHooks _testHooks = new TestHooks(this);

    /*
     * The default Arion constructor simply initializes itself with a default headless
     * value of false.
     *
     * Input: no input value.
     * Output: new non-headless Arion class.
     */
    public Arion() {
        this(false);
    }

    /*
     * The Arion constructor constructs a new Arion, optionally initializing
     * a new display.
     *
     * Input: boolean representing whether Arion should be headless.
     * Output: new Arion class.
     */
    public Arion(boolean headless) {
        writeMockFlashcards();
        
        if (!headless) {
            Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (windowSize.width / WINDOW_SCREEN_RATIO);
            int height = (int) (windowSize.height / WINDOW_SCREEN_RATIO);
            displayOption = Optional.of(new ArionDisplay("Arion", width, height));

            prepareMenuBar();
            enterMainScreen();
        } else {
            displayOption = Optional.empty();
        }
    }

    /*
     * loadFlashcards loads the flashcards in the flashcard database
     * into memory.
     *
     * Input: no input value.
     * Output: no return value, modifies the flashcard ArrayList.
     */
    public void loadFlashcards() {
        if (confirmOverwrite()) {
            boolean success = loadFlashcardRoutine();
            if (success) {
                ArionDisplay.alert("Read Flashcards.");
            }
        }
    }

    /*
     * saveFlashcards saves the flashcards in memory to the
     * flashcard database.
     *
     * Input: no input value.
     * Output: no return value, writes to the flashcard database.
     */
    public void saveFlashcards() {
        if (!confirmOverwrite()) {
            return;
        }
        boolean success = saveFlashcardRoutine();
        if (success) {
            ArionDisplay.alert("Wrote Flashcards.");
        }
    }

    /*
     * deleteFlashcards deletes the flashcards at the indices provided in an array.
     * Since this method is called with indices from a JTable, they are always in ascending order;
     * thus, no sorting needs to be done in order to loop the indices in descending order.
     * The getDeletionIndex method checks that the indices do ascend.
     *
     * Input: ascending array of indices at which to delete flashcards.
     * Output: no return value, modifies the flashcard ArrayList.
     */
    public void deleteFlashcards(int[] indices) {
        if (indices == null) {
            throw new NullPointerException("Null array when trying to delete flashcard.");
        }
        for (int i = indices.length - 1; i >= 0; --i) { // loop backwards so indices are in descending order;
            int idx = getDeletionIndex(indices, i);     // otherwise deleting them will move the index of the other elements
            flashcards.remove(idx);
        }
    }

    /*
     * editFlashcard edits a flashcard at a given index, replacing it with a new flashcard given by
     * the fields String array.
     *
     * Input: the index of the flashcard to edit, the new fields of the flashcards.
     * Output: no return value, modifies the flashcard ArrayList.
     */
    public void editFlashcard(int index, String[] fields) {
        int flashcardNum = index + 1; // index starts at 0, numbering starts at 1

        if (fields == null) {
            throw new NullPointerException("Null flashcard fields when trying to edit flashcard #" + flashcardNum);
        }
        if (fields.length != 4) {
            throw new IllegalArgumentException("Editing flashcards requires four fields.");
        }
        if (index < 0 || index >= flashcards.size()) {
            throw new IllegalArgumentException("Invalid index.");
        }

        try {
            flashcards.set(index, Flashcard.fromStringArray(fields));
        } catch (DateFormatException e) {
            ArionDisplay.warningAlert("Incorrectly formatted review date; discarding edits to flashcard #" + flashcardNum);
        } catch (IntervalFormatException e) {
            ArionDisplay.warningAlert(
                    "Incorrectly formatted review interval; discarding edits to flashcard #" + flashcardNum);
        }
    }

    /*
     * addFlashcard appends a flashcard to the flashcard ArrayList.
     * The flashcard to append is represented as a String array containing the
     * flashcard front and back.
     * 
     * Input: String array representing a flashcard.
     * Output: no return value, appends to the flashcard ArrayList.
     */
    public void addFlashcard(String[] fields) {
        if (fields.length != 2) {
            String msg = "Could not construct flashcards because field array is improperly sized.";
            throw new IllegalArgumentException(msg);
        }

        // the exceptions can never be thrown, because the review date and interval
        // are not given here
        try {
            flashcards.add(Flashcard.fromStringArray(fields));
        } catch (IntervalFormatException | DateFormatException e) {
            displayException(e);
        }
    }

    /*
     * studyFlashcards initiates the studying loop.
     *
     * Input: no input.
     * Output: no output.
     */
    public void studyFlashcards() {
        dueFlashcards = new LinkedList<>();
        for (Flashcard flashcard : flashcards) {
            if (flashcard.isDue()) {
                dueFlashcards.add(flashcard);
            }
        }

        if (dueFlashcards.isEmpty()) {
            ArionDisplay.alert("There are no flashcards due to study.");
            return;
        }

        if (!displayOption.isPresent()) {
            return;
        }
        displayOption.get().displayStudyScreen(dueFlashcards.peek(), true, reviewCallback);
    }

    /*
     * sortFlashcards sorts the flashcards ArrayList by the given field, and
     * whether to reverse it.
     *
     * Input: field to sort the flashcards by, and whether to reverse the list.
     * Output: no return value, modifies the flashcard ArrayList.
     */
    public void sortFlashcards(Flashcard.Field field, boolean reversed) {
        if (field == null) {
            throw new NullPointerException("Cannot sort with null field.");
        }

        Flashcard[] oldFlashcards = flashcards.toArray(new Flashcard[] {});
        Flashcard[] newFlashcards = mergeSort(oldFlashcards, field, reversed, 0, flashcards.size());
        flashcards = ArionUtils.toArrayList(newFlashcards);
    }

    /*
     * This displayException signature accepts a message to display along with the exception,
     * and an exception.
     *
     * Input: the message to display with the exception, the exception to display and log.
     * Output: no return value, displays the message and exception to the user, and writes
     * the exception to a file.
     */
    public static void displayException(String message, Exception e) {
        if (message == null) {
            throw new NullPointerException("Cannot display null message.");
        }
        _displayException(Optional.of(message), e);
    }

    /*
     * This displayException signature only accepts an exception to display.
     *
     * Input: the exception to display and log.
     * Output: no return value, displays the exception to the user, and writes
     * the exception to a file.
     */
    public static void displayException(Exception e) {
        _displayException(Optional.empty(), e);
    }

    /*
     * displayException properly handles an exception by displaying it to the user
     * and logging it to a file.
     * It can optionally accept a message to display with the exception.
     *
     * Input: optional message to display with the exception, exception to display.
     * Output: no return value, displays the exception and optionally the message to the user, and writes
     * the exception to a file.
     */
    private static void _displayException(Optional<String> messageOption, Exception e) {
        if (e == null) {
            throw new NullPointerException("Attempted to display null exception.");
        }

        StringBuilder output = new StringBuilder();
        if (messageOption.isPresent()) {
            output.append(messageOption.get() + "\n");
        }

        output.append("An exception occurred");
        String message = e.getMessage();
        if (message != null)
            output.append(": " + message + "\n");
        else
            output.append(".\n");

        output.append("See " + LOG_FILEPATH + " for more information.");

        ArionDisplay.alert(output.toString());
        if (exceptionWriterOption.isPresent())
            e.printStackTrace(exceptionWriterOption.get());
        else
            e.printStackTrace();
    }

    /*
     * quit quits Arion.
     *
     * Input: no input value.
     * Output: no output.
     */
    public void quit() {
        if (exceptionWriterOption.isPresent()) {
            exceptionWriterOption.get().flush();
        }
        if (displayOption.isPresent()) {
            displayOption.get().quit();
        }
    }
    
    /*
     * prepareMenuBar prepares the menu bar displayed in the GUI.
     *
     * Input: no input.
     * Output: no return value, modifies the display.
     */
    private void prepareMenuBar() {
        if (!displayOption.isPresent()) {
            ArionDisplay.alert("Cannot prepare menu bar when display is not present.");
        }
        ArionDisplay display = displayOption.get();

        String[] menuTitles = new String[] { "File", "Edit", "View", "Help", "Quit" };
        String[][] actions = new String[][] {
                { "Load", "Save" },
                { "Browse", "Add" },
                { "Study", "Sort" },
                { "Guide", "About" },
                { "Confirm?" },
        };

        final SortCallback sortCallback = (Flashcard.Field field, boolean reversed) -> sortFlashcards(field, reversed);
        final DeleteCallback deleteCallback = (int[] indices) -> deleteFlashcards(indices);
        final EditCallback editCallback = (int index, String[] fields) -> editFlashcard(index, fields);

        Runnable[][] callbacks = new Runnable[][] {
                { // File
                        () -> loadFlashcards(), // Load
                        () -> saveFlashcards(), // Save
                },
                { // Edit
                        () -> display.displayBrowseScreen(flashcards, editCallback, deleteCallback),
                        () -> display.displayAddScreen(addCallback), // Add
                },
                { // View
                        () -> studyFlashcards(), // Study
                        () -> display.displaySortScreen(sortCallback), // Sort
                },
                { // Help
                        () -> display.displayGuidePage(0), // Guide
                        () -> display.displayAboutScreen(), // About
                },
                { // Quit
                        () -> quit(), // Quit
                }
        };
        display.displayMenuBar(menuTitles, actions, callbacks);
    }

    private void writeMockFlashcards() {
        //flashcards.add(new Flashcard("What is the capital of France?", "Paris"));
        //flashcards.add(new Flashcard("Who wrote \"To Kill a Mockingbird\"?", "Harper Lee"));
        //flashcards.add(new Flashcard("What is the chemical symbol for gold?", "Au"));
        //flashcards.add(new Flashcard("What year did World War II end?", "1945"));
        //flashcards.add(new Flashcard("Who painted the Mona Lisa?", "Leonardo da Vinci"));
        //flashcards.add(new Flashcard("What is the powerhouse of the cell?", "Mitochondria"));
        //flashcards.add(new Flashcard("What is the tallest mountain in the world?", "Mount Everest"));
        //flashcards.add(new Flashcard("Who invented the telephone?", "Alexander Graham Bell"));
        //flashcards.add(new Flashcard("What is the largest planet in our solar system?", "Jupiter"));
        //flashcards.add(new Flashcard("What is the chemical formula for water?", "H2O"));
        //flashcards.add(new Flashcard("What is the process of plants making their food called?", "Photosynthesis"));
        //flashcards.add(new Flashcard("Who discovered penicillin?", "Alexander Fleming"));
        //// flashcards.add(new Flashcard("What is the speed of light in a vacuum?",
        //// "Approximately 299,792,458 meters per second"));
        //flashcards.add(new Flashcard("What is the longest river in the world?", "The Nile"));
        //flashcards.add(new Flashcard("What is the main component of the Earth's atmosphere?", "Nitrogen"));
        //flashcards.add(new Flashcard("Who developed the theory of relativity?", "Albert Einstein"));
        //flashcards.add(new Flashcard("What is the largest ocean on Earth?", "Pacific Ocean"));
        //flashcards.add(new Flashcard("Who wrote \"Romeo and Juliet\"?", "William Shakespeare"));
        //flashcards.add(new Flashcard("What is the capital of Japan?", "Tokyo"));
        //flashcards.add(new Flashcard("What is the chemical symbol for silver?", "Ag"));
        //flashcards.add(new Flashcard("What is the freezing point of water in Fahrenheit?", "32 degrees Fahrenheit"));
        //flashcards.add(new Flashcard("What is the largest mammal on Earth?", "Blue whale"));
        //flashcards.add(new Flashcard("Who was the first woman to win a Nobel Prize?", "Marie Curie"));
        //flashcards.add(new Flashcard("What is the largest desert in the world?", "Antarctica"));
        //flashcards.add(new Flashcard("What is the chemical formula for glucose?", "C6H12O6"));
    }

    /*
     * enterMainScreen generates the main screen callbacks, then calls ArionDisplay
     * to display the main screen.
     *
     * Input: no input.
     * Output: no return value, displays the main screen to the user.
     */
    private void enterMainScreen() {
        if (!displayOption.isPresent()) {
            ArionDisplay.warningAlert("Cannot enter main screen when display is not present.");
            return;
        }
        ArionDisplay display = displayOption.get();
        display.displayMainScreen(
                () -> display.displayAddScreen(addCallback),
                () -> studyFlashcards());
    }

    /*
     * confirmOverwrite confirms with the user whether they would like to overwrite
     * flashcards.
     *
     * Input: no input.
     * Output: boolean representing whether the user confirmed the over write.
     */
    private boolean confirmOverwrite() {
        if (displayOption.isEmpty()) {
            ArionDisplay.warningAlert("Cannot confirm overwrite when display is not present.");
            return true;
        }
        return displayOption.get().displayConfirmationWindow("Overwrite Flashcards?", "Overwrite Confirmation");
    }

    /*
     * updateReviewedFlashcard updates the most recent flashcard to be reviewed,
     * which is the first flashcard in the dueFlashcards queue.
     *
     * Input: whether the user correctly reviewed the last flashcard.
     * Output: no return value, modifies the dueFlashcards queue.
     */
    private void updateReviewedFlashcard(boolean success) {
        if (!displayOption.isPresent()) {
            ArionDisplay.warningAlert("Cannot perform study function without display.");
            return;
        }
        ArionDisplay display = displayOption.get();

        // flashcard can never be null, but it is checked just in case
        Flashcard flashcard = dueFlashcards.poll();
        if (flashcard == null) {
            Arion.displayException(new NullPointerException("Reviewed flashcard is null"));
            return;
        }
        
        flashcard.updateReview(success);
        if (!success) {
            dueFlashcards.add(flashcard);
        }

        if (dueFlashcards.isEmpty()) {
            display.displaySuccessScreen();
            return;
        }
        display.displayStudyScreen(dueFlashcards.peek(), true, reviewCallback);
    }

    /*
     * mergeSort sorts the provided array by the given field and direction, between the provided start and end.
     * It uses merge sort to ensure adequate performance even at large Flashcard lists (O(nlogn) vs O(n*n)).
     *
     * Input: flashcard array to sort, field to sort by, whether to sort in reversed order, the starting index,
     * and the number of elements to sort.
     * Output: a new array containing the elements in sorted order.
     */
    private Flashcard[] mergeSort(Flashcard[] flashcards, Flashcard.Field field, boolean reversed, int start, int len) {

        if (len == 1) {
            return new Flashcard[] { flashcards[start] };
        }
        if (len == 2) {
            Flashcard flashcard1 = flashcards[start];
            Flashcard flashcard2 = flashcards[start + 1];
            if (flashcard1.compareTo(flashcard2, field, reversed)) {
                return new Flashcard[] { flashcard1, flashcard2 };
            }
            else {
                return new Flashcard[] { flashcard2, flashcard1 };
            }
        }

        int mid = len / 2 + start;
        Flashcard[] left = mergeSort(flashcards, field, reversed, start, len / 2);
        Flashcard[] right = mergeSort(flashcards, field, reversed, mid, len - len / 2);

        int leftIdx = 0;
        int rightIdx = 0;
        Flashcard[] sorted = new Flashcard[len];

        for (int sortedIdx = 0; sortedIdx < sorted.length; sortedIdx++) {
            boolean appendLeft;

            // determine whether to append an element from the left or right half
            if (leftIdx == left.length) {
                appendLeft = false;
            } else if (rightIdx == right.length) {
                appendLeft = true;
            } else if (left[leftIdx].compareTo(right[rightIdx], field, reversed)) {
                appendLeft = true;
            } else {
                appendLeft = false;
            }

            // append the appropriate element
            if (appendLeft) {
                sorted[sortedIdx] = left[leftIdx++];
            }
            else {
                sorted[sortedIdx] = right[rightIdx++];
            }
        }

        return sorted;
    }

    /*
     * generateExceptionWriter conditionally generates a PrintWriter to the exception log file
     * if it can; otherwise, nothing is returned.
     *
     * Input: no input.
     * Output: optionally a PrintWriter that writes to the log file.
     */
    private static Optional<PrintWriter> generateExceptionWriter() {
        try {
            return Optional.of(new PrintWriter(LOG_FILEPATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /*
     * loadFlashcardRoutine loads the flashcards from the database into memory, without asking
     * for confirmation from the user.
     * This method exists because when testing, confirmation is not required; thus, it was brought
     * into its own method so the overwrite confirmation is not run during testing.
     *
     * Input: no input.
     * Output: boolean representing whether the operation succeeded, writes to flashcard ArrayList.
     */
    private boolean loadFlashcardRoutine() {
        try {
            flashcards = database.readFlashcards();
            return true;
        } catch (DatabaseFormatException e) {
            ArionDisplay.warningAlert("Database file is improperly formatted; please delete " + DATABASE_FILENAME);
        } catch (DatabaseReadException e) {
            ArionDisplay.warningAlert("Cannot read from " + database.filepath + "\nDoes it exist?");
        } catch (IOException e) {
            displayException(e);
        }
        return false;
    }

    /*
     * saveFlashcardRoutine saves the flashcards in memory into the database, without asking
     * for confirmation from the user.
     * This method exists because when testing, confirmation is not required; thus, it was brought
     * into its own method so the overwrite confirmation is not run during testing.
     *
     * Input: no input.
     * Output: boolean representing whether the operation succeeded, writes to flashcard database.
     */
    private boolean saveFlashcardRoutine() {
        try {
            database.writeFlashcards(flashcards);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            ArionDisplay.warningAlert("Could not save flashcards due to error.");
        } catch (DatabaseWriteException e) {
            ArionDisplay.warningAlert("Cannot write to " + database.filepath);
        }
        return false;
    }

    /*
     * getDeletionIndex reads the flashcard index to delete from the provided indices array.
     * This method is separate from the deleteFlashcards method to simplify the code; the checks
     * take many lines, and have little relevant functionality to understanding the program.
     *
     * Input: array of flashcard indices to delete, index into the indices array.
     * Output: index of the flashcard to delete.
     */
    private int getDeletionIndex(int[] indices, int i) {
        int idx = indices[i]; 
        if (idx < 0 || idx >= flashcards.size()) {
            throw new IllegalArgumentException("Invalid index when trying to delete flashcard.");
        }
        if (idx > 0 && indices[i-1] <= idx) {
            throw new IllegalArgumentException("Indices to delete were not provided in ascending order");
        }
        return idx;
    }

    /*
     * main is the entry point to the program. It initializes an Arion class, and
     * catches exceptions so they are displayed to the user.
     *
     * Input: command line arguments.
     * Output: no output.
     */
    public static void main(String[] args) {
        try {
            Arion arion = new Arion();
        } catch (Exception e) {
            displayException(e);
        }
    }
}
