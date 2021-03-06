package edu.brown.cs.athenia.gui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.*;
import java.util.*;

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
import edu.brown.cs.athenia.updaterscheduler.UpdaterScheduler;
import edu.brown.cs.athenia.driveapi.GoogleDriveApiException;
import edu.brown.cs.athenia.driveapi.GoogleDriveApi;
import edu.brown.cs.athenia.main.Athenia;
import edu.brown.cs.athenia.review.*;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.TemplateViewRoute;

/**
 * GUICommand will handle GUI commands, FreeMarker methods (gets and posts),
 * and dynamic URLs to account for arbitrary number of "pages".
 */
public class GUICommand {

  private static final Gson GSON = new Gson();

  // Prevent creation of instances of private class.
  private GUICommand() {
  }

  /**
   * GET request for directing the user to the landing sign-in page.
   */
  public static class LandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
        throws GoogleDriveApiException {
      // String userId = checkLoggedIn(req, res);
      Map<String, Object> variables =
              new ImmutableMap.Builder<String, Object>()
          .put("title", "Welcome to Athenia!").build();

      return new ModelAndView(variables, "landing.ftl");
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- LOGIN/LOGOUT HANDLERS ------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * Handles initial login request, redirecting user to Google Authentication
   * page if not already logged in.
   */
  public static class LoginHandler implements Route {

    @Override
    public ModelAndView handle(Request req, Response res)
        throws GoogleDriveApiException {
      // Set destination to go after login
      if (req.session().attribute("loginDestination") == null) {
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

  /**
   * Handles logout request, redirecting user to the landing
   * page if not already logged in.
   */
  public static class LogoutHandler implements Route {

    @Override
    public ModelAndView handle(Request req, Response res)
            throws GoogleDriveApiException, DatabaseParserException {
      String userId = req.session().attribute("user_id");

      // Update user's database
      DatabaseParser.updateUser(userId);

      // Remove scheduler for updating user's database
      UpdaterScheduler.delete(userId);

      // Invalidate previous session and create a new one
      req.session().invalidate();
      req.session();

      // Redirect to landing page
      res.redirect("/");
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
        throws GoogleDriveApiException {
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String lang = qm.value("newLanguage");

      // successful variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      // try to pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        // check if user has the language already
        if (user.getLanguages().contains(lang)) {
          message = "language already exists";
        } else {
          // else add the new language
          user.addLanguage(lang);
          successful = true;
          message = "succesfully added language";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from " +
                "database in language add handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
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
        message = "error getting user from " +
                "database in language remove handler";
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
        throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables = new ImmutableMap.Builder<String, Object>();

      // try to pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check for null current language
        if (lang != null) {

          // get information about lang to present on home
          int vocabCount = lang.getModuleCount(StorageType.VOCAB);
          int noteCount = lang.getModuleCount(StorageType.FREE_NOTE); // this should work but thomas says it is getting the number of note modules instead?
          int conjugationCount = lang.getModuleCount(StorageType.CONJUGATION);

          List<String> recentList = new ArrayList<>();
          // pull all recent notes from language

          for (FreeNote note : lang.getRecentFreeNotes()) {
            recentList.add(toData(note));
          }

          // add this info to the map
          variables.put("username", "");
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
        throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        variables.put("username", "");

        // check if current language not null
        if (lang != null) {

          variables.put("currentLanguage", lang.getName());
          // edit success messages
          successful = true;
          message = "successfully pulled vocab information";
        } else {
          message = "current language null in vocabulary page handler";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database in vocabulary page handler";
      }

      // prepare variables for front end
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
  public static class getVocabularyModulesHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      // QueryParamsMap qm = req.queryMap();

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      // try to pull user from database
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

          variables.put("vocabContent", vocabList);
          // edit success messages
          successful = true;
          message = "successfully pulled vocab information";
        } else {
          message = "current language null in vocabulary page handler";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database " +
                "in vocabulary page handler";
      }

      variables.put("message", message);
      variables.put("successful", successful);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request handler for adding a new Vocab object to the user's current
   * Language.
   */
  public static class VocabularyAddHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull information
      String newTerm = qm.value("newTerm");
      String newDef = qm.value("newDef");

      // get free note id
      String freeNoteId = qm.value("freeNoteId");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        // check if language not null
        if (lang != null) {
          Vocab newVocab = new Vocab(newTerm, newDef);
          lang.addModule(StorageType.VOCAB, newVocab);

          // call to data on new object
          variables.put("newVocabModule", toData(newVocab));

          // set freenote if can
          if (lang.containsFreeNote(freeNoteId)) {
            FreeNote freeNote = lang.getFreeNote(freeNoteId);
            freeNote.addModule(newVocab);
            newVocab.setFreeNote(freeNote);
          }

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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull information from frontend
      String vocabId = qm.value("vocabId");
      String updatedTerm = qm.value("updatedTerm");
      String updatedDef = qm.value("updatedDef");
      String updatedRating = qm.value("updatedRating");

      // get free note id
      String freeNoteId = qm.value("freeNoteId");

      // successful messages
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      // try to pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        // check if language not null
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
            message = "vocab module not in language " +
                    "module map in vocab update handler";
          }
        } else {
          message = "current language null in vocab update handler";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database in " +
                "vocab update handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String vocabId = qm.value("vocabId");

      // get free note id
      String freeNoteId = qm.value("freeNote");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      // try to pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if language not null
        if (lang != null) {
          if (lang.getModule(StorageType.VOCAB, vocabId) != null) {
            Vocab vocabToRemove = (Vocab) lang.getModule(StorageType.VOCAB,
                vocabId);

            // remove the module from the freenote it is in
            if (lang.containsFreeNote(freeNoteId)) {
              FreeNote freeNote = lang.getFreeNote(freeNoteId);
              freeNote.removeModule(vocabToRemove);
            }

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
        message = "error getting user from " +
                "database in vocab remove handler";
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
        throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      // successful messages
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      // pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        variables.put("username", "");

        // check for null language
        if (lang != null) {
          variables.put("currentLanguage", lang.getName());

          successful = true;
          message = "successfully pulled conjugation information";
        } else {
          message = "current language null in conjugation page handler";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database in " +
                "conjugation page handler";
      }

      variables.put("title", "Conjugation");
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "conjugations.ftl");
    }
  }

  /**
   * POST request handler for pulling all Conjugation content for the
   * conjugation page.
   */
  public static class GetConjugationContentHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to pull user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        // check for null language
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
          variables.put("conjugationContent", conjList);
          successful = true;
          message = "successfully pulled conjugation information";

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
   * POST request which adds a completely new conjugation module.
   */
  public static class ConjugationAddHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // get free note id
      String freeNoteId = qm.value("freeNoteId");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to pull user
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          // set up conjugation (but this isn't complete)
          Conjugation conjToAdd = new Conjugation();
          conjToAdd.setHeader("Table Header");
          variables.put("newConjugationModule", conjToAdd);

          successful = true;
          message = "successfully added conjugation";
        } else {
          message = "current language null in conjugation add handler";
        }
      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation " +
                "add handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
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

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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
                message = "index out of bounds in conjugation add " +
                        "entry handler";
              }
              // catch number format exception
            } catch (NumberFormatException e) {
              message = "index to add at not an int in conjugation " +
                      "add entry handler";
            }
            // catch if conjugation module not in map
          } else {
            message = "conjugation module not in user map";
          }
          // catch if current language is null
        } else {
          message = "current language null in conjugation add " +
                  "entry handler";
        }
        // catch if user not in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation " +
                "add entry handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
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
                message = "index out of bounds in conjugation " +
                        "update handler";
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
        message = "error getting user from database in conjugation " +
                "update handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
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
                message = "index out of bounds in conjugation " +
                        "remove entry handler";
              } else {
                // if all is good, do the update
                conjToRemoveFrom.remove(indexToRemoveInt);
                successful = true;
                message = "successfully remove conjugation entry";
              }
              // catch if index to remove is not an integer
            } catch (NumberFormatException e) {
              message = "index of conjugation entry to remove " +
                      "not an integer";
            }
            // catch if module not in map
          } else {
            message = "conjugation module not in user map";
          }
          // catch if current language is null
        } else {
          message = "current language null in conjugation entry " +
                  "remove handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database in conjugation " +
                "entry remove handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String conjId = qm.value("conjId");
      // successful variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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
        message = "error getting user from database in " +
                "conjugation remove handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // get info from front end
      String noteStr = qm.value("noteString");

      // get FreeNote id
      String freeNoteId = qm.value("freeNoteId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // create new note and add to variables map
          Note note = new Note(noteStr);

          lang.addModule(StorageType.NOTE, note);

          // connecting everything to freenote if applicable
          if (lang.containsFreeNote(freeNoteId)) {
            FreeNote freeNote = lang.getFreeNote(freeNoteId);
            freeNote.addModule(note);
            note.setFreeNote(freeNote);
          }

          variables.put("newNoteModule", toData(note));
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String noteId = qm.value("noteId");
      String noteChange = qm.value("noteUpdate");

      String tagUpdate = qm.value("tagUpdate");
      String ratingUpdate = qm.value("ratingUpdate");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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

            // update text of note
            noteToUpdate.update(noteChange);

            // try to update rating
            try {
              int newRating = Integer.parseInt(ratingUpdate);
              noteToUpdate.setRating(newRating);
            } catch (NumberFormatException e) {
              message = "rating not a number";
            }

            // update tags
            List<Tag> tagsToReplace = new ArrayList<>();
            String[] tagsSplit = tagUpdate.split(",");
            for (String t : tagsSplit) {
              String tTrim = t.trim();
              if (lang.hasTag(t)) {
                Tag temp = lang.getTag(t);
                tagsToReplace.add(temp);
              } else {
                Tag temp = new Tag(t);
                lang.addTag(temp);
                tagsToReplace.add(temp);
              }
            }
            noteToUpdate.replaceAllTags(tagsToReplace);

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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String noteId = qm.value("idToRemove");

      // get FreeNote id
      String freeNoteId = qm.value("freeNoteId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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

            // edit the freenote accordingly
            if (lang.containsFreeNote(freeNoteId)) {
              FreeNote freeNote = lang.getFreeNote(freeNoteId);
              freeNote.removeModule(noteToRemove);
              noteToRemove.removeFreeNote();
            }

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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String alertStr = qm.value("alertString");

      // get free note id
      String freeNoteId = qm.value("freeNoteId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          AlertExclamation alert = new AlertExclamation(alertStr);
          lang.addModule(StorageType.ALERT_EXCLAMATION, alert);

          // set up connection to freenote
          if (lang.containsFreeNote(freeNoteId)) {
            FreeNote freeNote = lang.getFreeNote(freeNoteId);
            freeNote.addModule(alert);
            alert.setFreeNote(freeNote);
          }

          variables.put("newAlertModule", toData(alert));

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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String alertId = qm.value("alertId");
      String alertUpdate = qm.value("alertUpdate");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          // check if alert module is in map
          if (lang.getModule(StorageType.ALERT_EXCLAMATION, alertId)
                  != null) {
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String alertId = qm.value("alertId");

      // get FreeNote id
      String freeNoteId = qm.value("freeNoteId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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

            // sort out everything with free note
            if (lang.containsFreeNote(freeNoteId)) {
              FreeNote freeNote = lang.getFreeNote(freeNoteId);
              freeNote.removeModule(alertToRemove);
              alertToRemove.removeFreeNote();
            }

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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String questionStr = qm.value("questionString");

      // get freenote id
      String freeNoteId = qm.value("freeNoteId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {
          Question question = new Question(questionStr);

          lang.addModule(StorageType.QUESTION, question);

          // handle all free note connections
          if (lang.containsFreeNote(freeNoteId)) {
            FreeNote freeNote = lang.getFreeNote(freeNoteId);
            freeNote.addModule(question);
            question.setFreeNote(freeNote);
          }

          variables.put("newQuestionModule", toData(question));
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String questionID = qm.value("questionId");
      String questionUpdate = qm.value("questionUpdate");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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
            variables.put("updatedQuestion", toData(questionToUpdate));
            // update successful variables
            successful = true;
            message = "successfully updated question module";

            // catch if module not in map
          } else {
            message = "question module not in map in question " +
                    "update handler";
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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String questionId = qm.value("questionId");

      // get freenote id
      String freeNoteId = qm.value("freeNoteId");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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

            // remove the question from the freenotes
            if (lang.containsFreeNote(freeNoteId)) {
              FreeNote freeNote = lang.getFreeNote(freeNoteId);
              freeNote.removeModule(questionToRemove);
              questionToRemove.removeFreeNote();
            }

            // update successful variables
            successful = true;
            message = "successfully removed question module";

            // catch if module not in user map
          } else {
            message = "question module not in user map " +
                    "in question remove handler";
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
        throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String tagName = qm.value("tag");

      // successful variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      // try to find user in database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current language is not null
        if (lang != null) {

          // this handler is not used lol

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
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String tagVal = qm.value("tagValue");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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

  /**
   * POST request for removing a tag from the user map.
   */
  public static class TagRemoveHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();
      String tagValue = qm.value("tagValue");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

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
   * -- GENERIC MODULE & TAG HANDLERS ----------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * POST request for adding a tag to a module.
   */
  public static class AddTagToModule implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull information from the front end
      String moduleId = qm.value("moduleId");
      String modtype = qm.value("modtype");
      String tagToAdd = qm.value("tagToAdd");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {

          // pull tag information
          Tag tag;
          if (lang.hasTag(tagToAdd)) {
            tag = lang.getTag(tagToAdd);
          } else {
            tag = new Tag(tagToAdd);
            lang.addTag(tag);
          }

          // check for vocab module and update accordingly
          if (modtype.equals(StorageType.VOCAB.toString())) {
            if (lang.getModule(StorageType.VOCAB, moduleId) != null) {
              Vocab vocabToUpdate = (Vocab)
                      lang.getModule(StorageType.VOCAB, moduleId);
              vocabToUpdate.addTag(tag);
              successful = true;
              message = "successfully added tag to " + moduleId;
            } else {
              message = "module not in map " + moduleId;
            }

            // check for alert exclamation module and update accordingly
          } else if (modtype.equals(StorageType.ALERT_EXCLAMATION.toString())) {
            if (lang.getModule(StorageType.ALERT_EXCLAMATION, moduleId) != null) {
              AlertExclamation alertToUpdate = (AlertExclamation)
                      lang.getModule(StorageType.ALERT_EXCLAMATION, moduleId);
              alertToUpdate.addTag(tag);
              successful = true;
              message = "successfully added tag to " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // check for conjugation module and update accordingly
          } else if (modtype.equals(StorageType.CONJUGATION.toString())) {
            if (lang.getModule(StorageType.CONJUGATION, moduleId) != null) {
              Conjugation conjToUpdate = (Conjugation)
                      lang.getModule(StorageType.CONJUGATION, moduleId);
              conjToUpdate.addTag(tag);
              successful = true;
              message = "successfully added tag to " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // check for free note module and update accordingly
          } else if (modtype.equals(StorageType.FREE_NOTE.toString())) {
              if (lang.getFreeNote(moduleId) != null) {
                lang.getFreeNote(moduleId).addTag(tag);
                successful = true;
                message = "successfully added tag to " + moduleId;
              } else {
                message = "module not in map: " + moduleId;
              }

            // check for note module and update accordingly
          } else if (modtype.equals(StorageType.NOTE.toString())) {
            if (lang.getModule(StorageType.NOTE, moduleId) != null) {
              Note noteToUpdate = (Note)
                      lang.getModule(StorageType.NOTE, moduleId);
              noteToUpdate.addTag(tag);
              successful = true;
              message = "succesfully added tag to " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // check for question module and update accordingly
          } else if (modtype.equals(StorageType.QUESTION.toString())) {
            if (lang.getModule(StorageType.QUESTION, moduleId) != null) {
              Question questionToUpdate = (Question)
                      lang.getModule(StorageType.QUESTION, moduleId);
              questionToUpdate.addTag(tag);
              successful = true;
              message = "succesfully added tag to " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }
            
            // catch all other cases
          } else {
            message = "modtype not recognized " + modtype;
          }

          // catch if current lang is null
        } else {
          message = "language in add tag to module handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not in database in add tag to module handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request for removing a tag from a module.
   */
  public static class RemoveTagFromModule implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull info from front end
      String moduleId = qm.value("moduleId");
      String modtype = qm.value("modtype");
      String tagToRemove = qm.value("tagToRemove");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {

          // check for vocab module and update accordingly
          if (modtype.equals(StorageType.VOCAB.toString())) {
            if (lang.getModule(StorageType.VOCAB, moduleId) != null) {
              Vocab vocabToUpdate = (Vocab)
                      lang.getModule(StorageType.VOCAB, moduleId);
              vocabToUpdate.removeTag(tagToRemove);
              successful = true;
              message = "succesfully removed tag from " + moduleId;
            } else {
              message = "module not in map " + moduleId;
            }

            // check for alert exclamation module and update accordingly
          } else if (modtype.equals(StorageType.ALERT_EXCLAMATION.toString())) {
            if (lang.getModule(StorageType.ALERT_EXCLAMATION, moduleId) != null) {
              AlertExclamation alertToUpdate = (AlertExclamation)
                      lang.getModule(StorageType.ALERT_EXCLAMATION, moduleId);
              alertToUpdate.removeTag(tagToRemove);
              successful = true;
              message = "succesfully removed tag from " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // check for conjugation module and update accordingly
          } else if (modtype.equals(StorageType.CONJUGATION.toString())) {
            if (lang.getModule(StorageType.CONJUGATION, moduleId) != null) {
              Conjugation conjToUpdate = (Conjugation)
                      lang.getModule(StorageType.CONJUGATION, moduleId);
              conjToUpdate.removeTag(tagToRemove);
              successful = true;
              message = "succesfully removed tag from " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // check for free note module and update accordingly
          } else if (modtype.equals(StorageType.FREE_NOTE.toString())) {
            if (lang.getFreeNote(moduleId) != null) {
              lang.getFreeNote(moduleId).removeTag(tagToRemove);
              successful = true;
              message = "succesfully removed tag from " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // check for note module and update accordingly
          } else if (modtype.equals(StorageType.NOTE.toString())) {
            if (lang.getModule(StorageType.NOTE, moduleId) != null) {
              Note noteToUpdate = (Note)
                      lang.getModule(StorageType.NOTE, moduleId);
              noteToUpdate.removeTag(tagToRemove);
              successful = true;
              message = "succesfully remove tag from " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // check for question module and update accordingly
          } else if (modtype.equals(StorageType.QUESTION.toString())) {
            if (lang.getModule(StorageType.QUESTION, moduleId) != null) {
              Question questionToUpdate = (Question)
                      lang.getModule(StorageType.QUESTION, moduleId);
              questionToUpdate.removeTag(tagToRemove);
              successful = true;
              message = "succesfully removed tag from " + moduleId;
            } else {
              message = "module not in map: " + moduleId;
            }

            // catch all other cases
          } else {
            message = "modtype not recognized " + modtype;
          }

          // catch if current lang is null
        } else {
          message = "language in remove tag from module handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not in database in remove tag from module handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
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
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      String currentLanguage = "";
      String username = "";

      // get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          currentLanguage = lang.getName();
          successful = true;
          message = "successfully opened freenotes";

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
   * POST request for compiling the FreeNotes to display on the
   * View Notes page.
   */
  public static class GetFreeNotesList implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      String currentLanguage = "";
      String username = "";

      // pull user from the database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {
          currentLanguage = lang.getName();

          // prepare freenotes
          List<Map<String, Object>> recentNotes = new ArrayList<>();
          for (FreeNote note : lang.getFreeNotes()) {
            recentNotes.add(toDataNoModules(note));
          }

          variables.put("allNotes", recentNotes);
          successful = true;
          message = "successfully retrieved FreeNotes";

        } else {
          message = "current language null in freenotes page handler";
        }
      } catch (DatabaseParserException e) {
        message = "user not found in database in free notes page handler";
      }

      variables.put("username", username);
      variables.put("currentLanguage", currentLanguage);
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /**
   * POST request handler for editing the title of an arbitrary FreeNote;
   */
  public static class FreeNotesTitleEditorHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull out information from query map
      String freeNoteId = qm.value("freeNoteId");
      String newTitle = qm.value("newTitle");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        if (lang != null) {

          // check if freenote in the language map
          if (lang.containsFreeNote(freeNoteId)) {

            FreeNote freeNote = lang.getFreeNote(freeNoteId);
            freeNote.setTitle(newTitle);

            successful = true;
            message = "successfully changed title of freenote";

          } else {
            message = "language does not have freenote in it";
          }

        } else {
          message = "current language null in freenotes " +
                  "title editor handler";
        }
      } catch (DatabaseParserException e) {
        message = "user not found in database in free notes " +
                "title editor handler";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
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
        throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");

      // pull info from front end
      QueryParamsMap qm = req.queryMap();
      String noteId = qm.value("id");

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<String, Object>();

      String currentLanguage = "";
      String username = "";
      String title = "";

      // successful variables
      String message = "";
      boolean successful = false;

      // get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        // check for null language
        if (lang != null) {
          currentLanguage = lang.getName();

          if (noteId.equals("new")) {

            // create a new freenote
            FreeNote newFreeNote = new FreeNote("Note Title");
            lang.addFreeNote(newFreeNote);
            title = newFreeNote.getTitle();
            variables.put("freeNote", toData(newFreeNote));
            variables.put("freeNoteId", newFreeNote.getId());
            successful = true;
            message = "successfully added new freenote";
          } else {
            // pull an old one out

            if (lang.containsFreeNote(noteId)) {

              FreeNote oldFreeNote = lang.getFreeNote(noteId);
              variables.put("freeNote", toData(oldFreeNote));
              variables.put("freeNoteId", oldFreeNote.getId());
              title = oldFreeNote.getTitle();
              successful = true;
              message = "successfully pulled old freenote";

            } else {
              message = "freenote id does not exist in language map";
            }
          }

        } else {
          message = "language null in freenotes editor handler";
        }

      } catch (DatabaseParserException e) {
        message = "user not in database in free note editor handler";
      }

      variables.put("title", title);
      variables.put("username", username);
      variables.put("currentLanguage", currentLanguage);
      variables.put("successful", successful);
      variables.put("message", message);

      return new ModelAndView(variables.build(), "notePageEdit.ftl");
    }
  }

  /**
   * POST request for removing an arbitrary FreeNote from the View Notes page.
   */
  public static class RemoveFreeNote implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull info from front end
      String idToRemove = qm.value("noteId");

      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        // check if current lang is not null
        if (lang != null) {

          // check if lang has the freenote in it
          if (lang.containsFreeNote(idToRemove)) {
            lang.removeFreeNote(idToRemove);

            successful = true;
            message = "successfully removed freenote";

            // catch if language does not have freenote in it
          } else {
            message = "freenote id not in current language";
          }
          // catch if current language is null
        } else {
          message = "current language is null";
        }
        // catch if user not in database
      } catch (DatabaseParserException e) {
        message = "user not found in database";
      }

      // prepare message for front-end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- RATING HANDLERS ------------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * POST request for setting the rating of a reviewable module.
   */
  public static class SetRatingHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull information from front end
      String moduleId = qm.value("moduleId");
      String newRating = qm.value("newRating");
      String modtype = qm.value("modtype");

      // success variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();
        // check if current lang is not null
        if (lang != null) {

          try {

            // parse out the new rating
            int newRatingInt = Integer.parseInt(newRating);

            // check for vocab module and update accordingly
            if (modtype.equals(StorageType.VOCAB.toString())) {
              if (lang.getModule(StorageType.VOCAB, moduleId) != null) {
                Vocab vocabToUpdate = (Vocab)
                        lang.getModule(StorageType.VOCAB, moduleId);
                vocabToUpdate.setRating(newRatingInt);
                successful = true;
                message = "succesfully set rating of " + moduleId;
              } else {
                message = "module not in map " + moduleId;
              }

              // check for conjugation module and update accordingly
            } else if (modtype.equals(StorageType.CONJUGATION.toString())) {
              if (lang.getModule(StorageType.CONJUGATION, moduleId) != null) {
                Conjugation conjToUpdate = (Conjugation)
                        lang.getModule(StorageType.CONJUGATION, moduleId);
                conjToUpdate.setRating(newRatingInt);
                successful = true;
                message = "succesfully set rating of " + moduleId;
              } else {
                message = "module not in map: " + moduleId;
              }

              // check for note module and update accordingly
            } else if (modtype.equals(StorageType.NOTE.toString())) {
              if (lang.getModule(StorageType.NOTE, moduleId) != null) {
                Note noteToUpdate = (Note)
                        lang.getModule(StorageType.NOTE, moduleId);
                noteToUpdate.setRating(newRatingInt);
                successful = true;
                message = "succesfully set rating of " + moduleId;
              } else {
                message = "module not in map: " + moduleId;
              }

              // check for question module and update accordingly
            } else {
              message = "modtype not recognized " + modtype;
            }

          } catch (NumberFormatException e) {
            message = "new rating specified is not in integer";
          }

          // catch if current lang is null
        } else {
          message = "current language null in set rating handler";
        }
        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in set rating handler";
      }

      // prepare variables to send to front end
      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
    }
  }

  /*
   * -------------------------------------------------------------------------
   * -- REVIEW HANDLERS ------------------------------------------------------
   * -------------------------------------------------------------------------
   */

  /**
   * GET request handler which pulls out all of the information of the modules
   * they should review and sets a list of reviewables back to the front end.
   */
  public static class ReviewModeHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");

      // success variables
      boolean successful = false;
      String message = "";

      // global info to pass to frontend
      String username = "";
      String currentLanguage = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {
        Athenia user = DatabaseParser.getUser(userId);
        username = ""; // TODO get the username
        Language lang = user.getCurrLanguage();
        // check if current language not null
        if (lang != null) {
          currentLanguage = lang.getName();

          List<String> tags = new ArrayList<>();
          for (Tag t : lang.getTags()) {
            tags.add(t.getTag());
          }

          variables.put("allTags", tags);

          // update successful variables
          successful = true;
          message = "successfully pulled tags from lang object";

        } else {
          message = "current language null in review mode handler";
        }

        // catch if user not found in database
      } catch (DatabaseParserException e) {
        message = "user not found in database in review mode handler";
      }

      // prepare variables to send to front end
      variables.put("title", "Review");
      variables.put("username", username);
      variables.put("currentLanguage", currentLanguage);
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "review.ftl");
    }
  }

  /**
   * GET request handler for populating the desired items to review.
   */
  public static class ReviewingHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res)
      throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull data from frontend
      String startDate = qm.value("startDate");
      String endDate = qm.value("endDate");
      String tagSelection = qm.value("tagSelection");

      boolean successful = false;
      String message = "";

      // global info to pass to frontend
      String username = "";
      String currentLanguage = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      variables.put("startDate", startDate);
      variables.put("endDate", endDate);
      variables.put("tagSelection", tagSelection);

      try {
        Athenia user = DatabaseParser.getUser(userId);
        username = ""; // TODO get username
        Language lang = user.getCurrLanguage();

        if (lang != null) {
          currentLanguage = lang.getName();

        } else {
          message = "current language null reviewing handler";
        }

      } catch (DatabaseParserException e) {
        message = "error getting user from database in reviewing handler";
      }

      variables.put("title", "Reviewing");
      variables.put("username", username);
      variables.put("currentLanguage", currentLanguage);
      variables.put("successful", successful);
      variables.put("message", message);
      return new ModelAndView(variables.build(), "reviewing.ftl");
    }
  }

  /**
   * POST request which pulls out all of the reviewables to present on
   * the front end.
   */
  public static class GetReviewListHandler implements Route {
    @Override
    public String handle(Request req, Response res)
            throws GoogleDriveApiException {
      String userId = req.session().attribute("user_id");
      QueryParamsMap qm = req.queryMap();

      // pull information from front end
      String startDate = qm.value("startDate");
      String endDate = qm.value("endDate");
      String tagsSelected = qm.value("tagSelection");

      // successful variables
      boolean successful = false;
      String message = "";

      ImmutableMap.Builder<String, Object> variables =
              new ImmutableMap.Builder<>();

      // try to get user from database
      try {

        Athenia user = DatabaseParser.getUser(userId);
        Language lang = user.getCurrLanguage();

        // check if current language is not null
        if (lang != null) {

          try {
            // parse out date objects
            Date startDateObject =
                    new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            Date endDateObject =
                    new SimpleDateFormat("yyyy-MM-dd").parse(endDate);

            // prepare tag selected lists to create new tag list
            String[] tagsSplit = tagsSelected.split(",");
            List<Tag> tagList = new ArrayList<>();

            // create tag list
            for (String t : tagsSplit) {
              Tag tempTag = lang.getTag(t);
              // add tag to list if not null
              if (tempTag != null) {
                tagList.add(tempTag);
                System.out.println(tempTag.getTag());
              }
            }

            // THESE ARE TESTS
            Vocab vocabNew = new Vocab("VOCAB TEST TERM", "VOCAB TEST DEF");
            Note noteNew = new Note("THIS IS A NOTE TEST");

            List<Reviewable> reviewablesList = new ArrayList<>();
            reviewablesList.add(vocabNew);
            reviewablesList.add(noteNew);
            // THESE ARE TESTS

            // prepare list of modules to review for frontend
            List<Map<String, Object>> reviewablesListConverted =
                new ArrayList<>();
            for (Reviewable r : reviewablesList) {
              if (r instanceof Vocab) {
                Vocab vocab = (Vocab) r;
                reviewablesListConverted.add(toData(vocab));
              } else if (r instanceof Conjugation) {
                Conjugation conj = (Conjugation) r;
                reviewablesListConverted.add(toData(conj));
              } else if (r instanceof Note) {
                Note note = (Note) r;
                reviewablesListConverted.add(toData(note));
              }
            }

            variables.put("reviewModules", reviewablesListConverted);

            successful = true;
            message = "successfully pulled reviewables";
          } catch (ParseException e) {
            message = "unable to correctly parse start and date strings";
          }

          // catch if current language is null
        } else {
          message = "current language null";
        }

        // catch if user not in database
      } catch (DatabaseParserException e) {
        message = "error getting user from database";
      }

      variables.put("successful", successful);
      variables.put("message", message);
      return GSON.toJson(variables.build());
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
    variables.put("modtype", StorageType.FREE_NOTE);
    variables.put("noteId", note.getId());
    variables.put("dateCreated", note.getDateCreated());
    variables.put("dateModified", note.getDateModified());
    variables.put("noteTitle", note.getTitle());
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
  private static String toData(FreeNote note) {

    ImmutableMap.Builder<String, Object> noteData =
            new ImmutableMap.Builder<String, Object>();

    noteData.put("modtype", StorageType.FREE_NOTE);
    noteData.put("noteId", note.getId());
    noteData.put("dateCreated", note.getDateCreated());
    noteData.put("dateModified", note.getDateModified());
    noteData.put("noteTitle", note.getTitle());
    List<String> tags = new ArrayList<>();
    for (Tag t : note.getTags()) {
      tags.add(t.getTag());
    }
    noteData.put("tags", tags);

    // add all module data
    List<Map<String, Object>> modulesList = new ArrayList<>();
    for (Module m : note.getModules()) {
      if (m instanceof Vocab) {
        Vocab vocab = (Vocab) m;
        modulesList.add(toData(vocab));
      } else if (m instanceof Conjugation) {
        Conjugation conjugation = (Conjugation) m;
        modulesList.add(toData(conjugation));
      } else if (m instanceof Note) {
        Note newNote = (Note) m;
        modulesList.add(toData(newNote));
      } else if (m instanceof AlertExclamation) {
        AlertExclamation alert = (AlertExclamation) m;
        modulesList.add(toData(alert));
      } else if (m instanceof Question) {
        Question question = (Question) m;
        modulesList.add(toData(question));
      }
    }

    // put module data into map
    noteData.put("moduleContent", modulesList);
    return GSON.toJson(noteData.build());
  }

  /**
   * Converts a Vocab module into a data map for JSON.
   * @param vocab
   *          the Vocab object to convert
   * @return a map of data from the Vocab object
   */
  private static Map<String, Object> toData(Vocab vocab) {

    ImmutableMap.Builder<String, Object> vocabData =
            new ImmutableMap.Builder<String, Object>();
    // pull information of vocab
    vocabData.put("modtype", StorageType.VOCAB);

    toData(vocab, vocabData);

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
    ImmutableMap.Builder<String, Object> conjugationData =
            new ImmutableMap.Builder<String, Object>();
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
    ImmutableMap.Builder<String, Object> tagData =
            new ImmutableMap.Builder<String, Object>();
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
    ImmutableMap.Builder<String, Object> alertData =
            new ImmutableMap.Builder<String, Object>();
    alertData.put("modtype", StorageType.ALERT_EXCLAMATION);
    toData(alert, alertData);
    alertData.put("content", alert.getText());
    return alertData.build();
  }

  /**
   * Converts a Question module into a data map for JSON.
   * @param question
   *          the Question object to convert
   * @return a map of dating storing the Question information
   */
  private static Map<String, Object> toData(Question question) {
    ImmutableMap.Builder<String, Object> questionData =
      new ImmutableMap.Builder<String, Object>();
    questionData.put("modtype", StorageType.QUESTION);
    toData(question, questionData);
    questionData.put("content", question.getText());
    return questionData.build();
  }

  /**
   * Converts a Note into a data map for JSON.
   * @param note
   *          the Note object to convert
   * @return the ImmutableMap to add the data to
   */
  private static Map<String, Object> toData(Note note) {
    ImmutableMap.Builder<String, Object> noteData =
            new ImmutableMap.Builder<String, Object>();
    noteData.put("modtype", StorageType.NOTE);
    toData(note, noteData);
    noteData.put("content", note.getText());
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
