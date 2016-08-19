package com.liufu.guessmusic.model;

public class Song {
	// 歌曲名
	private String mSongName;
	// 歌曲的文件名
	private String mSongFileName;
	// 歌曲名的長度
	private int mSongLength;

	public char[] getNameCharacters() {
		return mSongName.toCharArray();
	}

	public String getSongName() {
		return mSongName;
	}

	public void setSongName(String songName) {
		this.mSongName = songName;
		this.mSongLength = songName.length();
	}

	public String getSongFileName() {
		return mSongFileName;
	}

	public void setSongFileName(String songFileName) {
		this.mSongFileName = songFileName;
	}

	public int getSongLength() {
		return mSongLength;
	}
}
