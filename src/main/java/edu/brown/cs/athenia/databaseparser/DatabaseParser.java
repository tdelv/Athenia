package edu.brown.cs.athenia.databaseparser;


import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.Module;

import edu.brown.cs.athenia.data.modules.Pair;
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
import java.util.stream.Collectors;

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
                "SELECT t.name tag_name " +
                        "FROM languages l, language_tags lt, tags t " +
                        "WHERE lt.tag_id = t.id " +
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
                while (rs.next()) {
                    String tagId = rs.getString("id");
                    if (!language.hasTag(tagId)) {
                        throw new DatabaseParserException("Bad data.");
                    }

                    module.addTag(language.getTag(tagId));
                }
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
                    while (rs.next()) {
                        StorageType type = StorageType.valueOf(rs.getString("type"));
                        String moduleId = rs.getString("id");
                        Module module = language.getModule(type, moduleId);
                        fn.addModule(module);
                    }
                }
            }
        }
    }

    public static void updateUser(String userId) throws DatabaseParserException, DriveApiException {
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
                updateFreeNotes(conn, language);
                updateModules(conn, language);
                updateFreeNoteModules(conn, language);
            }

        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (DriveApiException e) {
            e.printStackTrace();
        }

        File file = new java.io.File(
                "src/main/resources/userData/" + userId + ".sqlite3");
        GoogleDriveApi.setDataBase(userId, file);
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
                "SELECT t.name tag_name " +
                "FROM languages l, language_tags lt, tags t " +
                "WHERE lt.tag_id = t.id " +
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

    private static void updateFreeNotes(Connection conn, Language language) throws SQLException {
        Set<String> oldFreeNotes = new HashSet<>();
        Collection<FreeNote> newFreeNotes = language.getFreeNotes();

        // Find old and new freenotes
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT  fn.id fn_id " +
                "FROM freenotes fn, languages l " +
                "WHERE fn.language_id = l.id " +
                "AND l.language = ?")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String freeNoteId = rs.getString("fn_id");
                    if (language.containsFreeNote(freeNoteId)) {
                        newFreeNotes.remove(language.getFreeNote(freeNoteId));
                    } else {
                        oldFreeNotes.add(freeNoteId);
                    }
                }
            }
        }

        // Add new freenotes into database
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO freenotes " +
                        "(id, language_id, title, created, last_modified) VALUES " +
                        "(?, ?, ?, ?, ?)")) {
            for (FreeNote freeNote : newFreeNotes) {
                statement.setString(1, freeNote.getId());
                statement.setString(2, language.getName());
                statement.setString(3, freeNote.getTitle());
                statement.setLong(4, freeNote.getDateCreated().getTime());
                statement.setLong(5, freeNote.getDateModified().getTime());

                statement.addBatch();
            }

            statement.executeBatch();
        }

        // Remove old freenotes from database
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM freenotes " +
                        "WHERE id = ?")) {
            for (String langName : oldFreeNotes) {
                statement.setString(1, langName);
                statement.addBatch();
            }

            statement.executeBatch();
        }

        // Update all current freenotes in database
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE freenotes " +
                "SET title = ?, last_modified = ? " +
                "WHERE id = ?")) {
            for (FreeNote freeNote : language.getFreeNotes()) {
                statement.setString(1, freeNote.getTitle());
                statement.setLong(2, freeNote.getDateModified().getTime());
                statement.setString(3, freeNote.getId());
                statement.addBatch();
            }

            statement.executeBatch();
        }

        // Update freenote tags
        for (FreeNote freeNote : language.getFreeNotes()) {
            updateFreeNoteTags(conn, freeNote, language);
        }
    }

    private static void updateFreeNoteTags(Connection conn, FreeNote freeNote, Language language) throws SQLException {
        Set<String> oldTags = new HashSet<>();
        Collection<Tag> newTags = language.getTags();

        // Get old and new tags
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT t.name tag_name " +
                        "FROM freenote_tags ft, tags t " +
                        "WHERE ft.tag_id = t.id " +
                        "AND ft.freenote_id = ?")) {
            statement.setString(1, freeNote.getId());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String tagName = rs.getString("tag_name");
                    if (freeNote.hasTag(tagName)) {
                        newTags.remove(language.getTag(tagName));
                    } else {
                        oldTags.add(tagName);
                    }
                }
            }
        }

        // Add new tags
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO freenote_tags " +
                        "(freenote_id, tag_id) VALUES " +
                        "(?, ?)")) {
            for (Tag tag : newTags) {
                statement.setString(1, freeNote.getId());
                statement.setString(2, tag.getTag());
                statement.addBatch();
            }

            statement.executeBatch();
        }

        // Remove old tags
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM freenote_tags " +
                        "WHERE freenote_id = ? " +
                        "AND tag_id = ?")) {
            for (String tagName : oldTags) {
                statement.setString(1, freeNote.getId());
                statement.setString(2, tagName);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void updateModules(Connection conn, Language language) throws SQLException, DatabaseParserException {
        Collection<Module> newModules = language.getModules();
        Set<String> oldModules = new HashSet<>();

        if (newModules.contains(null)) {
            throw new DatabaseParserException("Null module.");
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT m.id m_id, m.type m_type " +
                "FROM modules m, languages l " +
                "WHERE m.language_id = l.id " +
                "AND l.language = ?")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    StorageType type = StorageType.valueOf(rs.getString("m_type"));
                    String moduleId = rs.getString("m_id");
                    Module module = language.getModule(type, moduleId);
                    if (module != null) {
                        newModules.remove(module);
                    } else {
                        oldModules.add(moduleId);
                    }
                }
            }
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO modules " +
                "(id, type, language_id, created, last_modified) VALUES " +
                "(?, ?, (SELECT id FROM languages WHERE language = ?), ?, ?)")) {
            for (Module module : newModules) {
                statement.setString(1, module.getId());
                statement.setString(2, module.getType().toString());
                statement.setString(3, language.getName());
                statement.setLong(4, module.getDateCreated().getTime());
                statement.setLong(5, 0);
                statement.addBatch();
            }

            statement.executeBatch();

            for (Module module : newModules) {
                createModule(conn, module, language);
            }
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM modules WHERE id = ?")) {
            for (String moduleId : oldModules) {
                statement.setString(1, moduleId);
                statement.addBatch();
            }

            statement.executeBatch();
        }

        for (Module module : language.getModules()) {
            updateModule(conn, module, language);
            updateModuleTags(conn, module, language);
        }
    }

    private static void createModule(Connection conn, Module module, Language language) throws DatabaseParserException, SQLException {
        String query;
        switch (module.getType()) {
            case NOTE:
                query = "INSERT INTO note_modules " +
                        "(module_id, body, rating) VALUES " +
                        "(?, '', 0)";
                break;
            case VOCAB:
                query = "INSERT INTO vocab_modules " +
                        "(module_id, term, definition, rating) VALUES " +
                        "(?, '', '', 0)";
                break;
            case CONJUGATION:
                query = "INSERT INTO conjugation_modules " +
                        "(module_id, header, rating, height) VALUES " +
                        "(?, '', 0, 0)";
                break;
            case QUESTION:
                query = "INSERT INTO question_modules " +
                        "(module_id, body) VALUES " +
                        "(?, '')";
                break;
            case ALERT_EXCLAMATION:
                query = "INSERT INTO alert_exclamation_modules " +
                        "(module_id, body) VALUES " +
                        "(?, '')";
                break;
            default:
                throw new DatabaseParserException("Bad data.");
        }

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, module.getId());
            statement.execute();
        }
    }

    private static void updateModule(Connection conn, Module module, Language language)
            throws SQLException, DatabaseParserException {
        Date lastUpdated;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT last_modified FROM modules WHERE id = ?")) {
            statement.setString(1, module.getId());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data.");
                }
                lastUpdated = new Date(rs.getInt("last_modified"));
            }
        }

        if (lastUpdated.equals(module.getDateModified())) {
            return;
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE modules " +
                "SET last_modified = ? " +
                "WHERE id = ?")) {
            statement.setLong(1, module.getDateModified().getTime());
            statement.setString(2, module.getId());
            statement.execute();
        }

        switch (module.getType()) {
            case NOTE:
                updateNoteModule(conn, (Note) module, language);
                break;
            case VOCAB:
                updateVocabModule(conn, (Vocab) module, language);
                break;
            case CONJUGATION:
                updateConjugationModule(conn, (Conjugation) module, language);
                break;
            case QUESTION:
                updateQuestionModule(conn, (Question) module, language);
                break;
            case ALERT_EXCLAMATION:
                updateAlertModule(conn, (AlertExclamation) module, language);
                break;
            default:
                throw new DatabaseParserException("Bad data.");
        }
    }

    private static void updateNoteModule(Connection conn, Note module, Language language) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE note_modules " +
                "SET body = ?, rating = ? " +
                "WHERE id = ?")) {
            statement.setString(1, module.getText());
            statement.setInt(2, module.getRating());
            statement.setString(3, module.getId());
            statement.execute();
        }
    }

    private static void updateVocabModule(Connection conn, Vocab module, Language language) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE vocab_modules " +
                "SET term = ?, definition = ?, rating = ? " +
                "WHERE module_id = ?")) {
            statement.setString(1, module.getPair().getTerm());
            statement.setString(2, module.getPair().getDefinition());
            statement.setInt(3, module.getRating());
            statement.setString(4, module.getId());
            statement.execute();
        }
    }

    private static void updateConjugationModule(Connection conn, Conjugation module, Language language) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE conjugation_modules " +
                        "SET header = ?, rating = ?, height = ? " +
                        "WHERE id = ?")) {
            statement.setString(1, module.getHeader());
            statement.setInt(2, module.getRating());
            statement.setInt(3, module.getHeight());
            statement.setString(4, module.getId());
            statement.execute();
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM conjugation_rows WHERE module_id = ?")) {
            statement.setString(1, module.getId());
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO conjugation_rows " +
                "(module_id, col1, col2, position) VALUES " +
                "(?, ?, ?, ?)")) {
            for (Pair pair : module.getTable()) {
                statement.setString(1, module.getId());
                statement.setString(2, pair.getTerm());
                statement.setString(3, pair.getDefinition());
                statement.setInt(4, module.getTable().indexOf(pair));
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void updateQuestionModule(Connection conn, Question module, Language language) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE question_modules " +
                        "SET body = ? " +
                        "WHERE id = ?")) {
            statement.setString(1, module.getText());
            statement.setString(2, module.getId());
            statement.execute();
        }
    }

    private static void updateAlertModule(Connection conn, AlertExclamation module, Language language) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE alert_exclamation_modules " +
                        "SET body = ? " +
                        "WHERE id = ?")) {
            statement.setString(1, module.getText());
            statement.setString(2, module.getId());
            statement.execute();
        }
    }

    private static void updateModuleTags(Connection conn, Module module, Language language) throws SQLException {
        Set<String> oldTags = new HashSet<>();
        Collection<Tag> newTags = module.getTags();

        // Get old and new tags
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT t.name tag_name " +
                        "FROM module_tags mt, tags t " +
                        "WHERE mt.tag_id = t.id " +
                        "AND mt.module_id = ?")) {
            statement.setString(1, module.getId());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String tagName = rs.getString("tag_name");
                    if (module.hasTag(tagName)) {
                        newTags.remove(language.getTag(tagName));
                    } else {
                        oldTags.add(tagName);
                    }
                }
            }
        }

        // Add new tags
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO module_tags " +
                        "(module_id, tag_id) VALUES " +
                        "(?, ?)")) {
            for (Tag tag : newTags) {
                statement.setString(1, module.getId());
                statement.setString(2, tag.getTag());
                statement.addBatch();
            }

            statement.executeBatch();
        }

        // Remove old tags
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM module_tags " +
                        "WHERE module_id = ? " +
                        "AND tag_id = ?")) {
            for (String tagName : oldTags) {
                statement.setString(1, module.getId());
                statement.setString(2, tagName);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void updateFreeNoteModules(Connection conn, Language language) throws SQLException {
        for (FreeNote freeNote : language.getFreeNotes()) {
            List<String> moduleOrder = new ArrayList<>();

            try (PreparedStatement statement = conn.prepareStatement(
                    "SELECT m.id m_id FROM modules m, freenote_modules fm " +
                    "WHERE fm.freenote_id = ? " +
                    "AND fm.module_id = m.id " +
                    "ORDER BY fm.position")) {

                statement.setString(1, freeNote.getId());
                try (ResultSet rs = statement.executeQuery()) {
                    moduleOrder.add(rs.getString("m_id"));
                }
            }

            List<String> currentModuleIds =
                    freeNote.getModules()
                            .stream()
                            .map(Module::getId)
                            .collect(Collectors.toList());

            if (moduleOrder.equals(currentModuleIds)) {
                continue;
            }

            try (PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM freenote_modules " +
                    "WHERE freenote_id = ?")) {
                statement.setString(1, freeNote.getId());
                statement.execute();
            }

            try (PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO freenote_modules " +
                    "(freenote_id, module_id, position) VALUES " +
                    "(?, ?, ?)")) {
                for (Module module : freeNote.getModules()) {
                    statement.setString(1, freeNote.getId());
                    statement.setString(2, module.getId());
                    statement.setInt(3, freeNote.getModules().indexOf(module));
                }
            }
        }
    }
}