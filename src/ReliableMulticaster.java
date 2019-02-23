
import mcgui.*;

import java.util.*;
import java.util.concurrent.*;

public class ReliableMulticaster extends BasicMulticaster {

    BasicCommunicator bcom;
    Set<Integer> deliveredMessagesHash = new HashSet<>();
    Set<Integer> liveNodes = new HashSet<>();
    int sequenceNumber = 0;

    public ReliableMulticaster(BasicCommunicator bcom, int id, int hosts) {
        this.bcom = bcom;
        this.id = id;
        this.hosts = hosts;
        for (int i = 0; i < hosts; i++)
            liveNodes.add(i);
    }

    @Override
    public <M extends Message> void cast(M message) {
        sendToAll(new ReliableMessage<M>(message));
    }

    @Override
    public void basicreceive(int peer, Message message) {
        if (deliveredMessagesHash.add(message.hashCode())) {
            System.out.println(message.hashCode());
            if (message.getSender() != id) {
                sendToAll(message);
            }
            ReliableMessage msg = (ReliableMessage) message;
            upperLayer.deliver(msg.message);
        }

    }

    public void sendToAll(Message message) {
        for(Integer node : liveNodes) {
            bcom.basicsend(node, message);
        }
    }

    @Override
    public void basicpeerdown(int peer) {
        hosts--;
        liveNodes.remove(peer);
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
