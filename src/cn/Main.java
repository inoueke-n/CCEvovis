package cn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import cn.analyze.AnalyzeManager;
import cn.data.Project;
import cn.data.SetDate;
import cn.generate.OutputGenerator;

/**
 * CCEvovisメインクラス.
 *
 * @version 3.0.180115
 * @author y-yuuki
 * @author s-tokui
 * @author h-honda
 */

public class Main {
	/**
	 * メインメソッド.
	 *
	 * @param args
	 *            <code><i>SettingFileName</i> [ <i>SettingFileName</i>... ]</code>
	 */
	public static void main(String[] args) {
		boolean Flag1 = false;
		boolean Flag2 = true;

		// ログファイル初期化
		try {
			Logger.init();
		} catch (IOException e) {
			Logger.printlnConsole("Can't generate log file.", Logger.ERROR);
			System.exit(1);
		}

		// CloneNotifierのパス設定
		Path path = null;
		try {
			path = Paths.get(Main.class.getClassLoader().getResource("").toURI());
		} catch (URISyntaxException e) {
			Logger.writeError(e);
			System.exit(1);
		}
		if (path.endsWith("bin"))
			path = path.getParent();
		Def.NOTIFIER_PATH = path.toString();

		// 引数には複数の設定ファイルを指定できる
		int argnum = 1;
		for (String arg : args) {
			Logger.writeln("--- Analyze Project" + Integer.toString(argnum) + "---", Logger.SYSTEM);
			Logger.printlnConsole("--- Analyze Project" + Integer.toString(argnum++) + "---", Logger.SYSTEM);

			Project project = new Project();

			// 設定ファイル読込み
			if (SettingFileLoader.loadSettingFile(arg, project)) {
				Logger.writeln("<Success> Load setting file.", Logger.SYSTEM);
				user_info(project);



				// 中間ファイルディレクトリ生成
				if (!(new File(project.getWorkDir())).exists()) {
					Logger.writeln("Create directory 'file'.", Logger.INFO);
					(new File(project.getWorkDir())).mkdirs();
				}

				boolean okFlg = true; // プロジェクトのチェックアウトに失敗したら false になる

				// 自動チェックアウトの実行
				if (project.isCheckout()) {
					if (!(okFlg = VCSController.checkoutProject(project))) {
						Logger.writeln("Can't checkout project.", Logger.ERROR);
						okFlg = false;
					} else {
						Logger.writeln("<Success> Checkout new version of " + project.getName() + ".", Logger.SYSTEM);
					}
				}


				//対象となる分析日の配列
				ArrayList<Integer> getanalysisdays = new ArrayList<Integer>();


				//Gitから直接cloneして実行
				if(project.isGitDirect()) {
					getanalysisdays = getAnalysisDays(project);
					for(int i=0; i<getanalysisdays.size(); i++) {
						System.out.println("day = " + i + getanalysisdays.get(i));
					}

					/*
					 * clone ファイル名は一番目の日付と二番目の日付
					 *
					 * */

					//String repository = "https://github.com/apache/ant.git";
					String repository = project.getGitRepository();
					String branch = project.getGitBranch();
					String workDir = project.getTargetDir();
					//					GitHubから分析対象のリポジトリを用意
					//					for(int i=0; i<getanalysisdays.size(); i++) {
					//						try {
					//							//							gitController.gitClone(repository,branch, workDir,getanalysisdays.get(i), project);
					//							gitController.gitclone(repository,branch, workDir,getanalysisdays.get(i), project);
					//						} catch (InterruptedException e1) {
					//							// TODO 自動生成された catch ブロック
					//							e1.printStackTrace();
					//						}
					//					}
					try {
						//				gitController.gitClone(repository,branch, workDir,getanalysisdays.get(i), project);
						gitController.gitClone(repository,branch, workDir,getanalysisdays.get(0), Flag1,project);
						gitController.gitClone(repository,branch, workDir,getanalysisdays.get(1), Flag2 ,project);
					} catch (InterruptedException e1) {
						// TODO 自動生成された catch ブロック
						e1.printStackTrace();
					}
				}else if(project.isLocaltarget()){
					System.out.println("Local folder target");
					//System.out.println(project.getLocalTargetDir());
					File localtargetdir = new File(project.getTargetDir());
					String[] localfiles = localtargetdir.list();
					for (int i = 0; i < localfiles.length; i++) {
						System.out.println(localfiles[i]);
						project.setAnalysisdayList(Integer.parseInt(localfiles[i]));
						getanalysisdays.add(Integer.parseInt(localfiles[i]));
					}
				}

				for(int i=0; i<getanalysisdays.size()-1; i++) {
					project.setAnalysistime(project.getAnalysistime()+1);
					//分析日の指定
					project.setAnalysisdate(getanalysisdays.get(1+i));
					project.setOldAnalysisdate(getanalysisdays.get(i));
					String oldCommitId;
					String newCommitId;
					if(project.isGitDirect()) {

						if(i == 0 ) {
							oldCommitId = gitController.gitCheckout(project,getanalysisdays.get(i), project.getTargetDir(),project.getGitBranch(),Flag1 );
							newCommitId = gitController.gitCheckout(project,getanalysisdays.get(i+1), project.getTargetDir(),project.getGitBranch(), Flag2);
							project.setAddCommitId(oldCommitId);
							project.setAddCommitId(newCommitId);
							project.setOldDir(project.getTargetDir() + "\\1");
							project.setNewDir(project.getTargetDir() + "\\2");
						}else if(i >0) {
							if(i%2 ==0) {
								newCommitId = gitController.gitCheckout(project,getanalysisdays.get(i+1), project.getTargetDir(),project.getGitBranch(), Flag2);
								//OldバージョンとNewバージョンのプロジェクトの場所を指定
								project.setOldDir(project.getTargetDir() + "\\1");
								project.setNewDir(project.getTargetDir() + "\\2");
							}else {
								newCommitId = gitController.gitCheckout(project,getanalysisdays.get(i+1), project.getTargetDir(),project.getGitBranch(), Flag1);
								//OldバージョンとNewバージョンのプロジェクトの場所を指定
								project.setOldDir(project.getTargetDir() + "\\2");
								project.setNewDir(project.getTargetDir() + "\\1");
							}
							project.setAddCommitId(newCommitId);
						}
					}else {
						//OldバージョンとNewバージョンのプロジェクトの場所を指定
						project.setOldDir(project.getTargetDir() + "\\" + getanalysisdays.get(i));
						project.setNewDir(project.getTargetDir() + "\\" + getanalysisdays.get(i+1));
					}


					//	project.setNewDir(project.getNewDir() + project.getTerm);
					// チェックアウトに成功した
					if (okFlg) {

						// コードクローン情報の取得
						if (AnalyzeManager.getCloneInf(project, i)) {
							Logger.writeln("<Success> Extract code clone information.", Logger.SYSTEM);
							Logger.printlnConsole("Analyze code clones.", Logger.SYSTEM);

							// コードクローンの分類，分析
							if (AnalyzeManager.analyzeClone(project)) {
								Logger.writeln("<Success> Categorize code clones.", Logger.SYSTEM);

								// 分類結果の出力
								OutputGenerator generator = new OutputGenerator(project);

								// TEXT
								if (project.isGenerateText()) {
									generator.generateTextFile();
								}

								// HTML
								if (project.isGenerateHtml()) {
									generator.generateHTMLFile();
								}

								// CSV
								if (project.isGenerateCSV()) {
									generator.generateCSVFile();
								}

								// JSON
								if (project.isGenerateJson()) {
									generator.generateJsonFile();
								}

								generator = null;
								// project 部分clear
								project.init_project();

								System.gc();
							}
						} else {
							Logger.writeln("Can't extract code clone information.", Logger.ERROR);
						}
					}
					try {
						deleteTempfiles(new File(Paths.get(project.getWorkDir(), "clone").toString()));
					} catch (Exception e) {
						Logger.printlnConsole("Can't delete temp files under \"workdir/clone/\".", Logger.ERROR);
					}


				}

			} else {
				Logger.writeln("Can't load setting file.", Logger.ERROR);
			}
			// delete temp files under "workdir/clone/"
			try {
				deleteTempfiles(new File(Paths.get(project.getWorkDir(), "clone").toString()));
			} catch (Exception e) {
				Logger.printlnConsole("Can't delete temp files under \"workdir/clone/\".", Logger.ERROR);
			}
		}
		Logger.finish("End.", Logger.SYSTEM);
	}


