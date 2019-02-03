
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class MulticastMessage extends Message {

    public static final long serialVersionUID = 0;

    public final int sequence;
    public final String text;
    //public final String hash;

    public MulticastMessage(int sender, int sequence, String text) {
        super(sender);
        this.sequence = sequence;
        this.text = text;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + sender;
        hash = 31 * hash + sequence;
        hash = 31 * hash + (text == null ? 0 : text.hashCode());
        return hash;
    }

}
