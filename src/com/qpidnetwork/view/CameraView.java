/**
 * Author: Martin Shum
 * This is the camera view which used below api 21.
 */

package com.qpidnetwork.view;

import java.util.List;

import com.qpidnetwork.framework.util.SystemUtil;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, PictureCallback{
	
	
	private Camera camera;
	private List<Size> supportedPreviewSizes;
	private Size optimalPreviewSize;
	private int viewHeight = 0;

	//private String capturedFileUrl;
	private PhotoCaptureCallback photoCaptureCallback;
	private OnCameraChangedListener onCameraChangedListener;
	
	private Point screenSize;
	private int frontCamera = -1;
	private int backCamera = -1;
	private static int currentCamera = - 1;
	
	
	public static interface OnCameraChangedListener{
		public void onCameraChanged(int cameraIndex);
	}
	public static interface PhotoCaptureCallback{
		public void onPhotoCaptured(byte[] data);
	}
	
	public CameraView(Context context){
		this(context, null);
	}
	
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		screenSize = new Point();
		screenSize.x = SystemUtil.getDisplayMetrics(this.getContext()).widthPixels;
		screenSize.y = SystemUtil.getDisplayMetrics(this.getContext()).heightPixels;
		this.getKeepScreenOn();
		this.getHolder().addCallback(this);
		
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			this.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		assignCamera();
		currentCamera = backCamera;
		
		try{
			if (currentCamera == -1){
				camera = Camera.open();
			}else{
				camera = Camera.open(currentCamera);
			}
			
		}catch(Exception e){
			
		}
		
		calculateOptimalPreviewSize();
		
	}
	
	public void setPhotoCaptureCallback(PhotoCaptureCallback photoCaptureCallback){
		this.photoCaptureCallback = photoCaptureCallback;
	}
	
	public void setOnCameraChangedListener(OnCameraChangedListener onCameraChangedListener){
		this.onCameraChangedListener = onCameraChangedListener;
	}
	
	public int getCurrentCamera(){
		return currentCamera;
	}
	
	public int getBackCamera(){
		return backCamera;
	}
	
	public int getFrontCamera(){
		return frontCamera;
	}
	
	public void setCamera(int cameraIndex){
		if (cameraIndex == -1 || 
				(cameraIndex != backCamera &&
				cameraIndex != frontCamera)) return;
		currentCamera = cameraIndex;
		releaseCamera();
		reconnectCamera();
		
		if (onCameraChangedListener != null) onCameraChangedListener.onCameraChanged(currentCamera);
		
	}
	
	public void swapCamera(){
		if (currentCamera == backCamera){
			setCamera(frontCamera);
		}else{
			setCamera(backCamera);
		}
	}
	
	
	
	public void startCameraPreview(){
		
		if (camera == null) return;
		
		Camera.Parameters param = camera.getParameters();
        param.setPreviewFrameRate(20);
        List<Integer> supportPreviewFrameRates = param.getSupportedPreviewFrameRates();
        if (supportPreviewFrameRates != null)
        	param.setPreviewFrameRate(supportPreviewFrameRates.get(supportPreviewFrameRates.size() - 1));
        
        if (param.getSupportedFocusModes().size() != 0){
        	for (int i = 0; i < param.getSupportedFocusModes().size(); i++){
        		if (param.getSupportedFocusModes().get(i).equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
        			param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        		}
        	}
        }
        
        param.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);

        try {
        	camera.setDisplayOrientation(90);
            camera.setParameters(param);
			camera.setPreviewDisplay(getHolder());
			camera.startPreview();
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
        
	}
	
	public void capture(){

		try{
			camera.takePicture(null, null, this);
		}catch(Exception e){
			if (photoCaptureCallback != null) photoCaptureCallback.onPhotoCaptured(null);
		}
		
	}
	
	public void reconnectCamera(){

		try {
			if (currentCamera == -1){
				camera = Camera.open();
			}else{
				camera = Camera.open(currentCamera);
			}
			
			calculateOptimalPreviewSize();
			startCameraPreview();
			this.setKeepScreenOn(false);
		} catch (Exception e) {
			Log.v("exception", e.toString());
		}
	}
	
	public void releaseCamera(){
		if (camera == null) return;
		try{
			camera.stopPreview();
			camera.release();
		}catch(Exception e){

		}
		
		this.setKeepScreenOn(false);
	}
	
	private void calculateOptimalPreviewSize(){
		if (camera != null) {
			supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
			optimalPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, 600, 600);
			float sizePercentage = (float)screenSize.x / (float)optimalPreviewSize.height;
			viewHeight = (int)((float)optimalPreviewSize.width * sizePercentage);
		}
		
		this.invalidate();
	}
	
	private void assignCamera(){
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
	        Camera.getCameraInfo(i, cameraInfo);
	        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	        	frontCamera = i;
	        }
	        
	        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
	        	backCamera = i;
	        }
	    }
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		if (photoCaptureCallback != null) photoCaptureCallback.onPhotoCaptured(data);
		//camera.startPreview();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		try{
			startCameraPreview();
		}catch(Exception e){
			
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		releaseCamera();
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.setMeasuredDimension(screenSize.x, viewHeight);
    }
	
	
	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }



}
