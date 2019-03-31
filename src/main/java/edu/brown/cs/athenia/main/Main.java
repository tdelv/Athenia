package edu.brown.cs.athenia.main;

//import joptsimple.OptionParser;

import edu.brown.cs.athenia.driveapi.GoogleDriveApi;

public class Main {

    private static final int DEFAULT_PORT = 4567;

    /**
     * The initial method called when execution begins.
     * @param args
     *          An array of command line arguments
     */
    public static void main(String[] args) {
        new Main(args).run();
    }

    private String[] args;

    private Main(String[] args) {
        this.args = args;
    }

    /**
     * The main logic flow of the program.
     */
    private void run() {
        //runSparkServer(DEFAULT_PORT);
        try {
            GoogleDriveApi.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
