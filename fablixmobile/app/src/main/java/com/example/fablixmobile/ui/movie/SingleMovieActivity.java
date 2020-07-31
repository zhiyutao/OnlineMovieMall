package com.example.fablixmobile.ui.movie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.fablixmobile.R;
import com.example.fablixmobile.data.NetworkManager;
import com.example.fablixmobile.data.model.Movie;
import org.json.JSONObject;

import java.util.ArrayList;

public class SingleMovieActivity extends AppCompatActivity {

    private TextView titleView;
    private TextView yearView;
    private TextView directorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemovie);
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        titleView = findViewById(R.id.title);
        yearView = findViewById(R.id.year);
        directorView = findViewById(R.id.director);
        titleView.setText(id);
        final SingleMovieActivity me = this;
        if (id != null && !id.equals("")) {
            final RequestQueue queue = NetworkManager.sharedManager(this).getQueue();
            String url = "https://10.0.2.2:8443/movie/app";
            Movie movie = new Movie();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url+ "/singlemovie?id="+id,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);
                            try {
                                JSONObject resp = new JSONObject(response);
                                titleView.setText(resp.getString("title"));
                                directorView.setText(resp.getString("director"));
                                yearView.setText(resp.getInt("year")+"");
                                System.out.println(resp.getString("title")+" "+resp.getString("director")+" "+resp.getInt("year"));
                                ArrayList<String> stars = new ArrayList<>();
                                ArrayList<String> genres = new ArrayList<>();
                                int sign = 2;
                                int index = 0;
                                while (true) {
                                    if (resp.has("star" + index)) stars.add(resp.getString("star" + index));
                                    else break;
                                    index++;
                                }
                                index = 0;
                                while (true) {
                                    if (resp.has("genre" + index)) genres.add(resp.getString("genre" + index));
                                    else break;
                                    index++;
                                }
                                SingleMovieViewAdaptor adapter1 = new SingleMovieViewAdaptor(genres, me);
                                ListView listView1 = SingleMovieActivity.this.findViewById(R.id.list1);
                                listView1.setAdapter(adapter1);
                                SingleMovieViewAdaptor adapter2 = new SingleMovieViewAdaptor(stars, me);
                                ListView listView2 = SingleMovieActivity.this.findViewById(R.id.list2);
                                listView2.setAdapter(adapter2);

                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("singlemovie.error", error.toString());
                }
            });
            queue.add(stringRequest);
        }
    }


}
