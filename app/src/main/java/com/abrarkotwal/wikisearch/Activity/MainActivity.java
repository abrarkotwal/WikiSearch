package com.abrarkotwal.wikisearch.Activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.abrarkotwal.wikisearch.Adapter.Pojo.WikiData;
import com.abrarkotwal.wikisearch.Adapter.SearchAdapter;
import com.abrarkotwal.wikisearch.Other.AlertDialogManager;
import com.abrarkotwal.wikisearch.Other.NetworkBroadcastReceiver;
import com.abrarkotwal.wikisearch.Other.SingletonInstance;
import com.abrarkotwal.wikisearch.R;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private ImageButton voiceSearch;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView      = findViewById(R.id.searchWiki);
        recyclerView    = findViewById(R.id.recyclerView);
        voiceSearch     = findViewById(R.id.voiceSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        isConnected = NetworkBroadcastReceiver.isConnected();
        if (!isConnected){
            fetchOffline();
        }

        searchView.setQueryHint("Start typing to search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                isConnected = NetworkBroadcastReceiver.isConnected();
                if (isConnected) {
                    if (newText.length() > 1) {
                        getSearchData(newText);
                    }
                    else if (newText.length() <= 1){
                        fetchOffline();
                    }
                }
                else{
                    fetchOffline();
                }
                return false;
            }

        });



        voiceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    private void fetchOffline() {
        try {
            FileInputStream fileIn=openFileInput("request.json");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[1024*1024*8];
            String content="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                content +=readstring;
            }
            InputRead.close();
            parseJsonData(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getSearchData(final String searchData) {
        String url = "https://en.wikipedia.org//w/api.php?action=query&format=json&prop=pageimages%7Cpageterms&generator=prefixsearch&redirects=1&formatversion=2&piprop=thumbnail&pithumbsize=200&wbptterms=description&gpssearch=" + searchData;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseJsonData(response);
                        try {
                            String textjson= response.toString();
                            FileOutputStream fos = openFileOutput("request.json",MODE_PRIVATE);
                            fos.write(textjson.getBytes()); //encode
                            fos.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
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
                                getSearchData(searchData);
                            }
                        };
                        if (error instanceof TimeoutError)
                            AlertDialogManager.timeoutErrorAlert(MainActivity.this, onClickTryAgain);
                        else if (error instanceof NoConnectionError)
                            AlertDialogManager.internetConnectionErrorAlert(MainActivity.this, onClickTryAgain);
                    }
                });
        SingletonInstance.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void parseJsonData(String response) {
        final List<WikiData> wikiDataList = new ArrayList<>();
        try {
            JSONObject mainJsonObject = new JSONObject(response);

            JSONArray jsonArray = mainJsonObject.getJSONObject("query").getJSONArray("pages");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                WikiData wikiData = new WikiData();
                wikiData.title = jsonObject.getString("title");
                wikiData.pageid = jsonObject.getString("pageid");
                if (jsonObject.has("terms")) {
                    JSONArray jaDesc = jsonObject.getJSONObject("terms").getJSONArray("description");
                    for (int j = 0; j < jaDesc.length(); j++) {
                        wikiData.description = jaDesc.getString(j);
                    }
                } else {
                    wikiData.description = "Not Found";
                }

                if (jsonObject.has("thumbnail")) {
                    wikiData.imagepath = jsonObject.getJSONObject("thumbnail").getString("source");
                } else {
                    wikiData.imagepath = "Not Found";
                }

                wikiDataList.add(wikiData);
            }

            if (wikiDataList.size() == 0) {
                fetchOffline();
            }
            else{
                adapter = new SearchAdapter(MainActivity.this, wikiDataList);
                recyclerView.setAdapter(adapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("Abrar",result.get(0));
                    getSearchData(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

}
