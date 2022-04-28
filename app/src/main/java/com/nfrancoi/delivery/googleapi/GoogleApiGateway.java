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
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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


    private Map<String, String> directoryAndParentCacheMap = new HashMap<>();
    public String createDirectory(String directory, String parentDirectoryId) throws IOException {

        String directoryId = directoryAndParentCacheMap.get(directory+parentDirectoryId);
        if(directoryId != null){
            return directoryId;
        }

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
            directoryAndParentCacheMap.put(directory+parentDirectoryId, directories.get(0).getId());
            return directories.get(0).getId();
        }

        //
        //create the directory
        //
        if (parentDirectoryId == null) {
            File fileMetadata = new File();
            fileMetadata.setName(directory);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");


            File file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();

            directoryAndParentCacheMap.put(directory+parentDirectoryId, file.getId());

            return file.getId();
        } else {
            File fileMetadata = new File();
            fileMetadata.setName(directory);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            fileMetadata.setParents(Collections.singletonList(parentDirectoryId));

            File file = driveService.files().create(fileMetadata)
                    .setFields("id, parents")
                    .execute();

            directoryAndParentCacheMap.put(directory+parentDirectoryId, file.getId());
            return file.getId();
        }


    }

    public List<Integer> findLinesInSpreadsheet(String spreadSheetId, String sheetName, String value, String columnIndex) throws IOException {

        List<Integer> returnIndexes = new LinkedList<>();

        Sheets sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        String range = sheetName + "!" + columnIndex + ":" + columnIndex;
        ValueRange response = sheetService.spreadsheets().values()
                .get(spreadSheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values != null && !values.isEmpty()) {
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) != null && !values.get(i).isEmpty() && StringTools.Equals(values.get(i).get(0) + "", value)) {
                    returnIndexes.add(i);
                }
            }
        }

        return returnIndexes;
    }

    public void deleteLinesInSpreadsheet(String spreadSheetId, String sheetName, List<Integer> linesIds) throws IOException {
        Sheets sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();

        linesIds.sort(Collections.reverseOrder());

        Spreadsheet spreadSheet = sheetService.spreadsheets().get(spreadSheetId).execute();
        Sheet sheet = spreadSheet.getSheets().stream().filter(sheetlocal -> sheetlocal.getProperties().getTitle().equals(sheetName)).findFirst().get();
        Integer sheetId = sheet.getProperties().getSheetId();

        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();

        List<Request> requests = new ArrayList<Request>();
        for (Integer lineId : linesIds) {
            Request request = new Request();
            DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();
            DimensionRange dimensionRange = new DimensionRange();
            dimensionRange.setDimension("ROWS");
            dimensionRange.setStartIndex(lineId);
            dimensionRange.setEndIndex(lineId + 1);
            dimensionRange.setSheetId(sheetId);
            deleteDimensionRequest.setRange(dimensionRange);
            request.setDeleteDimension(deleteDimensionRequest);

            requests.add(request);
        }

        content.setRequests(requests);

        sheetService.spreadsheets().batchUpdate(spreadSheetId, content).execute();


    }

    public int appendToSpreadSheet(String spreadSheetId, String sheetName, List<List<Object>> values) throws IOException {
        if (values == null || values.size() == 0) {
            return 0;
        }
        Sheets sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        //append
        ValueRange body = new ValueRange()
                .setValues(values);
        AppendValuesResponse result =
                sheetService.spreadsheets().values().append(spreadSheetId, sheetName, body)
                        .setValueInputOption("RAW")
                        .execute();
        Integer updatedRows = result.getUpdates().getUpdatedRows();

        return updatedRows == null ? 0 : updatedRows;

    }

    public boolean isSheetExistInSpreadSheet(String spreadSheetId, String sheetName) throws IOException {
        Sheets sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        Spreadsheet spreadSheet = sheetService.spreadsheets().get(spreadSheetId).execute();
        boolean sheetNameExists = spreadSheet.getSheets().stream().filter(sheet -> sheet.getProperties().getTitle().equals(sheetName)).findAny().map(sheet -> true).orElse(false);

        return sheetNameExists;
    }


    public void addSheetToSpreadSheet(String spreadSheetId, String newSheetName) throws IOException {

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


    public String getOrCreateSpreadSheetIdInFolder(String parentFolderId, String sheetName) throws IOException {
        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.spreadsheet' AND trashed = false AND name = '" + sheetName + "' AND '" + parentFolderId + "' in parents")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                return file.getId();
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        //not found
        File fileMetadata = new File();
        fileMetadata.setName(sheetName);
        fileMetadata.setParents(Collections.singletonList(parentFolderId));
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
        File file = driveService.files().create(fileMetadata)
                .setFields("id, parents")
                .execute();
        return file.getId();
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

    //
    // TODO remove delivery specific code from the gateway
    //
    public ValueRange getPointOfDeliveriesGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "PointsDeLivraison").execute();

        return result;
    }


    public ValueRange getProductsGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        // ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Produits").setKey("AIzaSyCV_8QInXhAn9q92QsfJhRcaIRndDswOMc").execute();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Produits").execute();
        return result;
    }

    public ValueRange getCompaniesGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Entreprise").execute();
        return result;
    }

    public ValueRange getEmployeeGoogleSheet() throws IOException {
        String spreadsheetId = getConfigSpreadSheetIdFromPrefs();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();
        ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "Livreurs").execute();
        return result;
    }


    private String getConfigSpreadSheetIdFromPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).getString("sync_spreadsheet_id", "");

    }

    public void deleteFileByIdOnGoogleDrive(String fileId) throws IOException {
        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName("Delivery")
                .build();

        driveService.files().delete(fileId).execute();
    }

    public String getFileIdByNameOnGoogleDrive(String fileName) throws IOException {
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
                return file.getId();
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return null;


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


}


