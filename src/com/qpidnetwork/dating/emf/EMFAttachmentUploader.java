package com.qpidnetwork.dating.emf;

import java.io.File;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.qpidnetwork.dating.R;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.request.OnEMFUploadAttachCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniEMF.UploadAttachType;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.view.ProgressImageHorizontalView;

/**
 * EMF模块
 * 上传附件控制器
 * @author Max.Chiu
 */
public class EMFAttachmentUploader implements OnClickListener {
	public interface EMFAttachmentUploaderCallback {
		public void OnUploadFinish(boolean isSuccess, String errno, String errmsg, String attachId);
		public void OnClickCancel(EMFAttachmentUploader uploader);
		public void OnClickWarning(EMFAttachmentUploader uploader);
	}
	
	private enum RequestFlag {
		REQUEST_FAIL,
		REQUEST_SUCCESS,
		REQUEST_UPLOADING,
	}
	
	private ProgressImageHorizontalView view;
	private Handler mHandler = null;
	private RequestFlag mRequestFlag;
	private EMFAttachmentUploaderCallback callback = null;
	private String localPath;
	private UploadAttachType attachType;
	private long mRequestId = -1;
	private String mErrno = "";
	private String mErrmsg = "";
	private String attachmentId = "";
	private boolean mIsCancel = false;
	
	public EMFAttachmentUploader() {
		mHandler = new Handler() {
			@Override
            public void handleMessage(Message msg) {
				RequestFlag flag = RequestFlag.values()[msg.what];
				switch (flag) {
				case REQUEST_SUCCESS: {
					mRequestId = -1;
					mRequestFlag = RequestFlag.REQUEST_SUCCESS;
				}break;
				case REQUEST_FAIL: {
					mRequestId = -1;
					mRequestFlag = RequestFlag.REQUEST_FAIL;
				}break;
				case REQUEST_UPLOADING: {
//					int progress = view.progressBar.getProgress();
					
					// 预留20%最后冲刺
					int ContentLength = -1;
					int SendLength = -1;
					if( mRequestId != -1 ) {
						ContentLength = RequestJni.GetUploadContentLength(mRequestId);
						SendLength = RequestJni.GetSendLength(mRequestId);
					}
					
					if( mRequestFlag == RequestFlag.REQUEST_UPLOADING ) {
						if( ContentLength > 0 && SendLength > 0 ) {
							//view.progressBar.setMax(ContentLength);
							
							view.progressBar.setInstantProgress((float)SendLength / (float)ContentLength);
							Log.v("progress", (float)SendLength / (float)ContentLength + "");
							//view.progressBar.setProgress(SendLength);
						}
//						// 预留20%最后冲刺
//						if( progress < (int) ContentLength ) {
//							progress += (view.progressBar.getMax() - progress) / 30;
//							view.progressBar.setProgress(++progress);
//						}
						mHandler.sendEmptyMessageDelayed(RequestFlag.REQUEST_UPLOADING.ordinal(), 100);
//					} else if( false/* progress != view.progressBar.getMax() */ ) {
//						// 最后冲刺
//						if( progress < view.progressBar.getMax() ) {
//							progress += view.progressBar.getMax() / 30;
//							view.progressBar.setProgress(progress);
//						}
//						mHandler.sendEmptyMessageDelayed(RequestFlag.REQUEST_UPLOADING.ordinal(), 100);
					} else {
						// 上传完成
						view.progressBar.setVisibility(View.GONE);		
						if( mRequestFlag == RequestFlag.REQUEST_FAIL ) {
							view.textView.setText("Upload fail");
							view.buttonCancel.setImageResource(R.drawable.ic_warning_amber_18dp);
						} else {
							view.textView.setText("Uploaded");
							view.buttonCancel.setImageResource(R.drawable.ic_close_white_18dp);
						}
					}
				}break;
				default:break;
				}
			}
		};
	}
	
	public boolean Upload(
			final ProgressImageHorizontalView view, 
			final UploadAttachType attachType,
			final String localPath,
			final EMFAttachmentUploaderCallback callback) {
		
		this.view = view;
		this.view.buttonCancel.setOnClickListener(this);
		
		this.callback = callback;
		this.localPath = localPath;
		this.attachType = attachType;
		
		return Reload();
	}
	
