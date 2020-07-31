package com.example.fablixmobile.ui.movie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.example.fablixmobile.R;
import com.example.fablixmobile.data.NetworkManager;
import com.example.fablixmobile.data.model.Movie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewActivity extends AppCompatActivity {
    private int currOffset;
    private String searchKey="";
    static final int LIMIT = 20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listview);
        // test();
        String movieJson = getIntent().getStringExtra("movieJson");
        currOffset = getIntent().getIntExtra("offset", 0);
        searchKey = getIntent().getStringExtra("searchKey");

        try {
            ArrayList<Movie> list = parseMovies(movieJson);
            final ArrayList<Movie> movies;
            if(list.size() > LIMIT)
                movies = new ArrayList<>(list.subList(0, LIMIT));
            else
                movies = list;
            Button prevButton = findViewById(R.id.prevButton);
            Button nextButton = findViewById(R.id.nextButton);

            MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);
            ListView listView = ListViewActivity.this.findViewById(R.id.list);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Movie movie = movies.get(position);
                    String message = String.format("Clicked on position: %d, name: %s, %d", position,
                            movie.getTitle(), movie.getYear());
                    Toast.makeText(ListViewActivity.this.getApplicationContext(), message,
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ListViewActivity.this, SingleMovieActivity.class);
                    intent.putExtra("id",movie.getId());
                    startActivity(intent);
                }
            });

            // prev and next
            if(list.size() <= LIMIT) nextButton.setEnabled(false);
            if(currOffset == 0) prevButton.setEnabled(false);
            addClickListener(prevButton, nextButton, listView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addClickListener(final Button prevButton, final Button nextButton, final ListView listView) {
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                if(currOffset - LIMIT < 0) return;
                currOffset -= LIMIT;
                sendSearchRequest(prevButton, nextButton, listView);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                currOffset += LIMIT;
                sendSearchRequest(prevButton, nextButton, listView);
            }
        });
    }

    private void sendSearchRequest(final Button prevButton, final Button nextButton, final ListView listView) {
        RequestQueue queue = NetworkManager.sharedManager(this).getQueue();
        StringRequest request = new StringRequest(Request.Method.GET,
                getString(R.string.search_url)+"?title="+searchKey+"&offset="+currOffset,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            ArrayList<Movie> movies = ListViewActivity.parseMovies(response);
                            // prev and next
                            if(movies.size() <= LIMIT) nextButton.setEnabled(false);
                            else nextButton.setEnabled(true);
                            if(currOffset == 0) prevButton.setEnabled(false);
                            else prevButton.setEnabled(true);

                            if(movies.size() > LIMIT)
                                movies = new ArrayList<>(movies.subList(0, LIMIT));

                            MovieListViewAdapter adapter = new MovieListViewAdapter(movies, ListViewActivity.this);
                            listView.setAdapter(adapter);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    private static ArrayList<Movie> parseMovies(String movieJson) throws JSONException {
        ArrayList<Movie> movies = new ArrayList<>();
        JSONArray resp = new JSONArray(movieJson);
        for (int i = 0; i < resp.length(); i++) {
            Movie tmp = new Movie();
            JSONObject jtmp = resp.getJSONObject(i);
            tmp.setTitle(jtmp.getString("title"));
            tmp.setId(jtmp.getString("id"));
            tmp.setYear(jtmp.getInt("year"));
            tmp.setDirector(jtmp.getString("director"));
            List<String> stars = new ArrayList<>();
            List<String> genres = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                if (jtmp.has("star" + j)) stars.add(jtmp.getString("star" + j));
                else stars.add(null);
                if (jtmp.has("genre" + j)) genres.add(jtmp.getString("genre" + j));
                else genres.add(null);
            }
            tmp.setActors(stars);
            tmp.setGenres(genres);
            movies.add(tmp);
        }
        return movies;
    }

    private void test() {
        //this should be retrieved from the database and the backend server
        String url = "https://10.0.2.2:8443/movie/app";
        final ListViewActivity me = this;
        final RequestQueue queue = NetworkManager.sharedManager(this).getQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+ "/list",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try {
                            final ArrayList<Movie> movies = parseMovies(response);
                            System.out.println("hhh1");
                            MovieListViewAdapter adapter = new MovieListViewAdapter(movies, me);
                            ListView listView = ListViewActivity.this.findViewById(R.id.list);
                            listView.setAdapter(adapter);
                            System.out.println("hhh2");
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Movie movie = movies.get(position);
                                    String message = String.format("Clicked on position: %d, name: %s, %d", position,
                                            movie.getTitle(), movie.getYear());
                                    Toast.makeText(ListViewActivity.this.getApplicationContext(), message,
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ListViewActivity.this, SingleMovieActivity.class);
                                    intent.putExtra("id",movie.getId());
                                    startActivity(intent);
                                }
                            });
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                    }
                });
        queue.add(stringRequest);
    }
}