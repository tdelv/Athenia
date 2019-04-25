package edu.brown.cs.athenia.driveapi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class GoogleDriveApi {
  // Variables used with generating authorization code flow
  private static NetHttpTransport HTTP_TRANSPORT;
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final List<String> SCOPES = Collections
          .singletonList(DriveScopes.DRIVE_APPDATA);
  private static final String APPLICATION_NAME = "Athenia";

  /**
   * Generates a flow for use with Google API.
   * @return The authorization flow.
   * @throws IOException when credentials.json can't be loaded.
   * @throws GeneralSecurityException for other reasons.
   */
  public static GoogleAuthorizationCodeFlow getFlow() throws IOException, GeneralSecurityException {
    // Generate transport
    HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    // Generate credentials
    InputStream in = GoogleDriveApi.class
            .getResourceAsStream(CREDENTIALS_FILE_PATH);
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
            new InputStreamReader(in));

    // Create flow for Google
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            clientSecrets,
            SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();

    return flow;
  }

  /**
   * Loads the credentials of the given userId. Returns null if no token.
   * @param userId the user to load credentials for.
   * @return the credentials, or null if no token.
   * @throws IOException
   * @throws GeneralSecurityException
   */
  private static Credential getCredential(String userId) throws IOException, GeneralSecurityException {
    return getFlow().loadCredential(userId);
  }

  /**
   * Setup a new Drive service.
   * @param userId The userId
   * @return the service.
   * @throws IOException
   * @throws GeneralSecurityException
   */
  private static Drive getService(String userId) throws IOException,
      GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport
        .newTrustedTransport();
    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        getCredential(userId)).setApplicationName(APPLICATION_NAME).build();
    return service;
  }

  private static void setupDriveFile(Drive service) throws IOException {
    File metaData = new File().setName("userData.sqlite3")
        .setId("userData.sqlite3");

    service.files().create(metaData);
  }

  public static boolean isLoggedIn(String userId) throws IOException, GeneralSecurityException {
    return getCredential(userId) != null;
  }

  public static String getUrl(String state) throws IOException, GeneralSecurityException {
    return getFlow().newAuthorizationUrl()
            .setRedirectUri("https://athenia.herokuapp.com/validate")
            .setState(state)            // Prevent request forgery
            .build();
  }

  public static void createCredential(String userId, String code) throws IOException, GeneralSecurityException {
    // Create flow for Google
    GoogleAuthorizationCodeFlow flow = GoogleDriveApi.getFlow();

    // Get token from authentication code
    final TokenResponse tokenResponse =
            flow.newTokenRequest(code)
                    .setRedirectUri("https://athenia.herokuapp.com/validate")
                    .execute();

    flow.createAndStoreCredential(tokenResponse, userId);
  }


  // Loading and updating of database file

  private static final Map<String, java.io.File> FILE_MAP = new HashMap<>();

  public static java.io.File getDataBase(String userAuth)
          throws DriveApiException {
    if (FILE_MAP.containsKey(userAuth)) {
      return FILE_MAP.get(userAuth);
    }

    try {
      java.io.File file = new java.io.File(
              "/userData/" + userAuth + ".sqlite3");

      Drive service = getService(userAuth);
      if (service.files().list().containsKey("userData.sqlite3")) {
        OutputStream outputStream = new FileOutputStream(file);
        service.files().get("userData.sqlite3")
                .executeMediaAndDownloadTo(outputStream);
      } else {
        setupDriveFile(service);
      }

      return FILE_MAP.put(userAuth, file);
    } catch (IOException | GeneralSecurityException e) {
      throw new DriveApiException(e);
    }
  }

  public static void setDataBase(String userAuth, java.io.File dataBase)
          throws DriveApiException {
    FILE_MAP.put(userAuth, dataBase);

    try {
      Drive service = getService(userAuth);

      if (!service.files().list().containsKey("userData.sqlite3")) {
        setupDriveFile(service);
      }

      File driveFile = service.files().get("userData.sqlite3").execute();

      FileContent fileContent = new FileContent("application/x-sqlite3",
              dataBase);

      service.files().update("userData.sqlite3", driveFile, fileContent);
    } catch (IOException | GeneralSecurityException e) {
      throw new DriveApiException(e);
    }

  }
}
