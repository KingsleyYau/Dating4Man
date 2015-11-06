package com.qpidnetwork.dating.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import com.qpidnetwork.framework.base.BaseFragmentActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestEnum.Marry;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LadyMatch;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogNumberRangeChooser;
import com.qpidnetwork.view.MaterialDialogSingleChoice;

/**
 * MyProfile模块
 * @author Max.Chiu
 */
public class MyProfileMatchCriteriaActivity extends BaseFragmentActivity implements OnClickListener, OnRequestCallback {

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
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()){
		case android.R.id.button1:
			finish();
			break;
		default:{
			switch ((int)v.getTag()){
			case R.id.layoutAge:{
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
			}break;
			case R.id.layoutChildren:{
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
			}break;
			case R.id.layoutEducation:{
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
			}break;
			case R.id.layoutRelationship:{
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
			}break;
			}
		}break;
		}
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
		appbar.setOnButtonClickListener(this);
		
		/**
		 * 年龄
		 */
		layoutAge = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutAge);
		layoutAge.textViewLeft.setText("Her Age");
		layoutAge.imageView.setTag(R.id.layoutAge);
		layoutAge.imageView.setOnClickListener(this);
		
		/**
		 * 子女情况
		 */
		layoutChildren = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutChildren);
		layoutChildren.textViewLeft.setText("She has children");
		layoutChildren.imageView.setTag(R.id.layoutChildren);
		layoutChildren.imageView.setOnClickListener(this);
		
		/**
		 * 教育程度
		 */
		layoutEducation = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutEducation);
		layoutEducation.textViewLeft.setText("Her education");
		layoutEducation.imageView.setTag(R.id.layoutEducation);
		layoutEducation.imageView.setOnClickListener(this);
		
		/**
		 * 感情状况
		 */
		layoutRelationship = (MyProfileMatchCriteriaItemView) findViewById(R.id.layoutRelationship);
		layoutRelationship.textViewLeft.setText("Relationship Status");
		layoutRelationship.imageView.setTag(R.id.layoutRelationship);
		layoutRelationship.imageView.setOnClickListener(this);
		
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
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
				this
				);
	}

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
		sendUiMessage(msg);
	}
}
