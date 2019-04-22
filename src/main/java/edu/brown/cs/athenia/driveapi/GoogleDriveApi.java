package edu.brown.cs.athenia.driveapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDriveApi {
    private static final String APPLICATION_NAME = "Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final Map<String, Credential> CREDENTIAL_MAP = new HashMap<>();
    private static final Map<String, java.io.File> FILE_MAP = new HashMap<>();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_APPDATA);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveApi.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        VerificationCodeReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(1000)
                .setHost("herokuapp.com")
                .setLandingPages(null, null)
                .setCallbackPath(null)
                .build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Sets up a server for the user to login and authorize credentials.
     * @param cookie the user to log in.
     */
    public static void login(String cookie) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(HTTP_TRANSPORT);
        CREDENTIAL_MAP.put(cookie, credential);
    }

    /**
     * Checks if a given user is authenticated.
     * @param cookie the user to be checked.
     * @return whether they are authenticated.
     */
    public static boolean isAuthenticated(String cookie) {
        return CREDENTIAL_MAP.containsKey(cookie);
    }

    /**
     * Get stored credentials if already received, otherwise prompt user for authorization.
     * @param userAuth The user that we want credentials from.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     * @throws UnauthenticatedUserException If the user does not have loaded credentials.
     */
    private static Credential getCredentials(String userAuth)
            throws IOException, UnauthenticatedUserException {
        if (!CREDENTIAL_MAP.containsKey(userAuth)) {
            throw new UnauthenticatedUserException();
        }

        return CREDENTIAL_MAP.get(userAuth);
    }

    /**
     * Setup a new Drive service.
     * @param userAuth The user that we want credentials from.
     * @return the service.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static Drive getService(String userAuth)
            throws IOException, GeneralSecurityException, UnauthenticatedUserException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(userAuth))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    private static void setupDriveFile(Drive service) throws IOException {
        File metaData = new File()
                .setName("userData.sqlite3")
                .setId("userData.sqlite3");

        service.files().create(metaData);
    }

    public static java.io.File getDataBase(String userAuth) throws DriveApiException {
        if (FILE_MAP.containsKey(userAuth)) {
            return FILE_MAP.get(userAuth);
        }

        try {
            java.io.File file = new java.io.File("/userData/" + userAuth + ".sqlite3");

            Drive service = getService(userAuth);
            if (service.files().list().containsKey("userData.sqlite3")) {
                OutputStream outputStream = new FileOutputStream(file);
                service.files().get("userData.sqlite3").executeMediaAndDownloadTo(outputStream);
            } else {
                setupDriveFile(service);
            }

            return FILE_MAP.put(userAuth, file);
        } catch (IOException | GeneralSecurityException e) {
            throw new DriveApiException(e);
        }
    }

    public static void setDataBase(String userAuth, java.io.File dataBase) throws DriveApiException {
        FILE_MAP.put(userAuth, dataBase);

        try {
            Drive service = getService(userAuth);

            if (!service.files().list().containsKey("userData.sqlite3")) {
                setupDriveFile(service);
            }

            File driveFile = service.files().get("userData.sqlite3").execute();

            FileContent fileContent = new FileContent("application/x-sqlite3", dataBase);

            service.files().update("userData.sqlite3", driveFile, fileContent);
        } catch (IOException | GeneralSecurityException e) {
            throw new DriveApiException(e);
        }

    }
}
