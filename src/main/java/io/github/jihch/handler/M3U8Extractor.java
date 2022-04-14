package io.github.jihch.handler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import io.github.jihch.model.M3U8;

@Component
public class M3U8Extractor {

	/**
	 * AES key 文件下载地址前缀标签
	 */
	public static final String AES_KEY_FILE_URL_PREFIX = "#EXT-X-KEY";

	public static final String regex = "\"\\S+\"";

	public static final Pattern P = Pattern.compile(regex);
	
	public M3U8 extract(File file) {
		
		URI keyURI = null;

		List<URI> tsURIList = new ArrayList<>();
		
		Path filePath = Paths.get(file.getAbsolutePath());
		
		List<String> lines = null;
		
		try {
			lines = Files.readAllLines(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		URI tmp = null; 
				
		for (String str : lines) {
			
			//秘钥文件行
			if (str.startsWith(AES_KEY_FILE_URL_PREFIX)) {
				keyURI = extractKeyURI(str);
			}
			
			if (str.startsWith("#")) {
				continue;
			}
			
			try {
				tmp = new URI(str);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			tsURIList.add(tmp);
			
		}
		
		return new M3U8(keyURI, tsURIList);
	}
	
	private URI extractKeyURI(String str) {
		
		String tmp = null;
		
		Matcher m = P.matcher(str);
		if (m.find()) {
			tmp = str.subSequence(m.start() + 1, m.end() - 1).toString(); 
			
		}
		
		URI keyURI = null;
		try {
			keyURI = new URI(tmp);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return keyURI;
	}
	
	public M3U8 extract(String pathname) {
		return extract(new File(pathname));
	}
	
	public static void main(String[] args) {
		//String pathnameA = "E:\\record\\2022\\2\\23\\东北虎\\index.m3u8"; //无key,绝对URL,混杂png后缀的 v
		//String pathnameA = "E:\\record\\2021\\11\\20\\video\\index.m3u8"; //无key,相对URL
		String pathnameA = "G:\\record\\2021\\2\\15\\赘婿\\6\\index.m3u8"; //有key,相对URL
		
		M3U8 m3u8 = new M3U8Extractor().extract(pathnameA);
		
		List<URI> l = new ArrayList<>();
		
		
		URI uri = null;

		
		
		System.out.println(m3u8.toString());
		
		
	}

}
