package io.github.jihch.handler;

import java.util.List;

import io.github.jihch.model.DownloadTask;

public interface Downloader {

	void download(List<DownloadTask> taskList);
	
	void download(DownloadTask... tasks);
	
}
