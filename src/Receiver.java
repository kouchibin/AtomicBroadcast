import mcgui.*;

public interface Receiver {
    public abstract <M extends Message> void deliver(M m);
}
