
import mcgui.*;

import java.util.*;
import java.util.concurrent.*;

public class ReliableMulticaster<M extends Message> extends BasicMulticaster<M> {

    BasicCommunicator bcom;
    Set<Integer> deliveredMessagesHash = new HashSet<>();
    int sequenceNumber = 0;

    public ReliableMulticaster(BasicCommunicator bcom, int id, int hosts) {
        this.bcom = bcom;
        this.id = id;
        this.hosts = hosts;
    }

    @Override
    public void cast(M message) {
        System.out.println("In Reliable:" + message.getClass().getName());
        sendToAll(new ReliableMessage<M>(message));
    }

    @Override
    public void basicreceive(int peer, Message message) {
        if (deliveredMessagesHash.add(message.hashCode())) {
            System.out.println(message.hashCode());
            if (message.getSender() != id) {
                sendToAll(message);
            }
            ReliableMessage<M> msg = (ReliableMessage<M>) message;
            upperLayer.deliver(msg.message);
        }

    }

    public void sendToAll(Message message) {
        for(int i=0; i < hosts; i++) {
            bcom.basicsend(i, message);
        }
    }

    @Override
    public void basicpeerdown(int peer) {

    }
}

class ReliableMessage<T extends Message> extends Message {
    public final T message;

    public ReliableMessage(T m) {
        super(m.getSender());
        message = m;
    }

    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + message.hashCode();
        return hash;
    }
}
