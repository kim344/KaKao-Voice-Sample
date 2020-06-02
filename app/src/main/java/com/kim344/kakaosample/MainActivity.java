package com.kim344.kakaosample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.impl.util.PermissionUtils;
import com.kakao.util.helper.Utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SpeechRecognizeListener {

    String TAG = "Confirm";

    private SpeechRecognizerClient client;
    private static final int REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE = 0;

    GlideDrawableImageViewTarget imageViewTarget;

    ImageView imageKaKaoVoiceStart;
    ImageView imageKaKaoVoiceStop;
    TextView txtComment;
    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String hash = getKeyHash(MainActivity.this);
        Log.d(TAG, "getHash : " + hash);

        // SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this);

        //주요 권한 사용자에게 다시 체크 받음
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE);
            } else {
                // 유저가 거부하면서 다시 묻지 않기를 클릭.. 권한이 없다고 유저에게 직접 알림.
                String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WEB;

                if (PermissionUtils.checkAudioRecordPermission(MainActivity.this)) {

                    SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(serviceType);
                    client = builder.build();

                    client.setSpeechRecognizeListener(MainActivity.this);
                    client.startRecording(true);

                    Toast.makeText(MainActivity.this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            Log.e(TAG, "else");
        }

        imageKaKaoVoiceStart = findViewById(R.id.img_kakao_voice_start);
        imageKaKaoVoiceStop = findViewById(R.id.img_kakao_voice_stop);
        txtComment = findViewById(R.id.txt_kakao_main_comment);
        txtResult = findViewById(R.id.txt_result_speech);

        imageViewTarget = new GlideDrawableImageViewTarget(imageKaKaoVoiceStart);
        //GIF Start Stop 관리
        Glide.with(this).load(R.raw.recording).into(imageViewTarget).onStart();
        //Glide.with(KaKaoTest.this).load(R.raw.recording).into(imageViewTarget).onStop();

        imageKaKaoVoiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "imageKaKaoVoiceStart Click");

                Glide.with(MainActivity.this).load(R.raw.recording).into(imageViewTarget).onStart();

                String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WEB;

                if (PermissionUtils.checkAudioRecordPermission(MainActivity.this)) {

                    SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(serviceType);
                    client = builder.build();

                    client.setSpeechRecognizeListener(MainActivity.this);
                    client.startRecording(true);

                    Toast.makeText(MainActivity.this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();

        // API를 더이상 사용하지 않을 때 finalizeLibrary()를 호출한다.
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }

    //SpeechRecognize Listener 의 여러가지 메소드들...
    @Override
    public void onReady() {//모든 하드웨어및 오디오 서비스가 모두 준비 된 다음 호출
        Log.d(TAG, "onReady : 모든 준비가 완료 되었습니다.");
    }

    @Override
    public void onBeginningOfSpeech() { //사용자가 말하기 시작하는 순간 호출
        Log.d(TAG, "onBeginningOfSpeech : 말하기 시작 했습니다.");
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onEndOfSpeech() {//사용자가 말하기를 끝냈다고 판단되면 호출
        Log.d(TAG, "onEndOfSpeech : 말하기가 끝났습니다.");
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        Log.e(TAG, "onError : 에러코드 : " + errorCode + " / 에러내용 : " + errorMsg);

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 해당 작업을 처리함
                        // 원래 하고싶었던 일들 (UI변경작업 등...)
                        txtComment.setText(getResources().getString(R.string.kakao_main_comment_stop));
                        txtResult.setText(getResources().getString(R.string.kakao_main_comment_stop_body));
                        imageKaKaoVoiceStart.setVisibility(View.GONE);
                        imageKaKaoVoiceStop.setVisibility(View.VISIBLE);

                        imageKaKaoVoiceStop.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                txtComment.setText(getResources().getString(R.string.kakao_main_comment_start));
                                txtResult.setText("");
                                imageKaKaoVoiceStop.setVisibility(View.GONE);
                                imageKaKaoVoiceStart.setVisibility(View.VISIBLE);

                                String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WEB;

                                if (PermissionUtils.checkAudioRecordPermission(MainActivity.this)) {

                                    SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(serviceType);
                                    client = builder.build();

                                    client.setSpeechRecognizeListener(MainActivity.this);
                                    client.startRecording(true);

                                    Toast.makeText(MainActivity.this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        }).start();

    }

    @Override
    public void onPartialResult(String partialResult) {//인식된 음성 데이터를 문자열로 알려 준다.

    }

    @Override
    public void onResults(Bundle results) {//음성 입력이 종료된것으로 판단하고 서버에 질의를 모두 마치고 나면 호출
        final StringBuilder builder = new StringBuilder();

        final ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
        ArrayList<Integer> confs = results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);

        Log.d(TAG, "Result: " + texts);

        for (int i = 0; i < texts.size(); i++) {
            builder.append(texts.get(i));
            builder.append(" (");
            builder.append(confs.get(i).intValue());
            builder.append(")\n");
        }

        //모든 콜백함수들은 백그라운드에서 돌고 있기 때문에 메인 UI를 변경할려면 runOnUiThread를 사용해야 한다.
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing()) return;

                txtResult.setText(texts.get(0));
                Glide.with(MainActivity.this).load(R.raw.recording).into(imageViewTarget).onStop();
            }
        });

    }

    @Override
    public void onAudioLevel(float audioLevel) {

    }

    @Override
    public void onFinished() {
        Log.d(TAG, "onFinished");
    }

    public String getKeyHash(final Context context) {
        PackageInfo packageInfo = Utility.getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;
        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.d(TAG, "디버그 keyHash" + signature, e);
            }
        }
        return null;
    }


}