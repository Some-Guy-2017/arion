package arion;

import exception.*;

import java.io.*;
import java.util.ArrayList;

/*
 * The Database class allows reading from and writing to a database file containing flashcards.
 */

public class Database {
    private File file;
    public String filepath;

    /*
     * The constructor initializes a new Database class.
     *
     * Input: path of the file to read from and write to.
     * Output: new Database class.
     */
    public Database(String filepath) {
        if (filepath == null) {
            throw new NullPointerException("Null file name in Database constructor.");
        }

        this.filepath = filepath;
        this.file = new File(filepath);
    }

    /*
     * readFlashcards parses the flashcards from this database's file.
     *
     * Input: no input.
     * Output: ArrayList containing the flashcards in the database file.
     */
    public ArrayList<Flashcard> readFlashcards()
            throws IOException, NumberFormatException, DatabaseFormatException, DatabaseReadException {
        if (!file.canRead()) {
            throw new DatabaseReadException("Cannot read " + filepath);
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        int flashcardCount = Integer.valueOf(reader.readLine()).intValue();
        ArrayList<Flashcard> flashcards = new ArrayList<>(flashcardCount);

        for (int i = 0; i < flashcardCount; i++) {
            String[] fields = new String[Flashcard.FIELD_COUNT];
            for (int j = 0; j < Flashcard.FIELD_COUNT; j++) {
                fields[j] = reader.readLine();
            }

            // check that none of the fields are null
            if (fields[Flashcard.FIELD_COUNT - 1] == null) {
                throw new DatabaseFormatException("Database file is too short.");
            }

            try {
                flashcards.add(Flashcard.fromStringArray(fields));
            } catch (DateFormatException | IntervalFormatException e) {
                throw new DatabaseFormatException("Database has incorrectly formatted flashcards\n" + e.getMessage());
            }
        }

        if (reader.readLine() != null) {
            throw new DatabaseFormatException("Database file is too long.");
        }

        return flashcards;
    }

    /*
     * writeFlashcards writes the flashcards ArrayList to this database's file.
     *
     * Input: an ArrayList of flashcards to write.
     * Output: no return value, writes to this database's file.
     */
    public void writeFlashcards(ArrayList<Flashcard> flashcards) throws IOException, DatabaseWriteException {
        
        if (!file.canWrite()) {
            throw new DatabaseWriteException("Cannot write to " + filepath);
        }
        if (flashcards == null) {
            throw new NullPointerException("Null Flashcard Array");
        }

        PrintWriter writer = new PrintWriter(file);
        writer.println(flashcards.size()); // header is entry count

        for (Flashcard flashcard : flashcards) {
            for (String field : flashcard.toStringArray()) {
                writer.println(field);
            }
        }

        writer.close();
    }
}
