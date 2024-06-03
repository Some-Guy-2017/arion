// Written by - Joseph Hare
// Thursday April 25 2024

/*
 * Arion is a spaced repetition flashcard application.
 */

package arion;

import callback.*;
import exception.*;

import java.util.*; // Arraylist, Queue, LinkedList, List, Arrays
import java.io.*;

public class Arion {
    private final static String databaseFilename = "./flashcards.txt";
    private final Database database = new Database(databaseFilename);

    private final static String logFilename = "./log.txt";
    private static PrintWriter exceptionWriter;

    public ArionDisplay display;
    private boolean headless;

    private ArrayList<Flashcard> flashcards = new ArrayList<Flashcard>();
    private Queue<Flashcard> dueFlashcards = new LinkedList<>();

    private AddCallback addCallback = (String[] fields) -> addFlashcard(fields);
    private ReviewCallback reviewCallback = (boolean success) -> updateReviewedFlashcard(success);

    public Arion() {
        this(false);
    }

    public Arion(boolean headless) {
        writeMockFlashcards();
        if (!headless) {
            display = new ArionDisplay("Arion", 1024, 576);
            prepareMenuBar();
            enterMainScreen();
        }
        else display = null;
        
        try {
            exceptionWriter = new PrintWriter(logFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadFlashcards() {
        try {
            if (confirmOverwrite()) {
                flashcards = database.readFlashcards();
                displayMessage("Read Flashcards.");
            }
        } catch (DatabaseFormatException e) {
            displayWarningMessage("Database file is improperly formatted; please delete " + databaseFilename);
        } catch (DatabaseReadException e) {
            displayWarningMessage("Cannot read from " + database.filename + "\nDoes it exist?");
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            displayWarningMessage("Could not load flashcards due to error.");
        }
    }

    public void saveFlashcards() {
        try {
            if (confirmOverwrite()) {
                database.writeFlashcards(flashcards);
                displayMessage("Wrote Flashcards.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            displayWarningMessage("Could not save flashcards due to error.");
        } catch (DatabaseWriteException e) {
            displayWarningMessage("Cannot write to " + database.filename);
        }
    }

    public void deleteFlashcards(int[] indices) {
        if (indices == null)
            throw new NullPointerException("Null array when trying to delete flashcard.");

        int prev = -1;
        for (int i = indices.length - 1; i >= 0; --i) { // loop backwards so indices are in descending order;
            int idx = indices[i]; // otherwise deleting them will move the index of the other elements
            if (idx < 0 || idx >= flashcards.size())
                throw new IllegalArgumentException("Invalid index when trying to delete flashcard.");
            if (prev != -1 && prev <= idx)
                throw new IllegalArgumentException("Indices to delete were not provided in ascending order");
            flashcards.remove(idx);
            prev = idx;
        }
    }

    public void editFlashcard(int index, String[] fields) {
        int flashcardNum = index + 1; // index starts at 0, numbering starts at 1

        if (fields == null)
            throw new NullPointerException("Null flashcard fields when trying to edit flashcard #" + flashcardNum);
        if (fields.length != 4)
            throw new IllegalArgumentException("Editing flashcards requires four fields.");
        if (index < 0 || index >= flashcards.size())
            throw new IllegalArgumentException("Invalid index.");

        try {
            flashcards.set(index, Flashcard.fromStringArray(fields));
        } catch (DateFormatException e) {
            displayWarningMessage("Incorrectly formatted review date; discarding edits to flashcard #" + flashcardNum);
        } catch (IntervalFormatException e) {
            displayWarningMessage("Incorrectly formatted review interval; discarding edits to flashcard #" + flashcardNum);
        }
    }

    public void addFlashcard(String[] fields) {
        if (fields.length != 2)
            throw new IllegalArgumentException(
                    "Could not construct flashcards because field array is improperly sized.");

        String front = fields[0];
        String back = fields[1];
        flashcards.add(new Flashcard(front, back));
    }

    public void studyFlashcards() {
        dueFlashcards = new LinkedList<>();
        for (Flashcard flashcard : flashcards)
            if (flashcard.isDue()) dueFlashcards.add(flashcard);

        if (dueFlashcards.isEmpty()) {
            displayMessage("There are no flashcards due to study.", "Study Message");
            return;
        }

        if (display == null) return;
        display.displayStudyScreen(dueFlashcards.peek(), true, reviewCallback);
    }

    public void sortFlashcards(Flashcard.Field field, boolean reversed) {
        Flashcard[] oldFlashcards = flashcards.toArray(new Flashcard[] {});
        Flashcard[] newFlashcards = mergeSort(oldFlashcards, field, reversed, 0, flashcards.size());
        flashcards = new ArrayList<>(Arrays.asList(newFlashcards));
    }

    public static void displayException(Exception e) {
        if (e == null) throw new NullPointerException("Attempted to display null exception.");
        
        StringBuilder output = new StringBuilder("An exception occurred");
        String message = e.getMessage();
        if (message != null)
            output.append(": " + message + "\n");
        else
            output.append(".\n");

        output.append("See " + logFilename + " for more information.");

        ArionDisplay.alert(output.toString());
        e.printStackTrace(exceptionWriter);
    }

    public void quit() {
        exceptionWriter.close();
        if (display != null) display.quit();
    }

    public ArrayList<Flashcard> getFlashcards() {
        return flashcards;
    }

    private void prepareMenuBar() {
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
        flashcards.add(new Flashcard("What is the capital of France?", "Paris"));
        flashcards.add(new Flashcard("Who wrote \"To Kill a Mockingbird\"?", "Harper Lee"));
        flashcards.add(new Flashcard("What is the chemical symbol for gold?", "Au"));
        flashcards.add(new Flashcard("What year did World War II end?", "1945"));
        flashcards.add(new Flashcard("Who painted the Mona Lisa?", "Leonardo da Vinci"));
        flashcards.add(new Flashcard("What is the powerhouse of the cell?", "Mitochondria"));
        flashcards.add(new Flashcard("What is the tallest mountain in the world?", "Mount Everest"));
        flashcards.add(new Flashcard("Who invented the telephone?", "Alexander Graham Bell"));
        flashcards.add(new Flashcard("What is the largest planet in our solar system?", "Jupiter"));
        flashcards.add(new Flashcard("What is the chemical formula for water?", "H2O"));
        flashcards.add(new Flashcard("What is the process of plants making their food called?", "Photosynthesis"));
        flashcards.add(new Flashcard("Who discovered penicillin?", "Alexander Fleming"));
        flashcards.add(new Flashcard("What is the speed of light in a vacuum?",
                "Approximately 299,792,458 meters per second"));
        flashcards.add(new Flashcard("What is the longest river in the world?", "The Nile"));
        flashcards.add(new Flashcard("What is the main component of the Earth's atmosphere?", "Nitrogen"));
        flashcards.add(new Flashcard("Who developed the theory of relativity?", "Albert Einstein"));
        flashcards.add(new Flashcard("What is the largest ocean on Earth?", "Pacific Ocean"));
        flashcards.add(new Flashcard("Who wrote \"Romeo and Juliet\"?", "William Shakespeare"));
        flashcards.add(new Flashcard("What is the capital of Japan?", "Tokyo"));
        flashcards.add(new Flashcard("What is the chemical symbol for silver?", "Ag"));
        flashcards.add(new Flashcard("What is the freezing point of water in Fahrenheit?", "32 degrees Fahrenheit"));
        flashcards.add(new Flashcard("What is the largest mammal on Earth?", "Blue whale"));
        flashcards.add(new Flashcard("Who was the first woman to win a Nobel Prize?", "Marie Curie"));
        flashcards.add(new Flashcard("What is the largest desert in the world?", "Antarctica"));
        flashcards.add(new Flashcard("What is the chemical formula for glucose?", "C6H12O6"));
    }

    private void displayMessage(String message) {
        if (display != null)
            display.displayMessage(message);
        else
            System.out.println(message);
    }

    private void displayMessage(String message, String title) {
        if (display != null)
            display.displayMessage(message, title);
        else
            System.out.println(message);
    }

    private void displayWarningMessage(String message) {
        if (display != null)
            display.displayWarningMessage(message);
        else
            System.out.println("WARNING: " + message);
    }

    private void enterMainScreen() {
        display.displayMainScreen(
                () -> display.displayAddScreen(addCallback),
                () -> studyFlashcards());
    }

    private boolean confirmOverwrite() {
        if (display == null) return true;
        return display.displayConfirmationWindow("Overwrite Flashcards?", "Overwrite Confirmation");
    }

    private void updateReviewedFlashcard(boolean success) {
        Flashcard flashcard = dueFlashcards.poll();
        flashcard.updateReviewDate(success);
        if (!success) dueFlashcards.add(flashcard);
        
        if (dueFlashcards.isEmpty()) {
            if (display != null) display.displaySuccessScreen();
            return;
        }
        display.displayStudyScreen(dueFlashcards.peek(), true, reviewCallback);
    }

    private Flashcard[] mergeSort(Flashcard[] flashcards, Flashcard.Field field, boolean reversed, int start, int len) {
        
        if (len == 1) return new Flashcard[] { flashcards[start] };
        if (len == 2) {
            Flashcard flashcard1 = flashcards[start];
            Flashcard flashcard2 = flashcards[start+1];
            if (flashcard1.compareTo(flashcard2, field, reversed)) return new Flashcard[] { flashcard1, flashcard2 };
            else return new Flashcard[] { flashcard2, flashcard1 };
        }
        
        int mid = len / 2 + start;
        Flashcard[] left = mergeSort(flashcards, field, reversed, start, len / 2);
        Flashcard[] right = mergeSort(flashcards, field, reversed, mid, len - len / 2);
        
        int leftIdx = 0;
        int rightIdx = 0;
        Flashcard[] sorted = new Flashcard[len];
        
        for (int sortedIdx = 0; sortedIdx < sorted.length; sortedIdx++) {
            boolean appendLeft;
            
            if (leftIdx == left.length) appendLeft = false;
            else if (rightIdx == right.length) appendLeft = true;
            else if (left[leftIdx].compareTo(right[rightIdx], field, reversed)) appendLeft = true;
            else appendLeft = false;
            
            if (appendLeft) sorted[sortedIdx] = left[leftIdx++];
            else sorted[sortedIdx] = right[rightIdx++];
        }

        return sorted;
    }

    public static void main(String[] args) {
        try {
            Arion arion = new Arion();
        } catch (Exception e) {
            displayException(e);
        }

        exceptionWriter.close();
    }
}
