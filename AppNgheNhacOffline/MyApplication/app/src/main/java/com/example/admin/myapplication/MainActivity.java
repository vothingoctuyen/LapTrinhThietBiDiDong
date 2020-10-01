package com.example.admin.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.app.SearchableInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public class AdapterSongList extends ArrayAdapter<String> {

        public AdapterSongList(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.item_list_view, null);
            }

            ImageView  imageEmbedded = (ImageView) convertView.findViewById(R.id.imageEmbedded);
            TextView tvArtist = (TextView) convertView.findViewById(R.id.tvArtist);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);

            //set Image embedded
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(arrListLocation.get(position));
            if (arrListImage.get(position) != null) {
                InputStream inputStream = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageEmbedded.setImageBitmap(bitmap);
            }   //embedded image already set background.jpg for default

            //set textView artist
            tvArtist.setText(arrListArtist.get(position));

            //set textView title
            tvTitle.setText(arrListTitle.get(position));

            //return view
            return convertView;
        }
    }

    private static final int MY_PERMISSION_REQUEST = 1;
    private static final int MY_REQUEST_CODE = 100;

    ArrayList<String> arrListTitle;
    ArrayList<String> arrListArtist;
    ArrayList<String> arrListLocation;
    ArrayList<byte[]> arrListImage;


    ListView listView;

    AdapterSongList adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        } else {
            doStuff();
        }
    }

    //get all music in device
    public void getMusic(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //Uri songUriInternal = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUriExternal, null, null,null, null);
       // Cursor songCursorInternal = contentResolver.query(songUriInternal, null, null, null);

        if (songCursor != null && songCursor.moveToFirst() ) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA); //Vi tri bai hat

            do{
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentLocation = songCursor.getString(songLocation);

                if ( !currentTitle.equals("Hangouts Message") && !currentTitle.equals("Hangouts Call")
                        && !currentTitle.equals("Over the Horizon") && !currentTitle.equals("Facebook Pop") ){
                    //Dont add Hangsout Message and Hangsout Call into arrList
                    arrListArtist.add(currentArtist);
                    arrListTitle.add(currentTitle);
                    arrListImage.add(getEmbeddedImage(currentLocation));
                    arrListLocation.add(currentLocation);
                }
            } while (songCursor.moveToNext());
        }
    }

    public void doStuff(){
        listView = (ListView) findViewById(R.id.listView);
        arrListTitle = new ArrayList<>();
        arrListLocation = new ArrayList<>();
        arrListArtist = new ArrayList<>();
        arrListImage = new ArrayList<>();

        //put data to array list
        getMusic();

        adapter = new AdapterSongList(this, R.layout.item_list_view, arrListLocation);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //play myusic here
                //intent to another view
                String selectedItem = (String) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.putExtra("selectedItem", selectedItem);
                intent.putExtra("currentPosition", position);
                intent.putStringArrayListExtra("arrListArtist", arrListArtist);
                intent.putStringArrayListExtra("arrListTitle", arrListTitle);
                intent.putStringArrayListExtra("arrListLocation", arrListLocation);
                startActivityForResult(intent, MY_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                        doStuff();
                    }
                } else {
                    Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    Dialog dialog;
    @Override
    public void onBackPressed() {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_dang_xuat);
        dialog.setTitle("Cảnh báo: ");
        dialog.show();

        Button btnLogoutOk, btnLogoutCancel;
        btnLogoutOk = (Button) dialog.findViewById(R.id.btnLogoutOk);
        btnLogoutCancel = (Button) dialog.findViewById(R.id.btnLogoutCancel);

        btnLogoutOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });

        btnLogoutCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public byte[] getEmbeddedImage(String songLocation) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songLocation);
        return  mmr.getEmbeddedPicture();
    }

   //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        //SearchView  searchView = (SearchView) menu.findItem(R.id.mnuSearch).getActionView();
        //earchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //@Override
            //public boolean onQueryTextSubmit(String query) {
            //    return false;
           //}

            //@Override
            //public boolean onQueryTextChange(String newText) {
              //  arrListTitle = new ArrayList<>();
                //for(arrListTitle arrlisttitle:arrListTitle){
                  // if()
                //}
                //return false;
            //}
        //}
        //return super.onCreateOptionsMenu(menu);
    //}


    @Override
    protected void onResume() {
        super.onResume();
        Collections.sort(arrListTitle);
        adapter.notifyDataSetChanged();
    }
}


