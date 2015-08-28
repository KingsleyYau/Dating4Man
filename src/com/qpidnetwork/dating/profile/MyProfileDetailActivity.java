package com.qpidnetwork.dating.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.dating.BaseActivity;
import com.qpidnetwork.dating.R;
import com.qpidnetwork.dating.bean.RequestBaseResponse;
import com.qpidnetwork.framework.widget.wrap.WrapBaseAdapter;
import com.qpidnetwork.framework.widget.wrap.WrapListView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnGetMyProfileCallback;
import com.qpidnetwork.request.OnQueryLadyMatchCallback;
import com.qpidnetwork.request.OnUpdateMyProfileCallback;
import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Drink;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestEnum.Ethnicity;
import com.qpidnetwork.request.RequestEnum.Height;
import com.qpidnetwork.request.RequestEnum.Income;
import com.qpidnetwork.request.RequestEnum.Language;
import com.qpidnetwork.request.RequestEnum.Profession;
import com.qpidnetwork.request.RequestEnum.Religion;
import com.qpidnetwork.request.RequestEnum.Smoke;
import com.qpidnetwork.request.RequestEnum.Weight;
import com.qpidnetwork.request.RequestOperator;
import com.qpidnetwork.request.item.LadyMatch;
import com.qpidnetwork.request.item.ProfileItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.view.MaterialAppBar;
import com.qpidnetwork.view.MaterialDialogSingleChoice;

public class MyProfileDetailActivity extends BaseActivity {
	private class InterestLabelAdapter extends WrapBaseAdapter {
		
		private Context mContext;
		private List<String> mList;
		
		public InterestLabelAdapter(Context context, List<String> list){
			this.mContext = context;
			this.mList = list;
		}

