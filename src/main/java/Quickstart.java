import com.google.api.client.auth.oauth2.Credential;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;


public class Quickstart {
    /** Application name. */
    private static final String APPLICATION_NAME = "Google Sheets test";
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);
    
    private static final java.io.File DATA_STORE_DIR = new java.io.File( System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static String emailAddress = "administrador@eforcers.com.co";
    private static String SERVICE_ACCOUNT_EMAIL = "delegate@gam-prueba-160819.iam.gserviceaccount.com";
    private static String FILE_STORE_CREDENTIAL = "/home/poxstone/Projects/java-spreadsheets/src/main/resources/delegate_service_account.p12";
    private static Credential credential;
    private static List<String> SCOPES_ARRAY = Arrays.asList("https://www.googleapis.com/auth/spreadsheets");
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

	public static Credential delegateAuthentication() throws FileNotFoundException, IOException, GeneralSecurityException {
		
		credential = new GoogleCredential.Builder()
				.setTransport(HTTP_TRANSPORT)
			    .setJsonFactory(JSON_FACTORY)
	            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
	            .setServiceAccountPrivateKeyFromP12File(new File(FILE_STORE_CREDENTIAL))
	            .setServiceAccountScopes(SCOPES_ARRAY)
	            .setServiceAccountUser(emailAddress)
	            .build();
	            credential.refreshToken();
		
		return credential;
	}

    public static Credential authorize() throws IOException {
        InputStream in = Quickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
																	                .setDataStoreFactory(DATA_STORE_FACTORY)
																	                .setAccessType("offline")
																	                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        
        return credential;
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = delegateAuthentication();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
					                .setApplicationName(APPLICATION_NAME)
					                .build();
    }
    
	static Boolean storeMysql(List<List<Object>> sheetRows) throws Exception {
    	
    	Boolean success = true;
        Connection connect = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
    	
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/feedback?user=root&password=mypass");
            
            if (sheetRows == null || sheetRows.size() == 0) {
                System.out.println("No data found.");
                
            } else {
            	
              for (List<Object> row : sheetRows) {

          		preparedStatement = connect.prepareStatement("insert into  feedback.users values (?, ?, ?, ?)");
          		
            	if (row.size() == 4) {
            		
            		for (Integer i=0; i < 4; i++) {
            			String Value = (String) row.get(i);
            			preparedStatement.setString(i+1, Value);
            		}
            		preparedStatement.executeUpdate();
            	}
              }
            }

        } catch (Exception e) {
        	success = false;
            throw e;
            
        } finally {
        	try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connect != null) connect.close();
                
            } catch (Exception e) {
            	throw e;
            }
        }
        
        return success;
    }

    public static void main(String[] args) throws Exception {
    	
        Sheets service = getSheetsService();
        
        String spreadsheetId = "1qlzsDrSbXeuEgN0m4SJi8SDSgsXGmxhlZ514cgvhyCQ";
        String range = "reg!A2:D";
        
        ValueRange response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute();
        
        List<List<Object>> values = response.getValues();
        
        if(storeMysql(values)) {
        	System.out.println("Finalizó exitosamente");
        }
    
    }
}
