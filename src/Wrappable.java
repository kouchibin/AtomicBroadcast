import mcgui.*;

public interface Wrappable {
    Multicaster upperLayer = null;

    default void setUpperLayer(Multicaster upperLayer) {
        this.upperLayer = upperLayer;
    }
}
