package pt.ulisboa.tecnico.cnv.javassist.dynamodb;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private String[] TABLES = {"compressimage", "simulate", "insectwar"};

    private AmazonDynamoDB dynamoDB;

    public DynamoDB() throws Exception{
      try{
        this.dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new EnvironmentVariableCredentialsProvider())
            .withRegion(AWS_REGION)
            .build();  
        
        for(String tableName: TABLES){
          createMetricsTable(tableName);
        }
      }catch (AmazonClientException ace) {
          System.out.println("Caught an AmazonClientException, which means the client encountered "
                  + "a serious internal problem while trying to communicate with AWS, "
                  + "such as not being able to access the network.");
          System.out.println("Error Message: " + ace.getMessage());
      }
    }

    public void createMetricsTable(String tableName) throws Exception{
      try{
        // Create a table with a primary hash key named 'id', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest()
                          .withTableName(tableName)
                          .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
                          .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
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
        UUID uuid = UUID.randomUUID();
        item.put("id", new AttributeValue(uuid.toString()));
        for (Map.Entry<String, String> entry : metrics.entrySet()) {
            String key = entry.getKey();
            if(key!="app"){
              String value = entry.getValue();
              item.put(key, new AttributeValue(value));
            }
        }
        return item;
    }
}
