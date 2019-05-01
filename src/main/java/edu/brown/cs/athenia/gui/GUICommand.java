package edu.brown.cs.athenia.gui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

import com.google.api.services.drive.*;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.athenia.databaseparser.*;
import edu.brown.cs.athenia.driveapi.DriveApiException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;
import edu.brown.cs.athenia.data.modules.*;
import edu.brown.cs.athenia.data.modules.module.*;
import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.main.Athenia;
import edu.brown.cs.athenia.review.*;
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

        // WAS: req.session().attribute("loginDestination", "/home");
        // I changed it to go to languages instead of home (Mia)
        req.session().attribute("loginDestination", "/languages");
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
        System.out.println(user.getLanguages());
        variables.put("languages", user.getLanguages());
        successful = true;
        message = "successful";
      } catch (DatabaseParserException e) {
        // else send an error message that user not found
        message = "User not found in database in language prompt handler";
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
          message = "succesfully added language";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database in language add handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request for changing the current language of the user to that
   * specified on the front-end.
   */
  public static class LanguageChangeHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String language = qm.value("language");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        // find and check for user
        Athenia user = DatabaseParser.getUser(userId);
        if (user.getLanguages().contains(language)) {
          // remove language if in user class
          user.setCurrLang(language);
          successful = true;
          message = "successfully changed language";
        } else {
          // else leave message
          message = "language not in user in language change handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in language change handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request handler for removing a language from the user's database.
   */
  public static class LanguageRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String lang = qm.value("language");

      // prepare message to send to front end
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        // find and check for user
        Athenia user = DatabaseParser.getUser(userId);
        if (user.getLanguages().contains(lang)) {
          // remove language if in user class
          user.removeLanguage(lang);
          successful = true;
          message = "successfully removed language";
        } else {
          // else leave message
          message = "language not in user in language remove handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in language remove handler";
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
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          // get information about lang to present on home
          int vocabCount = lang.getModuleCount(StorageType.VOCAB);
          int noteCount = lang.getModuleCount(StorageType.NOTE);
          int conjugationCount = lang.getModuleCount(StorageType.CONJUGATION);

          List<Map<String, Object>> recentList = new ArrayList<>();
          // pull all recent notes from language

          for (FreeNote note : lang.getRecentFreeNotes()) {
            recentList.add(toData(note));
          }

          // add this info to the map
          variables.put("username", ""); // TODO: get the user's name. <3 mia
          variables.put("currentLanguage", lang.getName());
          variables.put("vocabCount", vocabCount);
          variables.put("noteCount", noteCount);
          variables.put("conjugationCount", conjugationCount);
          variables.put("recent", recentList);

          message = "successfully grabbed module information";
          successful = true;
        } else {
          message = "current language null in home page handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in home page handler";
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
   * on the "Vocabulary" page.
   */
  public static class VocabularyPageHandler implements TemplateViewRoute {
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
          Map<String, Module> vocabMap = lang.getModuleMap(StorageType.VOCAB);
          List<Map<String, Object>> vocabList = new ArrayList<>();

          // translate vocab objects to JSON
          for (Map.Entry<String, Module> vocab : vocabMap.entrySet()) {
            vocabList.add(toData((Vocab) vocab.getValue()));
          }

          variables.put("content", vocabList);
          // edit success messages
          successful = true;
          message = "successfully pulled vocab information";
        } else {
          message = "current language null in vocabulary page handler";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database in vocabulary page handler";
      }

      variables.put("title", "Vocabulary");
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "vocab.ftl");
    }
  }

  /**
   * POST request handler for adding a new Vocab object to the user's
   * current Language.
   */
  public static class VocabularyAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String newTerm = qm.value("newTerm");
      String newDef = qm.value("newDef");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {
          // todo : create a new vocab module and add to language - from jason

          // todo : call toData on this and add to variables map - from jason

          successful = true;
          message = "successfully added vocab";
        } else {
          message = "current language null in vocab add handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in vocab add handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);

      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request handler for adding, updating, or deleting vocabulary
   * information. Called whenever the user edits anything on the Vocabulary
   * landing page, a specific vocabulary page, or in a FreeNotes page.
   */
  public static class VocabularyUpdateHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String vocabId = qm.value("vocabId");
      String updatedTerm = qm.value("updatedTerm");
      String updatedDef = qm.value("updatedDef");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          // TODO call update on this module somehow (done with language method?) - from jason

          // TODO toData this updated module object and put in variables - from jason

          successful = true;
          message = "successfully updated vocab";
        } else {
          message = "current language null in vocab update handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in vocab update handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  public static class VocabularyRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String vocabId = qm.value("vocabId");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          // TODO call remove on this module somehow (through language) - from jason

          successful = true;
          message = "successfully removed vocab";
        } else {
          message = "current language null in vocab remove handler";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database in vocab remove handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }


  /**
   * GET request handler which retrieves all conjugation information from the
   * database and formats this information to send to the front end for display
   * on the user's "Conjugation" page.
   */
  public static class ConjugationPageHandler implements TemplateViewRoute {
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

        if (lang != null) {

          Map<String, Module> conjMap = lang.getModuleMap(StorageType.CONJUGATION);
          List<Map<String, Object>> conjList = new ArrayList<>();

          // translate conj objects to JSON
          for (Map.Entry<String, Module> conj : conjMap.entrySet()) {
            conjList.add(toData((Conjugation) conj.getValue()));
          }

          // add content and update success messages
          variables.put("content", conjList);
          successful = true;
          message = "successfully pulled conjugation information";
        } else {
          message = "current language null in conjugation page handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation page handler";
      }

      variables.put("title", "Conjugation");
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "conjugations.ftl");
    }
  }

  public static class ConjugationAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String newHeader = qm.value("header"); // just a string
      String newContent = qm.value("content"); // list of lists of strings

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          // todo : create new vocab module and add to language - from jason
          // todo : call toData on this and add to variables map - from jason

          successful = true;
          message = "successfully added conjugation";
        } else {
          message = "current language null in conjugation add handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation add handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }


  /**
   * POST request handler for adding, updating, or deleting a conjugation module
   * within the database. Called whenever the user makes an edit to any
   * conjugation module which can be found on the following pages: the
   * conjugation landing and individual pages and the FreeNotes landing and
   * individual pages.
   */
  public static class ConjugationUpdateHandler implements Route {
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
   * GET request handler for retrieving information pertaining to a specific tag
   * the user uses and wishes to view all modules categorized by that tag. Pulls
   * this information from the database and formats it to present to the front
   * end.
   */
  public static class TagPageHandler implements TemplateViewRoute {
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

        if (lang != null) {
          Set<Tag> tagSet = lang.getTagSet();
          List<Map<String, Object>> tagList = new ArrayList<>();

          for (Tag tag : tagSet) {
            tagList.add(toData(tag));
          }

          // add tagList to variables map
          variables.put("content", tagSet);

          // edit success messages
          successful = true;
          message = "successful pulled tag information";
        } else {
          message = "current language null in tag page handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in tag page handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);
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
  public static class TagUpdateHandler implements Route {
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
  public static class ModuleTagUpdateHandler implements Route {
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
   * GET request handler which retrieves all free notes information from the
   * database and formats this information to send to the front end for display
   * on the user's "FreeNotes" landing page.
   */
  public static class FreeNotesPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: recognize user wants to visit landing page of FreeNotes
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull out all information about each separate FreeNotes page
      // of the user and format it appropriately for the user to
      // view
      return new ModelAndView(variables, "notes.ftl");
    }
  }

  /**
   * GET request handler for an individual FreeNotes page which the user wishes
   * to access. Pulls out all of the information on that page and organizes it
   * in a way for the front end to display to the user.
   */
  public static class FreeNotesEditorHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      // TODO: send id through the url in the js (for mia from mia lol)
      String noteId = qm.value("id");

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      if (noteId.equals("new")) {
        // send the default values to front end / empty lists and stuff
        variables.put("title", "Note Title");
      } else {
        // TODO: use noteId to find the note in the database
        // send modules and other relevant data (note title, date, etc)
        variables.put("title", "TODO"); // TODO: put note title
      }

      // Will just retain this info in the front end
      variables.put("currentLanguage", "temp");
      variables.put("username", "temp");

      // update any info about last date viewed and stuff if we have it

      // OLD NOTES:
      // TODO: determine which free note the user wants to view and find in
      // database
      // TODO: pull out all of the information (modules, text, etc.) of the
      // landing page from the database, add to appropriate areas in the
      // backend, and format and send to the front end for display
      // > involves a lot of module storing (a cache in both front end and
      // backend?)
      return new ModelAndView(variables.build(), "notePageEdit.ftl");
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
  public static class AddModuleToFreeNotesHandler implements Route {
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
  public static class FreeNotesUpdateHandler implements Route {
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
  public static class ReviewModeLandingHandler implements TemplateViewRoute {
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
  public static class ReviewModeIndividualHandler implements TemplateViewRoute {
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
