package fieldling.quilldriver.task;

import java.util.*;
import fieldling.quilldriver.gui.QD;

public abstract class BasicTask {
    public static Map tasksOnOffer = new HashMap();
    
    public abstract void execute(QD qd, String parameters);
    
    public static BasicTask getTaskForClass(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class c = Class.forName(className);
        if (!tasksOnOffer.containsKey(c))
            tasksOnOffer.put(c, c.newInstance());
        return (BasicTask)tasksOnOffer.get(c);
    }
}
