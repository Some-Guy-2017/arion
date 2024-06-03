package unit_test;

import arion.*;
import exception.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.*;

public class DatabaseTests {

    static Test[] tests = new Test[] {
            new ConstructorTest("constructor-test"),
            new ConstructorTest("does not exist"),
            new ConstructorTest(""),
            new ConstructorTest(null).withException("NullPointerException"),
            IOTestCase.readWriteCase("Regular Case", "regular-case", new Flashcard[] {
                    new Flashcard("Front", "Back", -137989, 3),
                    new Flashcard("Flashcards are", "useful for learning", -70672, 12),
            }),
            IOTestCase.readWriteCase("Empty Flashcards", "empty-flashcards", new Flashcard[] {
                    new Flashcard("", "", 0, 0),
                    new Flashcard("", "", 0, 0),
            }),
            IOTestCase.readWriteCase("No Flashcards", "no-flashcards", new Flashcard[] {}),
            IOTestCase.readWriteCase("Leap Year", "leap-year", new Flashcard[] {
                    new Flashcard("Front", "Back", 18321, 9),
            }),
            IOTestCase.readExceptionCase("Does Not Exist", "does not exist", "DatabaseReadException"),
            IOTestCase.readExceptionCase("Short File", "short", "DatabaseFormatException"),
            IOTestCase.readExceptionCase("Long File", "long", "DatabaseFormatException"),
            IOTestCase.readExceptionCase("Database Without Header", "no-header", "NumberFormatException"),
            IOTestCase.readExceptionCase("Float Header", "float-header", "NumberFormatException"),
            IOTestCase.readExceptionCase("Illegal Date", "illegal-date", "DatabaseFormatException"),
            IOTestCase.readExceptionCase("Invalid Fate", "invalid-date", "DatabaseFormatException"),
            IOTestCase.readExceptionCase("Invalid Interval", "invalid-interval", "DatabaseFormatException"),
            IOTestCase.readExceptionCase("Does Not Exist", "does not exist", "FileNotFoundException"),
            IOTestCase.writeExceptionCase("Null Parameter", null, "NullPointerException"),
    };

    public static void evaluate() {
        for (Test test : tests) {
            test.evaluate();
        }
    }

    public static void main(String[] args) {
        evaluate();
    }
}

class IOTestCase extends Test {
    String filepath;
    static String writeFilepath = "./test-files/write.txt";

    ArrayList<Flashcard> flashcards;

    boolean readCase;
    boolean writeCase;

    IOTestCase(String name, String filename, Flashcard[] flashcards, boolean readCase, boolean writeCase) {
        this.name = name;
        this.filepath = "./test-files/" + filename + ".txt";
        this.readCase = readCase;
        this.writeCase = writeCase;
        
        if (flashcards == null) this.flashcards = null;
        else this.flashcards = new ArrayList<>(Arrays.asList(flashcards));
    }

    static IOTestCase readWriteCase(String name, String filename, Flashcard[] flashcards) {
        return new IOTestCase(name, filename, flashcards, true, true);
    }

    static IOTestCase readExceptionCase(String name, String filename, String exception) {
        return (IOTestCase) new IOTestCase(name, filename, null, true, false).withException(exception);
    }

    static IOTestCase writeExceptionCase(String name, Flashcard[] flashcards, String exception) {
        return (IOTestCase) new IOTestCase(name, null, flashcards, false, true).withException(exception);
    }

    public void execute() throws DatabaseFormatException, IOException, DatabaseReadException, DatabaseWriteException {
        if (readCase) runReadTestCase();
        if (writeCase) runWriteTestCase();
    }

    void runReadTestCase() throws DatabaseFormatException, IOException, DatabaseReadException {
        Database database = new Database(filepath);
        ArrayList<Flashcard> readFlashcards;
        readFlashcards = database.readFlashcards();

        if (flashcards == null) return;
        
        assert readFlashcards.size() == flashcards.size()
                : "read " + readFlashcards.size() + " flashcards instead of " + flashcards.size() + ".";
        for (int i = 0; i < readFlashcards.size(); i++) {
            Flashcard correct = flashcards.get(i);
            Flashcard read = readFlashcards.get(i);

            StringBuilder errorMessage = new StringBuilder(
                    "Incorrectly read " + filepath + " at flashcard " + i + ":");
            errorMessage.append("\nCorrect: " + correct);
            errorMessage.append("\nRead: " + read);
            assert correct.equals(read) : errorMessage.toString();
        }

    }

    void runWriteTestCase() throws DatabaseFormatException, IOException, DatabaseWriteException, FileNotFoundException {
        new Database(writeFilepath).writeFlashcards(flashcards);

        BufferedReader expected = new BufferedReader(new FileReader(filepath));
        BufferedReader written = new BufferedReader(new FileReader(writeFilepath));
        String expectedLine = null;
        String writtenLine = null;

        while (expected.ready() && written.ready()) {
            expectedLine = expected.readLine();
            writtenLine = written.readLine();
            assert expectedLine.equals(writtenLine) : "Expected output from " + filepath + " does not match actual output";
        }
        expectedLine = expected.readLine();
        assert expectedLine == null : "Content written from test case " + filepath + " had at least one extra line: " + expectedLine;
        writtenLine = written.readLine();
        assert writtenLine == null : "Content written from test case " + filepath + " had at least one extra line: " + writtenLine;
    }

    private static <T> String arrayListToString(ArrayList<T> list) {
        if (list == null) return "null";
        StringBuilder str = new StringBuilder("[");
        int end = list.size()-1;

        for (int i = 0; i <= end; i++) {
            str.append(list.get(i).toString());
            if (i != end) str.append(", ");
        }

        str.append("]");
        return str.toString();
    }
}

class ConstructorTest extends Test {
    String filename;

    public ConstructorTest(String filename) {
        this.filename = filename;
    }
    
    public void execute() {
        new Database("./test-files/" + filename + ".txt");
    }
}
