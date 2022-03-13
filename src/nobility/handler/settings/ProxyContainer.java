package nobility.handler.settings;

public class ProxyContainer {

    private final String ip;
    private int port;
    private String username;
    private String password;
    private int retries;

    public ProxyContainer(String proxy) {
        String[] split = proxy.split(":");
        this.ip = split[0];
        try {
            this.port = Integer.parseInt(split[1]);
        } catch (Exception ignored) {
            this.port = 80;
        }
        if (split.length >= 4) {
            try {
                this.username = split[2];
                this.password = split[3];
            } catch (Exception ignored) {}
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void addRetry() {
        retries++;
    }

    @Override
    public String toString() {
        return ip + ":" + port + (username != null && password != null ? ":" + username + ":" + password : "");
    }
}