	private static ArrayList<Integer> getAnalysisDays(Project project) {
		ArrayList<Integer> getanalysisdays;
		String y_s = Integer.toString(project.getStartdate()).substring(0,4);
		String m_s = Integer.toString(project.getStartdate()).substring(4,6);
		String d_s = Integer.toString(project.getStartdate()).substring(6,8);

		int y = Integer.parseInt(y_s);
		int m = Integer.parseInt(m_s);
		int d = Integer.parseInt(d_s);

		//分析日の配列を作成

		SetDate nd = new SetDate(y, m, d);

		project.setAnalysisdayList(project.getStartdate());


		//分析日の数が10までになっている？
		for(int i=0; i<30; i++) {
			nd.addDate(project.getInterval());
			if(Integer.parseInt(nd.getDate()) > project.getEnddate()){
				break;
			}

			project.setAnalysisdayList(Integer.parseInt(nd.getDate()));

		}

		getanalysisdays = project.getAnalysisdayList();
		return getanalysisdays;
	}


	private static void deleteTempfiles(File file) throws Exception {
		// 存在しない場合は処理終了
		if (!file.exists()) {
			return;
		}
		// 対象がディレクトリの場合は再帰処理
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				deleteTempfiles(child);
			}
		}
		// 対象がファイルもしくは配下が空のディレクトリの場合は削除する
		file.delete();
	}



	//Runtime Checkoutruntime = Runtime.getRuntime(); // ランタイムオブジェクトを取得する

	static void gitclone(String repository,String branch, String workDir, int analysis_day, Project project) throws InterruptedException {
		try{
			Repository localRepo = new FileRepository( workDir+ "/"+ analysis_day + "/src/.git" );
			Git git = new Git( localRepo );
			File clone_dir = new File (workDir + "/" + analysis_day + "/src");
			if( git != null && !clone_dir.exists()){
				//. git clone 無理やりsrcディレクトリを設定
				Git.cloneRepository().setURI( repository ).setDirectory( clone_dir).call();
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		String y_s = Integer.toString(analysis_day).substring(0,4);
		String m_s = Integer.toString(analysis_day).substring(4,6);
		String d_s = Integer.toString(analysis_day).substring(6,8);
		String date = y_s + "-" + m_s + "-" + d_s;


		String checkoutbranch = "git  --git-dir=" + workDir + "\\" + analysis_day + "\\src\\.git  --work-tree=" + workDir + "\\" + analysis_day  + "\\src" + " checkout " + branch;
		Runtime  Checkoutbranchruntime = Runtime.getRuntime();
		Process Checkoutbranch_p;
		try {
			Checkoutbranch_p = Checkoutbranchruntime.exec(checkoutbranch);
			Checkoutbranch_p.waitFor();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}


		//String Revlistcmd = "git  --git-dir=E:\\tomcat_test\\antlr6\\.git  --work-tree=E:\\tomcat_test\\antlr6  rev-list -1  --until=\"2018-12-16 00:00\"  master";
		String Revlistcmd = "git  --git-dir=" + workDir + "\\" + analysis_day + "\\src\\.git  --work-tree=" + workDir + "\\" + analysis_day + "\\src" + " rev-list -1  --until=\""+date+" 00:00\" " + branch;
		//	System.out.println(Arrays.toString(Revlistcmd));
		System.out.println(Revlistcmd);
		try {
			Runtime  Revlistruntime = Runtime.getRuntime();
			Process Revlist_p = Revlistruntime.exec(Revlistcmd);

			Revlist_p.waitFor();

			InputStream is = Revlist_p.getInputStream(); // プロセスの結果を変数に格納する
			BufferedReader br = new BufferedReader(new InputStreamReader(is)); // テキスト読み込みを行えるようにする

			while (true) {
				String line = br.readLine();
				if (line == null) {
					break; // 全ての行を読み切ったら抜ける
				} else {
					project.setAddCommitId(line);
					String Checkoutidcmd = "git  --git-dir=" + workDir + "\\" + analysis_day + "\\src\\.git  --work-tree=" + workDir + "\\" + analysis_day  + "\\src" + " checkout " + line;
					Runtime  Checkoutidruntime = Runtime.getRuntime();
					Process Checkoutid_p = Checkoutidruntime.exec(Checkoutidcmd);
					Checkoutid_p.waitFor();

				}
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}



	public static void printInputStream(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			for (;;) {
				String line = br.readLine();
				if (line == null) break;
			}
		} finally {
			br.close();
		}
	}
	static void copy(InputStream in, OutputStream out) throws IOException {
		while (true) {
			int c = in.read();
			if (c == -1) break;
			out.write((char)c);
		}
	}

	public static void user_info(Project project) {
		if(project.getUserId() == null) {
			project.setUserId("guest");

		}
	}
}

