package edu.usc.abhisheksharmaramachandra.stockmarketviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private NonSwipeableViewPager viewPager;
    private boolean favAdded = false;
    HashMap<String,Object> quote= new HashMap<>();
    String symbol;
    SharedPreferences sharedPref ;
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupTabs();

        parseJson();

        setActionBarTitle(quote.get("Name").toString());

        sharedPref =getSharedPreferences("favorite_list",Context.MODE_PRIVATE);
    }

    /***************** set title to company name  ***************************/
    private void setActionBarTitle(String company_name){
        this.setTitle(company_name);
    }

    /*****************  Parse quote json passed from main activity **********/
    private void parseJson(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            quote = (HashMap<String, Object>) extras.get("quote_json");
            symbol=quote.get("Symbol").toString();
        }
    }

    /*************************** Tab helpers *******************************/
    private void setupTabs() {
        viewPager = (NonSwipeableViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(final NonSwipeableViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CurrentFragment(), "CURRENT");
        adapter.addFragment(new HistoricalFragment(), "HISTORICAL");
        adapter.addFragment(new NewsFragment(), "NEWS");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    /*************** Action Bar helpers , Favorites, Facebook *****************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean fav=false;
        Map<String,?> keys = getSharedPreferences("favorite_list",Context.MODE_PRIVATE).getAll();
        for(String key:keys.keySet()){
            if(keys.get(key).equals(symbol)){
                fav=true;
            }
        }

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(fav){
            menu.getItem(0).setIcon(R.drawable.yellowstar);
        }else{
            menu.getItem(0).setIcon(R.drawable.emptystar);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.favorites:
                favAdded=false;
                Map<String,?> keys = getSharedPreferences("favorite_list",Context.MODE_PRIVATE).getAll();
                for(String key:keys.keySet()){
                    if(keys.get(key).equals(symbol)){
                        favAdded=true;
                    }
                }

                if (favAdded==false) {
                    item.setIcon(R.drawable.yellowstar);
                    favorites("Bookmarked "+ this.getTitle() + "!!");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getNextKey()+quote.get("Symbol").toString(), quote.get("Symbol").toString());
                    editor.commit();

                    FavoriteRow row=new FavoriteRow();
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

                    MainActivity.rows.add(row);
                }
                else {
                    item.setIcon(R.drawable.emptystar);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove(getKey(quote.get("Symbol").toString()));
                    editor.commit();
                    for(FavoriteRow row : MainActivity.rows){
                        if(row.getSymbol().equals(symbol)){
                            MainActivity.rows.remove(row);
                            break;
                        }
                    }
                }

                return true;
            case R.id.facebook:
                facebook_post("Sharing "+ this.getTitle() + "!!");
                return true;

            case android.R.id.home:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void favorites(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void facebook_post(String message) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Current Stock Price of "+quote.get("Name").toString()+", "+quote.get("LastPrice").toString())
                    .setContentDescription(
                            "Stock Information of "+quote.get("Name").toString())
                    .setContentUrl(Uri.parse("http://chart.finance.yahoo.com/t?s="+quote.get("Symbol").toString()+"&lang=en-US&width=4000&height=3000"))
                    .build();

            shareDialog.show(linkContent);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(resultCode==-1)
        {
            Toast.makeText(this, "You shared this post", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "You did not share this post", Toast.LENGTH_SHORT).show();

        }

    }

    /******************** General Helpers *************************/

    private String getNextKey(){
        Map<String,?> keys = getSharedPreferences("favorite_list",Context.MODE_PRIVATE).getAll();
        int next=0;
        for(String key:keys.keySet()){
            int row=Integer.parseInt(key.split(keys.get(key).toString())[0]);
            next= next<row?row:next;
        }
        return (next+1)+"";
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

}
