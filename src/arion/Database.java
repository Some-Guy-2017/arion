package arion;

import exception.*;

import java.io.*;
import java.util.ArrayList;

public class Database {
    private File file;
    public String filename;

    public Database(String filename) {
        if (filename == null) throw new NullPointerException("Null file name in Database constructor.");
        
        this.filename = filename;
        this.file = new File(filename);
    }


    public ArrayList<Flashcard> readFlashcards() throws IOException, NumberFormatException, DatabaseFormatException, DatabaseReadException {
        if (!file.canRead()) throw new DatabaseReadException("Cannot read " + filename);
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int flashcardCount = Integer.valueOf(reader.readLine()).intValue();
        ArrayList<Flashcard> flashcards = new ArrayList<>(flashcardCount);

        for (int i = 0; i < flashcardCount; i++) {
            String[] fields = new String[Flashcard.FIELD_COUNT];
            for (int j = 0; j < Flashcard.FIELD_COUNT; j++) fields[j] = reader.readLine();

            if (fields[Flashcard.FIELD_COUNT-1] == null)
                throw new DatabaseFormatException("Database file is too short.");

            try {
                flashcards.add(Flashcard.fromStringArray(fields));
            }
            catch (DateFormatException|IntervalFormatException e) {
                throw new DatabaseFormatException("Database has incorrectly formatted flashcards.");
            }
        }

        if (reader.readLine() != null) throw new DatabaseFormatException("Database file is too long.");

        return flashcards;
    }

    public void writeFlashcards(ArrayList<Flashcard> flashcards) throws IOException, DatabaseWriteException {
        if (!file.canWrite()) throw new DatabaseWriteException("Cannot write to " + filename);
        if (flashcards == null) throw new NullPointerException("Null Flashcard Array");
        
        PrintWriter writer = new PrintWriter(file);
        writer.println(flashcards.size()); // header is entry count
        
        for (Flashcard flashcard: flashcards)
        for (String field: flashcard.toStringArray())
            writer.println(field);

        writer.close();
    }
}

