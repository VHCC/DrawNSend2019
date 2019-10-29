package ichen.chu.drawnsend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import ichen.chu.drawableviewlibs.DrawableView;
import ichen.chu.drawableviewlibs.DrawableViewConfig;
import ichen.chu.drawnsend.HoverMenu.HoverMenuFactory;
import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private DrawableView drawableView;
    private DrawableViewConfig config = new DrawableViewConfig();

    private int RC_SIGN_IN = 1001;

    private HoverView mHoverView;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(App.TAG, "* onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();

        initGoogleAPI();
        initHover();
    }

    private void initGoogleAPI() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    private void initUi() {
        drawableView = (DrawableView) findViewById(R.id.paintView);
        Button strokeWidthMinusButton = (Button) findViewById(R.id.strokeWidthMinusButton);
        strokeWidthMinusButton.setVisibility(View.GONE);
        Button strokeWidthPlusButton = (Button) findViewById(R.id.strokeWidthPlusButton);
        strokeWidthPlusButton.setVisibility(View.GONE);
        final Button changeColorButton = (Button) findViewById(R.id.changeColorButton);
        Button undoButton = (Button) findViewById(R.id.undoButton);
        undoButton.setVisibility(View.GONE);
        Button clearButton = (Button) findViewById(R.id.clearButton);
        Button getButton = (Button) findViewById(R.id.getButton);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        config.setStrokeColor(getResources().getColor(android.R.color.black));
        config.setShowCanvasBounds(true);
        config.setStrokeWidth(20.0f);
        config.setMinZoom(1.0f);
        config.setMaxZoom(2.0f);
        config.setCanvasHeight(height);
        config.setCanvasWidth(width);
        drawableView.setConfig(config);

        strokeWidthPlusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                config.setStrokeWidth(config.getStrokeWidth() + 10);
            }
        });
        strokeWidthMinusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                config.setStrokeWidth(config.getStrokeWidth() - 10);
            }
        });
        changeColorButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Random random = new Random();
                int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                config.setStrokeColor(color);
                changeColorButton.setBackgroundColor(color);
            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawableView.undo();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawableView.clear();
            }
        });

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap outB = drawableView.obtainBitmap().copy(Bitmap.Config.ARGB_8888, true);
                        Canvas canvas = new Canvas(outB);
                        canvas.drawColor(Color.WHITE);
                        canvas.drawBitmap(drawableView.obtainBitmap(), 0, 0, null);

//                        image.setImageBitmap(outB);

                        String tmp = "/sdcard/test/" + System.currentTimeMillis() + ".jpeg";
                        File file = new File(tmp);

                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            if (outB.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                                out.flush();
                                out.close();
                            }
                            uploadFile(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }

            private void signIn() {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void initHover() {
        Log.d(App.TAG, "* initHover()");
        try {
            final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.AppTheme);
            HoverMenu hoverMenu = new HoverMenuFactory().createDemoMenuFromCode(contextThemeWrapper, Bus.getInstance());

            mHoverView = findViewById(R.id.hovermenu);
            mHoverView.setMenu(hoverMenu);
            mHoverView.collapse();
        } catch (Exception e) {
            Log.e(App.TAG, "Failed to create demo menu from file. e= " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Log.d(App.TAG, "* Signed in successfully");
//            Log.d(TAG, "- account= " + account);
            Log.d(App.TAG, "- getDisplayName= " + account.getDisplayName());
            Log.d(App.TAG, "- getEmail= " + account.getEmail());
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(App.TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void uploadFile(File file) {
        try {
            try {
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
                Log.d(App.TAG, "B");
                RequestBody req = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name", file.getName())
                        .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file)).build();
                Log.d(App.TAG, "C");

                Request request = new Request.Builder()
                        .url("http://172.20.10.3:3000/api/post_official_doc_upload_file")
                        .post(req)
                        .build();
                Log.d(App.TAG, "D");

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                Log.d(App.TAG, "uploadImage: " + response.body().string());

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                Log.e(App.TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e(App.TAG, "Other Error: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
