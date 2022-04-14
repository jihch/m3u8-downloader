package io.github.jihch.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * 只读代码的指定行
 * 返回指定行的字符串
 * @author jihch
 *
 */
@Component
public class LineReader {

	/**
	 * 
	 * @param file 
	 * @param n 第n行 从0开始
	 * @return
	 */
	public String read(File file, int n) {
		String ret = null;
		if (!file.exists() || n < 0) {
			return ret;
		}
		
		int i = 0;
		
		try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
			// 按行读取字符串
			while ((ret = br.readLine()) != null) {
				if (n == i) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return ret;
	}
	
	public String read(String pathname, int n) {
		return this.read(new File(pathname), 0);
	}
	
	public static void main(String[] args) {
		String str = new LineReader().read("D:\\file.txt", 0);
		System.out.println(str);
	}

}
