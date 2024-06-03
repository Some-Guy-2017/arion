package unit_test;

public abstract class Test {
    String name;
    String exception;
    
    public void evaluate() {
        try {
            execute();
            assert exception == null : "Executing " + name + " did not throw " + exception + ".";
        } catch (Exception e) {
            if (!e.getClass().getSimpleName().equals(exception)) {
                e.printStackTrace();
                assert false;
            }
        }
    }

    public Test withException(String exception) {
        this.exception = exception;
        return this;
    }

    abstract void execute() throws Exception;

    public static void printSuccessMessage(String method, String name) {
        System.out.println("Successful " + method + " test: " + name + ".");
    }
    
}