		@Override
		protected int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		protected String getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		protected int getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		protected View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_label_item, null);
				holder.cvLabel = (CardView)convertView.findViewById(R.id.cvLabel);
				holder.ivLabelCheck = (ImageView)convertView.findViewById(R.id.ivLabelCheck);
				holder.ivLabelCheck.setVisibility(View.GONE);
				holder.tvLabelDesc = (TextView)convertView.findViewById(R.id.tvLabelDesc);
				holder.elementContainer = (LinearLayout)convertView.findViewById(R.id.element_container);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			String name = getItem(position);
			holder.cvLabel.setCardElevation(0);
			holder.cvLabel.setCardBackgroundColor(mContext.getResources().getColor(R.color.thin_grey));
			holder.elementContainer.setLayoutParams(new CardView.LayoutParams(LayoutParams.WRAP_CONTENT, (int)(24.0f * mContext.getResources().getDisplayMetrics().density)));
			holder.tvLabelDesc.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
			holder.tvLabelDesc.setTextSize(14.0f);
			holder.tvLabelDesc.setText(name);
			
			
			return convertView;
		}
		
		private class ViewHolder {
			CardView cvLabel;
			ImageView ivLabelCheck;
			TextView tvLabelDesc;
			LinearLayout elementContainer;
		}

	}
	
	/**
	 * 兴趣爱好
	 */
	private WrapListView wrapListView;
	private List<String> mList = new ArrayList<String>();
	private InterestLabelAdapter interestLabelAdapter = new InterestLabelAdapter(this, mList);
	
	/**
	 * 编辑个人简介
	 */
	private static final int RESULT_SELF_INTRO = 0;
	/**
	 * 编辑匹配女士
	 */
	private static final int RESULT_MATCH_CRITERIA = 1;
	/**
	 * 编辑兴趣爱好
	 */
	private static final int RESULT_INTEREST = 2;
	
	private enum RequestFlag {
		REQUEST_UPDATE_PROFILE_SUCCESS,
		REQUEST_PROFILE_SUCCESS,
		REQUEST_QUERYLADYMATCH_SUCCESS,
		REQUEST_FAIL,
	}
	
	
	/**
	 * 上下文
	 */
	private Context mContext;
	
	/**
	 * 个人信息
	 */
	private ProfileItem mProfileItem;
	
	/**
	 * 女士匹配条件
	 */
	private LadyMatch mLadyMatch;
	
	private View rootView;
	/**
	 * 用户头像
	 */
	private ImageView imageViewHeader;
	private ImageViewLoader loader = new ImageViewLoader(this);

	/**
	 *  用户名称
	 */
	private TextView textViewName;
	
	/**
	 * 国家/年龄
	 */
	private TextView textViewAge;
	private TextView textViewCountry;
	
	/**
	 * 个人简介
	 */
	private TextView textViewSelfInfo;
	
	/**
	 * 匹配说明
	 */
	private TextView textViewMatchCriteria;
	
	/**
	 * 展开个人资料
	 */
	private Button buttonMoreSelfInfo;
	
	/**
	 * 是否已经展开个人资料
	 */
	private boolean mMore = false;
	
	/**
	 *  详细资料项目
	 */
	
	private MyProfileDetailEditItemView titleMyselfIntro;
	private MyProfileDetailEditItemView titleMyInterests;
	private MyProfileDetailEditItemView titleMyMatchCriteria;
	
	private MyProfileDetailEditItemView layoutMemberId;
	private MyProfileDetailEditItemView layoutHeight;
	private MyProfileDetailEditItemView layoutWeight;
	private MyProfileDetailEditItemView layoutSmoke;
	private MyProfileDetailEditItemView layoutDrink;
	private MyProfileDetailEditItemView layoutEducation;
	private MyProfileDetailEditItemView layoutProfession;
	private MyProfileDetailEditItemView layoutEthnicity;
	private MyProfileDetailEditItemView layoutReligion;
	private MyProfileDetailEditItemView layoutPrimaryLanguage;
	private MyProfileDetailEditItemView layoutHaveChildren;
	private MyProfileDetailEditItemView layoutCurrentIncome;
	private MyProfileDetailEditItemView layoutZodiac;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;

		// 创建界面时候，获取缓存数据
		mProfileItem = MyProfilePerfence.GetProfileItem(mContext);
		if( mProfileItem != null ) {
			setEnabledAll(rootView, true);
		} else {
			setEnabledAll(rootView, false);
		}
		
		// 请求个人资料
		GetMyProfile();
		
		// 请求匹配女士
		QueryLadyMatch();
		
		// 刷新界面
		ReloadData();
	}
	
	public static void setEnabledAll(View v, boolean enabled) {
	    v.setEnabled(enabled);
	    v.setFocusable(enabled);

	    if(v instanceof ViewGroup) {
	        ViewGroup vg = (ViewGroup) v;
	        for (int i = 0; i < vg.getChildCount(); i++) {
	            setEnabledAll(vg.getChildAt(i), enabled);
	        }
	    }
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
		// 如果有需要, 则请求个人资料
		
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
		setContentView(R.layout.activity_my_profile_detail);
		rootView = findViewById(R.id.rootView);
		
		//APP BAR
		MaterialAppBar appbar = (MaterialAppBar)findViewById(R.id.appbar);
		appbar.setTouchFeedback(MaterialAppBar.TOUCH_FEEDBACK_HOLO_LIGHT);
		appbar.addButtonToLeft(android.R.id.button1, "", R.drawable.ic_close_grey600_24dp);
		appbar.setTitle(getString(R.string.title_activity_profile_detail), getResources().getColor(R.color.text_color_dark));
		appbar.setOnButtonClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()){
				case android.R.id.button1:
					finish();
					break;
				}
			}
			
		});
		
		/**
		 * 用户头像
		 */
		imageViewHeader = (ImageView) findViewById(R.id.imageViewHeader);

		/**
		 *  用户名称
		 */
		textViewName = (TextView) findViewById(R.id.textViewName);

		/**
		 * 国家/年龄
		 */
		textViewAge = (TextView) findViewById(R.id.textViewAge);
		textViewCountry = (TextView) findViewById(R.id.textViewCountry);
		
		/**
		 * 展开个人资料
		 */
		
		titleMyselfIntro = (MyProfileDetailEditItemView)findViewById(R.id.layoutSelfIntro);
		titleMyInterests = (MyProfileDetailEditItemView)findViewById(R.id.layoutMyInterests);
		titleMyMatchCriteria = (MyProfileDetailEditItemView)findViewById(R.id.layoutMatchCriteria);
		
		titleMyselfIntro.textViewValue.setVisibility(View.GONE);
		titleMyInterests.textViewValue.setVisibility(View.GONE);
		titleMyMatchCriteria.textViewValue.setVisibility(View.GONE);
		
		titleMyselfIntro.textViewLeft.setTypeface(null, Typeface.BOLD);
		titleMyInterests.textViewLeft.setText(getString(R.string.my_interest));
		titleMyInterests.textViewLeft.setTypeface(null, Typeface.BOLD);
		titleMyMatchCriteria.textViewLeft.setTypeface(null, Typeface.BOLD);
		
		textViewSelfInfo = (TextView) findViewById(R.id.textViewSelfInfo);
		buttonMoreSelfInfo = (Button) findViewById(R.id.buttonMoreSelfInfo);
		buttonMoreSelfInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( mMore ) {
					buttonMoreSelfInfo.setText("more");
					textViewSelfInfo.setMaxLines(4);
				} else {
					buttonMoreSelfInfo.setText("less");
					textViewSelfInfo.setMaxLines(999);
				}
				
				mMore = !mMore;
			}
		});
		
		/**
		 * 个人简介
		 */
		textViewSelfInfo = (TextView) findViewById(R.id.textViewSelfInfo);
		MyProfileDetailEditItemView view = (MyProfileDetailEditItemView) findViewById(R.id.layoutSelfIntro);
		view.imageViewRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 打开编辑个人简介
				Intent intent = new Intent(mContext, MyProfileDetailSelfIntroActivity.class);
				if( mProfileItem != null ) {
					intent.putExtra(MyProfileDetailSelfIntroActivity.SELF_INTRO, mProfileItem.resume);
				}
				startActivityForResult(intent, RESULT_SELF_INTRO);
			}
		});
		
		/**
		 * 匹配说明
		 */
		textViewMatchCriteria = (TextView) findViewById(R.id.textViewMatchCriteria);
		view = (MyProfileDetailEditItemView) findViewById(R.id.layoutMatchCriteria);
		view.textViewLeft.setText(R.string.My_match_criteria);
		view.imageViewRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 打开编辑匹配女士
				Intent intent = new Intent(mContext, MyProfileMatchCriteriaActivity.class);
				startActivityForResult(intent, RESULT_MATCH_CRITERIA);
			}
		});
		
		/**
		 * 兴趣爱好
		 */
		view = (MyProfileDetailEditItemView) findViewById(R.id.layoutMyInterests);
		view.textViewLeft.setText(R.string.my_interest);
		view.imageViewRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 打开编辑兴趣爱好
				Intent intent = new Intent(mContext, MyProfileDetailInterestActivity.class);
				intent.putStringArrayListExtra(MyProfileDetailInterestActivity.INTEREST, mProfileItem.interests);
				startActivityForResult(intent, RESULT_INTEREST);
			}
		});
		wrapListView = (WrapListView) findViewById(R.id.wrapListView);
		wrapListView.setDividerWidth(5);
		wrapListView.setDividerHeight(10);
		wrapListView.setAdapter(interestLabelAdapter);
		
		layoutMemberId = (MyProfileDetailEditItemView) findViewById(R.id.layoutMemberId);
		layoutMemberId.textViewLeft.setText("Member ID");
		layoutMemberId.textViewValue.setText("");
		layoutMemberId.imageViewRight.setVisibility(View.INVISIBLE);
		
		/**
		 * 身高
		 */
		layoutHeight = (MyProfileDetailEditItemView) findViewById(R.id.layoutHeight);
		layoutHeight.textViewLeft.setText("Height");
		layoutHeight.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String[] array = getResources().getStringArray(R.array.height);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Height.values().length ) {
									mProfileItem.height = Height.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.height.ordinal());
				
				dialog.setTitle("Your height");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);

			}
		});
		
		/**
		 * 体重
		 */
		layoutWeight = (MyProfileDetailEditItemView) findViewById(R.id.layoutWeight);
		layoutWeight.textViewLeft.setText("Weight");
		layoutWeight.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Weight");  
                builder.setSingleChoiceItems(R.array.weight, mProfileItem.weight.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "体重: " + String.valueOf(which));
						if( which > -1 && which < Weight.values().length ) {
							mProfileItem.weight = Weight.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
				
				
				String[] array = getResources().getStringArray(R.array.weight);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Weight.values().length ) {
									mProfileItem.weight = Weight.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.weight.ordinal());
				
				dialog.setTitle("Your weight");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
			}
		});
		
		/**
		 * 吸烟状态
		 */
		layoutSmoke = (MyProfileDetailEditItemView) findViewById(R.id.layoutSmoke);
		layoutSmoke.textViewLeft.setText("Smoke");
		layoutSmoke.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				String[] array = getResources().getStringArray(R.array.smoke);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Smoke.values().length ) {
									mProfileItem.smoke = Smoke.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.smoke.ordinal());
				
				dialog.setTitle("Do you smoke?");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
				
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Smoke");  
                builder.setSingleChoiceItems(R.array.smoke, mProfileItem.smoke.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "吸烟状态: " + String.valueOf(which));
						if( which > -1 && which < Smoke.values().length ) {
							mProfileItem.smoke = Smoke.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
			}
		});
		
		/**
		 * 喝酒状态
		 */
		layoutDrink = (MyProfileDetailEditItemView) findViewById(R.id.layoutDrink);
		layoutDrink.textViewLeft.setText("Drink");
		layoutDrink.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String[] array = getResources().getStringArray(R.array.drink);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Drink.values().length ) {
									mProfileItem.drink = Drink.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.drink.ordinal());
				
				dialog.setTitle("How often do you drink?");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Drink");  
                builder.setSingleChoiceItems(R.array.drink, mProfileItem.drink.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "喝酒状态: " + String.valueOf(which));
						if( which > -1 && which < Drink.values().length ) {
							mProfileItem.drink = Drink.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
			}
		});
		
		/**
		 * 教育程度
		 */
		layoutEducation = (MyProfileDetailEditItemView) findViewById(R.id.layoutEducation);
		layoutEducation.textViewLeft.setText("Education");
		layoutEducation.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String[] array = getResources().getStringArray(R.array.education);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Education.values().length ) {
									mProfileItem.education = Education.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.education.ordinal());
				
				dialog.setTitle("What is you education level?");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
				
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Education");  
                builder.setSingleChoiceItems(R.array.education, mProfileItem.education.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "教育程度: " + String.valueOf(which));
						if( which > -1 && which < Education.values().length ) {
							mProfileItem.education = Education.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
			}
		});
		
		/**
		 * 职业
		 */
		layoutProfession = (MyProfileDetailEditItemView) findViewById(R.id.layoutProfession);
		layoutProfession.textViewLeft.setText("Profession");
		layoutProfession.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String[] array = getResources().getStringArray(R.array.profression);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Profession.values().length ) {
									mProfileItem.profession = Profession.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.profession.ordinal());
				
				dialog.setTitle("Your profession");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
				
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Profession");  
                builder.setSingleChoiceItems(R.array.profression, mProfileItem.profession.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "职业: " + String.valueOf(which));
						if( which > -1 && which < Profession.values().length ) {
							mProfileItem.profession = Profession.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
			}
		});
		
		/**
		 * 信仰
		 */	
		layoutEthnicity = (MyProfileDetailEditItemView) findViewById(R.id.layoutEthnicity);
		layoutEthnicity.textViewLeft.setText("Ethnicity");
		layoutEthnicity.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				String[] array = getResources().getStringArray(R.array.ethnicity);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Ethnicity.values().length ) {
									mProfileItem.ethnicity = Ethnicity.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.ethnicity.ordinal());
				
				dialog.setTitle("What is you ethnicity?");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
				
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Ethnicity");  
                builder.setSingleChoiceItems(R.array.ethnicity, mProfileItem.ethnicity.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "信仰: " + String.valueOf(which));
						if( which > -1 && which < Ethnicity.values().length ) {
							mProfileItem.ethnicity = Ethnicity.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
			}
		});
		
		/**
		 * 种族
		 */
		layoutReligion = (MyProfileDetailEditItemView) findViewById(R.id.layoutReligion);
		layoutReligion.textViewLeft.setText("Religion");
		layoutReligion.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Religion");  
                builder.setSingleChoiceItems(R.array.religion, mProfileItem.religion.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "种族: " + String.valueOf(which));
						if( which > -1 && which < Religion.values().length ) {
							mProfileItem.religion = Religion.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
				
				
				String[] array = getResources().getStringArray(R.array.religion);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Religion.values().length ) {
									mProfileItem.religion = Religion.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.religion.ordinal());
				
				dialog.setTitle("You religion");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
			}
		});
		
		/**
		 * 语言
		 */
		layoutPrimaryLanguage = (MyProfileDetailEditItemView) findViewById(R.id.layoutPrimaryLanguage);
		layoutPrimaryLanguage.textViewLeft.setText("Primary Language");
		layoutPrimaryLanguage.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String[] array = getResources().getStringArray(R.array.language);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Language.values().length ) {
									mProfileItem.language = Language.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.language.ordinal());
				
				dialog.setTitle("What language do you speak?");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
				
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Language");  
                builder.setSingleChoiceItems(R.array.language, mProfileItem.language.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "语言: " + String.valueOf(which));
						if( which > -1 && which < Language.values().length ) {
							mProfileItem.language = Language.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
			}
		});
		
		/**
		 * 子女情况
		 */
		layoutHaveChildren = (MyProfileDetailEditItemView) findViewById(R.id.layoutHaveChildren);
		layoutHaveChildren.textViewLeft.setText("Have Children");
		layoutHaveChildren.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Children");  
                builder.setSingleChoiceItems(R.array.children, mProfileItem.children.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "子女情况: " + String.valueOf(which));
						if( which > -1 && which < Children.values().length ) {
							mProfileItem.children = Children.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
				
				
				String[] array = getResources().getStringArray(R.array.children);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Children.values().length ) {
									mProfileItem.children = Children.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.children.ordinal());
				
				dialog.setTitle("Do you have children?");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
			}
		});
		
		/**
		 * 收入情况
		 */
		layoutCurrentIncome = (MyProfileDetailEditItemView) findViewById(R.id.layoutCurrentIncome);
		layoutCurrentIncome.textViewLeft.setText("Current Income");
		layoutCurrentIncome.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Builder builder = new AlertDialog.Builder(mContext);  
                builder.setTitle("Income");  
                builder.setSingleChoiceItems(R.array.income, mProfileItem.income.ordinal(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
						Log.d("max", "收入情况: " + String.valueOf(which));
						if( which > -1 && which < Income.values().length ) {
							mProfileItem.income = Income.values()[which];
							ReloadData();
							UploadProfile();
							dialog.dismiss();
						}
					}
				});
                builder.create().show();*/
				
				String[] array = getResources().getStringArray(R.array.income);
				
				MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
						MyProfileDetailActivity.this, 
						array,
						new MaterialDialogSingleChoice.OnClickCallback() {
							
							@Override
							public void onClick(AdapterView<?> adptView, View v, int which) {
								// TODO Auto-generated method stub
								if( which > -1 && which < Income.values().length ) {
									mProfileItem.income = Income.values()[which];
									ReloadData();
									UploadProfile();
								}
							}
						},
						mProfileItem.income.ordinal());
				
				dialog.setTitle("Your income");
				dialog.show();
				dialog.setCanceledOnTouchOutside(true);
				
			}
		});
		
		/**
		 * 星座
		 */
		layoutZodiac = (MyProfileDetailEditItemView) findViewById(R.id.layoutZodiac);
		layoutZodiac.textViewLeft.setText("Zodiac");
		layoutZodiac.imageViewRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		layoutZodiac.setVisibility(View.GONE);
	}
	
	@Override
	public void InitHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				RequestBaseResponse obj = (RequestBaseResponse) msg.obj;
				switch ( RequestFlag.values()[msg.what] ) {
				case REQUEST_PROFILE_SUCCESS:
					hideProgressDialog();
					// 缓存数据
					mProfileItem = (ProfileItem)obj.body;
					MyProfilePerfence.SaveProfileItem(mContext, mProfileItem);
					
					// 刷新界面
					ReloadData();
					break;
				case REQUEST_UPDATE_PROFILE_SUCCESS:
					showToastDone("Done!");

					// 缓存数据
					MyProfilePerfence.SaveProfileItem(mContext, mProfileItem);
					
					ReloadData();
					break;
				case REQUEST_QUERYLADYMATCH_SUCCESS:
					cancelToast();

					// 获取匹配女士成功
					// 缓存数据
					mLadyMatch = (LadyMatch)obj.body;
					MyProfilePerfence.SaveLadyMatch(mContext, mLadyMatch);
					
					ReloadData();
					break;
				case REQUEST_FAIL:{
					hideProgressDialog();
					Toast.makeText(mContext, obj.errmsg, Toast.LENGTH_LONG).show();	
				}break;
				default:
					break;
				}
			};
		};
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	 
	    switch(requestCode) {
	    case RESULT_SELF_INTRO:{
	    	// 编辑个人简介返回
	    	if( resultCode == RESULT_OK ) {
	    		String resume = data.getExtras().getString(MyProfileDetailSelfIntroActivity.SELF_INTRO);
	    		mProfileItem.resume = resume;
	    		
	    		// 刷新界面
	    		ReloadData();
	    		
	    		// 上传资料
	    		UploadProfile();
	    	}
	    }break;
	    case RESULT_MATCH_CRITERIA:{
	    	// 编辑匹配女士
	    	if( resultCode == RESULT_OK ) {
	    	
	    	}
	    }break;
	    case RESULT_INTEREST:{
	    	// 编辑兴趣爱好
	    	if( resultCode == RESULT_OK ) {
	    		ArrayList<String> interests = data.getExtras().getStringArrayList(MyProfileDetailInterestActivity.INTEREST);
	    		mProfileItem.interests = interests;
	    		
	    		// 刷新界面
	    		ReloadData();
	    		
	    		// 上传资料
	    		UploadProfile();
	    	}
	    }
	    default:break;
	    }
	}
	
	/**
	 * 获取个人信息
	 */
	public void GetMyProfile() {
		// 此处应有菊花
		showProgressDialog("Loading...");
		RequestOperator.getInstance().GetMyProfile(new OnGetMyProfileCallback() {
			@Override
			public void OnGetMyProfile(boolean isSuccess, String errno, String errmsg,
					ProfileItem item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, item);
				if( isSuccess ) {
					// 获取个人信息成功
					msg.what = RequestFlag.REQUEST_PROFILE_SUCCESS.ordinal();
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 获取配皮女士
	 */
	private void QueryLadyMatch() {
		// 此处应有菊花
		RequestOperator.getInstance().QueryLadyMatch(new OnQueryLadyMatchCallback() {
			@Override
			public void OnQueryLadyMatch(boolean isSuccess, String errno,
					String errmsg, LadyMatch item) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, item);
				if( isSuccess ) {
					// 获取配皮女士成功
					msg.what = RequestFlag.REQUEST_QUERYLADYMATCH_SUCCESS.ordinal();
				} else {
					// 获取个人信息失败
					msg.what = RequestFlag.REQUEST_FAIL.ordinal();
				}
				msg.obj = obj;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 上传个人信息
	 */
	private void UploadProfile() {
		// 此处应有菊花
		showToastProgressing("Updating...");
		RequestOperator.getInstance().UpdateProfile(
				mProfileItem.weight, 
				mProfileItem.height, 
				mProfileItem.language, 
				mProfileItem.ethnicity, 
				mProfileItem.religion, 
				mProfileItem.education, 
				mProfileItem.profession, 
				mProfileItem.income, 
				mProfileItem.children, 
				mProfileItem.smoke, 
				mProfileItem.drink, 
				mProfileItem.resume, 
				mProfileItem.interests.toArray(new String[mProfileItem.interests.size()]),
				new OnUpdateMyProfileCallback() {
					
					@Override
					public void OnUpdateMyProfile(boolean isSuccess, String errno,
							String errmsg, boolean rsModified) {
						// TODO Auto-generated method stub
						Message msg = Message.obtain();
						RequestBaseResponse obj = new RequestBaseResponse(isSuccess, errno, errmsg, null);
						if( isSuccess ) {
							// 上传个人信息成功
							msg.what = RequestFlag.REQUEST_UPDATE_PROFILE_SUCCESS.ordinal();
						} else {
							// 上传个人信息失败
							msg.what = RequestFlag.REQUEST_FAIL.ordinal();
						}
						msg.obj = obj;
						mHandler.sendMessage(msg);
					}
				});
	}
	
	/**
	 * 刷新界面
	 */
	private void ReloadData() {
		if( mProfileItem != null ) {
			/**
			 * 头像
			 */
			String url = mProfileItem.photoURL;
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(url);
			if (mProfileItem.showPhoto()){
				loader.DisplayImage(imageViewHeader, url, localPath, null);
				imageViewHeader.setVisibility(View.VISIBLE);
			}else{
				imageViewHeader.setVisibility(View.GONE);
			}
			
			/**
			 * 名称
			 */
			textViewName.setText(mProfileItem.firstname + " " + mProfileItem.lastname);
			
			/**
			 * 国家/年龄
			 */
			textViewAge.setText(mProfileItem.age + "");
			textViewCountry.setText(mProfileItem.country.name());
			
			/**
			 * 个人简介
			 */
			if (mProfileItem.resume != null && mProfileItem.resume.trim().length() > 0)textViewSelfInfo.setText(mProfileItem.resume);
			
			/**
			 * 兴趣爱好
			 */
			mList.clear();
			if( mProfileItem.interests != null ) {
				for( String item : mProfileItem.interests ) {
					mList.add(InterestToString(item));
				}
			}
			interestLabelAdapter.notifyDataSetChanged();
			
			/**
			 * 匹配说明
			 */
			textViewMatchCriteria.setText(MatchCriteriaToString());
			
			/**
			 * 详细资料项目
			 */
			layoutMemberId.textViewValue.setText(mProfileItem.manid.toUpperCase(Locale.ENGLISH));
			layoutHeight.textViewValue.setText(getResources().getStringArray(R.array.height)[mProfileItem.height.ordinal()]);
			layoutWeight.textViewValue.setText(getResources().getStringArray(R.array.weight)[mProfileItem.weight.ordinal()]);
			layoutSmoke.textViewValue.setText(getResources().getStringArray(R.array.smoke)[mProfileItem.smoke.ordinal()]);
			layoutDrink.textViewValue.setText(getResources().getStringArray(R.array.drink)[mProfileItem.drink.ordinal()]);
			layoutEducation.textViewValue.setText(getResources().getStringArray(R.array.education)[mProfileItem.education.ordinal()]);
			layoutProfession.textViewValue.setText(getResources().getStringArray(R.array.profression)[mProfileItem.profession.ordinal()]);
			layoutEthnicity.textViewValue.setText(getResources().getStringArray(R.array.ethnicity)[mProfileItem.ethnicity.ordinal()]);
			layoutReligion.textViewValue.setText(getResources().getStringArray(R.array.religion)[mProfileItem.religion.ordinal()]);
			layoutPrimaryLanguage.textViewValue.setText(getResources().getStringArray(R.array.language)[mProfileItem.language.ordinal()]);
			layoutHaveChildren.textViewValue.setText(getResources().getStringArray(R.array.children)[mProfileItem.children.ordinal()]);
			layoutCurrentIncome.textViewValue.setText(getResources().getStringArray(R.array.income)[mProfileItem.income.ordinal()]);
		}
		
		if( mProfileItem != null ) {
			setEnabledAll(rootView, true);
		} else {
			setEnabledAll(rootView, false);
		}
	}
	
	/**
	 * 获取兴趣爱好字符串
	 * @param name	兴趣爱好Id
	 * @return
	 */
	private String InterestToString(String name) {
		String result = "";
		switch (name) {
		case "1":{
			result = getResources().getString(R.string.my_profile_going_to_restaurants);
		}break;
		case "2":{
			result = getResources().getString(R.string.my_profile_cooking);
		}break;
		case "3":{
			result = getResources().getString(R.string.my_profile_travel);
		}break;
		case "4":{
			result = getResources().getString(R.string.my_profile_hiking_outdoor_activities);
		}break;
		case "5":{
			result = getResources().getString(R.string.my_profile_dancing);
		}break;
		case "6":{
			result = getResources().getString(R.string.my_profile_watching_movies);
		}break;
		case "7":{
			result = getResources().getString(R.string.my_profile_shopping);
		}break;
		case "8":{
			result = getResources().getString(R.string.my_profile_having_pets);
		}break;
		case "9":{
			result = getResources().getString(R.string.my_profile_reading);
		}break;
		case "10":{
			result = getResources().getString(R.string.my_profile_sports_exercise);
		}break;
		case "11":{
			result = getResources().getString(R.string.my_profile_playing_cards_chess);
		}break;
		case "12":{
			result = getResources().getString(R.string.my_profile_Music_play_instruments);
		}break;
		default:
			break;
		}
		return result;
	}
	
	/**
	 * 获取匹配条件女士简介字符串
	 * @return
	 */
	private String MatchCriteriaToString() {
		String result = "";
		
		if( mLadyMatch != null ) {
			// age
			String format = getResources().getString(R.string.my_profile_match_criteria_age);
			result += String.format(format, mLadyMatch.age1,  mLadyMatch.age2);
			result += " ";
			
			// children 
			switch (mLadyMatch.children) {
			case Yes:{
				result += getResources().getString(R.string.my_profile_match_criteria_children_yes);
			}break;
			default: {
				result += getResources().getString(R.string.my_profile_match_criteria_children_no);
			}break;
			}
			result += " ";
			
			// education
			switch (mLadyMatch.education) {
			case Unknow:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_height_school);
			}break;
			case SecondaryHighSchool:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_height_school);
			}break;
			case VocationalSchool:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_vocational_school);
			}break;
			case College:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_college);
			}break;
			case Bachelor:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_bachelor);
			}break;
			case Master:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_master);
			}break;
			case Doctorate:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_doctorate);
			}break;
			case PostDoctorate:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_post_doctorate);
			}break;
			default:{
				result += getResources().getString(R.string.my_profile_match_criteria_education_height_school);
			}break;
			}
			result += " ";
			
			// married
			switch (mLadyMatch.marry) {
			case Unknow:{
				result += getResources().getString(R.string.my_profile_match_criteria_married_single);
			}break;
			case NeverMarried:{
				result += getResources().getString(R.string.my_profile_match_criteria_married_single);
			}break;
			case Divorced:{
				result += getResources().getString(R.string.my_profile_match_criteria_married_divorced);
			}break;
			case Widowed:{
				result += getResources().getString(R.string.my_profile_match_criteria_married_widowed);
			}break;
			case Separated:{
				result += getResources().getString(R.string.my_profile_match_criteria_married_separated);
			}break;
			case Married:{
				result += getResources().getString(R.string.my_profile_match_criteria_married_married);
			}break;
			default:{
				result += getResources().getString(R.string.my_profile_match_criteria_married_single);
			}break;
			}
		}
		
		return result;
	}
}
