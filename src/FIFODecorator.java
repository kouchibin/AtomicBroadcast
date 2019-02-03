
import mcgui.*;

import java.util.*;

public class FIFODecorator extends MulticastDecorator {

    private List<Integer> next;
    private int sequence;

    public FIFODecorator(Multicaster mt) {
        multicaster = mt;
        sequence = 1;

        next = new ArrayList<Integer>();
        for (int i = 0; i < hosts; i++) {
            next.add(1);
        }
    }

    public void cast(String messagetext) {
        Message message = new MulticastMessage(id, sequence++, messagetext);
        cast(message);
    }

    public void basicreceive(int peer, Message message) {
        multicaster.basicreceive(peer, message);
    }

    public void basicpeerdown(int peer) {
        multicaster.basicpeerdown(peer);
    }


}
