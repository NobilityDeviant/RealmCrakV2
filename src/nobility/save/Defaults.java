package nobility.save;

public enum Defaults {

    //0 bool, 1 int, 2 string
    SKIPTOOSHORT("skiptooshort", 0, true),
    AUTOSCROLL("autoscroll", 0, true),
    NAMECHOSEN("namechosen", 0, true),
    CLOSETOSYSTEMTRAY("closetotray", 0, false),
    AUTOREMOVEDUPE_PROXIES("ardupeproxies", 0, true),
    AUTOREMOVEDUPE_COMBOS("ardupecombos", 0, true),
    SHOWHITS("showhits", 0, true),
    SHOWDEBUG("showdebug", 0, true),
    SHOWPROXYERRORS("showproxyerrors", 0, false),
    EMPTYCONSOLECYCLE("emptycycle", 0, true),
    //SAVECOMBOS("savecombos", 0, true),
    SHOWCONSOLEALERT("consolealert", 0, true),
    CHECKERTHREADS("threadsc", 1, 150),
    PROXYTHREADS("threadsp", 1, 100),
    CHECKERTIMEOUT("timeoutc", 1, 10),
    PROXYTIMEOUT("timeoutp", 1, 10),
    CHECKERRETRIES("retriesc", 1, 2),
    CHECKERTRIESPETS("retriescp", 1 , 2),
    REALMEYERETRIES("retriesre", 1, 5),
    PROXYRETRIES("retriesp", 1, 2),
    LASTCOMBOFOLDER("lastcombofolder", 2, System.getProperty("user.home")),
    LASTCHECKERPROXYFOLDER("lastproxyfolderc", 2, System.getProperty("user.home")),
    LASTPROXYFOLDER("lastproxyfolderp", 2, System.getProperty("user.home")),
    SEPERATOR("seperator", 2, ":"),
    SAVECHECKED("savechecked", 0, true),
    SAVENAMESINFILE("savenames", 0, false),
    //CHECKFORPETS("checkpets", 0, false),
    HQRANK("rank", 1, 30),
    HQGOLD("gold", 1, 1000),
    HQFAME("fame", 1, 10_000),
    HQCHARS("chars", 1, 0),
    //HQVAULTS("vaults", 1, 0),
    //HQPASSWORD("hqpassword", 2, ""),
    HQPETRARITY("hqpetrarity", 2, "Rare"),
    CHECKREALMEYE("realmeye", 0, true),
    //HQCHANGEPASSWORD("hqchangepass", 0, false),
    SOCKS("socks", 0, false),
    BLOCKEDNAMES("blockednames", 0, true),
    DENIEDUPDATE("deniedupdate", 0, false),
    UPDATEVERSION("version", 2, "1.0"),
    SAVEGOLDINFILE("savegold", 0, false),
    SAVERANKINFILE("saveranks", 0, false);

    private final String key;
    private final int type;
    private final Object value;

    public String getKey() {
        return key;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    Defaults(String key, int type, Object value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

}
