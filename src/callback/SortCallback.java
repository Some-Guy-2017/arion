package callback;
import arion.Flashcard;

public interface SortCallback
{
    public void run(Flashcard.Field field, boolean reversed);
}
