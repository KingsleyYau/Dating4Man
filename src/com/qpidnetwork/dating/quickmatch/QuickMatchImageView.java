package com.qpidnetwork.dating.quickmatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.qpidnetwork.framework.util.Log;

/**
 * QuickMatch模块
 * 可拖动旋转图片界面
 * @author Max.Chiu
 *
 */
@SuppressLint("RtlHardcoded")
public class QuickMatchImageView extends ImageView {

	static final int CLICK = 3;
    
	private enum MovekArea {
		LEFT,
		RIGHT,
		CENTER,
	}
	private MovekArea mMovekArea = MovekArea.CENTER;
		
	/**
	 * 象限枚举
	 * ********************
	 * *第二象限*	|	*第一象限*
	 * --------------------
	 * *第三象限*	|	*第四象限*
	 * ********************
	 */
	private enum ClickArea {
		FIRST,
		SECOND,
		THIRD,
		FOURTH,
	}
	/**
	 * 点击时候所在象限
	 */
	private ClickArea mClickArea = ClickArea.FIRST;
	
	/**
	 * 释放时候所在象限
	 */
	private ClickArea mUpArea = ClickArea.FIRST;
	
	/**
	 * 释放时候即时速度方向
	 */
//	private ClickArea mUpSpeed = ClickArea.FIRST;		
	
	private enum Mode {
		NONE,
		DRAG,
		RELEASE,
	}
	private Mode mMode;
    
	private Handler handler;
	 
	private Matrix matrix  = new Matrix();
	private Matrix matrixOrignal  = new Matrix();
    private PointF last = new PointF();
    private PointF start = new PointF();

    private int alpha;
    private int viewWidth, viewHeight;
//    private float origWidth, origHeight;
    private int oldMeasuredWidth, oldMeasuredHeight;
    
    private OnQuickMatchImageViewLinstener listener = null;
    
    public QuickMatchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public QuickMatchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        /* 获取布局大小 */
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        /**/
        if (oldMeasuredWidth == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0) {
            return;
        }
        
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;
        
        Log.d("QuickMatch.QuickMatchImageView", "onMeasure( " +
        		"viewWidth : " + String.valueOf(viewWidth) + ", " +
        		"viewHeight : " + String.valueOf(viewHeight) +
        		" )");
        
