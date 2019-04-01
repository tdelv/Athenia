package edu.brown.cs.athenia.main;

//import joptsimple.OptionParser;

import edu.brown.cs.athenia.driveapi.GoogleDriveApi;
import freemarker.template.Configuration;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


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


    // Adding GUI

    /**
     * Creates the engine to run the Spark server.
     * @return The FreeMarkerEngine to run the server.
     */
    private static FreeMarkerEngine createEngine() {
        Configuration config = new Configuration(); // Configuration.VERSION_2_3_25);
        File templates = new File("src/main/resources/spark/template/freemarker");
        try {
            config.setDirectoryForTemplateLoading(templates);
        } catch (IOException ioe) {
            System.out.printf("ERROR: Unable use %s for template loading.%n",
                    templates);
            System.exit(1);
        }
        return new FreeMarkerEngine(config);
    }


    /**
     * Open the server on the local port.
     * @param port
     *          The port for server to run on.
     */
    @SuppressWarnings("unchecked")
    private void runSparkServer(int port) {
        Spark.port(port);
        Spark.externalStaticFileLocation("src/main/resources/static");
        Spark.exception(Exception.class, new ExceptionPrinter());

        FreeMarkerEngine freeMarker = createEngine();

        // Setup Spark Routes
        //Spark.get("/stars", new StarsHandlers.FrontHandler(), freeMarker);
    }

    /**
     * Display an error page when an exception occurs in the server.
     * @author Thomas Del Vecchio
     */
    private static class ExceptionPrinter implements ExceptionHandler {
        @Override
        public void handle(Exception e, Request req, Response res) {
            res.status(500);
            StringWriter stacktrace = new StringWriter();
            try (PrintWriter pw = new PrintWriter(stacktrace)) {
                pw.println("<pre>");
                e.printStackTrace(pw);
                pw.println("</pre>");
            }
            res.body(stacktrace.toString());
        }
    }
}
