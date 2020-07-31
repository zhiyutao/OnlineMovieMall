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

import java.util.ArrayList;

public class SingleMovieViewAdaptor extends ArrayAdapter<String> {
    private ArrayList<String> names;

    public SingleMovieViewAdaptor(ArrayList<String> names, Context context) {
        super(context, R.layout.row, names);
        this.names = names;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.row2, parent, false);

        String name = names.get(position);

        TextView titleView = view.findViewById(R.id.title);
        titleView.setText(name);
        return view;
    }
}
