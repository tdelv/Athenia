package edu.brown.cs.athenia.databaseparser;


import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.Module;

import edu.brown.cs.athenia.data.modules.Pair;
import edu.brown.cs.athenia.data.modules.Tag;
import edu.brown.cs.athenia.data.modules.module.*;
import edu.brown.cs.athenia.driveapi.GoogleDriveApiException;
import edu.brown.cs.athenia.main.Athenia;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * A class for handling the translation between program memory and
 * database storage. Has external interactions allowing:
 *  - Getting the Athenia object for a given user;
 *  - Updating the user's database with their Athenia object.
 * @author Thomas Del Vecchio
 */
public class DatabaseParser {

    private static final Map<String, Athenia> USER_MAP = new HashMap<>();

    // External interactions with other parts of project.

    /**
     * Generates a new Athenia object for the given user.
     * The order of preference is:
     *  - Read from program memory if in cache;
     *  - Read from local server memory if in GoogleDriveApi cache;
     *  - Read from Google Drive file;
     *  - Create new database file for user.
     * @param userId The id of the user.
     * @return the Athenia object containing the user's data.
     * @throws DatabaseParserException when something goes wrong generating the user.
     */
    public static Athenia getUser(String userId)
            throws DatabaseParserException {
        // Grab from cache if available.
        if (USER_MAP.containsKey(userId)) {
            return USER_MAP.get(userId);
        }

        // Create a new Athenia for user
        Athenia user = new Athenia(userId);

        // Set data for the user from their database
        try (Connection conn = getConnection(userId)) {
            // Get meta data for user
            getUserData(conn, user);

            // Get languages
            getLanguages(conn, user);

            // Fill in languages
            for (String langName : user.getLanguages()) {
                user.setCurrLang(langName);
                Language language = user.getCurrLanguage();

                // Get data for each language
                getTags(conn, language);
                getFreeNotes(conn, language);
                getModules(conn, language);
                getFreeNoteModules(conn, language);
            }

        } catch (SQLException | ClassNotFoundException | IOException | GoogleDriveApiException e) {
            e.printStackTrace();
            // Wrap any exceptions from interaction with API with project exception
            throw new DatabaseParserException(e);
        }

        // Add Athenia to cache if generation has succeeded
        USER_MAP.put(userId, user);

        return user;
    }

    /**
     * Update the user's database with their current Athenia data, and
     * attempts to write it to their Google Drive file.
     * Does not recreate database, but instead applies updates based on
     * differences between database and program memory.
     * @param userId The id of the user.
     * @throws DatabaseParserException when something goes wrong updating the database.
     */
    public static void updateUser(String userId)
            throws DatabaseParserException {
        if (!USER_MAP.containsKey(userId)) {
            throw new DatabaseParserException("No such user.");
        }

        // Get the user
        Athenia user = USER_MAP.get(userId);

        // Update (or create) the user's database file
        try (Connection conn = getConnection(userId)) {
            // Update the user's metadata
            updateUserData(conn, user);

            // Update the user's languages
            updateLanguages(conn, user);

            for (String langName : user.getLanguages()) {
                user.setCurrLang(langName);
                Language language = user.getCurrLanguage();

                // Update the data for each language
                updateTags(conn, language);
                updateFreeNotes(conn, language);
                updateModules(conn, language);
                updateFreeNoteModules(conn, language);
            }

        } catch (IOException | SQLException | ClassNotFoundException | GoogleDriveApiException e) {
            // Wrap any exceptions from interaction with API with project exception
            throw new DatabaseParserException(e);
        }

        // Write to the user's Google Drive
        try {
            File file = new java.io.File(
                    "src/main/resources/userData/" + userId + ".sqlite3");
            GoogleDriveApi.setDataBase(userId, file);
        } catch (GoogleDriveApiException e) {
            // Wrap any exceptions from interaction with API with project exception
            throw new DatabaseParserException(e);
        }
    }

    /**
     * Checks whether the given user has a locally stored Athenia.
     * @param userId The id of the user.
     * @return whether they have a locally stored Athenia.
     */
    public static boolean hasUser(String userId) {
        return USER_MAP.containsKey(userId);
    }

