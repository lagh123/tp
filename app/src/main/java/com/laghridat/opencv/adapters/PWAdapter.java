package com.laghridat.opencv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.laghridat.opencv.R;
import com.laghridat.opencv.entities.PW;

import java.util.List;




public class PWAdapter extends ArrayAdapter<PW> {
    private List<PW> pwsList;
    private Context context;

    public PWAdapter(Context context, List<PW> pwlist) {
        super(context, 0, pwlist);
        this.pwsList = pwlist;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PW pw = pwsList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        }


        TextView nom = convertView.findViewById(R.id.docs);
        TextView prenom = convertView.findViewById(R.id.objectif);
        TextView ville = convertView.findViewById(R.id.title);
        TextView sexe = convertView.findViewById(R.id.toothid);


        nom.setText("Docs: " + pw.getDocs());
        prenom.setText("Objectif: " + pw.getObjectif());
        ville.setText("Title: " + pw.getTitle());


        return convertView;
    }
}

