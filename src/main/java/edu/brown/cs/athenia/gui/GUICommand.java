package edu.brown.cs.athenia.gui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.athenia.data.FreeNote;
import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.Pair;
import edu.brown.cs.athenia.data.modules.Tag;
import edu.brown.cs.athenia.data.modules.module.AlertExclamation;
import edu.brown.cs.athenia.data.modules.module.Conjugation;
import edu.brown.cs.athenia.data.modules.module.Note;
import edu.brown.cs.athenia.data.modules.module.Question;
import edu.brown.cs.athenia.data.modules.module.StorageType;
import edu.brown.cs.athenia.data.modules.module.Vocab;
import edu.brown.cs.athenia.databaseparser.DatabaseParser;
import edu.brown.cs.athenia.databaseparser.DatabaseParserException;
import edu.brown.cs.athenia.driveapi.DriveApiException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;
import edu.brown.cs.athenia.main.Athenia;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.TemplateViewRoute;

/**
 * GUICommand will handle GUI commands, FreeMarker methods (gets and posts), and
 * dynamic URLs to account for arbitrary number of "pages".
 * @author makaylamurphy
 *
 */
public class GUICommand {

  private static final Gson GSON = new Gson();

  private GUICommand() {
  }

  /**
   *
   */
  public static class LandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
      // String userId = checkLoggedIn(req, res);
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "landing page").build();

      return new ModelAndView(variables, "landing.ftl");
    }
  }

  /**
   * Handles initial login request, redirecting user to Google Authenication
   * page if not already logged in.
   */
  public static class LoginHandler implements Route {

    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
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
   * Handles the redirect from Google Authentication, storing credentials and
   * redirecting user to correct url.
   */
  public static class ValidateHandler implements Route {

    @Override
    public Object handle(Request req, Response res) throws Exception {
      QueryParamsMap qm = req.queryMap();
      // Ensure that this is no request forgery going on, and that the user
      // sending us this connect request is the user that was supposed to.
      if (req.session().attribute("state") == null || !qm.value("state")
          .equals((String) req.session().attribute("state"))) {
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

  /*
   * -------------------------------------------------------------------------
   * -- LANGUAGE HANDLERS ----------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * GET request handler which pulls the different languages the user has so far
   * logged in the app and displays the prompt to choose from the options, if
   * they exist.
   */
  public static class LanguagePromptHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
      String userId = req.session().attribute("user_id");
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

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

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

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

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

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

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

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

  /*
   * -------------------------------------------------------------------------
   * -- HOMEPAGE HANDLERS ----------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * GET request handler which pulls the most recent activity of the appropriate
   * user and presents this information on the home page of Athenia.
   */
  public static class HomePageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

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

  /*
   * -------------------------------------------------------------------------
   * -- VOCAB HANDLERS -------------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * GET request handler which pulls all of the vocabulary information saved by
   * the user in the database and formats it to send to the front end to display
   * on the "Vocabulary" page.
   */
  public static class VocabularyPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
      String userId = req.session().attribute("user_id");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        variables.put("username", ""); // TODO get the name

        if (lang != null) {
          Map<String, Module> vocabMap = lang.getModuleMap(StorageType.VOCAB);
          List<Map<String, Object>> vocabList = new ArrayList<>();

          variables.put("currentLanguage", lang.getName());

          // translate vocab objects to JSON
          for (Map.Entry<String, Module> vocab : vocabMap.entrySet()) {
            vocabList.add(toData((Vocab) vocab.getValue()));
          }

          variables.put("vocabContent", vocabList);
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
   * POST request handler for adding a new Vocab object to the user's current
   * Language.
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

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {
          Vocab newVocab = new Vocab(newTerm, newDef);
          lang.addModule(StorageType.VOCAB, newVocab);

          System.out.println("term: " + newVocab.getPair().getTerm());
          System.out.println("def: " + newVocab.getPair().getDefinition());

          System.out.println("to data: " + toData(newVocab));

          // call to data on new object
          variables.put("newVocabModule", toData(newVocab));

          // edit success message
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
      // successful messages
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        if (lang != null) {
          if (lang.getModule(StorageType.VOCAB, vocabId) != null) {
            // update the vocab
            Vocab vocabToUpdate = (Vocab) lang.getModule(StorageType.VOCAB,
                vocabId);
            vocabToUpdate.getPair().updatePair(updatedTerm, updatedDef);
            // convert to JSON for frontend
            variables.put("updatedVocabModule", toData(vocabToUpdate));
            // update successful message
            successful = true;
            message = "updated vocab module successful";
          } else {
            message = "vocab module not in language module map in vocab update handler";
          }
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

  /**
   * POST request for removing a Vocab object from the user globally.
   */
  public static class VocabularyRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String vocabId = qm.value("vocabId");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        if (lang != null) {
          if (lang.getModule(StorageType.VOCAB, vocabId) != null) {
            Vocab vocabToRemove = (Vocab) lang.getModule(StorageType.VOCAB,
                vocabId);
            lang.removeModule(StorageType.VOCAB, vocabToRemove);
            successful = true;
            message = "successfully removed vocab";
          } else {
            message = "vocab module does not exist in language modmap";
          }
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

  /*
   * -------------------------------------------------------------------------
   * -- CONJUGATION HANDLERS -------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * GET request handler which retrieves all conjugation information from the
   * database and formats this information to send to the front end for display
   * on the user's "Conjugation" page.
   */
  public static class ConjugationPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
      String userId = req.session().attribute("user_id");
      // successful messages
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        variables.put("username", ""); // TODO get username

        if (lang != null) {
          Map<String, Module> conjMap = lang
              .getModuleMap(StorageType.CONJUGATION);
          List<Map<String, Object>> conjList = new ArrayList<>();

          variables.put("currentLanguage", lang.getName());

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

  /**
   * POST request which adds a completely new conjugation module.
   */
  public static class ConjugationAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String conjugationId = qm.value("conjId");
      String newHeader = qm.value("header"); // just a string
      String newContent = qm.value("content"); // list of lists of strings

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          Conjugation conjToAdd = new Conjugation();
          conjToAdd.setHeader(newHeader);

          // TODO : parse out how newContent is formatted and translate

          // TODO : add content to conjToAdd and throw into map to present to
          // front-end

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
   * POST request for adding a conjugation entry to a preexisting conjugation
   * table.
   */
  public static class ConjugationAddEntryHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      // conjugation add information
      String conjId = qm.value("conjId");
      String indexToAddAt = qm.value("indexToAddAt");
      String termToAdd = qm.value("termToAdd");
      String defToAdd = qm.value("defToAdd");
      // successful information
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current language not null
        if (lang != null) {
          // check if user map has the module in it
          if (lang.getModule(StorageType.CONJUGATION, conjId) != null) {
            Conjugation conjToAddTo = (Conjugation) lang
                .getModule(StorageType.CONJUGATION, conjId);
            // try to parse indexToAddAt into integer
            try {
              int indexToAddAtInt = Integer.parseInt(indexToAddAt);
              // check for index out of bounds
              if (indexToAddAtInt < 0
                  || indexToAddAtInt >= conjToAddTo.getTable().size()) {
                conjToAddTo.add(termToAdd, defToAdd, indexToAddAtInt);
                successful = true;
                message = "successfully added entry to conjugation table";

                // catch if index out of bounds
              } else {
                message = "index out of bounds in conjugation add entry handler";
              }
              // catch number format exception
            } catch (NumberFormatException e) {
              message = "index to add at not an int in conjugation add entry handler";
            }
            // catch if conjugation module not in map
          } else {
            message = "conjugation module not in user map";
          }
          // catch if current language is null
        } else {
          message = "current language null in conjugation add entry handler";
        }
        // catch if user not in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation add entry handler";
      }
      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request which handles any edits made to a preexisting conjugation
   * table's entries.
   */
  public static class ConjugationEntryUpdateHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      // information pulled from front-end
      String conjId = qm.value("conjId");
      String indexToUpdateStr = qm.value("updateIndex");
      String updatedTerm = qm.value("updatedTerm");
      String updatedDef = qm.value("updatedDef");
      // successful messages
      String message = "";
      boolean successful = true;

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to pull the user info from the database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        if (lang != null) {
          // check if module in map
          if (lang.getModule(StorageType.CONJUGATION, conjId) != null) {
            Conjugation conjToUpdate = (Conjugation) lang
                .getModule(StorageType.CONJUGATION, conjId);
            List<Pair> conjToUpdatePairs = conjToUpdate.getTable();
            // try to parse index into integer
            try {
              int indexToUpdateInt = Integer.parseInt(indexToUpdateStr);
              // check for index out of bounds
              if (indexToUpdateInt < 0
                  || indexToUpdateInt >= conjToUpdatePairs.size()) {
                message = "index out of bounds in conjugation update handler";
              } else {
                // else finally do the update
                conjToUpdate.update(updatedTerm, updatedDef, indexToUpdateInt);
                variables.put("updatedConjModule", toData(conjToUpdate));
                successful = true;
                message = "successfully updated conjugation entry";
              }
              // catch a number format exception on passed in index
            } catch (NumberFormatException e) {
              message = "index of conjugation entry not a number";
            }
            // catch if conjugation module is not in the map
          } else {
            message = "conjugation module not in language module map";
          }
          // catch if current user language is null
        } else {
          message = "current language null in conjugation update handler";
        }
        // catch if user not properly found in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation update handler";
      }

      // put successful variables in map and send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables);
    }
  }

  /**
   * POST request handler for removing a single entry from a conjugation table.
   */
  public static class ConjugationRemoveEntryHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // conjugation remove information
      String conjId = qm.value("conjId");
      String indexToRemoveStr = qm.value("indexToRemove");

      // successful messages
      boolean successful = false;
      String message = "";

      // try to pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current language not null
        if (lang != null) {
          if (lang.getModule(StorageType.CONJUGATION, conjId) != null) {
            Conjugation conjToRemoveFrom = (Conjugation) lang
                .getModule(StorageType.CONJUGATION, conjId);
            // try for parsing out int of index
            try {
              int indexToRemoveInt = Integer.parseInt(indexToRemoveStr);
              // check for index out of bounds error
              if (indexToRemoveInt < 0
                  || indexToRemoveInt >= conjToRemoveFrom.getTable().size()) {
                message = "index out of bounds in conjugation remove entry handler";
              } else {
                // if all is good, do the update
                conjToRemoveFrom.remove(indexToRemoveInt);
                successful = true;
                message = "successfully remove conjugation entry";
              }
              // catch if index to remove is not an integer
            } catch (NumberFormatException e) {
              message = "index of conjugation entry to remove not an integer";
            }
            // catch if module not in map
          } else {
            message = "conjugation module not in user map";
          }
          // catch if current language is null
        } else {
          message = "current language null in conjugation entry remove handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation entry remove handler";
      }

      // prepare information to send to front-end
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("successful", successful).put("message", message).build();
      return GSON.toJson(variables);
    }
  }

  /**
   * POST request for removing an entire conjugation table from the user's
   * language map.
   */
  public static class ConjugationRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String conjId = qm.value("conjId");
      // successful variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if language not null
        if (lang != null) {
          // check if module in user map
          if (lang.getModule(StorageType.CONJUGATION, conjId) != null) {
            // actually remove conjugation module
            Conjugation conjToRemove = (Conjugation) lang
                .getModule(StorageType.CONJUGATION, conjId);
            lang.removeModule(StorageType.CONJUGATION, conjToRemove);
            // edit sucessful messages
            successful = true;
            message = "successfully removed conjugation module";

            // catch if conjugation module does not exist
          } else {
            message = "conjugation module does not exist in user map";
          }
          // catch if current language is null
        } else {
          message = "current language null in conjugation remove handler";
        }
        // catch if user not in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation remove handler";
      }

      // prepare successful messages to front-end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- NOTE HANDLERS --------------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * POST request which adds a completely new note module.
   */
  public static class NoteAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String noteStr = qm.value("noteString");
      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // create new note and add to variables map
          Note note = new Note(noteStr);
          variables.put("newNote", toData(note));
          // edit successful variables
          successful = true;
          message = "successfully added note to map";

          // catch if current lang is null
        } else {
          message = "current language is null in add note handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in add note handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request which handles any edits made to a preexisting note module.
   */
  public static class NoteUpdateHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String noteId = qm.value("noteId");
      String noteChange = qm.value("noteUpdate");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if note module is in map
          if (lang.getModule(StorageType.NOTE, noteId) != null) {
            // update the actual node
            Note noteToUpdate = (Note) lang.getModule(StorageType.NOTE, noteId);
            noteToUpdate.update(noteChange);
            variables.put("updatedNote", toData(noteToUpdate));
            // update successful messages
            successful = true;
            message = "successfully update note";
            // catch if note module is not in map
          } else {
            message = "note module not in map in update note handler";
          }
          // catch if current lang is null
        } else {
          message = "current language is null in update note handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in update note handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }

  }

  /**
   * POST request for handling the removal of a note object from the user map.
   */
  public static class NoteRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String noteId = qm.value("noteId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if module is in user map
          if (lang.getModule(StorageType.NOTE, noteId) != null) {
            Note noteToRemove = (Note) lang.getModule(StorageType.NOTE, noteId);
            lang.removeModule(StorageType.NOTE, noteToRemove);
            // edit successful messages
            successful = true;
            message = "successfully removed note";
            // catch if note module not in user map
          } else {
            message = "note module not in user map";
          }
          // catch if current lang is null
        } else {
          message = "current language null in note remove handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in note remove handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- ALERT EXCLAMATION HANDLERS -------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * POST request for handling the addition of a completely new alert
   * exclamation module.
   */
  public static class AlertAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String alertStr = qm.value("alertString");
      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          AlertExclamation alert = new AlertExclamation(alertStr);
          variables.put("newAlert", toData(alert));
          // edit successful variables
          successful = true;
          message = "successfully added new alert";

          // catch if current lang is null
        } else {
          message = "current language null in alert add handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in alert add handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request for handling an update to an alert exclamation module.
   */
  public static class AlertUpdateHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String alertId = qm.value("alertId");
      String alertUpdate = qm.value("alertUpdate");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if alert module is in map
          if (lang.getModule(StorageType.ALERT_EXCLAMATION, alertId) != null) {
            // update module
            AlertExclamation alertToUpdate = (AlertExclamation) lang
                .getModule(StorageType.ALERT_EXCLAMATION, alertId);
            alertToUpdate.update(alertUpdate);
            variables.put("updatedAlert", toData(alertToUpdate));
            // update successful messages
            successful = true;
            message = "successfully updated alert module";

            // catch if alert module not in map
          } else {
            message = "alert module not in map in alert update handler";
          }
          // catch if current lang is null
        } else {
          message = "current language null in alert update handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in alert update handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request for handling the removal of an alert exclamation module.
   */
  public static class AlertRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String alertId = qm.value("alertId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if module is in user map
          if (lang.getModule(StorageType.ALERT_EXCLAMATION, alertId) != null) {
            // remove module
            AlertExclamation alertToRemove = (AlertExclamation) lang
                .getModule(StorageType.ALERT_EXCLAMATION, alertId);
            lang.removeModule(StorageType.ALERT_EXCLAMATION, alertToRemove);
            // edit successful variables
            successful = true;
            message = "successfully removed alert module";

            // catch if module not in map
          } else {
            message = "alert module not in map in alert removal handler";
          }
          // catch if current lang is null
        } else {
          message = "current language null in alert removal handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in alert removal handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- QUESTION HANDLERS ----------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * POST request for handling the addition of a completely new Question module.
   */
  public static class QuestionAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String questionStr = qm.value("questionStr");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          Question question = new Question(questionStr);
          variables.put("newQuestion", toData(question));
          // edit successful variables
          successful = true;
          message = "successfully added new question";

          // catch if current lang is null
        } else {
          message = "current language null in question add handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not in database in question add handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request for updating a preexisting Question module.
   */
  public static class QuestionUpdateHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String questionID = qm.value("questionId");
      String questionUpdate = qm.value("questionUpdate");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if question module is in map
          if (lang.getModule(StorageType.QUESTION, questionID) != null) {
            // update the module
            Question questionToUpdate = (Question) lang
                .getModule(StorageType.QUESTION, questionID);
            questionToUpdate.update(questionUpdate);
            variables.put("updatedQuestion", questionToUpdate);
            // update successful variables
            successful = true;
            message = "successfully updated question module";

            // catch if module not in map
          } else {
            message = "question module not in map in question update handler";
          }
          // catch if current lang is null
        } else {
          message = "current language null in question update handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not in database in question update handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request for handling the removal of a question module.
   */
  public static class QuestionRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String questionId = qm.value("questionId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if module is in user map
          if (lang.getModule(StorageType.QUESTION, questionId) != null) {
            // remove the question module
            Question questionToRemove = (Question) lang
                .getModule(StorageType.QUESTION, questionId);
            lang.removeModule(StorageType.QUESTION, questionToRemove);
            // update successful variables
            successful = true;
            message = "successfully removed question module";

            // catch if module not in user map
          } else {
            message = "question module not in user map in question remove handler";
          }
          // catch if current lang is null
        } else {
          message = "current language is null in question remove handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in question remove handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- TAG HANDLERS ---------------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * GET request handler for retrieving information pertaining to a specific tag
   * the user uses and wishes to view all modules categorized by that tag. Pulls
   * this information from the database and formats it to present to the front
   * end.
   */
  public static class TagPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String tagName = qm.value("tag");

      // successful variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

      // try to find user in database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current language is not null
        if (lang != null) {
          Collection<Tag> tagSet = lang.getTags();
          List<Map<String, Object>> tagList = new ArrayList<>();
          // create list of tags
          for (Tag tag : tagSet) {
            tagList.add(toData(tag));
          }
          // add tagList to variables map
          variables.put("content", tagSet);
          // edit success messages
          successful = true;
          message = "successful pulled tag information";

          // catch if current language is null
        } else {
          message = "current language null in tag page handler";
        }
        // catch if user not in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database in tag page handler";
      }
      // prepare variables for front end
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "...");
    }
  }

  /**
   * POST request for adding a completely new tag to the user map.
   */
  public static class TagAddHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String tagVal = qm.value("tagValue");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // add new tag
          if (!lang.hasTag(tagVal)) {
            lang.addTag(new Tag(tagVal));
          }
          // update successful variables
          successful = true;
          message = "successfully added tag";

          // catch if current lang is null
        } else {
          message = "current language is null in tag add handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not in database in tag add handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  // TODO maybe? an update tag handler

  /**
   * POST request for removing a tag from the user map.
   */
  public static class TagRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res) throws DriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String tagValue = qm.value("tagValue");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if lang has the tag
          if (lang.hasTag(tagValue)) {
            lang.removeTag(tagValue);
            // update successful variables
            successful = true;
            message = "successfully removed tag";

            // catch if tag is not in lang
          } else {
            message = "tag is not in user map in tag remove handler";
          }

          // catch if current lang is null
        } else {
          message = "current language is null in tag remove handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not in database in tag remove handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- GENERIC MODULE HANDLERS ----------------------------------------------
   * -------------------------------------------------------------------------
   */

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

      // if (language != null) {
      // if (type.equals("add")) {
      //
      // } else if (type.equals("update")) {
      //
      // } else if (type.equals("delete")) {
      //
      // } else {
      // // TODO: throw error
      // }
      // }
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

  /*
   * -------------------------------------------------------------------------
   * -- FREENOTES HANDLERS ---------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * GET request handler which retrieves all free notes information from the
   * database and formats this information to send to the front end for display
   * on the user's "FreeNotes" landing page.
   */
  public static class FreeNotesPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
            throws DriveApiException {
      String userId = req.session().attribute("user_id");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      String currentLanguage = "";
      String username = ""; // TODO get the user's name

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          currentLanguage = lang.getName();

          List<Map<String, Object>> recentNotes = new ArrayList<>();
          for (FreeNote note : lang.getRecentFreeNotes()) {
            recentNotes.add(toDataNoModules(note));
          }

          variables.put("recentNote", recentNotes);
          successful = true;
          message = "successfully retrieved FreeNotes";

        } else {
          message = "current language null in freenotes page handler";
        }
      } catch (DatabaseParserException e) {
        message = "user not found in database in free notes page handler";
      }

      variables.put("title", "FreeNotes");
      variables.put("username", username);
      variables.put("currentLanguage", currentLanguage);
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "notes.ftl");
    }
  }

  /**
   * GET request handler for an individual FreeNotes page which the user wishes
   * to access. Pulls out all of the information on that page and organizes it
   * in a way for the front end to display to the user.
   */
  public static class FreeNotesEditorHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
      QueryParamsMap qm = req.queryMap();
      String noteId = qm.value("id");
      String currentLanguage = qm.value("currentLanguage");

      System.out.println("curr lang: " + currentLanguage);

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

      if (noteId.equals("new")) {
        // send the default values to front end / empty lists and stuff
        variables.put("title", "Note Title");
      } else {
        // TODO: use noteId to find the note in the database
        // send modules and other relevant data (note title, date, etc)
        variables.put("title", "TODO"); // TODO: put note title
      }

      // TODO LOOK AT notes.js for the variables names


      variables.put("username", "temp");
      variables.put("currentLanguage", currentLanguage);

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

  /*
   * -------------------------------------------------------------------------
   * -- RATING HANDLERS ------------------------------------------------------
   * -------------------------------------------------------------------------
   */

  // TODO create a set handler for all things reviewable

  /*
   * -------------------------------------------------------------------------
   * -- REVIEW HANDLERS ------------------------------------------------------
   * -------------------------------------------------------------------------
   */

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
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
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
   * GET request handler for an individual Review page which pulls all of the
   * information of the modules and tags the user has chosen to review, packages
   * it, and formats it to send to the front-end to display to the user.
   * Retrieves all information including content, type, and rating. Sends all of
   * this info to the front-end in the ordered rating according to the
   * algorithm.
   */
  public static class ReviewModeIndividualHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws DriveApiException {
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

  /*
   * -------------------------------------------------------------------------
   * -- CLASS TO JSON HANDLERS -----------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * Converts a FreeNote into a data map for JSON, excluding all of the
   * sub-modules' information.
   * @param note the FreeNote to convert
   * @return a map containing the superficial information of a FreeNote
   */
  private static Map<String, Object> toDataNoModules(FreeNote note) {
    ImmutableMap.Builder<String, Object> variables =
            new ImmutableMap.Builder<>();
    variables.put("noteId", note.getId());
    variables.put("dateCreated", note.getDateCreated());
    variables.put("dateModified", note.getDateModified());
    variables.put("title", note.getTitle());
    List<String> tags = new ArrayList<>();
    for (Tag t : note.getTags()) {
      tags.add(t.getTag());
    }
    variables.put("tags", tags);
    return variables.build();
  }

  /**
   * Converts a FreeNote into a data map for JSON.
   * @param note
   *          the FreeNote object to convert
   * @return a map of data from the FreeNote object
   */
  private static Map<String, Object> toData(FreeNote note) {

    ImmutableMap.Builder<String, Object> noteData = new ImmutableMap.Builder<String, Object>();
    noteData.put("modtype", StorageType.FREE_NOTE);
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
    noteData.put("freeNoteContent", modulesList);
    return noteData.build();
  }

  /**
   * Converts a Vocab module into a data map for JSON.
   * @param vocab
   *          the Vocab object to convert
   * @return a map of data from the Vocab object
   */
  private static Map<String, Object> toData(Vocab vocab) {

    ImmutableMap.Builder<String, Object> vocabData = new ImmutableMap.Builder<String, Object>();
    // pull information of vocab
    vocabData.put("modtype", StorageType.VOCAB);

    toData(vocab, vocabData);


    // prepare map of content
    /*
     * Map<String, String> vocabContentList = new HashMap<>();
     * vocabContentList.put("vocabTerm", vocab.getPair().getTerm());
     * vocabContentList.put("vocabDef", vocab.getPair().getDefinition());
     * vocabData.put("vocabContent", vocabContentList);
     */


    vocabData.put("term", vocab.getPair().getTerm());
    vocabData.put("def", vocab.getPair().getDefinition());
    vocabData.put("rating", vocab.getRating());
    return vocabData.build();
  }

  /**
   * Converts a Conjugation module into a data map for JSON.
   * @param conjugation
   *          the Conjugation module to convert
   * @return a map of data from the FreeNote object
   */
  private static Map<String, Object> toData(Conjugation conjugation) {
    ImmutableMap.Builder<String, Object> conjugationData = new ImmutableMap.Builder<String, Object>();
    // pull information of conjugation table
    conjugationData.put("modtype", StorageType.CONJUGATION);
    toData(conjugation, conjugationData);
    conjugationData.put("header", conjugation.getHeader());

    List<List<String>> conjPairData = new ArrayList<>();
    // parse out the pair data
    for (Pair p : conjugation.getTable()) {
      List<String> pairData = new ArrayList<>();
      pairData.add(p.getTerm());
      pairData.add(p.getDefinition());
      conjPairData.add(pairData);
    }

    // content of the conjugation table
    conjugationData.put("tableContent", conjPairData);
    conjugationData.put("rating", conjugation.getRating());
    return conjugationData.build();
  }

  /**
   * Converts a Tag module into a data map for JSON.
   * @param tag
   *          the Tag module to convert
   * @return a map of data from the Tag object
   */
  private static Map<String, Object> toData(Tag tag) {
    ImmutableMap.Builder<String, Object> tagData = new ImmutableMap.Builder<String, Object>();
    tagData.put("modtype", StorageType.TAG);
    tagData.put("tagContent", tag.getTag());
    return tagData.build();
  }

  /**
   * Converts an AlertExclamation into a data map for JSON.
   * @param alert
   *          the AlertExclamation object to convert
   * @return a map of data from the AlertExclamation object
   */
  private static Map<String, Object> toData(AlertExclamation alert) {
    ImmutableMap.Builder<String, Object> alertData = new ImmutableMap.Builder<String, Object>();
    alertData.put("modtype", StorageType.ALERT_EXCLAMATION);
    toData(alert, alertData);
    alertData.put("alertContent", alert.getText());
    return alertData.build();
  }

  /**
   * Converts a Question module into a data map for JSON.
   * @param question
   *          the Question object to convert
   * @return a map of dating storing the Question information
   */
  private static Map<String, Object> toData(Question question) {
    ImmutableMap.Builder<String, Object> questionData = new ImmutableMap.Builder<String, Object>();
    questionData.put("modtype", StorageType.ALERT_EXCLAMATION);
    toData(question, questionData);
    questionData.put("questionContent", question.getText());
    return questionData.build();
  }

  /**
   * Converts a Note into a data map for JSON.
   * @param note
   *          the Note object to convert
   * @return the ImmutableMap to add the data to
   */
  private static Map<String, Object> toData(Note note) {
    ImmutableMap.Builder<String, Object> noteData = new ImmutableMap.Builder<String, Object>();
    noteData.put("modtype", StorageType.NOTE);
    toData(note, noteData);
    noteData.put("noteContent", note.getText());
    noteData.put("rating", note.getRating());
    return noteData.build();
  }

  /**
   * Converts any generic Module data into a data map for JSON.
   * @param module
   *          the generic Module object to convert
   * @param map
   *          the ImmutableMap to add the data information to
   */
  private static void toData(Module module,
      ImmutableMap.Builder<String, Object> map) {

    map.put("id", module.getId());
    map.put("dateCreated", module.getDateCreated());
    map.put("dateModified", module.getDateModified());

    // generate tag list
    List<Map<String, Object>> tagList = new ArrayList<>();
    for (Tag tag : module.getTags()) {
      tagList.add(toData(tag));
    }

    map.put("tags", tagList);
  }

}
