package edu.brown.cs.athenia.databaseparser;


import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.Module;

import edu.brown.cs.athenia.data.modules.Tag;
import edu.brown.cs.athenia.data.modules.module.*;
import edu.brown.cs.athenia.driveapi.UnauthenticatedUserException;
import edu.brown.cs.athenia.main.Athenia;
import edu.brown.cs.athenia.driveapi.DriveApiException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;

/**
 * DatabaseParser will hold our SQL parsing information.
 * @author makaylamurphy
 *
 */
public class DatabaseParser {

    private static final Map<String, Athenia> USER_MAP = new HashMap<>();
    private static final Map<String, Date> LAST_UPDATED_MAP = new HashMap<>();

    private static Connection connectToDatabase(String filePath) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:" + filePath;
        Connection conn = DriverManager.getConnection(url);
        try (Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    private static Connection getConnection(String userId)
            throws SQLException, ClassNotFoundException, IOException, DriveApiException {
        File file = GoogleDriveApi.getDataBase(userId);

        if (!file.exists()) {
            setup(file);
        }

        return connectToDatabase(file.getPath());
    }

    private static void setup(File file) throws IOException, SQLException, ClassNotFoundException {
        File queryFile = new File("src/main/resources/SQLCommands/setup_database");

        String data;
        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile))) {
            data = reader.lines().reduce("", (acc, ele) -> acc + "\n" + ele);
        }

        String[] queries = data.split(";");

        file.createNewFile();
        try (Connection conn = connectToDatabase(file.getPath());
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
        LAST_UPDATED_MAP.put(userId, new Date());

        try (Connection conn = getConnection(userId)) {
            // Get meta data for user
            getUserData(conn, user);

            // Get languages
            getLanguages(conn, user);

            // Fill in languages
            for (String langName : user.getLanguages()) {
                user.setCurrLang(langName);
                Language language = user.getCurrLanguage();

                getTags(conn, language);
                getFreeNotes(conn, language);
                getModules(conn, language);
                getFreeNoteModules(conn, language);
            }

        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new DatabaseParserException(e);
        } catch (DriveApiException e) {
            e.printStackTrace();
            throw new DatabaseParserException(e);
        }

        return user;
    }

    private static void getUserData(Connection conn, Athenia user) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT * FROM user_data")) {

        }
    }

    private static void getLanguages(Connection conn, Athenia user) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT * FROM languages")) {
            while (rs.next()) {
                user.addLanguage(rs.getString("language"));
            }
        }
    }

    private static void getTags(Connection conn, Language language) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT tag.name tag_name " +
                        "FROM languages l, language_tags lt, tags t " +
                        "WHERE lt.tag_id = tag.id " +
                        "AND lt.language_id = l.id " +
                        "AND l.language = ?")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    language.addTag(new Tag(rs.getString("tag_name")));
                }
            }
        }
    }

    private static void getFreeNotes(Connection conn, Language language) throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT  fn.title title, " +
                        "fn.id fn_id, " +
                        "fn.created created, " +
                        "fn.last_modified last_modified " +
                        "FROM freenotes fn, languages l, tags t, language_tags lt " +
                        "WHERE fn.language_id = l.id " +
                        "AND l.language = ? " +
                        "AND t.id = lt.tag_id " +
                        "AND lt.language_id = l.id")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    FreeNote fn = new FreeNote(
                            rs.getString("title"),
                            rs.getString("fn_id"));

                    getFreeNoteTags(conn, fn, language);
                    fn.setDateCreated(new Date(rs.getInt("created")));
                    fn.setDateModified(new Date(rs.getInt("last_modified")));
                }
            }
        }
    }

    private static void getFreeNoteTags(Connection conn, FreeNote fn, Language language) throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM freenote_tags WHERE module_id = ?")) {
            statement.setString(1, fn.getId());
            try (ResultSet rs = statement.executeQuery()) {
                String tagId = rs.getString("id");
                if (!language.hasTag(tagId)) {
                    throw new DatabaseParserException("Bad data.");
                }

                fn.addTag(language.getTag(tagId));
            }
        }
    }

    private static void getModules(Connection conn, Language language) throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT m.* " +
                        "FROM modules m, languages l " +
                        "WHERE m.language_id = l.id " +
                        "AND l.language = ?")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    StorageType type = StorageType.valueOf(rs.getString("type"));
                    Module module;
                    switch (type) {
                        case NOTE:
                            module = getNoteModule(conn, rs.getString("id"));
                            break;
                        case VOCAB:
                            module = getVocabModule(conn, rs.getString("id"));
                            break;
                        case CONJUGATION:
                            module = getConjugationModule(conn, rs.getString("id"));
                            break;
                        case QUESTION:
                            module = getQuestionModule(conn, rs.getString("id"));
                            break;
                        case ALERT_EXCLAMATION:
                            module = getAlertModule(conn, rs.getString("id"));
                            break;
                        default:
                            throw new DatabaseParserException("Bad data.");
                    }

                    language.addModule(type, module);

                    getModuleTags(conn, module, language);

                }
            }
        }
    }

    private static Note getNoteModule(Connection conn, String id) throws SQLException, DatabaseParserException {
        Note module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM note_modules " +
                        "WHERE module_id = ?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
                module = new Note(rs.getString("body"));
                module.setRating(rs.getInt("rating"));
                if (rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
            }
        }

        return module;
    }

    private static Vocab getVocabModule(Connection conn, String id) throws SQLException, DatabaseParserException {
        Vocab module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM vocab_modules " +
                        "WHERE module_id = ?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
                module = new Vocab(
                        rs.getString("term"),
                        rs.getString("definition"));
                if (rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
            }
        }

        return module;
    }

    private static Conjugation getConjugationModule(Connection conn, String id) throws SQLException, DatabaseParserException {
        Conjugation module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM conjugation_modules " +
                        "WHERE module_id = ?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
                module = new Conjugation();
                module.setHeight(rs.getInt("height"));
                if (rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
            }
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM conjugation_rows " +
                        "WHERE module_id = ? " +
                        "ORDER BY position")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    module.add(rs.getString("row1"), rs.getString("row2"));
                }
            }
        }

        return module;
    }

    private static Question getQuestionModule(Connection conn, String id) throws SQLException, DatabaseParserException {
        Question module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM question_modules " +
                        "WHERE module_id = ?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
                module = new Question(rs.getString("body"));
                if (rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
            }
        }

        return module;
    }

    private static AlertExclamation getAlertModule(Connection conn, String id) throws SQLException, DatabaseParserException {
        AlertExclamation module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM alert_exclamation_modules " +
                        "WHERE module_id = ?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
                module = new AlertExclamation(rs.getString("body"));
                if (rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
            }
        }

        return module;
    }

    private static void getModuleTags(Connection conn, Module module, Language language) throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM module_tags WHERE module_id = ?")) {
            statement.setString(1, module.getId());
            try (ResultSet rs = statement.executeQuery()) {
                String tagId = rs.getString("id");
                if (!language.hasTag(tagId)) {
                    throw new DatabaseParserException("Bad data.");
                }

                module.addTag(language.getTag(tagId));
            }
        }
    }

    private static void getFreeNoteModules(Connection conn, Language language) throws SQLException {
        for (FreeNote fn : language.getFreeNotes()) {
            try (PreparedStatement statement = conn.prepareStatement(
                    "SELECT m.* FROM modules m, freenote_modules fm " +
                    "WHERE fm.freenote_id = ? " +
                    "AND fm.module_id = m.id " +
                    "ORDER BY fm.position")) {
                statement.setString(1, fn.getId());
                try (ResultSet rs = statement.executeQuery()) {
                    StorageType type = StorageType.valueOf(rs.getString("type"));
                    String moduleId = rs.getString("id");
                    Module module = language.getModule(type, moduleId);
                    fn.addModule(module);
                }
            }
        }
    }

    public static void updateUser(String userId) throws DatabaseParserException {
        if (!USER_MAP.containsKey(userId)) {
            throw new DatabaseParserException("No such user.");
        }

        Athenia user = USER_MAP.get(userId);

        try (Connection conn = getConnection(userId)) {
            updateUserData(conn, user);
            updateLanguages(conn, user);

            for (String langName : user.getLanguages()) {
                user.setCurrLang(langName);
                Language language = user.getCurrLanguage();

                updateTags(conn, language);
//                updateFreeNotes(conn, language);
//                updateModules(conn, language);
//                updateFreeNoteModules(conn, language);
            }

        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (DriveApiException e) {
            e.printStackTrace();
        }
    }

    private static void updateUserData(Connection conn, Athenia user) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE user_data " +
                "SET username = ?, joined = ?, last_update = ?")) {
            statement.setString(1, "");
            statement.setLong(2, new Date().getTime());
            statement.setLong(3, new Date().getTime());

            statement.execute();
        }
    }

    private static void updateLanguages(Connection conn, Athenia user) throws SQLException {
        Set<String> oldLanguages = new HashSet<>();
        Collection<String> newLanguages = user.getLanguages();

        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT * FROM languages")) {
            while (rs.next()) {
                String langName = rs.getString("language");
                if (newLanguages.contains(langName)) {
                    newLanguages.remove(langName);
                } else {
                    oldLanguages.add(langName);
                }
            }
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO languages " +
                "(id, language) VALUES " +
                "(?, ?)")) {
            for (String langName : newLanguages) {
                statement.setString(1, langName);
                statement.setString(2, langName);
                statement.addBatch();
            }

            statement.executeBatch();
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM languages " +
                "WHERE id = ?")) {
            for (String langName : oldLanguages) {
                statement.setString(1, langName);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void updateTags(Connection conn, Language language) throws SQLException {
        Set<String> oldTags = new HashSet<>();
        Collection<Tag> newTags = language.getTags();

        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT tag.name tag_name " +
                "FROM languages l, language_tags lt, tags t " +
                "WHERE lt.tag_id = tag.id " +
                "AND lt.language_id = l.id " +
                "AND l.language = ?")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String tagName = rs.getString("tag_name");
                    if (language.hasTag(tagName)) {
                        newTags.remove(language.getTag(tagName));
                    } else {
                        oldTags.add(tagName);
                    }
                }
            }
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO tags " +
                        "(id, name) VALUES " +
                        "(?, ?)")) {
            for (Tag tag : newTags) {
                statement.setString(1, tag.getTag());
                statement.setString(2, tag.getTag());
                statement.addBatch();
            }

            statement.executeBatch();
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO language_tags " +
                "(language_id, tag_id) VALUES " +
                "((SELECT id FROM languages WHERE language = ?), ?)")) {
            for (Tag tag : newTags) {
                statement.setString(1, language.getName());
                statement.setString(2, tag.getTag());
                statement.addBatch();
            }

            statement.executeBatch();
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM tags " +
                        "WHERE id = ?")) {
            for (String tagName : oldTags) {
                statement.setString(1, tagName);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
}