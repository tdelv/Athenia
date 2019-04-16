package edu.brown.cs.athenia.driveapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
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
    private static final Map<String, File> FILE_MAP = new HashMap<>();

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

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Get stored credentials if already received, otherwise prompt user for authorization.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @param userAuth The user that we want credentials from.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String userAuth) throws IOException {
        if (CREDENTIAL_MAP.containsKey(userAuth)) {
            return CREDENTIAL_MAP.get(userAuth);
        }

        return CREDENTIAL_MAP.put(userAuth, getCredentials(HTTP_TRANSPORT));
    }

    /**
     * Setup a new Drive service.
     * @param userAuth The user that we want credentials from.
     * @return the service.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static Drive setup(String userAuth) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, userAuth))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    public static File getDataBase(String userAuth) throws DriveApiException {
        if (FILE_MAP.containsKey(userAuth)) {
            return FILE_MAP.get(userAuth);
        }

        try {
            File file = new File("/userData/" + userAuth + ".sqlite3");
            OutputStream outputStream = new FileOutputStream(file);

            Drive service = setup(userAuth);
            service.files().get("userData.sqlite3").executeMediaAndDownloadTo(outputStream);

            return FILE_MAP.put(userAuth, file);
        } catch (IOException | GeneralSecurityException e) {
            throw new DriveApiException(e);
        }
    }

    public static void setDataBase(String userAuth, File dataBase) throws DriveApiException {
        FILE_MAP.put(userAuth, dataBase);


    }
}
