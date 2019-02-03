import mcgui.*;

import java.util.*;

public class TotalDecorator extends MulticastDecorator {

    public TotalDecorator(Multicaster mt) {
        multicaster = mt;
    }

}
