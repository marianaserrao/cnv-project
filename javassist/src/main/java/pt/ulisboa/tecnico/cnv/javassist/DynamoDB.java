package pt.ulisboa.tecnico.cnv.javassist;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

public class DynamoDB {

    private String AWS_REGION = "us-east-1";
    private String[] TABLES = {"compressimage", "simulation", "insectwar"};

    private AmazonDynamoDB dynamoDB;

    public DynamoDB() throws Exception{
      this.dynamoDB = AmazonDynamoDBClientBuilder.standard()
          .withCredentials(new EnvironmentVariableCredentialsProvider())
          .withRegion(AWS_REGION)
          .build();  
      
      for(String tableName: TABLES){
        createMetricsTable(tableName);
      }
    }

    public void createMetricsTable(String tableName) throws Exception{
      try{

        // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
            .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
            .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
            .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
  
        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(dynamoDB, tableName);      
      } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }
    }

    public void addMetrics(Map<String, String> metrics) {
      String tableName = metrics.get("app");
      dynamoDB.putItem(new PutItemRequest(tableName, newItem(metrics)));
    }

    private static Map<String, AttributeValue> newItem(Map<String,String> metrics) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        for (Map.Entry<String, String> entry : metrics.entrySet()) {
            String value = entry.getValue();
            item.put(entry.getKey(), new AttributeValue(value));
        }
        return item;
    }
}
