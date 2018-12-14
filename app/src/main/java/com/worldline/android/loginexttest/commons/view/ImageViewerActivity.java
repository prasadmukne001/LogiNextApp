package com.worldline.android.loginexttest.commons.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import com.worldline.android.loginexttest.utility.LogiNextConstants;
import com.worldline.android.loginexttest.R;

public class ImageViewerActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_viewer);

		try
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			loadImageInWebView();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void loadImageInWebView()
	{
		String imageUrl = "file:///"+getIntent().getStringExtra(LogiNextConstants.FILE_PATH);
		WebView wv = (WebView) findViewById(R.id.yourwebview);
		wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setLoadWithOverviewMode(true);
		wv.getSettings().setUseWideViewPort(true);
		wv.loadUrl(imageUrl);
	}
}
