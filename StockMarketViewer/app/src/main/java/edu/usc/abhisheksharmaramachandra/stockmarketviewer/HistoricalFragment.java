package edu.usc.abhisheksharmaramachandra.stockmarketviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;


public class HistoricalFragment extends Fragment {
    String symbol;
    View historical_view;
    public HistoricalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        symbol = ((HashMap<String, Object>) (this.getActivity().getIntent().getExtras().get("quote_json"))).get("Symbol").toString();
        // Inflate the layout for this fragment
        historical_view = inflater.inflate(R.layout.historical_fragment, container, false);
        displayHistoricalChart();
        return historical_view;
    }

    private void displayHistoricalChart() {
        final WebView webview = (WebView)historical_view.findViewById(R.id.webView);
        try{
            webview.getSettings().setJavaScriptEnabled(true);
            webview.addJavascriptInterface(new Symbol(), symbol);
            webview.loadUrl("file:///android_asset/highchart.html");
            webview.setWebViewClient(new WebViewClient(){
                public void onPageFinished(WebView view, String url){
                    webview.loadUrl("javascript:init('" + symbol + "')");
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private class Symbol {
        @JavascriptInterface
        public String toString() { return symbol; }
    }

}
