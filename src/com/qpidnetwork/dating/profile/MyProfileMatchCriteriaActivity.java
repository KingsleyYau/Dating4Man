package com.qpidnetwork.dating.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestEnum.Marry;
import com.qpidnetwork.request.RequestJniLady;
import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LadyMatch;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogNumberRangeChooser;
import com.qpidnetwork.view.MaterialDialogSingleChoice;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfileMatchCriteriaActivity extends BaseActivity {

	private enum RequestFlag {
		REQUEST_SAVE_LADYMATCH_SUCCESS,
		REQUEST_FAIL,
	}
	
	/**
	 * 上下文
	 */
	private Context mContext;
	
	/**
	 * 女士匹配条件
	 */
	private LadyMatch mLadyMatch;
	
	/**
	 *  详细资料项目
	 */
	private MaterialAppBar appbar;
	private MyProfileMatchCriteriaItemView layoutAge;
	private MyProfileMatchCriteriaItemView layoutChildren;
	private MyProfileMatchCriteriaItemView layoutEducation;
	private MyProfileMatchCriteriaItemView layoutRelationship;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		// 创建界面时候，获取缓存数据
		mLadyMatch = MyProfilePerfence.GetLadyMatch(mContext);
		
		// 刷新界面
		ReloadData();
	}
	
	/**
	 * 点击取消
	 * @param v
	 */
	public void onClickCancel(View v) {
		finish();
	}
	
	/**
	 * 初始化界面
	 */
	public void InitView() {
		setContentView(R.layout.activity_my_profile_match_criteria);
		
		
		//app bar
		appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "back", R.drawable.ic_arrow_back_grey600_24dp);
		appbar.setTitle("Match criteria", getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()){
				case android.R.id.button1:
					finish();
					break;
				default:
					break;
				}
			}
			
		});
		
		/**
		 * 年龄
		 */
		layoutAge = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutAge);
		layoutAge.textViewLeft.setText("Her Age");
		layoutAge.imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*final ChooseAgeDialog dialog = new ChooseAgeDialog(mContext, R.style.ChoosePhotoDialog);
				dialog.mMinValue = mLadyMatch.age1;
				dialog.mMaxValue = mLadyMatch.age2;
		        dialog.show();
		        dialog.textViewConfirm.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mLadyMatch.age1 = dialog.mMinValue;
						mLadyMatch.age2 = dialog.mMaxValue;
						dialog.dismiss();
						ReloadData();
						SaveLadyMatch();
					}
				});*/
		        
		        MaterialDialogNumberRangeChooser dialog = new MaterialDialogNumberRangeChooser(
		        		MyProfileMatchCriteriaActivity.this, 
		        		new MaterialDialogNumberRangeChooser.OnClickCallback() {
							
							@Override
							public void onClick(View v, int[] range) {
								// TODO Auto-generated method stub
								mLadyMatch.age1 = range[0];
								mLadyMatch.age2 = range[1];
								SaveLadyMatch();
							}
						},
						new int[]{18, 99});
		        dialog.setCanceledOnTouchOutside(true);
		        dialog.setTitle("Her age");
		        dialog.setSelectRange(new int[]{mLadyMatch.age1, mLadyMatch.age2});
		        dialog.show();
				
				
			}
		});
		
		/**
		 * 子女情况
		 */
		layoutChildren = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutChildren);
		layoutChildren.textViewLeft.setText("She has children");
		layoutChildren.imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Children");  
                builder.setSingleChoiceItems(R.array.children, mLadyMatch.children.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "子女情况: " + String.valueOf(which));
						if( which > -1 && which < Children.values().length ) {
							mLadyMatch.children = Children.values()[which];
							ReloadData();
						}
					}
				});
                builder.setPositiveButton(R.string.common_btn_confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						SaveLadyMatch();
					}
				});
                builder.create().show();*/
				
				String[] items = getResources().getStringArray(R.array.children);
						MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(MyProfileMatchCriteriaActivity.this, items, new MaterialDialogSingleChoice.OnClickCallback(){

							@Override
							public void onClick(AdapterView<?> adptView,
									View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Children.values().length ) {
									mLadyMatch.children = Children.values()[which];
									SaveLadyMatch();
								}
							}
							
						},
						mLadyMatch.children.ordinal());
						dialog.setCanceledOnTouchOutside(true);
						dialog.setTitle("Does her have children?");
				dialog.show();
				
				
			}
		});
		
		/**
		 * 教育程度
		 */
		layoutEducation = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutEducation);
		layoutEducation.textViewLeft.setText("Her education");
		layoutEducation.imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Education");  
                builder.setSingleChoiceItems(R.array.education, mLadyMatch.education.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "教育程度: " + String.valueOf(which));
						if( which > -1 && which < Education.values().length ) {
							mLadyMatch.education = Education.values()[which];
							ReloadData();
						}
					}
				});
                builder.setPositiveButton(R.string.common_btn_confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						SaveLadyMatch();
					}
				});
                builder.create().show();*/
				
				String[] items = getResources().getStringArray(R.array.education);
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(MyProfileMatchCriteriaActivity.this, items, new MaterialDialogSingleChoice.OnClickCallback(){

					@Override
					public void onClick(AdapterView<?> adptView,
							View v, int which) {
						// TODO Auto-generated method stub
						if( which > -1 && which < Education.values().length ) {
							mLadyMatch.education = Education.values()[which];
							SaveLadyMatch();
						}
					}
					
				},
				mLadyMatch.education.ordinal());
				
				dialog.setCanceledOnTouchOutside(true);
				dialog.setTitle("Her education level");
				dialog.show();
			}
		});
		
		/**
		 * 感情状况
		 */
		layoutRelationship = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutRelationship);
		layoutRelationship.textViewLeft.setText("Relationship Status");
		layoutRelationship.imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Relationship");  
                builder.setSingleChoiceItems(R.array.marry, mLadyMatch.marry.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "婚姻情况: " + String.valueOf(which));
						if( which > -1 && which < Marry.values().length ) {
							mLadyMatch.marry = Marry.values()[which];
							ReloadData();
						}
					}
				});
                builder.setPositiveButton(R.string.common_btn_confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						SaveLadyMatch();
					}
				});
                builder.create().show();*/
				
				
				String[] items = getResources().getStringArray(R.array.marry);
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(MyProfileMatchCriteriaActivity.this, items, new MaterialDialogSingleChoice.OnClickCallback(){

					@Override
					public void onClick(AdapterView<?> adptView,
							View v, int which) {
						// TODO Auto-generated method stub
						if( which > -1 && which < Marry.values().length ) {
							mLadyMatch.marry = Marry.values()[which];
							SaveLadyMatch();
						}
					}
					
				},
				mLadyMatch.marry.ordinal());
				
				dialog.setCanceledOnTouchOutside(true);
				dialog.setTitle("Her relationship status");
				dialog.show();
			}
		});
		
	}
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				// 收起菊花
				//hideProgressDialog();
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_SAVE_LADYMATCH_SUCCESS:{
					// 上传匹配女士成功
					// 刷新界面
					showToastDone("Done!");
					ReloadData();
				}break;
				case REQUEST_FAIL:{
					// 请求失败
					showToastFailed("Failed!");
				}break;
				default:
					break;
				}
			};
		};
	}
	
	/**
	 * 刷新界面
	 */
	private void ReloadData() {
		if( mLadyMatch != null ) {
			/**
			 * 年龄
			 */
			layoutAge.textViewValue.setText(AgeToString());
			/**
			 * 子女情况
			 */
			layoutChildren.textViewValue.setText(ChildrenToString());
			/**
			 * 教育程度
			 */
			layoutEducation.textViewValue.setText(EducationToString());
			/**
			 * 感情状况
			 */
			layoutRelationship.textViewValue.setText(RelationshipToString());
		}
	}
	
	/**
	 * 年龄
	 * @return
	 */
	private String AgeToString() {
		return String.format("%d - %d", mLadyMatch.age1, mLadyMatch.age2);
	}
	
	/**
	 * 子女状况
	 * @return
	 */
	private String ChildrenToString() {
		String result = "";
		switch (mLadyMatch.children) {
		case Unknow:{
			result += "";
		}break;
		case Yes:{
			result += getResources().getString(R.string.match_criteria_children_yes);
		}break;
		case None:{
			result += getResources().getString(R.string.match_criteria_children_no);
		}break;
		default: {
		}break;
		}
		return result;
	}
	
	/**
	 * 教育程度
	 * @return
	 */
	private String EducationToString() {
		String result = "";
		switch (mLadyMatch.education) {
		case Unknow:{
			result += "--";
		}break;
		case SecondaryHighSchool:{
			result += getResources().getString(R.string.match_criteria_education_height_school);
		}break;
		case VocationalSchool:{
			result += getResources().getString(R.string.match_criteria_education_vocational_school);
		}break;
		case College:{
			result += getResources().getString(R.string.match_criteria_education_college);
		}break;
		case Bachelor:{
			result += getResources().getString(R.string.match_criteria_education_bachelor);
		}break;
		case Master:{
			result += getResources().getString(R.string.match_criteria_education_master);
		}break;
		case Doctorate:{
			result += getResources().getString(R.string.match_criteria_education_doctorate);
		}break;
		case PostDoctorate:{
			result += getResources().getString(R.string.match_criteria_education_post_doctorate);
		}break;
		default:{
			result += getResources().getString(R.string.match_criteria_education_height_school);
		}break;
		}
		return result;
	}
	
	/**
	 * 感情状况
	 * @return
	 */
	private String RelationshipToString() {
		String result = "";
		switch (mLadyMatch.marry) {
		case Unknow:{
			result += "--";
		}break;
		case NeverMarried:{
			result += getResources().getString(R.string.match_criteria_married_single);
		}break;
		case Divorced:{
			result += getResources().getString(R.string.match_criteria_married_divorced);
		}break;
		case Widowed:{
			result += getResources().getString(R.string.match_criteria_married_widowed);
		}break;
		case Separated:{
			result += getResources().getString(R.string.match_criteria_married_separated);
		}break;
		case Married:{
			result += getResources().getString(R.string.match_criteria_married_married);
		}break;
		default:{
			result += getResources().getString(R.string.match_criteria_married_single);
		}break;
		}
		return result;
	}
	
	/**
	 * 上传匹配女士
	 */
	private void SaveLadyMatch() {
		// 此处应有菊花
		//showProgressDialog("Loading...");
		showToastProgressing("Updating");
		RequestOperator.getInstance().SaveLadyMatch(
				mLadyMatch.age1, 
				mLadyMatch.age2, 
				mLadyMatch.children, 
				mLadyMatch.marry, 
				mLadyMatch.education, 
				new OnRequestCallback() {
					
					@Override
					public void OnRequest(boolean isSuccess, String errno, String errmsg) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						if( isSuccess ) {
							// 上传匹配女士成功
							msg.what = RequestFlag.REQUEST_SAVE_LADYMATCH_SUCCESS.ordinal();
							
							// 缓存数据到配置
							MyProfilePerfence.SaveLadyMatch(mContext, mLadyMatch);
							
						} else {
							// 获取个人信息失败
							msg.what = RequestFlag.REQUEST_FAIL.ordinal();
						}
						mHandler.sendMessage(msg);
					}
				});
	}
}
