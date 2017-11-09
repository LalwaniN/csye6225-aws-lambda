
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class dynamodbentry implements RequestHandler<SNSEvent, Object> {

  public Object handleRequest(SNSEvent request, Context context) {

    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

    context.getLogger().log("Invocation started: " + timeStamp);

    timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

    context.getLogger().log("Invocation completed: " + timeStamp);

    try {
      final AmazonDynamoDBClient client = new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider());
      client.withRegion(Regions.US_EAST_1); // specify the region you created the table in.
      context.getLogger().log("client created!");

      DynamoDB dynamoDB = new DynamoDB(client);

      Table table = dynamoDB.getTable("csye6225");
      context.getLogger().log("table found!");
      final Item item = new Item()
              .withPrimaryKey("id", request.getRecords().get(0).getSNS().getMessageId().toString()) // Every item gets a unique id
              .withString("userName", request.getRecords().get(0).getSNS().getMessage().toString())
              .withNumber("expiry", 150890000);
      table.putItem(item);

      //Item item = new Item().withPrimaryKey("id", "" + request.getRecords().get(0).getSNS().getMessageId())
      //.withString("UserName", "" + request.getRecords().get(0).getSNS().getMessage())
      // .withNumber("ttlexpiry", 150885000);

      //Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
      //item.put("id", new AttributeValue().withN(request.getRecords().get(0).getSNS().getMessageId().toString()));
      //item.put("UserName", new AttributeValue(request.getRecords().get(0).getSNS().getMessage().toString()));
      //item.put("expiry", new AttributeValue("150990000"));

      //context.getLogger().log("map of values created!");
      //PutItemRequest putItemRequest = new PutItemRequest();
      //putItemRequest.setTableName("csye6225");
      //putItemRequest.setItem(item);
      //context.getLogger().log("putItemRequest created!");
      //dbItem.put(request.getRecords().get(0).getSNS().getMessageId().toString(),request.getRecords().get(0).getSNS().getMessage().toString());
      //client.putItem(putItemRequest);
      //PutItemOutcome outcome = table.putItem(item);
      context.getLogger().log("Added to dynamo Db");
    }catch(Exception e){
      context.getLogger().log("Error:"+e.getMessage());
    }

    return null;
  }

}
