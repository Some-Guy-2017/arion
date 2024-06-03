package unit_test;

import arion.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import exception.*;

public class ArionTests {
    /*

    private static void testConstructors() {
        new Arion();
        new Arion(true);
        new Arion(false);
        System.out.println("Successful constructor test.");
    }

    private static void testLoadFlashcards() {
        new Arion(true).loadFlashcards();
        new Arion(false).loadFlashcards();
        System.out.println("Successful loadFlashcards test.");
    }

    private static void testSaveFlashcards() {
        new Arion(true).saveFlashcards();
        new Arion(false).saveFlashcards();
        System.out.println("Successful saveFlashcard test.");
    }

    private static void testDeleteFlashcards() {
        deleteTestCase(5, new int[] { 1, 2, 3 }, "regular case", new int[] { 0, 4 });
        deleteTestCase(5, new int[] {}, "no indices with flashcards", new int[] { 0, 1, 2, 3, 4 });
        deleteTestCase(0, new int[] {}, "no indices without flashcards", new int[] {});
        exceptionDeleteTestCase(5, new int[] { 2, 3, 1 }, "IllegalArgumentException", "out of order indices");
        exceptionDeleteTestCase(2, new int[] { 0, 2 }, "IllegalArgumentException", "out of bounds indices");
        exceptionDeleteTestCase(2, new int[] { -1 }, "IllegalArgumentException", "negative index");
        exceptionDeleteTestCase(2, null, "NullPointerException", "null array");
    }

    private static void testEditFlashcard() {
        // null, extra fields, less fields, out of bounds index, incorrect date,
        // incorrect interval
        try {
            editTestCase(2, 1, new String[] { "Front", "Back", "May 9, 2019", "20 days" }, "regular case",
                    new Flashcard[] {
                            new Flashcard("0", "0"),
                            new Flashcard("Front", "Back", "May 9, 2019", "20 days"),
                    });
            editTestCase(1, 0, new String[] { "", "", "May 10, 2010", "1 day" }, "empty flashcard", new Flashcard[] {
                    new Flashcard("", "", "May 10, 2010", "1 day"),
            });
            editTestCase(1, 0, new String[] { "Front", "Back", "February 29 2019", "11 days" }, "illegal date",
                    new Flashcard[] {
                            new Flashcard("0", "0"),
                    });
            editTestCase(1, 0, new String[] { "Front", "Back", "February 29 2019", "11days" }, "illegal interval",
                    new Flashcard[] {
                            new Flashcard("0", "0"),
                    });

            exceptionEditTestCase(1, 0, null, "NullPointerException", "null array");
            exceptionEditTestCase(1, 0, new String[] { null, null, null, null }, "NullPointerException", "null fields");
            exceptionEditTestCase(1, 0, new String[] { "Front", "Back", "May 9 2019", "11 days", "Extra" },
                    "IllegalArgumentException", "extra field");
            exceptionEditTestCase(1, 0, new String[] { "Front", "Back", "May 9 2019" }, "IllegalArgumentException",
                    "missing field");
            exceptionEditTestCase(1, 1, new String[] { "Front", "Back", "May 9 2019", "11 days" },
                    "IllegalArgumentException", "out of bounds index");
            exceptionEditTestCase(1, -1, new String[] { "Front", "Back", "May 9 2019", "11 days" },
                    "IllegalArgumentException", "negative index");
        } catch (DateFormatException | IntervalFormatException e) {
            System.out.println("WARNING: Caught unexpected error:");
            e.printStackTrace();
            return;
        }
    }

    private static void testAddFlashcard() {
        addTestCase(2, new String[] { "Front", "Back" }, "regular case", new Flashcard[] {
                new Flashcard("0", "0"),
                new Flashcard("1", "1"),
                new Flashcard("Front", "Back"),
        });
        addTestCase(0, new String[] { "Front", "Back" }, "initially empty list", new Flashcard[] {
                new Flashcard("Front", "Back"),
        });
        exceptionAddTestCase(1, null, "NullPointerException", "null array");
        exceptionAddTestCase(1, new String[] { null, null }, "NullPointerException", "null fields");
        exceptionAddTestCase(1, new String[] { "Front", "Back", "Extra" }, "IllegalArgumentException", "extra field");
        exceptionAddTestCase(1, new String[] { "Front" }, "IllegalArgumentException", "missing field");
    }

    private static void testStudyFlashcards() {
        new Arion(true).studyFlashcards();
        new Arion(false).studyFlashcards();
    }

    private static void testDisplayException() {
        displayExceptionTestCase(new Exception());
        displayExceptionTestCase(new Exception("message"));
        Test.expectUnhandledException(() -> displayExceptionTestCase(null), "NullPointerException",
                "Calling displayException with null did not throw NullPointerException", "null exception",
                "displayException");
    }

    private static void testQuit() {
        new Arion().quit();
    }

    private static void deleteTestCase(int flashcardCount, int[] indices, String name, int[] expectedIndices) {
        ArrayList<Flashcard> received = runDeleteTestCase(flashcardCount, indices);
        assert received.size() == expectedIndices.length
                : "Expected " + expectedIndices.length + " flashcards, got " + received.size() + ".";

        for (int i = 0; i < expectedIndices.length; i++) {
            int origIdx = Integer.valueOf(received.get(i).front).intValue();
            assert origIdx == expectedIndices[i]
                    : "Expected flashcard at original index " + expectedIndices[i] + ", instead got " + origIdx + ".";
        }

        printSuccessMessage("delete", name);
    }

    private static ArrayList<Flashcard> runDeleteTestCase(int flashcardCount, int[] indices) {
        Arion arion = generateArion(flashcardCount);
        arion.deleteFlashcards(indices);
        return arion.getFlashcards();
    }

    private static void exceptionDeleteTestCase(int flashcardCount, int[] indices, String exception, String name) {
        expectUnhandledException(
                () -> runDeleteTestCase(flashcardCount, indices),
                exception,
                "Deleting " + Arrays.toString(indices) + " did not throw " + exception + ".",
                name,
                "delete");
    }

    private static void editTestCase(int flashcardCount, int index, String[] fields, String name,
            Flashcard[] expectedArray) {
        ArrayList<Flashcard> expected = new ArrayList<>(Arrays.asList(expectedArray));
        ArrayList<Flashcard> flashcards = runEditTestCase(flashcardCount, index, fields);
        assertFlashcardsEqual(flashcards, expected);

        printSuccessMessage("edit", name);
    }

    private static ArrayList<Flashcard> runEditTestCase(int flashcardCount, int index, String[] fields) {
        Arion arion = generateArion(flashcardCount);
        arion.editFlashcard(index, fields);
        return arion.getFlashcards();
    }

    private static void exceptionEditTestCase(int flashcardCount, int index, String[] fields, String exception,
            String name) {
        expectUnhandledException(
                () -> runEditTestCase(flashcardCount, index, fields),
                exception,
                "Editing flashcard at " + index + " with fields " + Arrays.toString(fields) + " did not throw "
                        + exception + ".",
                name,
                "edit");
    }

    private static void addTestCase(int flashcardCount, String[] fields, String name, Flashcard[] expectedArray) {
        ArrayList<Flashcard> expected = new ArrayList<>(Arrays.asList(expectedArray));
        ArrayList<Flashcard> flashcards = runAddTestCase(flashcardCount, fields);
        assertFlashcardsEqual(flashcards, expected);

        printSuccessMessage("add", name);
    }

    private static ArrayList<Flashcard> runAddTestCase(int flashcardCount, String[] fields) {
        Arion arion = generateArion(flashcardCount);
        arion.addFlashcard(fields);
        return arion.getFlashcards();
    }

    private static void exceptionAddTestCase(int flashcardCount, String[] fields, String exception, String name) {
        expectUnhandledException(
                () -> runAddTestCase(flashcardCount, fields),
                exception,
                "Adding flashcard with fields " + Arrays.toString(fields) + " did not throw " + exception + ".",
                name,
                "add");
    }

    private static void displayExceptionTestCase(Exception exception) {
        runDisplayExceptionTestCase(exception, true);
        runDisplayExceptionTestCase(exception, false);
    }

    private static void runDisplayExceptionTestCase(Exception exception, boolean headless) {
        Arion arion = new Arion(headless);
        arion.displayException(exception);
        arion.quit(); // writes the log

        try {
            BufferedReader reader = new BufferedReader(new FileReader("log.txt"));
            String line;
            while ((line = reader.readLine()) != null) System.out.println(line);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static void assertFlashcardsEqual(ArrayList<Flashcard> expectedList, ArrayList<Flashcard> returnedList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Flashcard expected = expectedList.get(i);
            Flashcard returned = returnedList.get(i);
            assert expectedList.get(i).equals(returnedList.get(i))
                    : "Expected " + expected + ", received: " + returned + ".";
        }
    }

    private static Arion generateArion(int flashcardCount) {
        Arion arion = new Arion(true);
        for (int i = 0; i < flashcardCount; i++)
            arion.addFlashcard(new String[] { String.valueOf(i), String.valueOf(i) });
        return arion;
    }

    public static void main(String[] args) {
        //testConstructors();
        
        //testLoadFlashcards();
        
        //testDeleteFlashcards();
        //testEditFlashcard();
        //testAddFlashcard();
        
        testDisplayException();
    }
    */
}
