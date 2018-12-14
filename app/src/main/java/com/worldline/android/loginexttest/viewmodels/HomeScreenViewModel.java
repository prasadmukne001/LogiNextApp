package com.worldline.android.loginexttest.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.android.databinding.library.baseAdapters.BR;


public class HomeScreenViewModel extends BaseObservable
{

	public static final String galleryMessage = "Select Gallery Photos";
	public static final String cameraMessage = "Opening Camera";

	@Bindable
	public String action = null;

	public String getAction()
	{
		return action;
	}


	private void setAction(String action)
	{

		this.action = action;
		notifyPropertyChanged(BR.action);
	}


	public void onGalleryClick()
	{
		setAction(galleryMessage);
	}

	public void onCameraClick()
	{
		setAction(cameraMessage);
	}

}