    /**
     * Deletes user from local memory cache.
     * @param userId The id of the user.
     */
    public static void deleteUser(String userId) {
        USER_MAP.remove(userId);
    }


    // Internal helper methods

    // Generating connection to database

    /**
     * Generates the SQL Connection to a given database file.
     * @param filePath The path to the database file.
     * @return the Connection to the SQL database.
     * @throws ClassNotFoundException when something goes wrong loading SQLite3.
     * @throws SQLException when a SQL command is incorrectly executed.
     */
    private static Connection connectToDatabase(String filePath)
            throws ClassNotFoundException, SQLException {
        // Generate connection
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:" + filePath;
        Connection conn = DriverManager.getConnection(url);

        // Force foreign keys to work correctly
        try (Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return conn;
    }

    /**
     * Gets the connection to the database for a given user.
     * @param userId The id of the user whose database we want.
     * @return the Connection to the database.
     * @throws ClassNotFoundException when something goes wrong loading SQLite3.
     * @throws SQLException when a SQL command is incorrectly executed.
     * @throws IOException when something goes wrong on initial database setup.
     * @throws GoogleDriveApiException when something goes wrong getting the database from Google Drive.
     */
    private static Connection getConnection(String userId)
            throws SQLException, ClassNotFoundException, IOException, GoogleDriveApiException {
        // Get database from Google Drive, or else set one up for user
        File file = GoogleDriveApi.getDataBase(userId);
        if (!file.exists()) {
            setup(file);
        }

        // Connect to the database
        return connectToDatabase(file.getPath());
    }

    /**
     * Sets up a new database file for the user if none found in Google Drive.
     * @param file The file where we want to create the database.
     * @throws ClassNotFoundException when something goes wrong loading SQLite3.
     * @throws SQLException when a SQL command is incorrectly executed.
     * @throws IOException when something goes wrong reading from database setup file or creating new database file.
     */
    private static void setup(File file)
            throws IOException, SQLException, ClassNotFoundException {
        // Read the SQL database setup commands for a new user
        File queryFile = new File("src/main/resources/SQLCommands/setup_database");

        String data;
        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile))) {
            data = reader.lines().reduce("", (acc, ele) -> acc + "\n" + ele);
        }

        String[] queries = data.split(";");

        // Create new local file for user and execute SQL commands
        file.createNewFile();
        try (Connection conn = connectToDatabase(file.getPath());
             Statement statement = conn.createStatement()) {
            for (String query : queries) {
                statement.addBatch(query);
            }

            statement.executeBatch();
        }
    }

    // Helpers for getting user data

    /**
     * Gets the metadata for the given user.
     * Unpopulated until user metadata is added to data structures.
     * @param conn The connection to the user's database.
     * @param user The object holding user's data.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void getUserData(Connection conn, Athenia user)
            throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT * FROM user_data")) {
            rs.next();
        }
    }

    /**
     * Gets the languages for the given user.
     * @param conn The connection to the user's database.
     * @param user The object holding user's data.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void getLanguages(Connection conn, Athenia user)
            throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT * FROM languages")) {
            while (rs.next()) {
                user.addLanguage(rs.getString("language"));
            }
        }
    }

    /**
     * Gets the tags for the given language.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void getTags(Connection conn, Language language)
            throws SQLException {
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

    /**
     * Gets the free notes for the given language.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static void getFreeNotes(Connection conn, Language language)
            throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT  fn.title title, " +
                        "fn.id fn_id, " +
                        "fn.created created, " +
                        "fn.last_modified last_modified " +
                "FROM freenotes fn, languages l " +
                "WHERE fn.language_id = l.id " +
                "AND l.language = ?")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    FreeNote freeNote = new FreeNote(
                            rs.getString("title"),
                            rs.getString("fn_id"));
                    language.addFreeNote(freeNote);

                    getFreeNoteTags(conn, freeNote, language);
                    freeNote.setDateCreated(new Date(rs.getInt("created")));
                    freeNote.setDateModified(new Date(rs.getInt("last_modified")));
                }
            }
        }
    }

    /**
     * Gets the tags for the given free note.
     * @param conn The connection to the user's database.
     * @param freeNote The current free note.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static void getFreeNoteTags(Connection conn, FreeNote freeNote, Language language)
            throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM freenote_tags WHERE freenote_id = ?")) {
            statement.setString(1, freeNote.getId());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String tagId = rs.getString("tag_id");
                    if (!language.hasTag(tagId)) {
                        throw new DatabaseParserException("Bad data.");
                    }

                    freeNote.addTag(language.getTag(tagId));
                }
            }
        }
    }

    /**
     * Gets the modules for the given language.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static void getModules(Connection conn, Language language)
            throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT m.* " +
                "FROM modules m, languages l " +
                "WHERE m.language_id = l.id " +
                "AND l.language = ?")) {
            statement.setString(1, language.getName());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    // Get the module specific data
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

                    module.setId(rs.getString("id"));
                    language.addModule(type, module);

                    getModuleTags(conn, module, language);

                }
            }
        }
    }

    /**
     * Create a new Note module from database.
     * @param conn The connection to the user's database.
     * @param moduleId The id of the module.
     * @return the created module.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static Note getNoteModule(Connection conn, String moduleId)
            throws SQLException, DatabaseParserException {
        Note module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM note_modules " +
                "WHERE module_id = ?")) {
            statement.setString(1, moduleId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data: " +
                            "Module with type Note not in note_modules table.");
                }

                module = new Note(rs.getString("body"));
                module.setRating(rs.getInt("rating"));
            }
        }

        return module;
    }

    /**
     * Create a new Vocab module from database.
     * @param conn The connection to the user's database.
     * @param moduleId The id of the module.
     * @return the created module.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static Vocab getVocabModule(Connection conn, String moduleId)
            throws SQLException, DatabaseParserException {
        Vocab module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM vocab_modules " +
                "WHERE module_id = ?")) {
            statement.setString(1, moduleId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data: " +
                            "Module with type Vocab not in vocab_modules table.");
                }

                module = new Vocab(
                        rs.getString("term"),
                        rs.getString("definition"));
            }
        }

        return module;
    }

    /**
     * Create a new Conjugation module from database.
     * @param conn The connection to the user's database.
     * @param moduleId The id of the module.
     * @return the created module.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static Conjugation getConjugationModule(Connection conn, String moduleId)
            throws SQLException, DatabaseParserException {
        Conjugation module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM conjugation_modules " +
                "WHERE module_id = ?")) {
            statement.setString(1, moduleId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data: " +
                            "Module with type Conjugation not in conjugation_modules table.");
                }

                module = new Conjugation();
                module.setHeight(rs.getInt("height"));
            }
        }

        // Get the rows of the conjugation table
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM conjugation_rows " +
                        "WHERE module_id = ? " +
                        "ORDER BY position")) {
            statement.setString(1, moduleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    module.add(rs.getString("row1"), rs.getString("row2"));
                }
            }
        }

        return module;
    }

    /**
     * Create a new Question module from database.
     * @param conn The connection to the user's database.
     * @param moduleId The id of the module.
     * @return the created module.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static Question getQuestionModule(Connection conn, String moduleId)
            throws SQLException, DatabaseParserException {
        Question module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM question_modules " +
                "WHERE module_id = ?")) {
            statement.setString(1, moduleId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data:" +
                            "Module with type Question not in question_modules table.");
                }

                module = new Question(rs.getString("body"));
            }
        }

        return module;
    }


    /**
     * Create a new AlertExclamation module from database.
     * @param conn The connection to the user's database.
     * @param moduleId The id of the module.
     * @return the created module.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static AlertExclamation getAlertModule(Connection conn, String moduleId)
            throws SQLException, DatabaseParserException {
        AlertExclamation module;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM alert_exclamation_modules " +
                "WHERE module_id = ?")) {
            statement.setString(1, moduleId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data: " +
                            "Module with type AlertExclamation not in alert_exclamation_modules table.");
                }

                module = new AlertExclamation(rs.getString("body"));
            }
        }

        return module;
    }

    /**
     * Gets the tags for the given module.
     * @param conn The connection to the user's database.
     * @param module The module.
     * @param language The language the module belongs to.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static void getModuleTags(Connection conn, Module module, Language language)
            throws SQLException, DatabaseParserException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM module_tags WHERE module_id = ?")) {
            statement.setString(1, module.getId());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String tagId = rs.getString("tag_id");
                    if (!language.hasTag(tagId)) {
                        throw new DatabaseParserException("Bad data: " +
                                "Tag " + tagId + " not in language " + language.getName());
                    }

                    module.addTag(language.getTag(tagId));
                }
            }
        }
    }

    /**
     * Gets the relationships between the free notes and the modules.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void getFreeNoteModules(Connection conn, Language language)
            throws SQLException {
        for (FreeNote freeNote : language.getFreeNotes()) {
            try (PreparedStatement statement = conn.prepareStatement(
                    "SELECT m.* FROM modules m, freenote_modules fm " +
                    "WHERE fm.freenote_id = ? " +
                    "AND fm.module_id = m.id " +
                    "ORDER BY fm.position")) {
                statement.setString(1, freeNote.getId());
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        // Get the module
                        StorageType type = StorageType.valueOf(rs.getString("type"));
                        String moduleId = rs.getString("id");
                        Module module = language.getModule(type, moduleId);

                        // Add it to the free note
                        freeNote.addModule(module);
                    }
                }
            }
        }
    }

    // Helpers for updating user data

    /**
     * Updates the metadata for the given user.
     * Unpopulated until user metadata is added to data structures.
     * @param conn The connection to the user's database.
     * @param user The object holding user's data.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void updateUserData(Connection conn, Athenia user)
            throws SQLException {
        //noinspection SqlWithoutWhere
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE user_data " +
                "SET username = ?, joined = ?, last_update = ?")) {
            statement.setString(1, "");
            statement.setLong(2, new Date().getTime());
            statement.setLong(3, new Date().getTime());

            statement.execute();
        }
    }

    /**
     * Updates the languages for the given user.
     * @param conn The connection to the user's database.
     * @param user The object holding user's data.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void updateLanguages(Connection conn, Athenia user)
            throws SQLException {
        // Keep track of what languages need to be removed/added
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

        // Add new languages to database
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

        // Remove old languages from database
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

    /**
     * Updates the tags for the given language.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void updateTags(Connection conn, Language language)
            throws SQLException {
        // Keep track of what tags need to be removed/added
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

        // Get current tags in database
        Set<String> allTags = new HashSet<>();
        try (Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(
                        "SELECT id FROM tags")) {
            while (rs.next()) {
                allTags.add(rs.getString(1));
            }
        }

        // Add new tags to database
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO tags " +
                "(id, name) VALUES " +
                "(?, ?)")) {
            for (Tag tag : newTags) {
                if (!allTags.contains(tag.getTag())) {
                    statement.setString(1, tag.getTag());
                    statement.setString(2, tag.getTag());
                    statement.addBatch();
                }
            }

            statement.executeBatch();
        }

        // Add new tags to language
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

        // Remove old tags from database
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

    /**
     * Updates the free notes for the given language.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void updateFreeNotes(Connection conn, Language language)
            throws SQLException {
        // Keep track of what free notes need to be removed/added
        Set<String> oldFreeNotes = new HashSet<>();
        Collection<FreeNote> newFreeNotes = language.getFreeNotes();

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

        // Update all current freenotes metadata in database
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

    /**
     * Updates the tags for the given free note.
     * @param conn The connection to the user's database.
     * @param freeNote The current free note.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void updateFreeNoteTags(Connection conn, FreeNote freeNote, Language language)
            throws SQLException {
        // Keep track of what tags need to be removed/added
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

    /**
     * Gets the modules for the given language.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     * @throws DatabaseParserException when user is improperly formatted.
     */
    private static void updateModules(Connection conn, Language language)
            throws SQLException, DatabaseParserException {
        // Keep track of what modules need to be removed/added
        Collection<Module> newModules = language.getModules();
        Set<String> oldModules = new HashSet<>();

        if (newModules.contains(null)) {
            throw new DatabaseParserException("Null module.");
        }

        // Get old and new modules
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

        // Add new modules to database
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO modules " +
                "(id, type, language_id, created, last_modified) VALUES " +
                "(?, ?, (SELECT id FROM languages WHERE language = ?), ?, ?)")) {
            for (Module module : newModules) {
                statement.setString(1, module.getId());
                statement.setString(2, module.getType().toString());
                statement.setString(3, language.getName());
                statement.setLong(4, module.getDateCreated().getTime());
                // Set date to 0 so updated immediately
                statement.setLong(5, 0);
                statement.addBatch();
            }

            statement.executeBatch();

            // Create the module in the database with empty data
            for (Module module : newModules) {
                createModule(conn, module);
            }
        }

        // Remove old modules from database
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM modules WHERE id = ?")) {
            for (String moduleId : oldModules) {
                statement.setString(1, moduleId);
                statement.addBatch();
            }

            statement.executeBatch();
        }

        // Update each module and its tags
        for (Module module : language.getModules()) {
            updateModule(conn, module);
            updateModuleTags(conn, module, language);
        }
    }

    /**
     * Creates the specific given module in the database
     * with empty data to be filled in with update.
     * @param conn The connection to the database.
     * @param module The current module.
     * @throws DatabaseParserException when module is not one of specified storage types.
     * @throws SQLException when a SQL command is improperly formatted.
     */
    private static void createModule(Connection conn, Module module)
            throws DatabaseParserException, SQLException {
        // Create query
        String query;
        switch (module.getType()) {
            case NOTE:
                query = "INSERT INTO note_modules " +
                        "(module_id, body, rating) VALUES " +
                        "(?, NULL, NULL)";
                break;
            case VOCAB:
                query = "INSERT INTO vocab_modules " +
                        "(module_id, term, definition, rating) VALUES " +
                        "(?, NULL, NULL, NULL)";
                break;
            case CONJUGATION:
                query = "INSERT INTO conjugation_modules " +
                        "(module_id, header, rating, height) VALUES " +
                        "(?, NULL, NULL, NULL)";
                break;
            case QUESTION:
                query = "INSERT INTO question_modules " +
                        "(module_id, body) VALUES " +
                        "(?, NULL)";
                break;
            case ALERT_EXCLAMATION:
                query = "INSERT INTO alert_exclamation_modules " +
                        "(module_id, body) VALUES " +
                        "(?, NULL)";
                break;
            default:
                throw new DatabaseParserException("Bad data: " +
                        "Module does not match any of set types.");
        }

        // Create module
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, module.getId());
            statement.execute();
        }
    }

    /**
     * Updates the specific module if changes have been made.
     * @param conn The connection to the user's database.
     * @param module The module being updated.
     * @throws SQLException when a SQL command is improperly formatted.
     * @throws DatabaseParserException when database is improperly formatted.
     */
    private static void updateModule(Connection conn, Module module)
            throws SQLException, DatabaseParserException {
        // Check if module has been updated since last database update
        Date lastUpdated;
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT last_modified FROM modules WHERE id = ?")) {
            statement.setString(1, module.getId());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseParserException("Bad data: " +
                            "Module not in database.");
                }
                lastUpdated = new Date(rs.getInt("last_modified"));
            }
        }

        // No update if no changes
        if (lastUpdated.equals(module.getDateModified())) {
            return;
        }

        // Update module metadata
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE modules " +
                "SET last_modified = ? " +
                "WHERE id = ?")) {
            statement.setLong(1, module.getDateModified().getTime());
            statement.setString(2, module.getId());
            statement.execute();
        }

        // Update module data
        switch (module.getType()) {
            case NOTE:
                updateNoteModule(conn, (Note) module);
                break;
            case VOCAB:
                updateVocabModule(conn, (Vocab) module);
                break;
            case CONJUGATION:
                updateConjugationModule(conn, (Conjugation) module);
                break;
            case QUESTION:
                updateQuestionModule(conn, (Question) module);
                break;
            case ALERT_EXCLAMATION:
                updateAlertModule(conn, (AlertExclamation) module);
                break;
            default:
                throw new DatabaseParserException("Bad data.");
        }
    }

    /**
     * Updates the data for a Note module.
     * @param conn The connection to the user's database.
     * @param module The module being updated.
     * @throws SQLException when a SQL command is improperly formatted.
     */
    private static void updateNoteModule(Connection conn, Note module)
            throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE note_modules " +
                "SET body = ?, rating = ? " +
                "WHERE module_id = ?")) {
            statement.setString(1, module.getText());
            statement.setInt(2, module.getRating());
            statement.setString(3, module.getId());
            statement.execute();
        }
    }

    /**
     * Updates the data for a Vocab module.
     * @param conn The connection to the user's database.
     * @param module The module being updated.
     * @throws SQLException when a SQL command is improperly formatted.
     */
    private static void updateVocabModule(Connection conn, Vocab module)
            throws SQLException {
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

    /**
     * Updates the data for a Conjugation module.
     * @param conn The connection to the user's database.
     * @param module The module being updated.
     * @throws SQLException when a SQL command is improperly formatted.
     */
    private static void updateConjugationModule(Connection conn, Conjugation module)
            throws SQLException {
        // Update module metadata
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE conjugation_modules " +
                        "SET header = ?, rating = ?, height = ? " +
                        "WHERE module_id = ?")) {
            statement.setString(1, module.getHeader());
            statement.setInt(2, module.getRating());
            statement.setInt(3, module.getHeight());
            statement.setString(4, module.getId());
            statement.execute();
        }

        // Delete all rows of conjugation table
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM conjugation_rows WHERE module_id = ?")) {
            statement.setString(1, module.getId());
        }

        // Add current rows into database
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

    /**
     * Updates the data for a Question module.
     * @param conn The connection to the user's database.
     * @param module The module being updated.
     * @throws SQLException when a SQL command is improperly formatted.
     */
    private static void updateQuestionModule(Connection conn, Question module)
            throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE question_modules " +
                        "SET body = ? " +
                        "WHERE module_id = ?")) {
            statement.setString(1, module.getText());
            statement.setString(2, module.getId());
            statement.execute();
        }
    }

    /**
     * Updates the data for an AlertExclamation module.
     * @param conn The connection to the user's database.
     * @param module The module being updated.
     * @throws SQLException when a SQL command is improperly formatted.
     */
    private static void updateAlertModule(Connection conn, AlertExclamation module)
            throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE alert_exclamation_modules " +
                        "SET body = ? " +
                        "WHERE module_id = ?")) {
            statement.setString(1, module.getText());
            statement.setString(2, module.getId());
            statement.execute();
        }
    }

    /**
     * Updates the tags for the given module.
     * @param conn The connection to the user's database.
     * @param module The module.
     * @param language The language the module belongs to.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void updateModuleTags(Connection conn, Module module, Language language)
            throws SQLException {
        // Keep track of old and new tags
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

    /**
     * Updates the relationships between the free notes and the modules.
     * @param conn The connection to the user's database.
     * @param language The current language.
     * @throws SQLException when a SQL command is improperly executed.
     */
    private static void updateFreeNoteModules(Connection conn, Language language)
            throws SQLException {
        for (FreeNote freeNote : language.getFreeNotes()) {
            // Get the order of modules in the database
            List<String> moduleOrder = new ArrayList<>();

            try (PreparedStatement statement = conn.prepareStatement(
                    "SELECT m.id m_id FROM modules m, freenote_modules fm " +
                    "WHERE fm.freenote_id = ? " +
                    "AND fm.module_id = m.id " +
                    "ORDER BY fm.position")) {

                statement.setString(1, freeNote.getId());
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        moduleOrder.add(rs.getString("m_id"));
                    }
                }
            }

            // Get the order of modules in the free note
            List<String> currentModuleIds =
                    freeNote.getModules()
                            .stream()
                            .map(Module::getId)
                            .collect(Collectors.toList());

            // If they match, no update
            if (moduleOrder.equals(currentModuleIds)) {
                continue;
            }

            // Delete old module order from free note in database
            try (PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM freenote_modules " +
                    "WHERE freenote_id = ?")) {
                statement.setString(1, freeNote.getId());
                statement.execute();
            }

            // Add new module order for free note into database
            try (PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO freenote_modules " +
                    "(freenote_id, module_id, position) VALUES " +
                    "(?, ?, ?)")) {
                for (Module module : freeNote.getModules()) {
                    statement.setString(1, freeNote.getId());
                    statement.setString(2, module.getId());
                    statement.setInt(3, freeNote.getModules().indexOf(module));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
    }
}