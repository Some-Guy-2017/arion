// Written by - Joseph Hare
// Thursday April 25 2024

/*
 * Arion is a spaced repetition flashcard application.
 */

package arion;
import callback.*;

public class Arion
{
    private static ArionDisplay display;
    private static Flashcard[] flashcards;

    public static void main(String[] args)
    {
        display = new ArionDisplay("Arion", 1024, 576);
        prepareMenuBar();
        display.displayMainScreen(
            () -> System.out.println("TODO: Add 'Add Button' callback"),
            () -> System.out.println("TODO: Add 'Study Button' callback")
        );
    }
    
    public static void loadFlashcards()
    {
        System.out.println("TODO: Load flashcards.");
    }

    public static void saveFlashcards()
    {
        System.out.println("TODO: Save flashcards.");
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
        System.out.println("TODO: Add flashcards.");
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
        
        final AddCallback    addCallback    =                         (String[] fields) -> addFlashcard(fields);
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
