import mcgui.*;

import java.util.*;
import java.io.*;

public class TotalDecorator extends BasicMulticaster implements Receiver {

    private BasicMulticaster multicaster;
    private BasicCommunicator bcom;
    private int seq = 0;
    private int counter = 0;
    private TreeSet<PendingMessage> holdback = new TreeSet<>();
    private Map<Integer, TreeSet<SeqSuggest>> suggestions = new HashMap<>();

    public TotalDecorator(BasicMulticaster mt, BasicCommunicator bcom) {
        multicaster = mt;
        this.bcom = bcom;
        id = mt.id;
        hosts = mt.hosts;
        mt.setUpperLayer(this);
    }

    public <M extends Message> void cast(M m) {
        SeqRequest<M> request = new SeqRequest<M>(m, ++counter);
        multicaster.cast(request);
    }

    public <M extends Message> void deliver(M m) {
        switch (m.getClass().getSimpleName()) {
            case "SeqRequest":
                handleSeqRequest((SeqRequest)m);
                break;
            case "SeqSuggest":
                handleSeqSuggest((SeqSuggest)m);
                break;
            case "SeqDecide":
                handleSeqDecide((SeqDecide)m);
                break;
        }
    }

    private void handleSeqRequest(SeqRequest request) {
        System.out.println("In handleSeqRequest.");
        seq++;
        SeqSuggest suggest = new SeqSuggest(request.tag, seq, id);
        multicaster.cast(suggest);
        addToHoldbackQueue(request);
    }

    private void addToHoldbackQueue(SeqRequest request) {
        PendingMessage pm = new PendingMessage();
        pm.tag = request.tag;
        pm.msg = request.message;
        pm.isDeliverable = false;

        holdback.add(pm);
        deliverAllDeliverableFromHoldback();
    }

    private void handleSeqSuggest(SeqSuggest suggest) {
        System.out.println("In handleSeqSuggest.");
        // Not intended for us
        if (id != suggest.tag.owner)
            return;
        System.out.println("Owner handling SeqSuggest.");
        int mid = suggest.tag.mid;
        if (suggestions.containsKey(mid)) {
            System.out.println("Indeed containsKey : " + mid);
            TreeSet<SeqSuggest> msgSug = suggestions.get(mid);
            boolean result = msgSug.add(suggest);
            System.out.println("same? :" + msgSug.first().equals(suggest));
            System.out.println("add result:" + result + " suggest:" + suggest + " hash:" +suggest.hashCode() );
            suggestions.put(mid, msgSug);
            // TODO: Needs to handle node crashes.
            System.out.println("msgSug size: " + msgSug.size() + " hosts:" + hosts);
        } else {
            TreeSet<SeqSuggest> set = new TreeSet<>();
            set.add(suggest);
            suggestions.put(mid, set);
        }
        decideAndCastSeq(mid);
        System.out.println(suggestions);
    }

    private void decideAndCastSeq(int mid) {
        TreeSet<SeqSuggest> msgSug = suggestions.get(mid);
        if (msgSug.size() != hosts)
            return;
        SeqSuggest last = msgSug.pollLast();
        int seqDecision = last.suggestSeq;
        int suggester = last.suggester;

        // It's possible that multiple nodes suggesting the same seq.
        // Choose the one with smallest id.
        while ((last = msgSug.pollLast()) != null) {
            if (last.suggestSeq != seqDecision)
                break;
            if (last.suggester < last.suggester)
                suggester = last.suggester;
        }
        suggestions.remove(mid);
        MsgIdTag tag = new MsgIdTag(mid, id);
        SeqDecide de = new SeqDecide(tag, seqDecision, suggester);
        System.out.println(de);
        multicaster.cast(de);
    }

    private void handleSeqDecide(SeqDecide de) {
        System.out.println("In handleSeqDecide.");
        seq = Math.max(seq, de.seqDecision);
        PendingMessage target = null;

        for (PendingMessage pm : holdback) {
            if (pm.tag.equals(de.tag)) {
                target = pm;
                break;
            }
        }
        if (target == null) {
            throw new RuntimeException("No message found in holdback queue for the SeqDecide.");
        }
        holdback.remove(target);
        target.suggestedSeq = de.seqDecision;
        target.suggester = de.suggester;
        target.isDeliverable = true;
        System.out.println("holdback:" + holdback);
        holdback.add(target);
        System.out.println("holdback:" + holdback);
        deliverAllDeliverableFromHoldback();
    }

