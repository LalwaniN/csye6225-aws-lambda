import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import javax.swing.text.html.HTML;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class SendMail implements RequestHandler<SNSEvent, Object> {

  public Object handleRequest(SNSEvent request, Context context) {

    context.getLogger().log("calling dynamodb");

    try{


      saveToken(request.getRecords().get(0).getSNS().getMessage(),context.getAwsRequestId());

      context.getLogger().log("After successfully storing the token");

      String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

      context.getLogger().log("Invocation started: " + timeStamp);

      context.getLogger().log("1: " + (request == null));

      context.getLogger().log("2: " + (request.getRecords().size()));

      context.getLogger().log(request.getRecords().get(0).getSNS().getMessage());

      timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

      context.getLogger().log("Invocation completed: " + timeStamp);
    } catch(Exception e){

      context.getLogger().log("error while saving in dynamodb" + e.getMessage());
    }
    return null;
  }

  public void saveToken(String userName, String requestId){

      AmazonDynamoDB dbClient = AmazonDynamoDBClientBuilder.standard()
              .withRegion(Regions.US_EAST_1)
              .build();

      DynamoDB dynamoDB = new DynamoDB(dbClient);

      Table table = dynamoDB.getTable("csye6225");
      Item userToken = table.getItem("id",userName);

      if(userToken == null){

          long time = Instant.now().getEpochSecond() + 60;

          Map<String, AttributeValue> item = new HashMap<>();
          item.put("id", new AttributeValue((String) userName));
          item.put("token", new AttributeValue((String) requestId));
          item.put("tokenexpiry",new AttributeValue(String.valueOf(time)));

          dbClient.putItem("csye6225",item);

          //send mail
          sendMail();

      }


    }

    public void sendMail(){

         String HTMLBODY = "Please click on the below link to reset the password" +
                 " <a> http://example.com/reset?email=user@somedomain.com&token=4e163b8b-889a-4ce7-a3f7-61041e323c23 </a>";


        String TEXTBODY = " ";

        String SUBJECT = " ";

        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(Regions.US_EAST_1).build();

        List<String> toAddrress = new ArrayList<>();
        toAddrress.add("lakhmani.a@husky.neu.edu");
        toAddrress.add("lalwani.n@husky.neu.edu");
        toAddrress.add("merchant.n@husky.neu.edu");
        toAddrress.add("koticha.c@husky.neu.edu");

        Message message = new Message();
        message.setBody(new Body());

        SendEmailRequest request = new SendEmailRequest().withSource("DONOTREPLY@csye6225-fall2017-lakhmania.me")
                .withDestination(
                        new Destination().withToAddresses(toAddrress))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withHtml(new Content()
                                        .withCharset("UTF-8").withData(HTMLBODY))
                                .withText(new Content()
                                        .withCharset("UTF-8").withData(TEXTBODY)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData(SUBJECT)));

        client.sendEmail(request);
    }

}

