package edu.brown.cs.athenia.gui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.athenia.databaseparser.*;
import edu.brown.cs.athenia.driveapi.DriveApiException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;
import edu.brown.cs.athenia.data.modules.*;
import edu.brown.cs.athenia.data.modules.module.*;
import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.main.Athenia;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.TemplateViewRoute;

import javax.xml.crypto.*;


/**
 * GUICommand will handle GUI commands, FreeMarker methods (gets and posts), and
 * dynamic URLs to account for arbitrary number of "pages".
 * @author makaylamurphy
 *
 */
public class GUICommand {

  private static final Gson GSON = new Gson();
  private GUICommand() { }

  /**
   *
   */
  public static class LandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      // String userId = checkLoggedIn(req, res);
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>().put("title", "landing page").build();

      return new ModelAndView(variables, "landing.ftl");
    }
  }

  /**
   * Handles initial login request, redirecting user to
   * Google Authenication page if not already logged in.
   */
  public static class LoginHandler implements Route {

    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      // Set destination to go after login
      if (req.session().attribute("loginDestination") == null) {
        req.session().attribute("loginDestination", "/home");
      }

      // Check if user token already loaded
      if (GoogleDriveApi.isLoggedIn(req.session().attribute("user_id"))) {
        res.redirect(req.session().attribute("loginDestination"));
      }

      // Create secure state to prevent request forgery
      String state = new BigInteger(130, new SecureRandom()).toString(32);
      req.session().attribute("state", state);

      // Create callback url for authentication
      String host = req.url().replace(req.pathInfo(), "/validate");
      String url = GoogleDriveApi.getUrl(state, host);

      // Redirect to Google authentication page
      res.redirect(url);
      return null;
    }
  }

  /**
   * Handles the redirect from Google Authentication,
   * storing credentials and redirecting user to correct url.
   */
  public static class ValidateHandler implements Route {

    @Override
    public Object handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      // Ensure that this is no request forgery going on, and that the user
      // sending us this connect request is the user that was supposed to.
      if (req.session().attribute("state") == null
              || !qm.value("state").equals((String) req.session().attribute("state"))) {
        res.redirect("/login");
        return null;
      }

      // Remove one-time use state.
      req.session().attribute("state");

      // Create credential and store it with user_id
      String userId = new BigInteger(130, new SecureRandom()).toString(32);
      req.session().attribute("user_id", userId);

      String host = req.url().replace(req.pathInfo(), "/validate");
      GoogleDriveApi.createCredential(userId, qm.value("code"), host);

      // Send user to correct destination after login
      String redirect = req.session().attribute("loginDestination");
      req.session().attribute("loginDestination", null);
      res.redirect(redirect);
      return null;
    }
  }

  /**
   * GET request handler which pulls the different languages the user has so far
   * logged in the app and displays the prompt to choose from the options, if
   * they exist.
   */
  public static class LanguagePromptHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        // try to find the user in database and send the language name to
        // the front-end
        Athenia user = DatabaseParser.getUser(userId);
        variables.put("languages", user.getLanguages());
        successful = true;
        message = "successful";
      } catch (DatabaseParserException e) {
        // else send an error message that user not found
        message = "User not found in database";
      }
      variables.put("title", "Select Language");
      variables.put("successful", successful);
      variables.put("message", message);

      return new ModelAndView(variables.build(), "languages.ftl");
    }
  }

  /**
   * GET request handler which pulls the different languages the user has so far
   * logged in the app and displays the prompt to choose from the options, if
   * they exist.
   */
  public static class LanguageAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String lang = qm.value("newLanguage");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        if (user.getLanguages().contains(lang)) {
          message = "language already exists";
        } else {
          user.addLanguage(lang);
          successful = true;
          message = "language added";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * GET request handler which pulls the most recent activity of the appropriate
   * user and presents this information on the home page of Athenia.
   */
  public static class HomePageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        // TODO be sure that the current language has been set at some point
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          // get information about lang to present on home
          int vocabCount = lang.getVocabCount();
          int noteCount = lang.getNoteCount();
          int conjugationCount = lang.getConjugationCount();

          List<Map<String, Object>> recentList = new ArrayList<>();
          // pull all recent notes from language
          for (FreeNote note : lang.getRecentFreeNotes()) {
            recentList.add(toData(note));
          }

          // add this info to the map
          variables.put("vocabCount", vocabCount);
          variables.put("noteCount", noteCount);
          variables.put("conjugationCount", conjugationCount);
          variables.put("recent", recentList);

          message = "successful";
          successful = true;
        } else {
          message = "current language null";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database";
      }

      variables.put("title", "Home");
      variables.put("successful", successful);
      variables.put("message", message);

      return new ModelAndView(variables.build(), "home.ftl");
    }
  }

  /**
   * GET request handler which pulls all of the vocabulary information saved by
   * the user in the database and formats it to send to the front end to display
   * on the "Vocabulary" landing page.
   */
  public class VocabularyLandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      // have tags as a certain part of frontend
      // --- use data-* thing for storing, filtering tags
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {
          Map<String, Vocab> vocabMap = lang.getVocabMap();
          List<Map<String, Object>> vocabList = new ArrayList<>();

          // translate vocab objects to JSON
          for (Map.Entry<String, Vocab> vocab : vocabMap.entrySet()) {
            vocabList.add(toData(vocab.getValue()));
          }

          // edit success messages
          successful = true;
          message = "successful";
        } else {
          message = "current language null";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database";
      }

      variables.put("title", "Vocabulary");
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "...");
    }
  }

  /**
   * POST request handler for adding, updating, or deleting vocabulary
   * information. Called whenever the user edits anything on the Vocabulary
   * landing page, a specific vocabulary page, or in a FreeNotes page.
   */
  public class UpdateVocabularyHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String type = qm.value("type");
      if (type.equals("add")) {
        Vocab vocab = null;
        // TODO: create information of the vocab and create object
//        language.addVocab(vocab);
      } else if (type.equals("update")) {
        // TODO: get ID of vocab module
        String id = "...";
//        language.updateVocabulary(id);
      } else if (type.equals("delete")) {
        // TODO: get ID of vocab module
        String id = "...";
//        language.deleteVocabulary(id);
      } else {
        // TODO: throw some type of error that vocab doesn't exist
      }
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO execute the appropriate operation:
      // 1. add new vocab module to appropriate database tables
      // 2. update vocab module within appropriate database tables
      // 3. delete vocab module from all database tables
      // a. deletion can send confirmation to user
      return GSON.toJson(variables);
    }
  }

  /**
   * GET request handler for retrieving information pertaining to a specific tag
   * the user uses and wishes to view all modules categorized by that tag. Pulls
   * this information from the database and formats it to present to the front
   * end.
   */
  public class TagLandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();



      } catch (DatabaseParserException e) {
        message = "error getting user from database";
      }


      // pull all tag information
      List<Map<String, Object>> tags = new ArrayList<>();
