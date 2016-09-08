package com.hy.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;

public class RefreshListView extends ListView {

	int refreshableHeight;
	float density;
	View refreshHeader;
	View loadFooter;
	float offsetY;
	RefreshTask refreshTask;
	LoadTask loadTask;
	boolean isUpdated = false;
	boolean isRecovered = false;
	// int[] winMsgLocation;
	// int[] listLocation;
	// LayoutParams params = loading.getLayoutParams();
	// int initialHeight = params.height;
	float[] location;
	float[] footerLocation;
	ViewGroup.LayoutParams refreshHeaderParams;
	ViewGroup.LayoutParams loadFooterParams;
	int mMotionPosition;

	public RefreshListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		getDensity(context);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		getDensity(context);
	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		getDensity(context);
	}

	@Override
	public void addHeaderView(View v) {
		// TODO Auto-generated method stub
		// 第一个header作为refresh view
		if (getHeaderViewsCount() == 0) {
			refreshHeader = v;
			refreshHeader.setVisibility(View.GONE);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			FrameLayout frame = new FrameLayout(getContext());
			frame.setLayoutParams(params);
			frame.addView(v);
			super.addHeaderView(frame);
			return;
		}
		super.addHeaderView(v);
	}

	@Override
	public void addFooterView(View v) {
		// TODO Auto-generated method stub
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		FrameLayout frame = new FrameLayout(getContext());
		frame.setLayoutParams(params);
		frame.addView(v);
		super.addFooterView(frame);
		if (loadFooter == null) {
			LayoutParams footerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			FrameLayout footer = new FrameLayout(getContext());
			footer.setLayoutParams(footerParams);
			loadFooter = footer;
			loadFooter.setVisibility(View.GONE);
			super.addFooterView(footer);
		} else {
			removeFooterView(loadFooter);
			super.addFooterView(loadFooter);
		}

	}

	@Override
	public boolean removeHeaderView(View v) {
		// TODO Auto-generated method stub
		boolean isSuccess = super.removeHeaderView(v);
		if (isSuccess && refreshHeader != null && refreshHeader == v) {
			if (getHeaderViewsCount() == 0) {
				refreshHeader = null;
			}

		}
		return isSuccess;
	}

	/**
	 * Set refreshable height for the refreshHeader; Only when the height of
	 * refreshHeader >= refreshableHeight, the refresh task will work.
	 * 
	 * @param height
	 *            the refreshable height
	 */
	public void setRefreshableHeight(int height) {
		this.refreshableHeight = (int) (height * this.density);
	}

	/**
	 * Get the header view for refreshing work. Using the listView's first
	 * child(the first header) as the refresh view, so when calling the
	 * addHeaderView(View), first put the refresh view into the header group
	 * 
	 * @return the refresh view
	 */
	public View getRefreshHeader() {
		return refreshHeader;
	}

	public void setRefreshTask(RefreshTask task) {
		this.refreshTask = task;
	}

	public void setLoadTask(LoadTask task) {
		this.loadTask = task;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub

		if (this.isEnabled() && refreshHeader != null) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				if (getFirstVisiblePosition() == 0) {
					mMotionPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
					if (location == null) {
						location = new float[2];
						location[1] = ev.getY();
					}
				}
				if (loadFooter != null && getLastVisiblePosition() == getAdapter().getCount() - 1) {
					if (footerLocation == null) {
						footerLocation = new float[2];
						footerLocation[1] = ev.getY();
					}
				}
			} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
				if (this.getFirstVisiblePosition() == 0) {
					// realize header scroll loading function
					if (footerLocation != null) {
						footerLocation = null;
					}
					if (refreshHeaderParams == null) {
						refreshHeaderParams = refreshHeader.getLayoutParams();
					}
					if (location == null) {
						offsetY = 0;
					} else {
						offsetY = ev.getY() - location[1];
					}
					if (offsetY > 0) {
						// int height = refreshHeader.getMeasuredHeight();
						float exceptedHeight = refreshHeaderParams.height < 0 ? 0
								: refreshHeaderParams.height + offsetY;
						if (exceptedHeight < 100 * density) {
							refreshHeaderParams.height = (int) (refreshHeaderParams.height + offsetY * 2 / 3);
							refreshHeader.setLayoutParams(refreshHeaderParams);
						} else if (exceptedHeight < 150 * density) {
							refreshHeaderParams.height = (int) (refreshHeaderParams.height
									+ offsetY * (250 * density - exceptedHeight) / (150 * density) * 2 / 3);
							refreshHeader.setLayoutParams(refreshHeaderParams);
						} else {
							refreshHeaderParams.height = (int) (150 * density);
							refreshHeader.setLayoutParams(refreshHeaderParams);
						}

						if (refreshHeader.getVisibility() == View.GONE) {
							refreshHeader.setVisibility(View.VISIBLE);
						}
						if (this.isPressed()) {
							this.setPressed(false);
							View motionView = getChildAt(mMotionPosition - getFirstVisiblePosition());
							if (motionView != null && motionView.isPressed()) {
								motionView.setPressed(false);
							}
						}
						location[1] = ev.getY();
						return true;
					} else if (offsetY < 0) {
						if (refreshHeader.getVisibility() != View.GONE) {
							// int height = refreshHeader.getMeasuredHeight();
							float exceptedHeight = refreshHeaderParams.height + offsetY;
							if (exceptedHeight > 50 * density) {
								refreshHeaderParams.height = (int) (refreshHeaderParams.height + offsetY * 2 / 3);
								refreshHeader.setLayoutParams(refreshHeaderParams);
							} else if (exceptedHeight > 0) {
								refreshHeaderParams.height = (int) (refreshHeaderParams.height
										+ offsetY * (100 * density + exceptedHeight) / (150 * density) * 2 / 3);
								refreshHeader.setLayoutParams(refreshHeaderParams);
							} else {
								refreshHeaderParams.height = 0;
								refreshHeader.setLayoutParams(refreshHeaderParams);
								refreshHeader.setVisibility(View.GONE);
							}
							if (this.isPressed()) {
								this.setPressed(false);
								View motionView = getChildAt(mMotionPosition - getFirstVisiblePosition());
								if (motionView != null && motionView.isPressed()) {
									motionView.setPressed(false);
								}
							}
							location[1] = ev.getY();
							return true;
						}
					} else {
						if (location == null) {
							location = new float[2];
						} else if (refreshHeader.getVisibility() != View.GONE) {
							return true;
						}
					}
					location[1] = ev.getY();
				} else if (loadFooter != null && getLastVisiblePosition() == getAdapter().getCount() - 1) {
					// realize the footer scroll loading function
					if (location != null) {
						location = null;
					}
					if (loadFooterParams == null) {
						loadFooterParams = loadFooter.getLayoutParams();
					}
					if (footerLocation == null) {
						offsetY = 0;
					} else {
						offsetY = ev.getY() - footerLocation[1];
					}
					if (offsetY < 0) {
						// int height = refreshHeader.getMeasuredHeight();
						float exceptedHeight = loadFooterParams.height < 0 ? 0 : loadFooterParams.height - offsetY;
						if (exceptedHeight > 50 * density && loadTask != null && !isUpdated) {
							loadTask.updateDisplay(true);
							isUpdated = true;
							isRecovered = false;
						}
						loadFooterParams.height = (int) (exceptedHeight);
						loadFooter.setLayoutParams(loadFooterParams);

						if (loadFooter.getVisibility() == View.GONE) {
							loadFooter.setVisibility(View.VISIBLE);
						}
						footerLocation[1] = ev.getY();
					} else if (offsetY > 0) {
						if (loadFooter.getVisibility() != View.GONE) {
							// int height = refreshHeader.getMeasuredHeight();
							float exceptedHeight = loadFooterParams.height - offsetY;
							if (exceptedHeight < 50 * density) {
								if (loadTask != null && !isRecovered) {
									loadTask.updateDisplay(false);
									isRecovered = true;
									isUpdated = false;
								}
							}

							if (exceptedHeight > 0) {
								loadFooterParams.height = (int) (exceptedHeight);
								loadFooter.setLayoutParams(loadFooterParams);
							} else {
								loadFooterParams.height = 0;
								loadFooter.setLayoutParams(loadFooterParams);
								loadFooter.setVisibility(View.GONE);
							}
							footerLocation[1] = ev.getY();
						}
					} else {
						if (footerLocation == null) {
							footerLocation = new float[2];
						}
					}
					footerLocation[1] = ev.getY();
				} else {
					if (location != null) {
						location = null;
					}
					if (footerLocation != null) {
						footerLocation = null;
						isRecovered = false;
						isUpdated = false;
					}
				}

			} else if ((ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP)) {
				if (location != null) {
					location = null;
				}
				if (footerLocation != null) {
					footerLocation = null;
					isRecovered = false;
					isUpdated = false;
				}
				if (refreshHeader.getVisibility() != View.GONE) {
					ViewGroup.LayoutParams params = refreshHeader.getLayoutParams();
					if (params.height >= refreshableHeight) {
						// 通知做更新的一系列操作
						if (refreshTask != null) {
							this.refreshTask.handleRefreshTask();
							return true;
						}
					}
					params.height = 0;
					refreshHeader.setLayoutParams(params);
					refreshHeader.setVisibility(View.GONE);
					return true;
				} else if (loadFooter != null && loadFooter.getVisibility() != View.GONE) {
					ViewGroup.LayoutParams params = loadFooter.getLayoutParams();
					if (params.height > 50 * density && loadFooter.getBottom() >= this.getMeasuredHeight()) {
						if (loadTask != null) {
							loadTask.handleLoadTask();
							// loadTask.updateDisplay(false);
							// return super.onTouchEvent(ev);
						}
					}
					params.height = 0;
					loadFooter.setLayoutParams(params);
					loadFooter.setVisibility(View.GONE);
					if (loadTask != null) {
						loadTask.updateDisplay(false);
					}
				}

			}
		}
		return super.onTouchEvent(ev);
	}

	public float getDensity(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		this.density = dm.density;
		return this.density;
	}

	public interface RefreshTask {
		public abstract void handleRefreshTask();
	}

	public interface LoadTask {
		public abstract void handleLoadTask();

		/**
		 * @param true
		 *            when update, false when recover
		 */
		public abstract void updateDisplay(boolean updateOrRecover);
	}
}
