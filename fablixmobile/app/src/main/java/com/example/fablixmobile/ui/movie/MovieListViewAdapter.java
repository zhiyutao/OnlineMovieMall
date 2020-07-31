package com.example.fablixmobile.ui.movie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.fablixmobile.R;
import com.example.fablixmobile.data.model.Movie;

import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private ArrayList<Movie> movies;

    public MovieListViewAdapter(ArrayList<Movie> movies, Context context) {
        super(context, R.layout.row, movies);
        this.movies = movies;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.row, parent, false);

        Movie movie = movies.get(position);

        TextView titleView = view.findViewById(R.id.title);
        TextView directorView = view.findViewById(R.id.director);
        TextView yearView = view.findViewById(R.id.year);
        TextView stars0 = view.findViewById(R.id.stars0);
        TextView stars1 = view.findViewById(R.id.stars1);
        TextView stars2 = view.findViewById(R.id.stars2);
        TextView genres0 = view.findViewById(R.id.genres0);
        TextView genres1 = view.findViewById(R.id.genres1);
        TextView genres2 = view.findViewById(R.id.genres2);

        titleView.setText(movie.getTitle());
        directorView.setText(movie.getDirector());
        yearView.setText(movie.getYear()+"");// need to cast the year to a string to set the label
//        System.out.println(movie.getActors().get(0)==null);
//        System.out.println(movie.getActors().get(0)==null?"":movie.getActors().get(0).getName());
//        System.out.println(movie.getActors().get(1)==null);
//        System.out.println(movie.getActors().get(1)==null?"":movie.getActors().get(1).getName());
//        System.out.println(movie.getActors().get(2)==null);
//        System.out.println(movie.getActors().get(2)==null?"":movie.getActors().get(2).getName());
        stars0.setText(movie.getActors().get(0)==null?"":movie.getActors().get(0));
        stars1.setText(movie.getActors().get(1)==null?"":movie.getActors().get(1));
        stars2.setText(movie.getActors().get(2)==null?"":movie.getActors().get(2));
        genres0.setText(movie.getGenres().get(0)==null?"":movie.getGenres().get(0));
        genres1.setText(movie.getGenres().get(1)==null?"":movie.getGenres().get(1));
        genres2.setText(movie.getGenres().get(2)==null?"":movie.getGenres().get(2));


        return view;
    }
}