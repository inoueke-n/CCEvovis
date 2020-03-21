package cn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import cn.data.Project;

public class gitController {

	static void gitClone(String repository,String branch, String workDir, int analysisDay, boolean isNew ,Project project) throws InterruptedException {
		String dirName = null;
		if(isNew) {
			dirName = "2";
		}else {
			dirName = "1";
		}
		try{
			Repository localRepo = new FileRepository( workDir+ "/" + dirName +"/src/.git" );
			Git git = new Git( localRepo );
			File clone_dir = new File (workDir + "/" + dirName + "/src");
			if( git != null && !clone_dir.exists()){
				//. git clone 無理やりsrcディレクトリを設定
				Git.cloneRepository().setURI( repository ).setDirectory( clone_dir).call();
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		String y_s = Integer.toString(analysisDay).substring(0,4);
		String m_s = Integer.toString(analysisDay).substring(4,6);
		String d_s = Integer.toString(analysisDay).substring(6,8);
		String date = y_s + "-" + m_s + "-" + d_s;


		String checkoutbranch = "git  --git-dir=" + workDir + "\\" + dirName + "\\src\\.git  --work-tree=" + workDir + "\\" + dirName  + "\\src" + " checkout " + branch;
		Runtime  Checkoutbranchruntime = Runtime.getRuntime();
		Process Checkoutbranch_p;
		try {
			Checkoutbranch_p = Checkoutbranchruntime.exec(checkoutbranch);
			Checkoutbranch_p.waitFor();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}

	}

	public static String  gitCheckout(Project project, int analysis_day, String workDir, String branch, boolean isNew) {
		// TODO 自動生成されたメソッド・スタブ
		String commitId = null;

		String dirName = null;
		if(isNew) {
			dirName = "2";
		}else {
			dirName = "1";
		}

		String y_s = Integer.toString(analysis_day).substring(0,4);
		String m_s = Integer.toString(analysis_day).substring(4,6);
		String d_s = Integer.toString(analysis_day).substring(6,8);
		String date = y_s + "-" + m_s + "-" + d_s;
		//String Revlistcmd = "git  --git-dir=E:\\tomcat_test\\antlr6\\.git  --work-tree=E:\\tomcat_test\\antlr6  rev-list -1  --until=\"2018-12-16 00:00\"  master";

		try {
			String cleanCmd = "git  --git-dir=" + workDir + "\\" + dirName + "\\src\\.git  --work-tree=" + workDir + "\\" + dirName + "\\src"  + " clean -f ";
			Runtime  cleanRuntime = Runtime.getRuntime();
			Process clean_p;
			clean_p = cleanRuntime.exec(cleanCmd);
			clean_p.waitFor();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		try {
			String addCmd = "git  --git-dir=" + workDir + "\\" + dirName + "\\src\\.git  --work-tree=" + workDir + "\\" + dirName  + "\\src" + " add .";
			Runtime  addRuntime = Runtime.getRuntime();
			Process add_p;
			add_p = addRuntime.exec(addCmd);
			add_p.waitFor();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}



		try {
			String stashCmd = "git  --git-dir=" + workDir + "\\" + dirName + "\\src\\.git  --work-tree=" + workDir + "\\" + dirName  + "\\src" +  " stash ";
			Runtime  stashRuntime = Runtime.getRuntime();
			Process stash_p;
			stash_p = stashRuntime.exec(stashCmd);
			stash_p.waitFor();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		String Revlistcmd = "git  --git-dir=" + workDir + "\\" + dirName + "\\src\\.git  --work-tree=" + workDir + "\\" + dirName + "\\src" + " rev-list -1  --until=\""+date+" 00:00\" " + branch;
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
//					project.setCommitID(line);
					commitId = line;
					String Checkoutidcmd = "git  --git-dir=" + workDir + "\\" + dirName + "\\src\\.git  --work-tree=" + workDir + "\\" + dirName  + "\\src" + " checkout " + line;
					Runtime  Checkoutidruntime = Runtime.getRuntime();
					Process Checkoutid_p = Checkoutidruntime.exec(Checkoutidcmd);
					Checkoutid_p.waitFor();

				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return commitId;


	}


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

	public static void changeDirName(Project project, Integer oldDir, Integer newDir) {
		// TODO 自動生成されたメソッド・スタブ
		Path sourcePath = Paths.get(project.getTargetDir(), String.valueOf(oldDir));
		Path targetPath = Paths.get(project.getTargetDir(), String.valueOf(newDir));
		File folder = new File(targetPath.toString());
		if(!folder.exists()) {
			try {
				Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				String repository = project.getGitRepository();
				String branch = project.getGitBranch();
				String workDir = project.getTargetDir();
				//				try {
				//					gitClone(repository,branch, workDir,newDir, project);
				//				} catch (InterruptedException e1) {
				//					// TODO 自動生成された catch ブロック
				//					e1.printStackTrace();
				//
				//				}
				e.printStackTrace();
			}

		}

	}
}
