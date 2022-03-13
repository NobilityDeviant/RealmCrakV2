package nobility.handler.modules.hits;

import org.apache.commons.text.TextStringBuilder;

public class Hit {

    private final String email;
    private final String password;
    private final String seperator;
    private final boolean highQuality;
    private final TextStringBuilder results;
    private final String proxy;

    public Hit(String email, String password, String seperator, boolean highQuality,
               TextStringBuilder results, String proxy) {
        this.email = email;
        this.password = password;
        this.seperator = seperator;
        this.highQuality = highQuality;
        this.results = results;
        this.proxy = proxy;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getSeperator() {
        return seperator;
    }

    public boolean isHighQuality() {
        return highQuality;
    }

    public TextStringBuilder getResults() {
        return results;
    }

    public String getProxy() {
        return proxy;
    }

    public String getCombo() {
        return email + seperator + password;
    }
}
