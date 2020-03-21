package cn.data;

import java.util.ArrayList;

/**
 * <p>プロジェクトデータクラス</p>
 * @author y-yuuki
 * @author h-honda
 */
public class Project {

	/** int用NULL */
	public final static int NULL = -1;

	// 基本情報
	/** project name */
	private String name = null;

	/** analysis name */
	private String analysisname = null;

	/** commit ID */
	private String commitId = null;

	/** commit ID */
	private String oldCommitId = null;

	/** project url */
	private String url = null;

	/** date */
	private String date = null;

	/** 対象となる言語 */
	private String lang = null;

	/**
	 * 使用するクローン検出ツール
	 * @author m-sano
	 */
	private String tool = null;

	/**
	 * Gitリポジトリとbranch
	 * @author h-honda
	 */
	private String repository = null;
	private String branch = null;

	/**
	 * user情報
	 * @author h-honda
	 */
	private String userid = null;

	/**
	 * 分析間隔
	 * @author h-honda
	 */
	private int interval= 0;

	/**
	 * 分析日
	 * @author h-honda
	 */
	private int analysis_date= 0;

	/**
	 * old の　分析日
	 * @author h-honda
	 */
	private int oldanalysis_date= 0;

	/**
	 * 分析日
	 * @author h-honda
	 */
	private int analysistime= 0;

	/**
	 * 分析日
	 * @author h-honda
	 */
	private int analysisday= 0;

	/**
	 * 分析期間
	 * @author h-honda
	 */
	private int startdate = 0;
	private int enddate = 0;
	private int currentdate = 0;

	/** ターゲットのディレクトリパス */
	private String targetDir = null;

	/** 作業ディレクトリパス */
	private String workDir = null;

	/** 最新バージョンのディレクトリパス */
	private String newDir = null;

	/** 旧バージョンのディレクトリパス */
	private String oldDir = null;

	/** トークンの閾値. 指定トークン数以下のクローンを除外. */
	private int tokenTh;

	/** Gitから直接分析を行うならtrue */
	private boolean gitdirect = false;

	/** ローカルを対象に分析を行うならtrue */
	private boolean localtarget = false;

	/** ローカルターゲットのディレクトリパス */
	private String localtargetDir = null;

	/** NoToolにおけるコードクローン検出結果があるDirパス */
	private String resultDir = null;

	/** チェックアウトを自動で行うならtrue */
	private boolean checkout = false;

	/** オーバーラップの処理を行うならtrue */
	private boolean olFilter = true;

	/** テキスト形式で結果を出力するならtrue */
	private boolean generateText = false;

	/** csv形式で結果を出力するならtrue */
	private boolean generateCSV = false;

	/** html形式で結果を出力するならtrue */
	private boolean generateHtml = false;

	/** Json形式で結果を出力するならtrue */
	private boolean generateJson = false;

	/** 自動チェックアウトを行うためのコマンド */
	private String checkoutCmd = null;

	/** プロジェクトが持つファイルのリスト */
	private ArrayList<SourceFile> fileList = new ArrayList<SourceFile>();

	/** プロジェクトが持つクローンセットのリスト */
	private ArrayList<CloneSet> cloneSetList = new ArrayList<CloneSet>();

	/** コミットIDを保存 **/
    private ArrayList<String> commitID = new ArrayList<String>();

    //対象となる分析日の配列
    private ArrayList<Integer> analysis_days = new ArrayList<Integer>();

	//CSVファイル出力
	/** csv形式の結果を出力するディレクトリ */
	private String generateCSVDir = null;
	private String generateCSVFileName = null;

	//ウェブインタフェース情報
	/** html形式の結果を出力するディレクトリ */
	private String generateHTMLDir = null;

	// クローン情報DB作成用JSON出力
	/** Json形式の結果を出力するディレクトリ */
	private String generateJSONDir = null;

	//電子メール通知情報
	/** テキスト形式の結果を出力するディレクトリ */
	private String generateTextDir = null;
	private String generateTextFileName = null;

	/** アカウント復号キーファイル名 */
	private String keyFile;

	/** アカウント情報ファイル名 */
	private String accountFile;

	/** メール用ポート番号 */
	private int port;

	/** メール用ホスト名 */
	private String host;

