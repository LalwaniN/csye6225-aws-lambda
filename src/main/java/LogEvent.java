import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;

public class LogEvent implements RequestHandler<SNSEvent, Object> {

  static DynamoDB dynamoDB;


  public Object handleRequest(SNSEvent request, Context context) {

    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

    context.getLogger().log("Invocation started: " + timeStamp);

    context.getLogger().log("1: " + (request == null));

    context.getLogger().log("2: " + (request.getRecords().size()));

    context.getLogger().log(request.getRecords().get(0).getSNS().getMessage());

    timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

    context.getLogger().log("Invocation completed: " + timeStamp);
    try {
      context.getLogger().log("trying to connect to dynamodb");
      init();
      long unixTime = Instant.now().getEpochSecond()+120;
      Table table = dynamoDB.getTable("csye6225");

      Item item = table.getItem("id", request.getRecords().get(0).getSNS().getMessage());

      if(item==null) {
        Item itemPut = new Item()
                .withPrimaryKey("id", request.getRecords().get(0).getSNS().getMessage())
                .withString("token", context.getAwsRequestId())
                .withNumber("tokenExpiry", unixTime);

        table.putItem(itemPut);
      }
    }

    catch(AmazonServiceException ase){
      context.getLogger().log("Could not complete operation");
      context.getLogger().log("Error Message:  " + ase.getMessage());
      context.getLogger().log("HTTP Status:    " + ase.getStatusCode());
      context.getLogger().log("AWS Error Code: " + ase.getErrorCode());
      context.getLogger().log("Error Type:     " + ase.getErrorType());
      context.getLogger().log("Request ID:     " + ase.getRequestId());
    }
    catch (AmazonClientException ace) {
      context.getLogger().log("Internal error occured communicating with DynamoDB");
      context.getLogger().log("Error Message:  " + ace.getMessage());
    }
    catch(Exception e){
      context.getLogger().log(e.getMessage());
    }

      try {
         String FROM = "donotreply@csye6225-fall2017-lalwanin.me";
         String TO = request.getRecords().get(0).getSNS().getMessage();
         String SUBJECT = "Amazon SES test (AWS SDK for Java)";
         String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
                  + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
                  + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>"
                  + "AWS SDK for Java</a>";
         String TEXTBODY = "This email was sent through Amazon SES "
                  + "using the AWS SDK for Java.";
          AmazonSimpleEmailService client =
                  AmazonSimpleEmailServiceClientBuilder.standard()
                          .withRegion(Regions.US_EAST_1).build();
          SendEmailRequest req = new SendEmailRequest()
                  .withDestination(
                          new Destination()
                                  .withToAddresses(TO))
                  .withMessage(
                          new Message()
                                  .withBody(
                                          new Body()
                                                  .withHtml(
                                                          new Content()
                                                                  .withCharset(
                                                                          "UTF-8")
                                                                  .withData(
                                                                          "This message body contains HTML formatting"))
                                                  .withText(
                                                          new Content()
                                                                  .withCharset(
                                                                          "UTF-8")
                                                                  .withData(
                                                                          "This is the message body in text format.")))
                                  .withSubject(
                                          new Content().withCharset("UTF-8")
                                                  .withData("Test email")))
                  .withSource(FROM);
          SendEmailResult response = client.sendEmail(req);
          context.getLogger().log ("Email sent!");
      } catch (Exception ex) {
          context.getLogger().log ("The email was not sent. Error message: "
                  + ex.getMessage());
      }


    return null;
  }

  private static void init() throws Exception {
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    dynamoDB = new DynamoDB(client);
  }


}

