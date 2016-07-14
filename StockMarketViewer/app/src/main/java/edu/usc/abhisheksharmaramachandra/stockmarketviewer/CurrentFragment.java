package edu.usc.abhisheksharmaramachandra.stockmarketviewer;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import uk.co.senab.photoview.PhotoViewAttacher;

public class CurrentFragment extends Fragment{

    HashMap<String,Object> quote=new HashMap<>();
    View current_view;
    PhotoViewAttacher mAttacher;

    public CurrentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        quote=(HashMap<String,Object>)(this.getActivity().getIntent().getExtras().get("quote_json"));
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.current_fragment, container, false);
        current_view=view;
        fillTable();
        displayDailyChart();
        return view;
    }

    /****************** formatting helpers ***********************/
    private  String roundTo2Decimals(String value){
        DecimalFormat df=new DecimalFormat("#0.00");
        String formatted_value=df.format(Double.parseDouble(value));
        return formatted_value;
    }

    private  String billionUnitDenomination(String value){
        Double double_val=Double.parseDouble(value);
        if(double_val>=(Double)1000000000.00){
            double_val/=(Double)1000000000.00;
            return roundTo2Decimals(double_val.toString())+" Billion";
        }else if(double_val>=(Double)1000000.00){
            double_val/=(Double)1000000.00;
            return roundTo2Decimals(double_val.toString())+" Million";
        }else{
            return roundTo2Decimals(double_val.toString());
        }
    }

    private String formatChange(String change,String changeperc,boolean change_trend){
        if(change.contains("-") || changeperc.contains("-")){
            if(change_trend) {
                ImageView iv=(ImageView)current_view.findViewById(R.id.change_trend);
                iv.setImageResource(R.drawable.down);
            }else{
                ImageView iv=(ImageView)current_view.findViewById(R.id.changeytd_trend);
                iv.setImageResource(R.drawable.down);
            }
            if(changeperc.contains("+")){
                return change+" (+"+changeperc+"%)";
            }
            return change+" ("+changeperc+"%)";
        }else if ("0.00".equals(change) && "0.00".equals(changeperc)){
            return change+" ("+changeperc+"%)";
        }else{
            if(change_trend) {
                ImageView iv=(ImageView)current_view.findViewById(R.id.change_trend);
                iv.setImageResource(R.drawable.up);
            }else{
                ImageView iv=(ImageView)current_view.findViewById(R.id.changeytd_trend);
                iv.setImageResource(R.drawable.up);
            }
            return change+" (+"+changeperc+"%)";
        }
    }

    private String formatTimestamp(String date_string){
        SimpleDateFormat input_format=new SimpleDateFormat("EEE MMM dd HH:mm:ss zZ yyyy");
        SimpleDateFormat output_format=new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss");

        String formatted_date="";
        try{
            Date input_date=input_format.parse(date_string);
            formatted_date=output_format.format(input_date);
        }catch(Exception e){
            e.printStackTrace();
        }
        return formatted_date;

    }

    /********************* display helpers **************************/

    private void fillTable() {
        TextView name=(TextView)current_view.findViewById(R.id.stock_name);
        name.setText(quote.get("Name").toString());

        TextView symbol=(TextView)current_view.findViewById(R.id.symbol);
        symbol.setText(quote.get("Symbol").toString());

        TextView lastprice=(TextView)current_view.findViewById(R.id.lastprice);
        lastprice.setText(roundTo2Decimals(quote.get("LastPrice").toString()));

        TextView change=(TextView)current_view.findViewById(R.id.change);
        change.setText(formatChange(roundTo2Decimals(quote.get("Change").toString()),roundTo2Decimals(quote.get("ChangePercent").toString()),true));

        TextView timestamp=(TextView)current_view.findViewById(R.id.timestamp);
        timestamp.setText(formatTimestamp(quote.get("Timestamp").toString()));

        TextView marketcap=(TextView)current_view.findViewById(R.id.marketcap);
        marketcap.setText(billionUnitDenomination(quote.get("MarketCap").toString()));

        int vol=Integer.parseInt(quote.get("Volume").toString());
        String formatted_vol;
        if(vol>=1000000){
            formatted_vol=billionUnitDenomination(quote.get("Volume").toString());
        }else{
            formatted_vol=vol+"";
        }
        TextView volume=(TextView)current_view.findViewById(R.id.volume);
        volume.setText(formatted_vol);

        TextView changeytd=(TextView)current_view.findViewById(R.id.changeytd);
        changeytd.setText(formatChange(roundTo2Decimals(quote.get("ChangeYTD").toString()),roundTo2Decimals(quote.get("ChangePercentYTD").toString()),false));

        TextView high=(TextView)current_view.findViewById(R.id.high);
        high.setText(roundTo2Decimals(quote.get("High").toString()));

        TextView low=(TextView)current_view.findViewById(R.id.low);
        low.setText(roundTo2Decimals(quote.get("Low").toString()));

        TextView open=(TextView)current_view.findViewById(R.id.open);
        open.setText(roundTo2Decimals(quote.get("Open").toString()));
    }

    private void displayDailyChart(){
        try{
            final String url="http://chart.finance.yahoo.com/t?s="+quote.get("Symbol").toString() ;
            ImageView imageView=(ImageView)current_view.findViewById(R.id.daily_chart);
            new ImageLoadTask(url, imageView).execute();
            //mAttacher = new PhotoViewAttacher(imageView);
            imageView.setOnClickListener(new ImageView.OnClickListener(){

                @Override
                public void onClick(View arg0) {

                    // custom dialog
                    final Dialog dialog = new Dialog(current_view.getContext());
                    dialog.setContentView(R.layout.popup);

                    final ImageView img = (ImageView)dialog.findViewById(R.id.charts1);
                    mAttacher = new PhotoViewAttacher(img);
                    new ImageLoadTask(url+"&width=4000&height=3000", img).execute();


                    LinearLayout btnDismiss = (LinearLayout)current_view.findViewById(R.id.LinearLayoutHeading);
                    btnDismiss.setOnClickListener(new LinearLayout.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }});

                    dialog.show();
                }});

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }


}
