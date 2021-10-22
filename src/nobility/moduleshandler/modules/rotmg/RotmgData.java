package nobility.moduleshandler.modules.rotmg;

public enum RotmgData {

    INVALIDCREDENTIALSERROR("WebChangePasswordDialog.passwordError"),
    CHARINVALIDCREDENTIALSERROR("Account credentials not valid"),
    INVALIDEMAIL("Error.invalidEmail"),
    NOCLIENTTOKENERROR("NoClientTokenSpecified"),
    PROXYBANNED("Internal error"),
    MIGRATING("<Migrate>"),
    NEEDSTOACCEPTTERMSERROR("Account is under maintenance");

    private final String identifier;

    public String getIdentifier() {
        return identifier;
    }

    RotmgData(String identifier) {
        this.identifier = identifier;
    }
}
