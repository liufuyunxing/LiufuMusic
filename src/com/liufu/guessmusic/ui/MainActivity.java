package com.liufu.guessmusic.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.PrivateCredentialPermission;

import android.R.bool;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.WorkSource;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.liufu.guessmusic.R;
import com.liufu.guessmusic.data.Const;
import com.liufu.guessmusic.model.IAlertDialogButtonListener;
import com.liufu.guessmusic.model.IWorkButtonClickListener;
import com.liufu.guessmusic.model.Song;
import com.liufu.guessmusic.model.WordButton;
import com.liufu.guessmusic.myui.MyGridView;
import com.liufu.guessmusic.util.MyLog;
import com.liufu.guessmusic.util.MyPlayer;
import com.liufu.guessmusic.util.Util;

public class MainActivity extends Activity implements IWorkButtonClickListener {

	public static final String TAG = "MainActivity";

	/** 答案狀態 -- 正確 */
	public static final int STATUS_ANSWER_RIGHT = 1;

	/** 答案狀態 -- 錯誤 */
	public static final int STATUS_ANSWER_ERROR = 2;

	/** 答案狀態 -- 不完整 */
	public static final int STATUS_ANSWER_LACK = 3;

	public static final int ID_DIALOG_DELETE_WORD = 1;

	public static final int ID_DIALOG_TIP_ANSWER = 2;

	public static final int ID_DIALOG_LACK_COINS = 3;

	public static final int SPASH_TIMES = 6;
	// 相關動畫
	private Animation mPanAnim;
	private LinearInterpolator mPanLin;

	private Animation mBarInAnim;
	private LinearInterpolator mBarInLin;

	private Animation mBarOutAnim;
	private LinearInterpolator mBarOutLin;

	// 盘面
	private ImageView mViewPan;
	// 拨杆
	private ImageView mViewPanBar;

	// Play 开始播放按钮
	private ImageButton mBtnPlayStart;

	// 当前关信息
	private TextView mCurrentStagePassView;

	// 歌曲名称
	private TextView mCurrentSongNamePassView;

	// 過關界面
	private View mPassView;

	// 判断盘面是否正在转动的标志
	private boolean mIsRunning = false;

	// 文字框容器
	private ArrayList<WordButton> mAllWords;

	private ArrayList<WordButton> mBtnSelectWords;

	private MyGridView mMyGridView;

	// 已选文字框UI容器
	private LinearLayout mViewWordsContainer;

	// 当前歌曲
	private Song mCurrentSong;

	// 当前关的索引
	private int mCurrentStageIndex = -1;

	// 当前的金币数量
	private int mCurrentCoins = Const.TOTAL_COINS;

	// 金币View
	private TextView mViewCurrentCoins;

	// 显示当前关索引的View
	private TextView mCurrentStageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//读取数据
		
		int[] data = Util.loadData(MainActivity.this);
		mCurrentStageIndex = data[Const.INDEX_LOAD_DATA_STAGE];
		mCurrentCoins = data[Const.INDEX_LOAD_DATA_COINS];

		// 鍒濆鍖栨帶浠�
		mViewPan = (ImageView) findViewById(R.id.imageView1);
		mViewPanBar = (ImageView) findViewById(R.id.imageView2);

		mMyGridView = (MyGridView) findViewById(R.id.gridview);
		mMyGridView.registerWorkButtonClick(this);

		mViewWordsContainer = (LinearLayout) findViewById(R.id.word_select_container);

		mViewCurrentCoins = (TextView) findViewById(R.id.txt_bar_coins);
		mViewCurrentCoins.setText(mCurrentCoins + "");

		mCurrentStageView = (TextView) findViewById(R.id.text_current_stage);

