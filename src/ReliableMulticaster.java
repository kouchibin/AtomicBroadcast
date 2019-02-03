
import mcgui.*;

import java.util.*;

public class ReliableMulticaster extends Multicaster {

    Set<Integer> deliveredMessagesHash = new HashSet<>();
    int sequenceNumber = 0;

    /**
     *
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        sendToAll(new MulticastMessage(id, sequenceNumber++, messagetext));
        mcui.debug("Sent out: \""+messagetext+"\"");
    }

    public void cast(Message message) {
        sendToAll(message);
    }

    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message m) {
        MulticastMessage message = (MulticastMessage)m;

        if (deliveredMessagesHash.add(message.hashCode())) {
            if (message.getSender() != id) {
                sendToAll(message);
            }
            mcui.deliver(peer, message.text);
        }

    }

    public void sendToAll(Message message) {
        for(int i=0; i < hosts; i++) {
            bcom.basicsend(i, message);
        }
    }

    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer "+peer+" has been dead for a while now!");
    }
}
