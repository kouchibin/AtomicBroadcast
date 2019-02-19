import mcgui.*;

public class TargetMulticaster extends Multicaster implements Receiver<TargetMessage>{
    private BasicMulticaster<TargetMessage> multicaster;

    /**
     * This is the place to assemble the target multicaster.
     */
    public TargetMulticaster() {

        //multicaster = new FIFODecorator<TargetMessage>(multicaster);
        // multicaster = new CausalDecorator<FIFOMessage>(multicaster);
        // multicaster = new TotalDecorator(multicaster);
    }

    @Override
    public void init() {
        multicaster = new ReliableMulticaster<>(bcom, id, hosts);
        multicaster = new FIFODecorator<>(multicaster);
        multicaster = new CausalDecorator<>(multicaster);
        multicaster.setUpperLayer(this);
    }

    @Override
    public void cast(String messagetext) {
        TargetMessage targetMsg = new TargetMessage(id, messagetext);
        multicaster.cast(targetMsg);
    }

    @Override
    public void deliver(TargetMessage m) {
        mcui.deliver(m.getSender(), m.text);
    }

    public void basicreceive(int peer, Message message) {
        System.out.println("basicreceive called");
        multicaster.basicreceive(peer, message);
    }

    public void basicpeerdown(int peer) {
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
