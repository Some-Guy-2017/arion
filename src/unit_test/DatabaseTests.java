package unit_test;

import arion.*;
import exception.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.*;

public class DatabaseTests {

    static Test[] tests = generateTests();

    private static Test[] generateTests() {
        Test[] tests = {
                new DatabaseConstructorTest("Regular Case", "./test-files/constructor-test.txt"),
                new DatabaseConstructorTest("File does not exist", "does not exist"),
                new DatabaseConstructorTest("No file", ""),
                new DatabaseConstructorTest("Null file", null).withException("java.lang.NullPointerException"),
        };
        ArrayList<Test> testList = ArionUtils.toArrayList(tests);
        for (IOTestData testData : IOTestData.testData) {
            testList.add(new DatabaseIOTest(testData));
        }

        return testList.toArray(new Test[0]);
    }

    public static void main(String[] args) {
        Test.evaluateTests(tests);
    }
}

class DatabaseConstructorTest extends Test {
    String filename;

    public DatabaseConstructorTest(String testName, String filename) {
        super("Database Constructor Test", testName);
        this.filename = filename;
    }

    public void execute() {
        new Database(filename);
    }
}

class DatabaseIOTest extends Test {
    IOTestData testData;

    public DatabaseIOTest(IOTestData testData) {
        super("Database Test", testData.testName);
        this.testData = testData;
        this.exceptionOption = testData.exceptionOption;
    }

    void execute() throws DatabaseFormatException, IOException, DatabaseReadException,
            DatabaseFormatException, IOException, DatabaseWriteException, FileNotFoundException {
        if (testData.readCase) {
            executeReadCase();
        }
        if (testData.writeCase) {
            executeWriteCase();
        }
    }

    void executeReadCase() throws DatabaseFormatException, IOException, DatabaseReadException {
        Database database = new Database(testData.filepath);
        ArrayList<Flashcard> flashcards = database.readFlashcards();
        testData.validateFlashcards(flashcards);
    }

    void executeWriteCase() throws DatabaseFormatException, IOException, DatabaseWriteException, FileNotFoundException {
        new Database(IOTestData.WRITE_FILEPATH).writeFlashcards(testData.flashcards);
        testData.validateWrittenFile();
    }
}
