package kz.growit.servicereceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.rey.material.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private MaterialDialog dialog;

    //    private BroadcastReceiver receiver;
    private String fileName;
    private String url;
    private EditText urlET;
    private EditText fileNameET;

    private Toolbar toolbar;

    BroadcastReceiver receiver = new BroadcastReceiver() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Home").withIcon(FontAwesome.Icon.faw_home).withIdentifier(1),
                        new PrimaryDrawerItem().withName("About Program").withIcon(FontAwesome.Icon.faw_gamepad).withIdentifier(2)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (drawerItem.getIdentifier()) {
                            case 1:
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                return false;
                            case 2:
                                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                                return false;
                            default:
                                return false;
                        }
                    }
                })
                .build();
//        receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Bundle bundle = intent.getExtras();
//                if (bundle != null) {
//                    String string = bundle.getString(DownloadService.FILEPATH);
//                    int resultCode = bundle.getInt(DownloadService.RESULT);
//                    if (resultCode == RESULT_OK) {
//                        Snackbar.make(findViewById(R.id.ll),
//                                "Download complete. Download URI: " + string,
//                                Snackbar.LENGTH_LONG).show();
//                        textView.setText("Download done");
//                    } else {
//                        Snackbar.make(findViewById(R.id.ll), "Download failed",
//                                Snackbar.LENGTH_LONG).show();
//                        textView.setText("Download failed");
//                    }
//
//                    // dismiss the dialog after the file was downloaded or failed
//                    dialog.dismiss();
//                }
//            }
//        };


        urlET = (EditText) findViewById(R.id.urlET);
        fileNameET = (EditText) findViewById(R.id.fileNameET);


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


    //this method calls onClick of Download button. declared in XML
    public void onClick(View view) {

        url = urlET.getText().toString();
        fileName = fileNameET.getText().toString();

        if (url.equals("") || fileName.equals("")) {
            Snackbar.make(view, "Enter url or filename", Snackbar.LENGTH_LONG).show();
        } else {

            //show dialog
            dialog = new MaterialDialog.Builder(this)
                    .title("Downloading file")
                    .content("Please wait while file is being downloaded in " + Environment.getExternalStorageDirectory() +
                            "/" + fileName)
                    .progress(true, 0)
                    .cancelable(true)
                    .negativeText("Stop downloading")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // TODO
                            dialog.dismiss();

                            unregisterReceiver(receiver);
                            Intent intent = new Intent(MainActivity.this, DownloadService.class);
                            stopService(intent);
                            textView.setText("Service stopped");
                        }
                    })
                    .progressIndeterminateStyle(true)
                    .show();


            //starting service with filename and url sent in intent
            Intent intent = new Intent(this, DownloadService.class);
            // add infos for the service which file to download and where to store
            intent.putExtra(DownloadService.FILENAME, fileName);
//        intent.putExtra(DownloadService.FILENAME, "hello.html");
//        intent.putExtra(DownloadService.URL, "https://www.jetbrains.com/webstorm/documentation/WebStorm_ReferenceCard.pdf");
            intent.putExtra(DownloadService.URL, url);
            startService(intent);
            textView.setText("Service started");

        }

    }
}
