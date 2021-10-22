package nobility.save;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import nobility.Constants;
import nobility.model.Model;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.Key;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("all")
public class Database {

    //
    private Connection connection = null;
    private final String databaseUrl = "uNEpPMUxdy9VZzFrXbtHzDtuwK481Ap5UBOWRx0OmuJIUyzXJq0RtKASuQ3YcNT0wbz0BwuHZTKh49Mb8SAb2lM/rqwDJbXxDzxqVu9vBUbHV8o57z19x0sayR2GTKxh";
    private final String uuidFileName = "/p7qMJA9IfzzMFZotopjug=="; //key
    private final String setIdForKey = "YUEoPUCfPjCQg5S43taiBdQ2PzFPNoUDvhUPB4GhR1M="; //key
    private final String setResetDateForKey = "YT9whS7YUUEV7oUFl/lvahsQtmHzi8s7FY8Pl+tTIJa4eanptr+Wtbo0+fwjci+w"; //key
    private final String databaseUsername = "Za+YBQWHSZTaUM6F9Iz2Iw=="; //name key
    private final String databasePassword = "v4RFCW5Rlwxti6ItEbmSxg=="; //password key
    private final String tableUsers = "wd7y2YPB94jhNLnooW+pCg=="; //key
    private final String tableTrials = "ngE4nC+83xBcdNtl8rJGgA=="; //core
    private final String selectFromWildcard = "ZuWOK6t027NoL65of0YBCQ=="; //core
    private final String update = "Sfh1eKgTojo1+2t1K1LU9Q=="; //key
    private final String loginWhereKeyReplace = "SO/PvN9CnyyiCbUfaBLy/uiLu5yngLLP0wMXJcqEhYY=";//"WHERE login_key=?"; //key
    private final String setTrialsUUIDDateLogsWhereKey = "qAR2V2m3xaeiV/pxnCiVVd560G+B8XAdABL889WVs0fmw4+satLJ9BH3/15qQY0Q"; //core
    private final String setTrialsLogsWhereKey = "2GTotSnJq9A4rsh5m5NSnacmGaQagHZL59TaG8Puhjc=";//"SET logs=? WHERE login_key=?"; //core
    private final String trialDateFormat = "P4oe1FZVH8SZ2gzrlk9ZeIEiQ1lfPj3UBy9GqPONrZo="; //key
    private final String trialUUIDAppName = "Ac5WTMUDAVxzJpt7DfH0lQ==";
    private final String trialUUIDVersion = "uWQ6Yh5HMUOz8sCTie+koA==";
    private final String trialUUIDAuthor = "V1MSiLszde/c4j/0lXVN6w==";
    private final String trialUUIDFilename = "hc0x0yGJ1NAVuk/P90a9Mg==";
    private Key key = null;
    private Key coreKey = null;
    private final String timezoneFormat = "US/Mountain";
    private final Model model;

    public Database(Model model) {
        this.model = model;
        try {
            key = new SecretKeySpec(Constants.keyBytes, Constants.type);
            coreKey = new SecretKeySpec(Constants.coreBytes, Constants.type);
        } catch (Exception e) {
            model.showError("Error building DB (GK) This program won't function without it. Shutting down...");
            System.exit(-1);
        }
        //encrypt(databaseUrl, key);
        //encrypt(databaseUsername, key);
        //encrypt(databasePassword, key);
        //System.out.println(model.decrypt(databaseUrl, key));
        storeUUID();
    }

    public boolean freeTrialEnded(String userKey) {
        if (connection == null) {
            try {
                connect(model.decrypt(databaseUrl, key),
                        model.decrypt(databaseUsername, key),
                        model.decrypt(databasePassword, key));
            } catch (Exception e) {
                return false;
            }
        }/* else {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    connection = null;
                    connect(model.decrypt(databaseUrl, key),
                            model.decrypt(databaseUsername, key),
                            model.decrypt(databasePassword, key));
                }
            } catch (Exception ignored) {
                return false;
            }
        }*/
        String query = model.decrypt(selectFromWildcard, coreKey) + " "
                + model.decrypt(tableTrials, coreKey) + " "
                + model.decrypt(loginWhereKeyReplace, key);
        String pattern = model.decrypt(trialDateFormat, key);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userKey);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int banned = resultSet.getInt(3);
                if (banned >= 1) {
                    return true;
                }
                String enddate = resultSet.getString(4);
                Instant now = Instant.now();
                ZonedDateTime mountainTime = now.atZone(ZoneId.of(timezoneFormat));
                String start = mountainTime.format(DateTimeFormatter.ofPattern(pattern, Locale.US));
                Instant startInstant = Instant.parse(start);
                Instant endInstant = Instant.parse(enddate);

