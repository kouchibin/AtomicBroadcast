
import mcgui.*;

import java.util.*;
import java.util.concurrent.*;

public class ReliableMulticaster extends Multicaster {

    Set<Integer> deliveredMessagesHash = new HashSet<>();
    Multicaster upperLayer = null;
    int sequenceNumber = 0;

    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        // Intercept to run tryToBreakFIFO() to emulate faults.
        if (messagetext.equals("break")) {
            tryToBreakFIFO();
            return;
        }

        sendToAll(new MulticastMessage(id, sequenceNumber++, messagetext));
        mcui.debug("Sent out: \""+messagetext+"\"");
    }

    public void tryToBreakFIFO() {
        List<MulticastMessage> msgs = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            msgs.add(new MulticastMessage(id, sequenceNumber, "" + id + "-" + sequenceNumber));
            sequenceNumber++;
        }
        Collections.shuffle(msgs);
        for (MulticastMessage msg : msgs)
            sendToAll(msg);
    }

    public void cast(Message message) {
        if ((message instanceof MulticastMessage) &&
            ((MulticastMessage)message).text.equals("break")) {
            tryToBreakFIFO();
            return;
        }
        sendToAll(message);
        System.out.println("Sent out MulticastMessage: \""+message+"\"");
    }

    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {

        if (deliveredMessagesHash.add(message.hashCode())) {
            if (message.getSender() != id) {
                sendToAll(message);
            }
            if (upperLayer == null) {
                assert (message instanceof MulticastMessage);
                mcui.deliver(peer, ((MulticastMessage)message).text);
            }
            else{
                mcui.debug("delivering to upperLayer.");
                ((MulticastDecorator)upperLayer).deliver(message);
            }
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
