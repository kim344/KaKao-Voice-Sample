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

    private SpeechRecognizerClient client;
    private static final int REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE = 0;

    GlideDrawableImageViewTarget imageViewTarget;

    ImageView imageKaKaoVoiceStart;
    ImageView imageKaKaoVoiceStop;
    ImageView imageKaKaoBack;
    TextView txtComment;
    TextView txtResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String hash = getKeyHash(MainActivity.this);
        Log.e("Confirm", hash);

        // SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this);

        //주요 권한 사용자에게 다시 체크 받음
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.e("Confirm", "두번쨰");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE);
            } else {
                // 유저가 거부하면서 다시 묻지 않기를 클릭.. 권한이 없다고 유저에게 직접 알림.
                Log.e("Confirm", "세번째");

                String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WEB;

                if (PermissionUtils.checkAudioRecordPermission(MainActivity.this)) {

                    SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(serviceType);
                    client = builder.build();

                    client.setSpeechRecognizeListener(MainActivity.this);
                    client.startRecording(true);

                    Toast.makeText(MainActivity.this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();

//                    setButtonsStatus(false);
                }
            }

        } else {
//            startUsingSpeechSDK();
            Log.e("Confirm", "네번째");

        }

        imageKaKaoVoiceStart = findViewById(R.id.img_kakao_voice_start);
        imageKaKaoVoiceStop = findViewById(R.id.img_kakao_voice_stop);
        imageKaKaoBack = findViewById(R.id.img_kakao_back);
        txtComment = findViewById(R.id.txt_kakao_main_comment);
        txtResult = findViewById(R.id.txt_result_speech);

        imageViewTarget = new GlideDrawableImageViewTarget(imageKaKaoVoiceStart);
        //GIF Start Stop 관리
        Glide.with(this).load(R.raw.ani_voice).into(imageViewTarget).onStart();
        //Glide.with(KaKaoTest.this).load(R.raw.ani_voice).into(imageViewTarget).onStop();

        imageKaKaoVoiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Confirm", "imageKaKaoVoiceStart Click");

                Glide.with(MainActivity.this).load(R.raw.ani_voice).into(imageViewTarget).onStart();


                String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WEB;

                if (PermissionUtils.checkAudioRecordPermission(MainActivity.this)) {

                    SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(serviceType);
                    client = builder.build();

                    client.setSpeechRecognizeListener(MainActivity.this);
                    client.startRecording(true);

                    Toast.makeText(MainActivity.this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();

//                    setButtonsStatus(false);
                }
            }
        });


        // 클라이언트 생성 - 마이이크 아이콘에 동작하도록 하자.
        //SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB);

//        setButtonsStatus(true);
    }


    public void onDestroy() {
        super.onDestroy();

        // API를 더이상 사용하지 않을 때 finalizeLibrary()를 호출한다.
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }

    //상황에 따라 버튼을 사용가능할지 불가능하게 할지 설정한다.
    private void setButtonsStatus(boolean enabled) {
        imageKaKaoBack.setEnabled(enabled);
    }


    //SpeechRecognizeListener의 여러가지 메소드들...
    @Override
    public void onReady() {//모든 하드웨어및 오디오 서비스가 모두 준비 된 다음 호출
        Log.e("Confirm onReady : ", "모든 준비가 완료 되었습니다.");
    }

    @Override
    public void onBeginningOfSpeech() { //사용자가 말하기 시작하는 순간 호출
        Log.e("Confirm", "onBeginningOfSpeech : 말하기 시작 했습니다.");
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onEndOfSpeech() {//사용자가 말하기를 끝냈다고 판단되면 호출
        Log.e("Confirm onEndOfSpeech : ", "말하기가 끝났습니다.");
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        Log.e("Confirm onError : ", "에러코드 : " + errorCode + " / 에러내용 : " + errorMsg);

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

                                Log.e("Confirm", "imageKaKaoVoiceStop Click");

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

//                    setButtonsStatus(false);
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

        Log.e("Confirm", "Result: " + texts);

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
                Log.e("Confirm 카카오", builder.toString());
                Glide.with(MainActivity.this).load(R.raw.ani_voice).into(imageViewTarget).onStop();

//                setButtonsStatus(true);
            }
        });

    }

    @Override
    public void onAudioLevel(float audioLevel) {

    }

    @Override
    public void onFinished() {
        Log.e("Confirm", "finish");
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
                Log.e("Confirm", "디버그 keyHash" + signature, e);
            }
        }
        return null;
    }


}