	/** SSL設定. 1 なら SSL/TLS, 2 なら STARTTLS */
	private int ssl;

	/** 送信元メールアドレス */
	private String from;

	/** 送信先メールアドレスのリスト */
	private ArrayList<String> toList = new ArrayList<String>();

	/**
	 * <p>プロジェクト名の取得</p>
	 * @return プロジェクト名
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>プロジェクト名の設定</p>
	 * @param name プロジェクト名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>分析名の取得</p>
	 * @return 分析名
	 */
	public String getAnalysisName() {
		return analysisname;
	}
	/**
	 * <p>分析名の設定</p>
	 * @param analysisname 分析名
	 */
	public void setAnalysisName(String analysisname) {
		this.analysisname = analysisname;
	}

	/**
	 * <p>user情報の取得</p>
	 * @return UserID
	 */
	public String getUserId() {
		return userid;
	}
	/**
	 * <p>UserID情報の設定</p>
	 * @param userid
	 */
	public void setUserId(String userid) {
		this.userid = userid;
	}

	/*
	 * <p>commitIdの取得</p>
	 * @return commitId
	 */
	public String getNewCommitId() {
		return commitId;
	}

	/**
	 * <p>commitIdの設定</p>
	 * @param name commitId
	 */
	public void setNewCommitId(String commitId) {
		this.commitId = commitId;
	}

	/*
	 * <p>commitIdの取得</p>
	 * @return commitId
	 */
	public String getOldCommitId() {
		return commitId;
	}

	/**
	 * <p>commitIdの設定</p>
	 * @param name commitId
	 */
	public void setOldCommitId(String oldCommitId) {
		this.oldCommitId = oldCommitId;
	}

	/**
	 * <p>projectURLの取得</p>
	 * @return projectURL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * <p>projectURLの設定</p>
	 * @param url projectURL
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * <p>日付の取得</p>
	 * @return 日付
	 */
	public String getDate() {
		return date;
	}

	/**
	 * <p>日付の設定</p>
	 * @param date 日付
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * <p>言語の取得</p>
	 * @return 言語
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * <p>言語の設定</p>
	 * @param lang 言語
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * 作業ディレクトリの取得
	 * @return workDir
	 */
	public String getWorkDir() {
		return workDir;
	}

	/**
	 * 作業ディレクトリの設定
	 * @param workDir
	 *            設定する作業ディレクトリ
	 */
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	/**
	 * <p>
	 * 新バージョンディレクトリ名の取得
	 * </p>
	 *
	 * @return 新バージョンディレクトリ名
	 */
	public String getNewDir() {
		return newDir;
	}

	/**
	 * <p>新バージョンディレクトリ名の設定</p>
	 * @param newDir 新バージョンディレクトリ名
	 */
	public void setNewDir(String newDir) {
		this.newDir = newDir;
	}

	/**
	 * <p>旧バージョンディレクトリ名の取得</p>
	 * @return 旧バージョンディレクトリ名
	 */
	public String getOldDir() {
		return oldDir;
	}

	/**
	 * <p>旧バージョンディレクトリ名の設定</p>
	 * @param oldDir 旧バージョンディレクトリ名
	 */
	public void setOldDir(String oldDir) {
		this.oldDir = oldDir;
	}

	/**
	 * <p>
	 * 結果があるディレクトリ名の取得
	 * </p>
	 *
	 * @return 結果があるディレクトリ名
	 */
	public String getResultDir() {
		return resultDir;
	}

	/**
	 * <p>結果があるディレクトリ名の設定</p>
	 * @param resultDir 結果があるディレクトリ名
	 */
	public void setResultDir(String resultDir) {
		this.resultDir = resultDir;
	}

	/**
	 * <p>チェックアウトの有無</p>
	 * @return <ul>
	 *           <li>チェックアウトする場合 - true</li>
	 *           <li>チェックアウトしない場合 - false</li>
	 *         </ul>
	 */
	public boolean isCheckout() {
		return checkout;
	}

	/**
	 * <p>チェックアウトの有無の設定</p>
	 * @param checkout <ul>
	 *                   <li>チェックアウトする場合 - true</li>
	 *                   <li>チェックアウトしない場合 - false</li>
	 *                 </ul>
	 */
	public void setCheckout(boolean checkout) {
		this.checkout = checkout;
	}