//      for (Map.Entry<String, Tag> tag : language.getTagMap().entrySet()) {
//        tags.add(toData(tag.getValue()));
//      }
      // prepare tag info to present to front-end
      variables.put("content", tags);
      return new ModelAndView(variables.build(), "...");
    }
  }

  /**
   * POST request handler for adding, updating, or deleting a tag and its
   * information. Called whenever the user edits a tag on any of the pages,
   * including the vocabulary landing and individual pages, the conjugation
   * landing and individual pages, and the FreeNotes landing and individual
   * pages. Specifically used for updating tag information and not for adding or
   * removing tags from a module.
   */
  public class UpdateTagHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      String type = qm.value("type");
      if (type.equals("add")) {
        Tag tag = null; // TODO: generate a new tag object and add to database
//        language.addTag(tag);
      } else if (type.equals("update")) {
        String id = "..."; // TODO: get ID of tag to update (aka to change name or add element to?)
//        language.updateTag(id);
      } else if (type.equals("delete")) {
        String id = "..."; // TODO: get ID of tag to delete from everything
//        language.deleteTag(id);
      } else {
        // TODO: throw an error message that tag doesn't exist
      }

      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: execute the appropriate operation to insert, update, or delete
      // the tag's information to the database
      // 1. add tag module to appropriate database tables
      // 2. update tag module within appropriate database tables
      // 1. delete tag module from all database tables
      // a. deletion can send confirmation alert to user
      return GSON.toJson(variables);
    }
  }

  /**
   * POST request handler for updating a module's tag set, particularly adding
   * or removing a tag to or from the module. Called wherever a module's tag can
   * be edited -- including on the vocabulary, conjugation, and FreeNotes
   * landing, and more importantly, individual pages as well as the tag landing
   * and individual pages.
   */
  public class UpdateModuleTagHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      String type = qm.value("type");

