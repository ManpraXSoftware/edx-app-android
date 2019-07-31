package org.tta.mobile.tta.ui.otp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;

public class IncomingSms extends BroadcastReceiver {
    private static final String SMS_EXTRA_NAME = "pdus";
    private  IMessageReceiver messageReceiver;
    public IncomingSms(IMessageReceiver messageReceiver)
    {
        try {
            if ( messageReceiver == null )
                return;

            this.messageReceiver = messageReceiver;
        }
        catch (Exception ex)
        {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, IncomingSms.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "Constructor");
            Logger.logCrashlytics(ex, parameters);
        }
        /*if ( messageReceiver == null )
            throw new IllegalArgumentException("messageReceiver");
        this.messageReceiver = messageReceiver;*/
    }

    @Override
    /**
     * Method that handles SMSs.
     * It iterates over all the messages and forward each method to IMessageReceiver object.
     */
    public void onReceive(Context context, Intent intent) {
        final Bundle extras = intent.getExtras();
        try {
            if ( extras != null )
            {
                Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );
                for ( int i = 0; i < smsExtra.length; ++i ) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
                    String body = sms.getMessageBody().toString();
                    String address = sms.getOriginatingAddress();

                    if (messageReceiver != null)
                        messageReceiver.onMessage(address, body);
                }
            }
            // Uncomment this if you do not want the SMS put into the inbox (for priority > 999)
            // this.abortBroadcast();
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, IncomingSms.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "onReceive");
            parameters.putString(Constants.KEY_DATA, "Extras = " + intent.getExtras());
            Logger.logCrashlytics(e, parameters);
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }
}
