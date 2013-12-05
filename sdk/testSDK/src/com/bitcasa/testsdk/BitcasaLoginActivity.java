package com.bitcasa.testsdk;

import com.bitcasa.client.utility.BitcasaUtility;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BitcasaLoginActivity extends Activity {

	private WebView  mWebview;
	public static final String EXTRA_BITCASA_AUTH_URL = "extra_bitcasa_authentication_url";
	public static final String EXTRA_BITCASA_AUTH_CODE = "extra_bitcasa_authentication_code";
	public static final int REQUEST_CODE_BITCASA_AUTH = 0;
	public static final int RESULT_CODE_BITCASA_AUTH = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mWebview = new WebView(this);
		mWebview.setVisibility(View.VISIBLE);
        setContentView(mWebview);
        
        Intent intent = getIntent();
        String authorizationUrl = intent.getStringExtra(EXTRA_BITCASA_AUTH_URL);
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
     
            		String authorization_code = null;
                
	                if ( url.indexOf("authorization_code=") != -1 ) {
	                	String temp = url;
	                	String[] token = temp.split("authorization_code=");
	                	authorization_code = token[1];
	                	
	                	Intent data = new Intent();
		                data.putExtra(EXTRA_BITCASA_AUTH_CODE, authorization_code);
		                BitcasaLoginActivity.this.setResult(RESULT_CODE_BITCASA_AUTH, data);
		                finish();
	                }                

                return super.shouldOverrideUrlLoading(view, url); 
            }
        });        
        mWebview.loadUrl(authorizationUrl);		
	}
	
}
