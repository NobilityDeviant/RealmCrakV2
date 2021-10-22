package nobility.save;

import nobility.Controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Combos implements Serializable {

    private final HashMap<String, Integer> savedCombos = new HashMap<>();

    public void addToCombos(String md5, int posistion) {
        if (!savedCombos.containsKey(md5)) {
            savedCombos.put(md5, posistion);
        } else {
            savedCombos.replace(md5, posistion);
        }
    }

    public boolean hasMD5(String md5) {
        return savedCombos.containsKey(md5);
    }

    public int getPosistionForMD5(String md5) {
        if (hasMD5(md5)) {
            return savedCombos.get(md5);
        } else {
            savedCombos.put(md5, 0);
            return 0;
        }
    }
}

