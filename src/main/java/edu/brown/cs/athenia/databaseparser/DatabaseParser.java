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

    private static Connection loadConnection(String userAuth) throws DatabaseParserException, SQLException {
        File file;
        try {
            file = GoogleDriveApi.getDataBase(userAuth);
        } catch (DriveApiException e) {
            throw new DatabaseParserException(e);
        }

        if (!file.exists()) {
            try {
                setup(userAuth, file);
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

    public static Language getUser(String userAuth) throws DatabaseParserException {
        if (USER_MAP.containsKey(userAuth)) {
            return USER_MAP.get(userAuth);
        }

        Language user;

        try (Connection conn = loadConnection(userAuth)){
            List<FreeNote> freeNoteList = loadFreeNoteList(conn);
            List<Conjugation> conjugationList = loadConjugationList(conn);
            List<Note> noteList = loadNoteList(conn);
            List<Tag> tagList = loadTagList(conn);
            List<Vocab> vocabList = loadVocabList(conn);
            user = new Language(freeNoteList, conjugationList, noteList, tagList, vocabList);
        } catch (SQLException e) {
            throw new DatabaseParserException(e);
        }

        return USER_MAP.put(userAuth, user);
    }

    private static List<FreeNote> loadFreeNoteList(Connection conn) throws SQLException {
        return null;
    }

    private static List<Conjugation> loadConjugationList(Connection conn) throws SQLException {
        return null;
    }

    private static List<Note> loadNoteList(Connection conn) throws SQLException {
        return null;
    }

    private static List<Tag> loadTagList(Connection conn) throws SQLException {
        return null;
    }

    private static List<Vocab> loadVocabList(Connection conn) throws SQLException {
        return null;
    }

}