                if (endInstant.compareTo(startInstant) < 0) {
                    return true; //times up!
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to init trial. Error: " + e.getMessage());
        }/* finally {
            try { if (preparedStatement != null) { preparedStatement.close(); } } catch (Exception ignored) {}
            try { if (resultSet != null) { resultSet.close(); } } catch (Exception ignored) {}
            try {
                connection.close();
            } catch (Exception ignored) {}
        }*/
        return false;
    }

    public DatabaseMessages freeTrialInit(String userKey) {
        String UUID = getTrialUUID();
        if (UUID == null) {
            return DatabaseMessages.NOUUID;
        }
        if (connection == null) {
            try {
                connect(model.decrypt(databaseUrl, key),
                        model.decrypt(databaseUsername, key),
                        model.decrypt(databasePassword, key));
            } catch (Exception e) {
                return DatabaseMessages.BADCONNECTION;
            }
        } /*else {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    connection = null;
                    connect(model.decrypt(databaseUrl, key),
                            model.decrypt(databaseUsername, key),
                            model.decrypt(databasePassword, key));
                }
            } catch (Exception ignored) {
                return DatabaseMessages.BADCONNECTION;
            }
        }*/

        String query = model.decrypt(selectFromWildcard, coreKey) + " "
                + model.decrypt(tableTrials, coreKey) + " "
                + model.decrypt(loginWhereKeyReplace, key);

        String pattern = model.decrypt(trialDateFormat, key);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        PreparedStatement updateStatement = null;
        PreparedStatement updateStatementLog = null;
        Statement statement = null;
        ResultSet resultSetCheckUUID = null;

        try {

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userKey);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                if (resultSet.getInt(3) >= 1) {
                    return DatabaseMessages.BANNED;
                }

                String uuid = resultSet.getString(2);

                if (uuid.isEmpty()) {
                    statement = connection.createStatement();
                    resultSetCheckUUID = statement.executeQuery(model.decrypt(selectFromWildcard, coreKey)
                            + " " + model.decrypt(tableTrials, coreKey));
                    while (resultSetCheckUUID.next()) {
                        if (resultSetCheckUUID.getString(2).equalsIgnoreCase(UUID)) {
                            return DatabaseMessages.UUIDONOTHERKEY;
                        }
                    }
                    Instant now = Instant.now();
                    ZonedDateTime mountainTime = now.atZone(ZoneId.of(timezoneFormat));

                    int days = resultSet.getInt(6);

                    if (days == 0) {
                        days = 3;
                    }

                    String sql = model.decrypt(update, key) + " " + model.decrypt(tableTrials, coreKey)
                            + " " + model.decrypt(setTrialsUUIDDateLogsWhereKey, coreKey);

                    updateStatement = connection.prepareStatement(sql);
                    updateStatement.setString(1, UUID);
                    updateStatement.setString(2, mountainTime.plusDays(days).format(DateTimeFormatter.ofPattern(pattern, Locale.US)));
                    updateStatement.setInt(3, 1);
                    updateStatement.setString(4, userKey);
                    int updated = updateStatement.executeUpdate();
                    if (updated <= 0) {
                        System.err.println("Failed to update database. No error thrown.");
                        return DatabaseMessages.UPDATEFAILED;
                    } else {
                        model.setDays(days);
                        model.setHours(0);
                        model.setMinutes(0);
                        System.out.println("Successfully set your uuid to your trial key.");
                        return DatabaseMessages.SUCCESS;
                    }
                } else {
                    String enddate = resultSet.getString(4);
                    Instant now = Instant.now();
                    ZonedDateTime mountainTime = now.atZone(ZoneId.of(timezoneFormat));
                    String start = mountainTime.format(DateTimeFormatter.ofPattern(pattern, Locale.US));
                    Instant startInstant = Instant.parse(start);
                    Instant endInstant = Instant.parse(enddate);

                    long seconds = endInstant.getEpochSecond() - startInstant.getEpochSecond();

                    model.setDays((int) seconds / 86400);
                    model.setHours((int) seconds / 3600 % 24);
                    model.setMinutes((int) seconds / 60 % 60);
                    //System.out.println(model.getDays() + ":" + model.getHours() + ":" + model.getMinutes());
                    if (endInstant.compareTo(startInstant) < 0) {
                        return DatabaseMessages.TRIALSOVER;
                    }
                    if (uuid.equalsIgnoreCase(UUID)) {
                        String sql = model.decrypt(update, key) + " " + model.decrypt(tableTrials, coreKey)
                                + " " + model.decrypt(setTrialsLogsWhereKey, coreKey);

                        updateStatementLog = connection.prepareStatement(sql);
                        updateStatementLog.setInt(1, resultSet.getInt(5) + 1);
                        updateStatementLog.setString(2, userKey);
                        if (updateStatementLog.executeUpdate() <= 0) {
                            System.err.println("Failed to update database. No error thrown.");
                            return DatabaseMessages.UPDATEFAILED;
                        }
                        return DatabaseMessages.SUCCESS;
                    } else {
                        return DatabaseMessages.MISMATCHUUID;
                    }
                }
            } else {
                return DatabaseMessages.INVALIDKEY;
            }
        } catch (Exception e) {
            System.err.println("Failed to init trial. Error: " + e.getMessage());
        }/* finally {
            try { if (preparedStatement != null) { preparedStatement.close(); } } catch (Exception ignored) {}
            try { if (resultSet != null) { resultSet.close(); } } catch (Exception ignored) {}
            try { if (updateStatement != null) { updateStatement.close(); } } catch (Exception ignored) {}
            try { if (updateStatementLog != null) { updateStatementLog.close(); } } catch (Exception ignored) {}
            try { if (statement != null) { statement.close(); } } catch (Exception ignored) {}
            try { if (resultSetCheckUUID != null) { resultSetCheckUUID.close(); } } catch (Exception ignored) {}
        }*/
        System.err.println("Error: Couldn't return type.");
        return DatabaseMessages.UPDATEFAILED;
    }

    public byte login(String userKey) {
        String UUID = readUUID();
        if (UUID == null) {
            return 8;
        }
        if (connection == null) {
            try {
                connect(model.decrypt(databaseUrl, key),
                        model.decrypt(databaseUsername, key),
                        model.decrypt(databasePassword, key));
            } catch (Exception e) {
                e.printStackTrace();
                return 4;
            }
        }
        userKey = userKey.trim();
        String query = model.decrypt(selectFromWildcard, coreKey) + " " + model.decrypt(tableUsers, key)
                + " " + model.decrypt(loginWhereKeyReplace, key);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userKey);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String column1 = resultSet.getString(1);
                if (userKey.equalsIgnoreCase(column1)) {
                    if (resultSet.getBoolean(3)) {
                        return 1;
                    }
                    String ID = resultSet.getString(2);
                    if (ID.isEmpty()) {
                        String sql = model.decrypt(update, key) + " " + model.decrypt(tableUsers, key) + " "
                                + model.decrypt(setIdForKey, key);
                        PreparedStatement update = connection.prepareStatement(sql);
                        update.setString(1, UUID);
                        update.setString(2, userKey);
                        int rowsUpdated = update.executeUpdate();
                        update.close();
                        if (rowsUpdated > 0) {
                            System.out.println("Successfully registered key to this PC.");
                        } else {
                            System.err.println("Failed to update db. No error thrown.");
                            return 5;
                        }
                    } else {
                        if (!ID.equalsIgnoreCase(UUID)) {
                            return 2;
                        }
                    }
                }
                return 0;
            }
            return 3;
        } catch (Exception e) {
            System.err.println("Failed to login. Error: " + e.getMessage());
        }/* finally {
            try { if (preparedStatement != null) { preparedStatement.close(); } } catch (Exception ignored) {}
            try { if (resultSet != null) { resultSet.close(); } } catch (Exception ignored) {}
        }*/
        return 5;
    }

    public byte resetUUID(String userKey) {
        if (connection == null) {
            try {
                connect(model.decrypt(databaseUrl, this.key),
                        model.decrypt(databaseUsername, this.key),
                        model.decrypt(databasePassword, this.key));
            } catch (Exception e) {
                return 4;
            }
        }/* else {
            try {
                if (connection.isClosed()) {
                    connect(model.decrypt(databaseUrl, key),
                            model.decrypt(databaseUsername, key),
                            model.decrypt(databasePassword, key));
                }
            } catch (Exception ignored) {
                return 4;
            }
        }*/
        userKey = userKey.trim();

        Instant now = Instant.now();
        ZonedDateTime mountainTime = now.atZone(ZoneId.of(timezoneFormat));
        String pattern = model.decrypt(trialDateFormat, key);
        String today = mountainTime.format(DateTimeFormatter.ofPattern(pattern, Locale.US));

        String query = model.decrypt(selectFromWildcard, coreKey) + " "
                + model.decrypt(tableUsers, key) + " " + model.decrypt(loginWhereKeyReplace, key);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userKey);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //String column1 = resultSet.getString(1);
                if (resultSet.getBoolean(3)) {
                    return 1; //banned
                }
                String ID = resultSet.getString(2);
                if (ID.isEmpty()) {
                    return 2;
                }
                String resetDate = resultSet.getString(4);

                if (resetDate == null || resetDate.isEmpty()) {
                    String sql = model.decrypt(update, key) + " " + model.decrypt(tableUsers, key) + " "
                            + model.decrypt(setResetDateForKey, key);
                    PreparedStatement update = connection.prepareStatement(sql);
                    update.setString(1, today);
                    update.setString(2, userKey);
                    int rowsUpdated = update.executeUpdate();
                    update.close();
                    if (rowsUpdated <= 0) {
                        System.err.println("Failed to update db. No error thrown.");
                        return 5;
                    }
                } else {
                    Instant startInstant = Instant.parse(today);
                    Instant endInstant = Instant.parse(resetDate);
                    if (startInstant.compareTo(endInstant) < 0) {
                        long seconds = endInstant.getEpochSecond() - startInstant.getEpochSecond();
                        model.setDays((int) seconds / 86400);
                        model.setHours((int) seconds / 3600 % 24);
                        model.setMinutes((int) seconds / 60 % 60);
                        return 6; // today is before reset, must wait
                    }
                }
                return updateUUID(userKey);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Unparseable date")
                    || e.getMessage().contains("could not be parsed")) {
                try {
                    return updateUUID(userKey);
                } catch (Exception e1) {
                    System.err.println("Error updating uuid.");
                    return 7;
                }
            }
            return 7;
        }/* finally {
            try { if (preparedStatement != null) { preparedStatement.close(); } } catch (Exception ignored) {}
            try { if (resultSet != null) { resultSet.close(); } } catch (Exception ignored) {}
        }*/
        return 3; //invalid key
    }

    private byte updateUUID(String userKey) throws SQLException {
        Instant now = Instant.now();
        ZonedDateTime mountainTime = now.atZone(ZoneId.of(timezoneFormat));
        String pattern = model.decrypt(trialDateFormat, key);

        String sqlreset = model.decrypt(update, key) + " " + model.decrypt(tableUsers, key) + " "
                + model.decrypt(setResetDateForKey, key);
        PreparedStatement update1 = connection.prepareStatement(sqlreset);
        update1.setString(1, mountainTime.plusDays(3).format(DateTimeFormatter.ofPattern(pattern, Locale.US)));
        update1.setString(2, userKey);
        int updated = update1.executeUpdate();
        update1.close();
        if (updated <= 0) {
            System.err.println("Failed to update db. No error thrown.");
            return 5;
        }
        String sql = model.decrypt(update, key) + " " + model.decrypt(tableUsers, key) + " "
                + model.decrypt(setIdForKey, key);
        PreparedStatement update = connection.prepareStatement(sql);
        update.setString(1, ""); //supposed to be empty.
        update.setString(2, userKey);
        int rowsUpdated = update.executeUpdate();
        update.close();
        if (rowsUpdated > 0) {
            return 0; //success
        } else {
            System.err.println("Failed to update db. No error thrown.");
            return 5; //failed to update, try again
        }
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private void storeUUID() {
        File f = new File(getSystemPath() + "/" + model.decrypt(uuidFileName, key));
        if (!f.exists()) {
            String s = generateUUID();
            try {
                if (!f.createNewFile()) {
                    model.showError("Unable to create UUID. Contact Nobility.");
                }
            } catch (IOException ignored) {
                model.showError("Unable to create UUID. Contact Nobility.");
            }
            try {
                BufferedWriter w = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
                w.write(s);
                w.flush();
                w.close();
            } catch (IOException ignored) {}
        }
    }

    private String readUUID() {
        File f = new File(getSystemPath() + "/" + model.decrypt(uuidFileName, key));
        if (!f.exists())
            return null;
        try {
            BufferedReader r = new BufferedReader(new FileReader(f.getAbsolutePath()));
            return r.readLine();
        } catch (Exception ignored) {
            storeUUID();
        }
        return null;
    }

    private void connect(String u, String n, String p) throws Exception {
        //System.out.println(u + ":" + n + ":" + p);
        connection = DriverManager.getConnection(u, n, p);
    }

    /*private void encrypt(String strToEncrypt, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(Constants.transform);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            System.out.println(Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(Charset.defaultCharset()))));
        } catch (Exception e) {
            System.out.println("Error while encrypting " + strToEncrypt + " Error: " + e.getMessage());
        }
    }*/

    private String getSystemPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return FilenameUtils.getPrefix(System.getProperty("user.home")) + File.separator + "Users"
                    + File.separator + "Public" + File.separator;
        } else if (SystemUtils.IS_OS_LINUX) {
            String home = System.getProperty("user.home");
            if (home.contains("root")) {
                return home + File.separator;
            }
            return  home + File.separator + "Public" + File.separator;
        } else if (SystemUtils.IS_OS_MAC) {
            String macFix = FilenameUtils.getPrefix(System.getProperty("user.home"))
                    + File.separator + "Shared" + File.separator;
            if (new File(macFix).exists()) {
                return macFix;
            }
            String macFix1 = FilenameUtils.getPrefix(System.getProperty("user.home"))
                    + File.separator;
            if (new File(macFix1).exists()) {
                return macFix1;
            }
            return "." + File.separator;
        } else {
            return "";
        }
    }

    public String getTrialUUID() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        String directory = appDirs.getSiteConfigDir(model.decrypt(trialUUIDAppName, coreKey),
                model.decrypt(trialUUIDVersion, coreKey), model.decrypt(trialUUIDAuthor, coreKey));
        File uuid = new File(directory + File.separator + model.decrypt(trialUUIDFilename, coreKey));
        try {
            Files.createDirectories(new File(directory).toPath());
            if (!uuid.exists()) {
                if (uuid.createNewFile()) {
                    String id = generateUUID();
                    Files.write(uuid.toPath(), Collections.singletonList(id));
                    return id;
                }
            } else {
                List<String> lines = Files.readAllLines(uuid.toPath());
                if (!lines.isEmpty()) {
                    return lines.get(0);
                }
                System.err.println("Found UUID file, but it is empty. Try again. Deleted: " + uuid.delete());
                System.err.println("If the above says Deleted: false then contact Nobility.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Unable to get the UUID file in your system.");
            if (SystemUtils.IS_OS_WINDOWS) {
                System.err.println("You must run this program with Admin privileges for trials.");
                System.err.println("Right click the batch or jar and select Run As administrator");
            } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
                System.err.println("You must run this program with sudo privileges for trials.");
                System.err.println("sudo java -jar RealmCrakV2.jar");
            } else {
                System.err.println("You must run this program with Admin privileges for trials.");
            }
        }
        return null;
    }


}
