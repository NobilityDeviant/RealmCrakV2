package nobility.save;

public enum DatabaseMessages {

    SUCCESS(0),
    INVALIDKEY(1),
    BANNED(2),
    NOUUID(3),
    BADCONNECTION(4),
    UPDATEFAILED(5),
    MISMATCHUUID(6),
    TRIALSOVER(7),
    UUIDONOTHERKEY(8);

    private final int type;

    DatabaseMessages(int type) {
        this.type = type;
    }

    public byte type() {
        return (byte) type;
    }

}
