package com.example.admin.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    TextView tvTitle, tvTimeTotal, tvTimeSong ;
    SeekBar seekBar, seekbarVolume;
    ImageButton btnBackward, btnForward, btnPlay, btnListSong, btnBack;

    Button btnTimer, btnDialogOK;
    EditText editTextDialog;

    ImageView imageAnimation;
    Animation animation;

    ArrayList<String> arrListTitle;
    ArrayList<String> arrListArtist;
    ArrayList<String> arrListLocation;

    MediaPlayer mediaPlayer;
    ArrayList<Song> arrSong;
    int currentPosition;
    String currentSong;

    Dialog dialog;

    int numberTimer;

    //for Service
    static String currentLocation = "";
    static Uri zz;

    AudioManager audioManager;
    boolean randomSong = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //When we click the listView item, its will play that song

        Intent intent = this.getIntent();
        currentSong = intent.getStringExtra("selectedItem");
        currentPosition = intent.getIntExtra("currentPosition", 0);
        arrListTitle = intent.getStringArrayListExtra("arrListTitle");
        arrListArtist = intent.getStringArrayListExtra("arrListArtist");
        arrListLocation = intent.getStringArrayListExtra("arrListLocation");

        //Add title, location into arrSong
        arrSong = new ArrayList<>();
        for (int i = 0; i < arrListTitle.size(); i++) {
            arrSong.add(new Song(arrListTitle.get(i), arrListLocation.get(i)));
        }

        //Mapping objects textview, imagebutton...
        Radiation();

        //Handle event for seekbar
        SeekbarChanged();

        //Start service, but its not complete yet
        zz = Uri.parse(arrSong.get(currentPosition).getFile());

        //Add current song into MediaPlayer
        //Start playing the current song
        generateMediaPlayer();

        //Set text for textview time total
        setTimeTotal();

        //Update time for textview progress
        //Automatic playing next song
        //Keep looping the list
        updateTime();

        //Start animation
        animation = AnimationUtils.loadAnimation(this, R.anim.disk_rotate);
        imageAnimation.startAnimation(animation);

        controlVolume();

    }

    //Get embedded image
    //Set become background
    //If it doesn't have embedded image, the default image will be set
    private void setImageResource() {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(currentLocation);
        byte[] artBytes =  mmr.getEmbeddedPicture();
        ImageView imgArt;
        imgArt = (ImageView) findViewById(R.id.imageView);
        if(artBytes != null)
        {
            InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
            Bitmap bm = BitmapFactory.decodeStream(is);
            imgArt.setImageBitmap(bm);
        }
        else
        {
            imgArt.setImageDrawable(getResources().getDrawable(R.drawable.background));
        }
    }

    public void backPressed(){
        showDialogBacklist();
        btnDialogBackOk = (Button) dialogBack.findViewById(R.id.btnBackOk);
        btnDialogBackCancel = (Button) dialogBack.findViewById(R.id.btnBackCancel);

        btnDialogBackCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBack.dismiss();
            }
        });

        btnDialogBackOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBack.dismiss();
                mediaPlayer.stop();
                Intent intentReturn = new Intent();
                setResult(RESULT_OK, intentReturn);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    Button btnDialogBackOk, btnDialogBackCancel ;
    Boolean timeRunning = false;
    long timeLeftInMiliSecond;

    //handle click event for btn play, back, next, backIntent
    @Override
    public void onClick(View view) {
        if (view == btnBack){
            backPressed();
        }
        if (view == btnPlay){
            if (mediaPlayer.isPlaying()){
                //Pause music
                mediaPlayer.pause();
                btnPlay.setImageResource(R.drawable.iconplay);
                //imageAnimation.animate().cancel();
                //Service stop
                //stopService(new Intent(Main2Activity.this, MyService.class));
            } else{
                //Play music
                mediaPlayer.start();
                btnPlay.setImageResource(R.drawable.iconpause);
                updateTime();
                setTimeTotal();
                //Start service
                //startService(new Intent(Main2Activity.this, MyService.class));
            }

        }
        if (view == btnBackward) {
            btnBackward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPosition--;
                    if (currentPosition < 0 ){
                        currentPosition = arrSong.size() -1 ;
                    }
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                    }
                    if (mediaPlayer.isPlaying() == false){
                        btnPlay.setImageResource(R.drawable.iconpause);
                    }
                    generateMediaPlayer();
                    updateTime();
                    setTimeTotal();
                    //Display current position in the list
                    //Toast.makeText(Main2Activity.this, currentPosition + "", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (view == btnForward) {
            btnForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPosition++;
                    if (currentPosition > arrSong.size() -1 ){
                        currentPosition = 0;
                    }
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                    }
                    if (mediaPlayer.isPlaying() == false){
                        btnPlay.setImageResource(R.drawable.iconpause);
                    }
                    generateMediaPlayer();
                    updateTime();
                    setTimeTotal();
                    //Display current position in the list
                    //Toast.makeText(Main2Activity.this, currentPosition + "", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (view == btnTimer) {
            btnTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showDialogTimer();
                    editTextDialog = (EditText) dialog.findViewById(R.id.editTextDialog);
                    btnDialogOK = (Button) dialog.findViewById(R.id.btnDialogOK);
                    btnDialogOK.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            numberTimer = Integer.parseInt(editTextDialog.getText().toString());
                            Toast.makeText(Main2Activity.this, numberTimer + "", Toast.LENGTH_SHORT).show();
                            timeLeftInMiliSecond = numberTimer * 60000;
                            CountDownTimer countDownTimer = new CountDownTimer(numberTimer * 60000, 60000) {
                                @Override
                                public void onTick(long currentL) {
                                    btnTimer.setText(currentL / 1000 + "");
                                }

                                @Override
                                public void onFinish() {
                                    mediaPlayer.pause();
                                    btnPlay.setImageResource(R.drawable.iconplay);
                                    //be continue
                                    showDialogKeepMusic();
                                }
                            }.start();
                            timeRunning = true;
                            dialog.dismiss();
                        }
                    });
                }
            });


        }
        if (view == btnListSong){
            if (!randomSong) {
                Toast.makeText(this, "Nhạc sẽ được chọn ngẫu nhiên", Toast.LENGTH_SHORT).show();
                randomSong = true;
            } else {
                Toast.makeText(this, "Nhạc sẽ chơi theo thứ tự", Toast.LENGTH_SHORT).show();
                randomSong = false;
            }

        }


    }

    //Create Media Player
    //Set title of the song
    //Set image background
    private void generateMediaPlayer() {
        mediaPlayer = MediaPlayer.create(Main2Activity.this,
                Uri.parse(arrSong.get(currentPosition).getFile()));
        tvTitle.setText(arrSong.get(currentPosition).getTitle());
        mediaPlayer.start();
        currentLocation = arrListLocation.get(currentPosition);

        //Set image background
        //This method need current location of the song for take the embedded picture
        setImageResource();

        //startService(new Intent(Main2Activity.this, MyService.class));
    }

    //Handle when seekbar changed progress
    private void SeekbarChanged() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    //update progress seekbar
    //Check song progress, if over --> next
    private void updateTime(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                tvTimeSong.setText(formatTime.format(mediaPlayer.getCurrentPosition()));

                //update progress seekbar
                seekBar.setProgress(mediaPlayer.getCurrentPosition());

                //Check song progress, if over --> next
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (!randomSong) {
                            currentPosition++;
                        }
                        if (randomSong) {
                            currentPosition = new Random().nextInt(arrSong.size());
                        }
                        if (currentPosition > arrSong.size() -1 ){
                            currentPosition = 0;
                        }
                        if (mediaPlayer.isPlaying()){
                            mediaPlayer.stop();
                        }
                        if (!mediaPlayer.isPlaying()){
                            btnPlay.setImageResource(R.drawable.iconpause);
                        }
                        generateMediaPlayer();
                        mediaPlayer.start();
                        setTimeTotal();
                        updateTime();
                    }
                });

                handler.postDelayed(this, 500);
            }
        }, 100);
    }

    //Set textview to view total time of the song
    private void setTimeTotal(){
        SimpleDateFormat hourFormat = new SimpleDateFormat("mm:ss");
        tvTimeTotal.setText(hourFormat.format((mediaPlayer.getDuration())));
        //set max cho seekbar = voi duration cua tung song
        seekBar.setMax(mediaPlayer.getDuration());
    }

    //Dialog when click "hẹn giờ"
    public void showDialogTimer(){
        dialog = new Dialog(Main2Activity.this);
        dialog.setContentView(R.layout.dialog_layout);

        //Set dialog match parent the screen
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);

    }

    Dialog dialogBack;
    //Dialog when click back to the list
    public void showDialogBacklist(){
        dialogBack = new Dialog(Main2Activity.this);
        //set layout for dialog
        dialogBack.setContentView(R.layout.dialog_back_to_list);

        dialogBack.setTitle("Cảnh báo: ");
        dialogBack.show();
    }

    Dialog dialogKeepMusic;
    public void showDialogKeepMusic(){
        dialogKeepMusic = new Dialog(Main2Activity.this);
        dialogKeepMusic.setContentView(R.layout.dialog_alert_keepmusic);
        dialog.setTitle("Thông báo:");
        dialogKeepMusic.show();
        Button btnKeepMusicOk = (Button) dialogKeepMusic.findViewById(R.id.btnPlayOk);
        Button btnKeepMusicCancel = (Button) dialogKeepMusic.findViewById(R.id.btnPlayCancel);

        btnKeepMusicOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.start();
                dialogKeepMusic.dismiss();
            }
        });

        btnKeepMusicCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogKeepMusic.dismiss();
            }
        });

    }

    private void Radiation() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTimeTotal = (TextView) findViewById(R.id.tvTimeTotal);
        tvTimeSong = (TextView) findViewById(R.id.tvTimeSong);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnListSong = (ImageButton) findViewById(R.id.btnListSong);
        imageAnimation =  findViewById(R.id.cd);
        btnTimer = (Button) findViewById(R.id.btnTimer);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        seekbarVolume = (SeekBar) findViewById(R.id.seekbarVolume);

        btnBack.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnBackward.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnTimer.setOnClickListener(this);
        btnListSong.setOnClickListener(this);
    }

    public void controlVolume(){
        try{
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            seekbarVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            seekbarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            seekbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
