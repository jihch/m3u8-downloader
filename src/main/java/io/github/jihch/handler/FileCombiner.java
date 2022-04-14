package io.github.jihch.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * 合并文件工具
 * 什么文件都能合并成一个文件
 * @author jihch
 * 2021-03-22 17:44:18 +0800
 */
@Component
public class FileCombiner {

	/**
	 * 合并源文件到目标文件
	 * 
	 * @param dst      合并到目标文件
	 * @param delete   合并后是否删除源文件 
	 * @param srcFiles 源文件数组 
	 * @return
	 */
	public File combine(File dst, boolean delete, File... srcFiles) {

		File parentFile = dst.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}

		final byte[] b = new byte[1024 * 1024];
		int l;

		// 拼接
		try (FileOutputStream fileOutputStream = new FileOutputStream(dst)) {

			for (File src : srcFiles) {

				try (FileInputStream fileInputStream = new FileInputStream(src)) {

					while ((l = fileInputStream.read(b)) != -1) {
						fileOutputStream.write(b, 0, l);
					}
				}
				
				if (delete) {
					src.delete();
				}

			} // end for

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		}

		return dst;

	}// end
	
	/**
	 * 合并文件
	 * @param dst 目标文件
	 * @param srcFiles 源文件
	 * @return
	 */
	public File combine(File dst, Collection<File> srcFiles) {
		File[] arr = srcFiles.toArray(new File[srcFiles.size()]);
		File ret = this.combine(dst, false, arr);
		return ret;
	}
	
	/**
	 * 合并文件后删除源文件
	 * @param dst
	 * @param srcFiles
	 * @return
	 */
	public File deleteAfterCombine(File dst, Collection<File> srcFiles) {
		File[] arr = srcFiles.toArray(new File[srcFiles.size()]);
		File ret = this.combine(dst, true, arr);
		return ret;
	}
	
	/**
	 * 合并后删除源文件
	 * @param pathname
	 * @param srcFiles
	 * @return
	 */
	public File deleteAfterCombine(String pathname, Collection<File> srcFiles) {
		File ret = deleteAfterCombine(new File(pathname), srcFiles);
		return ret;
	}
	
	/**
	 * 
	 * @param srcFiles
	 * @return 生成的文件
	 */
	public File deleteAfterCombine(Collection<File> srcFiles) {
		if (srcFiles == null || srcFiles.isEmpty()) {
			return null; 
		}
		
		File first = srcFiles.stream().findFirst().get();
		
		if (srcFiles.size() == 1) {
			return first; 
		}
		
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		
		String pathname = first.getParent() + File.separatorChar + uuid;
		
		File ret = deleteAfterCombine(pathname, srcFiles);
		
		return ret;
	}
	
}
