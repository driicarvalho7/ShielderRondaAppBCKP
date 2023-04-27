package com.example.location_tracker_shielder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Base64;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ResponseHandler;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.BasicResponseHandler;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;


public class MainActivity2 extends AppCompatActivity {


    private Button sendSMSButton;

    private EditText phone;
    private String baseURL = "https://www.shielder.com.br/alexa/";
    private static final String TAG = "MainActivity2";
    private static boolean mVerificationInProgress = true;
    private static Dialog dialog_progress;
    private String saved_mobile;
    private String mobile;
    private String imsi;
    private String msisdn;
    private EditText editMobile;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private Context context = null;
    private String manufacturer;
    private String model;
    private String condoPhone;
    private String firebaseRegId = null;
    private int width;
    private int height;
    private int currentapiVersion;
    private String appVersion;

    private EditText editValidationCode;

    private boolean _isOn = true;

    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferenceEditor;
    private boolean registeredUser = false;

    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();


    private String mVerificationId;

    private String encrypted_mobile;
    private String decrypted_mobile;

    private WebView mWebView;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private final BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!isOnline()) {
                    internetError();
                    //showToast(getResources().getString(R.string.internet_error));
                } else {
                    if (!saved_mobile.equals("")) {
                        registeredUser = true;
                    }
                }
            } catch (Exception e) {
                //new salvaLogs().execute("CRASH","BroadcastReceiver-onReceivedError: " + saved_mobile + ", " + e.getMessage());
                //FIREBASE EVENT
                Bundle bundle = new Bundle();
                bundle.putString("saved_mobile", mobile);
                bundle.putString("error", e.getMessage());
                mFirebaseAnalytics.logEvent("CRASH_BroadcastReceiver_onReceived", bundle);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        mWebView = findViewById(R.id.webview);


        sendSMSButton = findViewById(R.id.btn_send_validation_code);
        phone = findViewById(R.id.edt_mobile);


        mAuth = FirebaseAuth.getInstance();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Get application context
        context = getApplicationContext();

        // Create App saved preferences
        try {
            //Fabric.with(this, new Crashlytics());

            //Crashlytics.log(1, "tag", "msg");
            crashlytics.log("msg");

            // Obtain the FirebaseAnalytics instance.
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            manufacturer = Build.MANUFACTURER;
            manufacturer = Normalizer.normalize(manufacturer, Normalizer.Form.NFD).replaceAll("[^a-zA-Z0-9]", "").trim();
            manufacturer = manufacturer.replace(" ", "");
            model = Build.MODEL;
            model = Normalizer.normalize(model, Normalizer.Form.NFD).replaceAll("[^a-zA-Z0-9]", "").trim();
            model = model.replace(" ", "");
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
            currentapiVersion = Build.VERSION.SDK_INT;
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    getPackageName(), 0);
            appVersion = info.versionName;


            //showToast("Teste 2");

        } catch (Exception e) {
            //new salvaLogs().execute("CRASH","onCreate - App Info Exception: " + e.getMessage());
            //FIREBASE EVENT
            Bundle bundle = new Bundle();
            bundle.putString("saved_mobile", mobile);
            bundle.putString("error", e.getMessage());
            mFirebaseAnalytics.logEvent("CRASH_onCreate_App_Info_Exception", bundle);
        }


        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(getApplication());


        registerReceivers();
        setActionBar();


        decrypted_mobile = preferenceSettings.getString("mobile", "");
        saved_mobile = preferenceSettings.getString("encoded_mobile_AES", "");

        if(saved_mobile!=null && !saved_mobile.equals("")){
            if(decrypted_mobile!=null && !decrypted_mobile.equals("")){
                Intent intent = new Intent(this, RondaViewActivity.class);
                intent.putExtra("saved_mobile", decrypted_mobile);
                intent.putExtra("encoded_mobile_AES", saved_mobile);
                startActivity(intent);
                return;
            }
        }
        loadWebAuth();
    }

    private void loadWebAuth() {

        WebViewClient mWebViewClient = new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if(!isOnline()){
                    Toast.makeText(getApplicationContext(), "Verifique sua Internet!", Toast.LENGTH_LONG).show();
                    return false;
                }else if(!url.contains("shielder.com.br")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }else{
                    return false;
                }
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
                Log.i("LOADED_URL: ", url);

                if(url.contains("#grant_type=authorization_code")){

                    try {

                        decrypted_mobile = url.split("code=")[1].split("&")[0];

                        String passphrase = "e5cb9bca974621dc77221eaeb937ec3a7742a793874a9fad04650ec8e82f002b";


                        SecureRandom random = new SecureRandom();
                        byte[] iv_byte = new byte[16];
                        random.nextBytes(iv_byte);

                        byte[] bytes = hexStringToByteArray(passphrase);


                        SecretKey key = new SecretKeySpec(bytes, 0, bytes.length, "AES/CBC/PKCS5Padding");


                        String encrypted_bytes64 = Base64.encodeToString(encrypt(decrypted_mobile.getBytes(StandardCharsets.US_ASCII), key, iv_byte), Base64.DEFAULT);
                        String iv64 = Base64.encodeToString(iv_byte, Base64.DEFAULT);



                        JSONObject json_encrypted = new JSONObject();
                        json_encrypted.put("iv", iv64);
                        json_encrypted.put("data", encrypted_bytes64);


                        encrypted_mobile = Base64.encodeToString(new String(String.valueOf(json_encrypted)).getBytes(StandardCharsets.US_ASCII), Base64.DEFAULT);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
                        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();

                        preferenceEditor.putString("encoded_mobile_AES", encrypted_mobile);
                        preferenceEditor.putString("mobile", decrypted_mobile);

                        preferenceEditor.apply();

                        Intent intent = new Intent(context, RondaViewActivity.class);
                        intent.putExtra("encoded_mobile_AES", encrypted_mobile);
                        intent.putExtra("mobile", decrypted_mobile);


                        mWebView.destroy();

                        startActivity(intent);


                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        mWebView.setWebViewClient(mWebViewClient);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl(baseURL);

    }

    public static String decrypt(String encoded64_json, SecretKey key) {
        try {

            byte[] json64 = Base64.decode(encoded64_json, Base64.DEFAULT);

            JSONObject json = new JSONObject(new String(json64, StandardCharsets.US_ASCII));

            String iv_64 = json.getString("iv_64");
            String encrypted_bytes64 = json.getString("encrypted_bytes64");



            byte[] IV = Base64.decode(iv_64, Base64.DEFAULT);
            byte[] cipherText = Base64.decode(encrypted_bytes64, Base64.DEFAULT);


            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decryptedText = cipher.doFinal(cipherText);
            return new String(decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] cipherText = cipher.doFinal(plaintext);
        return cipherText;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }


    @Override
    public void onBackPressed() {

        if (isOnline() && mWebView!=null) {
            if(mWebView.isFocused() && mWebView.canGoBack()) {
                mWebView.goBack();
                return;
            }
        }

        super.onBackPressed();

    }

    private void showToast(final String message) {
        try {
            if (!this.isFinishing()) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            //new salvaLogs().execute("CRASH","showToast: " + saved_mobile + ", " + e.getMessage());
            //FIREBASE EVENT
            Bundle bundle = new Bundle();
            bundle.putString("saved_mobile", saved_mobile);
            bundle.putString("error", e.getMessage());
            mFirebaseAnalytics.logEvent("CRASH_showToast", bundle);
        }
    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();

            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            //new salvaLogs().execute("CRASH","isOnline : " + saved_mobile + ", " + e.getMessage());
            //FIREBASE EVENT
            Bundle bundle = new Bundle();
            bundle.putString("saved_mobile", saved_mobile);
            bundle.putString("error", e.getMessage());
            mFirebaseAnalytics.logEvent("CRASH_isOnline", bundle);
            return false;
        }
    }

    private void setActionBar() {

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            // Disable the default and enable the customaa
            actionBar.hide();
            actionBar.setElevation(0);
        }
    }

    private void registerReceivers() {
        registerReceiver(mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void internetError() {
        try {
            _isOn = false;
            showToast("Internet Error \nPlease, check your connection");

        } catch (Exception e) {
            //new salvaLogs().execute("CRASH","internetError: " + saved_mobile + ", " + e.getMessage());
            //FIREBASE EVENT
            Bundle bundle = new Bundle();
            bundle.putString("saved_mobile", saved_mobile);
            bundle.putString("error", e.getMessage());
            mFirebaseAnalytics.logEvent("CRASH_internetError", bundle);
        }
    }
}