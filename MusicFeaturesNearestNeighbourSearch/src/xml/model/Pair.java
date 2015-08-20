package xml.model;

/**
 * Created by ekaterina on 05.07.2015.
 */
public class Pair<T1, T2> {
    public T1 firstArg;
    public T2 secondArg;

    public Pair(T1 first, T2 second) {
        this.firstArg = first;
        this.secondArg = second;
    }

    public String toString() {
        return firstArg.toString() + ", " + secondArg.toString();
    }
}
