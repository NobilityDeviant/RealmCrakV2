package nobility.handler.settings;

public class ComboContainer {

    private final String email;
    private final String password;
    private final String seperator;

    public ComboContainer(String combo, String seperator) {
        this.seperator = seperator;
        String[] split = combo.split(seperator);
        email = split[0];
        password = split[1];
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return email + seperator + password;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComboContainer) {
            ComboContainer container = (ComboContainer) obj;
            return container.toString().equals(toString());
        }
        return super.equals(obj);
    }
}