	public boolean Reload() {
		if( this.view == null || this.view.progressBar == null || this.view.imageView == null ) {
			return false;
		}
		
		if( this.localPath == null || this.localPath.length() == 0 ) {
			return false;
		}
		
		Bitmap bm = null;
		
		File filePhoto = new File(localPath);
		if( filePhoto.exists() && filePhoto.isFile() ) {
			bm = ImageUtil.decodeSampledBitmapFromFile(localPath, 300, 300);
		}
		
		if( bm != null ) {
			// 显示缩略图
			this.view.imageView.setImageBitmap(bm);
			this.view.imageView.setVisibility(View.VISIBLE);
		}
		
		File file = new File(localPath);
		if( file.exists() && file.isFile() ) {
			this.view.progressBar.setVisibility(View.VISIBLE);
			this.view.progressBar.setProgress(0);
			this.view.layoutTips.setVisibility(View.VISIBLE);
			this.view.textView.setText("Uploading");
			this.view.buttonCancel.setImageResource(R.drawable.ic_close_grey600_18dp);

			mHandler.removeMessages(RequestFlag.REQUEST_SUCCESS.ordinal());
			mHandler.removeMessages(RequestFlag.REQUEST_FAIL.ordinal());
			mRequestFlag = RequestFlag.REQUEST_UPLOADING;
			mHandler.sendEmptyMessageDelayed(RequestFlag.REQUEST_UPLOADING.ordinal(), 100);
			
			this.attachmentId = "";
			this.mErrno = "";
			this.mErrmsg = "";
			this.mIsCancel = false;
			
			// 开始上传
			mRequestId = RequestOperator.getInstance().UploadAttach(this.attachType, this.localPath, new OnEMFUploadAttachCallback() {
				
				@Override
				public void OnEMFUploadAttach(boolean isSuccess, String errno,
						String errmsg, String attachId) {
					// TODO Auto-generated method stub
					attachmentId = attachId;
					mErrno = errno;
					mErrmsg = errmsg;
					
					if( callback != null && !mIsCancel ) {
						callback.OnUploadFinish(isSuccess, errno, errmsg, attachId);
					}
					
					Message msg = Message.obtain();
					if( isSuccess ) {
						// 成功
						msg.what = RequestFlag.REQUEST_SUCCESS.ordinal();
					} else {
						// 失败
						msg.what = RequestFlag.REQUEST_FAIL.ordinal();
					}
					mHandler.sendMessage(msg);
				}
			});
			
		} else {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 停止上传
	 */
	public void StopUpload(){
		if( mRequestFlag == RequestFlag.REQUEST_UPLOADING && mRequestId != -1 ) {
			// 标记为已停止
			RequestJni.StopRequest(mRequestId);
			mRequestId = -1;
		}
	}
	
	/**
	 * 回收uploader
	 */
	public void onDestroy(){
		StopUpload();
		mHandler.removeMessages(RequestFlag.REQUEST_SUCCESS.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_FAIL.ordinal());
		mHandler.removeMessages(RequestFlag.REQUEST_UPLOADING.ordinal());
		view = null;
	}

	@Override
	public void onClick(View v) {
//		if( mRequestFlag != RequestFlag.REQUEST_FAIL) {
		if( view.progressBar.getVisibility() != View.GONE ) {
			StopUpload();
		} 
		
		if( callback != null ) {
			if( mRequestFlag == RequestFlag.REQUEST_FAIL && view.progressBar.getVisibility() == View.GONE ) {
				callback.OnClickWarning(this);
			} else {
				mIsCancel = true;
				callback.OnClickCancel(this);
			}

		}
	}
	
	public View GetView(){
		return view;
	}
	
	public String GetLocalPath() {
		return localPath;
	}
	
	public String GetAttachmentId() {
		return attachmentId;
	}
	
	public boolean IsUploadSuccess() {
		return mRequestFlag == RequestFlag.REQUEST_SUCCESS;
	}
	
	public String GetErrno() {
		return mErrno;
	}
	
	public String GetErrmsg() {
		return mErrmsg;
	}
}
