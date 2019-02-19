import mcgui.*;

import java.util.*;

public class TotalDecorator extends BasicMulticaster implements Receiver {

    public TotalDecorator(Multicaster mt) {
        multicaster = mt;
    }

}
