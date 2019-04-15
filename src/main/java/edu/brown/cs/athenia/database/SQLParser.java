package edu.brown.cs.athenia.database;

import com.google.common.collect.ImmutableList;
import edu.brown.cs.athenia.data.modules.module.Vocab;

import java.sql.Connection;
import java.util.List;

/**
 * SQLParser will hold our SQL parsing information.
 * @author makaylamurphy
 *
 */
public class SQLParser {
    private static Connection loadConnection(String userAuth) {

        return null;
    }

    public static void setup(String userAuth) {

    }

    public static List<Vocab> getVocab(String userAuth) {
        Connection conn = loadConnection(userAuth);

        return ImmutableList.of();
    }
}
