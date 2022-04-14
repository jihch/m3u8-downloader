package io.github.jihch.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * 资源管理器抽象
 * @author jihch
 *
 */
@Component
public class Explorer {

	/**
	 * 如果是文件，打开父级目录，选中指定文件；
	 * 如果是文件夹，也是打开父文件夹，选中指定文件夹;
	 * @param file
	 */
	public void show(File file) {
		if (file.exists()) {
			List<String> command = new ArrayList<>();
			command.add("explorer.exe");
			command.add("/select,");
			command.add(file.getAbsolutePath());
			try {
				new ProcessBuilder(command).start();
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
	}

	public void show(String folderPath) {
		show(new File(folderPath));
	}
	
	public static void main(String[] args) {
		String pathA = "D:\\file.txt"; 
		String pathB = "D:\\a\\b"; //如果文件不存在，会打开“我的电脑” 
		List<String> command = new ArrayList<>();
		command.add("explorer.exe");
		command.add("/select,");
		command.add(pathB);
		try {
			Process p = new ProcessBuilder(command).start();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	
}
