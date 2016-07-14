package edu.usc.abhisheksharmaramachandra.stockmarketviewer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> suggestions=new ArrayList<>();
    String symbol="";
    static ArrayList<FavoriteRow> rows=new ArrayList<>();
    SharedPreferences sharedPref;
    FavoriteTableAdapter adapter;
    private int mInterval = 10000;
    private Handler mHandler;
    ProgressBar spinner,autocomplete_spinner;
    AutoCompleteTextView actv;


    /********************* Button functions + Helpers ***********************/

    private class FetchQuote extends AsyncTask<String, Void, HashMap<String,Object>>{
        String sym;
        boolean fav;
        int index;
        FetchQuote(){
            sym=symbol;
            this.fav=false;
        }

        FetchQuote(String stock){
            sym=stock;
            this.fav=false;
        }

        FetchQuote(String stock,boolean fav,int index){
            this.sym=stock;
            this.fav=fav;
            this.index=index;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(fav){
                spinner.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected HashMap<String,Object> doInBackground(String... params) {
            StringBuilder result = new StringBuilder();
            String urlString="http://daring-phoenix-127218.appspot.com/index.php?stock="+sym;
            HttpURLConnection urlConnection=null;
            HashMap<String,Object> hashmap_quote=new HashMap<>();

            try {
                URL url=new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                String json_data=String.valueOf(result);
                json_data=json_data.replaceAll("^\"|\"$", "");
                json_data= StringEscapeUtils.unescapeJava(json_data);
                JsonParser parser = new JsonParser();
                JsonElement elem   = parser.parse( json_data );
                JsonElement json_quote = elem.getAsJsonObject();

                JsonObject company_quote=json_quote.getAsJsonObject();
                hashmap_quote.put("Status",company_quote.get("Status").toString().replaceAll("^\"|\"$|\\\\", ""));
                hashmap_quote.put("Name",company_quote.get("Name").toString().replaceAll("^\"|\"$|\\\\", ""));
                hashmap_quote.put("Symbol",company_quote.get("Symbol").toString().replaceAll("^\"|\"$|\\\\", ""));
                hashmap_quote.put("LastPrice",Double.parseDouble(company_quote.get("LastPrice").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("Change",Double.parseDouble(company_quote.get("Change").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("ChangePercent",Double.parseDouble(company_quote.get("ChangePercent").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("Timestamp",company_quote.get("Timestamp").toString().replaceAll("^\"|\"$|\\\\", ""));
                hashmap_quote.put("MarketCap",company_quote.get("MarketCap").toString().replaceAll("^\"|\"$|\\\\", ""));
                hashmap_quote.put("Volume",Integer.parseInt(company_quote.get("Volume").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("ChangeYTD",Double.parseDouble(company_quote.get("ChangeYTD").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("ChangePercentYTD",Double.parseDouble(company_quote.get("ChangePercentYTD").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("Open",Double.parseDouble(company_quote.get("Open").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("Low",Double.parseDouble(company_quote.get("Low").toString().replaceAll("^\"|\"$|\\\\", "")));
                hashmap_quote.put("High",Double.parseDouble(company_quote.get("High").toString().replaceAll("^\"|\"$|\\\\", "")));

            } catch (Exception e) {
                Log.e("json response",e.getMessage());
            }
            finally {
                urlConnection.disconnect();
            }

            return hashmap_quote;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> quote) {
            super.onPostExecute(quote);
            if(fav){
                for(FavoriteRow row:rows){
                    if(row.getSymbol().equals(quote.get("Symbol").toString())){
                        String change = quote.get("ChangePercent").toString();
                        if (change.contains("-")) {
                            row.setChange(roundTo2Decimals(change) + "%");
                        } else {
                            row.setChange("+" + roundTo2Decimals(change) + "%");
                        }
                        row.setPrice("$ " + roundTo2Decimals(quote.get("LastPrice").toString()));
                    }
                }

                index+=1;
                updateRow(index);
            }
        }
    }

    private void getQuote(){
        try {
            ArrayList<HashMap<String,String>> newsfeed;
            HashMap<String,Object> quote= new FetchQuote().execute().get();
            if(quote.get("Status").equals("SUCCESS")){
                newsfeed = new FetchNewsFeed().execute().get();
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("quote_json",quote);
                intent.putExtra("NewsFeed", newsfeed);
                startActivity(intent);
            }else{
                noDetailsAlert();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class FetchNewsFeed extends AsyncTask<String, Void, ArrayList<HashMap<String,String>>>{

        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(String... params) {
            StringBuilder result = new StringBuilder();
            String urlString="http://daring-phoenix-127218.appspot.com/index.php?stock_news="+symbol;
            HttpURLConnection urlConnection=null;
            ArrayList<HashMap<String,String>> newsfeed = new ArrayList<HashMap<String,String>>();

            try {
                URL url=new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                if ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                String json_data=String.valueOf(result);
                json_data=json_data.replaceAll("^\"|\"$", "");
                json_data = StringEscapeUtils.unescapeJava(json_data);
                JsonParser parser = new JsonParser();
                JsonElement elem   = parser.parse( json_data );
                JsonElement json_quote = elem.getAsJsonObject();

                JsonArray newsdata = json_quote.getAsJsonObject().get("d").getAsJsonObject().get("results").getAsJsonArray();

                for(int i=0;i<newsdata.size();i++) {
                    HashMap<String,String> newsitem = new HashMap<String, String>();
                    JsonElement jsonitem = newsdata.get(i);
                    newsitem.put("Url", ((JsonObject) jsonitem).get("Url").toString().replaceAll("^\"|\"$", ""));
                    newsitem.put("Title", ((JsonObject) jsonitem).get("Title").toString().replaceAll("^\"|\"$", ""));
                    newsitem.put("Source", ((JsonObject) jsonitem).get("Source").toString().replaceAll("^\"|\"$", ""));
                    newsitem.put("Description", ((JsonObject) jsonitem).get("Description").toString().replaceAll("^\"|\"$", ""));
                    newsitem.put("Date", ((JsonObject) jsonitem).get("Date").toString().replaceAll("^\"|\"$", ""));
                    newsfeed.add(newsitem);
                }

            } catch (Exception e) {
                Log.e("json response",e.getMessage());
            }
            finally {
                urlConnection.disconnect();
            }

            return newsfeed;
        }
    }


    private void clearInput(){
        AutoCompleteTextView input_field=(AutoCompleteTextView)findViewById(R.id.searchField);
        input_field.setText("");
    }

    /********************   Autocomplete helpers  **************************/

    private void activateAutocomplete() {

        actv = (AutoCompleteTextView) findViewById(R.id.searchField);
        actv.setThreshold(1);
        AutoCompleteAdapter adap = new AutoCompleteAdapter(this,R.layout.simple_dropdown_item_2line);
        actv.setAdapter(adap);
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String company = adapterView.getItemAtPosition(position).toString();
                actv.setText(company.split("DELIM")[0]);
            }
        });
    }

    private class FetchCompanies extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(
                    new Runnable()
                    {
                        @Override
                        public void run() {
                            autocomplete_spinner.setVisibility(View.VISIBLE);
                        }
                    });
        }

        @Override
        protected ArrayList<String> doInBackground(String... constraint) {
            ArrayList<String> companiesList = new ArrayList<String>();
            StringBuilder result = new StringBuilder();
            String search_input=constraint[0].trim();
            String urlString="http://daring-phoenix-127218.appspot.com/index.php?q="+search_input;
            HttpURLConnection urlConnection=null;
            try {
                URL url=new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                String json_data=String.valueOf(result);
                json_data=json_data.replaceAll("^\"|\"$|\\\\", "");
                JsonParser parser = new JsonParser();
                JsonElement elem   = parser.parse( json_data );

                JsonArray elemArr = elem.getAsJsonArray();

                for(int i=0;i<elemArr.size();i++){
                    JsonObject json_suggestion = elemArr.get(i).getAsJsonObject();
                    String suggestion=json_suggestion.get("Symbol").toString().replaceAll("^\"|\"$|\\\\", "")+"DELIM"+json_suggestion.get("Name").toString().replaceAll("^\"|\"$|\\\\", "")+" ("+json_suggestion.get("Exchange").toString().replaceAll("^\"|\"$|\\\\", "")+")";
                    companiesList.add(suggestion);
                    suggestions.add(suggestion.split("DELIM")[0]);
                }


            } catch (Exception e) {
                Log.e("json response",e.getMessage());
            }
            finally {
                urlConnection.disconnect();
            }
            return companiesList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            runOnUiThread(
                    new Runnable()
                    {
                        @Override
                        public void run() {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    autocomplete_spinner.setVisibility(View.GONE);
                                }
                            }, 500);
                        }
                    });
        }
    }

    private class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

        ArrayList<String> companies;
        private Context ctx;

        public AutoCompleteAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);
            ctx=context;
            companies = new ArrayList<String>();
        }

        @Override
        public int getCount(){
            return companies.size();
        }

        @Override
        public String getItem(int index){
            return companies.get(index);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position).split("DELIM")[0]);
            ((TextView) convertView.findViewById(R.id.text2)).setText(getItem(position).split("DELIM")[1]);
            return convertView;
        }


        @Override
        public Filter getFilter(){

            Filter myFilter = new Filter(){

                @Override
                protected FilterResults performFiltering(CharSequence constraint){
                    FilterResults filterResults = new FilterResults();
                    if(constraint != null ) {
                        try {
                            companies = new FetchCompanies().execute(new String[]{constraint.toString()}).get();
                        }
                        catch(Exception e) {
                            Log.e("error in autocomplete", e.getMessage());
                        }
                        if(constraint.length()>=3){
                            // Now assign the values and count to the FilterResults object
                            filterResults.values = companies;
                            filterResults.count = companies.size();
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence contraint, Filter.FilterResults results) {
                    if(results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }

            };

            return myFilter;

        }

    }



    /***********************   Alert helpers   *******************************/

    private void noDetailsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setMessage("No stock details found for symbol: "+symbol);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void invalidSearchAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setMessage("Invalid Symbol");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void noInputAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setMessage("Please enter a Stock Name/Symbol");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    private void deleteFavoriteItemAlert(String name,final int position,final FavoriteTableAdapter adapter){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setMessage("Want to delete "+name+" from favorites?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeRow(position);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /*************************** Favorites table helpers *********************/

    private void removeRow(int position){
        sharedPref =getSharedPreferences("favorite_list",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getKey(rows.get(position).getSymbol()));
        editor.commit();
        rows.remove(position);
    }

    private void displayFavoritesTable() {
        DynamicListView list=(DynamicListView) findViewById(R.id.dynamiclistview);

        reloadFavoriteRows();

        adapter=new FavoriteTableAdapter(this,R.layout.row,rows);

        list.enableSwipeToDismiss(new OnDismissCallback() {
            @Override
            public void onDismiss(@NonNull ViewGroup listView, @NonNull int[] reverseSortedPositions) {
                for(int position:reverseSortedPositions){
                    deleteFavoriteItemAlert(rows.get(position).getName(),position,adapter);
                }
            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                symbol=rows.get(position).getSymbol();
                getQuote();
            }
        });

        list.setAdapter(adapter);

    }

    private void reloadFavoriteRows() {
        Map<String,?> keys=getSharedPreferences("favorite_list", Context.MODE_PRIVATE).getAll();
        if(keys.size()!=rows.size()){
            ArrayList<String> allkeys=new ArrayList<>();
            allkeys.addAll(keys.keySet());
            Collections.sort(allkeys, new CustomComparator());
            for(String key:allkeys){
                String sym=keys.get(key).toString();
                FavoriteRow row=new FavoriteRow();
                try {
                    HashMap<String, Object> quote = new FetchQuote(sym).execute().get();
                    row.symbol=quote.get("Symbol").toString();
                    row.name=quote.get("Name").toString();
                    row.marketcap="Market Cap : "+billionUnitDenomination(quote.get("MarketCap").toString());
                    row.price="$ "+roundTo2Decimals(quote.get("LastPrice").toString());
                    String change=quote.get("ChangePercent").toString();
                    if(change.contains("-")){
                        row.change=roundTo2Decimals(change)+"%";
                    }else{
                        row.change="+"+roundTo2Decimals(change)+"%";
                    }

                   rows.add(row);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateRow( int index) {
        Map<String,?> allkeys=getSharedPreferences("favorite_list", Context.MODE_PRIVATE).getAll();
        ArrayList<String> keys=new ArrayList<>();
        keys.addAll(allkeys.keySet());
        String sym;
        if(index<keys.size()) {
            String key = keys.get(index);
            sym = allkeys.get(key).toString();


            try {

                HashMap<String, Object> quote = new FetchQuote(sym,true,index).execute().get();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    spinner.setVisibility(View.GONE);
                }
            }, 1000);
            adapter.notifyDataSetChanged();
        }
    }

    private void refreshFavoriteTable() {
        spinner.setVisibility(View.VISIBLE);
        Map<String, ?> allkeys = getSharedPreferences("favorite_list", Context.MODE_PRIVATE).getAll();
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(allkeys.keySet());
        updateRow(0);


    }

    private void autorefreshFavoriteTable(boolean on){
        if(on){
            startRepeatingTask();
        }else{
            stopRepeatingTask();
        }
    }

    Runnable mRefresher = new Runnable() {
        @Override
        public void run() {
            try {
                refreshFavoriteTable();
            } finally {

                mHandler.postDelayed(mRefresher, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mRefresher.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mRefresher);
    }

    /******************** General helpers   ******************************/

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

    public class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            int num1=Integer.parseInt(s1.replaceAll("[^0-9]", ""));
            int num2=Integer.parseInt(s2.replaceAll("[^0-9]", ""));
            return num1-num2;
        }
    }

    private String getKey(String value){
        Map<String,?> keys = getSharedPreferences("favorite_list",Context.MODE_PRIVATE).getAll();
        for(String key : keys.keySet()){
            if(keys.get(key).equals(value)){
                return key;
            }
        }
        return null;
    }

    /**********************    Main function + Helpers   **********************/

    private boolean validateInput() {
        AutoCompleteTextView input_field=(AutoCompleteTextView)findViewById(R.id.searchField);
        input_field.setError(null);
        boolean validSymbol=false;
        String input=input_field.getText().toString().trim();
        if(input.length()==0){
            noInputAlert();
            return false;
        }
        for(String suggestion : suggestions){
            if(suggestion.equals(input.toUpperCase())){
                validSymbol=true;
            }
        }
        if(!validSymbol){
            invalidSearchAlert();
            return false;
        }
        symbol=input;
        return true;
    }

    private void setActionBarIcon() {
        ActionBar ab =getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.mipmap.stockmarket_icon);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mHandler=new Handler();
        spinner=(ProgressBar)findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        autocomplete_spinner=(ProgressBar)findViewById(R.id.autocomplete_spinner);
        autocomplete_spinner.setVisibility(View.GONE);

        setActionBarIcon();
        Button getBtn=(Button)findViewById(R.id.getQuoteButton);
        getBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid=validateInput();
                if(valid){
                    getQuote();
                }
            }
        });

        Button clearBtn=(Button)findViewById(R.id.clearButton);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInput();
            }
        });
        activateAutocomplete();

        ImageView refresh=(ImageView)findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                refreshFavoriteTable();
            }
        });

        Switch autorefresh=(Switch)findViewById(R.id.autoRefresh);
        autorefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autorefreshFavoriteTable(isChecked);
            }
        });
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        displayFavoritesTable();
    }

}

