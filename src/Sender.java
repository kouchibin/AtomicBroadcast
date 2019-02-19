public class Sender<M> {

    protected Receiver<M> upperLayer;

    public void setUpperLayer(Receiver<M> upperLayer) {
        this.upperLayer = upperLayer;
    }
}
