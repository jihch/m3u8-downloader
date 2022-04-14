package io.github.jihch.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 转换器，用于转换到目标编码和封装格式
 * @author jihch
 * 2021-03-22 17:45:04 +0800
 */
@Slf4j
@Component
public class TS2MP4Convertor {

	@Value("${ffmpeg-home}")
	private String ffmpegHome;
	
	/**
	 * 是否启用GPU加速
	 */
	@Value("${gpu.enabled}")
	private Boolean gpu;
	
	public File convert(File src, File dst, boolean delete) {
		if (!src.exists()) {
			return null;
		}
		
		File parent = dst.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		
		ProcessBuilder builder = null;
		
		String ffmepgExecutable = ffmpegHome + File.separator + "bin" + File.separator + "ffmpeg.exe";
		
		if (gpu) {
			builder = new ProcessBuilder(ffmepgExecutable, "-hwaccel", "cuvid", "-c:v", "h264_cuvid", "-i",
					src.getAbsolutePath(), "-c:v", "h264_nvenc", "-y", dst.getAbsolutePath());
			
		} else {
			builder = new ProcessBuilder(ffmepgExecutable, "-i", src.getAbsolutePath(), "-c:v", "libx264", "-c:a", "copy", "-bsf:a", "aac_adtstoasc", dst.getAbsolutePath());
			
		}
		 
		try {

			// 调用线程处理命令
			Process p = builder.start();

			// 获取进程的标准输入流
			final InputStream inputStream = p.getInputStream();

			// 获取进程的错误流
			final InputStream errorStream = p.getErrorStream();

			// 启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流
			new Thread(() -> {
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("GBK")));
				try {
					String readLine = null;
					while ((readLine = br.readLine()) != null) {
						log.info(readLine);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						inputStream.close();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			}).start();

			new Thread(() -> {
				BufferedReader br2 = new BufferedReader(new InputStreamReader(errorStream, Charset.forName("GBK")));
				try {
					String readLine = null;
					while ((readLine = br2.readLine()) != null) {
						log.error(readLine);
					}
				} catch (IOException e) {
					log.error(e.getMessage(), e);

				} finally {
					try {
						errorStream.close();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			}).start();

			p.waitFor();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		if (delete) {
			src.delete();
		}
		
		return dst;
	}
	
	public File deleteAfterConvert(File src, File dst) {
		return convert(src, dst, true);
	}
	
	public File convert(File src, File dst) {
		return convert(src, dst, false);
	}
	
	public File convert(String srcPathname, String pathname) {
		return this.convert(new File(srcPathname), pathname);
	}
	
	public File convert(File src, String pathname) {
		return this.convert(src, new File(pathname));
	}
	
	public File convert(File src, boolean delete) {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		String dst = src.getParent() + File.separatorChar + uuid + ".mp4";
		return convert(src, new File(dst), delete);
	}
	
	public File convert(File src) {
		return convert(src, false);
	}
	
	public File deleteAfterConvert(File src) {
		return convert(src, true);
	}
	
}
