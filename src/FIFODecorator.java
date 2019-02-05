
import mcgui.*;

import java.util.*;

public class FIFODecorator extends MulticastDecorator {

    private List<Integer> next;
    private List<Set<MulticastMessage>> undelivered_msgs;
    private int sequence;

    public FIFODecorator(Multicaster mt) {
        if (mt instanceof ReliableMulticaster) {
            ((ReliableMulticaster)mt).upperLayer = this;
        } else {
            throw new RuntimeException("Illegal Combination.");
        }
        multicaster = mt;
        sequence = 1;
        undelivered_msgs = new ArrayList<>();
    }

    @Override
    public void init() {
        next = new ArrayList<Integer>();
        for (int i = 0; i < hosts; i++) {
            next.add(1);
            undelivered_msgs.add(new HashSet<MulticastMessage>());
        }
        multicaster.init();
    }

    @Override
    public void cast(String messagetext) {
        Message message = new MulticastMessage(id, sequence++, messagetext);
        cast(message);
    }

    @Override
    public void deliver(Message m) {
        assert (m instanceof MulticastMessage);
        MulticastMessage mm = (MulticastMessage)m;
        int sender = mm.getSender();
        Set<MulticastMessage> undelivered_msgs_in_channel = undelivered_msgs.get(sender);
        undelivered_msgs_in_channel.add(mm);
        deliverAllDeliverable(undelivered_msgs_in_channel, sender);
    }

    private void deliverAllDeliverable(Set<MulticastMessage> undelivered_msgs_in_channel, int sender) {
        while (true) {
            boolean found = false;
            for (MulticastMessage msg : undelivered_msgs_in_channel) {
                if (msg.sequence == next.get(sender)) {
                    found = true;
                    if (upperLayer == null)
                        mcui.deliver(sender, msg.text);
                    else
                        upperLayer.deliver(msg);
                    undelivered_msgs_in_channel.remove(msg);
                    next.set(sender, next.get(sender)+1);
                    break;
                }
            }
            if (!found)
                break;
        }
    }
}
