package com.qpidnetwork.dating.livechat.picture;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.ChatActivity;
import com.qpidnetwork.framework.base.BaseFragment;

public class PictureSelectFragment extends BaseFragment{
	
	private GridView gvAlbum;
	private ImageView ivScale;
	
	private AlbumGridAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_picture_select, null);
		initViews(view);
		return view;
	}
		
	private void initViews(View view){
		gvAlbum = (GridView)view.findViewById(R.id.gvAlbum);
		ivScale = (ImageView)view.findViewById(R.id.ivScale);
		ivScale.setOnClickListener(this);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		AlbumHelper helper = new AlbumHelper(getActivity());
		mAdapter = new AlbumGridAdapter(getActivity(), helper.getAlbumImageList());
		gvAlbum.setAdapter(mAdapter);
		gvAlbum.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				((ChatActivity)getActivity()).sendPrivatePhoto(mAdapter.getItem(position).imagePath);
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.ivScale:
			Intent intent = new Intent(getActivity(), PictureSelectActivity.class);
			getActivity().startActivityForResult(intent, ChatActivity.CHAT_SELECT_PHOTO);
			break;
		default:
			break;
		}
	}

		
}