//      if (language != null) {
//        if (type.equals("add")) {
//
//        } else if (type.equals("update")) {
//
//        } else if (type.equals("delete")) {
//
//        } else {
//          // TODO: throw error
//        }
//      }
      // TODO pull in the information of the module, the tag, and the operation
      // wanted:
      // 1. add tag to the module
      // 3. delete tag from the module
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: update the appropriate tables in the database:
      // 1. adding tag to module, add to that module's tag set
      // 3. delete tag, remove tag from module's tag set
      // > confirmations can be sent
      // > be sure to handle edits to a tag page itself and do not
      // present the information if the user chooses to delete it
      return GSON.toJson(variables);
    }
  }

  /**
   * GET request handler which retrieves all conjugation information from the
   * database and formats this information to send to the front end for display
   * on the user's "Conjugation" landing page.
   */
  public class ConjugationLandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: recognize that user is requesting to view the conjugation page
      // (not really anything to parse out)
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull all conjugation modules from the database and format
      // appropriately to present to the user
      return new ModelAndView(variables, "...");
    }
  }

  /**
   * POST request handler for adding, updating, or deleting a conjugation module
   * within the database. Called whenever the user makes an edit to any
   * conjugation module which can be found on the following pages: the
   * conjugation landing and individual pages and the FreeNotes landing and
   * individual pages.
   */
  public class UpdateConjugationHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO pull out the information of the conjugation module and act on it
      // accordingly:
      // 1. adding a new conjugation module
      // 2. updating old conjugation module
      // 3. deleting old conjugation module
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO execute the appropriate operation:
      // 1. add new conjugation module to appropriate database tables
      // 2. update conjugation module within appropriate database tables
      // 3. delete conjugation module from all database tables
      // a. deletion can send confirmation alert to user
      return GSON.toJson(variables);
    }
  }

  /**
   * GET request handler which retrieves all free notes information from the
   * database and formats this information to send to the front end for display
   * on the user's "FreeNotes" landing page.
   */
  public class FreeNotesLandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: recognize user wants to visit landing page of FreeNotes
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull out all information about each separate FreeNotes page
      // of the user and format it appropriately for the user to
      // view
      return new ModelAndView(variables, "...");
    }
  }

  /**
   * GET request handler for an individual FreeNotes page which the user wishes
   * to access. Pulls out all of the information on that page and organizes it
   * in a way for the front end to display to the user.
   */
  public class FreeNotesIndividualPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: determine which free note the user wants to view and find in
      // database
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull out all of the information (modules, text, etc.) of the
      // landing page from the database, add to appropriate areas in the
      // backend, and format and send to the front end for display
      // > involves a lot of module storing (a cache in both front end and
      // backend?)
      return new ModelAndView(variables, "...");
    }
  }

  /**
   * POST request handler for adding a new module to an individual FreeNotes
   * page. These modules include a vocabulary module, a conjugation module, a
   * text box, or a note. Generates this module in the backend, adds it to the
   * areas in the backend where appropriate and inserts the information into the
   * database. Sends to the front-end the information necessary for the user to
   * use that module.
   */
  public class AddModuleToFreeNotesHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: determine which module (vocab, conjugation, text, etc.) the
      // user wishes to create
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: generate the information for that module and add to the database
      // for accessing with the above methods
      return GSON.toJson(variables);
    }
  }

  /**
   * POST request handler for updating the information on an existing FreeNote
   * page. Searches through the updates made and updates all of the areas in the
   * backend. Called sporadically as an auto save feature.
   */
  public class UpdateFreeNotesHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: pull out all information on this FreeNotes page
      // > do so either entirely (all information)
      // > or as updated and edited (adding, updating, deleting, etc.)
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: figure out which execution to run:
      // 1. add new freenotes page (title, tags, date created, date accessed,
      // date edited, etc.)
      // 2. update the information of the specific freenotes page
      // 2. delete the information on the freenotes page
      // a. need to decide if everything in it is deleted everywhere else
      return GSON.toJson(variables);
    }
  }

  /**
   * GET request handler for the Review landing page which pulls all of the tags
   * the user has created from the database and backend and formats this
   * information to send to the front-end for the user to choose from the
   * different options that they want to review. Information pulled includes the
   * tag names, the date they were created, the date they were edited, and their
   * rating.
   */
  public class ReviewModeLandingHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: grab all tags and format to send to front end
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: grab all tags, their date created/started, date edited
      // format to send to front end
      return new ModelAndView(variables, "...");
    }
  }

  /**
   * GET request handler for an individual Review page which pulls all of
   * the information of the modules and tags the user has chosen to review,
   * packages it, and formats it to send to the front-end to display to the
   * user. Retrieves all information including content, type, and rating.
   * Sends all of this info to the front-end in the ordered rating
   * according to the algorithm.
   */
  public class ReviewModeIndividualHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: parse out the options the user has chosen to review
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull out this information from the backend and format
      // to send to the user to review
      // > sends all of this info to the front end which then decides
      // how to present this info according to the ratings of the
      // modules
      return new ModelAndView(variables, "...");
    }
  }

  //--- Class info to JSON methods -------------------------------------------

  /**
   * Converts a FreeNote into a data map for JSON.
   * @param note the FreeNote object to convert
   * @return a map of data from the FreeNote object
   */
  private static Map<String, Object> toData(FreeNote note) {

    // TODO: get the id, name/title, dates, tags associate with this
    ImmutableMap.Builder<String, Object> noteData =
            new ImmutableMap.Builder<String, Object>();
    noteData.put("modtype", "FreeNote");
    noteData.put("title", note.getTitle());

    // add all module data
    List<Map<String, Object>> modulesList = new ArrayList<>();
    for (Module m : note.getModules()) {
      if (m instanceof Vocab) {
        Vocab vocab = (Vocab) m;
        modulesList.add(toData(vocab));
      } else if (m instanceof Conjugation) {
        Conjugation conjugation = (Conjugation) m;
        modulesList.add(toData(conjugation));
      }
    }

    // put module data into map
    noteData.put("content", modulesList);
    return noteData.build();
  }

  /**
   * Converts a Vocab module into a data map for JSON.
   * @param vocab the Vocab object to convert
   * @return a map of data from the Vocab object
   */
  private static Map<String, Object> toData(Vocab vocab) {
    // TODO: get vocab content (getContent())
    // TODO
    ImmutableMap.Builder<String, Object> vocabData =
            new ImmutableMap.Builder<String, Object>();
    // pull information of vocab
    vocabData.put("modtype", "Vocab");
    toData(vocab, vocabData);
    vocabData.put("content", vocab.getContent());
    return vocabData.build();
  }

  /**
   * Converts a Conjugation module into a data map for JSON.
   * @param conjugation the Conjugation module to convert
   * @return a map of data from the FreeNote object
   */
  private static Map<String, Object> toData(Conjugation conjugation) {
    ImmutableMap.Builder<String, Object> conjugationData =
            new ImmutableMap.Builder<String, Object>();
    // pull information of conjugation table
    conjugationData.put("modtype", "Conjugation");
    toData(conjugation, conjugationData);
    conjugationData.put("content", conjugation.getContent());
    return conjugationData.build();
  }

  /**
   * Converts a Tag module into a data map for JSON.
   * @param tag the Tag module to convert
   * @return a map of data from the Tag object
   */
  private static Map<String, Object> toData(Tag tag) {
    ImmutableMap.Builder<String, Object> tagData =
            new ImmutableMap.Builder<String, Object>();
    tagData.put("modtype", "tag");
    tagData.put("content", tag.getContent());
    return tagData.build();
  }

  // TODO some way to add information from a module in generic way?
  private static void toData(Module module,
                             ImmutableMap.Builder<String, Object> map) {
    map.put("id", module.getId()); //TODO get the module id
    map.put("dateCreated", module.getDateCreated());
    map.put("dateModified", module.getDateModified());
    map.put("tags", module.getTags());
  }

}
