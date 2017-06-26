package com.ilja.emv;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<CardObject> implements View.OnClickListener {

    private ArrayList<CardObject> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtNumber;
        TextView textState;
        TextView txtExp;
        TextView textMode;

    }

    public CustomAdapter(ArrayList<CardObject> data, Context context) {
        super(context, R.layout.custom_listview, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        CardObject dataModel = (CardObject) object;

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CardObject dataModel = getItem(position);
        ViewHolder viewHolder;
        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_listview, parent, false);
            viewHolder.txtNumber = (TextView) convertView.findViewById(R.id.number);
            viewHolder.txtExp = (TextView) convertView.findViewById(R.id.exp);
            viewHolder.textState = (TextView) convertView.findViewById(R.id.txtState);
            viewHolder.textMode = (TextView) convertView.findViewById(R.id.mode);
            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;
        viewHolder.txtNumber.setText(dataModel.getNumber());
        viewHolder.txtExp.setText(dataModel.getExp());

        if (dataModel.isState()) {
            viewHolder.textState.setBackgroundResource(R.color.colorActive);
            Log.i("Adapter", dataModel.getNumber() + " is active");
        } else {
            viewHolder.textState.setBackgroundResource(R.color.colorInactive);
            Log.i("Adapter", dataModel.getNumber() + " is not active");
        }

        if (dataModel.isMode()) {
            viewHolder.textMode.setText("MChip");
        } else {
            viewHolder.textMode.setText("MagStripe");
        }
        return convertView;
    }
}