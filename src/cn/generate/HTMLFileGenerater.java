package cn.generate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import cn.Def;
import cn.Logger;
import cn.data.Clone;
import cn.data.CloneSet;
import cn.data.Project;
import cn.data.SetDate;
import cn.data.SourceFile;

/**
 * <p>
 * HTML出力クラス
 * </p>
 *
 * @author y-yuuki
 */
public class HTMLFileGenerater {

	private OutputGenerator g = null;
	private Project project = null;

	/** Javascriptファイル名 */
	private static final String SCRIPT = "script.js";

	/** Javascriptディレクトリ名 */
	private final static String JS = "js";

	/** CSSディレクトリ名 */
	private final static String CSS = "css";

	/** 画像ディレクトリ名 */
	private final static String IMAGES = "images";

	/**/
	private final static String BUILD = "build";
	private final static String DOCS = "docs";
	private final static String PAGE = "page";
	private final static String SRC = "src";
	private final static String VENDORS = "vendors";
	private final static String DATA = "data";
	private final static String USERS = "users";

	/** インデックスページのhtmlファイル名 */
	private final static String INDEX_PAGE = "index.html";

	/** クローンセットリストページのhtmlファイル名 */
	private final static String CLONESETLIST_PAGE = "cloneset.html";
	/** Stableクローンセットページのhtmlファイル名 */
	private final static String STABLECLONESET_PAGE = "stablecloneset.html";


	/** パッケージ(ディレクトリ)リストページのhtmlファイル名 */
	private final static String PACKAGELIST_PAGE = "packagelist.html";
	private final static int STACK = 100;

	public HTMLFileGenerater(OutputGenerator g, Project project) {
		this.g = g;
		this.project = project;
	}

	static /*保守対象outputGenerate.javaに書き直しinconsistentの数をカウントする変数*/
	class Globalval{
		public static int inconsistent_cnt= 0;
	}

	/**
	 * <p>
	 * HTMLファイル生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	public boolean generateHTMLFile() {

		// リソースファイルのコピー
		if (!copyResourceFiles(project.getGenerateHTMLDir())) {
			return false;
		}


		// プロジェクトディレクトリの生成
		//		File dir = new File(project.getGenerateHTMLDir() + "\\projects\\"+ project.getName() + "\\" + project.getDate());
		File dir = new File(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\projects\\"+ project.getName() + "\\" +  project.getAnalysisName() + "\\" + project.getAnalysisdate());
		dir.mkdirs();
		File datadir = new File(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\data\\");
		datadir.mkdirs();
		/*File dir = new File(project.getGenerateHTMLDir() + "\\" + project.getDate());
		dir.mkdirs();
		 */

		/*順番変えたから直そう，inconsistent機能のため
		 * 		// プロジェクトホームファイルの出力
		if (!generateProjectPage(g, dir.getAbsolutePath(), project)) {
			return false;
		}
		 */
		// クローンセットファイルの出力
		if (!generateCloneSetListPage(g, dir.toString(), project)) {
			System.out.println("クローンセットファイル");
			return false;
		}
		// Stableクローンセットファイルの出力
		if (!generateStableClonesetPage(g, dir.toString(), project)) {
			System.out.println("Stableクローンセットファイル");
			return false;
		}
		// データファイルの出力
		if (!generateData(g, project)) {
			System.out.println("データファイル");
			return false;
		}
		if (!generateAllData(g, project)) {
			System.out.println("オールデータファイル");
			return false;
		}
		if (!generateData(g, project)) {
			System.out.println("すべてのプロジェクトデータの作成");
			return false;
		}
		if (!generateAnalysisData(g, project)) {
			System.out.println("analysisdataの作成");
			return false;
		}
		// プロジェクトホームファイルの出力
		if (!generateProjectPage(g, dir.getAbsolutePath(), project)) {
			System.out.println("Project");
			return false;
		}
		if (!generateProjectsData(g, project)) {
			System.out.println("すべてのプロジェクトデータの作成");
			return false;
		}
		// パッケージ一覧ファイルの出力
		if (!generatePackageListPage(g, dir.getAbsolutePath(), project)) {
			System.out.println("パッケージ一覧");
			return false;
		}

		// ソースファイルのindex.htmlの出力
		if (!generateIndexEachPackege(g, dir.getAbsolutePath(), project)) {
			System.out.println("ソースファイルindex");
			return false;
		}

		// ソースファイル一覧ファイルの出力

		if (!generateFileListPage(g, new File(dir.getAbsolutePath()), 1, dir.getAbsolutePath(), project)) {
			System.out.println("ソースファイル一覧");
			return false;
		}
		if (!generateDateListPage(project)) {
			System.out.println("datalist");
			return false;
		}
		if (!generateAnalysisListPage(project)) {
			System.out.println("Analysislist");
			return false;
		}

		Globalval.inconsistent_cnt=0;
		g = null;
		System.gc();

