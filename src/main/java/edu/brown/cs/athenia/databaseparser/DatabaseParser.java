package edu.brown.cs.athenia.databaseparser;

import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.module.Conjugation;
import edu.brown.cs.athenia.data.modules.module.Note;
import edu.brown.cs.athenia.data.modules.module.Vocab;
import edu.brown.cs.athenia.main.Athenia;
import edu.brown.cs.athenia.driveapi.DriveApiException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DatabaseParser will hold our SQL parsing information.
 * @author makaylamurphy
 *
 */
public class DatabaseParser {

    private static final Map<String, Athenia> USER_MAP = new HashMap<>();


    private static Connection getConnection(String filePath) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:" + filePath;
        return DriverManager.getConnection(url);
    }

    private static Connection loadConnection(String userId)
            throws SQLException, ClassNotFoundException, IOException, DriveApiException {
        File file = GoogleDriveApi.getDataBase(userId);

        if (!file.exists()) {
            setup(userId, file);
        }

        return getConnection(file.getPath());
    }

    private static void setup(String userId, File file) throws IOException, SQLException, ClassNotFoundException {
        File queryFile = new File("src/main/resources/SQLCommands/setup_database");

        String data;
        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile))) {
            data = reader.lines().reduce("", (acc, ele) -> acc + "\n" + ele);
        }

        String[] queries = data.split(";");

        file.createNewFile();
        try (Connection conn = getConnection(file.getPath());
                Statement statement = conn.createStatement()) {
            for (String query : queries) {
                statement.addBatch(query);
            }

            statement.executeBatch();
        }
    }

    public static Athenia getUser(String userId) throws DatabaseParserException {
        if (USER_MAP.containsKey(userId)) {
            return USER_MAP.get(userId);
        }

        Athenia user = new Athenia(userId);
        USER_MAP.put(userId, user);

        try (Connection conn = loadConnection(userId)) {
            // Get meta data for user
            try (Statement statement = conn.createStatement();
                 ResultSet rs = statement.executeQuery(
                    "SELECT username, joined, last_update FROM user_data")) {

            }

            // Get languages
            try (Statement statement = conn.createStatement();
                 ResultSet rs = statement.executeQuery(
                    "SELECT language FROM languages")) {
                while (rs.next()) {
                    user.addLanguage(rs.getString(1));
                }
            }

            // Fill in languages
            for (String langName : user.getLanguages()) {
                user.setCurrLang(langName);
                Language language = user.getCurrLanguage();

                int id;
                String type;
                int created;
                int lastModified;
                try (Statement statement = conn.createStatement();
                     ResultSet rs = statement.executeQuery(
                        "SELECT m.id, m.type, m.created, m.last_modified FROM " +
                                "modules AS m, languages AS l WHERE " +
                                "m.language_id = l.id AND " +
                                "l.language = " + langName)) {
                    while (rs.next()) {
                        id = rs.getInt(1);
                        type = rs.getString(2);
                        created = rs.getInt(3);
                        lastModified = rs.getInt(4);

                        Module module;
                        switch (type) {
                            case "note":
                                try (ResultSet rs2 = statement.executeQuery(
                                        "SELECT body FROM note_modules WHERE " +
                                                "id = " + id)) {
                                    module = new Note(rs.getString(1));
                                }
                                break;
                            case "vocab":
                                try (ResultSet rs2 = statement.executeQuery(
                                        "SELECT term, definition FROM vocab_modules WHERE " +
                                                "id = " + id)) {
                                    module = new Vocab();
                                    module.update(new String[]{rs.getString(1), rs.getString(2)});
                                }
                                break;
//                            case "conjugation":
//                                try (ResultSet rs2 = statement.executeQuery(
//                                        "SELECT header FROM vocab_modules WHERE " +
//                                                "id = " + id)) {
//                                    module = new Conjugation();
//                                    module.update(new String[]{rs.getString(1), rs.getString(2)});
//                                }
//                                break;
                            default:
                                module = null;
                                break;

                        }
                        module.setId(Integer.toString(id));
                        module.setDateModified(Date.from(Instant.ofEpochMilli(lastModified)));
                    }
                }


            }

        } catch (SQLException | ClassNotFoundException | IOException e) {
            throw new DatabaseParserException(e);
        } catch (DriveApiException e) {
            throw new DatabaseParserException(e);
        }

        return user;
    }
}
