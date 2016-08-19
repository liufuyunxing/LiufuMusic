package com.liufu.guessmusic.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.liufu.guessmusic.R;
import com.liufu.guessmusic.data.Const;
import com.liufu.guessmusic.model.IAlertDialogButtonListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Util {

	private static AlertDialog mAlertDialog;

	public static View getView(Context context, int layoutId) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// LayoutParams params = new LayoutParams(-2, -2);
		View layout = inflater.inflate(layoutId, null);

		return layout;
	}

	public static void startActivity(Context context, Class desti) {
		Intent intent = new Intent(context, desti);
		context.startActivity(intent);

		// 关闭当前的activity
		((Activity) context).finish();
	}

	/**
	 * 定义自定义的对话框
	 * 
	 * @param context
	 * @param message
	 * @param listener
	 */
	public static void showDialog(final Context context, String message,
			final IAlertDialogButtonListener listener) {

		View dialogView = null;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		dialogView = getView(context, R.layout.dialog_view);

		ImageButton btnOkView = (ImageButton) dialogView
				.findViewById(R.id.btn_dialog_ok);
		ImageButton btnCalcelView = (ImageButton) dialogView
				.findViewById(R.id.btn_dialog_cancel);
		TextView textMessageView = (TextView) dialogView
				.findViewById(R.id.text_dialog_message);
		textMessageView.setText(message);
		btnOkView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 关闭显示对话框
				if (mAlertDialog != null) {
					mAlertDialog.cancel();
				}
				// 事件回调
				if (listener != null) {
					listener.onClick();
				}
				// 播放确定音效
				MyPlayer.playTone(context, MyPlayer.INDEX_STONE_ENTER);
			}
		});
		btnCalcelView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 关闭显示对话框
				if (mAlertDialog != null) {
					mAlertDialog.cancel();
				}

				// 播放取消音效
				MyPlayer.playTone(context, MyPlayer.INDEX_STONE_CANCEL);
			}
		});

		// 为对话框设置View
		builder.setView(dialogView);
		mAlertDialog = builder.create();

		// 显示对话框
		mAlertDialog.show();

	}

	/**
	 * 保存游戏数据
	 * 
	 * @param context
	 * @param stageIndex
	 * @param coins
	 */
	public static void saveData(Context context, int stageIndex, int coins) {
		FileOutputStream fis = null;

		try {
			fis = context.openFileOutput(Const.FILE_NAME_SAVE_DATA,
					Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(fis);

			dos.writeInt(stageIndex);
			dos.writeInt(coins);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 读取游戏数据
	 * 
	 * @param context
	 * @return
	 */
	public static int[] loadData(Context context) {

		FileInputStream fis = null;
		int[] datas = { -1, Const.TOTAL_COINS };

		try {
			fis = context.openFileInput(Const.FILE_NAME_SAVE_DATA);
			DataInputStream dis = new DataInputStream(fis);

			try {
				datas[Const.INDEX_LOAD_DATA_STAGE] = dis.readInt();
				datas[Const.INDEX_LOAD_DATA_COINS] = dis.readInt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return datas;
	}

}
