package io.github.jihch.handler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.jihch.model.DownloadTask;
import io.github.jihch.model.M3U8;
import io.github.jihch.model.M3U8DownloadTask;

/**
 * M3U8文件下载任务解析器
 * 解析M3U8文件，确认哪些资源需要下载
 * @author jihch
 *
 */
@Component
public class M3U8DownloadTaskParser {

	@Value("${task.aes-key-filename}")
	private String aesKeyFilename;
	
	@Value("${task.ts-dir-pathname}")
	private String tsDir;
	
	public M3U8DownloadTask parse(URI uri, M3U8 m3u8) {
	
		M3U8DownloadTask ret = null;
		
		if (m3u8 == null) {
			return ret;
		}
		
		List<DownloadTask> taskList = new ArrayList<>();
		
		DownloadTask key = extractKey(uri, m3u8);
		
		List<DownloadTask> tmpList = extractTSList(uri, m3u8);
		taskList.addAll(tmpList);
		
		List<File> tsFileList = tmpList.stream().map(x -> x.getFile()).collect(Collectors.toList());
		
		if (key != null) {
			taskList.add(key);
			ret = new M3U8DownloadTask(taskList, tsFileList, key.getFile());
			
		} else {
			ret = new M3U8DownloadTask(taskList, tsFileList, null);
		}
		 
		
		return ret;
	}
	
	
	/**
	 * 提取AES key的下载任务
	 * @param m3u8
	 * @return
	 */
	public DownloadTask extractKey(URI uri, M3U8 m3u8) {
		//可能需要下载AES秘钥文件，进行AES解码
		if (m3u8 == null || m3u8.getKeyURI() == null) {
			return null;
		}
		URI tmp = m3u8.getKeyURI();  
		DownloadTask downloadTask = null;
		if (!tmp.isAbsolute()) {
			tmp = uri.resolve(tmp);
		}
		downloadTask = new DownloadTask(tmp, new File(aesKeyFilename));
		return downloadTask;
	}
	
	
	/**
	 * 提取TS文件列表的下载任务
	 * @param m3u8
	 * @return
	 */
	public List<DownloadTask> extractTSList(URI uri, M3U8 m3u8) {

		return m3u8.getTsURIs().stream().map(tsURI -> {
			
			if (!tsURI.isAbsolute()) {
				tsURI = uri.resolve(tsURI);
			}
			
			String path = tsURI.getPath();
			
			int lastIndexOf = path.lastIndexOf("/");
			
			String tsFilename = path.substring(lastIndexOf + 1);
			
			String dst = tsDir + File.separator + tsFilename;
			
			return new DownloadTask(tsURI, dst);
			
		}).collect(Collectors.toList());
		
		
	}
	
}
