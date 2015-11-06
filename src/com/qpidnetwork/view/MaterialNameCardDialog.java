package com.qpidnetwork.view;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.ContactBean;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;

public class MaterialNameCardDialog extends Dialog implements OnClickListener{

	
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	
	private Context mContext;
	private ContactBean contactItem;
	private Callback callback;
	
	private View contentView;
	private FitTopImageView image;
	private ImageButton btnChat;
	private ImageButton btnMail;
	private ImageButton btnCall;
	private ImageButton btnFavorite;
	private MaterialProgressBar progress;
	
	private TextView name;
	private TextView age;
//	private TextView country;
	
	
	private int[] brandColors = new int[]{
			R.color.brand_color_light11,
			R.color.brand_color_light12,
			R.color.brand_color_light13,
			R.color.brand_color_light14,
			R.color.brand_color_light15
		};
	
	
	public static enum NameCardButton{
		PHOTO,
		CHAT,
		MAIL,
		CALL,
		ADDFAVORITE,
		UNFAVORITE
	}
	
	public MaterialNameCardDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(createView());

	}
	
	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	private View createView(){
		
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	
    	if (Build.VERSION.SDK_INT > 12){
    		display.getSize(size);
    	}else{
    		size.y = display.getHeight();
    		size.x = display.getWidth();
    	}
    	
    	int width_times =  Math.round((float)size.x / (56.0f * density));
    	float image_width = ((float)(width_times - 1) * 56.0f * density);
    	float image_height = image_width / 16 * 12;
    	
    	
    	contentView = LayoutInflater.from(mContext).inflate(R.layout.item_name_card, null);
    	image = (FitTopImageView)contentView.findViewById(R.id.image);
    	
    	RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams((int)image_width, (int)image_height);
    	image.setLayoutParams(imageParams);
    	image.setBackgroundColor(mContext.getResources().getColor(brandColors[new Random().nextInt(brandColors.length -1)]));
    	btnChat = (ImageButton)contentView.findViewById(R.id.btn_chat);
    	btnMail = (ImageButton)contentView.findViewById(R.id.btn_mail);
    	btnCall = (ImageButton)contentView.findViewById(R.id.btn_call);
    	btnFavorite = (ImageButton)contentView.findViewById(R.id.btn_favorite);
    	progress = (MaterialProgressBar)contentView.findViewById(R.id.progress);
    	
    	name = (TextView)contentView.findViewById(R.id.name);
    	age = (TextView)contentView.findViewById(R.id.age);
//    	country = (TextView)contentView.findViewById(R.id.country);
    	
    	image.setOnClickListener(this);
    	btnChat.setOnClickListener(this);
    	btnMail.setOnClickListener(this);
    	btnCall.setOnClickListener(this);
    	btnFavorite.setOnClickListener(this);
    	
    	
    	
		return contentView;
    	
	}
	
	private void loadProfilePhoto(){
		if((contactItem.photoBigURL != null)&&(!contactItem.photoBigURL.equals(""))){
			progress.setVisibility(View.VISIBLE);
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(contactItem.photoBigURL);
			new ImageViewLoader(mContext).DisplayImage(image, contactItem.photoBigURL, localPath, new ImageViewLoaderCallback(){

				@Override
				public void OnDisplayNewImageFinish() {
					// TODO Auto-generated method stub
					((Activity) mContext).runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (isShowing())progress.setVisibility(View.GONE);
						}
						
					});
				}

				@Override
				public void OnLoadPhotoFailed() {
					// TODO Auto-generated method stub
					((Activity) mContext).runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (isShowing()) progress.setVisibility(View.GONE);
						}
						
					});
				
				}
				
			} );
		}else{
			progress.setVisibility(View.GONE);
		}
	}
	
	
	private void updateView(){
		if (contactItem == null) return;
		
		btnChat.setEnabled(true);
		image.setBackgroundColor(mContext.getResources().getColor(brandColors[new Random().nextInt(brandColors.length -1)]));
		image.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
		name.setText(contactItem.firstname);
		age.setText(contactItem.age + "");
		progress.requestLayout();
		
		if (contactItem.isInchating){
			btnChat.setImageResource(R.drawable.ic_chat_green_24dp);
		}else if(contactItem.isOnline){
			btnChat.setImageResource(R.drawable.ic_chat_grey600_24dp);
		}else{
			btnChat.setImageResource(R.drawable.ic_chat_greyc8c8c8_24dp);
			btnChat.setEnabled(false);
		}
		
		if (contactItem.isfavorite){
			btnFavorite.setImageResource(R.drawable.ic_favorite_grey600_24dp);
		}else{
			btnFavorite.setImageResource(R.drawable.ic_favorite_outline_grey600_24dp);
		}
		
		
		loadProfilePhoto();
		
	}
	
	@Override public void show(){
		if (contactItem == null){
			throw new  NullPointerException("Can not show without giving a contact item.");
		}
		
		super.show();
		updateView();
	}
	
	public void show(ContactBean contactItem){
		this.contactItem = contactItem;
		super.show();
		updateView();
		
	}
	
	
	public void setContactItem(ContactBean contactItem){
		this.contactItem = contactItem;
	}
	

	public void setCallback(Callback callback){
		this.callback = callback;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		this.dismiss();
		switch (arg0.getId()){
		case R.id.btn_chat:
			break;
		case R.id.btn_mail:
			break;
		case R.id.btn_call:
			break;
		case R.id.btn_favorite:
			break;
		case R.id.image:
			break;
		default:
			break;
		}
		
		if (callback != null){
			preformCallback(arg0.getId());
		}
	}
	
	
	
	private void preformCallback(int buttonId){
		switch (buttonId){
		case R.id.btn_chat:
			callback.OnButtonClick(NameCardButton.CHAT);
			break;
		case R.id.btn_mail:
			callback.OnButtonClick(NameCardButton.MAIL);
			break;
		case R.id.btn_call:
			callback.OnButtonClick(NameCardButton.CALL);
			break;
		case R.id.btn_favorite:
			callback.OnButtonClick((contactItem.isfavorite) ?  NameCardButton.UNFAVORITE : NameCardButton.ADDFAVORITE);
			break;
		case R.id.image:
			callback.OnButtonClick(NameCardButton.PHOTO);
			break;
		default:
			break;
		}
	}
	
	
	
	
	public interface Callback{
		public void OnButtonClick(NameCardButton buttton);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