        /* 居中图片 */
        CenterImage();
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
    		int bottom) {
    	// TODO Auto-generated method stub
    	super.onLayout(changed, left, top, right, bottom);

    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	
//    	Drawable drawable = getDrawable();
//    	Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();  
//    	bitmap.setHasAlpha(true);
//    	
//    	canvas.drawBitmap(bitmap, matrix, null);
    	
    	super.onDraw(canvas);
    }
    
    public void SetOnQuickMatchImageViewLinstener(OnQuickMatchImageViewLinstener listener) {
    	this.listener = listener;
    }
    
    @SuppressWarnings("deprecation")
	public void CenterImage() {
    	if( mMode == Mode.RELEASE ) {
    		return;
    	}
    	
        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
            return;
        
        int bmWidth = drawable.getIntrinsicWidth();
        int bmHeight = drawable.getIntrinsicHeight();

        /* 等比长边放大*/
        float scale = 1.0f;
//        float scaleX = (float) viewWidth / (float) bmWidth;
//        float scaleY = (float) viewHeight / (float) bmHeight;
//        scale = Math.min(scaleX, scaleY);
//        matrix.setScale(scale, scale);
        

        /* 平移至居中 */
        float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
        float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
        redundantXSpace /= (float) 2;
        redundantYSpace /= (float) 2;
        matrix.setTranslate(redundantXSpace, redundantYSpace);
        matrixOrignal.set(matrix);
        		
        /* 放大后图片长宽 */
//        origWidth = viewWidth - 2 * redundantXSpace;
//        origHeight = viewHeight - 2 * redundantYSpace;
        
        alpha = 255;
        if( Build.VERSION.SDK_INT >= 16 ) {
            setImageAlpha(alpha);
        } else {
        	setAlpha(alpha);
        }
        
        setImageMatrix(matrix);
    }
    
    private void sharedConstructing(Context context) {
        super.setClickable(true);
        mMode = Mode.NONE;
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(matrix);

        handler = new Handler(){
        	@Override
            public void handleMessage(Message msg) {
                float[] values = new float[9];
                matrix.getValues(values);
                float transX = values[Matrix.MTRANS_X];
                float transY = values[Matrix.MTRANS_Y];
                
                if( Math.abs(transX) < viewWidth || Math.abs(transY) < viewHeight ) {
                    switch (mUpArea) {
    				case FIRST: {
    					transX = 50;
    					transY = -50;
    				}break;
    				case SECOND: {
    					transX = -50;
    					transY = -50;
    				}break;
    				case THIRD: {
    					transX = -50;
    					transY = 50;
    				}break;
    				case FOURTH: {
    					transX = 50;
    					transY = 50;
    				}break;
    				default:
    					break;
    				}

                	matrix.postTranslate(transX, transY);
                	handler.sendEmptyMessageDelayed(0, 10);
                    
                	setImageMatrix(matrix);
                    invalidate();
                } else {
                	mMode = Mode.NONE;
					if( listener != null ) {
						listener.OnMoveOut();
					}
                }
                
        		super.handleMessage(msg);
        	}
        };

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
        		
                PointF p = new PointF(event.getX(), event.getY());
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    	if( mMode == Mode.NONE ) {
                    		/* 边距20的位置不处理事件  */
                    		PointF point = getViewLocation(v);
//                    		if( (v.getX() + v.getWidth() - p.x) < 20 || 
//                    				(p.x - v.getX()) < 20 ) {
//                    			break;
//                    		}
                    		if ( (point.x + v.getWidth() - p.x) < 20 ||
                    				(p.x - point.x) < 20 ) {
                    			break;
                    		}
                    		
                    		(v.getParent()).requestDisallowInterceptTouchEvent(true);  
                            mMode = Mode.DRAG;
                            mMovekArea = MovekArea.CENTER;
                            
                            // 记录点击位置 
                        	last.set(p);
                            start.set(last);
                            
                            CenterImage();
                            
                            // 设置点击象限
                            SetClickPointArea(p);
                            
    						if( listener != null ) {
    							listener.OnDrag();
    						}
                    	}
                        
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mMode == Mode.DRAG) {
                          
                            float deltaX = p.x - last.x;
                            float deltaY = p.y - last.y;
                            last.set(p.x, p.y);
                            
                            matrix.postTranslate(deltaX, deltaY);
                            
                            switch (mClickArea) {
							case FIRST: {
								matrix.postRotate(deltaX / 30, (p.x + last.x) / 2, (p.y + last.y) / 2);
							}break;
							case SECOND: {
								matrix.postRotate(deltaX / 30, (p.x + last.x) / 2, (p.y + last.y) / 2);
							}break;
							case THIRD: {
								matrix.postRotate(-deltaX / 30, (p.x + last.x) / 2, (p.y + last.y) / 2);
							}break;
							case FOURTH: {
								matrix.postRotate(-deltaX / 30, (p.x + last.x) / 2, (p.y + last.y) / 2);
							}break;
							default:
								break;
							}
                            
                            float[] values = new float[9];
                            matrix.getValues(values);
                            float[] valuesOrignal = new float[9]; 
                            matrixOrignal.getValues(valuesOrignal);
                            float transX = values[Matrix.MTRANS_X] - valuesOrignal[Matrix.MTRANS_X];
                            
                            /*alpha = (int)(255 * (viewWidth - Math.abs(transX)) / viewWidth);
                            if( Build.VERSION.SDK_INT >= 16 ) {
                                setImageAlpha(alpha);
                            } else {
                            	setAlpha(alpha);
                            }*/
                            
                            // 回调左右拖动事件
                            if( Math.abs(transX) > viewWidth / 10 ) {
                                if( transX < 0 ) {
                                	if( mMovekArea != MovekArea.LEFT ) {
                                		mMovekArea = MovekArea.LEFT;
	        							if( listener != null ) {
	        								listener.OnMoveLeft();
	        							}
                                	}
                                } else {
                                	if( mMovekArea != MovekArea.RIGHT ) {
                                		mMovekArea = MovekArea.RIGHT;
                                		if( listener != null ) {
                                			listener.OnMoveRight();
                                		}
                                	}
                                }
                            } else {  
                            	if( mMovekArea != MovekArea.CENTER ) {
                            		mMovekArea = MovekArea.CENTER;
	    							if( listener != null ) {
	    								listener.OnMoveCenter();
	    							}
                            	}
                            }
                            
                            // 重新计算释放时候即时方向
//                            if( deltaX > 0 && deltaY < 0 ) {
//                            	mUpSpeed = ClickArea.FIRST;
//                            } else if( deltaX > 0 && deltaY > 0 ) {
//                            	mUpSpeed = ClickArea.SECOND;
//                            } else if( deltaX < 0 && deltaY > 0 ) {
//                            	mUpSpeed = ClickArea.THIRD;
//                            } else if( deltaX < 0 && deltaY < 0 ) {
//                            	mUpSpeed = ClickArea.FOURTH;
//                            }
                            
                            // 重新计算释放象限
                            mUpArea = GetImageArea();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    	if (mMode == Mode.DRAG) {
                           	(v.getParent()).requestDisallowInterceptTouchEvent(false);
                            int xDiff = (int) Math.abs(p.x - start.x);
                            int yDiff = (int) Math.abs(p.y - start.y);
                            
                            // 单击 
                            if (xDiff < CLICK && yDiff < CLICK) {
                                performClick();
                            }
                            
                            float[] values = new float[9];
                            matrix.getValues(values);
                            float[] valuesOrignal = new float[9]; 
                            matrixOrignal.getValues(valuesOrignal);
                            float transX = values[Matrix.MTRANS_X] - valuesOrignal[Matrix.MTRANS_X];
                            
                            if( Math.abs(transX) < viewWidth / 10 ) {
                            	mMode = Mode.NONE;
                                // 还原图像位置
                                CenterImage();
                                
                                if( listener != null ) {
                                	listener.OnRestore();
                                }
                                
                            } else {
                            	// 移走
                            	mMode = Mode.RELEASE;
                            	handler.sendEmptyMessage(0);
                            	
                            	// 回调左右拖动并且释放事件
                            	
                            	// 根据最后所在象限
                            	switch ( mMovekArea ) {
								case LEFT:{
        							if( listener != null ) {
        								listener.OnReleaseLeft();
        							}
								}break;
								case RIGHT:{
        							if( listener != null ) {
        								listener.OnReleaseRight();
        							}
								}break;
								default:
									break;
								}
                            	
                            	// 根据最后即时速度方向
//                                switch (mUpSpeed) {
//        						case FIRST: {
//
//        						}
//        						case THIRD: {
//        							if( listener != null ) {
//        								listener.OnReleaseLeft();
//        							}
//        						}break;
//        						case SECOND: {
//
//        						}
//        						case FOURTH: {
//        							if( listener != null ) {
//        								listener.OnReleaseRight();
//        							}
//        						}break;
//        						default:
//        							break;
//        						}
                    		}
                    	}
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mMode = Mode.NONE;
                        break;
                }
                
                setImageMatrix(matrix);
                invalidate();
                return true;
            }

        });
    }

    private void SetClickPointArea(PointF p) {
    	float y0 = viewHeight / 2;
    	float x0 = viewWidth / 2;
    	
    	if( p.x > x0 && p.y < y0 ) {
    		 // 第一象限 
    		mClickArea = ClickArea.FIRST;
    	} else if( p.x < x0 && p.y < y0 ){
    		// 第二象限
    		mClickArea = ClickArea.SECOND;
    	} else if( p.x < x0 && p.y > y0 ){
    		// 第三象限
    		mClickArea = ClickArea.THIRD;
    	} else if( p.x > x0 && p.y > y0 ){
    		// 第四象限
    		mClickArea = ClickArea.FOURTH;
    	}
    }
    
    public ClickArea GetImageArea() {
    	ClickArea area = ClickArea.FIRST;
    	
        float[] values = new float[9];
        matrix.getValues(values);
        float[] valuesOrignal = new float[9]; 
        matrixOrignal.getValues(valuesOrignal);
        float transX = values[Matrix.MTRANS_X] - valuesOrignal[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y] - valuesOrignal[Matrix.MTRANS_Y];
        
    	if( transX > 0 && transY < 0 ) {
   		 // 第一象限 
    		area = ClickArea.FIRST;
	   	} else if( transX < 0 && transY < 0 ){
	   		// 第二象限
	   		area = ClickArea.SECOND;
	   	} else if( transX < 0 && transY > 0 ){
	   		// 第三象限
	   		area = ClickArea.THIRD;
	   	} else if( transX > 0 && transY > 0 ){
	   		// 第四象限
	   		area = ClickArea.FOURTH;
	   	}
        
        return area;
    }
    
    /**
     * 获取View的getX()和getY()值
     * @param v
     * @return
     */
    private PointF getViewLocation(View v)
    {
    	int []location = {0, 0};
    	v.getLocationOnScreen(location);
    	PointF point = new PointF();
    	point.x = location[0];
    	point.y = location[1];
//    	point.x = v.getX();
//    	point.y = v.getY();
    	
    	return point;
    }
}