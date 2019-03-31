package edu.brown.cs.athenia.main;

import edu.brown.cs.athenia.driveapi.DriveQuickstart;

public class Main {

    /**
     * Main function for Athenia project. Sets up Drive API and server.
     * @param args the args passed in from command line.
     */
    public static void main(String... args){
        try {
            DriveQuickstart.setup();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
