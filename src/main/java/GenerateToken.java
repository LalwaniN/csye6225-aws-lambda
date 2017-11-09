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

//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
//                .build();

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build();
        DynamoDB dynamoDB = new DynamoDB(client);

        // DynamoDB dynamoDB = new DynamoDB(Regions.US_EAST_1);

        context.getLogger().log("Trying to insert item");
        Table table = dynamoDB.getTable("csye6225");
        context.getLogger().log("Dynamo db Table" + table);
        String id = request.getRecords().get(0).getSNS().getMessage();
        try {
            Item item = new Item().withPrimaryKey("id", id).with("Token", context.getAwsRequestId()).with("ttl", System.currentTimeMillis() / 1000L + (3 * 60 * 1000));
            //   table.putItem(new Item().withPrimaryKey("id", id).withString("token", context.getAwsRequestId()).with("ttl",System.currentTimeMillis() / 1000L + (3 * 60 * 1000)));
            table.putItem(item);
            //System.out.println("PutItem succeeded: " + id );
            context.getLogger().log("PutItem succeeded: " + id);
        } catch (Exception e) {
            System.err.println("Unable to add id: " + id);
            context.getLogger().log("Unable to add id: " + id);
            context.getLogger().log(e.getMessage());
            //System.err.println(e.getMessage());


        }

        final String FROM = "donotreply@csye6225-fall2017-merchantn.me";

        final String TO = "merchant.n@husky.neu.edu";

        try {
            // The subject line for the email.
            final String SUBJECT = "Password reset";

            // The HTML body for the email.
            final String HTMLBODY = "<a href='https://aws.amazon.com/sdk-for-java/'>"
                    + "AWS SDK for Java</a>";

            // The email body for recipients with non-HTML email clients.
            final String TEXTBODY = "This email was sent through Amazon SES "
                    + "using the AWS SDK for Java.";


            AmazonSimpleEmailService sesClient =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            // Replace US_WEST_2 with the AWS Region you're using for
                            // Amazon SES.
                            .withRegion(Regions.US_EAST_1).build();


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
        }
        catch (Exception e)
        {
            System.out.println("The email was not sent. Error message: "
                    + e.getMessage());
        }

        context.getLogger().log("Invocation completed: " + timeStamp);


        return null;

    }




}