		// 鍒濆鍖栧姩鐢�
		mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
		mPanLin = new LinearInterpolator();
		mPanAnim.setInterpolator(mPanLin);
		mPanAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 寮�鍚嫧鏉嗛��鍑哄姩鐢�
				mViewPanBar.startAnimation(mBarOutAnim);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
		mBarInLin = new LinearInterpolator();
		mBarInAnim.setFillAfter(true);
		mBarInAnim.setInterpolator(mBarInLin);
		mBarInAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 寮�濮嬪敱鐗囧姩鐢�
				mViewPan.startAnimation(mPanAnim);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
		mBarOutLin = new LinearInterpolator();
		mBarOutAnim.setFillAfter(true);
		mBarOutAnim.setInterpolator(mBarOutLin);
		mBarOutAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 鏁村鍔ㄧ敾鎾斁瀹屾瘯
				mIsRunning = false;
				mBtnPlayStart.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mBtnPlayStart = (ImageButton) findViewById(R.id.btn_play_start);
		mBtnPlayStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				handlePlayButton();
			}
		});

		// 初始化游戏数据
		initCurrentStageData();

		// 增加删除按键逻辑
		handleDeleteWord();

		// 处理提示按键
		handleTipWord();

	}

	/**
     * 
     */
	private void handlePlayButton() {
		if (mViewPanBar != null) {
			if (!mIsRunning) {
				mIsRunning = true;

				mViewPanBar.startAnimation(mBarInAnim);
				mBtnPlayStart.setVisibility(View.INVISIBLE);

				// 播放音乐
				MyPlayer.playSong(MainActivity.this,
						mCurrentSong.getSongFileName());
			}
		}
	}

	@Override
	public void onPause() {
		//保存游戏
		Util.saveData(MainActivity.this, mCurrentStageIndex-1, mCurrentCoins);
		mViewPan.clearAnimation();
		// 停止音乐
		MyPlayer.stopThePlay(MainActivity.this);
		super.onPause();
	}

	/**
	 * 读取当前关的信息
	 */
	private Song loadStageSongInfo(int stageIndex) {
		Song song = new Song();

		String[] stage = Const.SONG_INFO[stageIndex];

		song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
		song.setSongName(stage[Const.INDEX_SONG_NAME]);

		return song;
	}

	/**
	 * 加载当前关卡的数据
	 */
	private void initCurrentStageData() {

		// 读取当前关的信息
		mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);
		// 初始化已選文字框
		mBtnSelectWords = initWordSelect();

		LayoutParams params = new LayoutParams(-2, -2);
		// 清除答案框
		mViewWordsContainer.removeAllViews();

		// 增加新的答案
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton,
					params);

		}
		if (mCurrentStageView != null) {
			// 显示当前索引
			mCurrentStageView.setText((mCurrentStageIndex + 1) + "");

		}

		// 獲取數據
		mAllWords = initAllWord();
		// 更新數據- MyGridView
		mMyGridView.updateData(mAllWords);

		// 一開始就播放音樂
		handlePlayButton();
	}

	/**
	 * 初始化待选文字框
	 */
	private ArrayList<WordButton> initAllWord() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();

		// 獲得所有的待選文字
		String[] words = generateWords();

		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			WordButton button = new WordButton();

			button.mWordString = words[i];

			data.add(button);
		}

		return data;
	}

	/**
	 * 初始化已選擇文字框
	 * 
	 * @return
	 */
	private ArrayList<WordButton> initWordSelect() {

		ArrayList<WordButton> data = new ArrayList<WordButton>();
		for (int i = 0; i < mCurrentSong.getSongLength(); i++) {
			View view = Util.getView(MainActivity.this,
					R.layout.self_ui_gridview_item);

			final WordButton holder = new WordButton();
			holder.mViewButton = (Button) view.findViewById(R.id.item_btn);
			holder.mViewButton.setTextColor(Color.WHITE);
			holder.mViewButton.setText("");
			holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
			holder.mIsVisiable = false;
			holder.mViewButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					clearTheAnswer(holder);
				}
			});

			data.add(holder);

		}
		return data;
	}

	@Override
	public void onWorkButtonClick(WordButton wordButton) {
		// Toast.makeText(this,wordButton.mIndex+"", Toast.LENGTH_LONG).show();
		setSelectWord(wordButton);

		// 獲得答案狀態
		int checkResult = checkTheAnswer();

		// 檢查答案
		if (checkResult == STATUS_ANSWER_RIGHT) {
			// 過關並獲得獎勵
			handlePassEvent();
		} else if (checkResult == STATUS_ANSWER_ERROR) {
			// 閃爍並提示錯誤
			sparkTheWords();
		} else if (checkResult == STATUS_ANSWER_LACK) {
			// 答案缺失的時候，顏色爲白色
			for (int i = 0; i < mBtnSelectWords.size(); i++) {
				mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);
			}
		}
	}

	/**
	 * 處理過關界面及事件
	 * 
	 * @param wordButton
	 */
	private void handlePassEvent() {
		// 显示过关页面
		mPassView = (LinearLayout) findViewById(R.id.pass_view);
		mPassView.setVisibility(View.VISIBLE);

		// 停止未完成的动画
		mViewPan.clearAnimation();

		// 停止正在播放的音樂
		MyPlayer.stopThePlay(MainActivity.this);

		// 播放音效
		MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_STONE_COIN);

		// 当前关的索引
		mCurrentStagePassView = (TextView) findViewById(R.id.text_current_stage_pass);
		mCurrentStagePassView.setText((mCurrentStageIndex + 1) + "");

		// 显示歌曲的名称
		mCurrentSongNamePassView = (TextView) findViewById(R.id.text_current_stage_pass_name);
		if (mCurrentSongNamePassView != null) {

			mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
		}
		// 下一关按键处理
		ImageButton btnPass = (ImageButton) findViewById(R.id.btn_next);
		btnPass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (judgeAppPassed()) {
					// 进入通关界面
					Util.startActivity(MainActivity.this, AllPassView.class);
				} else {
					// 进入下一关
					mPassView.setVisibility(View.GONE);
					// 加载关卡数据
					initCurrentStageData();
				}
			}
		});
	}

	/**
	 * 判断是否通关
	 * 
	 * @param wordButton
	 */
	private boolean judgeAppPassed() {
		return (mCurrentStageIndex == Const.SONG_INFO.length - 1);
	}

	private void clearTheAnswer(WordButton wordButton) {
		wordButton.mViewButton.setText("");
		wordButton.mIsVisiable = false;
		wordButton.mWordString = "";

		// 設置待選框可見性
		setButtonVisible(mAllWords.get(wordButton.mIndex), View.VISIBLE);

	}

	/**
	 * 设置答案选择框
	 */
	private void setSelectWord(WordButton wordButton) {
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).mWordString.length() == 0) {
				// 设置文字框内容及可见性
				mBtnSelectWords.get(i).mViewButton
						.setText(wordButton.mWordString);
				mBtnSelectWords.get(i).mIsVisiable = true;
				mBtnSelectWords.get(i).mWordString = wordButton.mWordString;
				// 记录索引
				mBtnSelectWords.get(i).mIndex = wordButton.mIndex;

				// Log......
				MyLog.d(TAG, mBtnSelectWords.get(i).mIndex + "");
				// 设置待选框文字可见性
				setButtonVisible(wordButton, View.INVISIBLE);
				break;
			}
		}
	}

	/**
	 * 设置待选框文字可见性
	 */
	private void setButtonVisible(WordButton button, int visibility) {
		button.mViewButton.setVisibility(visibility);
		button.mIsVisiable = (visibility == View.VISIBLE) ? true : false;

		// Log
		MyLog.d(TAG, button.mIsVisiable + "");
	}

	/**
	 * 生成所有待選文字
	 */
	private String[] generateWords() {
		String[] words = new String[MyGridView.COUNTS_WORDS];
		Random random = new Random();
		// 存入歌名
		for (int i = 0; i < mCurrentSong.getSongLength(); i++) {
			words[i] = mCurrentSong.getNameCharacters()[i] + "";
		}
		// 獲取待選隨機文字
		for (int i = mCurrentSong.getSongLength(); i < MyGridView.COUNTS_WORDS; i++) {
			words[i] = getRandomChar() + "";
		}
		// 打亂文字順序
		for (int i = MyGridView.COUNTS_WORDS - 1; i >= 0; i--) {
			int index = random.nextInt(i + 1);

			String buf = words[index];
			words[index] = words[i];
			words[i] = buf;
		}
		return words;
	}

	/**
	 * 生成随机汉字
	 * 
	 * @throws
	 */

	private char getRandomChar() {

		String str = "";

		int hightPos;
		int lowPos;

		Random random = new Random();

		hightPos = 176 + Math.abs(random.nextInt(39));
		lowPos = 161 + Math.abs(random.nextInt(87));

		byte[] b = new byte[2];

		b[0] = (Integer.valueOf(hightPos)).byteValue();
		b[1] = (Integer.valueOf(lowPos)).byteValue();
		try {
			str = new String(b, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str.charAt(0);
	}

	/**
	 * 檢查答案
	 */
	private int checkTheAnswer() {
		// 先檢查完整性
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			// 如果有空，說明答案不完整
			if (mBtnSelectWords.get(i).mWordString.length() == 0) {
				return STATUS_ANSWER_LACK;
			}
		}
		// 答案完整，繼續檢查答案正確性
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			str.append(mBtnSelectWords.get(i).mWordString);
		}
		return (str.toString().equals(mCurrentSong.getSongName())) ? STATUS_ANSWER_RIGHT
				: STATUS_ANSWER_ERROR;
	}

	/**
	 * 文字閃爍
	 */
	private void sparkTheWords() {
		// 定時器相關
		TimerTask task = new TimerTask() {
			boolean mChange = false;
			int mSpardTime = 0;

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					public void run() {
						if (++mSpardTime > SPASH_TIMES) {
							return;
						}

						for (int i = 0; i < mBtnSelectWords.size(); i++) {
							mBtnSelectWords.get(i).mViewButton
									.setTextColor(mChange ? Color.RED
											: Color.WHITE);
						}
						mChange = !mChange;
					}
				});

			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 1, 150);
	}

	/**
	 * 自动选择一个答案
	 */
	private void tipAnswer() {

		boolean tipword = false;

		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).mWordString.length() == 0) {
				// 根据当前的答案选择框填入相对应的文字
				onWorkButtonClick(findIsAnswerWord(i));

				tipword = true;

				// 减少金币
				if (!handleCoins(-getTipWordCoins())) {
					// 金币不够，显示提示对话框
					showConfimDialog(ID_DIALOG_LACK_COINS);
					return;
				}
				break;
			}

		}
		// 没有找到要填充的答案
		if (!tipword) {
			// 闪烁文字提示用户
			sparkTheWords();
		}

	}

	/**
	 * 找到一个答案文字
	 * 
	 * @param index
	 *            当前需要填入答案的索引
	 * @return
	 */
	private WordButton findIsAnswerWord(int index) {
		WordButton buf = null;

		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			buf = mAllWords.get(i);
			if (buf.mWordString.equals(""
					+ mCurrentSong.getNameCharacters()[index])) {
				return buf;
			}
		}
		return null;
	}

	/**
	 * 删除文字
	 */
	private void deleteOneWord() {
		// 减少金币
		if (!handleCoins(-getDeleteWordCoins())) {
			// 金币不够，显示提示对话框
			showConfimDialog(ID_DIALOG_LACK_COINS);
			return;
		}
		// 将这个索引对应的WorkButton设置为不可见
		setButtonVisible(findNotAnswerWord(), View.INVISIBLE);
	}

	/**
	 * 找到一个不是答案的文件，并且文件是可见的
	 */
	private WordButton findNotAnswerWord() {
		Random random = new Random();
		WordButton buf = null;

		while (true) {
			int index = random.nextInt(MyGridView.COUNTS_WORDS);

			buf = mAllWords.get(index);
			if (buf.mIsVisiable && !isTheAnswerWord(buf)) {
				return buf;
			}
		}

	}

	/**
	 * 判断某个文字是否为答案
	 * 
	 * @param word
	 * @return
	 */
	private boolean isTheAnswerWord(WordButton word) {
		boolean result = false;

		for (int i = 0; i < mCurrentSong.getSongLength(); i++) {
			if (word.mWordString.equals(""
					+ mCurrentSong.getNameCharacters()[i])) {
				result = true;

				break;
			}
		}
		return result;
	}

	/**
	 * 增加或減少指定的金幣數量
	 * 
	 * @param data
	 * @return true 增加/減少成功 false失敗
	 */
	private boolean handleCoins(int data) {
		// 判斷當前的金幣數量是否可以被減少
		if (mCurrentCoins + data >= 0) {
			mCurrentCoins += data;

			mViewCurrentCoins.setText(mCurrentCoins + "");
			return true;
		} else {
			// 金幣不夠
			return false;
		}
	}

	/**
	 * 从资源文件读取删除操作所要用的金币
	 * 
	 * @return
	 */
	private int getDeleteWordCoins() {
		return this.getResources().getInteger(R.integer.pay_delete_answer);
	}

	/**
	 * 提示操作所要用的金币
	 * 
	 * @return
	 */
	private int getTipWordCoins() {
		return this.getResources().getInteger(R.integer.pay_tip_answer);
	}

	/**
	 * 处理删除待选文字事件
	 */
	private void handleDeleteWord() {
		ImageButton button = (ImageButton) findViewById(R.id.btn_delete_word);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// deleteOneWord();
				showConfimDialog(ID_DIALOG_DELETE_WORD);
			}
		});
	}

	/**
	 * 處理提示按鍵事件
	 */
	private void handleTipWord() {
		ImageButton button = (ImageButton) findViewById(R.id.btn_tip_answer);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// tipAnswer();
				showConfimDialog(ID_DIALOG_TIP_ANSWER);
			}
		});
	}

	// 定义AlertDialog响应事件
	// 删除错误答案
	private IAlertDialogButtonListener mBtnOkDeleteWordListener = new IAlertDialogButtonListener() {

		@Override
		public void onClick() {
			// 执行事件
			deleteOneWord();
		}
	};
	// 答案提示
	private IAlertDialogButtonListener mBtnOkTipAnswerListener = new IAlertDialogButtonListener() {

		@Override
		public void onClick() {
			// 执行事件
			tipAnswer();
		}
	};
	// 金币不足
	private IAlertDialogButtonListener mBtnOkLockCoinsListener = new IAlertDialogButtonListener() {

		@Override
		public void onClick() {
			// 执行事件
		}
	};

	/**
	 * 显示对话框
	 * 
	 * @param id
	 */
	private void showConfimDialog(int id) {
		switch (id) {
		case ID_DIALOG_DELETE_WORD:
			Util.showDialog(MainActivity.this, "确定花掉" + getDeleteWordCoins()
					+ "去掉一个错误答案吗？", mBtnOkDeleteWordListener);
			break;
		case ID_DIALOG_TIP_ANSWER:
			Util.showDialog(MainActivity.this, "确定花掉" + getTipWordCoins()
					+ "提示一个答案吗？", mBtnOkTipAnswerListener);
			break;
		case ID_DIALOG_LACK_COINS:
			Util.showDialog(MainActivity.this, "金币不足,去商店补充吗？",
					mBtnOkLockCoinsListener);
			break;
		default:
			break;
		}
	}
}
