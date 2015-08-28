package com.qpidnetwork.request;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.qpidnetwork.dating.authorization.LoginManager;
import com.qpidnetwork.dating.authorization.LoginManager.OnLoginManagerCallback;
import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestEnum.Drink;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestEnum.Ethnicity;
import com.qpidnetwork.request.RequestEnum.Height;
import com.qpidnetwork.request.RequestEnum.Income;
import com.qpidnetwork.request.RequestEnum.Language;
import com.qpidnetwork.request.RequestEnum.Marry;
import com.qpidnetwork.request.RequestEnum.Profession;
import com.qpidnetwork.request.RequestEnum.Religion;
import com.qpidnetwork.request.RequestEnum.Smoke;
import com.qpidnetwork.request.RequestEnum.Weight;
import com.qpidnetwork.request.RequestJniAuthorization.Verify;
import com.qpidnetwork.request.RequestJniEMF.BlockReasonType;
import com.qpidnetwork.request.RequestJniEMF.MailType;
import com.qpidnetwork.request.RequestJniEMF.PrivatePhotoType;
import com.qpidnetwork.request.RequestJniEMF.ProgressType;
import com.qpidnetwork.request.RequestJniEMF.ReplyType;
import com.qpidnetwork.request.RequestJniEMF.SortType;
import com.qpidnetwork.request.RequestJniEMF.UploadAttachType;
import com.qpidnetwork.request.RequestJniLiveChat.PhotoModeType;
import com.qpidnetwork.request.RequestJniLiveChat.PhotoSizeType;
import com.qpidnetwork.request.RequestJniLiveChat.ToFlagType;
import com.qpidnetwork.request.RequestJniLiveChat.UseType;
import com.qpidnetwork.request.RequestJniLiveChat.VideoPhotoType;
import com.qpidnetwork.request.RequestJniLiveChat.VideoToFlagType;
import com.qpidnetwork.request.RequestJniLoveCall.ConfirmType;
import com.qpidnetwork.request.RequestJniOther.ActionType;
import com.qpidnetwork.request.RequestJniVideoShow.OrderByType;
import com.qpidnetwork.request.item.Coupon;
import com.qpidnetwork.request.item.EMFAdmirerListItem;
import com.qpidnetwork.request.item.EMFAdmirerViewerItem;
import com.qpidnetwork.request.item.EMFBlockListItem;
import com.qpidnetwork.request.item.EMFInboxListItem;
import com.qpidnetwork.request.item.EMFInboxMsgItem;
import com.qpidnetwork.request.item.EMFMsgTotalItem;
import com.qpidnetwork.request.item.EMFOutboxListItem;
import com.qpidnetwork.request.item.EMFOutboxMsgItem;
import com.qpidnetwork.request.item.EMFSendMsgErrorItem;
import com.qpidnetwork.request.item.EMFSendMsgItem;
import com.qpidnetwork.request.item.Gift;
import com.qpidnetwork.request.item.LCSendPhotoItem;
import com.qpidnetwork.request.item.LCVideoItem;
import com.qpidnetwork.request.item.Lady;
import com.qpidnetwork.request.item.LadyCall;
import com.qpidnetwork.request.item.LadyDetail;
import com.qpidnetwork.request.item.LadyMatch;
import com.qpidnetwork.request.item.LadyRecentContactItem;
import com.qpidnetwork.request.item.LadySignItem;
import com.qpidnetwork.request.item.LoginErrorItem;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.LoveCall;
import com.qpidnetwork.request.item.OtherEmotionConfigItem;
import com.qpidnetwork.request.item.OtherGetCountItem;
import com.qpidnetwork.request.item.OtherIntegralCheckItem;
import com.qpidnetwork.request.item.ProfileItem;
import com.qpidnetwork.request.item.QuickMatchLady;
import com.qpidnetwork.request.item.Record;
import com.qpidnetwork.request.item.RecordMutiple;
import com.qpidnetwork.request.item.TicketDetailItem;
import com.qpidnetwork.request.item.TicketListItem;
import com.qpidnetwork.request.item.VSPlayVideoItem;
import com.qpidnetwork.request.item.VSSavedVideoListItem;
import com.qpidnetwork.request.item.VSVideoDetailItem;
import com.qpidnetwork.request.item.VSVideoListItem;
import com.qpidnetwork.request.item.VSWatchedVideoListItem;

/**
 * 接口管理类
 * @author Max.Chiu
 * @see	逻辑处理包括
 * 		1.session错误自动重登陆
 */
public class RequestOperator {
//	private Context mContext = null;
	private Handler mHandler = null;
	private static RequestOperator gRequestOperator;
	
	/**
	 * 创建请求类实例
	 * 
	 * @param context
	 * @return 站点管理类实例
	 */
	public static RequestOperator newInstance(Context context) {
		if (gRequestOperator == null) {
			gRequestOperator = new RequestOperator(context);
		}
		return gRequestOperator;
	}
	
	public static RequestOperator getInstance() {
		return gRequestOperator;
	}

	/**
	 * 界面消息
	 */
	private class MessageCallbackItem {
		/**
		 * 
		 * @param errno				接口错误码
		 * @param errmsg			错误提示
		 * @param loginItem			登录正常返回
		 */
		public MessageCallbackItem(
				boolean isSuccess,
				String errno, 
				String errmsg,
				OnLoginManagerCallback callbackLogin
				) {
			this.isSuccess = isSuccess;
			this.errno = errno;
//			this.errmsg = errmsg;
			this.callbackLogin = callbackLogin;
		}
		public String errno;
//		public String errmsg;
		public boolean isSuccess;
		public OnLoginManagerCallback callbackLogin;
	}
	