	/**
	 * <p>オーバラッピングフィルタリングの有無</p>
	 * @return <ul>
	 *           <li>フィルタリングする場合 - true</li>
	 *           <li>フィルタリングしない場合 - false</li>
	 *         </ul>
	 */
	public boolean isOlFilter() {
		return olFilter;
	}

	/**
	 * <p>オーバラッピングフィルタリングの有無の設定</p>
	 * @param olFilter <ul>
	 *                   <li>フィルタリングする場合 - true</li>
	 *                   <li>フィルタリングしない場合 - false</li>
	 *                 </ul>
	 */
	public void setOlFilter(boolean olFilter) {
		this.olFilter = olFilter;
	}

	/**
	 * <p>チェックアウトコマンドの取得</p>
	 * @return チェックアウトコマンド
	 */
	public String getCheckoutCmd() {
		return checkoutCmd;
	}

	/**
	 * <p>チェックアウトコマンドの設定</p>
	 * @param checkoutCmd チェックアウトコマンド
	 */
	public void setCheckoutCmd(String checkoutCmd) {
		this.checkoutCmd = checkoutCmd;
	}

	/**
	 * <p>ソースファイルリストの取得</p>
	 * @return ソースファイルリスト
	 */
	public ArrayList<SourceFile> getFileList() {
		return fileList;
	}

	/**
	 * <p>クローンセットリストの取得</p>
	 * @return クローンセットリスト
	 */
	public ArrayList<CloneSet> getCloneSetList() {
		return cloneSetList;
	}

	public void setCloneSetList(ArrayList<CloneSet> newCloneSetList) {
		cloneSetList = new ArrayList<CloneSet>();
		cloneSetList.addAll(newCloneSetList);
	}

	/**
	 * <p>commitIDの取得</p>
	 * @return commmitID
	 */
	public ArrayList<String> getCommitIDList() {
		return commitID;
	}

	public  void setAddCommitId(String newCommitID) {
		commitID.add(newCommitID);
	}

	/**
	 * <p>commitIDの取得</p>
	 * @return commmitID
	 */
	public ArrayList<Integer> getAnalysisdayList() {
		return analysis_days;
	}

	public  void setAnalysisdayList(int analysisday) {
		analysis_days.add(analysisday);
	}

	/**
	 * <p>テキスト出力の有無</p>
	 * @return <ul>
	 *           <li>出力する場合 - true</li>
	 *           <li>出力しない場合 - false</li>
	 *         </ul>
	 */
	public boolean isGenerateText() {
		return generateText;
	}

	/**
	 * <p>テキスト出力の有無の設定</p>
	 * @param generateText <ul>
	 *                       <li>出力する場合 - true</li>
	 *                       <li>出力しない場合 - false</li>
	 *                     </ul>
	 */
	public void setGenerateText(boolean generateText) {
		this.generateText = generateText;
	}

	/**
	 * <p>HTMLファイル出力の有無</p>
	 * @return <ul>
	 *           <li>出力する場合 - true</li>
	 *           <li>出力しない場合 - false</li>
	 *         </ul>
	 */
	public boolean isGenerateHtml() {
		return generateHtml;
	}

	/**
	 * <p>HTMLファイル出力の有無の設定</p>
	 * @param generateHtml <ul>
	 *                       <li>出力する場合 - true</li>
	 *                       <li>出力しない場合 - false</li>
	 *                     </ul>
	 */
	public void setGenerateHtml(boolean generateHtml) {
		this.generateHtml = generateHtml;
	}

	/**
	 * <p>JSONファイル出力の有無</p>
	 * @return <ul>
	 *           <li>出力する場合 - true</li>
	 *           <li>出力しない場合 - false</li>
	 *         </ul>
	 */
	public boolean isGenerateJson() {
		return generateJson;
	}

	/**
	 * <p>Jsonファイル出力の有無の設定</p>
	 * @param generateJson <ul>
	 *                       <li>出力する場合 - true</li>
	 *                       <li>出力しない場合 - false</li>
	 *                     </ul>
	 */
	public void setGenerateJson(boolean generateJson) {
		this.generateJson = generateJson;
	}

