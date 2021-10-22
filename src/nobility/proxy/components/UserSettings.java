package nobility.proxy.components;

import javafx.util.Pair;
import nobility.proxy.components.entities.ProxyAnonymity;
import nobility.proxy.components.entities.ProxyType;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the user configurable system settings
 */
public class UserSettings {

    private int timeout; // timeout for checking the proxy
    private int threads; // max threads
    private String ip = "0.0.0.0"; // ip address of the user
    private Proxy.Type defaultType = Proxy.Type.HTTP;
    private boolean autoRemoveDuplicates = true;
    private String latestOpenPath = System.getProperty("user.home");
    private boolean autoCheckType = false;

    private List<Pair<ProxyAnonymity, String>> colorScheme = new ArrayList<>();

    /**
     * Creates a new UserSettings object
     */
    public UserSettings() {
        this.setTimeout(5000);
        this.setThreads(100);

        this.colorScheme.add(new Pair<>(ProxyAnonymity.ELITE, "#7f6bb5"));
        this.colorScheme.add(new Pair<>(ProxyAnonymity.ANONYMOUS, "#b3b5e6"));
        this.colorScheme.add(new Pair<>(ProxyAnonymity.TRANSPARENT, "#bd7ba1"));

    }

    public String getLatestOpenPath() {
        return latestOpenPath;
    }

    public void setLatestOpenPath(String latestOpenPath) {
        this.latestOpenPath = latestOpenPath;
    }

    /**
     * Gets the option to auto remove duplicate proxies on load
     * @return Boolean - Auto remove
     */
    public boolean getAutoRemoveDuplicates() {
        return autoRemoveDuplicates;
    }

    public UserSettings setAutoRemoveDuplicates(boolean autoRemoveDuplicates) {
        this.autoRemoveDuplicates = autoRemoveDuplicates;
        return this;
    }

    /**
     * Gets the current IP address of the user
     * @return String - IP address
     */
    public String getIp() {
        return this.ip;
    }

    /**
     * Gets the current maximum threads allowed to be spawned
     * @return int - number of threads
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Gets the timeout for web requests
     * @return int - the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the current users IP address
     * @param ip - String IP address
     * @return UserSettings
     */
    public UserSettings setIp(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * Sets the current preferred timeout for web requests
     * @param timeout - int
     * @return UserSettings
     */
    public UserSettings setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the preffered maximum threads allowed to be spawned
     * @param threads - int
     * @return UserSettings
     */
    public UserSettings setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    /**
     * Sets the default proxy type that will be checked
     * @param defaultType - java.net.Proxy.Type (HTTP or SOCKS)
     * @return UserSettings
     */
    public UserSettings setProxyType(Proxy.Type defaultType) {
        this.defaultType = defaultType;
        return this;
    }

    /**
     * Gets the default proxy type that will be checked
     * @return java.net.Proxy.Type
     */
    public Proxy.Type getProxyType() {
        if (this.defaultType == null) {
            return Proxy.Type.HTTP;
        }
        return this.defaultType;
    }

    /**
     * Gets the color scheme for the proxy table
     * @return List
     */
    public List<Pair<ProxyAnonymity, String>> getColorScheme() {
        if(colorScheme == null) {
            ProxySettings.saveConfig(new UserSettings());
        }
        return colorScheme;
    }

    /**
     * Sets the color scheme for the proxy table
     * @param colorScheme list
     */
    public void setColorScheme(List<Pair<ProxyAnonymity, String>> colorScheme) {
        this.colorScheme = colorScheme;
    }

    public boolean isAutoCheckType() {
        return autoCheckType;
    }

    public UserSettings setAutoCheckType(boolean autoCheckType) {
        this.autoCheckType = autoCheckType;
        return this;
    }
}
