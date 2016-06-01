package com.qpidnetwork.dating.livechat;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.livechat.picture.PictureHelper;
import com.qpidnetwork.framework.base.BaseFragment;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.view.CameraView;
import com.qpidnetwork.view.MaterialProgressBar;

public class CameraViewFragment extends BaseFragment implements CameraView.PhotoCaptureCallback, CameraView.OnCameraChangedListener{
	
	private static String KEY_FRAGMENT_HEIGHT = "KEY_FRAGMENT_HEIGHT";
	
	private RelativeLayout cameraViewHolder;
	private CameraView cameraView;
	private ImageButton sendPhotoButton;
	private ImageButton swapCameraButton;
	private ImageButton expandCameraButton;
	private MaterialProgressBar progressBar;
	
	private int fragmentHeight;
	
	public static CameraViewFragment newInstance(int fregamntHeight) {
		CameraViewFragment newInstance = new CameraViewFragment();

	    Bundle args = new Bundle();
	    args.putInt(KEY_FRAGMENT_HEIGHT, fregamntHeight);
	    newInstance.setArguments(args);

	    return newInstance;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Bundle bundle = this.getArguments();
		if (bundle == null || !bundle.containsKey(KEY_FRAGMENT_HEIGHT)) throw new NullPointerException("Null argument.");
		fragmentHeight = bundle.getInt(KEY_FRAGMENT_HEIGHT);
		
		View view = inflater.inflate(R.layout.fragment_livechat_camera_layout, container, false);
		cameraViewHolder = (RelativeLayout) view.findViewById(R.id.cameraViewHolder);
		cameraView = (CameraView) view.findViewById(R.id.cameraView);
		sendPhotoButton = (ImageButton) view.findViewById(R.id.sendButton);
		swapCameraButton = (ImageButton) view.findViewById(R.id.swapCameraButton);
		expandCameraButton = (ImageButton) view.findViewById(R.id.expandCameraButton);
		progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
		
		expandCameraButton.setOnClickListener(this);
		swapCameraButton.setOnClickListener(this);
		sendPhotoButton.setOnClickListener(this);
		cameraViewHolder.getLayoutParams().height = fragmentHeight;
		cameraView.setPhotoCaptureCallback(this);
		cameraView.setOnCameraChangedListener(this);
		
		return view;
	}

	@Override
	public void onPhotoCaptured(byte[] data) {

		processPhoto(data);
	}
	
	private void processPhoto(byte[] data){
		
		if (data == null){
			enableButtons();
			return;
		}
		
		AsyncTask<Object, Void, Object> task = new AsyncTask<Object, Void, Object>(){

			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				Bitmap bitmap = (Bitmap)params[0];
				if (bitmap == null) 
					return null;
				
				float ratio = (float)bitmap.getHeight() / (float)cameraViewHolder.getWidth();
				int heightDifference = bitmap.getWidth() - (int)((float)cameraViewHolder.getHeight() * ratio);
				
				Bitmap rotateBitmap = null;
				if (heightDifference > 2) {
					float offset = (float)heightDifference / 2.0f;
					Bitmap tempBitmap = Bitmap.createBitmap(bitmap, (int)offset, 0, (int)((float)cameraViewHolder.getHeight() * ratio), bitmap.getHeight());
					if(bitmap != null){
						bitmap.recycle();
						bitmap = null;
					}
					rotateBitmap = ImageUtil.createRotatedBitmap(getActivity(), tempBitmap, (cameraView.getCurrentCamera() == cameraView.getFrontCamera()) ? -90 : 90);
					tempBitmap.recycle();
				}else{
					rotateBitmap = bitmap;
				}
				
				String fileUrl = FileCacheManager.getInstance().getPrivatePhotoTempSavePath() + PictureHelper.getPhotoFileName();
				boolean written = ImageUtil.writeBitmapToFile(rotateBitmap, fileUrl);
				rotateBitmap.recycle();
				
				return (written) ? fileUrl : null;

			}
			
			@Override
			protected void onPostExecute(Object fileUrl){
				super.onPostExecute(fileUrl);
				enableButtons();
				if (fileUrl == null) return;
				if(getActivity()!= null){
					((ChatActivity)getActivity()).sendPrivatePhoto((String)fileUrl);
				}
			}
			
		};
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (Build.VERSION.SDK_INT >= 11){
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bitmap);
		}else{
			task.execute(bitmap);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		
		switch (v.getId()){
		case R.id.sendButton:{
			dissableButtons();
			cameraView.capture();
			break;
		}
		case R.id.swapCameraButton:{
			cameraView.swapCamera();
			break;
		}
		case R.id.expandCameraButton:{
			cameraView.releaseCamera();
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			String takePhotoTempPath = FileCacheManager.getInstance().getPrivatePhotoTempSavePath() + PictureHelper.getPhotoFileName();
			((ChatActivity)getActivity()).setTempPicturePath(takePhotoTempPath);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(takePhotoTempPath)));
			if (cameraView.getCurrentCamera() != -1){
				//this is supposed to set which camera to use (front camera or back camera)
				//It works only on some devices, not all devices work with this.
				intent.putExtra("android.intent.extras.CAMERA_FACING", cameraView.getCurrentCamera());
			}
			
			getActivity().startActivityForResult(intent, ChatActivity.RESULT_LOAD_IMAGE_CAPTURE);
			break;
		}
		}
		
		
	}
	
	private void enableButtons(){
		cameraView.startCameraPreview();
		this.progressBar.setVisibility(View.GONE);
		this.sendPhotoButton.setEnabled(true);
		this.expandCameraButton.setEnabled(true);
		this.swapCameraButton.setEnabled(true);
	}
	
	private void dissableButtons(){
		this.progressBar.setVisibility(View.VISIBLE);
		this.sendPhotoButton.setEnabled(false);
		this.expandCameraButton.setEnabled(false);
		this.swapCameraButton.setEnabled(false);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    cameraView.reconnectCamera();
	}

	@Override
	public void onCameraChanged(int cameraIndex) {
		// TODO Auto-generated method stub
		if (cameraIndex == cameraView.getFrontCamera()){
			swapCameraButton.setImageResource(R.drawable.ic_camera_front_white_24dp);
		}
		
		if (cameraIndex == cameraView.getBackCamera()){
			swapCameraButton.setImageResource(R.drawable.ic_camera_rear_white_24dp);
		}
	}
}
