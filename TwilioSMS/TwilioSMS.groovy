@Grab(group='com.twilio.sdk', module='twilio-java-sdk', version='4.5.1-SNAPSHOT')

import com.twilio.sdk.TwilioRestClient
import org.apache.http.message.BasicNameValuePair
// Find your Account Sid and Token at twilio.com/user/account

ACCOUNT_SID = 'ACe7126de777c8c018b186ecc5ded93a2c'
AUTH_TOKEN = '144a0cdd15c01d34a53a7142d9660e08'

client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN)
// Build a filter for the MessageList

params = []
params << new BasicNameValuePair("Body", "hello, world!")
params << new BasicNameValuePair("To", "+16128197698")
params << new BasicNameValuePair("From", "+17633163700")

message = client.getAccount().getMessageFactory().create(params)


println message.getSid()