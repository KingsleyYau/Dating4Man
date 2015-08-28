package com.qpidnetwork.dating.livechat.picture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.base.BaseFragmentActivity;

public class PictureSelectActivity extends BaseFragmentActivity{
	
	public static final String SELECT_PICTURE_PATH = "picturePath";
	private GridView gvAlbum;
	private ImageView ivClose;
	private AlbumGridAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_livechat_select_picture);
		initViews();
		initData();
	}
	
	private void initViews(){
		gvAlbum = (GridView)findViewById(R.id.gvAlbum);
		ivClose = (ImageView)findViewById(R.id.ivClose);
		ivClose.setOnClickListener(this);
	}
	
	private void initData(){
		AlbumHelper helper = new AlbumHelper(this);
		mAdapter = new AlbumGridAdapter(this, helper.getAlbumImageList());
		gvAlbum.setAdapter(mAdapter);
		gvAlbum.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra(SELECT_PICTURE_PATH, mAdapter.getItem(position).imagePath);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.ivClose:
			finish();
			break;
		default:
			break;
		}
	}
	
	@Override public void finish(){
		super.finish();
		overridePendingTransition(R.anim.anim_donot_animate, R.anim.anim_translate_from_top_to_buttom);
	}
	

}
