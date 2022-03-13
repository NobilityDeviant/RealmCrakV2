package nobility.handler.settings;

import nobility.model.Model;
import nobility.handler.modules.hits.Hit;
import nobility.save.Defaults;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

public class Collects {

    private final Model model;

    public Collects(Model model) {
        this.model = model;
    }

    private final List<ComboContainer> combos = Collections.synchronizedList(new ArrayList<>());
    private final List<ProxyContainer> proxies = Collections.synchronizedList(new ArrayList<>());
    private final List<ProxyContainer> backup_proxies = Collections.synchronizedList(new ArrayList<>());
    private final List<Hit> hitList = Collections.synchronizedList(new ArrayList<>());
    private final List<String> checkedList = Collections.synchronizedList(new ArrayList<>());
    private final List<String> nameList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Integer> goldMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Integer> rankMap = Collections.synchronizedMap(new HashMap<>());
    private String comboMD5;
    private volatile int comboProgress;

    public void setComboProgress(int comboProgress) {
        this.comboProgress = comboProgress;
    }

    public synchronized void removeComboProgress() {
        comboProgress++;
    }

    public void setComboMD5(File selectedFile) {
        if (selectedFile == null) {
            if (comboMD5 != null) {
                comboMD5 = null;
            }
            return;
        }
        byte[] hash;
        int posistion;
        try {
            hash = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(selectedFile.toPath()));
            comboMD5 = DatatypeConverter.printHexBinary(hash);
            posistion = model.combos().getPosistionForMD5(comboMD5);
            model.combos().addToCombos(comboMD5, posistion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getComboMD5() {
        return comboMD5;
    }

    public synchronized ComboContainer getCombo() {
        ComboContainer combo = null;
        try {
            combo = combos.get(comboProgress++);
            if (comboMD5 != null) {
                model.combos().addToCombos(comboMD5, comboProgress);
            }
        } catch (Exception ignored) {}
        return combo;
    }

    public List<ComboContainer> getCombos() {
        return combos;
    }

    public List<ProxyContainer> getProxies() {
        return proxies;
    }

    public List<ProxyContainer> getBackupProxies() {
        return backup_proxies;
    }

    public void updateBackupProxies() {
        backup_proxies.clear();
        backup_proxies.addAll(proxies);
    }

    public void reloadProxies() {
        proxies.addAll(backup_proxies);
    }

    public List<Hit> getHitList() {
        return hitList;
    }

    public List<String> getCheckedList() {
        return checkedList;
    }

    public void addToCheckedList(String s) {
        if (!model.save().getBoolean(Defaults.SAVECHECKED)) {
            return;
        }
        if (!checkedList.contains(s)) {
            checkedList.add(s);
            model.setChecked(checkedList.size());
        }
    }

    public List<String> getNameList() {
        return nameList;
    }

    public Map<String, Integer> getGoldMap() {
        return goldMap;
    }

    public Map<String, Integer> getRankMap() {
        return rankMap;
    }
}
