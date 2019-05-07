package edu.brown.cs.athenia.updaterscheduler;

import edu.brown.cs.athenia.databaseparser.DatabaseParser;
import edu.brown.cs.athenia.databaseparser.DatabaseParserException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;

import java.util.*;

/**
 * Used to automatically update a user every period of time,
 * as well as purge inactive users from memory and server storage.
 * Functionality:
 *  - Create a scheduler for a user
 *    - If the user already has one, updates the time last updated
 *  - Delete a user's scheduler
 * @author Thomas Del Vecchio
 */
public class UpdaterScheduler extends TimerTask {

    private static Timer timer = new Timer();
    private static Map<String, UpdaterScheduler> schedulers = new HashMap<>();

    private String userId;
    private Date lastUpdate;

    /**
     * Creates a new scheduler for a user.
     * If the user already has a scheduler, resets the lastUpdate to current time.
     * @param userId The user's id.
     */
    public static void create(String userId) {
        // Don't create schedulers for null users
        if (userId == null) {
            return;
        }

        // Update lastUpdate for already existing users
        if (schedulers.containsKey(userId)) {
            schedulers.get(userId).lastUpdate = new Date();
            return;
        }

        // Create a new scheduler
        if (DatabaseParser.hasUser(userId)) {
            UpdaterScheduler task = new UpdaterScheduler(userId);
            schedulers.put(userId, task);
            timer.schedule(task, 30 * 1000, 30 * 1000);
        }
    }

    /**
     * Deletes a user's scheduler.
     * @param userId The user's id.
     */
    public static void delete(String userId) {
        // Get the scheduler
        UpdaterScheduler task = schedulers.get(userId);

        // Delete the program memory references to user
        DatabaseParser.deleteUser(userId);
        GoogleDriveApi.deleteDataBaseFile(userId);

        // Delete the scheduler
        task.cancel();
        schedulers.remove(userId);
    }

    /**
     * Constructor for a new scheduler (used only locally).
     * @param userId The user's id.
     */
    private UpdaterScheduler(String userId) {
        super();

        this.userId = userId;
        this.lastUpdate = new Date();

        schedulers.put(userId, this);
    }

    @Override
    public void run() {
        // Try to update database
        try {
            DatabaseParser.updateUser(this.userId);
        } catch (DatabaseParserException e) {
            e.printStackTrace();
        }

        // Check if user should be purged
        Date currentTime = new Date();
        if (currentTime.getTime() - lastUpdate.getTime() > 5 * 60 * 1000) {
            delete(this.userId);
        }
    }
}
