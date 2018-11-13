package com.example.sample_oauth2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson.JacksonFactory;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.AuthorizationUIController;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private SharedPreferencesCredentialStore mCredentialStore;
    private OAuthManager mOAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCredentialStore =
                new SharedPreferencesCredentialStore(getApplicationContext(),
                        "preferenceFileName", new JacksonFactory());

        AuthorizationFlow.Builder builder = new AuthorizationFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                AndroidHttp.newCompatibleTransport(),
                new JacksonFactory(),
                new GenericUrl(OAuthConstants.TOKEN_ENDPOINT.toString()),
                new ClientParametersAuthentication("CLIENT_ID", "CLIENT_SECRET"),
                OAuthConstants.CLIENT_ID,
                OAuthConstants.AUTHORIZATION_ENDPOINT.toString());
        builder.setScopes(Arrays.asList("friends"));
        builder.setCredentialStore(mCredentialStore);

        AuthorizationUIController controller =
                new DialogFragmentController(getFragmentManager()) {

                    @Override
                    public String getRedirectUri() throws IOException {
                        return OAuthConstants.REDIRECT_URI.toString();
                    }

                    @Override
                    public boolean isJavascriptEnabledForWebView() {
                        return true;
                    }

                    @Override
                    public boolean disableWebViewCache() {
                        return false;
                    }

                    @Override
                    public boolean removePreviousCookie() {
                        return false;
                    }

                };


        mOAuth = new OAuthManager(builder.build(), controller);

        doAuthorization();

    }

    private void doAuthorization() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Credential credential = mOAuth.authorizeImplicitly("userId", null, null).getResult();
                    String token = credential.getAccessToken();
                    Log.d("test", "Token = " + token);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
