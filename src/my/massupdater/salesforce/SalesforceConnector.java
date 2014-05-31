/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.massupdater.salesforce;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dan
 */
public class SalesforceConnector {

    private static SalesforceConnector instance;
    PartnerConnection connection;
    protected String errorMessage;
    protected String successMessage;
    protected String[][] queryResult;
    protected String[] queryFields;
    protected String queryObject;

    private SalesforceConnector() {
        errorMessage = "";
    }

    ;
    
    public static SalesforceConnector getInstance() {
        if (instance == null) {
            instance = new SalesforceConnector();
        }

        return instance;
    }

    public boolean login(String username, String password, String endpoint) {
        boolean success = false;
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Endpoint: " + endpoint);

        try {
            ConnectorConfig config = new ConnectorConfig();
            config.setUsername(username);
            config.setPassword(password);

            config.setAuthEndpoint("https://" + endpoint + "/services/Soap/u/30.0");

            connection = new PartnerConnection(config);
            printUserInfo(config);

            success = true;
        } catch (ConnectionException ce) {
            ce.printStackTrace();
            System.out.println("Exception: " + ce.toString());
            System.out.println("Exception: " + ce.getMessage());
            handleConnectionException(ce);
        }

        return success;
    }

    public boolean query(String soqlQuery) {
        String queryObject;
        String fieldPart = StringUtils.substringBetween(soqlQuery.toUpperCase(), "SELECT", "FROM");

        queryFields = fieldPart.split(",");
        if (soqlQuery.contains("WHERE")) {
            queryObject = StringUtils.substringBetween(soqlQuery.toUpperCase(), "FROM ", " WHERE");
        } else {
            queryObject = StringUtils.substringAfter(soqlQuery, "FROM ");
        }
        //query real Names to match headers
        try {
            DescribeSObjectResult sResult = connection.describeSObject(queryObject.trim());
            System.out.println("Object: " + sResult.getName());
            this.queryObject = sResult.getName();
            for (Field sObjectField : sResult.getFields()) {
                for (int i = 0; i < queryFields.length; i++) {
                    if (queryFields[i].toUpperCase().trim().equals(sObjectField.getName().toUpperCase())) {
                        System.out.println(sObjectField.getName());
                        queryFields[i] = sObjectField.getName();
                    }
                }
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        System.out.println(fieldPart);
        System.out.println(queryObject);
        try {
            QueryResult qr = connection.query(soqlQuery);
            System.out.println(qr.getSize());
            queryResult = new String[qr.getSize()][queryFields.length];
            SObject[] records = qr.getRecords();
            for (int i = 0; i < qr.getSize(); i++) {
                SObject record = records[i];
                System.out.println(record.getId());
                for (int j = 0; j < queryFields.length; j++) {
                    queryResult[i][j] = String.valueOf(record.getField(queryFields[j].trim()));
                }

            }

        } catch (ConnectionException ce) {
            handleConnectionException(ce);
            return false;
        }

        return true;
    }

    public String[][] getQueryResult() {
        return queryResult;
    }

    public String[] getQueryFields() {
        return queryFields;
    }

    public Boolean update(HashMap<String, HashMap<String, String>> idMap2ValuesMap) {
        SObject[] objects = new SObject[idMap2ValuesMap.size()];
        int count = 0;
        boolean success = true;
        for (String id : idMap2ValuesMap.keySet()) {
            SObject obj = new SObject();
            obj.setType(this.queryObject);
            obj.setId(id);
            for (String fieldName : idMap2ValuesMap.get(id).keySet()) {
                obj.setField(fieldName, idMap2ValuesMap.get(id).get(fieldName));
            }
            objects[count] = obj;
            count++;
        }

        try {
            SaveResult[] saveResults = connection.update(objects);
            
            int successCount = 0;

            for (SaveResult res : saveResults) {
                if (res.isSuccess()) {
                    successCount++;
                } else {
                    errorMessage = "";
                    for (int i = 0; i < res.getErrors().length; i++) {
                        success = false;
                        errorMessage += res.getErrors()[i] + " ";
                    }
                }
            }
            successMessage = successCount + " Objects of Type " + queryObject + " were updated";
        } catch (ConnectionException ce) {
            handleConnectionException(ce);
        }

        return success;
    }

    public String getLastSuccessMessage() {
        return successMessage;
    }

    public String getLastErrorMessage() {
        return errorMessage;
    }

    private void printUserInfo(ConnectorConfig config) {
        try {
            GetUserInfoResult userInfo = connection.getUserInfo();

            System.out.println("\nLogging in ...\n");
            System.out.println("UserID: " + userInfo.getUserId());
            System.out.println("User Full Name: " + userInfo.getUserFullName());
            System.out.println("User Email: " + userInfo.getUserEmail());
            System.out.println();
            System.out.println("SessionID: " + config.getSessionId());
            System.out.println("Auth End Point: " + config.getAuthEndpoint());
            System.out.println("Service End Point: " + config.getServiceEndpoint());
            System.out.println();
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    private void handleConnectionException(ConnectionException ce) {
        if (ce.getMessage() == null) {
            errorMessage = StringUtils.substringBetween(ce.toString(), "exceptionMessage='", "'");
        } else {
            errorMessage = ce.getMessage();
        }
    }
}