		return true;
	}

	/**
	 * <p>
	 * 画像ファイルのコピー処理
	 * </p>
	 *
	 * @param generateHTMLDir
	 *            出力ディレクトリ
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean copyImageFiles(String generateHTMLDir) {
		File dir = new File(generateHTMLDir + "\\" + Def.RESOURCES);

		if (dir.exists()) {
			return true;
		}

		dir.mkdirs();
		String[] images = { "asc.gif", "desc.gif", "sort.gif" };
		for (String image : images) {
			try {
				FileInputStream src = new FileInputStream(Def.RESOURCES + "/" + image);
				FileOutputStream dest = new FileOutputStream(dir.toString() + "/" + image);
				FileChannel srcChannel = src.getChannel();
				FileChannel destChannel = dest.getChannel();
				try {
					srcChannel.transferTo(0, srcChannel.size(), destChannel);
				} catch (IOException e) {
					return false;
				} finally {
					try {
						srcChannel.close();
						destChannel.close();
						src.close();
						dest.close();
					} catch (IOException e) {
						return false;
					}
				}
			} catch (FileNotFoundException e) {
				Logger.writeln("Can't find image files.", Logger.ERROR);
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * 画像ファイルのコピー処理
	 * </p>
	 *
	 * @param generateHTMLDir
	 *            出力ディレクトリ
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean copyResourceFiles(String generateHTMLDir) {
		File srcDir = new File(Def.RESOURCES + "/DataTables-1.10.16/");
		File destDir = new File(generateHTMLDir);

		IOFileFilter filter = FileFilterUtils.nameFileFilter("LICENSE");
		try {
			FileUtils.copyDirectory(srcDir, destDir, FileFilterUtils.notFileFilter(filter));
		} catch (IOException e1) {
			Logger.writeln("Can't find image files.", Logger.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * JavaScriptの生成
	 * </p>
	 *
	 * @param dir
	 *            出力ディレクトリ
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateScript(String dir) {
		try {
			if (!new File(dir + "\\" + SCRIPT).exists()) {
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir + "\\" + SCRIPT)));
				writer.println("var table=function(){");
				writer.println("\tfunction sorter(n){");
				writer.println("\t\tthis.n=n; this.t; this.b; this.r; this.d; this.p; this.w; this.a=[]; this.l=0");
				writer.println("\t}");
				writer.println("\tsorter.prototype.init=function(t,f){");
				writer.println("\t\tthis.t=document.getElementById(t);");
				writer.println("\t\tthis.b=this.t.getElementsByTagName('tbody')[0];");
				writer.println("\t\tthis.r=this.b.rows; var l=this.r.length;");
				writer.println("\t\tfor(var i=0;i<l;i++){");
				writer.println("\t\t\tif(i==0){");
				writer.println("\t\t\t\tvar c=this.r[i].cells; this.w=c.length;");
				writer.println("\t\t\t\tfor(var x=0;x<this.w;x++){");
				writer.println("\t\t\t\t\tif(c[x].className!='nosort'){");
				writer.println("\t\t\t\t\t\tc[x].className='head';");
				writer.println("\t\t\t\t\t\tc[x].onclick=new Function(this.n+'.work(this.cellIndex)')");
				writer.println("\t\t\t\t\t}");
				writer.println("\t\t\t\t}");
				writer.println("\t\t\t}else{");
				writer.println("\t\t\t\tthis.a[i-1]={}; this.l++;");
				writer.println("\t\t\t}");
				writer.println("\t\t}");
				writer.println("\t\tif(f!=null){");
				writer.println("\t\t\tvar a=new Function(this.n+'.work('+f+')'); a()");
				writer.println("\t\t}");
				writer.println("\t}");
				writer.println("\tsorter.prototype.work=function(y){");
				writer.println("\t\tthis.b=this.t.getElementsByTagName('tbody')[0]; this.r=this.b.rows;");
				writer.println("\t\tvar x=this.r[0].cells[y],i;");
				writer.println("\t\tfor(i=0;i<this.l;i++){");
				writer.println("\t\t\tthis.a[i].o=i+1; var v=this.r[i+1].cells[y].firstChild;");
				writer.println("\t\t\tthis.a[i].value=(v!=null)?v.nodeValue:''");
				writer.println("\t\t}");
				writer.println("\t\tfor(i=0;i<this.w;i++){");
				writer.println("\t\t\tvar c=this.r[0].cells[i];");
				writer.println("\t\t\tif(c.className!='nosort'){c.className='head'}");
				writer.println("\t\t}");
				writer.println("\t\tif(this.p==y){");
				writer.println("\t\t\tthis.a.reverse(); x.className=(this.d)?'asc':'desc';");
				writer.println("\t\t\tthis.d=(this.d)?false:true");
				writer.println("\t\t}else{");
				writer.println("\t\t\tthis.p=y; this.a.sort(compare); x.className='asc'; this.d=false");
				writer.println("\t\t}");
				writer.println("\t\tvar n=document.createElement('tbody');");
				writer.println("\t\tn.appendChild(this.r[0]);");
				writer.println("\t\tfor(i=0;i<this.l;i++){");
				writer.println("\t\t\tvar r=this.r[this.a[i].o-1].cloneNode(true);");
				writer.println("\t\t\tn.appendChild(r); r.className=(i%2==0)?'even':'odd'");
				writer.println("\t\t}");
				writer.println("\t\tthis.t.replaceChild(n,this.b)");
				writer.println("\t}");
				writer.println("\tfunction compare(f,c){");
				writer.println("\t\tf=f.value,c=c.value;");
				writer.println(
						"\t\tvar i=parseFloat(f.replace(/(\\$|\\,)/g,'')),n=parseFloat(c.replace(/(\\$|\\,)/g,''));");
				writer.println("\t\tif(!isNaN(i)&&!isNaN(n)){f=i,c=n}");
				writer.println("\t\treturn (f>c?1:(f<c?-1:0))");
				writer.println("\t}");
				writer.println("\treturn{sorter:sorter}");
				writer.println("}();");
				writer.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * <p>/\
	 * プロジェクトページ生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param dir
	 *            出力ディレクトリ
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateData(OutputGenerator g, Project project) {
		try {
			// プロジェクトディレクトリの生成
			File datadir = new File(project.getGenerateHTMLDir() +"\\users\\" + project.getUserId() +  "\\projects\\"+ project.getName() + "\\" + project.getAnalysisName() + "\\data");
			datadir.mkdirs();
			//			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir + "\\data\\" + project.getDate() + ".json")));
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(project.getGenerateHTMLDir()  + "\\users\\" + project.getUserId() + "\\projects\\"  + project.getName() + "\\" + project.getAnalysisName() +"\\data\\"+ project.getAnalysisdate() + ".json")));
			writer.printf("{\r\n" +
					"	State: '%s',\r\n" +
					"	freq:\r\n" +
					"	{\r\n" +
					"		Stable: %d,\r\n" +
					"		Changed: %d,\r\n" +
					"		Deleted: %d,\r\n" +
					"		New: %d\r\n" +
					"	},\r\n" +
					"	clone:\r\n" +
					"	{\r\n" +
					"		Stable: %d,\r\n" +
					"		Modified: %d,\r\n" +
					"		Moved: %d,\r\n" +
					"		Added: %d,\r\n" +
					"		Deleted: %d\r\n" +
					"	}\r\n" +
					"}", project.getAnalysisdate(), g.getStableCloneSetNum(),g.getChangedCloneSetNum(), g.getDeletedCloneSetNum(), g.getNewCloneSetNum(),
					g.getStableCloneNum(), g.getModifiedCloneNum(), g.getMovedCloneNum(), g.getAddedCloneNum(), g.getDeletedCloneNum());

			writer.flush();
			writer.close();

		} catch (IOException e) {
			return false;
		}

		return true;
	}


	private boolean generateAllData(OutputGenerator g, Project project){
		try {
			//String project_folder = "\\" + project.getDate() + "\\";
			String project_folder = "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\" +project.getAnalysisName() + "\\" + project.getAnalysisdate() + "\\";
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(project.getGenerateHTMLDir()  + project_folder + "freqData.json")));
			writer.printf("var  freqData = [\n");
			//			String dir_path = project.getGenerateHTMLDir()  + "\\data\\" + project.getName() + "\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			//			String dir_path = project.getGenerateHTMLDir()  + "\\data\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			String dir_path = project.getGenerateHTMLDir()  + "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\" +project.getAnalysisName()  + "\\data\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			String extension = ".json";   //検索したいファイルの拡張子
			file_search(dir_path, extension, writer);
			writer.print("];");/*最後のカンマを消すたｋめ*/
			writer.printf("var inconsist_cnt = %d\r\n", Globalval.inconsistent_cnt);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	public static void file_search(String path, String extension, PrintWriter writer){
		File dir = new File(path);
		File files[] = dir.listFiles();
		for(int i=0; i<files.length; i++){
			String file_name = files[i].getName();
			if(files[i].isDirectory()){  //ディレクトリなら再帰を行う
				file_search(path+"/"+file_name, extension, writer);
			}else{
				if(file_name.endsWith(extension)){  //file_nameの最後尾(拡張子)が指定のものならば出力
					System.out.println(path + file_name);
					try {
						FileReader fr = new FileReader(path+"/"+file_name);
						BufferedReader br = new BufferedReader(fr);

						//読み込んだファイルを１行ずつ画面出力する
						String line;
						while ((line = br.readLine()) != null) {
							System.out.println(line);
							writer.printf("%s\n", line);
						}

						if(i != files.length -1 ) {
							writer.print(",\n");
						}
						br.close();
						fr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}
	}



	private boolean generateProjectsData(OutputGenerator g, Project project){
		try {
			File datafol = new File(project.getGenerateHTMLDir()  +"\\users\\" + project.getUserId() + "\\data\\");

			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(project.getGenerateHTMLDir()  + "\\users\\" + project.getUserId() + "\\data\\projects.json")));
			writer.printf("var  projects = [\n");
			//			String dir_path = project.getGenerateHTMLDir()  + "\\data\\" + project.getName() + "\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			//			String dir_path = project.getGenerateHTMLDir()  + "\\data\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			String dir_path = project.getGenerateHTMLDir()  + "\\users\\" + project.getUserId() + "\\projects\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			projectinfo_search(dir_path,  writer, project);
			writer.print("];");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private boolean generateAnalysisData(OutputGenerator g, Project project){
		try {
			File datafol = new File(project.getGenerateHTMLDir()  +"\\users\\" + project.getUserId() +  "\\projects\\" + project.getName() + "\\data\\");

			if(!datafol.exists()) {
				if (datafol.mkdirs()) {
					System.out.println("Success: Create data folder");
				} else {
					System.out.println("Error: Didn't create data foleder");
				}
			}
			//String project_folder = "\\" + project.getDate() + "\\";
			//	String project_folder = "\\projects\\" + project.getName() + "\\" + project.getAnalysisName() + "\\" + project.getAnalysisdate() + "\\";
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(project.getGenerateHTMLDir()  + "\\users\\" + project.getUserId() +  "\\projects\\" + project.getName() + "\\data\\analysis.json")));
			writer.printf("var  analysis = [\n");
			//			String dir_path = project.getGenerateHTMLDir()  + "\\data\\" + project.getName() + "\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			//			String dir_path = project.getGenerateHTMLDir()  + "\\data\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			String dir_path = project.getGenerateHTMLDir()  +"\\users\\" + project.getUserId()+  "\\projects\\" + project.getName() + "\\";  //検索開始したいフォルダのPath(今回の場合なら`~Folder/`まで書く)
			analysisinfo_search(dir_path,  writer, project);
			writer.print("];");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}


	public static void projectinfo_search(String path,  PrintWriter writer, Project project){
		File dir = new File(path);
		File files[] = dir.listFiles();
		for(int i=0; i<files.length; i++){
			String file_name = files[i].getName();
			if(file_name.equals("data")) {
				//				System.out.println("data folder");
			}else if(files[i].isDirectory()){
				File datedir = new File(path + "/" + file_name);
				File datefiles[] = datedir.listFiles();
				writer.print("{");
				writer.printf("	\"name\":\"%s\",\r\n",file_name);
				//				writer.printf("	\"date\":\"%s\"\r\n",datefiles[datefiles.length-2].getName());
				if(i==files.length-1) {
					writer.print("}\r\n");
				}else {
					writer.print("},\r\n");
				}
			}
		}
	}


	public static void analysisinfo_search(String path,  PrintWriter writer, Project project){
		File dir = new File(path);
		File files[] = dir.listFiles();
		for(int i=0; i<files.length; i++){
			String file_name = files[i].getName();
			if(file_name.equals("data")) {
				//				System.out.println("data_folder");
			}else if(files[i].isDirectory()){
				File datedir = new File(path + "/" + file_name);
				File datefiles[] = datedir.listFiles();
				String curday = datefiles[datefiles.length-2].getName();
				String cury_s = curday.substring(0,4);
				String curm_s = curday.substring(4,6);
				String curd_s = curday.substring(6,8);
				int cury = Integer.parseInt(cury_s);
				int curm = Integer.parseInt(curm_s);
				int curd = Integer.parseInt(curd_s);
				SetDate curdate = new SetDate(cury, curm, curd);
				writer.print("{");
				writer.printf("	\"name\":\"%s\",\r\n",file_name);
				//				writer.printf("	\"date\":\"%s\"\r\n",datefiles[datefiles.length-2].getName());
				writer.printf("	\"date\":\"%s\",\r\n",curdate.getDate());
				String secondday = datefiles[0].getName();
				String y_s = secondday.substring(0,4);
				String m_s = secondday.substring(4,6);
				String d_s = secondday.substring(6,8);
				int y = Integer.parseInt(y_s);
				int m = Integer.parseInt(m_s);
				int d = Integer.parseInt(d_s);
				SetDate date = new SetDate(y, m, d);
				date.addDate(-project.getInterval());
				writer.printf("	\"olddate\":\"%s\"\r\n",date.getDate());
				if(i==files.length-1) {
					writer.print("}\r\n");
				}else {
					writer.print("},\r\n");
				}
			}
		}
	}

	/**
	 * <p>
	 * プロジェクトページ生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param dir
	 *            出力ディレクトリ
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateProjectPage(OutputGenerator g, String dir, Project project) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir + "\\" + INDEX_PAGE)));

			String cur_date = Integer.toString(project.getAnalysisdate());
			String cur_year = cur_date.substring(0, 4);
			String cur_month = cur_date.substring(4, 6);
			String cur_day = cur_date.substring(6, 8);
			String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
			String old_year = old_date.substring(0, 4);
			String old_month = old_date.substring(4, 6);
			String old_day = old_date.substring(6, 8);

			String lib_path =  "../../../../../../";
			String home_path =  "../../../../";
			String pro_data_path =  "../../../../";
			String data_path =  "../../";

			writer.printf("<!DOCTYPE html>\r\n" +
					"<html lang=\"en\">\r\n" +
					"  <head>\r\n" +
					"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
					"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
					"    <meta charset=\"utf-8\">\r\n" +
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
					"\r\n" +
					"    <title>CCEvovis </title>\r\n" +
					"\r\n" +
					"");
			writer.printf("    <!-- Bootstrap -->\r\n" +
					"    <link href=\"%svendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Font Awesome -->\r\n" +
					"    <link href=\"%svendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <link href=\"%svendors/nprogress/nprogress.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <link href=\"%svendors/iCheck/skins/flat/green.css\" rel=\"stylesheet\">\r\n" +
					"	\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <link href=\"%svendors/bootstrap-progressbar/css/bootstrap-progressbar-3.3.4.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- JQVMap -->\r\n" +
					"    <link href=\"%svendors/jqvmap/dist/jqvmap.min.css\" rel=\"stylesheet\"/>\r\n" +
					"    <!-- bootstrap-daterangepicker -->\r\n" +
					"    <link href=\"%svendors/bootstrap-daterangepicker/daterangepicker.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Style -->\r\n" +
					"    <link href=\"%sbuild/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
					"    \r\n" +
					"    <!--Import Google Icon Font-->\r\n" +
					"    <link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">\r\n" +
					"    \r\n" +
					"     <!-- bootstrap-progressbar -->\r\n" +
					"    <link href=\"%svendors/bootstrap-progressbar/css/bootstrap-progressbar-3.3.4.min.css\" rel=\"stylesheet\">\r\n" +
					"", lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path);

			//スタイルシート
			writer.printf("    <style>\r\n" +
					"  	.bar:hover {\r\n" +
					"    	fill: gray;\r\n" +
					"  	}\r\n" +
					"    \r\n" +
					"      path {\r\n" +
					"        stroke: #fff;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      path:hover {\r\n" +
					"        opacity: 0.9;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      rect:hover {\r\n" +
					"        fill: blue;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      .axis {\r\n" +
					"        font: 10px sans-serif;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      .legend tr {\r\n" +
					"        border-bottom: 1px solid grey;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      .legend tr:first-child {\r\n" +
					"        border-top: 1px solid grey;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      .axis path,\r\n" +
					"      .axis line {\r\n" +
					"        fill: none;\r\n" +
					"        stroke: #000;\r\n" +
					"        shape-rendering: crispEdges;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      .x.axis path {\r\n" +
					"        display: none;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"/* 凡例の見え方*/\r\n" +
					"      .legend {\r\n" +
					"        margin-bottom: 76px;\r\n" +
					"        display: inline-block;\r\n" +
					"        border-collapse: collapse;\r\n" +
					"        border-spacing: 0px;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      .legend td {\r\n" +
					"        padding: 4px 5px;\r\n" +
					"        vertical-align: bottom;\r\n" +
					"      }\r\n" +
					"      \r\n" +
					"      #dashboard{\r\n" +
					"      /*position:absolute;\r\n*/" +
					"      /*top:150px;\r\n*/" +
					"      	text-align: center;\r\n" +
					"        margin-left: auto; \r\n" +
					"  		margin-right: auto; \r\n" +
					"      }\r\n" +
					"      \r\n" +
					"      #divdash{\r\n" +
					"      	text-align: center;\r\n" +
					"      }\r\n" +
					"      \r\n" +
					"      #divdash2{\r\n" +
					"      	text-align: center;\r\n" +
					"      }\r\n" +
					"      #divdash3{\r\n" +
					"      	text-align: center;\r\n" +
					"      }      \r\n" +
					"      \r\n" +
					"      #divdash4{\r\n" +
					"      	text-align: center;\r\n" +
					"      }\r\n" +
					"      \r\n" +
					"      #bars{\r\n" +
					"      	text-align: center;\r\n" +
					"      }\r\n" +
					"      \r\n" +
					"      #legend{\r\n" +
					"      	text-align: center;\r\n" +
					"      }\r\n" +
					"      \r\n" +
					"      \r\n" +
					"      #category{\r\n" +
					"      position:absolute;\r\n" +
					"      top: -20px;\r\n" +
					"      }\r\n" +
					"\r\n" +
					"      .legendFreq,\r\n" +
					"      .legendPerc {\r\n" +
					"        align: right;\r\n" +
					"        width: 50px;\r\n" +
					"      }\r\n" +
					"      \r\n" +
					"      .viz-bar .bar{\r\n" +
					"		  fill: steelblue;\r\n" +
					"		}\r\n" +
					"		.viz-legend:hover{\r\n" +
					"		  cursor:pointer;\r\n" +
					"		}\r\n" +
					"		\r\n" +
					"		#bars{\r\n" +
					" 		 float: left;\r\n" +
					"		}\r\n" +
					"		\r\n" +
					"		#legend{\r\n" +
					" 		 float: right;\r\n" +
					"		}\r\n" +
					"		\r\n" +
					"	  .x_text {\r\n" +
					"         writing-mode: tb-rl;\r\n" +
					"         font-size: 10px;\r\n" +
					"      }\r\n" +
					"      \r\n" +
					"     .content1 {\r\n" +
					"        overflow:visible;\r\n" +
					"    }\r\n" +
					"\r\n" +
					"    .content2 {\r\n" +
					"        overflow:visible;\r\n" +
					"    }\r\n" +
					"\r\n" +
					"    .inner {\r\n" +
					"        padding:10px;\r\n" +
					"    }\r\n" +
					"\r\n" +
					"    .caption1 {\r\n" +
					"        position:absolute;\r\n" +
					"        left: 0;\r\n" +
					"        right: 0;\r\n" +
					"        bottom: 0;\r\n" +
					"        top: -220px;\r\n" +
					"        margin: auto;\r\n" +
					"        display:none;\r\n" +
					"        color:#fff;\r\n" +
					"        /*padding:10px;*/\r\n" +
					"        width:180px;\r\n" +
					"        height:80px;\r\n" +
					"        text-align:center;\r\n" +
					"        /*margin: 0 -50px;\r\n" +
					"        padding: 0 50px;*/\r\n" +
					"        overflow: visible;\r\n" +
					"    }\r\n" +
					"\r\n" +
					"    .caption2 {\r\n" +
					"        position:absolute;\r\n" +
					"        left: 0;\r\n" +
					"        right: 0;\r\n" +
					"        bottom: 0;\r\n" +
					"        top: -220px;\r\n" +
					"        margin: auto;\r\n" +
					"        \r\n" +
					"        display:none;\r\n" +
					"        color:#fff;\r\n" +
					"        /*padding:10px;*/\r\n" +
					"        width:180px;\r\n" +
					"        height:80px;\r\n" +
					"        text-align:center;\r\n" +
					"        /*margin: 0 -50px;\r\n" +
					"        padding: 0 50px;*/\r\n" +
					"        overflow: visible;\r\n" +
					"    }\r\n" +
					"    .link {\r\n" +
					"        display:inline-block;\r\n" +
					"        margin:50px 0 0;\r\n" +
					"        left:100;\r\n" +
					"        /*background:#333;*/\r\n" +
					"        background-color: rgba(0,0,0,0.5);\r\n" +
					"        color:#fff;\r\n" +
					"        padding:5px;\r\n" +
					"        text-decoration:none;\r\n" +
					"        border-radius:3px;\r\n" +
					"        -webkit-border-radius:3px;\r\n" +
					"        -moz-border-radius:3px;\r\n" +
					"    }\r\n" +
					"    </style>\r\n" +
					"");


			writer.printf("</head>\r\n");

			//トップナビゲーション
			writer.printf("  <body class=\"nav-md\">\r\n" +
					"    <div class=\"container body\">\r\n" +
					"      <div class=\"main_container\">\r\n" +
					"        <div class=\"col-md-3 left_col\">\r\n" +
					"          <div class=\"left_col scroll-view\">\r\n" +
					"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
					"              <a href=\"%sindex.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
					"            </div>\r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"\r\n" +
					"            <!-- menu profile quick info -->\r\n" +
					"            <div class=\"profile clearfix\">\r\n" +
					"              <div class=\"profile_pic\">\r\n" +
					"                <img src=\"%simages/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">\r\n" +
					"              </div>\r\n" +
					"              <div class=\"profile_info\">\r\n" +
					"                <span>Welcome,</span>\r\n" +
					"                <h2>%s</h2>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu profile quick info -->\r\n" +
					"\r\n" +
					"            <br />\r\n" +
					"\r\n", pro_data_path, lib_path, project.getUserId() );


			//サイドバーメニュー
			writer.printf("            <!-- sidebar menu -->\r\n" +
					"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
					"              <div class=\"menu_section\">\r\n" +
					"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
					"                  <li><a href=\"%sindex.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
					"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-bar-chart\"></i> Analysis <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebaranalysis\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-table\"></i> Clone Set <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                  <ul class=\"nav child_menu\" id=\"sidebarcloneset\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                   <li><a><i class=\"fa fa-file-code-o\"></i> Directory <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebardirectory\">\r\n" +
					"                    </ul>\r\n" +
					"                  </li>\r\n" +
					"                </ul>\r\n" +
					"              </div>\r\n" +
					"            </div>", pro_data_path);


			writer.printf("            <!-- /sidebar menu -->\r\n" +
					"\r\n" +
					"            <!-- /menu footer buttons -->\r\n" +
					"            <div class=\"sidebar-footer hidden-small\">\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
					"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
					"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
					"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
					"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu footer buttons -->\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n" +
					"\r\n" +
					"        <!-- top navigation -->\r\n" +
					"        <div class=\"top_nav\">\r\n" +
					"          <div class=\"nav_menu\">\r\n" +
					"            <nav>\r\n" +
					"              <div class=\"nav toggle\">\r\n" +
					"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
					"              </div>\r\n" +
					"\r\n" +
					"              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
					"                <li class=\"\">\r\n" +
					"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n" +
					"                    <img src=\"%simages/img.jpg\" alt=\"\">%s\r\n" +
					"                    <span class=\" fa fa-angle-down\"></span>\r\n" +
					"                  </a>\r\n" +
					"                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
					"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
					"                  </ul>\r\n" +
					"                </li>\r\n" +
					"              </ul>\r\n" +
					"            </nav>\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n" +
					"        <!-- /top navigation -->\r\n" +
					"", lib_path,project.getUserId());

			/*ページコンテンツ*/
			writer.printf("        <!-- page content -->\r\n" +
					"        <div class=\"right_col\" role=\"main\">\r\n" +
					"          <div class=\"\">\r\n" +
					"            <div class=\"page-title\">\r\n" +
					"              <div class=\"title_left\">\r\n" +
					"                <h3>Project: %s \r\n" +
					"                <!--<small>Some examples of D3</small> -->\r\n" +
					"                </h3>\r\n" +
					"                <h4>Analysis Title: %s</h4>\r\n" +
					"                <h4>Date: %s/%s/%s</h4>\r\n" +
					"              </div>", project.getName(), project.getAnalysisName(), cur_month, cur_day, cur_year);

			//積み上げグラフ
			writer.printf("            </div>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"          <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"              <div class=\"row\">\r\n" +
					"                <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                  <div class=\"x_panel\">\r\n" +
					"                    <div class=\"x_title\">\r\n" +
					"                      <h2>Categorization of Clone Sets\r\n" +
					"                        <!-- <small></small> -->\r\n" +
					"                      </h2>\r\n" +
					"                      <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
					"                        <li>\r\n" +
					"                          <a class=\"collapse-link\">\r\n" +
					"                            <i class=\"fa fa-chevron-up\"></i>\r\n" +
					"                          </a>\r\n" +
					"                        </li>\r\n" +
					"                      </ul>\r\n" +
					"                      <div class=\"clearfix\"></div>\r\n" +
					"                    </div>\r\n" +
					"                    <div class=\"x_content\">\r\n" +
					"                      <div class=\"dashboard-widget-content\">\r\n" +
					"                        <div id='divdash' class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                         <div id='category'> </div>\r\n" +
					"                          <!--<div id='dashboard'  style=\"display: inline-block; _display: inline;\"> </div>\r\n" +
					"                          -->\r\n" +
					"						   </br>\r\n" +
					"                          <svg width=\"960\" height=\"700\">\r\n" +
					"                          <g transform=\"translate(50,50)\"></g>\r\n" +
					"						  <g transform=\"translate(50,50)\" id=\"bars\"></g>\r\n" +
					"						  <g transform=\"translate(300,600)\" id=\"legend\"></g><!--凡例の位置調整-->" +
					"						  <g transform=\"translate(50,50)\" id=\"label\"></g>\r\n" +
					"						  <g transform=\"translate(50,50)\" id=\"label2\"></g>\r\n" +
					"						  </svg>\r\n" +
					"                        </div>\r\n" +
					"                      </div>\r\n" +
					"                    </div>\r\n" +
					"                  </div>\r\n" +
					"                </div>\r\n" +
					"              </div>\r\n" +
					"          </div>\r\n" +
					"");

			/*各クローンセットの折れ線グラフ*/
			writer.printf("          <div class=\"clearfix\"></div>\r\n" +
					"          <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"              <div class=\"row\">\r\n" +
					"                <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                  <div class=\"x_panel\">\r\n" +
					"                    <div class=\"x_title\">\r\n" +
					"                      <h2>Clone Sets\r\n" +
					"                        <!-- <small></small> -->\r\n" +
					"                      </h2>\r\n" +
					"                      <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
					"                        <li>\r\n" +
					"                          <a class=\"collapse-link\">\r\n" +
					"                            <i class=\"fa fa-chevron-up\"></i>\r\n" +
					"                          </a>\r\n" +
					"                        </li>\r\n" +
					"                      </ul>\r\n" +
					"                      <div class=\"clearfix\"></div>\r\n" +
					"                    </div>\r\n" +
					"                    <div class=\"x_content\">\r\n" +
					"                      <div class=\"dashboard-widget-content\">\r\n" +
					"                        <div id='divdash2'class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                     <div class=\"clearfix\"></div>\r\n" +
					"\r\n" +
					"				            <div class=\"row\">\r\n" +
					"				              <div class=\"col-md-12\">\r\n" +
					"				                <div class=\"\">\r\n" +
					"				                  <div class=\"x_content\">\r\n" +
					"				                    <div class=\"row\">\r\n" +
					"				                    </div>\r\n" +
					"\r\n" +
					"				                    <div class=\"row top_tiles\" style=\"margin: 10px 0;\">\r\n");
			if(g.getNewCloneSetNum() > 0) {
				writer.printf("				                        <div class=\"col-md-3 tile content2\">\r\n" +
						"                                  <div class=\"caption2\"><a class=\"link\" href=\"cloneset.html#newcloneset\">Check if <strong style=\"color: red;\">%d</strong> clone sets can be merged</a></div>\r\n" +
						"                                  <a href = \"cloneset.html#newcloneset\"><span style=\"color: red;\">New</span>\r\n" +
						"                                  <h2 style=\"color: red;\">%d</h2></a>\r\n" +
						"				                          <span class=\"sparkline_three_new\" style=\"height: 160px;\">\r\n" +
						"				                            <canvas width=\"200\" height=\"60\" style=\"display: inline-block; vertical-align: top; width: 94px; height: 30px;\"></canvas>\r\n" +
						"				                          </span>\r\n" +
						"				                      </div>", g.getNewCloneSetNum(), g.getNewCloneSetNum());

			}else {
				writer.printf("				                      \r\n" +
						"				                        <div class=\"col-md-3 tile\">\r\n" +
						"				                        <span>New</span>\r\n" +
						"				                        <h2>%d</h2>\r\n" +
						"				                        <span class=\"sparkline_three_new\" style=\"height: 160px;\">\r\n" +
						"				                        	<canvas width=\"200\" height=\"60\" style=\"display: inline-block; vertical-align: top; width: 94px; height: 30px;\"></canvas>\r\n" +
						"				                         </span>\r\n" +
						"				                      </div>\r\n", g.getNewCloneSetNum());
			}


			if(Globalval.inconsistent_cnt > 0) {
				writer.printf("				                      <div class=\"col-md-3 tile content1\">\r\n" +
						"                                <div class=\"caption1\"><a class=\"link\" href=\"cloneset.html#changedcloneset\"><strong style=\"color: red;\">%d</strong> clone sets may have inconsistent modifications</a></div>\r\n" +
						"                                <a href = \"cloneset.html#newcloneset\"><span style=\"color: red;\">Changed</span>\r\n" +
						"                                <h2 style=\"color: red;\">%d</h2></a>\r\n" +
						"				                        <span class=\"sparkline_three_changed\" style=\"height: 160px;\">\r\n" +
						"				                          <canvas width=\"200\" height=\"60\" style=\"display: inline-block; vertical-align: top; width: 94px; height: 30px;\"></canvas>\r\n" +
						"                                </span>\r\n" +
						"				                      </div>", Globalval.inconsistent_cnt, g.getChangedCloneSetNum());
			}else {
				writer.printf("				                      <div class=\"col-md-3 tile\">\r\n" +
						"				                        <span>Changed</span>\r\n" +
						"				                        <h2>%d</h2>\r\n" +
						"				                        <span class=\"sparkline_three_changed\" style=\"height: 160px;\">\r\n" +
						"				                        	<canvas width=\"200\" height=\"60\" style=\"display: inline-block; vertical-align: top; width: 94px; height: 30px;\"></canvas>\r\n" +
						"				                        </span>\r\n" +
						"				                      </div>\r\n", g.getChangedCloneSetNum());
			}
			writer.printf("				                      \r\n" +
					"				                      <div class=\"col-md-3 tile\">\r\n" +
					"				                        <span>Deleted</span>\r\n" +
					"				                        <h2>%d</h2>\r\n" +
					"				                        <span class=\"sparkline_three_deleted\" style=\"height: 160px;\">\r\n" +
					"				                        	<canvas width=\"200\" height=\"60\" style=\"display: inline-block; vertical-align: top; width: 94px; height: 30px;\"></canvas>\r\n" +
					"				                        </span>\r\n" +
					"				                      </div>\r\n", g.getDeletedCloneSetNum());

			writer.printf("				                      <div class=\"col-md-3 tile\">\r\n" +
					"				                        <span>Stable</span>\r\n" +
					"				                        <h2>%d</h2>\r\n" +
					"				                        <span class=\"sparkline_three_stable\" style=\"height: 160px;\">\r\n" +
					"				                        	<canvas width=\"200\" height=\"60\" style=\"display: inline-block; vertical-align: top; width: 94px; height: 30px;\"></canvas>\r\n" +
					"				                         </span>\r\n" +
					"				                      </div>\r\n" +
					"				                      \r\n",  g.getStableCloneSetNum());


			writer.print("				                    </div>\r\n" +
					"				                  </div>\r\n" +
					"				                </div>\r\n" +
					"				              </div>\r\n" +
					"				            </div>\r\n" +
					"                        </div>\r\n" +
					"                      </div>\r\n" +
					"                    </div>\r\n" +
					"                  </div>\r\n" +
					"                </div>\r\n" +
					"              </div>\r\n" +
					"          </div>\r\n");

			writer.printf("          <div class=\"clearfix\"></div>\r\n" +
					"          <div class=\"col-xl-4 col-lg-4 col-md-4 col-sm-4 col-xs-12\">\r\n" +
					"              <div class=\"row\">\r\n" +
					"                <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                  <div class=\"x_panel\">\r\n" +
					"                    <div class=\"x_title\">\r\n" +
					"                      <h2>Sorce Files\r\n" +
					"                        <!-- <small></small> -->\r\n" +
					"                      </h2>\r\n" +
					"                      <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
					"                        <li>\r\n" +
					"                          <a class=\"collapse-link\">\r\n" +
					"                            <i class=\"fa fa-chevron-up\"></i>\r\n" +
					"                          </a>\r\n" +
					"                        </li>\r\n" +
					"                      </ul>\r\n" +
					"                      <div class=\"clearfix\"></div>\r\n" +
					"                    </div>\r\n" +
					"                    <div class=\"x_content\">\r\n" +
					"                      <div class=\"dashboard-widget-content\">\r\n" +
					"                        <div id='divdash2'class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                        <h3>Total:%d</h3><br>\r\n" +
					"                        <h3>Added:%d</h3><br>\r\n" +
					"                        <h3>Deleted:%d</h3><br>\r\n" +
					"                        <h3>Including clones:%d</h3><br>\r\n" +
					"                        </div>\r\n" +
					"                      </div>\r\n" +
					"                    </div>\r\n" +
					"                  </div>\r\n" +
					"                </div>\r\n" +
					"              </div>\r\n" +
					"          </div>\r\n" +
					"", g.getFileNum(), g.getAddedFileNum(), g.getDeletedFileNum(), g.getCloneFileNum());

			//クローン分類情報
			writer.printf("         <!-- クローン分類情報-->\r\n" +
					"          <!--<div class=\"clearfix\"></div> -->\r\n" +
					"          <div class=\"col-xl-8 col-lg-8 col-md-8 col-sm-8 col-xs-12\">\r\n" +
					"          <!--<div class=\"x_panel tile fixed_height_320\">-->\r\n" +
					"              <div class=\"row\">\r\n" +
					"                <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                  <div class=\"x_panel\">\r\n" +
					"                    <div class=\"x_title\">\r\n" +
					"                      <h2>Clones\r\n" +
					"                        <!-- <small></small> -->\r\n" +
					"                      </h2>\r\n" +
					"                      <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
					"                        <li>\r\n" +
					"                          <a class=\"collapse-link\">\r\n" +
					"                            <i class=\"fa fa-chevron-up\"></i>\r\n" +
					"                          </a>\r\n" +
					"                        </li>\r\n" +
					"                      </ul>\r\n" +
					"                      <div class=\"clearfix\"></div>\r\n" +
					"                    </div>\r\n" +
					"                    <div class=\"x_content\">\r\n" +
					"                      <div id='divdash3' class=\"dashboard-widget-content\">\r\n" +
					"                        <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                         <!-- <div id='dashboard'> </div> -->\r\n" +
					"						   <center><div id=\"echart_pie3\" style=\"height:350px;\"></div></center>\r\n" +
					"                        </div>\r\n" +
					"                        <div id=\"world-map-gdp\" class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\" style=\"height:10px;\"></div>\r\n" +
					"                      </div>\r\n" +
					"                    </div>\r\n" +
					"                  </div>\r\n" +
					"                </div>\r\n" +
					"              </div>\r\n" +
					"          </div>\r\n" +
					"        </div>" +
					"");


			writer.printf("        <footer>\r\n" +
					"          <div class=\"pull-right\"> CCEvovis by\r\n" +
					"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"        </footer>\r\n" +
					"        <!-- /footer content -->\r\n" +
					"      </div>\r\n" +
					"    </div>\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>\r\n" +
					"    <!-- D3 chart -->\r\n" +
					"    <script src=\"http://d3js.org/d3.v3.min.js\"></script>\r\n" +
					"    <script src=\"https://d3js.org/d3.v4.min.js\"></script>\r\n" +
					"	<script src=\"http://vizjs.org/viz.v1.3.0.min.js\"></script>\r\n", lib_path, lib_path, lib_path, lib_path);


			//スクリプト
			writer.print("<script src = \"freqData.json\"></script>\r\n");

			writer.print("<script>\r\n" +
					"    var total = [];\r\n" +
					"    var temp = [];\r\n" +
					"    freqData.forEach(function(d,i){\r\n" +
					"          d.total = d.freq.Changed + d.freq.Deleted + d.freq.New;\r\n" +
					"          total[i] = d.total;\r\n" +
					"          //console.log(\"total[i] = \" + total[i]);\r\n" +
					"          console.log(d.freq.New);\r\n" +
					"          temp[i] = d.freq.Changed;\r\n" +
					"    });\r\n" +
					"    \r\n" +
					"    \r\n" +
					"    console.log(freqData[0].freq.Stable);\r\n" +
					"    console.log(freqData.length);\r\n" +
					"    console.log(d3.max(freqData));\r\n" +
					"    var freq = freqData.map(function (p) {\r\n" +
					"  		//return p.freq.Stable + p.freq.Changed + p.freq.Deleted + p.freq.New;\r\n" +
					"      return  p.freq.Deleted  + p.freq.Changed + p.freq.New;\r\n" +
					"	});\r\n" +
					"    console.log(Math.max.apply(null, freq));\r\n" +
					"   \r\n" +
					"    \r\n" +
					"//	var fill = {\"Stable\":\"#337ab7\", \"Changed\":\"#26B99A\", \"Deleted\":\"#4B5F71\", \"New\":\"#d9534f\"};\r\n" +
					"var fill = {\"Deleted\":\"#4B5F71\", \"Changed\":\"#26B99A\", \"New\":\"#d9534f\"};\r\n" +
					"var keys = [\"Deleted\",\"Changed\",\"New\"];\r\n" +
					"var data = Data();\r\n" +
					"console.log(data);\r\n" +
					"		\r\n" +
					"var stack = d3.stack().keys(keys)(data)\r\n" +
					" 	\r\n" +
					"	var bar = viz.bar()\r\n" +
					"		.key(function(d){ console.log(\"d.data.key=\" + d.data.key); return d.data.key})\r\n" +
					"		.value0(function(d){ return d[0]})\r\n" +
					"		.value1(function(d){ return d[1]})\r\n" +
					"		//.valueScale(d3.scaleLinear().domain([0,Math.max.apply(null, freq)]).range([420,0]))\r\n" +
					"		.valueScale(d3.scaleLinear().domain([0,Math.max.apply(null, freq)]).range([420,0]))\r\n" +
					"		//.domanin で入力の範囲.rangeで出力の範囲\r\n" +
					"		\r\n" +
					"		\r\n" +
					"	\r\n" +
					"	var bars = d3.select(\"#bars\")\r\n" +
					"		.selectAll(\".bars\")\r\n" +
					"		.data(stack)\r\n" +
					"		.enter()\r\n" +
					"		.append(\"g\")\r\n" +
					"		.attr(\"class\",\"bars\")\r\n" +
					"		.style(\"fill\",function(d,i){ return fill[d.key]})\r\n" +
					"		//カラーの設定\r\n" +
					"		\r\n" +
					"		\r\n" +
					"		\r\n" +
					"		\r\n" +
					"		//長方形作成\r\n" +
					"	bars.selectAll(\".bar\")\r\n" +
					"		.data(function(d){ return bar.data(d).bars(); })\r\n" +
					"		.enter()\r\n" +
					"		.append(\"rect\")\r\n" +
					"		.attr(\"class\", \"bar\")\r\n" +
					"		.attr(\"x\", function(d){ /*console.log(\"d.x = \" + d.x);*/ return d.x;})\r\n" +
					"		.attr(\"y\", function(d){ /*console.log(\"d.y = \" + d.y);*/ return d.y;})\r\n" +
					"		.attr(\"width\", function(d){console.log(\"d.width = \" + d.width); return d.width;})\r\n" +
					"		.attr(\"height\", function(d){  return d.height;})\r\n" +
					"		//.onMouseOver(function(k){ return legendupdate(k); })\r\n" +
					"		//.on(\"mouseover\", (function(d){ console.log(\"aaa\");})\r\n" +
					"		.on(\"mouseover\", function(d){ console.log(d.data.data.key.substr(0,4) + \"/\" + d.data.data.key.substr(-4, 2) + \"/\" + d.data.data.key.substr(-2) ); $('#category').append(\"<h3>Date:\" +  d.data.data.key.substr(-4, 2) + \"/\" + d.data.data.key.substr(-2) + \"/\" + d.data.data.key.substr(0,4) + \"</h3>\" )})		.on(\"mouseout\", function(d){ console.log(d.data.data.key); $('#category').empty()})\r\n" +
					"		.on(\"click\", function(d){window.open(\"../\" + d.data.data.key + \"/cloneset.html\");})\r\n" +
					"	\r\n" +
					"	\r\n" +
					"	var label = d3.select(\"#label\")\r\n" +
					"		.selectAll(\".bar\")\r\n" +
					"		.data(bar.bars())\r\n" +
					"		.enter()\r\n" +
					"		.append(\"text\")\r\n" +
					"		//.text(functon(d){ retrun total[d];})\r\n" +
					"		//.text(100)\r\n" +
					"		.text(function(d){ console.log(\"text =\" + d.data[1]); return d.data[1];})\r\n" +
					"	//	.attr(\"x\",function(d){ return d.x -13 + d.width/2})\r\n" +
					"	//	.attr(\"y\",function(d){ return d.y -10 + d.height/2})\r\n" +
					"    .attr(\"x\",function(d){ return d.x -13 + d.width/2})\r\n" +
					"		.attr(\"y\",function(d){ return d.y -10})\r\n" +
					"		.attr(\"class\",\"bar\")\r\n" +
					"		.attr(\"fill\",\"gray\")\r\n" +
					"		.attr(\"font-size\", 20);\r\n" +
					"		//バー上部に表示される合計\r\n" +
					"		\r\n" +
					"		\r\n" +
					"\r\n" +
					"		\r\n" +
					"		 \r\n" +
					"	/* bars.append(\"text\")\r\n" +
					" 		.text(total[2])\r\n" +
					" 		.attr(\"fill\",\"gray\")\r\n" +
					" 		.attr(\"y\",100)\r\n" +
					" 		.attr(\"x\",80)\r\n" +
					" 		.attr(\"font-size\", 10);\r\n" +
					"  	*/\r\n" +
					"\r\n" +
					"	//凡例の追加(縦バージョン)\r\n" +
					"	var legend = viz.legend()\r\n" +
					"		.data(keys)\r\n" +
					"		.rows(1)\r\n" +
					"		.height(25)//凡例の物体の高さ\r\n" +
					"		.width(500)//凡例同士の間隔\r\n" +
					"		.size(25)//凡例の物体の横幅\r\n" +
					"		.fill(function(d){ return fill[d]})\r\n" +
					"		.onMouseOver(function(k){ $('#category').append(\"<h3>\" + k +\" Clone Set </h3>\"); return update(k); })\r\n" +
					"		.onMouseOut(function(k){ $('#category').empty(); return update(); })\r\n" +
					"/*\r\n" +
					"	//凡例の追加(縦バージョン)\r\n" +
					"	var legend = viz.legend()\r\n" +
					"		.data(keys)\r\n" +
					"		//.rows(1)\r\n" +
					"		.height(150)\r\n" +
					"		.width(500)\r\n" +
					"		.size(25)\r\n" +
					"		.fill(function(d){ return fill[d]})\r\n" +
					"		.onMouseOver(function(k){ return update(k); })\r\n" +
					"		.onMouseOut(function(k){ return update(); })\r\n" +
					"*/		\r\n" +
					"	d3.select(\"#legend\").call(legend) .on(\"click\", function(){window.open(\"cloneset.html\");})\r\n" +
					"\r\n" +
					"\r\n" +
					"/*\r\n" +
					"	//データ生成\r\n" +
					"	function Data(){\r\n" +
					"	  return d3.range(freqData.length).map(function(i){ return {key:freqData[i].State, Stable: freqData[i].freq.Stable, Changed: freqData[i].freq.Changed, Deleted: freqData[i].freq.Deleted, New: freqData[i].freq.New}; });\r\n" +
					"	}\r\n" +
					"	*/\r\n" +
					"  //データ生成(stable抜き)\r\n" +
					"	function Data(){\r\n" +
					"	  return d3.range(freqData.length).map(function(i){ return {key:freqData[i].State, Deleted: freqData[i].freq.Deleted, Changed: freqData[i].freq.Changed, New: freqData[i].freq.New}; });\r\n" +
					"	}\r\n" +
					"\r\n" +
					"	\r\n" +
					"	 // adjust the bl.ocks frame dimension.\r\n" +
					"	d3.select(self.frameElement).style(\"height\", \"600px\"); \r\n" +
					"		\r\n" +
					"		\r\n" +
					"	bars.append(\"bars\")\r\n" +
					"		.attr(\"transform\",\"translate(0,\"+bar.height()+\")\")\r\n" +
					"		.call(d3.axisBottom().scale(bar.keyScale()))\r\n" +
					"		\r\n" +
					"		\r\n" +
					"\r\n" +
					"\r\n" +
					"	var g = d3.select(\"g\");\r\n" +
					"\r\n" +
					"\r\n" +
					"	/*x軸の日付の値*/ \r\n" +
					"	g.append(\"g\")\r\n" +
					"		.attr(\"transform\",\"translate(0,\"+bar.height()+\")\")\r\n" +
					"       .call(d3.axisBottom().scale(bar.keyScale()).tickFormat(function(d){return   d.substr(-4, 2) + \"/\" + d.substr(-2) + \"/\" +  d.substr(0,4);}))\r\n" +
					"		.selectAll(\"text\")\r\n" +
					"		.attr(\"transform\",\"translate(0,\"+bar.height()+\")\")\r\n" +
					"		.attr(\"transform\", \"rotate(-55)\")\r\n" +
					"		.attr(\"x\", -80)   // X座標を指定する\r\n" +
					"    .attr(\"y\", 25)   // Y座標を指定する\r\n" +
					"		.style(\"text-anchor\", \"start\")\r\n" +
					"		.attr(\"font-size\", 15);\r\n" +
					"		\r\n" +
					"		\r\n" +
					"		\r\n" +
					"		\r\n" +
					"	//var country_name = span.append(\"span\").text(function(d){return d.total});\r\n" +
					" 	\r\n" +
					" 	//for(var i=0; i<total.length;)\r\n" +
					" 	//g.selectAll(\"g\").append(\"text\").text(\"100\")\r\n" +
					" 	//bars.append(\"text\").text(\"200\")\r\n" +
					" 	\r\n" +
					" 	\r\n" +
					"/*x軸の線を追加*/\r\n" +
					"g.append(\"rect\")\r\n" +
					"  .attr(\"width\", 880)\r\n" +
					"  .attr(\"height\", 1)\r\n" +
					"  .attr(\"x\", 0)\r\n" +
					"  .attr(\"y\", 420);\r\n" +
					"\r\n" +
					"  g.append('text') // 縦軸のラベル\r\n" +
					"        .attr('x',  -50)\r\n" +
					"        .attr('y', -30)\r\n" +
					"        .text('Num of Clone Sets')\r\n" +
					"        .attr(\"font-size\", 20);\r\n" +
					"\r\n" +
					"\r\n" +
					"	function legendupdate(k){\r\n" +
					"		\r\n" +
					"	}\r\n" +
					"\r\n" +
					"  var yScale = d3.scaleLinear().domain([0,Math.max.apply(null, freq)]).range([420,0]) \r\n" +
					"   \r\n" +
					"  g.append(\"g\")\r\n" +
					"    .attr(\"class\", \"y axis\")\r\n" +
					"    //.call(d3.axisLeft(yScale).ticks(Math.max.apply(null, freq) / 50))\r\n" +
					"    .call(d3.axisLeft(yScale).ticks(Math.max.apply(null, freq) / (Math.max.apply(null,freq)/5)))\r\n" +
					"    .selectAll(\"text\")\r\n" +
					"    .attr(\"font-size\", 15);\r\n" +
					"\r\n" +
					"	//アップデート\r\n" +
					"	function update(k){\r\n" +
					"\r\n" +
					"	  if(!arguments.length){\r\n" +
					"	   stack = d3.stack().keys(keys)(data);\r\n" +
					"	 //  $(\"#label\").empty();\r\n" +
					"	  } \r\n" +
					"	  else {\r\n" +
					"		//var newdata = data.map(function(d){ return (r = {key:d.key, Stable:0, Changed:0, Deleted:0, New:0}, r[k]=d[k], r); });\r\n" +
					"	  var newdata = data.map(function(d){ return (r = {key:d.key, Deleted:0,Changed:0, New:0}, r[k]=d[k], r); });\r\n" +
					"		stack = d3.stack().keys(keys)(newdata); \r\n" +
					"		\r\n" +
					"		console.warn(\"kita\");\r\n" +
					"	  }\r\n" +
					"	  \r\n" +
					"\r\n" +
					"	  bars\r\n" +
					"		.data(stack)\r\n" +
					"		.selectAll(\".bar\")\r\n" +
					"		.data(function(d){ return bar.data(d).bars(); })\r\n" +
					"		.transition().duration(500)\r\n" +
					"		.attr(\"x\", function(d){ return d.x;})\r\n" +
					"		.attr(\"y\", function(d){ return d.y;})\r\n" +
					"		.attr(\"width\", function(d){ return d.width;})\r\n" +
					"		.attr(\"height\", function(d){ return d.height;});  \r\n" +
					"		\r\n" +
					"		console.warn(\"kita2\");\r\n" +
					"		$(\"#label\").empty();\r\n" +
					"	\r\n" +
					"		\r\n" +
					"		$(function(){\r\n" +
					"    		setTimeout(function(){\r\n" +
					"        var label = d3.select(\"#label\")\r\n" +
					"				.selectAll(\".bar\")\r\n" +
					"				.data(bar.bars())\r\n" +
					"				.enter()\r\n" +
					"				.append(\"text\")\r\n" +
					"				//.text(functon(d){ retrun total[d];})\r\n" +
					"				//.text(100)\r\n" +
					"				.text(function(d){ console.log(\"text =\" + d.data[1]); return d.data[1];})\r\n" +
					"				.attr(\"x\",function(d){ return d.x -13 + d.width/2})\r\n" +
					"				.attr(\"y\",function(d){ return d.y -10 })\r\n" +
					"				.attr(\"class\",\"bar\")\r\n" +
					"				.attr(\"fill\",\"gray\")\r\n" +
					"				.attr(\"font-size\", 20);\r\n" +
					"        	\r\n" +
					"   			 },500);\r\n" +
					"		});\r\n" +
					"		\r\n" +
					"\r\n" +
					"		\r\n" +
					"		\r\n" +
					"		console.warn(\"kita3\");\r\n" +
					"	}\r\n" +
					"	\r\n" +
					"	\r\n" +
					"      \r\n" +
					"      \r\n" +
					"    var MAX_PIE = 0;\r\n" +
					"	  var STABLE_PIE = 0;\r\n" +
					"  	var MODIFIED_PIE = 0;\r\n" +
					"	  var MOVED_PIE = 0;\r\n" +
					"	  var ADDED_PIE = 0;\r\n" +
					"	  var DELETED_PIE = 0;\r\n" +
					"    \r\n" +
					"    freqData.forEach(function(d,i){\r\n" +
					"    	  if(i == freqData.length -1){\r\n" +
					"    	  	STABLE_PIE = d.clone.Stable;\r\n" +
					"    	  	MODIFIED_PIE = d.clone.Modified;\r\n" +
					"    	  	MOVED_PIE = d.clone.Moved;\r\n" +
					"    	  	ADDED_PIE = d.clone.Added;\r\n" +
					"    	  	DELETED_PIE = d.clone.Deleted;\r\n" +
					"    	  	MAX_PIE = Math.max(STABLE_PIE, MODIFIED_PIE, MOVED_PIE, ADDED_PIE,DELETED_PIE);\r\n" +
					"    	  	console.log(\"test\" + MAX_PIE);\r\n" +
					"    	  }\r\n" +
					"    });	"+
					"    </script>\r\n");

			writer.printf("    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>\r\n" +
					"    <!-- Chart.js -->\r\n" +
					"    <script src=\"%svendors/Chart.js/dist/Chart.min.js\"></script>\r\n" +
					"    <!-- gauge.js -->\r\n" +
					"    <script src=\"%svendors/gauge.js/dist/gauge.min.js\"></script>\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <script src=\"%svendors/bootstrap-progressbar/bootstrap-progressbar.min.js\"></script>\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <script src=\"%svendors/iCheck/icheck.min.js\"></script>\r\n" +
					"    <!-- Skycons -->\r\n" +
					"    <script src=\"%svendors/skycons/skycons.js\"></script>\r\n" +
					"    <!-- Flot -->\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.pie.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.time.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.stack.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.resize.js\"></script>\r\n" +
					"    <!-- Flot plugins -->\r\n" +
					"    <script src=\"%svendors/flot.orderbars/js/jquery.flot.orderBars.js\"></script>\r\n" +
					"    <script src=\"%svendors/flot-spline/js/jquery.flot.spline.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/flot.curvedlines/curvedLines.js\"></script>\r\n" +
					"    <!-- DateJS -->\r\n" +
					"    <script src=\"%svendors/DateJS/build/date.js\"></script>\r\n" +
					"    <!-- JQVMap -->\r\n" +
					"    <script src=\"%svendors/jqvmap/dist/jquery.vmap.js\"></script>\r\n" +
					"    <script src=\"%svendors/jqvmap/dist/maps/jquery.vmap.world.js\"></script>\r\n" +
					"    <script src=\"%svendors/jqvmap/examples/js/jquery.vmap.sampledata.js\"></script>\r\n" +
					"    <!-- bootstrap-daterangepicker -->\r\n" +
					"    <script src=\"%svendors/moment/min/moment.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/bootstrap-daterangepicker/daterangepicker.js\"></script>\r\n" +
					"\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>    \r\n" +
					"    <!-- Chart.js -->\r\n" +
					"    <script src=\"%svendors/Chart.js/dist/Chart.min.js\"></script>\r\n" +
					"    <!-- jQuery Sparklines -->\r\n" +
					"    <script src=\"%svendors/jquery-sparkline/dist/jquery.sparkline.min.js\"></script>\r\n" +
					"    <!-- easy-pie-chart -->\r\n" +
					"    <script src=\"%svendors/jquery.easy-pie-chart/dist/jquery.easypiechart.min.js\"></script>\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <script src=\"%svendors/bootstrap-progressbar/bootstrap-progressbar.min.js\"></script>\r\n" +
					"\r\n" +
					"	  <!-- ECharts -->\r\n" +
					"    <script src=\"%svendors/echarts/dist/echarts.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/echarts/map/js/world.js\"></script>" +
					"    <!-- Custom Theme Scripts -->\r\n" +
					"    <script src=\"%sbuild/js/custom.min.js\"></script>\r\n" +
					"	 <!-- Google Analytics -->\r\n" +
					"	 <script>\r\n" +
					"	 (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\r\n" +
					"	 (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\r\n" +
					"	 m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\r\n" +
					" 	 })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\r\n" +
					"	 \r\n" +
					"	 ga('create', 'UA-23581568-13', 'auto');\r\n" +
					"	 ga('send', 'pageview');\r\n" +
					"	 var stable_cloneset_data = [];\r\n" +
					"	 var changed_cloneset_data = [];\r\n" +
					" 	 var deleted_cloneset_data = [];\r\n" +
					"	 var new_cloneset_data = [];\r\n" +
					"	 freqData.forEach(function(d,i){\r\n" +
					"      stable_cloneset_data[i] = d.freq.Stable;\r\n" +
					"      changed_cloneset_data[i] = d.freq.Changed;\r\n" +
					"      deleted_cloneset_data[i] = d.freq.Deleted;\r\n" +
					"      new_cloneset_data[i] = d.freq.New;\r\n" +
					"	 });\r\n" +
					"\r\n" +
					"$(\".sparkline_three_stable\").sparkline(stable_cloneset_data,{type:\"line\",width:\"200\",height:\"40\",lineColor:\"#337ab7\",fillColor:\"rgba(223, 223, 223, 0.57)\",lineWidth:2,spotColor:\"#26B99A\",minSpotColor:\"#26B99A\"})\r\n" +
					"	 $(\".sparkline_three_changed\").sparkline(changed_cloneset_data,{type:\"line\",width:\"200\",height:\"40\",lineColor:\"#26B99A\",fillColor:\"rgba(223, 223, 223, 0.57)\",lineWidth:2,spotColor:\"#26B99A\",minSpotColor:\"#26B99A\"})\r\n" +
					"	 $(\".sparkline_three_deleted\").sparkline(deleted_cloneset_data,{type:\"line\",width:\"200\",height:\"40\",lineColor:\"#4B5F71\",fillColor:\"rgba(223, 223, 223, 0.57)\",lineWidth:2,spotColor:\"#26B99A\",minSpotColor:\"#26B99A\"})\r\n" +
					"	 $(\".sparkline_three_new\").sparkline(new_cloneset_data,{type:\"line\",width:\"200\",height:\"40\",lineColor:\"#d9534f\",fillColor:\"rgba(223, 223, 223, 0.57)\",lineWidth:2,spotColor:\"#26B99A\",minSpotColor:\"#26B99A\"})\r\n" +
					" //キャプションフェードイン\r\n" +
					"$(function () {\r\n" +
					"    $('.content1').hover(function () {\r\n" +
					"        $('.caption1', this).fadeIn(\"slow\");\r\n" +
					"    }, function () {\r\n" +
					"        $('.caption1', this).fadeOut(\"slow\");\r\n" +
					"    });\r\n" +
					"});\r\n" +
					"\r\n" +
					"$(function () {\r\n" +
					"    $('.content2').hover(function () {\r\n" +
					"        $('.caption2', this).fadeIn(\"slow\");\r\n" +
					"    }, function () {\r\n" +
					"        $('.caption2', this).fadeOut(\"slow\");\r\n" +
					"    });\r\n" +
					"});" +
					"</script>\r\n" +
					"  <script src=\"%sdata/projects.json\"></script>\r\n" +
					"  <script src=\"%sdata/analysis.json\"></script>\r\n" +
					"  <script>\r\n" +
					"    //projects配列の探索\r\n" +
					"    for (var i = 0; i < projects.length; i++) {\r\n" +
					"      $('#sidebarproject').append('<li><a href=\"%sprojects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebaranalysis').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/index.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebarcloneset').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/cloneset.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebardirectory').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/packagelist.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    $(function () {\r\n" +
					"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
					"    })\r\n" +
					"  </script>" +
					"	\r\n" +
					"  </body>\r\n" +
					"</html>", lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path ,pro_data_path, data_path, pro_data_path ,data_path, data_path, data_path);


			writer.flush();
			writer.close();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * </p>
	 * クローンセット一覧ページ生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param dir
	 *            出力ディレクトリ
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateCloneSetListPage(OutputGenerator g, String dir, Project project) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir + "\\" + CLONESETLIST_PAGE)));
			/*ヘッダー部分*/
			//String cur_date = project.getDate();
			String cur_date = Integer.toString(project.getAnalysisdate());
			String cur_year = cur_date.substring(0, 4);
			String cur_month = cur_date.substring(4, 6);
			String cur_day = cur_date.substring(6, 8);
			String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
			String old_year = old_date.substring(0, 4);
			String old_month = old_date.substring(4, 6);
			String old_day = old_date.substring(6, 8);
			String lib_path = "../../../../../../";
			String pro_data_path = "../../../../";
			String data_path = "../../";
			writer.printf("<!DOCTYPE html>\r\n" +
					"<html lang=\"en\">\r\n" +
					"  <head>\r\n" +
					"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
					"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
					"    <meta charset=\"utf-8\">\r\n" +
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
					"\r\n" +
					"    <title>CCEvovis </title>\r\n" +
					"\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <link href=\"%svendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Font Awesome -->\r\n" +
					"    <link href=\"%svendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <link href=\"%svendors/nprogress/nprogress.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <link href=\"%svendors/iCheck/skins/flat/green.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Datatables -->\r\n" +
					"    <link href=\"%svendors/datatables.net-bs/css/dataTables.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-buttons-bs/css/buttons.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-fixedheader-bs/css/fixedHeader.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-responsive-bs/css/responsive.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-scroller-bs/css/scroller.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    \r\n" +
					"       <!-- bootstrap-daterangepicker -->\r\n" +
					"    <link href=\"%svendors/bootstrap-daterangepicker/daterangepicker.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Ion.RangeSlider -->\r\n" +
					"    <link href=\"%svendors/normalize-css/normalize.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/ion.rangeSlider/css/ion.rangeSlider.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/ion.rangeSlider/css/ion.rangeSlider.skinFlat.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Bootstrap Colorpicker -->\r\n" +
					"    <link href=\"%svendors/mjolnic-bootstrap-colorpicker/dist/css/bootstrap-colorpicker.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <link href=\"%svendors/cropper/dist/cropper.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Style -->\r\n" +
					"    <link href=\"%sbuild/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
					"    <style>\r\n" +
					"    button#btnmodified{\r\n" +
					"    	background-color: #807dba;\r\n" +
					"    	color: white;\r\n" +
					"    }\r\n" +
					"	button#btnadded{\r\n" +
					"    	background-color: #FFC0CB;\r\n" +
					"    	color: white;\r\n" +
					"    }" +
					"    </style>\r\n" +
					"   \r\n" +
					"  </head>\r\n", lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path, lib_path);

			/*ナビゲーション部分*/
			writer.printf("  <body class=\"nav-md\">\r\n" +
					"    <div class=\"container body\">\r\n" +
					"      <div class=\"main_container\">\r\n" +
					"        <div class=\"col-md-3 left_col\">\r\n" +
					"          <div class=\"left_col scroll-view\">\r\n" +
					"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
					"              <a href=\"%sindex.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
					"            </div>\r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"\r\n" +
					"            <!-- menu profile quick info -->\r\n" +
					"            <div class=\"profile clearfix\">\r\n" +
					"              <div class=\"profile_pic\">\r\n" +
					"                <img src=\"%simages/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">\r\n" +
					"              </div>\r\n" +
					"              <div class=\"profile_info\">\r\n" +
					"                <span>Welcome,</span>\r\n" +
					"                <h2>%s</h2>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n", pro_data_path, lib_path,project.getUserId());

			writer.printf("            <!-- sidebar menu -->\r\n" +
					"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
					"              <div class=\"menu_section\">\r\n" +
					"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
					"                  <li><a href=\"%sindex.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
					"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-bar-chart\"></i> Analysis <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebaranalysis\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-table\"></i> Clone Set <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                  <ul class=\"nav child_menu\" id=\"sidebarcloneset\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                   <li><a><i class=\"fa fa-file-code-o\"></i> Directory <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebardirectory\">\r\n" +
					"                    </ul>\r\n" +
					"                  </li>\r\n" +
					"                </ul>\r\n" +
					"              </div>\r\n" +
					"            </div>",pro_data_path);

			//sidebar menu
			writer.printf("            <!-- /sidebar menu -->\r\n" +
					"\r\n" +
					"            <!-- /menu footer buttons -->\r\n" +
					"            <div class=\"sidebar-footer hidden-small\">\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
					"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
					"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
					"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
					"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu footer buttons -->\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n" +
					"\r\n");
			//分割
			writer.printf("        <!-- /top navigation -->\r\n" +
					"        <div class=\"top_nav\">\r\n" +
					"          <div class=\"nav_menu\">\r\n" +
					"            <nav>\r\n" +
					"              <div class=\"nav toggle\">\r\n" +
					"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
					"              </div>\r\n" +
					"\r\n" +
					"              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
					"                <li class=\"\">\r\n" +
					"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n" +
					"                    <img src=\"%simages/img.jpg\" alt=\"\">%s\r\n" +
					"                    <span class=\" fa fa-angle-down\"></span>\r\n" +
					"                  </a>\r\n", lib_path,project.getUserId());
			writer.print("                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
					"                    <li><a href=\"javascript:;\"> Profile</a></li>\r\n" +
					"                    <li>\r\n" +
					"                      <a href=\"javascript:;\">\r\n" +
					"                        <span class=\"badge bg-red pull-right\">50%</span>\r\n" +
					"                        <span>Settings</span>\r\n" +
					"                      </a>\r\n" +
					"                    </li>\r\n");
			writer.printf("                    <li><a href=\"javascript:;\">Help</a></li>\r\n" +
					"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
					"                  </ul>\r\n" +
					"                </li>\r\n");
			/*分割*/
			writer.printf("                    <li>\r\n" +
					"                  </ul>\r\n" +
					"                </li>\r\n" +
					"              </ul>\r\n" +
					"            </nav>\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n");

			///////////////////////////////////////////
			/*ページコンテンツ*/
			writer.printf("        <div class=\"right_col\" role=\"main\">\r\n" +
					"          <div class=\"\">\r\n" +
					"            <div class=\"page-title\">\r\n" +
					"              <div class=\"title_left\">\r\n");
			//プロジェクト名と日付を入力
			if(project.isGitDirect()) {
				writer.printf("                <h3>Project: %s</h3>" +
						"                      <h4>Analysis Title: %s</h4>\r\n" +
						"								<h4>Previous Ver. %s/%s/%s <input id=\"oldcommitID\" type=\"text\" size=\"37\" value=\"%s\" readonly=\"readonly\"><button style=\"WIDTH: 42px; HEIGHT: 30px\" class=\"btn btn-default fa fa-clipboard\" onclick=\"copyToClipboard1();\"></button></h4>\r\n" +
						"								<h4>Current&nbsp&nbsp Ver. %s/%s/%s <input id=\"newcommitID\" type=\"text\" size=\"37\" value=\"%s\" readonly=\"readonly\"><button style=\"WIDTH: 42px; HEIGHT: 30px\" class=\"btn btn-default fa fa-clipboard\" type=\"button\" onclick=\"copyToClipboard2();\"></button></h4>"
						, project.getName(), project.getAnalysisName() ,old_month, old_day, old_year, project.getCommitIDList().get(project.getAnalysistime()-1), cur_month, cur_day,cur_year,  project.getCommitIDList().get(project.getAnalysistime()));
			}else {
				writer.printf("                <h3>Project: %s</h3>" +
						"                      <h4>Analysis Title: %s</h4>\r\n" +
						"								<h4>Previous Ver. %s/%s/%s </h4>\r\n" +
						"								<h4>Current&nbsp&nbsp Ver. %s/%s/%s </h4>"
						, project.getName(), project.getAnalysisName() ,old_month, old_day, old_year,  cur_month, cur_day,cur_year );

			}
			writer.printf("              </div>\r\n" +
					"            </div>\r\n" +
					"            \r\n" +
					"            \r\n" +
					"            \r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"            <div class=\"row\">\r\n" +
					"\r\n" +
					"              <div class=\"col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                <div class=\"x_panel\">\r\n" +
					"                  <div class=\"x_title\">\r\n" +
					"                    <h2>Clone Set List</small></h2>\r\n" +
					"                    <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
					"                      <li><a class=\"collapse-link\"><i class=\"fa fa-chevron-up\"></i></a>\r\n" +
					"                      </li>\r\n" +
					"                    </ul>\r\n" +
					"                    <div class=\"clearfix\"></div>\r\n" +
					"                  </div>\r\n" +
					"                  <div class=\"x_content\">\r\n" +
					"                    <p class=\"text-muted font-13 m-b-30\">\r\n" +
					"                 \r\n" +
					"                    </p>\r\n" +
					"                    <button type=\"button\" class=\"btn btn-danger\" onclick=\"location.href='#newcloneset'\">NEW</button>\r\n" +
					"                    <button type=\"button\" class=\"btn btn-success\" onclick=\"location.href='#changedcloneset'\">CHANGED</button>\r\n" +
					"                    <button type=\"button\" class=\"btn btn-dark\" onclick=\"location.href='#deletedcloneset'\">DELETED</button>\r\n" +
					"                    <button type=\"button\" class=\"btn btn-primary\" id = \"btnstable\" onclick=\"location.href='#stablecloneset'\">STABLE</button>\r\n" +
					"                    <table id=\"datatable-checkbox\" class=\"table table-striped table-bordered bulk_action\">\r\n" +
					"                      <thead>\r\n" +
					"                        <tr>\r\n" +
					"                          <th width=\"5\">\r\n" +
					"							 <th width=\"50\"><input type=\"checkbox\" id=\"check-all\" class=\"flat\"></th>\r\n" +
					"						  </th>\r\n" +
					"						   <th width=\"50\">Link</th>\r\n" +
					"						   <th width=\"50\">ID</th>\r\n" +
					"                          <th width=\"100\">Classification</th>\r\n");
			// クローンセットが全く無い場合
			if (project.getCloneSetList().isEmpty()) {
				writer.printf("<hr>\r\n");
				writer.printf("Clone set does not exist\r\n ");
			} else {
				if (project.getTool().equals(Def.CCFX_TOOLNAME)) {
					writer.printf("    					  <th >LEN</th>\r\n" +
							"    				      <th>POP</th>\r\n" +
							"    					  <th>NIF</th>\r\n" +
							"    					  <th>RAD</th>\r\n" +
							"    					  <th>RNR</th>\r\n" +
							"    					  <th>TKS</th>\r\n" +
							"						  <th>LOOP</th>\r\n" +
							"						  <th>COND</th>\r\n" +
							"						  <th>McCabe</th>\r\n"
							);
				}
				writer.printf("                        </tr>\r\n" +
						"                      </thead>\r\n" +
						"                      <tbody>\r\n"
						);

				for (CloneSet cloneSet : project.getCloneSetList()) {
					writer.printf("						 <tr>\r\n" +
							"                          <td>\r\n" +
							"							 <th><input type=\"checkbox\" id=\"check-all\" class=\"flat\"></th>\r\n" +
							"						  </td>\r\n");
					if(cloneSet.getCategory() == CloneSet.STABLE) {
						writer.printf("						  <td><center><a href=\"stablecloneset.html#cloneset%d\"><span class=\"glyphicon glyphicon-search\" aria-hidden=\"true\" style=\"font-size:large\"></span></a></center></td>\r\n", cloneSet.getOutputId());
						writer.printf("					  <td><a href=\"stablecloneset.html#cloneset%d\">%d</a></td>\r\n", cloneSet.getOutputId(),
								cloneSet.getOutputId());

					}else {
						writer.printf("						  <td><center><a href=\"cloneset.html#cloneset%d\"><span class=\"glyphicon glyphicon-search\" aria-hidden=\"true\" style=\"font-size:large\"></span></a></center></td>\r\n", cloneSet.getOutputId());
						writer.printf("					  <td><a href=\"cloneset.html#cloneset%d\">%d</a></td>\r\n", cloneSet.getOutputId(),
								cloneSet.getOutputId());
					}
					writer.printf("						<td bgcolor=\"%s\" style=\"color:white\">%s</td>\r\n", getCloneSetColor(cloneSet), cloneSet.getCategoryString());

					if (project.getTool().equals(Def.CCFX_TOOLNAME)) {
						writer.printf("<td>%d</td>\r\n", cloneSet.getLEN());
						writer.printf("<td>%d</td>\r\n", cloneSet.getPOP());
						writer.printf("<td>%d</td>\r\n", cloneSet.getNIF());
						writer.printf("<td>%d</td>\r\n", cloneSet.getRAD());
						writer.printf("<td>%f</td>\r\n", cloneSet.getRNR());
						writer.printf("<td>%d</td>\r\n", cloneSet.getTKS());
						writer.printf("<td>%d</td>\r\n", cloneSet.getLOOP());
						writer.printf("<td>%d</td>\r\n", cloneSet.getCOND());
						writer.printf("<td>%d</td>\r\n", cloneSet.getMcCabe());
					}
					writer.printf("					</tr>\r\n");
				}
				writer.printf("                      </tbody>\r\n" +
						"                    </table>\r\n" +
						"                  </div>\r\n" +
						"                </div>\r\n" +
						"              </div>\r\n" +
						"            </div>\r\n");
				/*クローンセット一覧表示終了*/

				/*NEW CLONE SET*/
				writer.printf("            <div class=\"clearfix\"></div>\r\n" +
						"            <div class=\"row\">\r\n" +
						"\r\n" +
						"              <div class=\"col-md-12 col-sm-12 col-xs-12\" id=\"newcloneset\">\r\n" +
						"                <div class=\"x_panel\">\r\n" +
						"                  <div class=\"x_title\">\r\n" +
						"                    <h2>NEW Clone Set</h2>\r\n" +
						"                    <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
						"                      <li><a class=\"collapse-link\"><i class=\"fa fa-chevron-up\"></i></a>\r\n" +
						"                      </li>\r\n" +
						"                    </ul>\r\n" +
						"                    <div class=\"clearfix\"></div>\r\n" +
						"                  </div>\r\n" +
						"                  <div class=\"x_content\">\r\n" +
						"                    <p class=\"text-muted font-13 m-b-30\">\r\n" +
						"                 \r\n" +
						"                    </p>\r\n");
				if (g.getNewCloneSetNum() == 0) {
					writer.printf("New Clone Set does not exist\r\n ");
					writer.print("                  </div>\r\n" +
							"                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>");
				} else {
					for (CloneSet cloneSet : project.getCloneSetList()) {
						if (cloneSet.getCategory() == CloneSet.NEW) {
							outputCloneSet(writer, project, cloneSet);
						}
					}
					writer.print("                  </div>\r\n" +
							"                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>");
				}



				/*CHANGED CLONE SET*/
				writer.printf("            <div class=\"clearfix\"></div>\r\n" +
						"            <div class=\"row\">\r\n" +
						"\r\n" +
						"              <div class=\"col-md-12 col-sm-12 col-xs-12\" id=\"changedcloneset\">\r\n" +
						"                <div class=\"x_panel\">\r\n" +
						"                  <div class=\"x_title\">\r\n" +
						"                    <h2>CHANGED Clone Set</h2>\r\n" +
						"                    <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
						"                      <li><a class=\"collapse-link\"><i class=\"fa fa-chevron-up\"></i></a>\r\n" +
						"                      </li>\r\n" +
						"                    </ul>\r\n" +
						"                    <div class=\"clearfix\"></div>\r\n" +
						"                  </div>\r\n" +
						"                  <div class=\"x_content\">\r\n" +
						"                    <p class=\"text-muted font-13 m-b-30\">\r\n" +
						"                 \r\n" +
						"                    </p>\r\n");

				if (g.getChangedCloneSetNum() == 0) {
					writer.printf("Changed Clone Set does not exist\r\n ");
					writer.printf("                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>\r\n");
				} else {
					for (CloneSet cloneSet : project.getCloneSetList()) {
						if (cloneSet.getCategory() == CloneSet.CHANGED) {
							outputCloneSet(writer, project, cloneSet);
						}
					}
					writer.print("                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>");
				}


				/*DELETED CLONE SET*/
				writer.printf("            <div class=\"clearfix\"></div>\r\n" +
						"            <div class=\"row\">\r\n" +
						"\r\n" +
						"              <div class=\"col-md-12 col-sm-12 col-xs-12\" id=\"deletedcloneset\">\r\n" +
						"                <div class=\"x_panel\">\r\n" +
						"                  <div class=\"x_title\">\r\n" +
						"                    <h2>DELETED Clone Set</h2>\r\n" +
						"                    <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
						"                      <li><a class=\"collapse-link\"><i class=\"fa fa-chevron-up\"></i></a>\r\n" +
						"                      </li>\r\n" +
						"                    </ul>\r\n" +
						"                    <div class=\"clearfix\"></div>\r\n" +
						"                  </div>\r\n" +
						"                  <div class=\"x_content\">\r\n" +
						"                    <p class=\"text-muted font-13 m-b-30\">\r\n" +
						"                 \r\n" +
						"                    </p>\r\n");

				if (g.getDeletedCloneSetNum() == 0) {
					writer.printf("Deleted Clone Set does not exist\r\n ");
					writer.printf("                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>\r\n");
				} else {
					for (CloneSet cloneSet : project.getCloneSetList()) {
						if (cloneSet.getCategory() == CloneSet.DELETED) {
							outputCloneSet(writer, project, cloneSet);
						}
					}
					writer.print("                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>");
				}

				// 現状維持クローンセット一覧
				writer.printf("            <div class=\"clearfix\"></div>\r\n" +
						"            <div class=\"row\">\r\n" +
						"\r\n" +
						"              <div class=\"col-md-12 col-sm-12 col-xs-12\" id=\"stablecloneset\">\r\n" +
						"                <div class=\"x_panel\">\r\n" +
						"                  <div class=\"x_title\">\r\n" +
						"                    <h2>STABLE Clone Set</h2>\r\n" +
						"                    <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
						"                      <li><a class=\"collapse-link\"><i class=\"fa fa-chevron-up\"></i></a>\r\n" +
						"                      </li>\r\n" +
						"                    </ul>\r\n" +
						"                    <div class=\"clearfix\"></div>\r\n" +
						"                  </div>\r\n" +
						"                  <div class=\"x_content\">\r\n" +
						"                    <p class=\"text-muted font-13 m-b-30\">\r\n" +
						"                 \r\n" +
						"                    </p>\r\n");

				if (g.getStableCloneSetNum() == 0) {
					writer.printf("Stable Clone Set does not exist\r\n ");
					writer.printf("                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>\r\n");
				} else {
					writer.printf("                        <a href=\"stablecloneset.html\">Click Here For Stable Clone Set Info!</a>\r\n");
					writer.printf("                  </div>\r\n" +
							"                </div>\r\n" +
							"              </div>\r\n" +
							"            </div>\r\n");
				}
			}
			writer.printf("          </div>\r\n" +
					"        </div>\r\n" +
					"        <!-- /page content -->\r\n" +
					"\r\n");


			//フッター
			writer.printf("        <!-- footer content -->\r\n" +
					"        <footer>\r\n" +
					"          <div class=\"pull-right\"> CCEvovis by\r\n" +
					"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"        </footer>\r\n" +
					"        <!-- /footer content -->\r\n");

			writer.printf("      </div>\r\n" +
					"    </div>\r\n" +
					"\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <script src=\"%svendors/iCheck/icheck.min.js\"></script>\r\n" +
					"    <!-- Datatables -->\r\n" +
					"    <script src=\"%svendors/datatables.net/js/jquery.dataTables.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-bs/js/dataTables.bootstrap.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/dataTables.buttons.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons-bs/js/buttons.bootstrap.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/buttons.flash.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/buttons.html5.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/buttons.print.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-fixedheader/js/dataTables.fixedHeader.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-keytable/js/dataTables.keyTable.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-responsive/js/dataTables.responsive.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-responsive-bs/js/responsive.bootstrap.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-scroller/js/dataTables.scroller.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/jszip/dist/jszip.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/pdfmake/build/pdfmake.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/pdfmake/build/vfs_fonts.js\"></script>\r\n" +
					"\r\n" +
					"    <!-- copytoclipboard -->\r\n" +
					"	 <script src=\"%sjs/copytoclipboard.js\"></script>" +
					"    <!-- Custom Theme Scripts -->\r\n" +
					"    <script src=\"%sbuild/js/custom.min.js\"></script>\r\n" +
					"    <!-- bootstrap-daterangepicker -->\r\n" +
					"    <script src=\"%svendors/moment/min/moment.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/bootstrap-daterangepicker/daterangepicker.js\"></script>\r\n" +
					"    <!-- Ion.RangeSlider -->\r\n" +
					"    <script src=\"%svendors/ion.rangeSlider/js/ion.rangeSlider.min.js\"></script>\r\n" +
					"    <!-- Bootstrap Colorpicker -->\r\n" +
					"    <script src=\"%svendors/mjolnic-bootstrap-colorpicker/dist/js/bootstrap-colorpicker.min.js\"></script>\r\n" +
					"    <!-- jquery.inputmask -->\r\n" +
					"    <script src=\"%svendors/jquery.inputmask/dist/min/jquery.inputmask.bundle.min.js\"></script>\r\n" +
					"    <!-- jQuery Knob -->\r\n" +
					"    <script src=\"%svendors/jquery-knob/dist/jquery.knob.min.js\"></script>\r\n" +
					"    <script src=\"%sdata/projects.json\"></script>\r\n" +
					"    <script src=\"%sdata/analysis.json\"></script>\r\n" +
					"  <script>\r\n" +
					"    //projects配列の探索\r\n" +
					"    for (var i = 0; i < projects.length; i++) {\r\n" +
					"      $('#sidebarproject').append('<li><a href=\"%sprojects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebaranalysis').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/index.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebarcloneset').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/cloneset.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebardirectory').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/packagelist.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    $(function () {\r\n" +
					"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
					"    })\r\n" +
					"  </script>" +
					"\r\n" +
					"\r\n" +
					"\r\n" +
					"	\r\n" +
					"  </body>\r\n" +
					"</html>\r\n", lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, pro_data_path, data_path ,pro_data_path, data_path, data_path, data_path);

			writer.flush();
			writer.close();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * </p>
	 * クローンセット一覧ページ生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param dir
	 *            出力ディレクトリ
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateStableClonesetPage(OutputGenerator g, String dir, Project project) {
		try {

			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir + "\\" + STABLECLONESET_PAGE)));

			//String cur_date = project.getDate();
			String cur_date = Integer.toString(project.getAnalysisdate());
			String cur_year = cur_date.substring(0, 4);
			String cur_month = cur_date.substring(4, 6);
			String cur_day = cur_date.substring(6, 8);
			String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
			String old_year = old_date.substring(0, 4);
			String old_month = old_date.substring(4, 6);
			String old_day = old_date.substring(6, 8);
			String lib_path = "../../../../../../";
			String pro_data_path = "../../../../";
			String data_path = "../../";
			writer.printf("<!DOCTYPE html>\r\n" +
					"<html lang=\"en\">\r\n" +
					"  <head>\r\n" +
					"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
					"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
					"    <meta charset=\"utf-8\">\r\n" +
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
					"\r\n" +
					"    <title>CCEvovis </title>\r\n" +
					"\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <link href=\"%svendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Font Awesome -->\r\n" +
					"    <link href=\"%svendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <link href=\"%svendors/nprogress/nprogress.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <link href=\"%svendors/iCheck/skins/flat/green.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Datatables -->\r\n" +
					"    <link href=\"%svendors/datatables.net-bs/css/dataTables.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-buttons-bs/css/buttons.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-fixedheader-bs/css/fixedHeader.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-responsive-bs/css/responsive.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-scroller-bs/css/scroller.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    \r\n" +
					"       <!-- bootstrap-daterangepicker -->\r\n" +
					"    <link href=\"%svendors/bootstrap-daterangepicker/daterangepicker.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Ion.RangeSlider -->\r\n" +
					"    <link href=\"%svendors/normalize-css/normalize.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/ion.rangeSlider/css/ion.rangeSlider.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/ion.rangeSlider/css/ion.rangeSlider.skinFlat.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Bootstrap Colorpicker -->\r\n" +
					"    <link href=\"%svendors/mjolnic-bootstrap-colorpicker/dist/css/bootstrap-colorpicker.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <link href=\"%svendors/cropper/dist/cropper.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Style -->\r\n" +
					"    <link href=\"%sbuild/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
					"    <style>\r\n" +
					"    button#btnmodified{\r\n" +
					"    	background-color: #807dba;\r\n" +
					"    	color: white;\r\n" +
					"    }\r\n" +
					"	button#btnadded{\r\n" +
					"    	background-color: #FFC0CB;\r\n" +
					"    	color: white;\r\n" +
					"    }" +
					"    </style>\r\n" +
					"   \r\n" +
					"  </head>\r\n",lib_path, lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path);

			/*ナビゲーション部分*/
			writer.printf("  <body class=\"nav-md\">\r\n" +
					"    <div class=\"container body\">\r\n" +
					"      <div class=\"main_container\">\r\n" +
					"        <div class=\"col-md-3 left_col\">\r\n" +
					"          <div class=\"left_col scroll-view\">\r\n" +
					"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
					"              <a href=\"%sindex.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
					"            </div>\r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"\r\n" +
					"            <!-- menu profile quick info -->\r\n" +
					"            <div class=\"profile clearfix\">\r\n" +
					"              <div class=\"profile_pic\">\r\n" +
					"                <img src=\"%simages/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">\r\n" +
					"              </div>\r\n" +
					"              <div class=\"profile_info\">\r\n" +
					"                <span>Welcome,</span>\r\n" +
					"                <h2>%s</h2>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n", pro_data_path, lib_path,project.getUserId());

			//サイドバー生成終了
			writer.printf("            <!-- sidebar menu -->\r\n" +
					"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
					"              <div class=\"menu_section\">\r\n" +
					"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
					"                  <li><a href=\"%sindex.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
					"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-bar-chart\"></i> Analysis <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebaranalysis\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-table\"></i> Clone Set <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                  <ul class=\"nav child_menu\" id=\"sidebarcloneset\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                   <li><a><i class=\"fa fa-file-code-o\"></i> Directory <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebardirectory\">\r\n" +
					"                    </ul>\r\n" +
					"                  </li>\r\n" +
					"                </ul>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n", pro_data_path);
			//sidebar menu
			writer.printf("            <!-- /sidebar menu -->\r\n" +
					"\r\n" +
					"            <!-- /menu footer buttons -->\r\n" +
					"            <div class=\"sidebar-footer hidden-small\">\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
					"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
					"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
					"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
					"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu footer buttons -->\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n" +
					"\r\n");
			//分割
			writer.printf("        <!-- /top navigation -->\r\n" +
					"        <div class=\"top_nav\">\r\n" +
					"          <div class=\"nav_menu\">\r\n" +
					"            <nav>\r\n" +
					"              <div class=\"nav toggle\">\r\n" +
					"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
					"              </div>\r\n" +
					"\r\n" +
					"              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
					"                <li class=\"\">\r\n" +
					"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n" +
					"                    <img src=\"%simages/img.jpg\" alt=\"\">%s\r\n" +
					"                    <span class=\" fa fa-angle-down\"></span>\r\n" +
					"                  </a>\r\n", lib_path,project.getUserId());
			writer.print("                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
					"                    <li><a href=\"javascript:;\"> Profile</a></li>\r\n" +
					"                    <li>\r\n" +
					"                      <a href=\"javascript:;\">\r\n" +
					"                        <span class=\"badge bg-red pull-right\">50%</span>\r\n" +
					"                        <span>Settings</span>\r\n" +
					"                      </a>\r\n" +
					"                    </li>\r\n");
			writer.printf("                    <li><a href=\"javascript:;\">Help</a></li>\r\n" +
					"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
					"                  </ul>\r\n" +
					"                </li>\r\n");
			/*分割*/
			writer.printf("                    <li>\r\n" +
					"                  </ul>\r\n" +
					"                </li>\r\n" +
					"              </ul>\r\n" +
					"            </nav>\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n");

			///////////////////////////////////////////
			/*ページコンテンツ*/
			writer.printf("        <div class=\"right_col\" role=\"main\">\r\n" +
					"          <div class=\"\">\r\n" +
					"            <div class=\"page-title\">\r\n" +
					"              <div class=\"title_left\">\r\n");
			//プロジェクト名と日付を入力
			writer.printf("                <h3>Project: %s</h3>" +
					" 			   <h4>Date: %s/%s/%s </h4>\r\n", project.getName(),cur_year, cur_month, cur_day);
			writer.printf("                <div class=\"x_content\">\r\n" +
					"                        <fieldset>\r\n" +
					"                          <div class=\"control-group\">\r\n" +
					"                            <div class=\"controls\">\r\n" +
					"                              <div class=\"col-md-11 xdisplay_inputx form-group has-feedback\">\r\n" +
					"                                <input type=\"text\" class=\"form-control has-feedback-left\" id=\"single_cal4\" placeholder=\"First Name\" aria-describedby=\"inputSuccess2Status4\">\r\n" +
					"                                <span class=\"fa fa-calendar-o form-control-feedback left\" aria-hidden=\"true\"></span>\r\n" +
					"                                <span id=\"inputSuccess2Status4\" class=\"sr-only\">(success)</span>\r\n" +
					"                              </div>\r\n" +
					"                            </div>\r\n" +
					"                          </div>\r\n" +
					"                        </fieldset>\r\n" +
					"                      </div>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n" +
					"            \r\n" +
					"            \r\n" +
					"            \r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"            <div class=\"row\">\r\n" +
					"              <div class=\"col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                <div class=\"x_panel\">\r\n" +
					"                  <div class=\"x_title\">\r\n" +
					"                    <h2>STABLE Clone Set</small></h2>\r\n" +
					"                    <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
					"                      <li><a class=\"collapse-link\"><i class=\"fa fa-chevron-up\"></i></a>\r\n" +
					"                      </li>\r\n" +
					"                      <li class=\"dropdown\">\r\n" +
					"                        <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" role=\"button\" aria-expanded=\"false\"><i class=\"fa fa-wrench\"></i></a>\r\n" +
					"                        <ul class=\"dropdown-menu\" role=\"menu\">\r\n" +
					"                          <li><a href=\"#\">Settings 1</a>\r\n" +
					"                          </li>\r\n" +
					"                          <li><a href=\"#\">Settings 2</a>\r\n" +
					"                          </li>\r\n" +
					"                        </ul>\r\n" +
					"                      </li>\r\n" +
					"                      <li><a class=\"close-link\"><i class=\"fa fa-close\"></i></a>\r\n" +
					"                      </li>\r\n" +
					"                    </ul>\r\n" +
					"                    <div class=\"clearfix\"></div>\r\n" +
					"                  </div>\r\n" +
					"                  <div class=\"x_content\">\r\n" +
					"                    <p class=\"text-muted font-13 m-b-30\">\r\n" +
					"                 \r\n" +
					"                    </p>\r\n");

			if (g.getStableCloneSetNum() == 0) {
				writer.printf("Stable Clone Set does not exist\r\n ");
				writer.printf("                  </div>\r\n" +
						"                </div>\r\n" +
						"              </div>\r\n" +
						"            </div>\r\n");
			} else {
				for (CloneSet cloneSet : project.getCloneSetList()) {
					if (cloneSet.getCategory() == CloneSet.STABLE) {
						outputCloneSet(writer, project, cloneSet);
					}
				}
				writer.printf("                  </div>\r\n" +
						"                </div>\r\n" +
						"              </div>\r\n" +
						"            </div>\r\n");
			}
			writer.printf("          </div>\r\n" +
					"        </div>\r\n" +
					"        <!-- /page content -->\r\n" +
					"\r\n");


			//フッター
			writer.print("        <!-- footer content -->\r\n" +
					"        <footer>\r\n" +
					"          <div class=\"pull-right\"> CCEvovis by\r\n" +
					"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"        </footer>\r\n" +
					"        <!-- /footer content -->\r\n");

			writer.printf("      </div>\r\n" +
					"    </div>\r\n" +
					"\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <script src=\"%svendors/iCheck/icheck.min.js\"></script>\r\n" +
					"    <!-- Datatables -->\r\n" +
					"    <script src=\"%svendors/datatables.net/js/jquery.dataTables.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-bs/js/dataTables.bootstrap.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/dataTables.buttons.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons-bs/js/buttons.bootstrap.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/buttons.flash.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/buttons.html5.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-buttons/js/buttons.print.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-fixedheader/js/dataTables.fixedHeader.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-keytable/js/dataTables.keyTable.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-responsive/js/dataTables.responsive.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-responsive-bs/js/responsive.bootstrap.js\"></script>\r\n" +
					"    <script src=\"%svendors/datatables.net-scroller/js/dataTables.scroller.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/jszip/dist/jszip.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/pdfmake/build/pdfmake.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/pdfmake/build/vfs_fonts.js\"></script>\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Scripts -->\r\n" +
					"    <script src=\"%sbuild/js/custom.min.js\"></script>\r\n" +
					"    <!-- bootstrap-daterangepicker -->\r\n" +
					"    <script src=\"%svendors/moment/min/moment.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/bootstrap-daterangepicker/daterangepicker.js\"></script>\r\n" +
					"    <!-- Ion.RangeSlider -->\r\n" +
					"    <script src=\"%svendors/ion.rangeSlider/js/ion.rangeSlider.min.js\"></script>\r\n" +
					"    <!-- Bootstrap Colorpicker -->\r\n" +
					"    <script src=\"%svendors/mjolnic-bootstrap-colorpicker/dist/js/bootstrap-colorpicker.min.js\"></script>\r\n" +
					"    <!-- jquery.inputmask -->\r\n" +
					"    <script src=\"%svendors/jquery.inputmask/dist/min/jquery.inputmask.bundle.min.js\"></script>\r\n" +
					"    <!-- jQuery Knob -->\r\n" +
					"    <script src=\"%svendors/jquery-knob/dist/jquery.knob.min.js\"></script>\r\n" +
					"    <script src=\"%sdata/projects.json\"></script>\r\n" +
					"    <script src=\"%sdata/analysis.json\"></script>\r\n" +
					"    <script>\r\n" +
					"    //projects配列の探索\r\n" +
					"    for (var i = 0; i < projects.length; i++) {\r\n" +
					"      $('#sidebarproject').append('<li><a href=\"%sprojects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebaranalysis').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/index.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebarcloneset').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/cloneset.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebardirectory').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/packagelist.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    $(function () {\r\n" +
					"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
					"    })\r\n" +
					"    </script>" +
					"\r\n" +
					"\r\n" +
					"\r\n" +
					"	\r\n" +
					"  </body>\r\n" +
					"</html>\r\n", lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path
					, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path
					, lib_path, lib_path, lib_path, lib_path, lib_path,pro_data_path, data_path, pro_data_path, data_path, data_path, data_path);

			writer.flush();
			writer.close();

			return true;
		} catch (IOException e) {
			return false;
		}
	}
	/**
	 * <p>
	 * クローンセットのコードクローン一覧出力
	 * </p>
	 *
	 * @param writer
	 *            PrintWriterオブジェクト
	 * @param project
	 *            Projectオブジェクト
	 * @param cloneSet
	 *            CloneSetオブジェクト
	 */
	private void outputCloneSet(PrintWriter writer, Project project, CloneSet cloneSet) {


		writer.print("                    <table class=\"table table-striped table-bordered\">\r\n");
		writer.printf("						   <thead id=\"cloneset%d\">\r\n", cloneSet.getOutputId());
		writer.printf("                        <tr>\r\n" +
				"                          <tr><th bgcolor=\"%s\" colspan=\"7\" style=\"color:white\">Clone Set ID : <font size=\"4\">%d</font></th></tr>\r\n" +
				"                          <th width=\"%s\">\r\n" +
				"							 <th width=\"%s\"><input type=\"checkbox\" id=\"check-all\" class=\"flat\"></th>\r\n" +
				"						  </th>\r\n" +
				"						  <th width=\"%s\">Link</th>\r\n" +
				"						  <th width=\"%s\">ID</th>\r\n" +
				"                         <th width=\"%s\">Classification</th>\r\n" +
				"    					  <th width=\"%s\">File name</th>\r\n" +
				"    				      <th width=\"%s\">Location</th>\r\n" +
				"                        </tr>\r\n" +
				"                      </thead>\r\n" +
				"                      <tbody>\r\n", getCloneSetColor(cloneSet), cloneSet.getOutputId(),"5", "50", "50", "50", "100", "720", "120");



		int clone_cnt = 0;
		int modified_clone_flag = 0;
		int stable_clone_flag = 0;
		// クローン一覧出力
		for (Clone clone : cloneSet.getNewCloneList()) {
			String temp_clone = null;

			writer.printf("                        <tr>\r\n" +
					"                          <td>\r\n" +
					"							 <th><input type=\"checkbox\" id=\"check-all\" class=\"flat\"></th>\r\n" +
					"						  </td>\r\n");
			writer.printf("							<td><center><a href=\"%s#clone%d.%d\"><span class=\"fa fa-file-code-o\" aria-hidden=\"true\" style=\"font-size:large\"></span></a></center></td>\r\n",
					clone.getFile().getName().replace("\\", "/") + ".html", cloneSet.getOutputId(), clone.getOutputId());
			writer.printf("						    <td><a href=\"%s#clone%d.%d\">%d.%d</a></td>\r\n",
					clone.getFile().getName().replace("\\", "/") + ".html", cloneSet.getOutputId(), clone.getOutputId(),
					cloneSet.getOutputId(), clone.getOutputId());
			writer.printf("							<td bgcolor=\"%s\" style=\"color:white\">%s</td>", getCloneColor(clone), clone.getCategoryString());
			if(project.isGitDirect()) {
				writer.printf("							<td>%s</td>\r\n", clone.getFile().getName().substring(4));
			}else {
				writer.printf("							<td>%s</td>\r\n", clone.getFile().getName());
			}
			writer.printf("							<td>%d.%d-%d.%d</td>\n", clone.getStartLine(), clone.getStartColumn(), clone.getEndLine(),
					clone.getEndColumn());
			writer.printf("						   </tr>\n");



			/*inconsistentか判定 OUTOTUGENERATOR.JAVAに書き直し*/
			if(cloneSet.getCategory() == CloneSet.CHANGED) {
				if(clone.getCategory() == Clone.MODIFIED) {
					modified_clone_flag = 1;
				}
				if(clone.getCategory() == Clone.STABLE) {
					stable_clone_flag = 1;
				}
			}
		}
		if(cloneSet.getCategory() == CloneSet.CHANGED) {
			if(modified_clone_flag == 1 && stable_clone_flag == 1) {
				Globalval.inconsistent_cnt++;
			}else {
				//				System.out.println("consistent");
			}
		}

		// 旧バージョンのクローン
		for (Clone clone : cloneSet.getOldCloneList()) {
			if (clone.getCategory() == Clone.DELETED || clone.getCategory() == Clone.DELETE_MODIFIED) {
				if (clone.getChildClone() != null)
					continue;
				writer.printf("<tr><th colspan=\"7\">Code Clone of previous version</th></tr>\r\n");
				break;
			}
		}


		// 元クローン出力
		for (Clone clone : cloneSet.getOldCloneList()) {
			if (clone.getCategory() == Clone.DELETED || clone.getCategory() == Clone.DELETE_MODIFIED) {
				if (clone.getChildClone() != null)
					continue;

				writer.printf("                        <tr>\r\n" +
						"                          <td>\r\n" +
						"							 <th><input type=\"checkbox\" id=\"check-all\" class=\"flat\"></th>\r\n" +
						"						  </td>\r\n");
				writer.printf("						<td><center><a href=\"%s#clone%d.%d\"><span class=\"fa fa-file-code-o\" aria-hidden=\"true\" style=\"font-size:large\"></span></a></center></td>\r\n",
						clone.getFile().getName().replace("\\", "/") + ".html", cloneSet.getOutputId(), clone.getOutputId());
				writer.printf("						<td><a href=\"%s#clone%d.%d\">%d.%d</a></td>\r\n",
						clone.getFile().getName().replace("\\", "/") + ".html", cloneSet.getOutputId(), clone.getOutputId(),
						cloneSet.getOutputId(), clone.getOutputId());
				writer.printf("						<td bgcolor=\"%s\" style=\"color:white\">%s</td>", getCloneColor(clone), clone.getCategoryString());
				if(project.isGitDirect()) {
					writer.printf("						<td>%s</td>\r\n", clone.getFile().getName().substring(4));
				}else {
					writer.printf("						<td>%s</td>\r\n", clone.getFile().getName());
				}
				writer.printf("						<td>%d.%d-%d.%d</td>\n", clone.getStartLine(), clone.getStartColumn(), clone.getEndLine(),
						clone.getEndColumn());
				writer.printf("						</tr>\n");
			}
		}
		writer.printf("						</tbody>\r\n");
		writer.printf("					</table>\r\n");


	}

	/**
	 * <p>
	 * パッケージリストの生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param dir
	 *            出力ディレクトリ
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generatePackageListPage(OutputGenerator g, String dir, Project project) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir + "/" + PACKAGELIST_PAGE)));

			String cur_date = Integer.toString(project.getAnalysisdate());
			String cur_year = cur_date.substring(0, 4);
			String cur_month = cur_date.substring(4, 6);
			String cur_day = cur_date.substring(6, 8);
			String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
			String old_year = old_date.substring(0, 4);
			String old_month = old_date.substring(4, 6);
			String old_day = old_date.substring(6, 8);
			String lib_path = "../../../../../../";
			String pro_data_path = "../../../../";
			String data_path = "../../";

			writer.printf("<!DOCTYPE html>\r\n" +
					"<html lang=\"en\">\r\n" +
					"  <head>\r\n" +
					"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
					"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
					"    <meta charset=\"utf-8\">\r\n" +
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
					"\r\n" +
					"    <title>CCEvovis </title>\r\n" +
					"\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <link href=\"%svendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Font Awesome -->\r\n" +
					"    <link href=\"%svendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <link href=\"%svendors/nprogress/nprogress.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <link href=\"%svendors/iCheck/skins/flat/green.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Datatables -->\r\n" +
					"    <link href=\"%svendors/datatables.net-bs/css/dataTables.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-buttons-bs/css/buttons.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-fixedheader-bs/css/fixedHeader.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-responsive-bs/css/responsive.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/datatables.net-scroller-bs/css/scroller.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    \r\n" +
					"       <!-- bootstrap-daterangepicker -->\r\n" +
					"    <link href=\"%svendors/bootstrap-daterangepicker/daterangepicker.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Ion.RangeSlider -->\r\n" +
					"    <link href=\"%svendors/normalize-css/normalize.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/ion.rangeSlider/css/ion.rangeSlider.css\" rel=\"stylesheet\">\r\n" +
					"    <link href=\"%svendors/ion.rangeSlider/css/ion.rangeSlider.skinFlat.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Bootstrap Colorpicker -->\r\n" +
					"    <link href=\"%svendors/mjolnic-bootstrap-colorpicker/dist/css/bootstrap-colorpicker.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <link href=\"%svendors/cropper/dist/cropper.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Style -->\r\n" +
					"    <link href=\"%sbuild/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
					"    <style>\r\n" +
					"    button#btnmodified{\r\n" +
					"    	background-color: #807dba;\r\n" +
					"    	color: white;\r\n" +
					"    }\r\n" +
					"    </style>\r\n" +
					"   \r\n" +
					"    \r\n" +
					"  </head>\r\n" +
					"\r\n" +
					"  <body class=\"nav-md\">\r\n" +
					"    <div class=\"container body\">\r\n" +
					"      <div class=\"main_container\">\r\n" +
					"        <div class=\"col-md-3 left_col\">\r\n" +
					"          <div class=\"left_col scroll-view\">\r\n" +
					"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
					"              <a href=\"%sindex.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
					"            </div>\r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"\r\n" +
					"            <!-- menu profile quick info -->\r\n" +
					"            <div class=\"profile clearfix\">\r\n" +
					"              <div class=\"profile_pic\">\r\n" +
					"                <img src=\"%simages/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">\r\n" +
					"              </div>\r\n" +
					"              <div class=\"profile_info\">\r\n" +
					"                <span>Welcome,</span>\r\n" +
					"                <h2>%s</h2>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu profile quick info -->\r\n" +
					"\r\n" +
					"            <br />", lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, pro_data_path, lib_path,project.getUserId());

			writer.printf("            <!-- sidebar menu -->\r\n" +
					"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
					"              <div class=\"menu_section\">\r\n" +
					"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
					"                  <li><a href=\"%sindex.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
					"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-bar-chart\"></i> Analysis <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebaranalysis\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-table\"></i> Clone Set <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                  <ul class=\"nav child_menu\" id=\"sidebarcloneset\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                   <li><a><i class=\"fa fa-file-code-o\"></i> Directory <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebardirectory\">\r\n" +
					"                    </ul>\r\n" +
					"                  </li>\r\n" +
					"                </ul>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n", pro_data_path);
			//トップナビゲーション
			writer.printf("           <!-- /menu footer buttons -->\r\n" +
					"            <div class=\"sidebar-footer hidden-small\">\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
					"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
					"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
					"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
					"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
					"              </a>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu footer buttons -->\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n" +
					"\r\n" +
					"        <!-- top navigation -->\r\n" +
					"        <div class=\"top_nav\">\r\n" +
					"          <div class=\"nav_menu\">\r\n" +
					"            <nav>\r\n" +
					"              <div class=\"nav toggle\">\r\n" +
					"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
					"              </div>\r\n" +
					"\r\n" +
					"              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
					"                <li class=\"\">\r\n" +
					"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n" +
					"                    <img src=\"%simages/img.jpg\" alt=\"\">%s\r\n" +
					"                    <span class=\" fa fa-angle-down\"></span>\r\n" +
					"                  </a>\r\n" +
					"                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
					"                    <li><a href=\"javascript:;\">Help</a></li>\r\n" +
					"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
					"                  </ul>\r\n" +
					"                </li>\r\n" +
					"              </ul>\r\n" +
					"            </nav>\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n" +
					"        <!-- /top navigation -->", lib_path,project.getUserId());

			//ぺ―ジコンテンツ
			writer.printf("       <div class=\"right_col\" role=\"main\">\r\n" +
					"          <div class=\"\">\r\n" +
					"            <div class=\"page-title\">\r\n" +
					"              <div class=\"title_left\">\r\n" +
					"                <h3>Project: %s</h3>\r\n " +
					"                <h4>Analysis Title: %s</h4>\r\n" +
					"                <h4>Date: %s/%s/%s</h4>\r\n" +
					/*				"                <div class=\"x_content\">\r\n" +
					"                        <fieldset>\r\n" +
					"                          <div class=\"control-group\">\r\n" +
					"                            <div class=\"controls\">\r\n" +
					"                              <div class=\"col-md-11 xdisplay_inputx form-group has-feedback\">\r\n" +
					"                                <input type=\"text\" class=\"form-control has-feedback-left\" id=\"single_cal3\" placeholder=\"First Name\" aria-describedby=\"inputSuccess2Status3\">\r\n" +
					"                                <span class=\"fa fa-calendar-o form-control-feedback left\" aria-hidden=\"true\"></span>\r\n" +
					"                                <span id=\"inputSuccess2Status3\" class=\"sr-only\">(success)</span>\r\n" +
					"                              </div>\r\n" +
					"                            </div>\r\n" +
					"                          </div>\r\n" +
					"                        </fieldset>\r\n" +
					"                    </div>\r\n" +
					 */			"              </div>\r\n" +
					 "            </div>", project.getName(),project.getAnalysisdate(), cur_month, cur_day, cur_year);

			writer.print("	          <div class=\"clearfix\"></div>\r\n" +
					"	          <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"	              <div class=\"row\">\r\n" +
					"	                <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"	                  <div class=\"x_panel\">\r\n" +
					"	                    <div class=\"x_title\">\r\n" +
					"	                      <h2>Directory List\r\n" +
					"	                        <!-- <small></small> -->\r\n" +
					"	                      </h2>\r\n" +
					"	                      <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
					"	                        <li>\r\n" +
					"	                          <a class=\"collapse-link\">\r\n" +
					"	                            <i class=\"fa fa-chevron-up\"></i>\r\n" +
					"	                          </a>\r\n" +
					"	                        </li>\r\n" +
					"	                      </ul>\r\n" +
					"	                      <div class=\"clearfix\"></div>\r\n" +
					"	                    </div>\r\n" +
					"	                    <div class=\"x_content\">\r\n" +
					"	                      <div class=\"dashboard-widget-content\">\r\n" +
					"		                      <div class=\"\">\r\n" +
					"		                        <ul class=\"to_do\">\r\n");
			//パーケージ一覧出力
			ArrayList<String> packageList = new ArrayList<String>();
			boolean flag = false;
			for (SourceFile file : project.getFileList()) {
				if (file.getNewCloneList().isEmpty() && file.getOldCloneList().isEmpty())
					continue;
				flag = true;

				String fileName;
				if (file.getState() != SourceFile.DELETED)
					fileName = (new File(file.getNewPath())).getName();
				else
					fileName = (new File(file.getOldPath())).getName();

				// ディレクトリ生成
				String packageDirPath = dir + "\\" + file.getName();
				packageDirPath = packageDirPath.replace("\\" + fileName, "");
				File packageDir = new File(packageDirPath);
				if (!packageList.contains(packageDirPath.toString())) {
					packageList.add(packageDirPath.toString());
					packageDir.mkdirs();
					writer.print("<li>\r\n");
					if(project.isGitDirect()) {
						writer.printf("<a href=\"%s\"><input type=\"checkbox\" class=\"flat\">&nbsp &nbsp %s</a>\r\n",
								packageDirPath.replace(dir, "").replace("\\", "/").substring(1) + "/" + INDEX_PAGE,
								packageDirPath.replace(dir, "").substring(1).substring(4));
					}else {
						writer.printf("<a href=\"%s\"><input type=\"checkbox\" class=\"flat\">&nbsp &nbsp %s</a>\r\n",
								packageDirPath.replace(dir, "").replace("\\", "/").substring(1) + "/" + INDEX_PAGE,
								packageDirPath.replace(dir, "").substring(1));
					}

					writer.print("</li>\r\n");
				}

			}

			// ファイルが存在しない場合
			if (!flag) {
				writer.printf("<tr><td>クローンを含むディレクトリ（パッケージは存在しません）</td></tr>\r\n");
			}

			writer.print("		                        </ul>\r\n" +
					"		                      </div>\r\n" +
					"	                      </div>\r\n" +
					"	                    </div>\r\n" +
					"	                  </div>\r\n" +
					"	                </div>\r\n" +
					"	              </div>\r\n" +
					"	          </div>\r\n" +
					"          \r\n" +
					"            \r\n" +
					"            \r\n" +
					"            \r\n" +
					"          </div>\r\n" +
					"        </div>\r\n" +
					"        <!-- /page content -->\r\n");
			//フッター
			writer.printf("        <!-- footer content -->\r\n" +
					"        <footer>\r\n" +
					"          <div class=\"pull-right\"> CCEvovis by\r\n" +
					"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"        </footer>\r\n" +
					"        <!-- /footer content -->\r\n");

			writer.printf("      </div>\r\n" +
					"    </div>\r\n" +
					"    <!-- jQuery -->\r\n" +
					"\r\n" +
					"\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>\r\n" +
					"    <!-- Chart.js -->\r\n" +
					"    <script src=\"%svendors/Chart.js/dist/Chart.min.js\"></script>\r\n" +
					"    <!-- gauge.js -->\r\n" +
					"    <script src=\"%svendors/gauge.js/dist/gauge.min.js\"></script>\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <script src=\"%svendors/bootstrap-progressbar/bootstrap-progressbar.min.js\"></script>\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <script src=\"%svendors/iCheck/icheck.min.js\"></script>\r\n" +
					"    <!-- Skycons -->\r\n" +
					"    <script src=\"%svendors/skycons/skycons.js\"></script>\r\n" +
					"    <!-- Flot -->\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.pie.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.time.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.stack.js\"></script>\r\n" +
					"    <script src=\"%svendors/Flot/jquery.flot.resize.js\"></script>\r\n" +
					"    <!-- Flot plugins -->\r\n" +
					"    <script src=\"%svendors/flot.orderbars/js/jquery.flot.orderBars.js\"></script>\r\n" +
					"    <script src=\"%svendors/flot-spline/js/jquery.flot.spline.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/flot.curvedlines/curvedLines.js\"></script>\r\n" +
					"    <!-- DateJS -->\r\n" +
					"    <script src=\"%svendors/DateJS/build/date.js\"></script>\r\n" +
					"    <!-- JQVMap -->\r\n" +
					"    <script src=\"%svendors/jqvmap/dist/jquery.vmap.js\"></script>\r\n" +
					"    <script src=\"%svendors/jqvmap/dist/maps/jquery.vmap.world.js\"></script>\r\n" +
					"    <script src=\"%svendors/jqvmap/examples/js/jquery.vmap.sampledata.js\"></script>\r\n" +
					"    <!-- bootstrap-daterangepicker -->\r\n" +
					"    <script src=\"%svendors/moment/min/moment.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/bootstrap-daterangepicker/daterangepicker.js\"></script>\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Scripts -->\r\n" +
					"    <script src=\"%sbuild/js/custom.min.js\"></script>\r\n" +
					"        <!-- bootstrap-daterangepicker -->\r\n" +
					"    <script src=\"%svendors/moment/min/moment.min.js\"></script>\r\n" +
					"    <script src=\"%svendors/bootstrap-daterangepicker/daterangepicker.js\"></script>\r\n" +
					"    <!-- Ion.RangeSlider -->\r\n" +
					"    <script src=\"%svendors/ion.rangeSlider/js/ion.rangeSlider.min.js\"></script>\r\n" +
					"    <!-- Bootstrap Colorpicker -->\r\n" +
					"    <script src=\"%svendors/mjolnic-bootstrap-colorpicker/dist/js/bootstrap-colorpicker.min.js\"></script>\r\n" +
					"    <!-- jquery.inputmask -->\r\n" +
					"    <script src=\"%svendors/jquery.inputmask/dist/min/jquery.inputmask.bundle.min.js\"></script>\r\n" +
					"    <!-- jQuery Knob -->\r\n" +
					"    <script src=\"%svendors/jquery-knob/dist/jquery.knob.min.js\"></script>\r\n" +
					"	\r\n" +
					"  <script src=\"%sdata/projects.json\"></script>\r\n" +
					"  <script src=\"%sdata/analysis.json\"></script>\r\n" +
					"  <script>\r\n" +
					"    //projects配列の探索\r\n" +
					"    for (var i = 0; i < projects.length; i++) {\r\n" +
					"      $('#sidebarproject').append('<li><a href=\"%sprojects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebaranalysis').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/index.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebarcloneset').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/cloneset.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    for (var i = 0; i < analysis.length; i++) {\r\n" +
					"      $('#sidebardirectory').append('<li><a href=\"%s' + analysis[i].name + '/' + analysis[i].date + '/packagelist.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
					"    }\r\n" +
					"    $(function () {\r\n" +
					"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
					"    })\r\n" +
					"  </script>" +
					"  </body>\r\n" +
					"</html>", lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path, lib_path,lib_path, lib_path, pro_data_path, data_path,pro_data_path, data_path, data_path, data_path);



			writer.flush();
			writer.close();

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private boolean generateIndexEachPackege(OutputGenerator g, String dir, Project project) {
		for (SourceFile file : project.getFileList()) {
			if (file.getNewCloneList().isEmpty() && file.getOldCloneList().isEmpty())
				continue;
			String fileName;
			if (file.getState() != SourceFile.DELETED)
				fileName = (new File(file.getNewPath())).getName();
			else
				fileName = (new File(file.getOldPath())).getName();
			String packageDirPath = dir + "\\" + file.getName();
			packageDirPath = packageDirPath.replace("\\" + fileName, "");
			// ソースコードの生成
			if (file.getState() == SourceFile.ADDED) {
				generateAddedSourceFile(g, file, packageDirPath + "\\" + fileName + ".html", project.getName());
			} else if (file.getState() == SourceFile.NORMAL) {
				generateNormalSourceFile(g, file, packageDirPath + "\\" + fileName + ".html", project.getName());
			} else if (file.getState() == SourceFile.DELETED) {
				generateDeletedSourceFile(g, file, packageDirPath + "\\" + fileName + ".html", project.getName());
			}
		}
		return true;
	}

	/**
	 * <p>
	 * 存続ソースファイルの生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param file
	 *            出力対象ソースファイル
	 * @param fileName
	 *            出力ファイル名
	 * @param projectName
	 *            プロジェクト名
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateNormalSourceFile(OutputGenerator g, SourceFile file, String fileName,
			String projectName) {

		BufferedReader readerA = null, readerB = null;
		try {
			readerA = new BufferedReader(new InputStreamReader(new FileInputStream(file.getNewPath())));
			readerB = new BufferedReader(new InputStreamReader(new FileInputStream(file.getOldPath())));
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

			String lineA, lineB;
			int countClone = 0;
			int lineNumA = 0, lineNumB = 0;
			int addCodeId = 0, deleteCodeId = 0;
			// ヘッダ部出力
			String cloneSetFile = outputHtmlHead(g, writer, file, projectName);

			// ソースコード出力
			writer.printf("<table cellpadding=\"0\">\r\n");
			writer.printf("<tr><td width=\"50\"></td><td width=\"50\"></td><td></td></tr>\r\n");
			while (true) {
				if ((lineA = readerA.readLine()) != null) {
					lineNumA++;
				}
				if ((lineB = readerB.readLine()) != null) {
					lineNumB++;
				}

				// 追加コードの場合
				while (addCodeId < file.getAddedCodeList().size()
						&& lineNumA == file.getAddedCodeList().get(addCodeId)) {
					addCodeId++;
					countClone = countClone
							+ writeCloneStartSign(writer, file.getNewCloneList(), lineNumA, true, cloneSetFile);
					writeCodeLine(writer, Integer.toString(lineNumA), "+", lineA, countClone);
					countClone = countClone - writeCloneEndSign(writer, file.getNewCloneList(), lineNumA, true);
					if ((lineA = readerA.readLine()) != null) {
						lineNumA++;
					}
				}

				// 削除コードの場合
				while (deleteCodeId < file.getDeletedCodeList().size()
						&& lineNumB == file.getDeletedCodeList().get(deleteCodeId)) {
					deleteCodeId++;
					countClone = countClone
							+ writeCloneStartSign(writer, file.getOldCloneList(), lineNumB, false, cloneSetFile);
					writeCodeLine(writer, "", "-", lineB, countClone);
					countClone = countClone - writeCloneEndSign(writer, file.getOldCloneList(), lineNumB, false);
					if ((lineB = readerB.readLine()) != null) {
						lineNumB++;
					}
				}

				if (lineA == null && lineB == null) {
					break;
				} else {
					countClone = countClone
							+ writeCloneStartSign(writer, file.getNewCloneList(), lineNumA, true, cloneSetFile);
					countClone = countClone
							+ writeCloneStartSign(writer, file.getOldCloneList(), lineNumB, false, cloneSetFile);
					writeCodeLine(writer, Integer.toString(lineNumA), "", lineA, countClone);
					countClone = countClone - writeCloneEndSign(writer, file.getNewCloneList(), lineNumA, true);
					countClone = countClone - writeCloneEndSign(writer, file.getOldCloneList(), lineNumB, false);
				}
			}

			String lib_path = "../../../../";
			String pro_data_path = "../../";
			String data_path = "";
			// 階層の計算
			String[] tmp = file.getName().split("\\\\");
			for (int i = 0; i < tmp.length - 1; i++) {
				lib_path = "../" + lib_path;
				pro_data_path = "../" + pro_data_path;
				data_path = "../" + data_path;
			}


			writer.printf("						</table>\r\n" +
					"                    </div>\r\n" +
					"                  </div>\r\n" +
					"                </div>\r\n" +
					"              </div>\r\n" +
					"          </div>\r\n" +
					"          \r\n" +
					"        </div>\r\n" +
					"        <!-- /page content -->\r\n" +
					"        <!-- footer content -->\r\n" +
					"        <footer>\r\n" +
					"          <div class=\"pull-right\"> CCEvovis by\r\n" +
					"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"        </footer>\r\n" +
					"        <!-- /footer content -->\r\n" +
					"      </div>\r\n" +
					"    </div>\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%s../../vendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%s../../vendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%s../../vendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%s../../vendors/nprogress/nprogress.js\"></script>\r\n" +
					"    <!-- Custom Theme Scripts -->\r\n" +
					"    <script src=\"%s../../build/js/custom.min.js\"></script>\r\n" +
					"    <!-- D3 chart -->\r\n" +
					"    <script src=\"http://d3js.org/d3.v3.min.js\"></script>\r\n" +
					"    <!--JavaScript at end of body for optimized loading-->\r\n" +
					"\r\n", lib_path,lib_path,lib_path,lib_path,lib_path);
			writer.printf("    <!-- jQuery -->\r\n" +
					"    <script src=\"%s../../vendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%s../../vendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%s../../vendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%s../../vendors/nprogress/nprogress.js\"></script>\r\n" +
					"    <!-- Chart.js -->\r\n" +
					"    <script src=\"%s../../vendors/Chart.js/dist/Chart.min.js\"></script>\r\n" +
					"    <!-- gauge.js -->\r\n" +
					"    <script src=\"%s../../vendors/gauge.js/dist/gauge.min.js\"></script>\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <script src=\"%s../../vendors/bootstrap-progressbar/bootstrap-progressbar.min.js\"></script>\r\n" +
					"    <!-- iCheck -->\r\n" +
					"    <script src=\"%s../../vendors/iCheck/icheck.min.js\"></script>\r\n" +
					"    <!-- Skycons -->\r\n" +
					"    <script src=\"%s../../vendors/skycons/skycons.js\"></script>\r\n"
					,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path);
			writer.printf(
					"    <!-- Flot -->\r\n" +
							"    <script src=\"%s../../vendors/Flot/jquery.flot.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/Flot/jquery.flot.pie.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/Flot/jquery.flot.time.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/Flot/jquery.flot.stack.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/Flot/jquery.flot.resize.js\"></script>\r\n" +
							"    <!-- Flot plugins -->\r\n" +
							"    <script src=\"%s../../vendors/flot.orderbars/js/jquery.flot.orderBars.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/flot-spline/js/jquery.flot.spline.min.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/flot.curvedlines/curvedLines.js\"></script>\r\n" +
							"    <!-- DateJS -->\r\n" +
							"    <script src=\"%s../../vendors/DateJS/build/date.js\"></script>\r\n" +
							"    <!-- JQVMap -->\r\n" +
							"    <script src=\"%s../../vendors/jqvmap/dist/jquery.vmap.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/jqvmap/dist/maps/jquery.vmap.world.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/jqvmap/examples/js/jquery.vmap.sampledata.js\"></script>\r\n" +
							"    <!-- bootstrap-daterangepicker -->\r\n" +
							"    <script src=\"%s../../vendors/moment/min/moment.min.js\"></script>\r\n" +
							"    <script src=\"%s../../vendors/bootstrap-daterangepicker/daterangepicker.js\"></script>\r\n" +
							"\r\n" +
							"   <script src=\"%s../../data/projects.json\"></script>\r\n" +
							"  <script src=\"%s../../data/analysis.json\"></script>\r\n" +
							"  <script>\r\n" +
							"    //projects配列の探索\r\n" +
							"    for (var i = 0; i < projects.length; i++) {\r\n" +
							"      $('#sidebarproject').append('<li><a href=\"%s../../projects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"    for (var i = 0; i < analysis.length; i++) {\r\n" +
							"      $('#sidebaranalysis').append('<li><a href=\"%s../../' + analysis[i].name + '/' + analysis[i].date + '/index.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"    for (var i = 0; i < analysis.length; i++) {\r\n" +
							"      $('#sidebarcloneset').append('<li><a href=\"%s../../' + analysis[i].name + '/' + analysis[i].date + '/cloneset.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"    for (var i = 0; i < analysis.length; i++) {\r\n" +
							"      $('#sidebardirectory').append('<li><a href=\"%s../../' + analysis[i].name + '/' + analysis[i].date + '/packagelist.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"    $(function () {\r\n" +
							"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
							"    })\r\n" +
							"  </script>\r\n" +
							"  </body>\r\n" +
							"</html>\r\n",lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path,lib_path
							,lib_path,lib_path,lib_path,lib_path,pro_data_path, data_path ,pro_data_path,data_path,data_path,data_path);

			writer.flush();
			writer.close();

			if (readerA != null) {
				readerA.close();
			}
			if (readerB != null) {
				readerB.close();
			}

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * <p>
	 * 追加ソースファイルの生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param file
	 *            出力対象ソースファイル
	 * @param fileName
	 *            出力ファイル名
	 * @param projectName
	 *            プロジェクト名
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateAddedSourceFile(OutputGenerator g, SourceFile file, String fileName,
			String projectName) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getNewPath())));
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

			int countClone = 0;
			String line;
			int lineNum = 0;

			// ヘッダ部出力
			String cloneSetFile = outputHtmlHead(g, writer, file, projectName);

			// ソースコード出力
			writer.printf("<table cellpadding=\"0\">\r\n");
			writer.printf("<tr><td width=\"50\"></td><td width=\"50\"></td><td></td></tr>\r\n");
			while ((line = reader.readLine()) != null) {
				lineNum++;
				countClone = writeCloneStartSign(writer, file.getNewCloneList(), lineNum, true, cloneSetFile)
						+ countClone;
				writeCodeLine(writer, Integer.toString(lineNum), "+", line, countClone);
				countClone = countClone - writeCloneEndSign(writer, file.getNewCloneList(), lineNum, true);
			}
			writer.printf("</table>\r\n");
			writer.printf("</body>\r\n");
			writer.printf("</html>\r\n");

			writer.flush();
			writer.close();
			if (reader != null) {
				reader.close();
			}

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * <p>
	 * 削除ソースファイルの生成
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param file
	 *            出力対象ソースファイル
	 * @param fileName
	 *            出力ファイル名
	 * @param projectName
	 *            プロジェクト名
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateDeletedSourceFile(OutputGenerator g, SourceFile file, String fileName,
			String projectName) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getOldPath())));
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

			String line;
			int lineNum = 0;
			int countClone = 0;

			// ヘッダ部出力
			String cloneSetFile = outputHtmlHead(g, writer, file, projectName);

			// ソースコード出力
			writer.printf("<table cellpadding=\"0\">\r\n");
			writer.printf("<tr><td width=\"50\"></td><td width=\"50\"></td><td></td></tr>\r\n");
			while ((line = reader.readLine()) != null) {
				lineNum++;

				// ソースコード１行出力
				countClone = countClone
						+ writeCloneStartSign(writer, file.getOldCloneList(), lineNum, false, cloneSetFile);
				writeCodeLine(writer, Integer.toString(lineNum), "-", line, countClone);
				countClone = countClone - writeCloneEndSign(writer, file.getOldCloneList(), lineNum, false);
			}
			writer.printf("</table>\r\n");
			writer.printf("</body>\r\n");
			writer.printf("</html>\r\n");

			writer.flush();
			writer.close();
			if (reader != null) {
				reader.close();
			}

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * <p>
	 * クローン開始サインの出力
	 * </p>
	 *
	 * @param writer
	 *            書き込み用のライター
	 * @param cloneList
	 *            クローン一覧
	 * @param line
	 *            チェックする行番号
	 * @param isNew
	 *            新バージョンのクローンならtrue
	 * @param cloneSetFile
	 *            クローンセット一覧ファイル
	 * @return lineから開始するクローンの総数
	 */
	private int writeCloneStartSign(PrintWriter writer, ArrayList<Clone> cloneList, int line, boolean isNew,
			String cloneSetFile) {
		int count = 0;
		for (Clone clone : cloneList) {
			if (clone.getOutputId() == Clone.NULL)
				continue;
			if (clone.getStartLine() != line)
				continue;
			if (!isNew) {
				if (clone.getChildClone() != null)
					continue;
			}
			writer.printf("<tr id=\"clone%d.%d\" ><th></th><th></th>", clone.getCloneSet().getOutputId(),
					clone.getOutputId());
			writer.printf("<th align=\"left\">[START CLONE:<a href=\"%s#cloneset%d\">%d.%d(%sClone)</a>]</th></tr>",
					cloneSetFile, clone.getCloneSet().getOutputId(), clone.getCloneSet().getOutputId(),
					clone.getOutputId(), clone.getCategoryString());
			writer.println();
			count++;
		}
		return count;
	}

	/**
	 * <p>
	 * クローン終了サインの出力
	 * </p>
	 *
	 * @param writer
	 *            書き込み用のライター
	 * @param cloneList
	 *            クローン一覧
	 * @param line
	 *            チェックする行番号
	 * @param isNew
	 *            新バージョンのクローンならtrue
	 * @return lineで終了するクローンの総数
	 */
	private int writeCloneEndSign(PrintWriter writer, ArrayList<Clone> cloneList, int line, boolean isNew) {
		int count = 0;
		for (Clone clone : cloneList) {
			if (clone.getOutputId() == Clone.NULL)
				continue;
			if (clone.getEndLine() != line)
				continue;
			if (!isNew) {
				if (clone.getChildClone() != null)
					continue;
			}
			writer.printf("<tr id=\"clone%d.%d\" ><th></th><th></th>", clone.getCloneSet().getOutputId(),
					clone.getOutputId());
			writer.printf("<th  align=\"left\">[END ID:%d.%d]</th></tr>", clone.getCloneSet().getOutputId(),
					clone.getOutputId());
			writer.println();
			count++;
		}
		return count;
	}

	/**
	 * <p>
	 * ソースコード1行出力
	 * </p>
	 *
	 * @param writer
	 *            書き込み用ライター
	 * @param lineNum
	 *            行番号
	 * @param state
	 *            該当コード行の状態
	 * @param line
	 *            書き込むコード
	 * @param count
	 *            その行を含むクローンの総数
	 */
	private void writeCodeLine(PrintWriter writer, String lineNum, String state, String line, int count) {
		writer.printf("<tr>\r\n");
		writer.printf("<th align=\"left\">%s</th>\r\n", lineNum);
		writer.printf("<th align=\"left\">%s</th>\r\n", state);
		if (count > 0 && state.equals("-")) {
			writer.printf("<td bgcolor=\"F2D500\"><b><xmp>%s</xmp></b></td>\r\n", line);
		} else if (state.equals("+")) {
			writer.printf("<td bgcolor=\"yellow\"><b><xmp>%s</xmp></b></td>\r\n", line);
		} else if (count > 0) {
			writer.printf("<td bgcolor=\"#FFFF99\"><b><xmp>%s</xmp></b></td>\r\n", line);
		} else if (count == 0 && state.equals("-")) {
			writer.printf("<td bgcolor=\"tan\"><b><xmp>%s</xmp></b></td>\r\n", line);
		} else {
			writer.printf("<td><b><xmp>%s</xmp></b></td>\r\n", line);
		}
		writer.println("</tr>");
	}
	/**
	 * <p>
	 * 各分析のIndexページの生成
	 * </p>
	 *
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateAnalysisListPage(Project project) {
		System.out.println("generateAnalysisListPage");



		// index.htmlの削除
		File index = new File(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() +  "\\projects\\" + project.getName() + "\\" + INDEX_PAGE);
		if (index.exists()) {
			index.delete();
		}

		// indexファイル生成
		try {
			PrintWriter writer = new PrintWriter(
					new BufferedWriter(new FileWriter(project.getGenerateHTMLDir()  + "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\" + INDEX_PAGE)));
			File[] dateList = (new File(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\" + project.getAnalysisName())).listFiles();
			//String cur_date = dateList[dateList.length-1].getName();
			int date_count = 0;
			// 分析日一覧

			//String cur_date = project.getDate();
			String now_date = project.getDate();
			String now_year = now_date.substring(0, 4);
			String now_month = now_date.substring(4, 6);
			String now_day = now_date.substring(6, 8);
			String cur_date = Integer.toString(project.getAnalysisdate());
			String cur_year = cur_date.substring(0, 4);
			String cur_month = cur_date.substring(4, 6);
			String cur_day = cur_date.substring(6, 8);
			String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
			String old_year = old_date.substring(0, 4);
			String old_month = old_date.substring(4, 6);
			String old_day = old_date.substring(6, 8);
			String pro_lang = project.getLang();
			System.out.println(pro_lang);

			if(pro_lang.equals("c") || pro_lang.equals("cpp")) {
				pro_lang = "C/C++";
				System.out.println(pro_lang);

			}else if(pro_lang.equals("java")) {
				pro_lang = "Java";
				System.out.println(pro_lang);
			}else if(pro_lang.equals("csharp")) {
				pro_lang = "C#";
				System.out.println(pro_lang);
			}else {
				System.out.println("error");
			}
			/*	if(project.getLang() == c_lang) {

			}else if(project.getLang() == java_lang) {
				String pro_lang = "Java";
				System.out.println(pro_lang);
				System.out.println("2");
			}
			 */
			String dir_path = "../../../../";
			String pro_data_path = "../../";

			writer.printf("<!DOCTYPE html>\r\n" +
					"<html lang=\"en\">\r\n" +
					"  <head>\r\n" +
					"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
					"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
					"    <meta charset=\"utf-8\">\r\n" +
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
					"\r\n" +
					"    <title>CCEvovis </title>\r\n" +
					"\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <link href=\"%svendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Font Awesome -->\r\n" +
					"    <link href=\"%svendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <link href=\"%svendors/nprogress/nprogress.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <link href=\"%svendors/bootstrap-progressbar/css/bootstrap-progressbar-3.3.4.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Style -->\r\n" +
					"    <link href=\"%sbuild/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
					"    \r\n" +
					"    \r\n" +
					"  <style>\r\n" +
					"    .coverp:hover{\r\n" +
					"      border:1px solid #eea236;\r\n" +
					"    }\r\n" +
					"\r\n" +
					"  </style>\r\n" +
					"  </head>\r\n" +
					"\r\n" +
					"  <body class=\"nav-md\">\r\n" +
					"    <div class=\"container body\">\r\n" +
					"      <div class=\"main_container\">\r\n" +
					"        <div class=\"col-md-3 left_col\">\r\n" +
					"          <div class=\"left_col scroll-view\">\r\n" +
					"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
					"              <a href=\"%sindex.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
					"            </div>\r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"\r\n" +
					"            <!-- menu profile quick info -->\r\n" +
					"            <div class=\"profile clearfix\">\r\n" +
					"              <div class=\"profile_pic\">\r\n" +
					"                <img src=\"%simages/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">\r\n" +
					"              </div>\r\n" +
					"              <div class=\"profile_info\">\r\n" +
					"                <span>Welcome,</span>\r\n" +
					"                <h2>%s</h2>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu profile quick info -->\r\n" +
					"\r\n" +
					"            <br />\r\n" +
					"\r\n", dir_path, dir_path, dir_path, dir_path, dir_path, pro_data_path, dir_path,project.getUserId() );

			writer.printf("            <!-- sidebar menu -->\r\n" +
					"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
					"              <div class=\"menu_section\">\r\n" +
					"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
					"                  <li><a href=\"../../index.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
					"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-bar-chart\"></i> Analysis <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebaranalysis\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                  <li><a><i class=\"fa fa-table\"></i> Clone Set <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                  <ul class=\"nav child_menu\" id=\"sidebarcloneset\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                   <li><a><i class=\"fa fa-file-code-o\"></i> Directory <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                    <ul class=\"nav child_menu\" id=\"sidebardirectory\">\r\n" +
					"                    </ul>\r\n" +
					"                  </li>\r\n" +
					"                </ul>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n");

			writer.print(
					"            <!-- /sidebar menu -->\r\n" +
							"\r\n" +
							"            <!-- /menu footer buttons -->\r\n" +
							"            <div class=\"sidebar-footer hidden-small\">\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
							"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
							"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
							"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
							"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"            </div>\r\n" +
							"            <!-- /menu footer buttons -->\r\n" +
							"          </div>\r\n" +
							"        </div>\r\n" +
					"\r\n");
			writer.printf(
					"        <!-- top navigation -->\r\n" +
							"        <div class=\"top_nav\">\r\n" +
							"          <div class=\"nav_menu\">\r\n" +
							"            <nav>\r\n" +
							"              <div class=\"nav toggle\">\r\n" +
							"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
							"              </div>\r\n" +
							"\r\n" +
							"              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
							"                <li class=\"\">\r\n" +
							"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n" +
							"                    <img src=\"../../../../images/img.jpg\" alt=\"\">%s\r\n" +
							"                    <span class=\" fa fa-angle-down\"></span>\r\n" +
							"                  </a>\r\n" +
							"                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
							"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
							"                  </ul>\r\n" +
							"                </li>\r\n" +
							"              </ul>\r\n" +
							"            </nav>\r\n" +
							"          </div>\r\n" +
							"        </div>\r\n" +
							"        <!-- /top navigation -->\r\n" +
							"\r\n",project.getUserId());
			/*ページコンテンツ*/
			writer.print(
					"        <!-- page content -->\r\n" +
							"        <div class=\"right_col\" role=\"main\">\r\n" +
							"          <div class=\"page-title\">\r\n" +
							"            <div class=\"title_left\">\r\n" +
							"              <h3>List of Previous Results\r\n" +
							"                <!--<small>Some examples of D3</small> -->\r\n" +
							"              </h3>\r\n" +
							"            </div>\r\n" +
							"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n");

			PrintWriter projectwr = new PrintWriter(new BufferedWriter(new FileWriter(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\" + project.getAnalysisName() + "\\data\\analysis.html")));
			projectwr.printf("        <div class=\"col-md-4 col-xs-12 widget widget_tally_box\">\r\n" +
					"          <div class=\"x_panel fixed_height_4100  coverp\">\r\n" +
					"            <a href=\"%s/%d/index.html\">\r\n" +
					"              <div class=\"x_title\" style=\"text-align: center;  font-size: x-large;\">\r\n" +
					"                %s\r\n" +
					"                <div class=\"clearfix\"></div>\r\n" +
					"              </div>\r\n" +
					"              <div class=\"x_content\">\r\n" +
					"                <div style=\"text-align: center; margin-bottom: 17px\">\r\n" +
					"                  <div>\r\n" +
					"                    <span>Total Clone Sets</span>\r\n" +
					"                    <h2>%d</h2>\r\n" +
					"                    <span class=\"sparkline_three_allclonesets\" style=\"height: 160px;\">\r\n" +
					"                      <canvas width=\"200\" height=\"60\" style=\"display: inline-block; vertical-align: top; width: 94px; height: 30px;\"></canvas>\r\n" +
					"                    </span>\r\n" +
					"                  </div>\r\n" +
					"                </div>\r\n" +
					"                <div class=\"divider\"></div>\r\n" +
					"                <center><i class=\"fa fa-calendar\" style=\"font-size:22px;\" id=\"%speriod\"></i></center>\r\n" +
					"                <div class=\"divider\"></div>\r\n" +
					"                <center><i class=\"fa fa-wrench\" style=\"font-size:22px\">&nbsp %s </i></center>\r\n" +
					"                <div class=\"divider\"></div>\r\n" +
					"                <center>\r\n" +
					"                  <h3><a href=\"../../update.html?projectname=%s&analysisname=%s&day=%s\" data-toggle=\"tooltip\" style=\"font-size: 29px\" data-placement=\"bottom\" data-original-title=\"Update\"><i class=\"fa fa-refresh\"></i></a></h3>\r\n" +
					"                </center>\r\n" +
					"                <div class=\"divider\"></div>\r\n" +
					"                <center>\r\n" +
					"                  <h4>%s/%s/%s</h4>\r\n" +
					"                </center>\r\n" +
					"                </div>\r\n" +
					"            </a>\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n"
					,project.getAnalysisName() ,project.getAnalysisdate(), project.getAnalysisName() ,g.getNewCloneSetNum() + g.getChangedCloneSetNum() + g.getDeletedCloneSetNum() + g.getStableCloneSetNum() , project.getAnalysisName(), project.getTool(),  project.getName(),project.getAnalysisName(),project.getAnalysisdate(), now_month,now_day, now_year );
			projectwr.flush();
			projectwr.close();




			/* (1) プロジェクト内を探索
			 * (2) 分析名を取得
			 * (3) プロジェクト内のjson freqDateを取得
			 * (4) freqDataより描画*/
			/*プロジェクトのデータを読み込む*/
			File analysisdir = new File(project.getGenerateHTMLDir() +"\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\");
			File[] analysislist = analysisdir.listFiles();
			System.out.println("analysislist" + analysislist.length);
			for(int i=0; i<analysislist.length; i++) {
				System.out.println(analysislist[i].toString());
				Path analysispath = Paths.get(analysislist[i].toString());
				String analysisstr = analysispath.getFileName().toString();
				System.out.println(analysisstr);
				//	String[] projectname = projectstr.split("\\",0);
				//	System.out.println("projectname = " + projectname);
				if(!(analysisstr.contains("data")) && !(analysisstr.contains("index.html"))) {
					//	if(!projectname[projectname.length].equals("data")) {
					try {
						FileReader fr = new FileReader(analysislist[i].toString() + "\\data\\analysis.html");
						BufferedReader br = new BufferedReader(fr);

						//読み込んだファイルを１行ずつ画面出力する
						String line;
						while ((line = br.readLine()) != null) {
							//							System.out.println(line);
							writer.printf("%s\n", line);
						}
						br.close();
						fr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}




			writer.printf("        <div class=\"col-md-4 col-xs-12 widget widget_tally_box\">\r\n" +
					"		   <div class=\"x_panel fixed_height_4100  coverp\" style=\"border-style:dashed\">" +
					"            <a href=\"../../newanalysis.html?projectname=%s&analysisname=%s\">\r\n" +
					"              <br><br><br><br><br><br><br><br><br>\r\n" +
					"              <center><i class=\"glyphicon glyphicon-plus-sign\" style=\"font-size:50px;\"></i></center>\r\n" +
					"              <h3 style=\"text-align:center\">New Analysis</h3>\r\n" +
					"              <br><br><br><br><br><br><br><br><br>\r\n" +
					"            </a>\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n",project.getName(),project.getAnalysisName());



			System.out.println("date_cont = " + date_count);
			System.out.println("tool = " + project.getTool());
			System.out.println(project.getLang());
			//writer.printf("<tr><td><a href = \"%s\">%c%c%c%c年%c%c月%c%c日</a></td></tr>\n",
			//	date + "/" + INDEX_PAGE, date.charAt(0), date.charAt(1), date.charAt(2), date.charAt(3),
			//	date.charAt(4), date.charAt(5), date.charAt(6), date.charAt(7));
			writer.printf("        </div>\r\n" +
					"        <!-- /page content -->\r\n" +
					"        <!-- footer content -->\r\n" +
					"        <footer>\r\n" +
					"          <div class=\"pull-right\"> CCEvovis by\r\n" +
					"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"        </footer>\r\n" +
					"        <!-- /footer content -->\r\n" +
					"      </div>\r\n" +
					"    </div>\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>    \r\n" +
					"    <!-- Chart.js -->\r\n" +
					"    <script src=\"%svendors/Chart.js/dist/Chart.min.js\"></script>\r\n" +
					"    <!-- jQuery Sparklines -->\r\n" +
					"    <script src=\"%svendors/jquery-sparkline/dist/jquery.sparkline.min.js\"></script>\r\n" +
					"    <!-- easy-pie-chart -->\r\n" +
					"    <script src=\"%svendors/jquery.easy-pie-chart/dist/jquery.easypiechart.min.js\"></script>\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <script src=\"%svendors/bootstrap-progressbar/bootstrap-progressbar.min.js\"></script>\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Scripts -->\r\n" +
					"    <script src=\"%sbuild/js/custom.min.js\"></script>\r\n", dir_path, dir_path, dir_path, dir_path, dir_path, dir_path, dir_path, dir_path, dir_path);
			writer.printf("    <script src = \"../../projects/%s/%s/%d/freqData.json\"></script>",project.getName(), project.getAnalysisName(), project.getAnalysisdate());
			writer.print(
					"	<!-- Google Analytics -->\r\n" +
							"	<script>\r\n" +
							"   setTimeout(\"location.reload(true)\",120000);" +
							"	(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\r\n" +
							"	(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\r\n" +
							"	m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\r\n" +
							"	})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\r\n" +
							"\r\n" +
							"	ga('create', 'UA-23581568-13', 'auto');\r\n" +
							"	ga('send', 'pageview');\r\n" +
							"\r\n" +
							"    var all_cloneset_data = [];\r\n" +
							"    freqData.forEach(function(d,i){\r\n" +
							"      all_cloneset_data[i] = d.freq.Stable + d.freq.Changed + d.freq.Deleted + d.freq.New;  \r\n" +
							"      console.log(\"all_coneset_data[i]= \" + all_cloneset_data[i]);   \r\n" +
							"    });\r\n" +
							"\r\n" +
							"    $(\".sparkline_three_allclonesets\").sparkline(all_cloneset_data,{type:\"line\",width:\"200\",height:\"40\",lineColor:\"#26B99A\",fillColor:\"rgba(223, 223, 223, 0.57)\",lineWidth:2,spotColor:\"#26B99A\",minSpotColor:\"#26B99A\"})\r\n" +
							"	</script>\r\n" +
							"	\r\n" +

							"  <script src=\"../../data/projects.json\"></script>\r\n" +
							"  <script src=\"data/analysis.json\"></script>\r\n" +
							"  <script>\r\n" +
							"    //projects配列の探索\r\n" +
							"    for (var i = 0; i < projects.length; i++) {\r\n" +
							"      $('#sidebarproject').append('<li><a href=\"../../projects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"    for (var i = 0; i < analysis.length; i++) {\r\n" +
							"      $('#sidebaranalysis').append('<li><a href=\"' + analysis[i].name + '/' + analysis[i].date + '/index.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
							"      $('#' + analysis[i].name + 'period').append('<br>' + analysis[i].olddate.substr(4, 2) + '/' + analysis[i].olddate.substr(6, 2) + '/' + analysis[i].olddate.substr(0, 4) + '<br><i style=\"margin : 5px ;\" class=\"fa fa-long-arrow-down\"></i><br>' + analysis[i].date.substr(4, 2) + '/' + analysis[i].date.substr(6, 2) + '/' + analysis[i].date.substr(0, 4));\r\n" +
							"    }\r\n" +
							"    for (var i = 0; i < analysis.length; i++) {\r\n" +
							"      $('#sidebarcloneset').append('<li><a href=\"' + analysis[i].name + '/' + analysis[i].date + '/cloneset.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"    for (var i = 0; i < analysis.length; i++) {\r\n" +
							"      $('#sidebardirectory').append('<li><a href=\"' + analysis[i].name + '/' + analysis[i].date + '/packagelist.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"    $(function () {\r\n" +
							"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
							"    })\r\n" +
							"    docCookies.setItem(\"temp\", \"true\"); //適当なcookieの書き込みを行い、\r\n" +
							"    if (\"true\" == docCookies.getItem(\"temp\")) { //正常に利用できる環境であれば実行\r\n" +
							"      if (\"true\" != docCookies.getItem(\"refresh\")) { //初回訪問時は実行される\r\n" +
							"        docCookies.setItem(\"refresh\", \"true\"); //2回目以降は、cookieに値が書き込まれているので実行されない\r\n" +
							"        docCookies.removeItem(\"temp\");\r\n" +
							"        location.reload(true); //ブラウザのキャッシュを使わずにリロードを実行\r\n" +
							"      } else {\r\n" +
							"        docCookies.removeItem(\"temp\");\r\n" +
							"      }\r\n" +
							"    }  \r\n" +
							"  </script>\r\n " +
							"  </body>\r\n" +
							"</html>\r\n" +
					"");

			writer.flush();
			writer.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * project フォルダーIndexページの生成
	 * </p>
	 *
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateDateListPage(Project project) {
		System.out.println("generateDateListPage");

		// index.htmlの削除
		File index = new File(project.getGenerateHTMLDir() +  "\\users\\" + project.getUserId() + "\\" + INDEX_PAGE);
		System.out.println("DateListPage = " + project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\");
		if (index.exists()) {
			index.delete();
		}

		// indexファイル生成
		try {
			PrintWriter writer = new PrintWriter(
					new BufferedWriter(new FileWriter(project.getGenerateHTMLDir() +"\\users\\" + project.getUserId() + "\\" + INDEX_PAGE)));

			//String cur_date = project.getDate();
			String now_date = project.getDate();
			String now_year = now_date.substring(0, 4);
			String now_month = now_date.substring(4, 6);
			String now_day = now_date.substring(6, 8);
			String cur_date = Integer.toString(project.getAnalysisdate());
			String cur_year = cur_date.substring(0, 4);
			String cur_month = cur_date.substring(4, 6);
			String cur_day = cur_date.substring(6, 8);
			String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
			String old_year = old_date.substring(0, 4);
			String old_month = old_date.substring(4, 6);
			String old_day = old_date.substring(6, 8);
			String pro_lang = project.getLang();
			System.out.println(pro_lang);

			if(pro_lang.equals("c") || pro_lang.equals("cpp")) {
				pro_lang = "C/C++";
				System.out.println(pro_lang);

			}else if(pro_lang.equals("java")) {
				pro_lang = "Java";
				System.out.println(pro_lang);
			}else if(pro_lang.equals("csharp")) {
				pro_lang = "C#";
				System.out.println(pro_lang);
			}else {
				System.out.println("error");
			}
			/*	if(project.getLang() == c_lang) {

			}else if(project.getLang() == java_lang) {
				String pro_lang = "Java";
				System.out.println(pro_lang);
				System.out.println("2");
			}
			 */

			String dir_path = "../../";

			writer.printf("<!DOCTYPE html>\r\n" +
					"<html lang=\"en\">\r\n" +
					"  <head>\r\n" +
					"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
					"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
					"    <meta charset=\"utf-8\">\r\n" +
					"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
					"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
					"\r\n" +
					"    <title>CCEvovis </title>\r\n" +
					"\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <link href=\"%svendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- Font Awesome -->\r\n" +
					"    <link href=\"%svendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <link href=\"%svendors/nprogress/nprogress.css\" rel=\"stylesheet\">\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <link href=\"%svendors/bootstrap-progressbar/css/bootstrap-progressbar-3.3.4.min.css\" rel=\"stylesheet\">\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Style -->\r\n" +
					"    <link href=\"%sbuild/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
					"    \r\n" +
					"    \r\n" +
					"  <style>\r\n" +
					"    .coverp:hover{\r\n" +
					"      border:1px solid #eea236;\r\n" +
					"    }\r\n" +
					"\r\n" +
					"  </style>\r\n" +
					"  </head>\r\n" +
					"\r\n" +
					"  <body class=\"nav-md\">\r\n" +
					"    <div class=\"container body\">\r\n" +
					"      <div class=\"main_container\">\r\n" +
					"        <div class=\"col-md-3 left_col\">\r\n" +
					"          <div class=\"left_col scroll-view\">\r\n" +
					"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
					"              <a href=\"index.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
					"            </div>\r\n" +
					"\r\n" +
					"            <div class=\"clearfix\"></div>\r\n" +
					"\r\n" +
					"            <!-- menu profile quick info -->\r\n" +
					"            <div class=\"profile clearfix\">\r\n" +
					"              <div class=\"profile_pic\">\r\n" +
					"                <img src=\"%simages/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">\r\n" +
					"              </div>\r\n" +
					"              <div class=\"profile_info\">\r\n" +
					"                <span>Welcome,</span>\r\n" +
					"                <h2>%s</h2>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n" +
					"            <!-- /menu profile quick info -->\r\n" +
					"\r\n" +
					"            <br />\r\n" +
					"\r\n",  dir_path,  dir_path,  dir_path,  dir_path,  dir_path,  dir_path,project.getUserId());

			writer.printf("            <!-- sidebar menu -->\r\n" +
					"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
					"              <div class=\"menu_section\">\r\n" +
					"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
					"                  <li><a href=\"index.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
					"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
					"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
					"                  </li>\r\n" +
					"                </ul>\r\n" +
					"              </div>\r\n" +
					"            </div>\r\n");

			writer.print(
					"            <!-- /sidebar menu -->\r\n" +
							"\r\n" +
							"            <!-- /menu footer buttons -->\r\n" +
							"            <div class=\"sidebar-footer hidden-small\">\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
							"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
							"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
							"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
							"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
							"              </a>\r\n" +
							"            </div>\r\n" +
							"            <!-- /menu footer buttons -->\r\n" +
							"          </div>\r\n" +
							"        </div>\r\n" +
					"\r\n");
			writer.printf(
					"        <!-- top navigation -->\r\n" +
							"        <div class=\"top_nav\">\r\n" +
							"          <div class=\"nav_menu\">\r\n" +
							"            <nav>\r\n" +
							"              <div class=\"nav toggle\">\r\n" +
							"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
							"              </div>\r\n" +
							"\r\n" +
							"              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
							"                <li class=\"\">\r\n" +
							"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n" +
							"                    <img src=\"%simages/img.jpg\" alt=\"\">%s\r\n" +
							"                    <span class=\" fa fa-angle-down\"></span>\r\n" +
							"                  </a>\r\n" +
							"                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
							"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
							"                  </ul>\r\n" +
							"                </li>\r\n" +
							"              </ul>\r\n" +
							"            </nav>\r\n" +
							"          </div>\r\n" +
							"        </div>\r\n" +
							"        <!-- /top navigation -->\r\n" +
							"\r\n", dir_path,project.getUserId());
			/*ページコンテンツ*/
			writer.print(
					"        <!-- page content -->\r\n" +
							"        <div class=\"right_col\" role=\"main\">\r\n" +
							"          <div class=\"page-title\">\r\n" +
							"            <div class=\"title_left\">\r\n" +
							"              <h3>Project List\r\n" +
							"                <!--<small>Some examples of D3</small> -->\r\n" +
							"              </h3>\r\n" +
							"            </div>\r\n" +
							"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n");

			try {
				PrintWriter projectwr = new PrintWriter(new BufferedWriter(new FileWriter(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\data\\project.html")));
				projectwr.printf("        <div class=\"col-md-4 col-xs-12 widget widget_tally_box\">\r\n" +
						"          <div class=\"x_panel fixed_height_4100  coverp\">\r\n" +
						"            <a href=\"projects/%s/index.html\">\r\n" +
						"              <div class=\"x_title\" style=\"text-align: center;  font-size: x-large;\">\r\n" +
						"                %s\r\n" +
						"                <div class=\"clearfix\"></div>\r\n" +
						"              </div>\r\n" +
						"              <div class=\"x_content\">\r\n" +
						"                <center><button type=\"button\" class=\"btn btn-round btn-danger\">%s</button></center>\r\n" +
						"                <div class=\"divider\"></div>\r\n" +
						"                <center>\r\n" +
						"                  <h3><a href=\"%s\" data-toggle=\"tooltip\" style=\"font-size: 35px\" data-placement=\"bottom\"\r\n" +
						"                      data-original-title=\"%s\"><i class=\"fa fa-github\"></i></a></h3>\r\n" +
						"                </center>\r\n" +
						"                <div class=\"divider\"></div>\r\n" +
						"                <center>\r\n" +
						"                  <h4>%s/%s/%s</h4>\r\n" +
						"                </center>\r\n" +
						"              </div>\r\n" +
						"            </a>\r\n" +
						"          </div>\r\n" +
						"        </div>",project.getName(),project.getName() ,pro_lang, project.getGitRepository(),project.getGitRepository(), now_month, now_day, now_year);
				projectwr.flush();
				projectwr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			/* (1) projects内を探索
			 * (2) プロジェクト名を取得
			 * (3) プロジェクト内のjson freqDateを取得
			 * (4) freqDataより描画*/
			/*プロジェクトのデータを読み込む*/
			File projectdir = new File(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() +"\\projects\\");
			File[] projectlist = projectdir.listFiles();
			System.out.println("projectlist" + projectlist.length);
			for(int i=0; i<projectlist.length; i++) {
				System.out.println(projectlist[i].toString());
				Path projectpath = Paths.get(projectlist[i].toString());
				String projectstr = projectpath.getFileName().toString();
				System.out.println(projectstr);



				//	String[] projectname = projectstr.split("\\",0);
				//if(!(projectname[projectname.length].equals("data"))) {
				if(!(projectstr.contains("data")) && !(projectstr.contains("index.html"))) {
					try {
						FileReader fr = new FileReader(projectlist[i].toString() + "\\data\\project.html");
						BufferedReader br = new BufferedReader(fr);

						//						System.out.println("project read");

						//読み込んだファイルを１行ずつ画面出力する
						String line;
						while ((line = br.readLine()) != null) {
							//							System.out.println(line);
							writer.printf("%s\n", line);
						}
						br.close();
						fr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}




			writer.printf("        <div class=\"col-md-4 col-xs-12 widget widget_tally_box\">\r\n" +
					"		   <div class=\"x_panel fixed_height_4100  coverp\" style=\"border-style:dashed\">" +
					"            <a href=\"input.html\">\r\n" +
					"              <br><br><br>\r\n" +
					"              <center><i class=\"glyphicon glyphicon-plus-sign\" style=\"font-size:45px;\"></i></center>\r\n" +
					"              <h3 style=\"text-align:center\">New Project</h3>\r\n" +
					"              <br><br><br>\r\n" +
					"            </a>\r\n" +
					"          </div>\r\n" +
					"        </div>\r\n");

			//	System.out.println(date_count);
			System.out.println(project.getTool());
			System.out.println(project.getLang());
			//writer.printf("<tr><td><a href = \"%s\">%c%c%c%c年%c%c月%c%c日</a></td></tr>\n",
			//	date + "/" + INDEX_PAGE, date.charAt(0), date.charAt(1), date.charAt(2), date.charAt(3),
			//	date.charAt(4), date.charAt(5), date.charAt(6), date.charAt(7));
			writer.printf("        </div>\r\n" +
					"        <!-- /page content -->\r\n" +
					"        <!-- footer content -->\r\n" +
					"        <footer>\r\n" +
					"          <div class=\"pull-right\"> CCEvovis by\r\n" +
					"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"        </footer>\r\n" +
					"        <!-- /footer content -->\r\n" +
					"      </div>\r\n" +
					"    </div>\r\n" +
					"    <!-- jQuery -->\r\n" +
					"    <script src=\"%svendors/jquery/dist/jquery.min.js\"></script>\r\n" +
					"    <!-- Bootstrap -->\r\n" +
					"    <script src=\"%svendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
					"    <!-- FastClick -->\r\n" +
					"    <script src=\"%svendors/fastclick/lib/fastclick.js\"></script>\r\n" +
					"    <!-- NProgress -->\r\n" +
					"    <script src=\"%svendors/nprogress/nprogress.js\"></script>    \r\n" +
					"    <!-- Chart.js -->\r\n" +
					"    <script src=\"%svendors/Chart.js/dist/Chart.min.js\"></script>\r\n" +
					"    <!-- jQuery Sparklines -->\r\n" +
					"    <script src=\"%svendors/jquery-sparkline/dist/jquery.sparkline.min.js\"></script>\r\n" +
					"    <!-- easy-pie-chart -->\r\n" +
					"    <script src=\"%svendors/jquery.easy-pie-chart/dist/jquery.easypiechart.min.js\"></script>\r\n" +
					"    <!-- bootstrap-progressbar -->\r\n" +
					"    <script src=\"%svendors/bootstrap-progressbar/bootstrap-progressbar.min.js\"></script>\r\n" +
					"\r\n" +
					"    <!-- Custom Theme Scripts -->\r\n" +
					"    <script src=\"%sbuild/js/custom.min.js\"></script>\r\n",   dir_path,  dir_path,  dir_path,  dir_path,  dir_path,  dir_path,  dir_path,  dir_path,  dir_path);
			writer.printf("    <script src = \"projects/%s/%d/freqData.json\"></script>",project.getName(), project.getAnalysisdate());
			writer.print(
					"	<!-- Google Analytics -->\r\n" +
							"	<script>\r\n" +
							"	(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\r\n" +
							"	(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\r\n" +
							"	m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\r\n" +
							"	})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\r\n" +
							"\r\n" +
							"	ga('create', 'UA-23581568-13', 'auto');\r\n" +
							"	ga('send', 'pageview');\r\n" +
							"\r\n" +
							"    var all_cloneset_data = [];\r\n" +
							"    freqData.forEach(function(d,i){\r\n" +
							"      all_cloneset_data[i] = d.freq.Stable + d.freq.Changed + d.freq.Deleted + d.freq.New;  \r\n" +
							"      console.log(\"all_coneset_data[i]= \" + all_cloneset_data[i]);   \r\n" +
							"    });\r\n" +
							"\r\n" +
							"    $(\".sparkline_three_allclonesets\").sparkline(all_cloneset_data,{type:\"line\",width:\"200\",height:\"40\",lineColor:\"#26B99A\",fillColor:\"rgba(223, 223, 223, 0.57)\",lineWidth:2,spotColor:\"#26B99A\",minSpotColor:\"#26B99A\"})\r\n" +
							"	</script>\r\n" +
							"	\r\n" +
							"  <script src=\"data/projects.json\"></script>\r\n" +
							"  <script>\r\n" +
							"    //projects配列の探索\r\n" +
							"    for(var i=0; i<projects.length; i++){\r\n" +
							"       $('#sidebarproject').append('<li><a href=\"projects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
							"    }\r\n" +
							"        $(function () {\r\n" +
							"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
							"    })\r\n" +
							"    docCookies.setItem(\"temp\", \"true\"); //適当なcookieの書き込みを行い、\r\n" +
							"    if(\"true\" == docCookies.getItem(\"temp\")){ //正常に利用できる環境であれば実行\r\n" +
							"        if(\"true\" != docCookies.getItem(\"refresh\")){ //初回訪問時は実行される\r\n" +
							"            docCookies.setItem(\"refresh\", \"true\"); //2回目以降は、cookieに値が書き込まれているので実行されない\r\n" +
							"            docCookies.removeItem(\"temp\");\r\n" +
							"            location.reload(true); //ブラウザのキャッシュを使わずにリロードを実行\r\n" +
							"        }else{\r\n" +
							"            docCookies.removeItem(\"temp\");\r\n" +
							"        }\r\n" +
							"    }" +
							"  </script>\r\n" +
							"  </body>\r\n" +
							"</html>\r\n" +
					"");

			writer.flush();
			writer.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * ソースファイルページ一覧
	 * </p>
	 *
	 * @param g
	 *            OutputGeneratorオブジェクト
	 * @param dir
	 *            出力ディレクトリ
	 * @param node
	 *            ノード
	 * @param indexDir
	 *            index.htmlの相対位置
	 * @param project
	 *            Projectオブジェクト
	 * @return
	 *         <ul>
	 *         <li>成功の場合 - true</li>
	 *         <li>失敗の場合 - false</li>
	 *         </ul>
	 */
	private boolean generateFileListPage(OutputGenerator g, File dir, int node, String indexDir,
			Project project) {

		File[] files = dir.listFiles();

		// ソースファイルが存在するか判定
		boolean flag = false;
		for (File file : files) {
			if (file.getName().equals(dir.getName() + "/" + INDEX_PAGE))
				continue;
			if (file.getName().equals(dir.getName() + "/" + CLONESETLIST_PAGE))
				continue;
			if (file.getName().equals(dir.getName() + "/" + PACKAGELIST_PAGE))
				continue;
			if (file.isFile()) {
				flag = true;
			} else if (file.isDirectory()) {
				generateFileListPage(g, file, node + 1, indexDir, project);
			}
		}
		//if (dir.getPath().equals(project.getGenerateHTMLDir() + "\\" + project.getDate()))
		if (dir.getPath().equals(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\" + project.getAnalysisName() + "\\" + project.getAnalysisdate()))
			return true;

		if (flag) {
			try {
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dir + "\\" + INDEX_PAGE)));
				// ヘッダ部出力
				//	outputHtmlHead(writer, project.getName() + "-" + dir.toString().replace(indexDir, "") + "-ソースファイル一覧");

				// タイトルの出力
				String projectFile = INDEX_PAGE;
				//String lib_path = "../" + INDEX_PAGE;
				String lib_path = "../../../../";
				String pro_data_path = "../../";
				String data_path = "";
				for (int i = 0; i < node-1; i++) {
					projectFile = "../" + projectFile;
					lib_path = "../" + lib_path;
					pro_data_path = "../" + pro_data_path;
					data_path = "../" + data_path;
				}

				String cur_date = Integer.toString(project.getAnalysisdate());
				String cur_year = cur_date.substring(0, 4);
				String cur_month = cur_date.substring(4, 6);
				String cur_day = cur_date.substring(6, 8);
				String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
				String old_year = old_date.substring(0, 4);
				String old_month = old_date.substring(4, 6);
				String old_day = old_date.substring(6, 8);

				String dir_path = dir.getPath().replace(project.getGenerateHTMLDir() +"\\users\\" + project.getUserId() +  "\\projects\\" + project.getName() + "\\" + project.getAnalysisdate() + "\\" ,"");




				writer.printf("<!DOCTYPE html>\r\n" +
						"<html lang=\"en\">\r\n" +
						"  <head>\r\n" +
						"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
						"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
						"    <meta charset=\"utf-8\">\r\n" +
						"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
						"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
						"\r\n" +
						"    <title>CCEvovis </title>\r\n" +
						"\r\n" +
						"    <!-- Bootstrap -->\r\n" +
						"    <link href=\"%s../../vendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
						"    <!-- Font Awesome -->\r\n" +
						"    <link href=\"%s../../vendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\r\n" +
						"    <!-- NProgress -->\r\n" +
						"    <link href=\"%s../../vendors/nprogress/nprogress.css\" rel=\"stylesheet\">\r\n" +
						"    <!-- iCheck -->\r\n" +
						"    <link href=\"%s../../vendors/iCheck/skins/flat/green.css\" rel=\"stylesheet\">\r\n" +
						"    <!-- Datatables -->\r\n" +
						"    <link href=\"%s../../vendors/datatables.net-bs/css/dataTables.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
						"    <link href=\"%s../../vendors/datatables.net-buttons-bs/css/buttons.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
						"    <link href=\"%s../../vendors/datatables.net-fixedheader-bs/css/fixedHeader.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
						"    <link href=\"%s../../vendors/datatables.net-responsive-bs/css/responsive.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
						"    <link href=\"%s../../vendors/datatables.net-scroller-bs/css/scroller.bootstrap.min.css\" rel=\"stylesheet\">\r\n" +
						"    \r\n" +
						"       <!-- bootstrap-daterangepicker -->\r\n" +
						"    <link href=\"%s../../vendors/bootstrap-daterangepicker/daterangepicker.css\" rel=\"stylesheet\">\r\n" +
						"    <!-- Ion.RangeSlider -->\r\n" +
						"    <link href=\"%s../../vendors/normalize-css/normalize.css\" rel=\"stylesheet\">\r\n" +
						"    <link href=\"%s../../vendors/ion.rangeSlider/css/ion.rangeSlider.css\" rel=\"stylesheet\">\r\n" +
						"    <link href=\"%s../../vendors/ion.rangeSlider/css/ion.rangeSlider.skinFlat.css\" rel=\"stylesheet\">\r\n" +
						"    <!-- Bootstrap Colorpicker -->\r\n" +
						"    <link href=\"%s../../vendors/mjolnic-bootstrap-colorpicker/dist/css/bootstrap-colorpicker.min.css\" rel=\"stylesheet\">\r\n" +
						"\r\n" +
						"    <link href=\"%s../../vendors/cropper/dist/cropper.min.css\" rel=\"stylesheet\">\r\n" +
						"\r\n" +
						"    <!-- Custom Theme Style -->\r\n" +
						"    <link href=\"%s../../build/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
						"    <style>\r\n" +
						"    button#btnmodified{\r\n" +
						"    	background-color: #807dba;\r\n" +
						"    	color: white;\r\n" +
						"    }\r\n" +
						"    </style>\r\n" +
						"   \r\n" +
						"    \r\n" +
						"  </head>\r\n" +
						"\r\n" +
						"  <body class=\"nav-md\">\r\n" +
						"    <div class=\"container body\">\r\n" +
						"      <div class=\"main_container\">\r\n" +
						"        <div class=\"col-md-3 left_col\">\r\n" +
						"          <div class=\"left_col scroll-view\">\r\n" +
						"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
						"              <a href=\"%sindex.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
						"            </div>\r\n" +
						"\r\n" +
						"            <div class=\"clearfix\"></div>\r\n" +
						"\r\n" +
						"            <!-- menu profile quick info -->\r\n" +
						"            <div class=\"profile clearfix\">\r\n" +
						"              <div class=\"profile_pic\">\r\n" +
						"                <img src=\"%s../../images/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">\r\n" +
						"              </div>\r\n" +
						"              <div class=\"profile_info\">\r\n" +
						"                <span>Welcome,</span>\r\n" +
						"                <h2>%s</h2>\r\n" +
						"              </div>\r\n" +
						"            </div>\r\n" +
						"            <!-- /menu profile quick info -->",
						lib_path,lib_path,lib_path,lib_path,lib_path,
						lib_path,lib_path,lib_path,lib_path,lib_path,
						lib_path,lib_path,lib_path,lib_path,lib_path,
						lib_path,pro_data_path,lib_path,project.getUserId()
						);


				writer.printf("            <!-- sidebar menu -->\r\n" +
						"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
						"              <div class=\"menu_section\">\r\n" +
						"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
						"                  <li><a href=\"%s../../index.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
						"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
						"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
						"                  </li>\r\n" +
						"                  <li><a><i class=\"fa fa-bar-chart\"></i> Analysis <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
						"                    <ul class=\"nav child_menu\" id=\"sidebaranalysis\"></ul>\r\n" +
						"                  </li>\r\n" +
						"                  <li><a><i class=\"fa fa-table\"></i> Clone Set <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
						"                  <ul class=\"nav child_menu\" id=\"sidebarcloneset\"></ul>\r\n" +
						"                  </li>\r\n" +
						"                   <li><a><i class=\"fa fa-file-code-o\"></i> Directory <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
						"                    <ul class=\"nav child_menu\" id=\"sidebardirectory\">\r\n" +
						"                    </ul>\r\n" +
						"                  </li>\r\n" +
						"                </ul>\r\n" +
						"              </div>\r\n" +
						"            </div>",pro_data_path);

				writer.printf("            </div>\r\n" +
						"            <!-- /sidebar menu -->\r\n" +
						"\r\n" +
						"            <!-- /menu footer buttons -->\r\n" +
						"            <div class=\"sidebar-footer hidden-small\">\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
						"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
						"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
						"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
						"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"            </div>\r\n" +
						"            <!-- /menu footer buttons -->\r\n" +
						"          </div>\r\n" +
						"        </div>\r\n" +
						"\r\n" );
				writer.print("        <!-- top navigation -->\r\n" +
						"        <div class=\"top_nav\">\r\n" +
						"          <div class=\"nav_menu\">\r\n" +
						"            <nav>\r\n" +
						"              <div class=\"nav toggle\">\r\n" +
						"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
						"              </div>\r\n" +
						"\r\n"
						);
				writer.printf("              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
						"                <li class=\"\">\r\n" +
						"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n" +
						"                    <img src=\"%s../../images/img.jpg\" alt=\"\">%s\r\n" +
						"                    <span class=\" fa fa-angle-down\"></span>\r\n" +
						"                  </a>\r\n" +
						"                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
						"                    <li><a href=\"javascript:;\"> Profile</a></li>\r\n" +
						"                    <li>\r\n" +
						"                      <a href=\"javascript:;\">\r\n", lib_path,project.getUserId());
				writer.print("                        <span class=\"badge bg-red pull-right\">50%</span>\r\n");
				writer.printf("                        <span>Settings</span>\r\n" +
						"                      </a>\r\n" +
						"                    </li>\r\n" +
						"                    <li><a href=\"javascript:;\">Help</a></li>\r\n" +
						"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
						"                  </ul>\r\n" +
						"                </li>\r\n" +
						"              </ul>\r\n" +
						"            </nav>\r\n" +
						"          </div>\r\n" +
						"        </div>\r\n");
				writer.print(
						"        <!-- /top navigation -->\r\n" +
								"\r\n" +
								"        <!-- page content -->\r\n" +
								"        <div class=\"right_col\" role=\"main\">\r\n" +
								"		  <div class=\"\">" +
								"          <div class=\"page-title\">\r\n" +
						"            <div class=\"title_left\">\r\n");
				if(project.isGitDirect()) {
					writer.printf("              <h3>Project: %s </h3>\r\n" +
							"              <h4>Analysis Title: %s</h4>\r\n" +
							"              <h4>Date: %s/%s/%s</h4>\r\n" +
							"			   <h4>Directory: %s</h4>" +
							"                <div class=\"x_content\">\r\n" +
							"                    </div>\r\n" +
							"			 </div>\r\n" +
							"          </div>\r\n"
							, project.getName(),project.getAnalysisName(), cur_month, cur_day,cur_year, dir_path.replace(project.getGenerateHTMLDir() + "\\users\\" + project.getUserId() + "\\projects\\" + project.getName() + "\\" + project.getAnalysisName() + "\\" + project.getAnalysisdate() + "\\src\\", ""));
				}else {
					writer.printf("              <h3>Project: %s </h3>\r\n" +
							"              <h4>Analysis Title: %s</h4>\r\n" +
							"              <h4>Date: %s/%s/%s</h4>\r\n" +
							"			   <h4>Directory: %s</h4>" +
							"                <div class=\"x_content\">\r\n" +
							"                    </div>\r\n" +
							"			 </div>\r\n" +
							"          </div>\r\n"
							, project.getName(),project.getAnalysisName(),cur_month, cur_day,cur_year, dir_path);
				}
				//			, project.getName(), project.getDate(), dir.toString().replace(indexDir, "").substring(1));



				/*なおして上*/






				writer.printf("            <div class=\"clearfix\"></div>\r\n" +
						"            <div class=\"row\">\r\n" +
						"\r\n" +
						"              <div class=\"col-md-12 col-sm-12 col-xs-12\">\r\n" +
						"                <div class=\"x_panel\">\r\n" +
						"                  <div class=\"x_title\">\r\n" +
						"                    <h2>List of source files including clones</h2>\r\n" +
						"                    <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
						"                      <li><a class=\"collapse-link\"><i class=\"fa fa-chevron-up\"></i></a>\r\n" +
						"                      </li>\r\n" +
						"                    </ul>\r\n" +
						"                    <div class=\"clearfix\"></div>\r\n" +
						"                  </div>\r\n" +
						"                  <div class=\"x_content\">");

				writer.printf("                    <table class=\"table table-bordered table-striped\">\r\n" +
						"                      <thead>\r\n" +
						"                        <tr>\r\n" +
						"                          <th>Link</th>\r\n" +
						"                          <th>File Name</th>\r\n" +
						"                          <th>Number of clones</th>\r\n" +
						"                          <th>Remarks</th>\r\n" +
						"                        </tr>\r\n" +
						"                      </thead>\r\n" +
						"                      <tbody>\r\n");

				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile() && !files[i].getName().equals(INDEX_PAGE)) {
						SourceFile file = null;
						for (SourceFile tmpFile : project.getFileList()) {
							if (tmpFile.getName().equals(
									files[i].toString().replace(indexDir, "").replace(".html", "").substring(1))) {
								file = tmpFile;
								break;
							}
						}
						if (file != null) {
							writer.printf("			   <tr bgcolor=\"%s\">\r\n", getSourceFileColor(file));
							writer.printf("			   <td><center><a href=\"%s\"><span class=\"fa fa-file-code-o\" aria-hidden=\"true\" style=\"font-size:large\"></span></a></center></td>\r\n",files[i].getName());
							writer.printf("			     <td><a href=\"%s\">%s</a></td>\r\n", files[i].getName(),
									files[i].getName().replace(".html", ""));
							writer.printf("			     <td>%d</td>\r\n", file.getNewCloneList().size());
							writer.printf("			     <td>%s</td>\r\n", getFileState(file));
							writer.println("		   </tr>\n");
						}
					}
				}



				writer.printf("                      </tbody>\r\n" +
						"                    </table>\r\n" +
						"\r\n" +
						"                  </div>\r\n" +
						"                </div>\r\n" +
						"              </div>\r\n" +
						"             </div>        \r\n" +
						"          </div>\r\n" +
						"        </div>\r\n" +
						"        <!-- /page content -->\r\n" +
						"\r\n" +
						"        <!-- footer content -->\r\n" +
						"        <footer>\r\n" +
						"          <div class=\"pull-right\"> CCEvovis by\r\n" +
						"            <a href=\"http://sel.ist.osaka-u.ac.jp/index.html.en\">Software Engineering Laboratory</a>\r\n" +
						"          </div>\r\n" +
						"          <div class=\"clearfix\"></div>\r\n" +
						"        </footer>\r\n" +
						"        <!-- /footer content -->\r\n" +
						"      </div>\r\n" +
						"    </div>\r\n" +
						"\r\n" +
						"    <!-- jQuery -->\r\n" +
						"    <script src=\"%s../../vendors/jquery/dist/jquery.min.js\"></script>\r\n" +
						"    <!-- Bootstrap -->\r\n" +
						"    <script src=\"%s../../vendors/bootstrap/dist/js/bootstrap.min.js\"></script>\r\n" +
						"    <!-- FastClick -->\r\n" +
						"    <script src=\"%s../../vendors/fastclick/lib/fastclick.js\"></script>\r\n" +
						"    <!-- NProgress -->\r\n" +
						"    <script src=\"%s../../vendors/nprogress/nprogress.js\"></script>\r\n" +
						"    <!-- iCheck -->\r\n" +
						"    <script src=\"%s../../vendors/iCheck/icheck.min.js\"></script>\r\n" +
						"    <!-- Datatables -->\r\n" +
						"    <script src=\"%s../../vendors/datatables.net/js/jquery.dataTables.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-bs/js/dataTables.bootstrap.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-buttons/js/dataTables.buttons.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-buttons-bs/js/buttons.bootstrap.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-buttons/js/buttons.flash.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-buttons/js/buttons.html5.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-buttons/js/buttons.print.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-fixedheader/js/dataTables.fixedHeader.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-keytable/js/dataTables.keyTable.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-responsive/js/dataTables.responsive.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-responsive-bs/js/responsive.bootstrap.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/datatables.net-scroller/js/dataTables.scroller.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/jszip/dist/jszip.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/pdfmake/build/pdfmake.min.js\"></script>\r\n" +
						"    <script src=\"%s../../vendors/pdfmake/build/vfs_fonts.js\"></script>\r\n",
						lib_path,lib_path,lib_path,lib_path,lib_path,
						lib_path,lib_path,lib_path,lib_path,lib_path,
						lib_path,lib_path,lib_path,lib_path,lib_path,
						lib_path,lib_path,lib_path,lib_path,lib_path
						);
				writer.printf(
						"\r\n" +
								"    <!-- Custom Theme Scripts -->\r\n" +
								"    <script src=\"%s../../build/js/custom.min.js\"></script>\r\n" +
								"    <!-- bootstrap-daterangepicker -->\r\n" +
								"    <script src=\"%s../../vendors/moment/min/moment.min.js\"></script>\r\n" +
								"    <script src=\"%s../../vendors/bootstrap-daterangepicker/daterangepicker.js\"></script>\r\n" +
								"    <!-- Ion.RangeSlider -->\r\n" +
								"    <script src=\"%s../../vendors/ion.rangeSlider/js/ion.rangeSlider.min.js\"></script>\r\n" +
								"    <!-- Bootstrap Colorpicker -->\r\n" +
								"    <script src=\"%s../../vendors/mjolnic-bootstrap-colorpicker/dist/js/bootstrap-colorpicker.min.js\"></script>\r\n" +
								"    <!-- jquery.inputmask -->\r\n" +
								"    <script src=\"%s../../vendors/jquery.inputmask/dist/min/jquery.inputmask.bundle.min.js\"></script>\r\n" +
								"    <!-- jQuery Knob -->\r\n" +
								"    <script src=\"%s../../vendors/jquery-knob/dist/jquery.knob.min.js\"></script>\r\n" +
								" 	 <script src=\"%s../../data/projects.json\"></script>\r\n" +
								"  <script src=\"%s../../data/analysis.json\"></script>\r\n" +
								"  <script>\r\n" +
								"    //projects配列の探索\r\n" +
								"    for (var i = 0; i < projects.length; i++) {\r\n" +
								"      $('#sidebarproject').append('<li><a href=\"%s../../projects/' + projects[i].name + '/index.html\"> ' + projects[i].name + '</a></li>');\r\n" +
								"    }\r\n" +
								"    for (var i = 0; i < analysis.length; i++) {\r\n" +
								"      $('#sidebaranalysis').append('<li><a href=\"%s../../' + analysis[i].name + '/' + analysis[i].date + '/index.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
								"    }\r\n" +
								"    for (var i = 0; i < analysis.length; i++) {\r\n" +
								"      $('#sidebarcloneset').append('<li><a href=\"%s../../' + analysis[i].name + '/' + analysis[i].date + '/cloneset.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
								"    }\r\n" +
								"    for (var i = 0; i < analysis.length; i++) {\r\n" +
								"      $('#sidebardirectory').append('<li><a href=\"%s../../' + analysis[i].name + '/' + analysis[i].date + '/packagelist.html\"> ' + analysis[i].name + '</a></li>');\r\n" +
								"    }\r\n" +
								"    $(function () {\r\n" +
								"      $('[data-toggle=\"tooltip\"]').tooltip()\r\n" +
								"    })\r\n" +
								"  </script>\r\n" +
								"\r\n" +
								"	\r\n" +
								"  </body>\r\n" +
								"</html>",  lib_path, lib_path, lib_path,lib_path,lib_path,lib_path,lib_path,pro_data_path, data_path,pro_data_path,data_path,data_path, data_path);

				writer.flush();
				writer.close();

			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * ディレクトリ削除
	 * </p>
	 *
	 * @param dir
	 *            削除するディレクトリ
	 */
	private void deleteDir(String dir) {
		File file = new File(dir);
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] f = file.listFiles();
				for (int i = 0; i < f.length; i++) {
					deleteDir(f[i].toString());
				}
			}
			file.delete();
		}
	}

	/**
	 * <p>
	 * クローンセットカラーの取得
	 * </p>
	 *
	 * @param cloneSet
	 *            クローンセット
	 * @return クローンセットカラー
	 */


	private String getCloneSetColor(CloneSet cloneSet) {
		switch (cloneSet.getCategory()) {
		case CloneSet.NEW:
			return "#d9534f";

		case CloneSet.CHANGED:
			return "#26B99A";

		case CloneSet.DELETED:
			return "#4B5F71";

		default:
			return "#337ab7";
		}
	}
	/*
	private String getCloneSetColor(CloneSet cloneSet) {
		switch (cloneSet.getCategory()) {
		case CloneSet.NEW:
			return "orange";

		case CloneSet.CHANGED:
			return "greenyellow";

		case CloneSet.DELETED:
			return "tan";

		default:
			return "white";
		}
	}*/

	/**
	 * <p>
	 * クローンカラーの取得
	 * </p>
	 *
	 * @param clone
	 *            クローン
	 * @return クローンカラー
	 */
	private String getCloneColor(Clone clone) {
		switch (clone.getCategory()) {
		case Clone.ADDED:
			return "#FFC0CB";

		case Clone.DELETED:
			return "#4B5F71";

		case Clone.MOVED:
			return "#5bc0de";

		case Clone.MODIFIED:
			return "#807dba";

		case Clone.DELETE_MODIFIED:
			return "#4B5F71";

		default:
			return "#337ab7";
		}
	}

	/**
	 * <p>
	 * ソースファイルカラーの取得
	 * </p>
	 *
	 * @param file
	 *            ソースファイル
	 * @return ソースファイルカラー
	 */
	private String getSourceFileColor(SourceFile file) {
		switch (file.getState()) {
		case SourceFile.ADDED:
			return "orange";

		case SourceFile.DELETED:
			return "tan";

		default:
			return "white";
		}
	}

	/**
	 * ソースファイル情報の取得
	 *
	 * @param file
	 *            ソースファイル
	 * @return - ソースファイル情報
	 */
	private String getFileState(SourceFile file) {
		switch (file.getState()) {
		case SourceFile.ADDED:
			return "追加ファイル";

		case SourceFile.DELETED:
			return "削除ファイル";

		default:
			return " ";
		}
	}

	/**
	 * <p>
	 * HTMLヘッダ部出力
	 * </p>
	 *
	 * @param writer
	 * @param title
	 */
	private void outputHtmlHead(PrintWriter writer, String title) {
		writer.println("<html>");
		writer.println("<head>");
		writer.println("\t<title>" + title + "</title>");
		writer.printf("\t<style type=\"text/css\">\r\n");
		writer.printf(
				"\t\t.sortable .head {background:gainsboro url(../image/sort.gif) 6px  center no-repeat; cursor:pointer; padding-left:18px}\r\n");
		writer.printf(
				"\t\t.sortable .desc {background:darkgray url(../image/desc.gif) 6px   center no-repeat; cursor:pointer; padding-left:18px}\r\n");
		writer.printf(
				"\t\t.sortable .asc {background:darkgray  url(../image/asc.gif) 6px  center no-repeat; cursor:pointer; padding-left:18px}\r\n");
		writer.printf("\t\t.sortable .head:hover, .sortable .desc:hover, .sortable .asc:hover {color:white}\r\n");
		writer.printf("\t</style>\r\n");
		writer.println("<script type=\"text/javascript\" src=\"../script.js\"></script>");
		writer.println("</head>");
		writer.println("<body>");
	}

	/**
	 * <p>
	 * HTMLヘッダ部出力(ソースファイルの場合)
	 * </p>
	 *
	 * @param writer
	 * @param file
	 * @return クローンセット一覧HTMLファイル名
	 */
	private String outputHtmlHead(OutputGenerator g, PrintWriter writer, SourceFile file, String projectName) {

		String lib_path = "../../../../";
		String pro_data_path = "../../";
		String data_path = "";
		String projectFile = INDEX_PAGE;
		String cloneSetFile = CLONESETLIST_PAGE;
		// 階層の計算
		String[] tmp = file.getName().split("\\\\");
		for (int i = 0; i < tmp.length - 1; i++) {
			projectFile = "../" + projectFile;
			cloneSetFile = "../" + cloneSetFile;
			lib_path = "../" + lib_path;
			pro_data_path = "../" + pro_data_path;
			data_path = "../" + data_path;
		}


		String cur_date = Integer.toString(project.getAnalysisdate());
		String cur_year = cur_date.substring(0, 4);
		String cur_month = cur_date.substring(4, 6);
		String cur_day = cur_date.substring(6, 8);
		String old_date = Integer.toString(project.getAnalysisdayList().get(project.getAnalysistime()-1));
		String old_year = old_date.substring(0, 4);
		String old_month = old_date.substring(4, 6);
		String old_day = old_date.substring(6, 8);


		writer.printf("<!DOCTYPE html>\r\n" +
				"<html lang=\"en\">\r\n" +
				"  <head>\r\n");
		writer.printf("		\t<style type=\"text/css\">\r\n");
		writer.println("	\t\t td,th {font-size:12px}");
		writer.printf("		\t\t td xmp {margin:0}\r\n");
		writer.printf("		\t</style>\r\n");
		writer.printf("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" +
				"    <!-- Meta, title, CSS, favicons, etc. -->\r\n" +
				"    <meta charset=\"utf-8\">\r\n" +
				"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" +
				"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n" +
				"\r\n" +
				"    <title>CCEvovis </title>\r\n");
		writer.printf("    <!-- Bootstrap -->\r\n" +
				"    <link href=\"%s../../vendors/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">", lib_path);
		writer.printf("    <!-- Font Awesome -->\r\n" +
				"    <link href=\"%s../../vendors/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">", lib_path);
		writer.printf("    <!-- NProgress -->\r\n" +
				"    <link href=\"%s../../vendors/nprogress/nprogress.css\" rel=\"stylesheet\">", lib_path);
		writer.printf("    <!-- iCheck -->\r\n" +
				"    <link href=\"%s../../vendors/iCheck/skins/flat/green.css\" rel=\"stylesheet\">\r\n" +
				"	\r\n" +
				"    <!-- bootstrap-progressbar -->\r\n" +
				"    <link href=\"%s../../vendors/bootstrap-progressbar/css/bootstrap-progressbar-3.3.4.min.css\" rel=\"stylesheet\">\r\n" +
				"    <!-- JQVMap -->\r\n" +
				"    <link href=\"%s../../vendors/jqvmap/dist/jqvmap.min.css\" rel=\"stylesheet\"/>\r\n" +
				"    <!-- bootstrap-daterangepicker -->\r\n" +
				"    <link href=\"%s../../vendors/bootstrap-daterangepicker/daterangepicker.css\" rel=\"stylesheet\">\r\n" +
				"\r\n" +
				"    <!-- Custom Theme Style -->\r\n" +
				"    <link href=\"%s../../build/css/custom.min.css\" rel=\"stylesheet\">\r\n" +
				"    \r\n" +
				"    <!--Import Google Icon Font-->\r\n" +
				"    <link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\">",lib_path, lib_path, lib_path,lib_path,lib_path );
		writer.printf("  </head>\r\n" +
				"\r\n" +
				"  <body class=\"nav-md\">\r\n" +
				"    <div class=\"container body\">\r\n" +
				"      <div class=\"main_container\">\r\n" +
				"        <div class=\"col-md-3 left_col\">\r\n" +
				"          <div class=\"left_col scroll-view\">\r\n" +
				"            <div class=\"navbar nav_title\" style=\"border: 0;\">\r\n" +
				"              <a href=\"%s../../index.html\" class=\"site_title\"><i class=\"fa fa-paw\"></i> <span>CCEvovis</span></a>\r\n" +
				"            </div>\r\n" +
				"\r\n" +
				"            <div class=\"clearfix\"></div>\r\n" +
				"\r\n" +
				"            <!-- menu profile quick info -->\r\n" +
				"            <div class=\"profile clearfix\">\r\n" +
				"              <div class=\"profile_pic\">", pro_data_path);
		writer.printf("<img src=\"%s../../images/img.jpg\" alt=\"...\" class=\"img-circle profile_img\">", lib_path);
		writer.printf("              </div>\r\n" +
				"              <div class=\"profile_info\">\r\n" +
				"                <span>Welcome,</span>\r\n" +
				"                <h2>%s</h2>\r\n" +
				"              </div>\r\n" +
				"            </div>\r\n" +
				"            <!-- /menu profile quick info -->\r\n" +
				"\r\n" +
				"            <br />\r\n" +
				"\r\n",project.getUserId());



		writer.printf("            <!-- sidebar menu -->\r\n" +
				"            <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\r\n" +
				"              <div class=\"menu_section\">\r\n" +
				"                <ul class=\"nav side-menu\" id=\"sidebar\">\r\n" +
				"                  <li><a href=\"%s../../index.html\"><i class=\"fa fa-home\"></i> Home </span></a></li>\r\n" +
				"                  <li><a><i class=\"fa fa-database\"></i> Project <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
				"                     <ul class=\"nav child_menu\" id=\"sidebarproject\"></ul>\r\n" +
				"                  </li>\r\n" +
				"                  <li><a><i class=\"fa fa-bar-chart\"></i> Analysis <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
				"                    <ul class=\"nav child_menu\" id=\"sidebaranalysis\"></ul>\r\n" +
				"                  </li>\r\n" +
				"                  <li><a><i class=\"fa fa-table\"></i> Clone Set <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
				"                  <ul class=\"nav child_menu\" id=\"sidebarcloneset\"></ul>\r\n" +
				"                  </li>\r\n" +
				"                   <li><a><i class=\"fa fa-file-code-o\"></i> Directory <span class=\"fa fa-chevron-down\"></span></a>\r\n" +
				"                    <ul class=\"nav child_menu\" id=\"sidebardirectory\">\r\n" +
				"                    </ul>\r\n" +
				"                  </li>\r\n" +
				"                </ul>\r\n" +
				"              </div>\r\n" +
				"            </div>", pro_data_path);



		writer.print(
				"            <!-- /sidebar menu -->\r\n" +
						"\r\n" +
						"            <!-- /menu footer buttons -->\r\n" +
						"            <div class=\"sidebar-footer hidden-small\">\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Settings\">\r\n" +
						"                <span class=\"glyphicon glyphicon-cog\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"FullScreen\">\r\n" +
						"                <span class=\"glyphicon glyphicon-fullscreen\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Lock\">\r\n" +
						"                <span class=\"glyphicon glyphicon-eye-close\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"              <a data-toggle=\"tooltip\" data-placement=\"top\" title=\"Logout\" href=\"login.html\">\r\n" +
						"                <span class=\"glyphicon glyphicon-off\" aria-hidden=\"true\"></span>\r\n" +
						"              </a>\r\n" +
						"            </div>\r\n" +
						"            <!-- /menu footer buttons -->\r\n" +
						"          </div>\r\n" +
						"        </div>\r\n" +
						"\r\n" +
						"        <!-- top navigation -->\r\n" +
						"        <div class=\"top_nav\">\r\n" +
						"          <div class=\"nav_menu\">\r\n" +
						"            <nav>\r\n" +
						"              <div class=\"nav toggle\">\r\n" +
						"                <a id=\"menu_toggle\"><i class=\"fa fa-bars\"></i></a>\r\n" +
						"              </div>\r\n" +
						"\r\n" +
						"              <ul class=\"nav navbar-nav navbar-right\">\r\n" +
						"                <li class=\"\">\r\n" +
				"                  <a href=\"javascript:;\" class=\"user-profile dropdown-toggle\" data-toggle=\"dropdown\" aria-expanded=\"false\">\r\n");

		writer.printf("                    <img src=\"%s../../images/img.jpg\" alt=\"\">%s\r\n", lib_path,project.getUserId());


		writer.print("                    <span class=\" fa fa-angle-down\"></span>\r\n" +
				"                  </a>\r\n" +
				"                  <ul class=\"dropdown-menu dropdown-usermenu pull-right\">\r\n" +
				"                    <li><a href=\"login.html\"><i class=\"fa fa-sign-out pull-right\"></i> Log Out</a></li>\r\n" +
				"                  </ul>\r\n" +
				"                </li>\r\n" +
				"\r\n"
				);
		writer.printf("              </ul>\r\n" +
				"            </nav>\r\n" +
				"          </div>\r\n" +
				"        </div>\r\n" +
				"        <!-- /top navigation -->\r\n" +
				"\r\n" +
				"        <!-- page content -->\r\n" +
				"        <div class=\"right_col\" role=\"main\">\r\n" +
				"          <div class=\"page-title\">\r\n" +
				"            <div class=\"title_left\">\r\n");
		if(project.isGitDirect()) {
			writer.printf("              <h3>Project: %s </h3>\r\n" +
					"              <h4>Analysis Title: %s</h4>\r\n" +
					"              <h4>Date: %s/%s/%s</h4>\r\n" +
					"              <h4>%s</h4>\r\n", project.getName(),project.getAnalysisName() ,cur_month, cur_day, cur_year, file.getName().substring(4));
			writer.printf("            </div>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"          <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"              <div class=\"row\">\r\n" +
					"                <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                  <div class=\"x_panel\">\r\n" +
					"                    <div class=\"x_title\">\r\n" +
					"                      <h2>Source file : %s\r\n", file.getName().substring(4));
		}else {
			writer.printf("              <h3>Project: %s </h3>\r\n" +
					"              <h4>Analysis Title: %s</h4>\r\n" +
					"              <h4>Date: %s/%s/%s</h4>\r\n" +
					"              <h4>%s</h4>\r\n", project.getName(),project.getAnalysisName() ,cur_month, cur_day, cur_year, file.getName());
			writer.printf("            </div>\r\n" +
					"          </div>\r\n" +
					"          <div class=\"clearfix\"></div>\r\n" +
					"          <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"              <div class=\"row\">\r\n" +
					"                <div class=\"col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n" +
					"                  <div class=\"x_panel\">\r\n" +
					"                    <div class=\"x_title\">\r\n" +
					"                      <h2>Source file : %s\r\n", file.getName());
		}
		writer.printf("                        <!-- <small></small> -->\r\n" +
				"                      </h2>\r\n" +
				"                      <ul class=\"nav navbar-right panel_toolbox\">\r\n" +
				"                        <li>\r\n" +
				"                          <a class=\"collapse-link\">\r\n" +
				"                            <i class=\"fa fa-chevron-up\"></i>\r\n" +
				"                          </a>\r\n" +
				"                        </li>\r\n" +
				"                      </ul>\r\n" +
				"                      <div class=\"clearfix\"></div>\r\n" +
				"                    </div>\r\n" +
				"                    <div class=\"x_content\">\r\n" +
				"                      <div class=\"dashboard-widget-content\">\r\n");






		return cloneSetFile;

	}
}