	/**
	 * <p>CSVファイル出力の有無</p>
	 * @return <ul>
	 *           <li>出力する場合 - true</li>
	 *           <li>出力しない場合 - false</li>
	 *         </ul>
	 */
	public boolean isGenerateCSV() {
		return generateCSV;
	}

	/**
	 * <p>CSVファイル出力の有無の設定</p>
	 * @param generateCVS <ul>
	 *                      <li>出力する場合 - true</li>
	 *                      <li>出力しない場合 - false</li>
	 *                    </ul>
	 */
	public void setGenerateCSV(boolean generateCSV) {
		this.generateCSV = generateCSV;
	}


	/**
	 * <p>テキストファイル生成ディレクトリの取得</p>
	 * @return ディレクトリ名
	 */
	public String getGenerateTextDir() {
		return generateTextDir;
	}

	/**
	 *  <p>テキストファイル生成ディレクトリの設定</p>
	 * @param generateTextDir ディレクトリ名
	 */
	public void setGenerateTextDir(String generateTextDir) {
		this.generateTextDir = generateTextDir;
	}

	/**
	 * <p>CSVファイル生成ディレクトリの取得</p>
	 * @return ディレクトリ名
	 */
	public String getGenerateCSVDir() {
		return generateCSVDir;
	}

	/**
	 * <p>CSVファイル生成ディレクトリの設定</p>
	 * @param generateCSVDir ディレクトリ名
	 */
	public void setGenerateCSVDir(String generateCSVDir) {
		this.generateCSVDir = generateCSVDir;
	}

	/**
	 * <p>HTMLファイル生成ディレクトリの取得</p>
	 * @return ディレクトリ名
	 */
	public String getGenerateHTMLDir() {
		return generateHTMLDir;
	}

	/**
	 * <p>HTMLファイル生成ディレクトリの設定</p>
	 * @param generateHTMLDir ディレクトリ名
	 */
	public void setGenerateHTMLDir(String generateHTMLDir) {
		this.generateHTMLDir = generateHTMLDir;
	}

	/**
	 * <p>JSONファイル生成ディレクトリの取得</p>
	 * @return ディレクトリ名
	 */
	public String getGenerateJSONDir() {
		return generateJSONDir;
	}

	/**
	 * <p>JSONファイル生成ディレクトリの設定</p>
	 * @param generateJSONDir ディレクトリ名
	 */
	public void setGenerateJSONDir(String generateJSONDir) {
		this.generateJSONDir = generateJSONDir;
	}

	/**
	 * <p>生成テキストファイル名の取得</p>
	 * @return テキストファイル名
	 */
	public String getGenerateTextFileName() {
		return generateTextFileName;
	}

	/**
	 * <p>生成テキストファイル名の設定</p>
	 * @param generateTextFileName テキストファイル名
	 */
	public void setGenerateTextFileName(String generateTextFileName) {
		this.generateTextFileName = generateTextFileName;
	}

	/**
	 * <p>生成CSVファイル名の取得</p>
	 * @return CSVファイル名
	 */
	public String getGenerateCSVFileName() {
		return generateCSVFileName;
	}

	/**
	 * <p>生成CSVファイル名の設定</p>
	 * @param generateCSVFileName
	 */
	public void setGenerateCSVFileName(String generateCSVFileName) {
		this.generateCSVFileName = generateCSVFileName;
	}

	/**
	 * <p>電子メール用アカウントファイルの取得</p>
	 * @return アカウントファイル名
	 */
	public String getAccountFile() {
		return accountFile;
	}

	/**
	 * <p>電子メール用アカウントファイルの設定</p>
	 * @param accountFile アカウントファイル名
	 */
	public void setAccountFile(String accountFile) {
		this.accountFile = accountFile;
	}

	/**
	 * <p>電子メール用キーファイルの取得</p>
	 * @return キーファイル名
	 */
	public String getKeyFile() {
		return keyFile;
	}

	/**
	 * <p>電子メール用キーファイルの設定</p>
	 * @param keyFile キーファイル名
	 */
	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	/**
	 * <p>電子メール用ポートの取得</p>
	 * @return ポート番号
	 */
	public int getPort() {
		return port;
	}

	/**
	 * <p>電子メール用ポートの設定</p>
	 * @param port ポート番号
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * <p>電子メール用ホストの取得</p>
	 * @return ホスト名
	 */
	public String getHost() {
		return host;
	}

