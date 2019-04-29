package edu.brown.cs.athenia.databaseparser;

import com.google.common.collect.ImmutableList;
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

    private static final Map<String, Language> USER_MAP = new HashMap<>();

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

    public static Language getUser(String userId) throws DatabaseParserException {
        if (USER_MAP.containsKey(userId)) {
            return USER_MAP.get(userId);
        }

        Language user;

        try (Connection conn = loadConnection(userId)){
            // TODO: NOTE! i hope it was okay to turn these into maps
            //          linking the ID of the module in the database to the module itself
            Map<String, FreeNote> freeNoteMap = loadFreeNoteMap(conn);
            Map<String, Conjugation> conjugationMap = loadConjugationMap(conn);
            Map<String, Note> noteMap = loadNoteMap(conn);
            Map<String, Tag> tagMap = loadTagMap(conn);
            Map<String, Vocab> vocabMap = loadVocabMap(conn);
            user = new Language(freeNoteMap, conjugationMap, noteMap, tagMap, vocabMap);
        } catch (SQLException e) {
            throw new DatabaseParserException(e);
        }

        return USER_MAP.put(userId, user);
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

}
