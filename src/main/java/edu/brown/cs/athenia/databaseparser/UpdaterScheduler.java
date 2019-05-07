package edu.brown.cs.athenia.databaseparser;

import edu.brown.cs.athenia.driveapi.GoogleDriveApi;

import java.util.*;

public class UpdaterScheduler extends TimerTask {

    private static Timer timer = new Timer();
    private static Map<String, UpdaterScheduler> schedulers = new HashMap<>();

    private String userId;
    private Date lastUpdate;

    public static void create(String userId) {
        if (userId == null) {
            return;
        }

        if (schedulers.containsKey(userId)) {
            schedulers.get(userId).lastUpdate = new Date();
            return;
        }

        UpdaterScheduler task = new UpdaterScheduler(userId);
        schedulers.put(userId, task);
        timer.schedule(task, 30 * 1000, 30 * 1000);
    }

    public static void delete(String userId) {
        UpdaterScheduler task = schedulers.get(userId);

        DatabaseParser.deleteUser(userId);
        GoogleDriveApi.deleteDataBaseFile(userId);

        task.cancel();
        schedulers.remove(userId);
    }

    private UpdaterScheduler(String userId) {
        super();

        this.userId = userId;
        this.lastUpdate = new Date();

        schedulers.put(userId, this);
    }

    @Override
    public void run() {
        try {
            DatabaseParser.updateUser(this.userId);
        } catch (DatabaseParserException e) {
            e.printStackTrace();
        }

        Date currentTime = new Date();
        if (currentTime.getTime() - lastUpdate.getTime() > 5 * 60 * 1000) {
            delete(this.userId);
        }
    }
}
