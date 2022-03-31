package com.nfrancoi.delivery.googleapi;

import android.accounts.Account;

import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.nfrancoi.delivery.DeliveryApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleApiGateway {

    //
    // Singleton
    //
    private static GoogleApiGateway googleApiGateway;

    public static synchronized GoogleApiGateway getInstance() {
        if (googleApiGateway == null) {
            googleApiGateway = new GoogleApiGateway();
        }
        return googleApiGateway;
    }

    private GoogleApiGateway() {

    }

    private String spreadSheetId;

    private JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();


    private List<File> getSpreadSheets() throws IOException {
        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();

        Drive.Files.List request = driveService.files().list()
                .setPageSize(10)
                // Available Query parameters here:
                //https://developers.google.com/drive/v3/web/search-parameters
                .setQ("mimeType = 'application/vnd.google-apps.spreadsheet'")
                .setFields("nextPageToken, files(id, name)");
        FileList result = request.execute();
        List<File> files = result.getFiles();

        return files;

    }


    public Scope[] scopes = new Scope[]{
            new Scope(SheetsScopes.SPREADSHEETS)
            , new Scope(Scopes.DRIVE_APPFOLDER)
            , new Scope(DriveScopes.DRIVE_FILE)
            , new Scope(DriveScopes.DRIVE_METADATA_READONLY)};

    private String[] scopeStrings = new String[]{
            SheetsScopes.SPREADSHEETS
            , Scopes.DRIVE_APPFOLDER
            , DriveScopes.DRIVE_FILE
            , DriveScopes.DRIVE_METADATA_READONLY};

    private GoogleAccountCredential credential;


    private GoogleAccountCredential getCredential() {
        GoogleSignInAccount gsoAccount = GoogleSignIn.getLastSignedInAccount(DeliveryApplication.getInstance());
        if (gsoAccount == null) return null;
        Account account = gsoAccount.getAccount();
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(DeliveryApplication.getInstance(), Arrays.asList(scopeStrings));
        credential.setBackOff(new ExponentialBackOff());
        credential.setSelectedAccount(account);

        return credential;
    }

    public boolean checkAccountDriveAccess() throws UserRecoverableAuthIOException {

        try {
            getSpreadSheets();
        } catch (UserRecoverableAuthIOException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getSpreadSheetIdByName(String spreadSheetName) {

        try {
            Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                    .setApplicationName("Delivery")
                    .build();

            Drive.Files.List request = driveService.files().list()
                    .setPageSize(10)
                    // Available Query parameters here:
                    //https://developers.google.com/drive/v3/web/search-parameters
                    .setQ("mimeType = 'application/vnd.google-apps.spreadsheet'")
                    .setFields("nextPageToken, files(id, name)");
            FileList result = request.execute();

            File fileFound = result.getFiles().stream().filter(file -> {
                boolean found = spreadSheetName.equals(file.getName());
                return found;
            }).findAny().orElse(null);

            if (fileFound != null) {
                return fileFound.getId();
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOrCreateSpreadSheetId(String pathFromRoot, String spreadSheetName) throws IOException {

        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        String parentFolderId = getFolderIdFromPath(driveService, pathFromRoot);
        if (parentFolderId == null) {
            throw new IllegalStateException("Folder Notes not created in google drive");
        }
        String sheetId = getSheetIdInFolder(driveService, parentFolderId, spreadSheetName);
        if (sheetId == null) {
            sheetId = this.createSheetIdInFolder(driveService, parentFolderId, spreadSheetName);

        }
        return sheetId;

    }


    public String createDirectory(String directory, String parentDirectoryId) throws IOException {
        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();

        //
        //check if the directory already exists
        //
        String query = parentDirectoryId == null ?
                "mimeType='application/vnd.google-apps.folder' AND trashed = false AND name = '" + directory + "'"
                : "mimeType='application/vnd.google-apps.folder' AND trashed = false AND name = '" + directory + "' AND '" + parentDirectoryId + "' in parents";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .execute();

        List<File> directories = result.getFiles();
        if (directories.size() > 0) {
            return directories.get(0).getId();
        }

        //
        //create the directory
        //
        if(parentDirectoryId == null) {
            File fileMetadata = new File();
            fileMetadata.setName(directory);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");


            File file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            return file.getId();
        }else{
            File fileMetadata = new File();
            fileMetadata.setName(directory);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            fileMetadata.setParents(Collections.singletonList(parentDirectoryId));

            File file = driveService.files().create(fileMetadata)
                    .setFields("id, parents")
                    .execute();
            return file.getId();
        }


    }


    private String createSheetIdInFolder(Drive driveService, String parentFolderId, String fileName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(parentFolderId));
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
        File file = driveService.files().create(fileMetadata)
                .setFields("id, parents")
                .execute();
        return file.getId();
    }


    public void appendToSpreadSheet(String spreadSheetId, String sheetName, List<List<Object>> values) throws IOException {
        Sheets sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();

        //check if sheetName exists
        Spreadsheet spreadSheet = sheetService.spreadsheets().get(spreadSheetId).execute();
        boolean sheetNameExists = spreadSheet.getSheets().stream().filter(sheet -> sheet.getProperties().getTitle().equals(sheetName)).findAny().map(sheet -> true).orElse(false);
        if (!sheetNameExists) {
            this.addSheetToSpreadSheet(spreadSheetId, sheetName);
        }

        //clear data if already updated
        /* not working withdata filter
        DataFilter clearFilter = new DataFilter();

        clearFilter.setA1Range("" + values.get(0).get(0));

        BatchClearValuesByDataFilterRequest batchClear = new BatchClearValuesByDataFilterRequest();
        batchClear.setDataFilters(Arrays.asList(clearFilter));
        BatchClearValuesByDataFilterResponse response = sheetService.spreadsheets().values().batchClearByDataFilter(spreadSheetId, batchClear).execute();*/

        //append
        ValueRange body = new ValueRange()
                .setValues(values);
        AppendValuesResponse result =
                sheetService.spreadsheets().values().append(spreadSheetId, sheetName, body)
                        .setValueInputOption("RAW")
                        .execute();

        int cellUpdated = result.getUpdates().getUpdatedRows();
        System.out.printf("%d cells appended.", cellUpdated);
    }


    private void addSheetToSpreadSheet(String spreadSheetId, String newSheetName) throws IOException {

        //Create a new AddSheetRequest
        AddSheetRequest addSheetRequest = new AddSheetRequest();
        SheetProperties sheetProperties = new SheetProperties();

        //Add the sheetName to the sheetProperties
        addSheetRequest.setProperties(sheetProperties);
        addSheetRequest.setProperties(sheetProperties.setTitle(newSheetName));

        //Create batchUpdateSpreadsheetRequest
        BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();

        //Create requestList and set it on the batchUpdateSpreadsheetRequest
        List<Request> requestsList = new ArrayList<Request>();
        batchUpdateSpreadsheetRequest.setRequests(requestsList);

        //Create a new request with containing the addSheetRequest and add it to the requestList
        Request request = new Request();
        request.setAddSheet(addSheetRequest);
        requestsList.add(request);

        //Add the requestList to the batchUpdateSpreadsheetRequest
        batchUpdateSpreadsheetRequest.setRequests(requestsList);

        //Call the sheets API to execute the batchUpdate
        Sheets sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        sheetService.spreadsheets().batchUpdate(spreadSheetId, batchUpdateSpreadsheetRequest).execute();
    }


    private String getSheetIdInFolder(Drive driveService, String folderId, String sheetName) throws IOException {
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.spreadsheet' AND trashed = false AND name = '" + sheetName + "' AND '" + folderId + "' in parents")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                return file.getId();
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return null;
    }

    private String getFolderIdFromPath(Drive driveService, String folderName) throws IOException {
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' AND trashed = false  AND name = '" + folderName + "'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                return file.getId();
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return null;
    }

    public ValueRange getPointOfDeliveriesGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "PointsDeLivraison").setKey("AIzaSyCV_8QInXhAn9q92QsfJhRcaIRndDswOMc").execute();

        return result;
    }


    public ValueRange getProductsGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Produits").setKey("AIzaSyCV_8QInXhAn9q92QsfJhRcaIRndDswOMc").execute();
        return result;
    }

    public ValueRange getCompaniesGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Entreprise").setKey("AIzaSyCV_8QInXhAn9q92QsfJhRcaIRndDswOMc").execute();
        return result;
    }

    public ValueRange getEmployeeGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Livreurs").setKey("AIzaSyCV_8QInXhAn9q92QsfJhRcaIRndDswOMc").execute();
        return result;
    }


    private String getConfigSpreadSheetIdFromPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).getString("sync_spreadsheet_id", "");

    }

    public boolean isFileExistsByNameOnGoogleDrive(String fileName) throws IOException {


        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("trashed = false  AND name = '" + fileName + "'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                return true;
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return false;






/*


            FileList result = driveService.files().list()
                    .setQ("trashed = false AND name = '" + fileName)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();

            if(files == null || files.size()==0){
                return false;
            }else{
                return true;
            }*/

    }
    public String savePdfFileOnGoogleDrive(java.io.File file, String parentDirectory) throws IOException {

        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();

        FileContent mediaContent = new FileContent("application/pdf", file);
        if (parentDirectory == null) {
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());

            File driveFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            return driveFile.getId();
        } else {
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setParents(Collections.singletonList(parentDirectory));
            File driveFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
            return driveFile.getId();

        }
    }

    public ValueRange saveNoteDetailsOnGoogleDrive() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Entreprise").setKey("AIzaSyCV_8QInXhAn9q92QsfJhRcaIRndDswOMc").execute();
        return result;
    }


}


