package io.github.jihch.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AES解码器
 * @author jihch
 * 2021-03-20 18:01:21 +0800
 */
@Component
public class AESDecoder {

	
	private final static String AES = "AES";
	
	private final static String UTF8 = "UTF-8";
	
	// 定义一个16byte的初始向量，由于m3u8没有IV值，则设置为0即可
	private static final String IV_STRING = "0000000000000000";
	
	/**
	 * 
	 * @param key 密钥
	 * @param delete 解码后是否删除编码源文件
	 * @param src 源文件
	 * @param dst 目标文件，可以为null，为null则在src同文件夹中生成一个同名的.decode的文件
	 * @return
	 */
	public File decode(String key, boolean delete, File src, File dst) {
		
		// 初始化一个密钥对象
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), AES);
		
		// 初始化一个初始向量,不传入的话，则默认用全0的初始向量
		IvParameterSpec ivSpec = new IvParameterSpec(IV_STRING.getBytes());
		
		// 指定加密的算法、工作模式和填充方式
		Cipher cipher = null;
		
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

		try {
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		Path srcPath = Paths.get(src.getAbsolutePath());
		
		Path dstPath = null;
		
		if (dst == null) {
			dstPath = Paths.get(src.getParent(), File.separator, src.getName() + ".decode");
			
		} else {
			
			dstPath = dst.toPath();
			
		}
				
		byte[] allBytes = null;
		
		try {
			allBytes = Files.readAllBytes(srcPath);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		byte[] ret = null;
		
		try {
			ret = cipher.doFinal(allBytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		try {
			Files.write(dstPath, ret, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (delete) {
			try {
				Files.delete(srcPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return dstPath.toFile();
		
	}
	
	public File decode(String key, boolean delete, File src) {
		return decode(key, delete, src, null);
	}
	
	public File decode(String key, File src) {
		return decode(key, false, src, null);
	}
	
	public File deleteAfterDecode(String key, File src) {
		return decode(key, true, src, null);
	}
	
	public Collection<File> decode(String key, File... encodedFile) {
		List<File> retList = new ArrayList<>();
		for(File src:encodedFile) {
			File decode = decode(key, src);
			retList.add(decode);
		}
		return retList;
	}
	
	public Collection<File> deleteAfterDecode(String key, File... encodedFile) {
		List<File> retList = new ArrayList<>();
		for(File src:encodedFile) {
			File decode = deleteAfterDecode(key, src);
			retList.add(decode);
		}
		return retList;
	}
	
	public Collection<File> deleteAfterDecode(String key, Collection<File> src) {
		File[] arr = new File[src.size()];
		return deleteAfterDecode(key, src);
	}

}
