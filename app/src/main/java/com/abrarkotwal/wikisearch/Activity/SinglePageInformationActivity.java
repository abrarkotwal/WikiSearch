package com.abrarkotwal.wikisearch.Activity;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.abrarkotwal.wikisearch.Other.AlertDialogManager;
import com.abrarkotwal.wikisearch.Other.SingletonInstance;
import com.abrarkotwal.wikisearch.R;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class SinglePageInformationActivity extends AppCompatActivity {

    private WebView webview;
    private String pageId,title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_page_information);

        pageId = getIntent().getStringExtra("pageId");
        title = getIntent().getStringExtra("title");

        getSupportActionBar().setTitle(title);
        webview = findViewById(R.id.webview);
        WebSettings webSettings= webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient());


        displaySinglePageInformation();
    }

    private void displaySinglePageInformation() {
        String url="https://en.wikipedia.org/w/api.php?action=query&prop=info&inprop=url&format=json&pageids="+pageId;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject mainJsonObject = new JSONObject(response);
                            webview.loadUrl(mainJsonObject.getJSONObject("query").getJSONObject("pages").getJSONObject(pageId).getString("fullurl"));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DialogInterface.OnClickListener onClickTryAgain = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                displaySinglePageInformation();
                            }
                        };
                        if (error instanceof TimeoutError)
                            AlertDialogManager.timeoutErrorAlert(SinglePageInformationActivity.this, onClickTryAgain);
                        else if (error instanceof NoConnectionError)
                            AlertDialogManager.internetConnectionErrorAlert(SinglePageInformationActivity.this, onClickTryAgain);
                    }
                });
        SingletonInstance.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(webview.canGoBack()) {
            webview.goBack();
        }

        else
        {
            super.onBackPressed();
        }
    }
}
