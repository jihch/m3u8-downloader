package io.github.jihch.handler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.jihch.model.DownloadTask;
import io.github.jihch.model.M3U8;
import io.github.jihch.model.M3U8DownloadTask;

@Component
public class Executor {

	@Value("${m3u8-index-url}")
	private String m3u8IndexUrl;
	
	@Value("${task.m3u8-index-file-pathname}")
	private String m3u8IndexFilePathname;
	
	@Value("${task.aes-key-filename}")
	private String aesKeyFilename;
	
	@Value("${task.home}")
	private String downloadPath;
	
	@Value("${final-media-filename}")
	private String finalMediaFilename;
	
	@Value("${task.ts-dir-pathname}")
	private String tsDir;
	
	/**
	 * 用于转TS到mp4
	 */
	@Autowired
	private TS2MP4Convertor convertor;
	
	/**
	 * 用于合并众多的小TS到大TS
	 */
	@Autowired
	private FileCombiner combiner;
	
	/**
	 * 用于下载文件
	 */
	@Autowired
	private AsyncClientDownloader downloader;
	
	/**
	 * 用于对进行AES编码的TS文件进行解码
	 */
	@Autowired
	private AESDecoder decoder;
	
	@Autowired
	private M3U8Extractor extractor;
	
	/**
	 * 用于从AES key文件中解析出AES字符串
	 */
	private LineReader lineReader;
	
	@Autowired
	private Explorer explorer;
	
	@Autowired
	M3U8DownloadTaskParser m3u8DownloadTaskParser;
	
	public void execute() {

		URI index = null;
		
		try {
			index = new URI(m3u8IndexUrl);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		//下载M3U8索引文件
		DownloadTask task = new DownloadTask(index, m3u8IndexFilePathname);
		List<DownloadTask> taskList = new ArrayList<>();
		taskList.add(task);
		downloader.download(taskList);
		
		//解析M3U8文件
		M3U8 m3u8 = extractor.extract(m3u8IndexFilePathname);
		
		//解析M3U8对象中的下载任务
		M3U8DownloadTask m3u8Task = m3u8DownloadTaskParser.parse(index, m3u8);
		
		//下载AES秘钥文件和TS文件
		downloader.download(m3u8Task.getTasks());
		
		Collection<File> tsC = m3u8Task.getTsFileList();
		
		if (m3u8Task.getKeyFile() != null) {
			
			String aesKey = lineReader.read(m3u8Task.getKeyFile(), 0);
			
			tsC = decoder.deleteAfterDecode(aesKey, tsC);
			
		}
		
		File ret = combiner.deleteAfterCombine(tsC);
		
		//实例化文件转换器
		ret = convertor.deleteAfterConvert(ret);
		
		//打开目标目录
		explorer.show(ret);
		
	}//execute
	
	
}
