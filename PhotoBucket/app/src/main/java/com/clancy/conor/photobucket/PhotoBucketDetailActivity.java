package com.clancy.conor.photobucket;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
                    Ion.with(mPhotoBucketImageView).load((String)documentSnapshot.get(Constants.KEY_IMAGE_URL));
                    //mPhotoBucketImageView.setImageResource((String)documentSnapshot.get(Constants.KEY_MOVIE));
                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean loadImageFromURL(String fileUrl,
                                    ImageView iv){
        try {

            URL myFileUrl = new URL (fileUrl);
            HttpURLConnection conn =
                    (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();
            iv.setImageBitmap(BitmapFactory.decodeStream(is));

            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
