// Written by - Joseph Hare
// Thursday April 25 2024

/*
 * Arion is a spaced repetition flashcard application.
 */

package arion;

import callback.*;
import java.util.ArrayList;
import java.io.IOException;

public class Arion
{
    private final static String databaseFilename = "./flashcards";
    private final static Database database = new Database(databaseFilename);
    private static ArionDisplay display = new ArionDisplay("Arion", 1024, 576);
    private static ArrayList<Flashcard> flashcards = new ArrayList<Flashcard>();
    
    private static AddCallback addCallback = (String[] fields) -> addFlashcard(fields);

    public static void main(String[] args)
    {
        prepareMenuBar();
        enterMainScreen();
    }

    private static void enterMainScreen() {
        display.displayMainScreen(
            () -> display.displayAddScreen(addCallback),
            () -> studyFlashcards()
        );
    }
    
    public static void loadFlashcards()
    {
        if (!database.canRead())
        {
            display.displayMessage("No database file found.", "Database Error");
        }
        try
        {
            flashcards = database.readFlashcards();
        }
        catch (IOException e)
        {
            display.displayMessage("Error while reading flashcards.", "IO Error");
            e.printStackTrace();
        }
        catch (NumberFormatException|Database.DatabaseFormatException e)
        {
            display.displayMessage("Database is formatted improperly.", "Database Format Error");
            e.printStackTrace();
        }
    }

    public static void saveFlashcards()
    {
        try {
            database.writeFlashcards(flashcards);
        }
        catch (IOException e) {
            display.displayMessage("Error while writing flashcards.", "IO Error");
            e.printStackTrace();
        }
    }
    
    public static void deleteFlashcard(int index)
    {
        System.out.println("TODO: Delete flashcards.");
    }

    public static void editFlashcard(int index, String[] fields)
    {
        System.out.println("TODO: Edit flashcards.");
    }

    public static void addFlashcard(String[] fields)
    {
        System.out.println("Adding flashcard.");
        if (fields.length != 2)
        {
            display.displayMessage("Could not construct flashcards because field array is improperly sized.", "Flashcard Creation Error");
            return;
        }
        
        String front = fields[0];
        String back = fields[1];
        flashcards.add(new Flashcard(front, back));

        enterMainScreen();
    }

    public static void studyFlashcards()
    {
        System.out.println("TODO: Study flashcards.");
    }

    public static void sortFlashcards(Flashcard.Field field, boolean reversed)
    {
        System.out.println("TODO: Sort flashcards.");
    }

    public static void quit()
    {
        System.out.println("TODO: Quit.");
    }

    private static void prepareMenuBar()
    {
        String[] menuTitles = new String[] { "File", "Edit", "View", "Help", "Quit" };
        String[][] actions = new String[][] {
            { "Load", "Save" },
            { "Browse", "Add" },
            { "Study", "Sort" },
            { "Guide", "About" },
            { "Confirm?" },
        };

        final SortCallback   sortCallback   = (Flashcard.Field field, boolean reversed) -> sortFlashcards(field, reversed);
        final DeleteCallback deleteCallback =                               (int index) -> deleteFlashcard(index);
        final EditCallback   editCallback   =              (int index, String[] fields) -> editFlashcard(index, fields);

        Runnable[][] callbacks = new Runnable[][] {
            { // File
                () -> loadFlashcards(), // Load
                () -> saveFlashcards(), // Save
            },
            { // Edit
                () -> display.displayBrowseScreen( // Browse
                    flashcards,
                    (int index, String[] fields) -> editFlashcard(index, fields),
                                     (int index) -> deleteFlashcard(index)
                ),
                () -> display.displayAddScreen(addCallback), // Add
            },
            { // View
                () -> studyFlashcards(),                       // Study
                () -> display.displaySortScreen(sortCallback), // Sort
            },
            { // Help
                () -> display.displayGuidePage(0),  // Guide
                () -> display.displayAboutScreen(), // About
            },
            { // Quit
                () -> quit(), // Quit
            }
        };
        display.displayMenuBar(menuTitles, actions, callbacks);
    }
}
