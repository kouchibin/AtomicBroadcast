import mcgui.*;

public class TargetMulticaster extends Multicaster {
    private Multicaster multicaster;

    /**
     * This is the place to assemble the target multicaster.
     */
    public TargetMulticaster() {
        multicaster = new ReliableMulticaster();
        multicaster = new FIFODecorator(multicaster);
        // multicaster = new CausalDecorator(multicaster);
        // multicaster = new TotalDecorator(multicaster);
    }

    public void init() {
        multicaster.init();
    }

    public void cast(String messagetext) {
        multicaster.cast(messagetext);
    }

    public void basicreceive(int peer, Message message) {
        System.out.println("basicreceive called");
        multicaster.basicreceive(peer, message);
    }

    public void basicpeerdown(int peer) {
        multicaster.basicpeerdown(peer);
    }

    public void setId(int id, int host) {
        multicaster.setId(id, host);
    }

    public void setCommunicator(BasicCommunicator bcom) {
        multicaster.setCommunicator(bcom);
    }

    public void setUI(MulticasterUI mcui) {
        multicaster.setUI(mcui);
    }

    public void enableUI() {
        multicaster.enableUI();
    }
}
