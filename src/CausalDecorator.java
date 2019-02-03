
import mcgui.*;

import java.util.*;

public class CausalDecorator extends MulticastDecorator {

    private List<Integer> next;

    public CausalDecorator(Multicaster mt) {
        multicaster = mt;
    }

}
