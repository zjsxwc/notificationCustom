package dev.wangchao.notification;

import java.util.Map;
import java.util.Objects;

import android.Manifest;
import android.app.NotificationChannel;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import dev.wangchao.notification.databinding.ActivityMainBinding;

/**
 * This one of two notification demos.  The second one uses the broadcast receiver located in this app.
 * <p>
 * This maybe helpful.
 * https://developer.android.com/training/notify-user/build-notification.html
 * https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html
 */
public class MainActivity extends AppCompatActivity {


    public static String idNormal = "nm_channel_normal" ;

    NotificationManager nm;
    int NotID = 1;
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.POST_NOTIFICATIONS};
    public static String TAG = "MainActivity";
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // for notifications permission now required in api 33
        //this allows us to check with multiple permissions, but in this case (currently) only need 1.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> isGranted) {
                boolean granted = true;
                for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                    logthis(x.getKey() + " is " + x.getValue());
                    if (!x.getValue()) granted = false;
                }
                if (granted) logthis("Permissions granted for api 33+");
            }
        });

        //Icon and message icon
        binding.btnIconMarquee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = Objects.requireNonNull(binding.inputContent).getText().toString();
                simplenoti(content);
                Toast t = Toast.makeText(MainActivity.this, "已发送", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        });

        binding.btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.inputContent.setText("");
            }
        });

        createchannel();
        //for the new api 33+ notifications permissions.
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) {
                rpl.launch(REQUIRED_PERMISSIONS);
            }
        }
    }

    /**
     * creates notification channels (required for API 26+)
     */
    private void createchannel() {
        //a medium level channel
        NotificationChannel mChannel = new NotificationChannel(idNormal, "channel_name:"+idNormal,  //name of the channel
                NotificationManager.IMPORTANCE_LOW);   //importance level
        // Configure the notification channel.
        mChannel.setDescription("channel_description:" + idNormal);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
        mChannel.setLightColor(Color.BLUE);
        mChannel.enableVibration(true);
        mChannel.setShowBadge(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        nm.createNotificationChannel(mChannel);
    }



    /**
     * create a notification with a icon and message, plus a title.
     */
    public void simplenoti(String content) {
        //Create a new notification. The construction Notification(int icon, CharSequence tickerText, long when) is deprecated.
        //If you target API level 11 or above, use Notification.Builder instead
        //With the second parameter, it would show a marquee
        Notification noti = new NotificationCompat.Builder(getApplicationContext(), idNormal).setSmallIcon(R.drawable.ic_announcement_black_24dp)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setWhen(System.currentTimeMillis())  //When the event occurred, now, since noti are stored by time.
                .setContentTitle("自定义消息")   //Title message top row.
                .setContentText(content)  //message when looking at the notification, second row
                .setAutoCancel(true)   //allow auto cancel when pressed.
                .setChannelId(idNormal).build();  //finally build and return a Notification.

        //Show the notification
        nm.notify(NotID, noti);
        NotID++;
    }

    //ask for permissions when we start.
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }




    public void logthis(String msg) {

        Log.d(TAG, msg);
    }

}
