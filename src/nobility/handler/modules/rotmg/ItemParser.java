package nobility.handler.modules.rotmg;

import org.apache.commons.text.TextStringBuilder;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemParser {

    private final List<Item> itemList = new ArrayList<>();
    private final Map<Integer, Integer> selectedItemList = new HashMap<>();
    private final File selectedItems = new File("./resources/items.txt");

    public ItemParser() {
        if (!selectedItems.exists()) {
            try {
                if (!selectedItems.createNewFile()) {
                    System.out.println("Couldn't create items.txt in resources folder.");
                }
            } catch (IOException e) {
                System.out.println("Error creating items.txt: " + e.getMessage());
            }
        }
        parseXMLs();
        parseItems();
        loadSelected();
    }

    private void parseItems() {
        File items = new File("./resources/ExtraItems.txt");
        if (!items.exists()) {
            try {
                FileOutputStream fos = null;
                try {
                    URL website = new URL("https://www.dropbox.com/s/gpheplskwc48bl8/ExtraItems.txt?dl=1");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream(items);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    System.out.println("Successfully downloaded ExtraItems.txt");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedReader r = new BufferedReader(new FileReader(items));
            String s;
            while ((s = r.readLine()) != null) {
                String id = s.substring(s.indexOf("ID:") + 3, s.indexOf("Name")).trim();
                String name = s.substring(s.lastIndexOf(":") + 1);
                Item i = new Item(name, id);
                i.setDescription("No description");
                if (!itemList.contains(i)) {
                    itemList.add(i);
                }
            }
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseXMLs() {
        File equipXml = new File("./resources/Equip.xml");
        if (!equipXml.exists()) {
            try {
                FileOutputStream fos = null;
                try {
                    URL website = new URL("https://static.drips.pw/rotmg/production/current/xml/Equip.xml");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream(equipXml);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    System.out.println("Successfully downloaded Equip.xml");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TextStringBuilder sb = new TextStringBuilder();
        BufferedReader r;
        try {
            r = new BufferedReader(new FileReader(equipXml));
            String s;
            while ((s = r.readLine()) != null) {
                sb.append(s);
                sb.appendNewLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File equipSkinsXml = new File("./resources/EquipSkins.xml");
        if (!equipSkinsXml.exists()) {
            try {
                FileOutputStream fos = null;
                try {
                    URL website = new URL("https://static.drips.pw/rotmg/production/current/xml/EquipSkins.xml");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream(equipSkinsXml);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    System.out.println("Successfully downloaded EquipSkins.xml");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            r = new BufferedReader(new FileReader(equipSkinsXml));
            String s;
            while ((s = r.readLine()) != null) {
                sb.append(s);
                sb.appendNewLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File equipEggsXml = new File("./resources/EquipEggs.xml");
        if (!equipEggsXml.exists()) {
            try {
                FileOutputStream fos = null;
                try {
                    URL website = new URL("https://static.drips.pw/rotmg/production/current/xml/EquipEggs.xml");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream(equipEggsXml);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    System.out.println("Successfully downloaded EquipSkins.xml");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            r = new BufferedReader(new FileReader(equipEggsXml));
            String s;
            while ((s = r.readLine()) != null) {
                sb.append(s);
                sb.appendNewLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File equipKeysXml = new File("./resources/EquipKeys.xml");
        if (!equipKeysXml.exists()) {
            try {
                FileOutputStream fos = null;
                try {
                    URL website = new URL("https://static.drips.pw/rotmg/production/current/xml/EquipKeys.xml");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream(equipKeysXml);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    System.out.println("Successfully downloaded EquipKeys.xml");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            r = new BufferedReader(new FileReader(equipKeysXml));
            String s;
            while ((s = r.readLine()) != null) {
                sb.append(s);
                sb.appendNewLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File equipTestXml = new File("./resources/EquipTest.xml");
        if (!equipTestXml.exists()) {
            try {
                FileOutputStream fos = null;
                try {
                    URL website = new URL("https://static.drips.pw/rotmg/production/current/xml/EquipTest.xml");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream(equipTestXml);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    System.out.println("Successfully downloaded EquipTest.xml");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            r = new BufferedReader(new FileReader(equipTestXml));
            String s;
            while ((s = r.readLine()) != null) {
                sb.append(s);
                sb.appendNewLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Matcher objectStart = Pattern.compile("(?=(<Object ))").matcher(sb.toString());
        Matcher objectEnd = Pattern.compile("(?=(</Object>))").matcher(sb.toString());
        List<Integer> objectIndexStart = new ArrayList<>();
        List<Integer> objectIndexEnd = new ArrayList<>();
        while (objectStart.find()) {
            objectIndexStart.add(objectStart.start());
        }
        while (objectEnd.find()) {
            objectIndexEnd.add(objectEnd.start());
        }
        for (int i = 0; i < objectIndexStart.size(); i++) {
            String object = sb.toString().substring(objectIndexStart.get(i) + 8, objectIndexEnd.get(i));
            if (object.contains("setName")) {
                continue;
            }
            String key1 = "<Description>";
            String key2 = "</Description>";
            String id1 = "type=\"";
            String id2 = "\" id=\"";
            String id3 = "\">";
            if (object.startsWith(id1)) {
                String id = String.valueOf(hex2decimal(object.substring(object.indexOf(id1) + id1.length(),
                        object.indexOf(id2)).replace("0x", "")));
                String name = object.substring(object.indexOf(id2) + id2.length(), object.indexOf(id3));
                Item item = new Item(name, id);
                if (object.contains(key1) && object.contains(key2)) {
                    String desc = object.substring(object.indexOf(key1) + key1.length(), object.indexOf(key2));
                    item.setDescription(desc.replaceAll("\\\\n", ""));
                } else {
                    item.setDescription("No Description");
                }
                itemList.add(item);
            }
        }
    }

    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    private void loadSelected() {
        try (BufferedReader r = new BufferedReader(new FileReader(selectedItems))) {
            String s;
            while ((s = r.readLine()) != null) {
                try {
                    int key = Integer.parseInt(s.substring(0, s.indexOf(":")));
                    if (!selectedItemList.containsKey(key)) {
                        selectedItemList.put(key,
                                Integer.parseInt(s.substring(s.indexOf(":") + 1)));
                    }
                } catch (Exception ignored) {
                }
            }
            itemList.forEach(i -> {
                if (selectedItemList.containsKey(Integer.parseInt(i.getIdString()))) {
                    i.setSelected(true);
                    i.setAmount(selectedItemList.get(Integer.parseInt(i.getIdString())));
                }
            });
        } catch (Exception e) {
            System.out.println("Error loading selected items: " + e.getMessage());
        }
    }

    public void saveSelected() {
        update();
        BufferedWriter w;
        try {
            w = new BufferedWriter(new FileWriter(selectedItems));
            for (int i : selectedItemList.keySet()) {
                w.write(i + ":" + selectedItemList.get(i));
                w.newLine();
            }
            w.flush();
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        selectedItemList.clear();
        itemList.forEach(item -> {
            if (item.getSelectedBool()) {
                if (!selectedItemList.containsKey(Integer.parseInt(item.getIdString()))) {
                    selectedItemList.put(Integer.parseInt(item.getIdString()), item.getAmountInt());
                }
            }
        });
    }

    public List<Item> getItemList() {
        return itemList;
    }

    Map<Integer, Integer> getSelectedMap() {
        return selectedItemList;
    }

    String idToName(int id) {
        for (Item i : itemList) {
            if (id == Integer.parseInt(i.getIdString())) {
                return i.getNameString();
            }
        }
        return null;
    }

}