    private void deliverAllDeliverableFromHoldback() {
        while ((holdback.size() > 0) && holdback.first().isDeliverable) {
            PendingMessage toBeDelivered = holdback.pollFirst();
            upperLayer.deliver(toBeDelivered.msg);
        }
    }

    @Override
    public void basicreceive(int peer, Message m) {
        multicaster.basicreceive(peer, m);
    }

    @Override
    public void basicpeerdown(int peer){
        System.out.println("in basicpeerdown 1:" +hosts);
        hosts--;
        System.out.println("in basicpeerdown 2:" +hosts);
        for (Integer mid : suggestions.keySet()) {
            decideAndCastSeq(mid);
        }
        removePendingMessageFromHoldbackByOwner(peer);
        deliverAllDeliverableFromHoldback();
        multicaster.basicpeerdown(peer);
    }

    private void removePendingMessageFromHoldbackByOwner(int owner) {
        while (true) {
            boolean modified = false;
            for (PendingMessage pm : holdback) {
                if (pm.tag.owner == owner) {
                    holdback.remove(pm);
                    modified = true;
                    break;
                }
            }
            if (!modified)
                break;
        }
    }

    private class PendingMessage implements Comparable<PendingMessage> {
        MsgIdTag tag;
        Message msg;
        int suggestedSeq;
        int suggester;
        boolean isDeliverable;

        @Override
        public int compareTo(PendingMessage other) {
            if (this == other)
                return 0;
            if (suggestedSeq < other.suggestedSeq)
                return -1;
            else if (suggestedSeq > other.suggestedSeq)
                return 1;
            else {
                if (!isDeliverable)
                    return -1;
                else if (!other.isDeliverable)
                    return 1;
                else {
                    if (suggester < other.suggester)
                        return -1;
                    else if (suggester > other.suggester)
                        return 1;
                    else
                        throw new RuntimeException("Impossible PendingMessage occured.");
                }

            }
        }
    }

}

class MsgIdTag implements Serializable {
    public final int mid;
    public final int owner;

    public MsgIdTag(int mid, int owner) {
        this.mid = mid;
        this.owner = owner;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (!(other instanceof MsgIdTag))
            return false;
        if (other == this)
            return true;
        MsgIdTag o = (MsgIdTag)other;
        if (mid == o.mid && owner == o.owner)
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 37 + owner;
        hash = hash * 37 + mid;
        return hash;
    }
}

class SeqRequest<M extends Message> extends Message {
    public final M message;
    public final MsgIdTag tag;

    public SeqRequest(M m, int mid) {
        super(m.getSender());
        message = m;
        tag = new MsgIdTag(mid, sender);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 37 + message.hashCode();
        hash = hash * 37 + tag.hashCode();
        return hash;
    }
}

class SeqSuggest extends Message implements Comparable<SeqSuggest> {
    public final MsgIdTag tag;
    public final int suggestSeq;
    public final int suggester;

    public SeqSuggest(MsgIdTag tag, int suggestSeq, int suggester) {
        super(suggester);
        this.tag = tag;
        this.suggestSeq = suggestSeq;
        this.suggester = suggester;
    }

    @Override
    public int compareTo(SeqSuggest s) {

        if (suggestSeq < s.suggestSeq)
            return -1;
        else if (suggestSeq > s.suggestSeq)
            return 1;
        else {
            if (suggester < s.suggester)
                return 1;
            else if (suggester > s.suggester)
                return -1;
            else
                return 0;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (!(other instanceof SeqSuggest))
            return false;
        if (other == this)
            return true;
        SeqSuggest o = (SeqSuggest)other;
        if (tag.equals(o.tag) &&
            suggestSeq == o.suggestSeq &&
            suggester == o.suggester &&
            sender == o.getSender())
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 37 + sender;
        hash = hash * 37 + tag.hashCode();
        hash = hash * 37 + suggestSeq;
        hash = hash * 37 + suggester;
        return hash;
    }
}

class SeqDecide extends Message {
    public final MsgIdTag tag;
    public final int seqDecision;
    public final int suggester;

    public SeqDecide(MsgIdTag tag, int seqDecision, int suggester) {
        super(tag.owner);
        this.tag = tag;
        this.seqDecision = seqDecision;
        this.suggester = suggester;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 37 + sender;
        hash = hash * 37 + seqDecision;
        hash = hash * 37 + tag.hashCode();
        hash = hash * 37 + suggester;
        return hash;
    }

}