	/**
	 * <p>電子メール用ホストの設定</p>
	 * @param host ホスト名
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * <p>電子メール用SSLの取得</p>
	 * @return SSL
	 */
	public int getSsl() {
		return ssl;
	}

	/**
	 * <p>電子メール用SSLの設定</p>
	 * @param ssl SSL
	 */
	public void setSsl(int ssl) {
		this.ssl = ssl;
	}

	/**
	 * <p>電子メール送信元の取得</p>
	 * @return 電子メール送信元
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * <p>電子メール送信元の設定</p>
	 * @param from 電子メール送信元
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * <p>電子メール送信先リストの取得</p>
	 * @return 電子メール送信先リスト
	 */
	public ArrayList<String> getToList() {
		return toList;
	}

	/**
	 * <p>トークン閾値の取得</p>
	 * @return
	 */
	public int getTokenTh() {
		return tokenTh;
	}

	/**
	 * <p>トークン閾値の設定</p>
	 * @param tokenTh
	 */
	public void setTokenTh(int tokenTh) {
		this.tokenTh = tokenTh;
	}

	/**
	 * <p>該当ファイル名の SourceFile オブジェクト取得</p>
	 * @param fileList ファイルリスト
	 * @param name ファイル名
	 * @return SourceFile オブジェクト
	 */
	public static SourceFile getFileObj(ArrayList<SourceFile> fileList, String name) {
		for(SourceFile file: fileList) {
			if(file.getName().equals(name)) {
				return file;
			}
		}
		return null;
	}







	/**
	 * <p>該当ファイルIDの SourceFile オブジェクト取得</p>
	 * @param fileList ファイルリスト
	 * @param id ファイルID
	 * @return SourceFile オブジェクト
	 */
	public static SourceFile getFileObj(ArrayList<SourceFile> fileList, int id){
		for(SourceFile file: fileList){
			if(file.getId()==id)
				return file;
		}
		return null;
	}



	 /**
	  * <p>クローンセットを危険度順で整列</p>
	  */
	public void sortCloneSetListbyRisk() {
		for(int i = 0; i < cloneSetList.size(); i++) {
			for(int j = i; j > 0 && cloneSetList.get(j - 1).getRisk() < cloneSetList.get(j).getRisk(); j--) {
				CloneSet tmp = cloneSetList.get(j);
				cloneSetList.set(j, cloneSetList.get(j-1));
				cloneSetList.set(j-1, tmp);
			}
		}
	}

	 /**
	  * <p>クローンセットを分類順で整列</p>
	  */
	public void sortCloneSetListbyCategory() {
		//分類順にソート
		for (int i = 0; i < cloneSetList.size(); i++) {
			for (int j = i; j > 0 && cloneSetList.get(j).getCategory() < cloneSetList.get(j - 1).getCategory(); j--) {
				CloneSet tmp = cloneSetList.get(j);
				cloneSetList.set(j, cloneSetList.get(j - 1));
				cloneSetList.set(j - 1, tmp);
			}
		}
		//ChangedCloneSetの細分類順にソート
		for(int i = 0; i < cloneSetList.size(); i++) {
			if (cloneSetList.get(i).getCategory() != CloneSet.CHANGED)
				continue;
			for (int j = i; j > 0
					&& cloneSetList.get(j).getDivCategory() > cloneSetList.get(j - 1).getDivCategory(); j--) {
				CloneSet tmp = cloneSetList.get(j);
				cloneSetList.set(j, cloneSetList.get(j-1));
				cloneSetList.set(j-1, tmp);
			}
		}
	}

	/**
	  * <p>クローンセットをLEN順で整列</p>
	  */
	public void sortCloneSetListbyLEN() {
		for(int i = 0; i < cloneSetList.size(); i++) {
			for(int j = i; j > 0 && cloneSetList.get(j).getLEN() > cloneSetList.get(j-1).getLEN(); j--) {
				CloneSet tmp = cloneSetList.get(j);
				cloneSetList.set(j, cloneSetList.get(j-1));
				cloneSetList.set(j-1, tmp);
			}
		}
	}

	/**
	 * 使用ツールの取得
	 * @author m-sano
	 * @return 使用ツール名
	 */
	public String getTool() {
		return tool;
	}

