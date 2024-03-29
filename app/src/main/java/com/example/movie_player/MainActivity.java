package com.example.movie_player;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    //videos used in testing direct link
    //https://img-9gag-fun.9cache.com/photo/aeMO8Zv_460svvp9.webm
    //https://v.redd.it/z08avb339n801/DASH_1_2_M

    //Objects
    private ImageButton _bbutton, _pbutton, _fbutton;
    private VideoView _vView;
    private SeekBar _sBar;

    //Resources
    private GestureLibrary gestureLibrary;
    private Handler _sbHandler;
    private Runnable _sbRunnable;

    //texts
    private EditText _URLtext;
    private TextView _currentTimeText , _videoDuration;

    //variables
    private int _currentTime = 0;
    private ArrayList<Uri> _playlist;
    private int _queuePos;

    //Create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //buttons
        _pbutton = findViewById(R.id.PlayVid);
        _bbutton = findViewById(R.id.BackVid);
        _fbutton = findViewById(R.id.ForwardVid);
        setButtonListeners();

        //texts
        _currentTimeText = findViewById(R.id.currentTime);
        _videoDuration = findViewById(R.id.videoDuration);
        _URLtext = findViewById(R.id.URL);

        //video view
        _vView = findViewById(R.id.videoView);
        _vView.setMediaController(null);

        //playlist
        StartPlaylist();
        SetVideoEndListener();

        //gestures
        GestureOverlayView gestureOverlayView = findViewById(R.id.gestures);
        gestureOverlayView.addOnGesturePerformedListener(this);
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if(!gestureLibrary.load()) finish();

    }

    //Initializations
    private void setButtonListeners() {
        _bbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BackVid(v);
            }
        });

        _bbutton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                PreviousVid(v);
                return true;
            }
        });

        _fbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ForwardVid(v);
            }
        });

        _fbutton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                NextVid(v);
                return true;
            }
        });

    }

    private void setupSB() {
        _sbHandler = new Handler();

        _sBar = findViewById(R.id.seekBar);
        _sBar.setMax(_vView.getDuration());
        int _maxDurMS = _vView.getDuration();
        String _maxDur = ConvertMs(_maxDurMS);

        _videoDuration.setText(_maxDur);

        _sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if(input){
                    _vView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void StartPlaylist(){
        _queuePos = 0;
        _playlist = new ArrayList<>();
    }

    //Video Actions
    private void PreviousVid(View v) {
        if(_queuePos == 0){
            _vView.seekTo(0);
        }
        else{
            _queuePos--;
            _vView.setVideoURI(_playlist.get(_queuePos));
            _vView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setupSB();
                    _vView.start();
                    playCycle();
                    _pbutton.setImageResource(R.mipmap.icpause_round);
                }
            });
        }
    }

    private void NextVid(View v) {
        if(_queuePos == _playlist.size() - 1){
            return;
        }
        else{
            _queuePos++;
            _vView.setVideoURI(_playlist.get(_queuePos));
            _vView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setupSB();
                    _vView.start();
                    playCycle();
                    _pbutton.setImageResource(R.mipmap.icpause_round);
                }
            });
        }
    }

    private void SetVideoEndListener() {
        _vView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(!_playlist.isEmpty() && _queuePos != _playlist.size() - 1) {
                    _queuePos++;
                    mp.reset();
                    _vView.setVideoURI(_playlist.get(_queuePos));
                    _vView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            setupSB();
                            _vView.start();
                            playCycle();
                            _pbutton.setImageResource(R.mipmap.icpause_round);
                        }
                    });
                }
            }
        });
    }

    public void PlayVid(View v){
        if(_playlist.size() > 0) {
            if (!_vView.isPlaying()) {
                _vView.start();
                playCycle();
                _pbutton.setImageResource(R.mipmap.icpause_round);
            } else {
                _vView.pause();
                _pbutton.setImageResource(R.mipmap.ic_launchernew_round);
            }
        }
    }

    public void ForwardVid(View v){
        _currentTime = _vView.getCurrentPosition();

        //10s atm
        _vView.seekTo(_currentTime + 10000);
        _sBar.setProgress(_vView.getCurrentPosition());
    }

    public void BackVid(View v){
        _currentTime = _vView.getCurrentPosition();

        //10s atm
        _vView.seekTo(_currentTime - 10000);
        _sBar.setProgress(_vView.getCurrentPosition());
    }

    //Updating functions
    private void playCycle(){
        String _newTime = "";

        _sBar.setProgress(_vView.getCurrentPosition());

        if(_vView.isPlaying())
        {
            _sbRunnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            _sbHandler.postDelayed(_sbRunnable,1000);

            _currentTime = _vView.getCurrentPosition();

            _newTime = ConvertMs(_currentTime);
            _currentTimeText.setText(_newTime);
        }
    }

    public void LoadYT(View v){
        String _url = _URLtext.getText().toString();

        Intent _intent = new Intent(getBaseContext(),YTActivity.class);
        _intent.putExtra("EXTRA_VIDEO_URL", _url);
        startActivity(_intent);
    }

    public void LoadVid(View v){

        final String _url = _URLtext.getText().toString();

        if(_playlist.isEmpty()) {
            try {
                Uri _vUri = Uri.parse(_url);
                _playlist.add(_vUri);
                _vView.setVideoURI(_vUri);

            } catch (Exception ex) {

            }
            _vView.requestFocus();
            _vView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setupSB();
                    _vView.start();
                    playCycle();
                    _pbutton.setImageResource(R.mipmap.icpause_round);
                }
            });
        }
        else {
            try{
                Uri _vUri = Uri.parse(_url);
                _playlist.add(_vUri);
            }
            catch (Exception ex){

            }
        }

        _URLtext.setText("");
    }

    //Auxiliary Functions
    private String ConvertMs(int _ms){
        String _result = "";

        _result = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(_ms),
                    TimeUnit.MILLISECONDS.toSeconds(_ms) - TimeUnit.MILLISECONDS.toMinutes(_ms));

        return _result;
    }

    //functions overriden
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture){
        ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);


        for(Prediction prediction : predictions)
        {
            if(prediction.score > 1.0)
            {
                switch (prediction.name)
                {
                    case "Foward":
                        ForwardVid(overlay);
                        break;
                    case "Back":
                        BackVid(overlay);
                        break;
                    case "Play":
                        PlayVid(overlay);
                        break;
                    case "Next":
                    case "Next2":
                        NextVid(overlay);
                        break;
                    case "Previous":
                    case "Previous2":
                        PreviousVid(overlay);
                        break;
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _sbHandler.removeCallbacks(_sbRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _vView.seekTo(_currentTime);
        _vView.pause();

    }

    @Override
    protected void onPause() {
        super.onPause();
        _currentTime = _vView.getCurrentPosition();
        _vView.pause();
    }
}