	private RequestOperator(Context context) {
//		mContext = context;
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 根据错误码处理
				MessageCallbackItem obj = (MessageCallbackItem) msg.obj;
				if( obj.isSuccess ) {
					// 去除回调
					LoginManager.getInstance().RemoveListenner(obj.callbackLogin);
				} else {
					switch (obj.errno) {
					case RequestErrorCode.MBCE0003:{
						// 服务器未登录
						// 注销本地
						LoginManager.getInstance().Logout();
						
						// 重新登录
						LoginManager.getInstance().AddListenner(obj.callbackLogin);
						LoginManager.getInstance().AutoLogin();
					}break;
					default: {
						// 去除回调
						LoginManager.getInstance().RemoveListenner(obj.callbackLogin);
					}break;
					}
					
				}
			}
		};
	}
	
	/**
	 * 错误代码处理
	 * @param isSuccess
	 * @param errno
	 * @param errmsg
	 */
	public boolean HandleRequestCallback(
			boolean isSuccess, 
			String errno, 
			String errmsg, 
			OnLoginManagerCallback callbackLogin
			) {
		// 判断错误码是否可以处理
		boolean bFlag = false;
		if( isSuccess ) {

		} else {
			switch (errno) {
			case RequestErrorCode.MBCE0003:{
				// 未登录
				bFlag = true;
			}break;
			default: {
			}break;
			}
			
		}
		
		// 发送消息到主线程处理
		Message msg = Message.obtain();
		MessageCallbackItem obj = new MessageCallbackItem(isSuccess, errno, errmsg, callbackLogin);
		msg.obj = obj;
		mHandler.sendMessage(msg);
		
		return bFlag;
	}
	
	
	
	/**************************************************************************************
	 * 认证模块
	 **************************************************************************************/	
	
    /**
     * 2.7.手机短信认证
     * @param verify_code		验证码
     * @param v_type			验证类型,参考枚举<Verify>
     * @param callback
     * @return					请求唯一标识
     */
    public long VerifySms(
    		final String verify_code, 
    		final Verify v_type, 
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniAuthorization.VerifySms(verify_code, v_type, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniAuthorization.VerifySms(verify_code, v_type, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理VerifySms
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    /**
     * 2.9.固定电话短信认证
     * @param verify_code		验证码
     * @param callback
     * @return					请求唯一标识
     */
    public long VerifyFixedPhone(
    		final String verify_code, 
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniAuthorization.VerifyFixedPhone(verify_code, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniAuthorization.VerifyFixedPhone(verify_code, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
	
	/**************************************************************************************
	 * 认证模块
	 **************************************************************************************/	
	
	
	
	/**************************************************************************************
	 * Setting模块
	 **************************************************************************************/	
	
	/**
	 * 3.1.修改密码
	 * @param oldPassword	新密码
	 * @param newPassword	旧密码
	 * @param callback
	 * @return				请求唯一标识
	 */
	public long ChangePassword(
			final String oldPassword, 
			final String newPassword, 
			final OnRequestCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniSetting.ChangePassword(oldPassword, newPassword, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniSetting.ChangePassword(oldPassword, newPassword, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**************************************************************************************
	 * Setting模块end
	 **************************************************************************************/	
	
	
	/**************************************************************************************
	 * MyProfile模块
	 **************************************************************************************/	
	
	/**
	 * 2.1.查询个人信息
	 * @return				请求唯一标识
	 */
	public long GetMyProfile(final OnGetMyProfileCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniProfile.GetMyProfile(callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnGetMyProfile(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniProfile.GetMyProfile(new OnGetMyProfileCallback() {

			@Override
			public void OnGetMyProfile(boolean isSuccess, String errno,
					String errmsg, ProfileItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnGetMyProfile(isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * 2.2.修改个人信息
	 * @param weight		体重
	 * @param height		身高
	 * @param language		语言
	 * @param ethnicity		人种
	 * @param religion		宗教
	 * @param education		教育程度
	 * @param profession	职业
	 * @param income		收入情况
	 * @param children		子女状况
	 * @param smoke			吸烟情况
	 * @param drink			喝酒情况
	 * @param resume		drink
	 * @return				请求唯一标识
	 */
	public long UpdateProfile(
			final Weight weight, 
			final Height height, 
			final Language language, 
			final Ethnicity ethnicity, 
			final Religion religion, 
			final Education education, 
			final Profession profession, 
			final Income income, 
			final Children children, 
			final Smoke smoke, 
			final Drink drink, 
			final String resume, 
			final String[] interest, 
			final OnUpdateMyProfileCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniProfile.UpdateProfile(
							weight, 
							height, 
							language, 
							ethnicity, 
							religion, 
							education, 
							profession, 
							income, 
							children, 
							smoke, 
							drink, 
							resume, 
							interest, 
							callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnUpdateMyProfile(isSuccess, errno, errmsg, false);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniProfile.UpdateProfile(
    			weight, 
				height, 
				language, 
				ethnicity, 
				religion, 
				education, 
				profession, 
				income, 
				children, 
				smoke, 
				drink, 
				resume, 
				interest, 
				new OnUpdateMyProfileCallback() {

			@Override
			public void OnUpdateMyProfile(boolean isSuccess, String errno,
					String errmsg, boolean rsModified) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnUpdateMyProfile(isSuccess, errno, errmsg, false);
				}
			}
		});
	}
	
	/**
	 * 2.3.开始编辑简介触发计时
	 * @return				请求唯一标识
	 */
	public long StartEditResume(final OnRequestCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniProfile.StartEditResume(callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniProfile.StartEditResume(new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**
	 * 2.4.保存联系电话
	 * @param telephone			电话号码
	 * @param telephone_cc		国家区号,参考枚举 <RequestEnum.Country>
	 * @param landline			固定电话号码
	 * @param landline_cc		固定电话号码国家区号,参考枚举 <RequestEnum.Country>
	 * @param landline_ac		固话区号
	 * @return					请求唯一标识
	 */
	public long SaveContact(
			final String telephone, 
			final Country telephone_cc, 
			final String landline, 
			final Country landline_cc, 
			final String landline_ac, 
			final OnRequestCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniProfile.SaveContact(
							telephone, 
							telephone_cc, 
							landline, 
							landline_cc, 
							landline_ac, 
							callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniProfile.SaveContact(
    							telephone, 
								telephone_cc, 
								landline, 
								landline_cc, 
								landline_ac, 
								new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**
	 * 2.5.上传头像
	 * @param fileName			文件名
	 * @return					请求唯一标识
	 */
	public long UploadHeaderPhoto(final String fileName, final OnRequestCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniProfile.UploadHeaderPhoto(fileName, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniProfile.UploadHeaderPhoto(fileName, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**************************************************************************************
	 * MyProfile模块end
	 **************************************************************************************/	
    
	
	/**************************************************************************************
	 * 女士信息模块
	 **************************************************************************************/
	
	/**
	 * 4.1.获取匹配女士条件
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long QueryLadyMatch(
			final OnQueryLadyMatchCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.QueryLadyMatch(callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryLadyMatch(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.QueryLadyMatch(new OnQueryLadyMatchCallback() {
			@Override
			public void OnQueryLadyMatch(boolean isSuccess, String errno,
					String errmsg, LadyMatch item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryLadyMatch(isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * 4.2.保存匹配女士条件
	 * @param ageRangeFrom		起始年龄
	 * @param ageRangeTo		结束年龄
	 * @param marry				婚姻状况,参考枚举 <RequestEnum.Marry>
	 * @param children			子女状况,参考枚举 <RequestEnum.Children>
	 * @param education			教育程度,参考枚举 <RequestEnum.Education>
	 * @param callback
	 * @return					请求唯一标识
	 */
	public long SaveLadyMatch(
			final int ageRangeFrom, 
			final int ageRangeTo, 
			final Children children, 
			final Marry marry, 
			final Education education, 
			final OnRequestCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.SaveLadyMatch(
								ageRangeFrom, 
								ageRangeTo, 
								children, 
								marry, 
								education, 
								callback
								);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.SaveLadyMatch(
								ageRangeFrom, 
								ageRangeTo, 
								children, 
								marry, 
								education, 
								new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
    /**
     * 4.3.条件查询女士列表
     * @param pageIndex			当前页数
     * @param pageSize			每页行数
     * @param searchType		查询类型,参考枚举<SearchType>
     * @param womanId			女士ID
     * @param isOnline			是否在线,参考枚举<OnlineType>
     * @param ageRangeFrom		起始年龄
     * @param ageRangeTo		结束年龄
     * @param country			国家
     * @param callback			
     * @return					请求唯一标识
     */
    public long QueryLadyList(
    		final int pageIndex, 
    		final int pageSize, 
    		final RequestJniLady.SearchType searchType, 
    		final String womanId, 
    		final RequestJniLady.OnlineType isOnline, 
    		final int ageRangeFrom, 
    		final int ageRangeTo, 
    		final String country, 
    		final RequestJniLady.OrderType orderType,
    		final String deviceId,
    		final OnQueryLadyListCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.QueryLadyList(
								pageIndex, 
					    		pageSize, 
					    		searchType, 
					    		womanId, 
					    		isOnline, 
					    		ageRangeFrom, 
					    		ageRangeTo, 
					    		country, 
					    		orderType,
					    		deviceId,
								callback
								);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryLadyList(isSuccess, errno, errmsg, null, 0);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.QueryLadyList(								
				    			pageIndex, 
					    		pageSize, 
					    		searchType, 
					    		womanId, 
					    		isOnline, 
					    		ageRangeFrom, 
					    		ageRangeTo, 
					    		country, 
					    		orderType,
					    		deviceId,
								new OnQueryLadyListCallback() {

			@Override
			public void OnQueryLadyList(boolean isSuccess, String errno,
					String errmsg, Lady[] ladyList, int totalCount) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryLadyList(isSuccess, errno, errmsg, ladyList, totalCount);
				}
			}
		});
    }
    
    
    /**
     * 4.4.查询女士详细信息
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    public long QueryLadyDetail(
    		final String womanId, 
    		final OnQueryLadyDetailCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.QueryLadyDetail(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryLadyDetail(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.QueryLadyDetail(womanId, new OnQueryLadyDetailCallback() {

			@Override
			public void OnQueryLadyDetail(boolean isSuccess, String errno,
					String errmsg, LadyDetail item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryLadyDetail(isSuccess, errno, errmsg, item);
				}
			}
		});
    }
    
    
    /**
     * 4.5.收藏女士
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    public long AddFavouritesLady(
    		final String womanId, 
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.AddFavouritesLady(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.AddFavouritesLady(womanId, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    
    /**
     * 4.6.删除收藏女士
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    public long RemoveFavouritesLady(
    		final String womanId, 
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.RemoveFavouritesLady(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.RemoveFavouritesLady(womanId, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    
    /**
     * 4.7.获取女士Direct Call TokenID
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    public long QueryLadyCall(
    		final String womanId, 
    		final OnQueryLadyCallCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.QueryLadyCall(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryLadyCall(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.QueryLadyCall(womanId, new OnQueryLadyCallCallback() {

			@Override
			public void OnQueryLadyCall(boolean isSuccess, String errno,
					String errmsg, LadyCall item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryLadyCall(isSuccess, errno, errmsg, item);
				}
			}
		});
    }
    
    /**
     * 获取最近联系人列表（ver3.0起）
     * @param callback
     * @return					请求唯一标识
     */
    public long RecentContact(final OnLadyRecentContactListCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.RecentContact(callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLadyRecentContactList(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.RecentContact(new OnLadyRecentContactListCallback() {
			@Override
			public void OnLadyRecentContactList(boolean isSuccess,
					String errno, String errmsg,
					LadyRecentContactItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLadyRecentContactList(isSuccess, errno, errmsg, listArray);
				}
			}
		});
    }
    
    /**
     * 删除最近联系人（ver3.0.3起）
     * @param womanIds 删除女士Id数组
     * @param callback
     * @return					请求唯一标识
     */
    public long RemoveContactList(final String[] womanIds, final OnRequestCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.RemoveContactList(womanIds, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.RemoveContactList(womanIds, new OnRequestCallback() {
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    /**
     * 查询女士标签列表（ver3.0起）
     * @param womanId			女士ID
     * @param callback
     * @return					请求唯一标识
     */
    public long SignList(
    		final String womanId, 
    		final OnLadySignListCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.SignList(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLadySignList(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.SignList(womanId, new OnLadySignListCallback() {
			@Override
			public void OnLadySignList(boolean isSuccess, String errno,
					String errmsg, LadySignItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLadySignList(isSuccess, errno, errmsg, listArray);
				}
			}
		});
    }
    
    /**
     * 提交女士标签（ver3.0起）
     * @param womanId			女士ID
     * @param signIdArray		选中的标签ID列表
     * @param callback
     * @return					请求唯一标识
     */
    public long UploadSign(
    		final String womanId, 
    		final String[] signIdArray, 
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLady.UploadSign(womanId, signIdArray, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLady.UploadSign(womanId, signIdArray, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
	/**************************************************************************************
	 * 女士信息模块end
	 **************************************************************************************/
	
	
	/**************************************************************************************
	 * LiveChat模块
	 **************************************************************************************/
	/**
	 * 5.1.查询是否符合试聊条件
	 * @param womanId			女士ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long CheckCoupon(
			final String womanId, 
			final OnCheckCouponCallCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.CheckCoupon(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnCheckCoupon(-1, isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.CheckCoupon(womanId, new OnCheckCouponCallCallback() {
			@Override
			public void OnCheckCoupon(long requestId, boolean isSuccess,
					String errno, String errmsg, Coupon item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnCheckCoupon(requestId, isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * 5.2.使用试聊券
	 * @param womanId			女士ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long UseCoupon(
			final String womanId, 
			final OnLCUseCouponCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.UseCoupon(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLCUseCoupon(-1, isSuccess, errno, errmsg, "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.UseCoupon(womanId, new OnLCUseCouponCallback() {
			@Override
			public void OnLCUseCoupon(long requestId, boolean isSuccess,
					String errno, String errmsg, String userId) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLCUseCoupon(requestId, isSuccess, errno, errmsg, userId);
				}
			}
		});
	}
	
	/**
	 * 5.3.获取虚拟礼物列表
	 * @param sessionId			登录成功返回的sessionid
	 * @param userId			登录成功返回的manid
	 * @param callback	
	 * @return					请求唯一标识
	 */
	public long QueryChatVirtualGift(
			final String sessionId, 
			final String userId, 
			final OnQueryChatVirtualGiftCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.QueryChatVirtualGift(sessionId, userId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryChatVirtualGift(isSuccess, errno, errmsg, null, 0, "", "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.QueryChatVirtualGift(sessionId, userId, new OnQueryChatVirtualGiftCallback() {

			@Override
			public void OnQueryChatVirtualGift(boolean isSuccess, String errno,
					String errmsg, Gift[] list, int totalCount, String path,
					String version) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryChatVirtualGift(isSuccess, errno, errmsg, list, totalCount, path, version);
				}
			}
		});
	}
	
	/**
	 * 5.4.查询聊天记录
	 * @param inviteId			邀请ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long QueryChatRecord(
			final String inviteId, 
			final OnQueryChatRecordCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.QueryChatRecord(inviteId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryChatRecord(isSuccess, errno, errmsg, 0, null, "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.QueryChatRecord(inviteId, new OnQueryChatRecordCallback() {

			@Override
			public void OnQueryChatRecord(boolean isSuccess, String errno,
					String errmsg, int dbTime, Record[] recordList, String inviteId) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryChatRecord(isSuccess, errno, errmsg, dbTime, recordList, inviteId);
				}
			}
		});
	}
	
	/**
	 * 5.5.批量查询聊天记录
	 * @param inviteId			邀请ID数组
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long QueryChatRecordMutiple(
			final String[] inviteIds, 
			final OnQueryChatRecordMutipleCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				// TODO Auto-generated method stub
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.QueryChatRecordMutiple(inviteIds, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryChatRecordMutiple(isSuccess, errno, errmsg, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.QueryChatRecordMutiple(inviteIds, new OnQueryChatRecordMutipleCallback() {

			@Override
			public void OnQueryChatRecordMutiple(boolean isSuccess,
					String errno, String errmsg,
					int dbTime, RecordMutiple[] recordMutipleList) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryChatRecordMutiple(isSuccess, errno, errmsg, dbTime, recordMutipleList);
				}
			}
		});
	}
	
	/**
	 * 发送私密照片
	 * @param targetId	接收方ID
	 * @param inviteId	邀请ID
	 * @param filePath	待发送的文件路径	
	 * @return
	 */
	public long SendPhoto(
			final String targetId, 
			final String inviteId, 
			final String userId,
			final String sid,
			final String filePath, 
			final OnLCSendPhotoCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.SendPhoto(targetId, inviteId, userId, sid, filePath, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLCSendPhoto(-1, isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.SendPhoto(targetId, inviteId, userId, sid, filePath, new OnLCSendPhotoCallback() {

			@Override
			public void OnLCSendPhoto(long requestId, boolean isSuccess,
					String errno, String errmsg, LCSendPhotoItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLCSendPhoto(requestId, isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * 付费获取私密照片
	 * @param targetId	接收方ID
	 * @param inviteId	邀请ID
	 * @param photoId	照片ID
	 * @return
	 */
	public long PhotoFee(
			final String targetId, 
			final String inviteId, 
			final String userId,
			final String sid,
			final String photoId, 
			final OnLCPhotoFeeCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.PhotoFee(targetId, inviteId, userId, sid, photoId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLCPhotoFee(-1, isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.PhotoFee(targetId, inviteId, userId, sid, photoId, new OnLCPhotoFeeCallback() {

			@Override
			public void OnLCPhotoFee(long requestId, boolean isSuccess,
					String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLCPhotoFee(requestId, isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**
	 * 获取照片
	 * @param toFlag	获取类型
	 * @param targetId	照片所有者ID
	 * @param photoId	照片ID
	 * @param sizeType	照片尺寸
	 * @param modeType	照片类型
	 * @param filePath	照片文件路径
	 * @return
	 */
	public long GetPhoto(
			final ToFlagType toFlag, 
			final String targetId, 
			final String userId, 
			final String sid,
			final String photoId, 
			final PhotoSizeType sizeType, 
			final PhotoModeType modeType, 
			final String filePath, 
			final OnLCGetPhotoCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.GetPhoto(toFlag, targetId, userId, sid, photoId, sizeType, modeType, filePath, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLCGetPhoto(-1, isSuccess, errno, errmsg, "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.GetPhoto(toFlag, targetId, userId, sid, photoId, sizeType, modeType, filePath, new OnLCGetPhotoCallback() {

			@Override
			public void OnLCGetPhoto(long requestId, boolean isSuccess,
					String errno, String errmsg, String filePath) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLCGetPhoto(requestId, isSuccess, errno, errmsg, filePath);
				}
			}
		});
	}
	
	/**
	 * 上传语音文件
	 * @param voiceCode		语音验证码
	 * @param inviteId		邀请ID
	 * @param mineId		自己的用户ID
	 * @param isMan			是否男士
	 * @param userId		对方的用户ID
	 * @param siteType		站点ID		
	 * @param fileType		文件类型(mp3, aac...)
	 * @param voiceLength	语音时长
	 * @param filePath		语音文件路径
	 * @param callback
	 * @return
	 */
	public long UploadVoice(
			final String voiceCode, 
			final String inviteId, 
			final String mineId, 
			final boolean isMan, 
			final String userId, 
			final int siteType, 
			final String fileType, 
			final int voiceLength, 
			final String filePath, 
			final OnLCUploadVoiceCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.UploadVoice(
							voiceCode, 
							inviteId, 
							mineId, 
							isMan, 
							userId, 
							siteType, 
							fileType, 
							voiceLength, 
							filePath, 
							callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLCUploadVoice(-1, isSuccess, errno, errmsg, "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.UploadVoice(
							voiceCode, 
							inviteId, 
							mineId, 
							isMan, 
							userId, 
							siteType, 
							fileType, 
							voiceLength, 
							filePath,
							new OnLCUploadVoiceCallback() {

			@Override
			public void OnLCUploadVoice(long requestId, boolean isSuccess,
					String errno, String errmsg, String voiceId) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLCUploadVoice(requestId, isSuccess, errno, errmsg, voiceId);
				}
			}
		});
	}
	
	/**
	 * 下载语音文件
	 * @param voiceId	语音ID
	 * @param siteType	站点ID
	 * @param filePath	文件路径
	 * @param callback
	 * @return
	 */
	public long PlayVoice(
			final String voiceId, 
			final int siteType, 
			final String filePath, 
			final OnLCPlayVoiceCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.PlayVoice(voiceId, siteType, filePath, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLCPlayVoice(-1, isSuccess, errno, errmsg, "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.PlayVoice(voiceId, siteType, filePath, new OnLCPlayVoiceCallback() {

			@Override
			public void OnLCPlayVoice(long requestId, boolean isSuccess,
					String errno, String errmsg, String filePath) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLCPlayVoice(requestId, isSuccess, errno, errmsg, filePath);
				}
			}
		});
	}

	/**
	 * 6.11.发送虚拟礼物
	 * @param womanId		女士ID
	 * @param vg_id			虚拟礼物ID
	 * @param device_id		设备唯一标识
	 * @param chat_id		livechat邀请ID或EMF邮件ID
	 * @param use_type		模块类型<UseType>
	 * @param user_sid		登录成功返回的sessionid
	 * @param user_id		登录成功返回的manid
	 * @param callback
	 * @return				请求唯一Id
	 */
	public long SendGift(
			final String womanId, 
			final String vg_id, 
			final String device_id, 
			final String chat_id, 
			final UseType use_type, 
			final String user_sid,
			final String user_id,
			final OnRequestCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.SendGift(
							womanId, 
							vg_id, 
							device_id, 
							chat_id, 
							use_type, 
							user_sid,
							user_id,
							callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.SendGift(
							womanId, 
							vg_id, 
							device_id, 
							chat_id, 
							use_type, 
							user_sid,
							user_id, 
							new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**
	 * 6.12.获取最近已看微视频列表（http post）（New）
	 * @param womanId			女士ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long QueryRecentVideo(
			final String user_sid,
			final String user_id,
			final String womanId, 
			final OnQueryRecentVideoListCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.QueryRecentVideo(user_sid, user_id, womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryRecentVideoList(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.QueryRecentVideo(user_sid, user_id, womanId, new OnQueryRecentVideoListCallback() {
			@Override
			public void OnQueryRecentVideoList(boolean isSuccess,
					String errno, String errmsg, LCVideoItem[] itemList) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryRecentVideoList(isSuccess, errno, errmsg, itemList);
				}
			}
		});
	}
	
	/**
	 * 6.13.获取微视频图片（http get）（New）
	 * @param womanId			女士ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long GetVideoPhoto(
			final String user_sid,
			final String user_id,
			final String womanId, 
			final String videoid,
			final VideoPhotoType type,
			final String filePath,
			final OnRequestFileCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.GetVideoPhoto(user_sid, user_id, womanId, videoid, type, filePath, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequestFile(-1, isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.GetVideoPhoto(user_sid, user_id, womanId, videoid, type, filePath, new OnRequestFileCallback() {
			@Override
			public void OnRequestFile(long requestId, boolean isSuccess,
					String errno, String errmsg, String filePath) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequestFile(requestId, isSuccess, errno, errmsg, filePath);
				}
			}
		});
	}
	
	/**
	 * 6.14.获取微视频文件URL（http post）（New）
	 * @param womanId			女士ID
	 * @param callback			
	 * @return					请求唯一标识
	 */
	public long GetVideo(
			final String user_sid,
			final String user_id,
			final String womanId, 
			final String videoid,
			final String inviteid,
			final VideoToFlagType toflag,
			final String sendid,
			final OnGetVideoCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniLiveChat.GetVideo(user_sid, user_id, womanId, videoid, inviteid, toflag, sendid, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnLCGetVideo(-1, isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLiveChat.GetVideo(user_sid, user_id, womanId, videoid, inviteid, toflag, sendid, new OnGetVideoCallback() {
			@Override
			public void OnLCGetVideo(long requestId, boolean isSuccess,
					String errno, String errmsg, String url) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnLCGetVideo(requestId, isSuccess, errno, errmsg, url);
				}
			}
		});
	}
	
	/**************************************************************************************
	 * LiveChat模块end
	 **************************************************************************************/
	
	/**************************************************************************************
	 * EMF模块
	 **************************************************************************************/	
	/**
	 * InboxList（查询收件箱列表：/emf/inboxlist）
	 * @param pageIndex
	 * @param pageSize
	 * @param sortType
	 * @param womanId
	 * @param callback
	 * @return -1 fails, else success
	 */
	public long InboxList(
			final int pageIndex, 
			final int pageSize, 
			final SortType sortType, 
			final String womanId, 
			final OnEMFInboxListCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.InboxList(pageIndex, pageSize, sortType, womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFInboxList(isSuccess, errno, errmsg, 0, 0, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.InboxList(pageIndex, pageSize, sortType, womanId, 
    			new OnEMFInboxListCallback() {
			@Override
			public void OnEMFInboxList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					EMFInboxListItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFInboxList(isSuccess, errno, errmsg, 0, 0, 0, listArray);
				}
			}
		});
    }
	
    /**
     * InboxMsg（查询已收邮件详细：/emf/inboxmsg）
     * @param messageId
     * @param callback
     * @return -1 fails, else success
     */
    public long InboxMsg(final String messageId, final OnEMFInboxMsgCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.InboxMsg(messageId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFInboxMsg(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.InboxMsg(messageId, new OnEMFInboxMsgCallback() {
			@Override
			public void OnEMFInboxMsg(boolean isSuccess, String errno,
					String errmsg, EMFInboxMsgItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFInboxMsg(isSuccess, errno, errmsg, item);
				}
			}
		});
    }
    
    /**
     * OutboxList（查询发件箱列表：/emf/outboxlist）
     * @param pageIndex
     * @param pageSize
     * @param womanId
     * @param progressType
     * @param callback
     * @return -1 fails, else success
     */
    public long OutboxList(
    		final int pageIndex, 
    		final int pageSize, 
    		final String womanId, 
    		final ProgressType progressType, 
    		final OnEMFOutboxListCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestOperator.getInstance().OutboxList(pageIndex, pageSize, womanId, progressType, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFOutboxList(isSuccess, errno, errmsg, 0, 0	, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.OutboxList(pageIndex, pageSize, womanId, progressType, new OnEMFOutboxListCallback() {
			@Override
			public void OnEMFOutboxList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					EMFOutboxListItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFOutboxList(isSuccess, errno, errmsg, pageIndex, pageSize, dataCount, listArray);
				}
			}
		});
    }
    
    /**
     * OutboxMsg（查询已发邮件详细：/emf/outboxmsg）
     * @param messageId
     * @param callback
     * @return -1 fails, else success
     */
	public long OutboxMsg(
			final String messageId, 
			final OnEMFOutboxMsgCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.OutboxMsg(messageId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFOutboxMsg(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.OutboxMsg(messageId, new OnEMFOutboxMsgCallback() {
			@Override
			public void OnEMFOutboxMsg(boolean isSuccess, String errno,
					String errmsg, EMFOutboxMsgItem item) {
				// TODO Auto-generated method stub
				
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFOutboxMsg(isSuccess, errno, errmsg, item);
				}

			}
		});
    }
    
    
    /**
     * MsgTotal（查询收件箱某状态邮件数量：/emf/msgtotal）
     * @param sortType
     * @param callback
     * @return -1 fails, else success
     */
    public long MsgTotal(
    		final SortType sortType, 
    		final OnEMFMsgTotalCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.MsgTotal(sortType, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFMsgTotal(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.MsgTotal(sortType, new OnEMFMsgTotalCallback() {
			@Override
			public void OnEMFMsgTotal(boolean isSuccess, String errno,
					String errmsg, EMFMsgTotalItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFMsgTotal(isSuccess, errno, errmsg, item);
				}
			}
		});
    }
    
    /**
     * SendMsg（发送邮件：/emf/sendmsg）
     * @param womanid
     * @param body
     * @param useIntegral
     * @param gifts
     * @param attachs
     * @return -1 fails, else success
     */
    public long SendMsg(
    		final String womanid, 
    		final String body, 
    		final boolean useIntegral, 
    		final ReplyType replyType, 
    		final String mtab, 
    		final String[] gifts, 
    		final String[] attachs, 
    		final OnEMFSendMsgCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.SendMsg(womanid, body, useIntegral, replyType, mtab, gifts, attachs, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFSendMsg(isSuccess, errno, errmsg, null, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.SendMsg(womanid, body, useIntegral, replyType, mtab, gifts, attachs, new OnEMFSendMsgCallback() {
			@Override
			public void OnEMFSendMsg(boolean isSuccess, String errno,
					String errmsg, EMFSendMsgItem item,
					EMFSendMsgErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFSendMsg(isSuccess, errno, errmsg, item, errItem);
				}
			}
		});
    }
    
    /**
     * UploadImage（追加邮件附件：/emf/uploadimage）
     * @param messageId
     * @param filename
     * @return -1 fails, else success
     */
    public long UploadImage(
    		final String messageId, 
    		final String[] filename, 
    		final OnEMFUploadImageCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.UploadImage(messageId, filename, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFUploadImage(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.UploadImage(messageId, filename, new OnEMFUploadImageCallback() {
			@Override
			public void OnEMFUploadImage(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFUploadImage(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    /**
     * UploadAttach（上传邮件附件：/emf/uploadattach）
     * @param attachType
     * @param filePath
     * @param callback
     * @return -1 fails, else success
     */
    public long UploadAttach(
    		final UploadAttachType attachType, 
    		final String filePath, 
    		final OnEMFUploadAttachCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.UploadAttach(attachType, filePath, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFUploadAttach(isSuccess, errno, errmsg, "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.UploadAttach(attachType, filePath, new OnEMFUploadAttachCallback() {
			@Override
			public void OnEMFUploadAttach(boolean isSuccess, String errno,
					String errmsg, String attachId) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFUploadAttach(isSuccess, errno, errmsg, attachId);
				}
			}
		});
    }
    
    /**
     * DeleteMsg（删除邮件：/emf/deletemsg）
     * @param messageId
     * @param mailType
     * @return -1 fails, else success
     */
    public long DeleteMsg(
    		final String messageId, 
    		final MailType mailType, 
    		final OnEMFDeleteMsgCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.DeleteMsg(messageId, mailType, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFDeleteMsg(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.DeleteMsg(messageId, mailType, new OnEMFDeleteMsgCallback() {
			@Override
			public void OnEMFDeleteMsg(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFDeleteMsg(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    
    /**
     * AdmirerList（查询意向信收件箱列表：/emf/admirerlist）
     * @param pageIndex
     * @param pageSize
     * @param sortType
     * @param womanid
     * @return -1 fails, else success
     */
    public long AdmirerList(
    		final int pageIndex, 
    		final int pageSize, 
    		final SortType sortType, 
    		final String womanid, 
    		final OnEMFAdmirerListCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.AdmirerList(pageIndex, pageSize, sortType, womanid, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFAdmirerList(isSuccess, errno, errmsg, 0, 0, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.AdmirerList(pageIndex, pageSize, sortType, womanid, new OnEMFAdmirerListCallback() {
			@Override
			public void OnEMFAdmirerList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					EMFAdmirerListItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFAdmirerList(isSuccess, errno, errmsg, pageIndex, pageSize, dataCount, listArray);
				}
			}
		});
    }
    
    /**
     * AdmirerViewer（查询意向信详细信息：/emf/admirerviewer）
     * @param messageId
     * @return -1 fails, else success
     */
    public long AdmirerViewer(
    		final String messageId, 
    		final OnEMFAdmirerViewerCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.AdmirerViewer(messageId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFAdmirerViewer(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.AdmirerViewer(messageId, new OnEMFAdmirerViewerCallback() {
			@Override
			public void OnEMFAdmirerViewer(boolean isSuccess, String errno,
					String errmsg, EMFAdmirerViewerItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFAdmirerViewer(isSuccess, errno, errmsg, item);
				}
			}
		});
    }
    
    /**
     * BlockList（查询黑名单列表：/emf/blocklist）
     * @param pageIndex
     * @param pageSize
     * @param womanid
     * @return -1 fails, else success
     */
    public long BlockList(
    		final int pageIndex, 
    		final int pageSize, 
    		final String womanid, 
    		final OnEMFBlockListCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.BlockList(pageIndex, pageSize, womanid, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFBlockList(isSuccess, errno, errmsg, 0, 0, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.BlockList(pageIndex, pageSize, womanid, new OnEMFBlockListCallback() {
			@Override
			public void OnEMFBlockList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					EMFBlockListItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFBlockList(isSuccess, errno, errmsg, pageIndex, pageSize, dataCount, listArray);
				}
			}
		});
    }

    /***
     * Block（添加黑名单：/emf/block）
     * @param womanid
     * @param blockReasonType
     * @return -1 fails, else success 
     */
    public long Block(
    		final String womanid, 
    		final BlockReasonType blockReasonType, 
    		final OnEMFBlockCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.Block(womanid, blockReasonType, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFBlock(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.Block(womanid, blockReasonType, new OnEMFBlockCallback() {
			@Override
			public void OnEMFBlock(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFBlock(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    /**
     * Unblock（删除黑名单：/emf/unblock）
     * @param womanid
     * @return -1 fails, else success
     */
    public long Unblock(
    		final String[] womanid, 
    		final OnEMFUnblockCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.Unblock(womanid, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFUnblock(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.Unblock(womanid, new OnEMFUnblockCallback() {
			@Override
			public void OnEMFUnblock(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFUnblock(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    /**
     * InboxPhotoFee（男士付费获取EMF私密照片：/emf/inbox_photo_fee）
     * @param womanId
     * @param photoId
     * @param sendId
     * @param messageId
     * @return -1 fails, else success
     */
    public long InboxPhotoFee(
    		final String womanId, 
    		final String photoId, 
    		final String sendId, 
    		final String messageId, 
    		final OnEMFInboxPhotoFeeCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.InboxPhotoFee(womanId, photoId, sendId, messageId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFInboxPhotoFee(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.InboxPhotoFee(womanId, photoId, sendId, messageId, new OnEMFInboxPhotoFeeCallback() {
			@Override
			public void OnEMFInboxPhotoFee(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFInboxPhotoFee(isSuccess, errno, errmsg);
				}
			}
		});
    }

    /**
     * PrivatePhotoView（获取对方或自己的EMF私密照片：/emf/private_photo_view）
     * @param womanId
     * @param photoId
     * @param sendId
     * @param messageId
     * @return -1 fails, else success
     */
    public long PrivatePhotoView(
    		final String womanId, 
    		final String photoId, 
    		final String sendId, 
    		final String messageId, 
    		final String filePath, 
    		final PrivatePhotoType type, 
    		final OnEMFPrivatePhotoViewCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniEMF.PrivatePhotoView(womanId, photoId, sendId, messageId, filePath, type, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnEMFPrivatePhotoView(isSuccess, errno, errmsg, "");
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniEMF.PrivatePhotoView(womanId, photoId, sendId, messageId, filePath, type, new OnEMFPrivatePhotoViewCallback() {
			@Override
			public void OnEMFPrivatePhotoView(boolean isSuccess, String errno,
					String errmsg, String filePath) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnEMFPrivatePhotoView(isSuccess, errno, errmsg, filePath);
				}
			}
		});
    }
    
	/**************************************************************************************
	 * EMF模块end
	 **************************************************************************************/
	
	
	/**************************************************************************************
	 * QiuckMatch模块
	 **************************************************************************************/	
    
    /**
     * 10.2.提交已标记的女士
     * @param likeList		喜爱的女士列表
     * @param unlikeList	不喜爱女士列表
     * @param callback
     * @return				请求唯一标识
     */
    public long SubmitQuickMatchMarkLadyList(
    		final String[] likeListId,
    		final String[] unlikeListId,
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniQuickMatch.SubmitQuickMatchMarkLadyList(likeListId, unlikeListId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniQuickMatch.SubmitQuickMatchMarkLadyList(likeListId, unlikeListId, new OnRequestCallback() {
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub

				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
	/**
	 * 10.3.查询已标记like的女士列表
	 * @param callback		
	 * @return				请求唯一标识
	 */
    public long QueryQuickMatchLikeLadyList(
    		final int pageIndex, 
    		final int pageSize, 
    		final OnQueryQuickMatchLikeLadyListCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniQuickMatch.QueryQuickMatchLikeLadyList(pageIndex, pageSize, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryQuickMatchLikeLadyList(isSuccess, errno, errmsg, null, 0);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniQuickMatch.QueryQuickMatchLikeLadyList(pageIndex, pageSize, new OnQueryQuickMatchLikeLadyListCallback() {
			@Override
			public void OnQueryQuickMatchLikeLadyList(boolean isSuccess,
					String errno, String errmsg, QuickMatchLady[] itemList,
					int totalCount) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryQuickMatchLikeLadyList(isSuccess, errno, errmsg, itemList, totalCount);
				}
			}
		});
    }
    
    /**
     * 10.4.删除已标记like的女士
     * @param likeListId	喜爱的女士列表
     * @param callback
     * @return				请求唯一标识
     */
    public long RemoveQuickMatchLikeLadyList(
    		final String[] likeListId,
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniQuickMatch.RemoveQuickMatchLikeLadyList(likeListId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniQuickMatch.RemoveQuickMatchLikeLadyList(likeListId, new OnRequestCallback() {
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub

				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
	/**************************************************************************************
	 * QiuckMatch模块end
	 **************************************************************************************/
    
	/**************************************************************************************
	 * LoveCall模块
	 **************************************************************************************/
    /**
     * 11.1.获取Love Call列表接口
     * @param pageIndex			当前页数
     * @param pageSize			每页行数
     * @param searchType		条件查询Love士列表接口，查询类型<SearchType>
     * @param callback
     * @return					请求唯一标识
     */
    public long QueryLoveCallList(
    		final int pageIndex, 
    		final int pageSize, 
    		final RequestJniLoveCall.SearchType searchType, 
    		final OnQueryLoveCallListCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniLoveCall.QueryLoveCallList(pageIndex, pageSize, searchType, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryLoveCallList(isSuccess, errno, errmsg, null, 0);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLoveCall.QueryLoveCallList(pageIndex, pageSize, searchType, new OnQueryLoveCallListCallback() {

			@Override
			public void OnQueryLoveCallList(boolean isSuccess, String errno,
					String errmsg, LoveCall[] itemList, int totalCount) {
				// TODO Auto-generated method stub

				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryLoveCallList(isSuccess, errno, errmsg, itemList, totalCount);
				}
			}
		});
    }
    
    /**
     * 11.2.确定Love Call接口
     * @param orderId			订单ID
     * @param confirmType		确定类型
     * @param callback
     * @return					请求唯一标识
     */
    public long ConfirmLoveCall(
    		final String orderId, 
    		final ConfirmType confirmType, 
    		final OnRequestCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniLoveCall.ConfirmLoveCall(orderId, confirmType, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLoveCall.ConfirmLoveCall(orderId, confirmType, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		});
    }
    
    /**
     * 11.3.获取LoveCall未处理数接口
     * @param searchType		条件查询Love士列表接口，查询类型<SearchType>
     * @param callback
     * @return					请求唯一标识
     */
    public long QueryLoveCallRequestCount(
    		final RequestJniLoveCall.SearchType searchType, 
    		final OnQueryLoveCallRequestCountCallback callback
    		) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniLoveCall.QueryLoveCallRequestCount(searchType, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnQueryLoveCallRequestCount(isSuccess, errno, errmsg, 0);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniLoveCall.QueryLoveCallRequestCount(searchType, new OnQueryLoveCallRequestCountCallback() {

			@Override
			public void OnQueryLoveCallRequestCount(boolean isSuccess, String errno, String errmsg, int count) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnQueryLoveCallRequestCount(isSuccess, errno, errmsg, count);
				}
			}
		});
    }
    
	/**************************************************************************************
	 * LoveCall模块end
	 **************************************************************************************/
    
    
	/**************************************************************************************
	 * VideoShow模块
	 **************************************************************************************/
    
	/**
	 * VideoList（查询视频列表：/member/videoshow）
	 * @param pageIndex
	 * @param pageSize
	 * @param age1
	 * @param age2
	 * @param callback
	 * @return -1 fails, else success
	 */
	public long VideoList(
			final int pageIndex, 
			final int pageSize, 
			final int age1, 
			final int age2, 
			final OrderByType orderBy, 
			final OnVSVideoListCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniVideoShow.VideoList(pageIndex, pageSize, age1, age2, orderBy, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnVSVideoList(isSuccess, errno, errmsg, 0, 0, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniVideoShow.VideoList(pageIndex, pageSize, age1, age2, orderBy, new OnVSVideoListCallback() {

			@Override
			public void OnVSVideoList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					VSVideoListItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnVSVideoList(isSuccess, errno, errmsg, pageIndex, pageSize, dataCount, listArray);
				}
			}
		});
	}
    
	/**
	 * VideoDetail（查询指定女士的视频信息：/member/video_detail）
	 * @param womanId
	 * @return -1 fails, else success
	 */
	public long VideoDetail(
			final String womanId, 
			final OnVSVideoDetailCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniVideoShow.VideoDetail(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnVSVideoDetail(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniVideoShow.VideoDetail(womanId, new OnVSVideoDetailCallback() {

			@Override
			public void OnVSVideoDetail(boolean isSuccess, String errno,
					String errmsg, VSVideoDetailItem[] item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnVSVideoDetail(isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * PlayVideo（查询视频详细信息：/member/play_video）
	 * @param womanId
	 * @param videoId
	 * @return -1 fails, else success
	 */
	public long PlayVideo(
			final String womanId, 
			final String videoId, 
			final OnVSPlayVideoCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniVideoShow.PlayVideo(womanId, videoId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnVSPlayVideo(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniVideoShow.PlayVideo(womanId, videoId, new OnVSPlayVideoCallback() {

			@Override
			public void OnVSPlayVideo(boolean isSuccess, String errno,
					String errmsg, VSPlayVideoItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnVSPlayVideo(isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * WatchedVideoList（查询已看过的视频列表：/member/watched_video）
	 * @param pageIndex
	 * @param pageSize
	 * @return -1 fails, else success
	 */
	public long WatchedVideoList(
			final int pageIndex, 
			final int pageSize, 
			final OnVSWatchedVideoListCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniVideoShow.WatchedVideoList(pageIndex, pageSize, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnVSWatchedVideoList(isSuccess, errno, errmsg, 0, 0, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniVideoShow.WatchedVideoList(pageIndex, pageSize, new OnVSWatchedVideoListCallback() {

			@Override
			public void OnVSWatchedVideoList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					VSWatchedVideoListItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnVSWatchedVideoList(isSuccess, errno, errmsg, pageIndex, pageSize, dataCount, listArray);
				}
			}
		});
	}
	
	/**
	 * SaveVideo（收藏视频：/member/save_video）
	 * @param videoId
	 * @return -1 fails, else success
	 */
	public long SaveVideo(
			final String videoId, 
			final OnVSSaveVideoCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniVideoShow.SaveVideo(videoId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnVSSaveVideo(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniVideoShow.SaveVideo(videoId, new OnVSSaveVideoCallback() {

			@Override
			public void OnVSSaveVideo(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnVSSaveVideo(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**
	 * RemoveVideo（删除收藏视频：/member/remove_video）
	 * @param videoId
	 * @return -1 fails, else success
	 */
	public long RemoveVideo(
			final String videoId, 
			final OnVSRemoveVideoCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniVideoShow.RemoveVideo(videoId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnVSRemoveVideo(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniVideoShow.RemoveVideo(videoId, new OnVSRemoveVideoCallback() {

			@Override
			public void OnVSRemoveVideo(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnVSRemoveVideo(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**
	 * SavedVideoList（查询已收藏的视频列表：/member/saved_video）
	 * @param pageIndex
	 * @param pageSize
	 * @return -1 fails, else success
	 */
	public long SavedVideoList(
			final int pageIndex, 
			final int pageSize, 
			final OnVSSavedVideoListCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniVideoShow.SavedVideoList(pageIndex, pageSize, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnVSSavedVideoList(isSuccess, errno, errmsg, 0, 0, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniVideoShow.SavedVideoList(pageIndex, pageSize, new OnVSSavedVideoListCallback() {

			@Override
			public void OnVSSavedVideoList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					VSSavedVideoListItem[] listArray) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnVSSavedVideoList(isSuccess, errno, errmsg, pageIndex, pageSize, dataCount, listArray);
				}
			}
		});
	}
	
	/**************************************************************************************
	 * VideoShow模块end
	 **************************************************************************************/
	
	/**************************************************************************************
	 * contact us模块
	 **************************************************************************************/	
	
	/**
	 * 获取所有反馈Ticketlist
	 * @param pageIndex
	 * @param pageSize
	 * @param callback
	 * @return
	 */
	public long TicketList(
			final int pageIndex, 
			final int pageSize, 
			final OnTicketListCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniTicket.TicketList(pageIndex, pageSize, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnTicketList(isSuccess, errno, errmsg, 0, 0, 0, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniTicket.TicketList(pageIndex, pageSize, new OnTicketListCallback() {

			@Override
			public void OnTicketList(boolean isSuccess, String errno,
					String errmsg, int pageIndex, int pageSize, int dataCount,
					TicketListItem[] list) {
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnTicketList(isSuccess, errno, errmsg, pageIndex, pageSize, dataCount, list);
				}
			}

			
		});
	}
	
	
	/**
	 * 获取反馈Ticket具体反馈及回复的详情
	 * @param ticketId
	 * @param callback
	 * @return
	 */
	public long TicketDetail(
			final String ticketId, 
			final OnTicketDetailCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniTicket.TicketDetail(ticketId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnTicketDetail(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniTicket.TicketDetail(ticketId, new OnTicketDetailCallback() {

			@Override
			public void OnTicketDetail(boolean isSuccess, String errno,
					String errmsg, TicketDetailItem item) {
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnTicketDetail(isSuccess, errno, errmsg, item);
				}
			}

		});
	}
	
	/**
	 * 回复继续反馈问题
	 * @param ticketId
	 * @param message
	 * @param filePath
	 * @param callback
	 * @return
	 */
	public long ReplyTicket(
			final String ticketId, 
			final String message,
			final String filePath,
			final OnRequestCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniTicket.ReplyTicket(ticketId, message, filePath, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniTicket.ReplyTicket(ticketId, message, filePath, new OnRequestCallback() {

			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}

		});
	}
	
	/**
	 * 将反馈问题设置为已解决
	 * @param ticketId
	 * @param callback
	 * @return
	 */
	public long ResolvedTicket(
			final String ticketId, 
			final OnRequestCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniTicket.ResolvedTicket(ticketId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniTicket.ResolvedTicket(ticketId, new OnRequestCallback() {

    		@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}

		});
	}
	
	/**
	 * 创建问题反馈
	 * @param ticketId
	 * @param callback
	 * @return
	 */
	public long AddTicket(
			final int typeId,
			final String title,
			final String message,
			final String filePath,
			final OnRequestCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功, 再次调用jni接口
					RequestJniTicket.AddTicket(typeId, title, message, filePath, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniTicket.AddTicket(typeId, title, message, filePath, new OnRequestCallback() {

    		@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnRequest(isSuccess, errno, errmsg);
				}
			}

		});
	}
	
	
	/**************************************************************************************
	 * contact us模块
	 **************************************************************************************/
	
	
	/**************************************************************************************
	 * Other模块
	 **************************************************************************************/	
	
	/**
	 * 查询高级表情配置
	 * @param callback
	 * @return
	 */
	public long EmotionConfig(final OnOtherEmotionConfigCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniOther.EmotionConfig(callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnOtherEmotionConfig(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniOther.EmotionConfig(new OnOtherEmotionConfigCallback() {

			@Override
			public void OnOtherEmotionConfig(boolean isSuccess, String errno,
					String errmsg, OtherEmotionConfigItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnOtherEmotionConfig(isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * 男士会员统计
	 * @param money			是否需要money操作
	 * @param coupon		是否需要coupon操作
	 * @param regStep		是否需要regStep操作
	 * @param allowAlbum	是否需要allowAlbum操作
	 * @param admirerUr		是否需要admirerUr操作
	 * @param callback
	 * @return
	 */
	public long GetCount(
			final boolean money, 
			final boolean coupon, 
			final boolean regStep, 
			final boolean allowAlbum, 
			final boolean admirerUr, 
			final boolean integral,
			final OnOtherGetCountCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniOther.GetCount(
							money, 
							coupon, 
							regStep, 
							allowAlbum, 
							admirerUr, 
							integral,
							callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnOtherGetCount(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniOther.GetCount(
				    			money, 
								coupon, 
								regStep, 
								allowAlbum, 
								admirerUr,
								integral, 
								new OnOtherGetCountCallback() {

			@Override
			public void OnOtherGetCount(boolean isSuccess, String errno,
					String errmsg, OtherGetCountItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnOtherGetCount(isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**
	 * 收集手机硬件信息
	 * @param manId			男士ID
	 * @param verCode		客户端内部版本号
	 * @param verName		客户端显示版本号
	 * @param action		新用户类型
	 * @param siteId		站点ID
	 * @param density		屏幕密度
	 * @param width			屏幕宽度
	 * @param height		屏幕高度
	 * @param lineNumber	电话号码
	 * @param simOptName	sim卡服务商名字
	 * @param simOpt		sim卡移动国家码
	 * @param simCountryIso	sim卡ISO国家码
	 * @param simState		sim卡状态
	 * @param phoneType		手机类型
	 * @param networkType	网络类型
	 * @param deviceId		设备唯一标识
	 * @param callback
	 * @return
	 */
	public long PhoneInfo(
			final String manId, 
			final int verCode, 
			final String verName, 
			final ActionType action, 
			final int siteId, 
			final double density, 
			final int width, 
			final int height, 
			final String lineNumber, 
			final String simOptName, 
			final String simOpt, 
			final String simCountryIso, 
			final String simState, 
			final int phoneType, 
			final int networkType, 
			final String deviceId, 
			final OnOtherPhoneInfoCallback callback
			) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniOther.PhoneInfo(
							manId, 
							verCode, 
							verName, 
							action, 
							siteId, 
							density, 
							width, 
							height, 
							lineNumber, 
							simOptName, 
							simOpt, 
							simCountryIso, 
							simState, 
							phoneType, 
							networkType, 
							deviceId,
							callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnOtherPhoneInfo(isSuccess, errno, errmsg);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniOther.PhoneInfo(
								manId, 
								verCode, 
								verName, 
								action, 
								siteId, 
								density, 
								width, 
								height, 
								lineNumber, 
								simOptName, 
								simOpt, 
								simCountryIso, 
								simState, 
								phoneType, 
								networkType, 
								deviceId,
								new OnOtherPhoneInfoCallback() {

			@Override
			public void OnOtherPhoneInfo(boolean isSuccess, String errno,
					String errmsg) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnOtherPhoneInfo(isSuccess, errno, errmsg);
				}
			}
		});
	}
	
	/**
	 * 查询可否对某女士使用积分
	 * @param womanId	女士ID
	 * @param callback
	 * @return
	 */
	public long IntegralCheck(final String womanId, final OnOtherIntegralCheckCallback callback) {
    	// 登录状态改变重新调用接口
		final OnLoginManagerCallback callbackLogin = new OnLoginManagerCallback() {
			
			@Override
			public void OnLogout(boolean bActive) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void OnLogin(boolean isSuccess, String errno, String errmsg,
					LoginItem item, LoginErrorItem errItem) {
				// TODO Auto-generated method stub
				// 公共处理
				HandleRequestCallback(isSuccess, errno, errmsg, this);
				if( isSuccess ) {
					// 登录成功
					// 再次调用jni接口
					RequestJniOther.IntegralCheck(womanId, callback);
				} else {
					// 登录不成功, 回调失败
					callback.OnOtherIntegralCheck(isSuccess, errno, errmsg, null);
				}
			}
		};
		
		// 调用jni接口
    	return RequestJniOther.IntegralCheck(womanId, new OnOtherIntegralCheckCallback() {

			@Override
			public void OnOtherIntegralCheck(boolean isSuccess, String errno,
					String errmsg, OtherIntegralCheckItem item) {
				// TODO Auto-generated method stub
				// 公共处理
				boolean bFlag = HandleRequestCallback(isSuccess, errno, errmsg, callbackLogin);
				if( bFlag ) {
					// 已经匹配处理, 等待回调
				} else {
					// 没有匹配处理, 直接回调
					callback.OnOtherIntegralCheck(isSuccess, errno, errmsg, item);
				}
			}
		});
	}
	
	/**************************************************************************************
	 * Other模块
	 **************************************************************************************/
}
