package io.github.jihch.model;

import java.io.File;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * m3u8下载任务对象
 * @author jihch
 *
 */
@Data
@AllArgsConstructor
public class M3U8DownloadTask {

	private List<DownloadTask> tasks;
	
	private List<File> tsFileList;
	
	private File keyFile;
}
