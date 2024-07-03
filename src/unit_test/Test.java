package unit_test;

import java.util.Optional;
import java.util.ArrayList;
import arion.*;

public abstract class Test {
    String qualifiedName;
    Optional<String> exceptionOption;

    public Test(String testType, String testName) {
        qualifiedName = "[" + testType + " ~ " + testName + "]";
        exceptionOption = Optional.empty();
    }

    public void evaluate() {
        try {
            System.out.println("Executing " + qualifiedName + "...");
            execute();
            assert exceptionOption.isEmpty()
                    : "Executing " + qualifiedName + " did not throw " + exceptionOption.get() + ".";
        } catch (Exception e) {
            if (exceptionOption.isEmpty()
                    || !e.getClass().getName().equals(exceptionOption.get())) {
                e.printStackTrace();
                assert false;
            }
        }
        System.out.println("Successfully ran " + qualifiedName);
    }

    public Test withException(String exception) {
        this.exceptionOption = Optional.of(exception);
        return this;
    }

    public static void evaluateTests(Test[] tests) {
        for (Test test : tests)
            test.evaluate();
    }

    public static void validateFlashcards(ArrayList<Flashcard> expectedList, ArrayList<Flashcard> receivedList) {
        assert expectedList.size() == receivedList.size()
                : "received " + expectedList.size() + " flashcards instead of " + receivedList.size() + ".";
        int len = expectedList.size();

        for (int i = 0; i < len; i++) {
            Flashcard correct = expectedList.get(i);
            Flashcard received = receivedList.get(i);

            StringBuilder errorMessage = new StringBuilder(
                    "Incorrect flashcard at flashcard " + i + ":");
            errorMessage.append("\nCorrect: " + correct);
            errorMessage.append("\nReceived: " + received);
            assert correct.equals(received) : errorMessage.toString();
        }
    }

    abstract void execute() throws Exception;
}
