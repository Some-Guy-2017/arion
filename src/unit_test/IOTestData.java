package unit_test;

import arion.*;
import exception.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import java.time.*;

public class IOTestData {
    public static String WRITE_FILEPATH = "./test-files/write.txt";

    public static IOTestData[] testData = {
            IOTestData.readWriteCase("Regular Case", "regular-case", new Flashcard[] {
                    new Flashcard("Front", "Back", -137989, 3),
                    new Flashcard("Flashcards are", "useful for learning", -70672, 12),
            }),
            IOTestData.readWriteCase("Empty Flashcards", "empty-flashcards", new Flashcard[] {
                    new Flashcard("", "", 0, 1),
                    new Flashcard("", "", 0, 1),
            }),
            IOTestData.readWriteCase("No Flashcards", "no-flashcards", new Flashcard[] {}),
            IOTestData.readWriteCase("Leap Year", "leap-year", new Flashcard[] {
                    new Flashcard("Front", "Back", 18321, 9),
            }),
            IOTestData.readExceptionCase("Does Not Exist", "does not exist", "exception.DatabaseReadException"),
            IOTestData.readExceptionCase("Short File", "short", "exception.DatabaseFormatException"),
            IOTestData.readExceptionCase("Long File", "long", "exception.DatabaseFormatException"),
            IOTestData.readExceptionCase("Database Without Header", "no-header", "java.lang.NumberFormatException"),
            IOTestData.readExceptionCase("Float Header", "float-header", "java.lang.NumberFormatException"),
            IOTestData.readExceptionCase("Illegal Date", "illegal-date", "exception.DatabaseFormatException"),
            IOTestData.readExceptionCase("Invalid Fate", "invalid-date", "exception.DatabaseFormatException"),
            IOTestData.readExceptionCase("Invalid Interval", "invalid-interval", "exception.DatabaseFormatException"),
            IOTestData.writeExceptionCase("Null Parameter", null, "java.lang.NullPointerException"),
    };

    public String testName;
    public String filepath;
    public String filename;
    public ArrayList<Flashcard> flashcards;

    public boolean readCase;
    public boolean writeCase;

    public Optional<String> exceptionOption = Optional.empty();

    IOTestData(String testName, String filename, Flashcard[] flashcards, boolean readCase, boolean writeCase) {
        this.testName = testName;
        this.filename = filename;
        this.filepath = "./test-files/" + filename + ".txt";
        this.readCase = readCase;
        this.writeCase = writeCase;

        this.flashcards = ArionUtils.toArrayList(flashcards);
    }

    static IOTestData readWriteCase(String name, String filename, Flashcard[] flashcards) {
        return new IOTestData(name, filename, flashcards, true, true);
    }

    static IOTestData readExceptionCase(String name, String filename, String exception) {
        return new IOTestData(name, filename, null, true, false).withException(exception);
    }

    static IOTestData writeExceptionCase(String name, Flashcard[] flashcards, String exception) {
        return new IOTestData(name, null, flashcards, false, true).withException(exception);
    }

    IOTestData withException(String exception) {
        exceptionOption = Optional.of(exception);
        return this;
    }

    public void validateFlashcards(ArrayList<Flashcard> received) throws FileNotFoundException {
        Test.validateFlashcards(flashcards, received);
    }

    public void validateWrittenFile() throws FileNotFoundException, IOException {
        BufferedReader expected = new BufferedReader(new FileReader(filepath));
        BufferedReader written = new BufferedReader(new FileReader(WRITE_FILEPATH));
        String expectedLine = null;
        String writtenLine = null;

        int line = 1;
        while (expected.ready() && written.ready()) {
            expectedLine = expected.readLine();
            writtenLine = written.readLine();

            String assertMessage = String.format("On line %d, expected %s but received %s.",
                    line, expectedLine, writtenLine);

            assert expectedLine.equals(writtenLine) : assertMessage;
            line++;
        }
        expectedLine = expected.readLine();
        assert expectedLine == null
                : "Content written from test case " + filepath + " had at least one extra line: " + expectedLine;
        writtenLine = written.readLine();
        assert writtenLine == null
                : "Content written from test case " + filepath + " had at least one extra line: " + writtenLine;
    }

    public IOTestData clone() {
        Flashcard[] flashcardArray = flashcards == null ? null : flashcards.toArray(new Flashcard[0]);
        IOTestData testData = new IOTestData(testName, filename, flashcardArray, readCase, writeCase);
        if (exceptionOption.isPresent()) {
            testData = testData.withException(exceptionOption.get());
        }
        return testData;
    }
}
