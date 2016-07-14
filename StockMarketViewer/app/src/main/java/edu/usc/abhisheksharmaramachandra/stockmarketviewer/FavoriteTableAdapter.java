package edu.usc.abhisheksharmaramachandra.stockmarketviewer;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class FavoriteTableAdapter extends ArrayAdapter {
    ArrayList<FavoriteRow> rows=new ArrayList<>();

    private Activity context;
    FavoriteTableAdapter(Activity context,int viewResourceId,ArrayList<FavoriteRow> rows){
        super(context,viewResourceId);
        this.context=context;
        this.rows=rows;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public FavoriteRow getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.row,null);

        FavoriteRow row=rows.get(position);

        TextView tv_sym=(TextView)rowView.findViewById(R.id.fav_symbol);
        tv_sym.setText(row.getSymbol());

        TextView tv_price=(TextView)rowView.findViewById(R.id.fav_price);
        tv_price.setText(row.getPrice());

        TextView tv_change=(TextView)rowView.findViewById(R.id.fav_change);
        tv_change.setText(row.getChange());
        if(row.getChange().contains("+")){
            tv_change.setBackgroundColor(Color.parseColor("#00FF00"));
        }else{
            tv_change.setBackgroundColor(Color.parseColor("#FF0000"));
        }

        TextView tv_name=(TextView)rowView.findViewById(R.id.fav_name);
        tv_name.setText(row.getName());

        TextView tv_market=(TextView)rowView.findViewById(R.id.fav_market);
        tv_market.setText(row.getMarketcap());

        return rowView;
    }
}
