
import mcgui.*;

import java.util.*;

public class FIFODecorator extends BasicMulticaster implements Receiver {

    private BasicMulticaster multicaster;
    private List<Integer> next;
    private List<Set<FIFOMessage>> undelivered_msgs;
    private int sequence;

    public FIFODecorator(BasicMulticaster mt) {
        mt.setUpperLayer(this);
        multicaster = mt;
        hosts = mt.hosts;
        id = mt.id;
        init();
        System.out.println("In FIFO:" + multicaster.getClass().getName());
    }

    @Override
    public void init() {
        sequence = 1;
        undelivered_msgs = new ArrayList<>();
        next = new ArrayList<Integer>();
        for (int i = 0; i < hosts; i++) {
            next.add(1);
            undelivered_msgs.add(new HashSet<FIFOMessage>());
        }
        multicaster.init();
    }

    @Override
    public <M extends Message> void cast(M m) {
        System.out.println("In FIFO:" + m.getClass().getName());
        FIFOMessage<M> message = new FIFOMessage<M>(sequence++, m);
        multicaster.cast(message);
    }

    @Override
    public void basicpeerdown(int peer) {
        multicaster.basicpeerdown(peer);
    }

    @Override
    public void basicreceive(int peer, Message message) {
        System.out.println("basicreceive called");
        multicaster.basicreceive(peer, message);
    }

    @Override
    public <M extends Message> void deliver(M m) {
        FIFOMessage fifoMsg = (FIFOMessage)m;
        int sender = fifoMsg.getSender();
        Set<FIFOMessage> undelivered_msgs_in_channel = undelivered_msgs.get(sender);
        undelivered_msgs_in_channel.add(fifoMsg);
        deliverAllDeliverable(undelivered_msgs_in_channel, sender);
    }

    private void deliverAllDeliverable(Set<FIFOMessage> undelivered_msgs_in_channel, int sender) {
        while (true) {
            boolean found = false;
            for (FIFOMessage msg : undelivered_msgs_in_channel) {
                if (msg.seq == next.get(sender)) {
                    found = true;
                    upperLayer.deliver(msg.message);
                    System.out.println("FIFO delivering to upperLayer.");
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

class FIFOMessage<T extends Message> extends Message {
    public final int seq;
    public final T message;

    public FIFOMessage(int seq, T message) {
        super(message.getSender());
        this.seq = seq;
        this.message = message;
    }

    public int hashCode() {
        int hash = 7;
        hash = hash * 37 + seq;
        hash = hash * 37 + message.hashCode();
        return hash;
    }
}
