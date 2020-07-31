package com.example.fablixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.example.fablixmobile.data.NetworkManager;
import com.example.fablixmobile.ui.movie.ListViewActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button searchButton = (Button) findViewById(R.id.searchButton);
        final EditText titleText = (EditText) findViewById(R.id.searchText);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleText.getText().toString();
                title = title.trim();
                if(title.length() > 0) {
                    sendSearchRequest(title);
                } else {
                    Toast.makeText(getApplicationContext(), "Movie name cannot be empty", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void sendSearchRequest(final String title) {
        RequestQueue queue = NetworkManager.sharedManager(this).getQueue();
        StringRequest request = new StringRequest(Request.Method.GET, getString(R.string.search_url)+"?title="+title,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("SEARCH RESULT", response);
                        Intent intent = new Intent(MainActivity.this, ListViewActivity.class);
                        intent.putExtra("movieJson", response);
                        intent.putExtra("offset", 0);
                        intent.putExtra("searchKey", title);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("SEARCH RESULT", "error");
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Accept", "application/json");
                return map;
            }
        };
        queue.add(request);
    }
}
