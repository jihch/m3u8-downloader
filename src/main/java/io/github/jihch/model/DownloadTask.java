package io.github.jihch.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import lombok.Data;

/**
 * 下载任务的抽象
 * @author jihch
 *
 */
@Data
public class DownloadTask {

	private URI uri;
	
	/**
	 * 下载文件
	 */
	private File file;
	
	public DownloadTask(String uri, String pathname) throws URISyntaxException {
		this(new URI(uri), pathname);
	}
	
	public DownloadTask(URI uri, String pathname) {
		this(uri, new File(pathname));
	}
	
	public DownloadTask(String uri, File file) throws URISyntaxException {
		this(new URI(uri), file);
	}
	
	public DownloadTask(URI uri, File file) {
		this.uri = uri;
		this.file = file;
	}
}
