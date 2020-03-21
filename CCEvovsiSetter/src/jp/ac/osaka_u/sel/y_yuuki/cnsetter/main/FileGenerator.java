package jp.ac.osaka_u.sel.y_yuuki.cnsetter.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>設定ファイル生成クラス</p>
 * @author y-yuuki
 */
public class FileGenerator {

	PrintWriter pw;
	String fileName;

	/**
	 * <p>設定ファイル生成</p>
	 * @param fileName
	 * @return
	 */
	public boolean init(String fileName){
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * <p>設定ファイルに書込み</p>
	 * @param context1
	 * @param context2
	 * @return
	 */
	public boolean  write(String context1, String context2){
		if(context2.isEmpty()){
			return false;
		}else{
			pw.println(context1+context2);
			return true;
		}
	}

	/**
	 * <p>書き込み終了</p>
	 */
	public void end(){
		pw.flush();
		pw.close();
	}


}
