package com.liufu.guessmusic.myui;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.liufu.guessmusic.R;
import com.liufu.guessmusic.model.IWorkButtonClickListener;
import com.liufu.guessmusic.model.WordButton;
import com.liufu.guessmusic.util.Util;

public class MyGridView extends GridView {
	public final static int COUNTS_WORDS = 24;

	private ArrayList<WordButton> mArrayList = new ArrayList<WordButton>();

	private MyGridAdapter mAdapter;

	private Context mContext;

	// 初始化动画
	private Animation mSacleAnimation;

	// 定义监听器
	private IWorkButtonClickListener mWorkButtonClickListener;

	public MyGridView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		mContext = context;

		mAdapter = new MyGridAdapter();
		this.setAdapter(mAdapter);
	}

	public void updateData(ArrayList<WordButton> list) {
		mArrayList = list;

		// 綁定數據源
		setAdapter(mAdapter);
	}

	class MyGridAdapter extends BaseAdapter {
		public int getCount() {
			return mArrayList.size();
		}

		public Object getItem(int pos) {
			return mArrayList.get(pos);
		}

		public long getItemId(int pos) {
			return pos;
		}

		public View getView(int pos, View v, ViewGroup p) {
			final WordButton holder;

			if (v == null) {
				v = Util.getView(mContext, R.layout.self_ui_gridview_item);

				holder = mArrayList.get(pos);

				// 加载动画
				mSacleAnimation = AnimationUtils.loadAnimation(mContext,
						R.anim.scale);
				mSacleAnimation.setStartOffset(pos * 100);

				holder.mIndex = pos;
				holder.mViewButton = (Button) v.findViewById(R.id.item_btn);
				holder.mViewButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						mWorkButtonClickListener.onWorkButtonClick(holder);
					}
				});
				v.setTag(holder);
			} else {
				holder = (WordButton) v.getTag();
			}

			v.startAnimation(mSacleAnimation);
			holder.mViewButton.setText(holder.mWordString);

			return v;
		}
	}

	/**
	 * 注册监听器
	 */
	public void registerWorkButtonClick(IWorkButtonClickListener listener) {
		mWorkButtonClickListener = listener;
	}
}
