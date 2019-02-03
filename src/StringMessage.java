
import mcgui.*;

public abstract class StringMessage extends Message {
    public final String text;

    public StringMessage(int sender, String text) {
        super(sender);
        this.text = text;
    }
}