	/**
	 * 使用ツールの設定
	 * @author m-sano
	 * @param tool 使用ツール名
	 */
	public void setTool(String tool) {
		this.tool = tool;
	}

	/**
	 * 分析間隔の取得
	 * @author h-honda
	 * @return 分析間隔
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * 分析間隔の設定
	 * @author h-honda
	 * @param 分析間隔
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}


	/**
	 * Gitリポジトリの取得
	 * @author h-honda
	 * @return リポジトリネーム
	 */
	public String getGitRepository() {
		return repository;
	}
	/**
	 * Gitリポジトリの設定
	 * @author h-honda
	 * @param リポジトリネーム
	 */
	public void setGitRepository(String repository) {
		this.repository = repository;
	}

	/**
	 * Gitリポジトリの取得
	 * @author h-honda
	 * @return リポジトリネーム
	 */
	public String getGitBranch() {
		return branch;
	}
	/**
	 * Gitリポジトリの設定
	 * @author h-honda
	 * @param リポジトリネーム
	 */
	public void setGitBranch(String branch) {
		this.branch = branch;
	}

	/**
	 * 分析期間の取得
	 * @author h-honda
	 * @return 分析期間
	 */
	public int getStartdate() {
		return startdate;
	}
	public int getEnddate() {
		return enddate;
	}

	/**
	 * 分析期間の設定
	 * @author h-honda
	 * @param 分析期間
	 */
	public void setStartdate(int startdate) {
		this.startdate= startdate;
	}
	public void setEnddate(int enddate) {
		this.enddate= enddate;
	}


	/**
	 * 現在日時の設定
	 * @author h-honda
	 * @param 現在日時
	 */
	public void setCurrentdate(int currentdate) {
		this.currentdate= currentdate;
	}
	public int getCurrentdate() {
		return currentdate;
	}


	/**
	 * 分析日の設定
	 * @author h-honda
	 * @param 分析日
	 */
	public void setAnalysisdate(int analysis_date) {
		this.analysis_date= analysis_date;
	}
	public int getAnalysisdate() {
		return analysis_date;
	}

	/**
	 * Oldバージョンの分析日の設定
	 * @author h-honda
	 * @param 分析日
	 */
	public void setOldAnalysisdate(int oldanalysis_date) {
		this.oldanalysis_date= oldanalysis_date;
	}
	public int getOldAnalysisdate() {
		return oldanalysis_date;
	}

	/**
	 * 分析回数の設定
	 * @author h-honda
	 * @param 分析回数
	 */
	public void setAnalysistime(int analysistime) {
		this.analysistime= analysistime;
	}
	public int getAnalysistime() {
		return analysistime;
	}

	/**
	 * ターゲットディレクトリの設定
	 * @author h-honda
	 * @param パス
	 */
	public void setTargetDir(String targetDir) {
		this.targetDir= targetDir;
	}
	public String getTargetDir() {
		return targetDir;
	}

	/**
	 * <p>Gitから直接cloneの有無</p>
	 * @return <ul>
	 *           <li>する場合 - true</li>
	 *           <li>しない場合 - false</li>
	 *         </ul>
	 */
	public boolean isGitDirect() {
		return gitdirect;
	}

	/**
	 * <p>Gitから直接cloneの有無の設定</p>
	 * @param checkout <ul>
	 *                   <li>する場合 - true</li>
	 *                   <li>しない場合 - false</li>
	 *                 </ul>
	 */
	public void setGitDirect(boolean gitdirect) {
		this.gitdirect = gitdirect;
	}

	/**
	 * <p>ローカル対象の有無</p>
	 * @return <ul>
	 *           <li>ローカルと対象とする場合 - true</li>
	 *           <li>ローカルを対象としない場合 - false</li>
	 *         </ul>
	 */
	public boolean isLocaltarget() {
		return localtarget;
	}

	/**
	 * <p>ローカル対象の有無の設定</p>
	 * @param checkout <ul>
	 *                   <li>ローカルを対象とする場合 - true</li>
	 *                   <li>ローカルを対象としない場合 - false</li>
	 *                 </ul>
	 */
	public void setLocaltarget(boolean localtarget) {
		this.localtarget = localtarget;
	}

	public void init_project() {

		fileList.clear();
		cloneSetList.clear();
	}

}
