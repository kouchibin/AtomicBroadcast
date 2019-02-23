import mcgui.*;

public class TargetMulticaster extends Multicaster implements Receiver{
    private BasicMulticaster multicaster;

    /**
     * This is the place to assemble the target multicaster.
     */
    public TargetMulticaster() {}

    @Override
    public void init() {
        multicaster = new ReliableMulticaster(bcom, id, hosts);
        multicaster = new TotalDecorator(multicaster, bcom);
        // multicaster = new FIFODecorator(multicaster);
        // multicaster = new CausalDecorator(multicaster);
        multicaster.setUpperLayer(this);
    }

    @Override
    public void cast(String messagetext) {
        TargetMessage targetMsg = new TargetMessage(id, messagetext);
        multicaster.cast(targetMsg);
    }

    @Override
    public <M extends Message> void deliver(M m) {
        TargetMessage msg = (TargetMessage) m;
        mcui.deliver(msg.getSender(), msg.text);
    }

    @Override
    public void basicreceive(int peer, Message message) {
        multicaster.basicreceive(peer, message);
    }

    @Override
    public void basicpeerdown(int peer) {
        System.out.println("In TargetMulticaster, basicpeerdown called.");
        multicaster.basicpeerdown(peer);
    }

}

class TargetMessage extends Message {

    private static int counter = 0;

    public final String text;
    public final int seq;
    public TargetMessage(int sender, String text) {
        super(sender);
        this.text = text;
        seq = counter++;
    }

    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + sender;
        hash = 37 * hash + seq;
        hash = 37 * hash + text.hashCode();
        return hash;
    }
}
