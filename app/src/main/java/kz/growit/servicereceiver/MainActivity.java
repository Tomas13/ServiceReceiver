package kz.growit.servicereceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private MaterialDialog dialog;

    private BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String string = bundle.getString(DownloadService.FILEPATH);
                    int resultCode = bundle.getInt(DownloadService.RESULT);
                    if (resultCode == RESULT_OK) {
                        Snackbar.make(findViewById(R.id.ll),
                                "Download complete. Download URI: " + string,
                                Snackbar.LENGTH_LONG).show();
                        textView.setText("Download done");
                    } else {
                        Snackbar.make(findViewById(R.id.ll), "Download failed",
                                Snackbar.LENGTH_LONG).show();
                        textView.setText("Download failed");
                    }

                    // dismiss the dialog after the file was downloaded or failed
                    dialog.dismiss();
                }
            }
        };

        textView = (TextView) findViewById(R.id.status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(MainActivity.this, "Register receiver onResume", Toast.LENGTH_SHORT).show();

        registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(MainActivity.this, "Unregister receiver onPAUSE", Toast.LENGTH_SHORT).show();
        unregisterReceiver(receiver);
    }


    //this method calls onClick of Download button. see in XML
    public void onClick(View view) {

        dialog = new MaterialDialog.Builder(this)
                .title("Downloading file")
                .content("Please wait while file is being downloaded")
                .progress(true, 0)
                .cancelable(true)
                .negativeText("Stop downloading")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO
                        dialog.dismiss();

                        Intent intent = new Intent(MainActivity.this, DownloadService.class);
                        unregisterReceiver(receiver);
                        stopService(intent);
                        textView.setText("Service stopped");
                    }
                })
                .progressIndeterminateStyle(true)
                .show();


        Intent intent = new Intent(this, DownloadService.class);
        // add infos for the service which file to download and where to store
        intent.putExtra(DownloadService.FILENAME, "index.html");
//        intent.putExtra(DownloadService.FILENAME, "hello.JPEG");
//        intent.putExtra(DownloadService.URL, "http://www.vogella.com/index.html");
        intent.putExtra(DownloadService.URL, "https://www.jetbrains.com/webstorm/documentation/WebStorm_ReferenceCard.pdf");
        startService(intent);
        textView.setText("Service started");
    }
}
