package edu.brown.cs.athenia.databaseparser;

import edu.brown.cs.athenia.driveapi.UnauthenticatedUserException;
import edu.brown.cs.athenia.main.Athenia;
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
import java.util.Map;

/**
 * DatabaseParser will hold our SQL parsing information.
 * @author makaylamurphy
 *
 */
public class DatabaseParser {

    private static final Map<String, Athenia> USER_MAP = new HashMap<>();


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
        if (USER_MAP.containsKey(userId)) {
            return USER_MAP.get(userId);
        }

        Athenia user = new Athenia(userId);
        // TODO: setup user
        return USER_MAP.put(userId, user);
    }
}
