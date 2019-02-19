import mcgui.*;

public abstract class BasicMulticaster<M> extends Sender<M> {
    int id;
    int hosts;
    public abstract void cast(M m);

    public abstract void basicreceive(int peer, Message m);

    public abstract void basicpeerdown(int peer);

    public void init() {}
}
