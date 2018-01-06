package com.xk.autojump.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class AutoJump {
	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	/**
	 * 主要用来执行adb命令
	 * @param cmd
	 * @author kui.xiao
	 */
	private static void runCmd(String[] cmd) {
		ProcessBuilder builder = new ProcessBuilder();
		builder.command(cmd);
		try {
			Process p = builder.start();
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 截手机图
	 * @param name
	 * @return
	 * @author kui.xiao
	 */
	public static String pullScreen(String name) {
		String create = String.format("adb shell screencap /sdcard/%s ", name);
		String pull = String.format("adb pull /sdcard/%s temp/%s", name, name);
		runCmd(create.split(" "));
		runCmd(pull.split(" "));
		return new File(String.format("temp/%s", name)).getAbsolutePath();
	}
	
	/**
	 * 模拟跳跃
	 * @param distance
	 * @param alpha
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @author kui.xiao
	 */
	public static void jump(double distance, double alpha, double x, double y, double x1, double y1){
		int pressTime = (int) Math.max(distance * alpha, 200);
		String cmd = String.format("adb shell input swipe %s %s %s %s %s", x, y, x1, y1, pressTime);
		runCmd(cmd.split(" "));
	}
	
	/**
	 * 计算距离并跳跃
	 * 
	 * @author kui.xiao
	 */
	public static void compute() {
		String name = "autojump.png";//截图文件名
		int chessX = 0;//小人所在位置
		int targetX = 0;//下一个跳跃点位置
		double fix = 1.19d;//这个是可变参数，根据自己手机来调整
		while(true) {
			String path = pullScreen(name);//抓取手机界面图片
			Mat image = Imgcodecs.imread(path);//使用opencv读取图片
			Mat img = new Mat();
			Imgproc.cvtColor(image, img, Imgproc.COLOR_BGR2RGB);//过滤色彩
			Mat gray = new Mat();
			Imgproc.Canny(img, gray, 20, 20d);//灰度化
			int width = img.width();
			int height = img.height();
			double x = width / 2;
			double y = width / 2;
			double x1 = height * 0.785;
			double y1 = height * 0.785;
			//计算小人所在位置
			List<Integer> linemax = new ArrayList<Integer>();
			for(int i = (int)(height * 0.4); i < (int)(height * 0.6); i++) {
				List<Integer> line = new ArrayList<Integer>();
				for(int j = (int)(width * 0.15);j < (int)(width * 0.85); j++) {
					double[] arr = img.get(i, j);
					if(arr[0] > 40 && arr[0] < 70 && arr[1] > 40 && arr[1] < 70 && arr[2] > 60 && arr[2] <110) {
						gray.put(i, j, new double[]{255});
						if (line.size() > 0 && j - line.get(line.size() - 1) > 1) {
							break;
						} else {
							line.add(j);
						}
					}
				}
				if(line.size() > 5 && line.size() > linemax.size()) {
					linemax = line;
				}
				if(linemax.size() > 20 && line.size() == 0) {
					break;
				}
			}
			chessX = abs(linemax);
			//计算目标所在位置
			for(int i =(int)(height * 0.3); i < (int)(height * 0.5); i++) {
				boolean flag = false;
				for(int j = 0; j < width; j ++) {
					if(Math.abs(j - chessX) < linemax.size()) {
						continue;
					}
					double[] grays = gray.get(i, j);
					
					if(grays[0] != 0.0d) {
						targetX = j;
						flag = true;
					}
				}
				if(flag) {
					break;
				}
			}
			System.out.println(chessX + "  " + targetX);
			jump(Math.abs(chessX - targetX), fix, x, y, x1, y1);
			//等待跳完，再下一波呗
			try {
				Thread.sleep(1200 + (int)(Math.random() * 500));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 计算平均值
	 * @param lines
	 * @return
	 * @author kui.xiao
	 */
	private static int abs(List<Integer> lines) {
		int total = 0;
		for(Integer i : lines) {
			total += i;
		}
		return total / lines.size();
	}
	
}
