package com.clancy.conor.photobucket;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.koushikdutta.ion.Ion;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PhotoBucketDetailActivity extends AppCompatActivity {
    private ImageView mPhotoBucketImageView;
    private TextView mPhotoBucketTextView;
    // DocRef & DocSnapShots
    private DocumentReference mDocRef;
    private DocumentSnapshot mDocSnapShot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_bucket_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Captured the text views
        mPhotoBucketTextView = findViewById(R.id.detail_text_view_caption);
        mPhotoBucketImageView = findViewById(R.id.detail_image_view);


        Intent receivedIntent = getIntent();
        //FRom received intent need to pull out the doc_id
        String docId = receivedIntent.getStringExtra(Constants.EXTRA_DOC_ID);

        // Where am I pointing to in Firebase, get the right reference, useful for read, update

        mDocRef = FirebaseFirestore.getInstance().
                collection(Constants.COLLECTION_PATH).document(docId);

        mDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            // Will get either an exception or a docuement SnapShot
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(Constants.TAG, "listen failed");
                }
                //Check if the doc exists, will be using delete so want to know if it exists first
                if (documentSnapshot.exists()) {
                    mDocSnapShot = documentSnapshot; //Save document snapshot so can use it in other places as well
                    mPhotoBucketTextView.setText((String) documentSnapshot.get(Constants.KEY_CAPTION));
                    Ion.with(mPhotoBucketImageView).load((String) documentSnapshot.get(Constants.KEY_IMAGE_URL));
                    //mPhotoBucketImageView.setImageResource((String)documentSnapshot.get(Constants.KEY_MOVIE));
                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            mDocRef.delete();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.photobucket_dialog, null, false);
        builder.setView(view);

        final TextView cEditText = view.findViewById(R.id.dialog_caption_edittext);
        final TextView iEditText = view.findViewById(R.id.dialog_imageurl_edittext);

        cEditText.setText((String) mDocSnapShot.get(Constants.KEY_CAPTION));
        iEditText.setText((String) mDocSnapShot.get(Constants.KEY_IMAGE_URL));

        builder.setTitle("Edit this caption/photo?");

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                Map<String, Object> mq = new HashMap<>();

                mq.put(Constants.KEY_CAPTION, cEditText.getText().toString());
                mq.put(Constants.KEY_IMAGE_URL, iEditText.getText().toString());
                mq.put(Constants.KEY_CREATED, new Date());

                mDocRef.update(mq);

            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.create().show();

    }
}
