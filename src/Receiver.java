public interface Receiver<M> {
    public abstract void deliver(M m);
}
