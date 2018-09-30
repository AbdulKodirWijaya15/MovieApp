package com.kamilismail.movieappandroid.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kamilismail.movieappandroid.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TVFragment extends Fragment {

    public static String TAG = "TVFragment";

    public TVFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tv, container, false);
    }

    public static TVFragment newInstance() {
        TVFragment tvFragment = new TVFragment();
        return tvFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Toast.makeText(getContext(), "Zaladowano TV", Toast.LENGTH_SHORT);
    }
}