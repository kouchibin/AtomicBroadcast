import mcgui.*;

public abstract class BasicMulticaster extends Sender {
    int id;
    int hosts;
    public abstract <M extends Message> void cast(M m);

    public abstract void basicreceive(int peer, Message m);

    public abstract void basicpeerdown(int peer);

    public void init() {}
}
