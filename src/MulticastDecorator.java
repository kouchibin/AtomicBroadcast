import mcgui.*;

public class MulticastDecorator extends Multicaster {
    protected Multicaster multicaster;

    public void init() {}

    public void cast(String messagetext) {
        multicaster.cast(messagetext);
    }

    public void cast(Message m) {
        if (multicaster instanceof MulticastDecorator)
            ((MulticastDecorator)multicaster).cast(m);
        else if (multicaster instanceof ReliableMulticaster)
            ((ReliableMulticaster)multicaster).cast(m);
        else
            throw new RuntimeException("multicaster doesn't have cast(Message) method.");
    }

    public void basicreceive(int peer, Message message) {
        multicaster.basicreceive(peer, message);
    }

    public void basicpeerdown(int peer) {
        multicaster.basicpeerdown(peer);
    }
}
