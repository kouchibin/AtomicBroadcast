
import mcgui.*;

import java.util.*;

public class CausalDecorator extends BasicMulticaster implements Receiver {

    private BasicMulticaster multicaster;

    private List<Message> prevDel;
    private Set<Integer> delivered;

    public CausalDecorator(BasicMulticaster mt) {
        multicaster = mt;
        mt.setUpperLayer(this);
        id = mt.id;
        hosts = mt.hosts;
        delivered = new HashSet<>();
        prevDel = new ArrayList<>();
    }

    @Override
    public <M extends Message> void cast(M m) {
        CausalMessage<M> message = new CausalMessage<M>(prevDel, m);
        multicaster.cast(message);
        prevDel.clear();
    }

    @Override
    public <M extends Message> void deliver(M msg) {
        CausalMessage<M> cm = (CausalMessage<M>) msg;
        for (M m : cm.msgList) {
            if (delivered.contains(m.hashCode()))
                continue;
            else {
                upperLayer.deliver(m);
                delivered.add(m.hashCode());
                prevDel.add(m);
            }
        }
    }

    @Override
    public void basicpeerdown(int peer) {
        multicaster.basicpeerdown(peer);
    }

    @Override
    public void basicreceive(int peer, Message message) {
        multicaster.basicreceive(peer, message);
    }
}

class CausalMessage<M extends Message> extends Message {
    List<M> msgList;
    public CausalMessage(List<Message> prevDel, M message) {
        super(message.getSender());
        msgList = new ArrayList<M>((List<M>)prevDel);
        msgList.add(message);
    }

    public int hashCode() {
        int hash = 7;
        for (M m : msgList)
            hash = hash * 37 + m.hashCode();
        return hash;
    }
}
