package edu.brown.cs.athenia.gui;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.athenia.data.User;
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
  private final User USER;

  public GUICommand(User user) {
    this.USER = user;
  }

  /**
   * GET request handler for the sign-in page of Athenia. Prompts the user to
   * sign-in via the Google API.
   */
  public class SignInHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("title", "Athenia").build();
      // TODO figure out what landing area they should face
      // 1. google sign in
      // a. prompt the user to sign-in, pull their info
      // b. load in this information to the backend (database stuff)
      // c. use this info to prompt the user to change the language
      // s. use this info to set the user info and go to home
      // 2. regular home page
      return new ModelAndView(variables, "landing.ftl");
    }
  }

  /**
   * GET request handler which pulls the different languages the user has so far
   * logged in the app and displays the prompt to choose from the options, if
   * they exist.
   */
  public class LanguagePromptHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull and handle the following information
      // 1. pull out all of the languages the user has worked in
      // 2. use their choice to load in the appropriate databases
      // into the backend
      return new ModelAndView(variables, "...");
    }
  }

  /**
   * GET request handler which pulls the most recent activity of the appropriate
   * user and presents this information on the home page of Athenia.
   */
  public class HomePageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull the following information
      // 1. pull the recent activity of the user from the database or user
      // object
      // 2. parse the information and format to send to the front end
      return new ModelAndView(variables, "...");
    }
  }

  /**
   * GET request handler which pulls all of the vocabulary information saved by
   * the user in the database and formats it to send to the front end to display
   * on the "Vocabulary" landing page.
   */
  public class VocabularyLandingPageHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      // TODO: pull the information from the user
      // 1. not much here... just know that the user is on the vocab page
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: parse the info and present to front end
      // 1. pull out all of the vocabulary information from the backend
      // and format appropriately for the front end to use
      return new ModelAndView(variables, "...");
    }
  }

  /**
   * POST request handler for adding, updating, or deleting vocabulary
   * information. Called whenever the user edits anything on the Vocabulary
   * landing page, a specific vocabulary page, or in a FreeNotes page.
   */
  public class UpdateVocabularyHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      // TODO: pull out the information of the vocabulary module and act
      // according to the following:
      // 1. adding new vocab module
      // 2. update old vocab module
      // 3. deleting old vocab module
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
    public ModelAndView handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      // TODO: parse out the tag label the user wishes to view (tag ID, tag
      // Name)
      Map<String, Object> variables = new ImmutableMap.Builder<String, Object>()
          .put("...", "...").build();
      // TODO: pull out all of the modules for the given tab
      // 1. format this information to send to the front end to display
      // on the generated "Tag" page
      return new ModelAndView(variables, "...");
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
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      // TODO: pull out the information of the specific tag being changed:
      // 1. adding new tag (tag ID, tag name)
      // 2. updating previous tag (tag ID, tag name)
      // 3. deleting previous tag (tag ID, tag name)
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
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
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
    public ModelAndView handle(Request req, Response res) {
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
    public String handle(Request req, Response res) {
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
    public ModelAndView handle(Request req, Response res) {
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
    public ModelAndView handle(Request req, Response res) {
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
    public String handle(Request req, Response res) {
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
    public String handle(Request req, Response res) {
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
    public ModelAndView handle(Request req, Response res) {
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
    public ModelAndView handle(Request req, Response res) {
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

}
