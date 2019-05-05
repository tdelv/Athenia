package edu.brown.cs.athenia.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.Set;

//import joptsimple.OptionParser;

import com.google.common.collect.ImmutableSet;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;
import edu.brown.cs.athenia.gui.GUICommand;
import freemarker.template.Configuration;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Main class.
 * @author makaylamurphy
 *
 */
public class Main {

  private static final int DEFAULT_PORT = 4569;

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
    ProcessBuilder process = new ProcessBuilder();
    Integer port;
    if (process.environment().get("PORT") != null) {
      port = Integer.parseInt(process.environment().get("PORT"));
    } else {
      port = DEFAULT_PORT;
    }

    runSparkServer(port);
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

    final Set<String> NO_FORCE_LOGIN = ImmutableSet.<String>builder()
            .add("/login")
            .add("/validate")
            .add("/")
            .build();
    Spark.before((req, res) -> {
      String userId = req.session().attribute("user_id");
      if (!NO_FORCE_LOGIN.contains(req.pathInfo())
              && !GoogleDriveApi.isLoggedIn(userId)) {
        req.session().attribute("loginDestination", req.pathInfo());
        res.redirect("/login");
      }
    });

    try {
      Spark.get("/login", new GUICommand.LoginHandler());
      Spark.get("/validate", new GUICommand.ValidateHandler());
    } catch (Exception e) {
      e.printStackTrace();
    }

    Spark.get("/", new GUICommand.LandingPageHandler(), freeMarker);
    Spark.get("/home", new GUICommand.HomePageHandler(), freeMarker);

    // language spark handlers
    Spark.get("/languages", new GUICommand.LanguagePromptHandler(), freeMarker);
    Spark.post("/addNewLanguage", new GUICommand.LanguageAddHandler());
    Spark.post("/removeLanguage", new GUICommand.LanguageRemoveHandler());
    Spark.post("/changeCurrentLanguage", new GUICommand.LanguageChangeHandler());

    // freenotes spark handlers
    Spark.get("/notes", new GUICommand.FreeNotesPageHandler(), freeMarker);
    Spark.get("/noteEditor", new GUICommand.FreeNotesEditorHandler(), freeMarker);

    // vocabulary module specific spark handlers
    Spark.get("/vocabulary", new GUICommand.VocabularyPageHandler(), freeMarker);
    Spark.post("/getVocabList", new GUICommand.getVocabularyModulesHandler());
    Spark.post("/vocabularyAdd", new GUICommand.VocabularyAddHandler());
    Spark.post("/vocabularyUpdate", new GUICommand.VocabularyUpdateHandler());
    Spark.post("/vocabularyRemove", new GUICommand.VocabularyRemoveHandler());

    // conjugation module specific spark handlers
    Spark.get("/conjugations", new GUICommand.ConjugationPageHandler(), freeMarker);
    Spark.post("/conjugationAdd", new GUICommand.ConjugationAddHandler());
    Spark.post("/conjugationEntryAdd", new GUICommand.ConjugationAddEntryHandler());
    Spark.post("/conjugationEntryUpdate", new GUICommand.ConjugationEntryUpdateHandler());
    Spark.post("/conjugationEntryRemove", new GUICommand.ConjugationRemoveEntryHandler());
    Spark.post("/conjugationRemove", new GUICommand.ConjugationRemoveHandler());

    // note module specific spark handlers
    Spark.post("/noteAdd", new GUICommand.NoteAddHandler());
    Spark.post("/noteUpdate", new GUICommand.NoteUpdateHandler());
    Spark.post("/noteRemover", new GUICommand.NoteRemoveHandler());

    // alert exclamation specific spark handlers
    Spark.post("/alertAdd", new GUICommand.AlertAddHandler());
    Spark.post("/alertUpdate", new GUICommand.AlertUpdateHandler());
    Spark.post("/alertRemove", new GUICommand.AlertRemoveHandler());

    // question specific spark handlers
    Spark.post("/questionAdd", new GUICommand.QuestionAddHandler());
    Spark.post("/questionUpdate", new GUICommand.QuestionUpdateHandler());
    Spark.post("/questionRemove", new GUICommand.QuestionRemoveHandler());

    // tag specific spark handlers
    Spark.post("/addTagToModule", new GUICommand.AddTagToModule());
    Spark.post("/removeTagFromModule", new GUICommand.RemoveTagFromModule());

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
