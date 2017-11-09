import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GenerateToken implements RequestHandler<SNSEvent, Object> {

    @Override
    public Object handleRequest(SNSEvent request, Context context) {

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Invocation started: " + timeStamp);
        context.getLogger().log("1: " + (request == null));
        context.getLogger().log("2: " + (request.getRecords().size()));
        context.getLogger().log(request.getRecords().get(0).getSNS().getMessage());
        timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Invocation completed: " + timeStamp);
        String id = request.getRecords().get(0).getSNS().getMessage();
        String token = context.getAwsRequestId();

        insertRecord(context,id, token);
        sendEmail(context, id, token);

        context.getLogger().log("Invocation completed: " + timeStamp);

        return null;

    }

    public void insertRecord( Context context, String id, String token)
    {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build();
        DynamoDB dynamoDB = new DynamoDB(client);

        context.getLogger().log("Trying to insert item in Dynamo DB");

        Table table = dynamoDB.getTable("csye6225");

        context.getLogger().log("Using Dynamo db Table" + table);

        try {
            Item item = new Item().withPrimaryKey("id", id).with("Token", context.getAwsRequestId()).with("passwordTokenExpiry", System.currentTimeMillis() / 1000L + (20 * 60));
            table.putItem(item);
            context.getLogger().log("PutItem succeeded: " + id);
        } catch (Exception e) {
            System.err.println("Unable to add id: " + id);
            context.getLogger().log("Unable to add id: " + id);
            context.getLogger().log(e.getMessage());

        }

    }

    public void sendEmail(Context context, String id, String token)
    {
        final String FROM = "donotreply@csye6225-fall2017-merchantn.me";

        final String TO = id;

        try {
            // The subject line for the email.
            final String SUBJECT = "Password reset";

            // The HTML body for the email.
            final String HTMLBODY = "<p><a href='donotreply@csye6225-fall2017-merchantn.me/reset?email="+TO+"&token="+token+"'></a></p>";

            context.getLogger().log("HTMLBODY"+HTMLBODY);
            final String TEXTBODY = "donotreply@csye6225-fall2017-merchantn.me/reset?email="+TO+"&token="+token;
            context.getLogger().log("HTMLBODY"+TEXTBODY);
            AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

            SendEmailRequest sesRequest = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                            .withHtml(new Content()
                                                    .withCharset("UTF-8").withData(HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);

            sesClient.sendEmail(sesRequest);
            context.getLogger().log("Email Sent");

        }
        catch (Exception e)
        {
            System.out.println("The email was not sent. Error message: "
                    + e.getMessage());
        }
    }

}