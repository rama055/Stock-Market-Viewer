package edu.usc.abhisheksharmaramachandra.stockmarketviewer;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class NewsFragment extends Fragment {
    String symbol;
    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.news_fragment, container, false);

        ArrayList<HashMap<String,String>> newsfeed = (ArrayList<HashMap<String,String>>)this.getActivity().getIntent().getExtras().get("NewsFeed");

        populate_newsfeed(myView, newsfeed, container);

        // Inflate the layout for this fragment
        return myView;
    }

    private void populate_newsfeed(View myView, ArrayList<HashMap<String,String>> newsfeed, ViewGroup container){
        HashMap<String,String> newsitem = new HashMap<String, String>();
        LinearLayout newslayout = (LinearLayout)myView.findViewById(R.id.newsfeed);

        for(int i=0;i<newsfeed.size();i++)
        {
            newsitem = newsfeed.get(i);
            TextView title = new TextView(container.getContext());
            title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            title.setTextSize(25);
            title.setPadding(0,10,0,10);
            title.setTypeface(null, Typeface.BOLD);
            title.setPaintFlags(title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            title.setClickable(true);
            String text = "<a href='"+newsitem.get("Url")+"'>"+newsitem.get("Title")+"</a>";
            title.setText(Html.fromHtml(text));
            title.setTextColor(Color.parseColor("#000000"));
            title.setClickable(true);
            title.setLinkTextColor(Color.parseColor("#000000"));
            Linkify.addLinks(title,Linkify.ALL);
            title.setMovementMethod(LinkMovementMethod.getInstance());
            newslayout.addView(title);

            TextView description = new TextView(container.getContext());
            description.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            description.setPadding(0,0,0,10);
            description.setText(newsitem.get("Description"));
            newslayout.addView(description);

            TextView publisher = new TextView(container.getContext());
            publisher.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            publisher.setText("Publisher: "+newsitem.get("Source"));
            newslayout.addView(publisher);

            TextView timestamp = new TextView(container.getContext());
            String tmstp = newsitem.get("Date");
            timestamp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            DateFormat inputft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DateFormat outputft = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss");
            Date date;
            try {
                date = inputft.parse(tmstp);
                timestamp.setText("Date: "+outputft.format(date));
            }
            catch(ParseException e)
            {
                e.printStackTrace();
            }
            timestamp.setPadding(0,0,0,20);
            newslayout.addView(timestamp);

            View line = new View(container.getContext());
            line.setBackgroundColor(Color.parseColor("#E0E0E0"));
            line.setMinimumHeight(4);
            newslayout.addView(line);
        }
    }

}
