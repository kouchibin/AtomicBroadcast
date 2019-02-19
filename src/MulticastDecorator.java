import mcgui.*;

public abstract class MulticastDecorator<M> extends EnhancedMulticaster<M> {
    protected Multicaster multicaster;

    public void init() {
        multicaster.init();
    }

    public abstract void cast(M message);

    public abstract void deliver(M m);

    @Override
    public void basicreceive(int peer, Message m) {
        multicaster.basicreceive(peer, m);
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
