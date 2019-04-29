package edu.brown.cs.athenia.databaseparser;

import com.google.common.collect.ImmutableList;
import edu.brown.cs.athenia.main.Athenia;
import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.FreeNote;
import edu.brown.cs.athenia.data.modules.module.Conjugation;
import edu.brown.cs.athenia.data.modules.module.Note;
import edu.brown.cs.athenia.data.modules.module.Tag;
import edu.brown.cs.athenia.data.modules.module.Vocab;
import edu.brown.cs.athenia.driveapi.DriveApiException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DatabaseParser will hold our SQL parsing information.
 * @author makaylamurphy
 *
 */
public class DatabaseParser {

    // TODO: can you change this to athenia because a user can have multiple languages
    //          but only have one athenia?
    private static final Map<String, Language> USER_MAP = new HashMap<>();
    private static final Map<String, Athenia> USER_MAP_UPDATED = new HashMap<>();

    // TODO: how are users being stored now? here it says the user map
    //          is just the user id to the language, but then the
    //          Athenia class has a list of strings to languages
    //      > which one is which?

    private static Connection loadConnection(String userId) throws DatabaseParserException, SQLException {
        File file;
        try {
            file = GoogleDriveApi.getDataBase(userId);
        } catch (DriveApiException e) {
            throw new DatabaseParserException(e);
        }

        if (!file.exists()) {
            try {
                setup(userId, file);
            } catch (IOException e) {
                throw new DatabaseParserException(e);
            }
        }

        return DriverManager.getConnection(file.getPath());
    }

    private static void setup(String userAuth, File file) throws IOException, DatabaseParserException, SQLException {
        file.createNewFile();
        try (Connection conn = DriverManager.getConnection(file.getPath());
                Statement stmt = conn.createStatement()) {
        }
    }

    public static Athenia getUser(String userId) throws DatabaseParserException {
        if (USER_MAP_UPDATED.containsKey(userId)) {
            return USER_MAP_UPDATED.get(userId);
        } else {
            Athenia user = new Athenia(userId);
            // TODO : create a new athenia object
            return USER_MAP_UPDATED.put(userId, user);
        }
    }

//    public static Language getUser(String userId) throws DatabaseParserException {
//        if (USER_MAP.containsKey(userId)) {
//            return USER_MAP.get(userId);
//        }
//
//        Language user;
//
//        try (Connection conn = loadConnection(userId)){
//            // TODO: NOTE! i hope it was okay to turn these into maps
//            //          linking the ID of the module in the database to the module itself
//            String name = loadLanguageName(conn);
//            Map<String, FreeNote> freeNoteMap = loadFreeNoteMap(conn);
//            Map<String, Conjugation> conjugationMap = loadConjugationMap(conn);
//            Map<String, Note> noteMap = loadNoteMap(conn);
//            Map<String, Tag> tagMap = loadTagMap(conn);
//            Map<String, Vocab> vocabMap = loadVocabMap(conn);
//            user = new Language(name, freeNoteMap, conjugationMap, noteMap, tagMap, vocabMap);
//        } catch (SQLException e) {
//            throw new DatabaseParserException(e);
//        }
//
//        return USER_MAP.put(userId, user);
//    }

    // TODO : add some way to get the language name
    private static String loadLanguageName(Connection conn) throws SQLException {
        return null;
    }

    private static Map<String, FreeNote> loadFreeNoteMap(Connection conn) throws SQLException {
        return null;
    }

    private static Map<String, Conjugation> loadConjugationMap(Connection conn) throws SQLException {
        return null;
    }

    private static Map<String, Note> loadNoteMap(Connection conn) throws SQLException {
        return null;
    }

    private static Map<String, Tag> loadTagMap(Connection conn) throws SQLException {
        return null;
    }

    private static Map<String, Vocab> loadVocabMap(Connection conn) throws SQLException {
        return null;
    }


    /**
     * ADDERS HEHHEHHEHEHEHEHEHEEe
     */

    // TODO TODO TODO TODO
    //      so... i'm unsure where the new module objects can be made...
    //          right now i'm thinking they can be made in the Language class
    //          and then sent here to add
    //              there are getter and setters for the module id so
    //              idk if that alleviates anything :)

    public static String addFreeNote(String userId, FreeNote note) {
        // TODO add to the database and return the new ID of this freenote in the database
        //              to store in the map

        return null;
    }

    public static String addConjugation(String userId, Conjugation conjugation) {
        // TODO add to database and return new ID of this freenote
        //          to store in the map
        return null;
    }

    public static String addVocab(String userId, Vocab vocab) {
        // TODO : same thing as above
        return null;
    }

    public static String addNote(String userId, Note note) {
        // TODO : same thing plzzzz
        return null;
    }

}
