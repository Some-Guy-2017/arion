package arion;
import java.io.*;
import java.util.ArrayList;

public class Database
{
    private File file;
    private String filename;

    Database(String filename)
    {
        this.filename = filename;
        this.file = new File(filename);
    }

    public boolean canRead()
    {
        return file.canRead();
    }

    public ArrayList<Flashcard> readFlashcards() throws IOException, NumberFormatException, DatabaseFormatException
    {
        System.out.println("Reading flashcards.");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int flashcardCount = Integer.valueOf(reader.readLine()).intValue();
        ArrayList<Flashcard> flashcards = new ArrayList<>(flashcardCount);

        for (int i = 0; i < flashcardCount; i++)
        {
            String[] fields = new String[Flashcard.FIELD_COUNT];
            for (int j = 0; j < Flashcard.FIELD_COUNT; j++)
                fields[j] = reader.readLine();

            if (fields[Flashcard.FIELD_COUNT-1] == null) throw new DatabaseFormatException("Database file is too short.");

            flashcards.add(Flashcard.fromStringArray(fields));
        }

        if (reader.readLine() != null) throw new DatabaseFormatException("Database file is too long.");

        return flashcards;
    }

    public void writeFlashcards(ArrayList<Flashcard> flashcards) throws IOException
    {
        System.out.println("Writing flashcards.");
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println(flashcards.size()); // header is entry count
        
        for (Flashcard flashcard: flashcards)
        for (String field: flashcard.toStringArray())
            writer.println(field);

        writer.close();
    }

    public class DatabaseFormatException extends Exception
    {
        DatabaseFormatException() {}
        DatabaseFormatException(String message) { super(message); }
    }
        
}

