package com.example.speechtranslator;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class Translate extends AppCompatActivity {

    public EditText textResult;
    public EditText transText;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        Intent intent = getIntent();

        textResult = findViewById(R.id.text1);
        transText = findViewById(R.id.text2);


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });
    }


    public void speechInput_1(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    Identifylanguage();
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textResult.setText(result.get(0));
                    textToSpeech.setLanguage(Locale.CANADA_FRENCH);

                    Set<Voice> voices = textToSpeech.getVoices();
                    Object[] voiceArray = voices.toArray();
                    textToSpeech.setVoice((Voice) voiceArray[2]);
                    textToSpeech.setSpeechRate((float) 0.45);
                    //textToSpeech.speak((String) textResult.getText(), TextToSpeech.QUEUE_FLUSH, null);

                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    //Identifying the source language ( the text that has to be converted )
    public void Identifylanguage() {
        String SourceText = textResult.getText().toString();

        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(SourceText)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String languageCode) {
                                if (!languageCode.equals("und")) {   //und means no language identified
                                    Log.i("TAG", "Language: " + languageCode);
                                    getLanguageCode(languageCode);
                                } else {
                                    Log.i("TAG", "Can't identify language.");
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("identify", "error during identifying the language code ");
                            }
                        });
    }

    // getting the language code that has to be passed to language identifier

    private void getLanguageCode(String languageCode) {
        int langCode;
        switch (languageCode) {
            case "hi":
                langCode = FirebaseTranslateLanguage.FR;
                break;
            case "ar":
                langCode = FirebaseTranslateLanguage.AR;
                break;
            default:
                langCode = FirebaseTranslateLanguage.EN;
                break;
        }
        translateText(langCode);
    }

    // After getting the language code translating it to desired language
    private void translateText(int langCode) {
        final String SourceText = textResult.getText().toString();

        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(langCode)  // identified
                        .setTargetLanguage(FirebaseTranslateLanguage.FR) //converted/translated
                        .build();
        final FirebaseTranslator translator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        // Downloading the language package
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                translator.translate(SourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        transText.setText(s);
                        textToSpeech.speak(String.valueOf(transText.getText()), TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failure", "translation failed");
                    }
                });
    }
}