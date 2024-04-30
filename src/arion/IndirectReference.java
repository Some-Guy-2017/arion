package arion;

public class IndirectReference<T>
{
    private T value;

    public IndirectReference(T value)
    {
        this.value = value;
    }

    public IndirectReference() {}

    public void setValue(T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return this.value;
    }
}
