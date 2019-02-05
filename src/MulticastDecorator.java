import mcgui.*;

public class MulticastDecorator extends Multicaster {
    protected Multicaster multicaster;
    protected MulticastDecorator upperLayer;

    public void init() {
        multicaster.init();
    }

    public void cast(String messagetext) {
        multicaster.cast(messagetext);
    }

    public void setUpperLayer(MulticastDecorator upperLayer) {
        this.upperLayer = upperLayer;
    }

    public void cast(Message m) {
        if (multicaster instanceof MulticastDecorator)
            ((MulticastDecorator)multicaster).cast(m);
        else if (multicaster instanceof ReliableMulticaster)
            ((ReliableMulticaster)multicaster).cast(m);
        else
            throw new RuntimeException("multicaster doesn't have cast(Message) method.");
    }

    public void deliver(Message m) {

    }

    public void basicreceive(int peer, Message message) {
        System.out.println("Receive msg from " + peer + " : " + message);
        multicaster.basicreceive(peer, message);
    }

    public void basicpeerdown(int peer) {
        multicaster.basicpeerdown(peer);
    }

    public void setId(int id, int host) {
        super.setId(id, host);
        multicaster.setId(id, host);
    }

    public void setCommunicator(BasicCommunicator bcom) {
        multicaster.setCommunicator(bcom);
    }

    public void setUI(MulticasterUI mcui) {
        super.setUI(mcui);
        multicaster.setUI(mcui);
    }

    public void enableUI() {
        multicaster.enableUI();
    }
}
