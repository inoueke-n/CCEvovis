package jp.ac.osaka_u.sel.y_yuuki.cnsetter.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * <p>GUIクラス</p>
 * @author y-yuuki
 */
public class Gui extends JFrame{

	private static final long serialVersionUID = 1L;

	//パネル
	final JPanel mainPanel = new JPanel();
	JScrollPane scrollpane = new JScrollPane(mainPanel);

	final JPanel baseSettingPanel = new JPanel();
	JScrollPane scrollpane2 = new JScrollPane(baseSettingPanel);
	final JPanel selectPanel = new JPanel();
	final JPanel csvSettingPanel = new JPanel();
	final JPanel mailSettingPanel = new JPanel();
	final JPanel webSettingPanel = new JPanel();
	//final JPanel cloneSettingPanel = new JPanel();
	final JPanel endSettingPanel = new JPanel();
	final JPanel inputSettingPanel = new JPanel();

	//テキストフィールド
	final JTextField userId = new JTextField(15);

	final JTextField projectName = new JTextField(15);
	final JTextField analysisName = new JTextField(15);
	final JTextField workDirName = new JTextField(35);
	final JTextField gitURL = new JTextField(40);
	final JTextField gitBranch = new JTextField(15);
	final JTextField gitCloneDir = new JTextField(40);
	final JTextField startDate = new JTextField(10);
	final JTextField endDate = new JTextField(10);
	final JTextField interval = new JTextField(5);
	final JTextField localtargetDir = new JTextField(40);
	final JTextField checkoutCMD = new JTextField(40);
	final JTextField checkoutDir = new JTextField(35);
	final JTextField newVersion = new JTextField(35);
	final JTextField oldVersion = new JTextField(35);
	final JTextField csv = new JTextField(35);
	final JTextField text = new JTextField(35);
	final JTextField html = new JTextField(35);
	final JTextField accountFile = new JTextField(35);
	final JTextField keyFile = new JTextField(35);
	final JTextField host = new JTextField(25);
	final JTextField port = new JTextField(10);
	final JTextField from = new JTextField(30);
	final JTextField to1 = new JTextField(30);
	final JTextField to2 = new JTextField(30);
	final JTextField to3 = new JTextField(30);
	final JTextField settingFileName = new JTextField(40);
	final JTextField inputsettingFileName = new JTextField(40);
	final JLabel message = new JLabel(" ");

	//コンボボックス
	JComboBox<String> lang = new JComboBox<String>();
	JComboBox<String> ssl = new JComboBox<String> ();
	JComboBox<String> tool = new JComboBox<String>();

	//チェックボックス
	final JCheckBox setWorkDir = new JCheckBox("作業用ディレクトリを設定する");
	final JCheckBox csvCheck = new JCheckBox("CSVファイルを出力する");
	final JCheckBox webCheck = new JCheckBox("ウェブUIの更新を行う");
//	final JCheckBox mailCheck = new JCheckBox("電子メールによる通知を行う");
	final JCheckBox tokenThreshold = new JCheckBox("トークン閾値を設定する");
	//final JCheckBox rnrFilter = new JCheckBox("RNRフィルタリングする");
	final JCheckBox overLappingFilter = new JCheckBox("オーバラッピングフィルタリングする");

	//スパイナ
	final JSpinner token = new JSpinner(new SpinnerNumberModel(50,30,60,1));
	//final JSpinner rnr = new JSpinner(new SpinnerNumberModel(50,0,100,1));

	//ラジオボタン
	//ラジオボタン
	final JRadioButton gitRadio = new JRadioButton("Gitリポジトリの分析をする",true);
	final JRadioButton localRadio = new JRadioButton("ローカルの分析をする");
//	final JRadioButton autoRadio = new JRadioButton("自動でチェックアウトを行う");
//	final JRadioButton manualRadio = new JRadioButton("手動でチェックアウトを行う");
	final ButtonGroup radioGroup = new ButtonGroup();

	//参照ボタン
	final JButton refButton0 = new JButton("参照");
	final JButton refButton1 = new JButton("参照");
	final JButton refButton2 = new JButton("参照");
	final JButton refButton3 = new JButton("参照");
	final JButton refButton4 = new JButton("参照");
	final JButton refButton5 = new JButton("参照");
	final JButton refButton6 = new JButton("参照");
	final JButton refButton7 = new JButton("参照");
	final JButton refButton8 = new JButton("参照");
	final JButton refButton9 = new JButton("参照");
	final JButton refButton10 = new JButton("参照");
	final JButton refButton11 = new JButton("参照");
	final JButton generateButton = new JButton("設定ファイル生成");
	final JButton runButton = new JButton("実行");

	//ファイル選択ディレクトリ
	final JFileChooser chooser = new JFileChooser();


	private final static String CCFX_NAME = "CCFinderX";
//	private final static String CD_NAME = "CloneDetector";
	private final static String CCV_NAME = "CCVolti";
	private final static String SCC_NAME = "SourcererCC";

	/**
	 * @param String ToolName
	 */
	private void changeLangComboBox(String ToolName) {
		lang.removeAllItems();
		if(ToolName.equals(CCFX_NAME)) {
			lang.addItem("java");
			lang.addItem("cpp");
			lang.addItem("cobol");
			lang.addItem("csharp");
			lang.addItem("visualbasic");
			lang.addItem("plaintext");
//		}else if(ToolName.equals(CD_NAME)) {
//			lang.addItem("java");
//			lang.addItem("c");
		}else if(ToolName.equals(CCV_NAME)) {
			lang.addItem("java");
			lang.addItem("c");
			lang.addItem("csharp");
		} else if (ToolName.equals(SCC_NAME)) {
			lang.addItem("cpp");
			lang.addItem("java");
			lang.addItem("csharp");
			lang.addItem("js");
			lang.addItem("python");
		} else {
			lang.addItem("java");
			lang.addItem("c");
		}
	}

	/**
	 * <p>コンストラクタ</p>
	 */
	public Gui(){

		//初期状態
		localtargetDir.setEditable(false);
		workDirName.setEditable(false);
		checkoutCMD.setEditable(false);
		checkoutDir.setEditable(false);
		newVersion.setEditable(false);
		oldVersion.setEditable(false);
		text.setEditable(false);
		html.setEditable(false);
		csv.setEditable(false);
		accountFile.setEditable(false);
		keyFile.setEditable(false);
		host.setEditable(false);
		port.setEditable(false);
		from.setEditable(false);
		to1.setEditable(false);
		to2.setEditable(false);
		to3.setEditable(false);
		ssl.setEnabled(false);
		token.setEnabled(false);
		//rnr.setEnabled(false);
		refButton0.setEnabled(false);
		refButton1.setEnabled(false);
		refButton2.setEnabled(false);
		refButton3.setEnabled(false);
		refButton4.setEnabled(false);
		refButton5.setEnabled(false);
		refButton6.setEnabled(false);
		refButton7.setEnabled(false);

		//ラジオボタン設定
		radioGroup.add(gitRadio);
		radioGroup.add(localRadio);
//		radioGroup.add(autoRadio);
//		radioGroup.add(manualRadio);

		// 言語コンボボックス設定
		changeLangComboBox(CCFX_NAME);

		// コンボボックス設定
		tool.addItem(CCFX_NAME);
//		tool.addItem(CD_NAME);
		tool.addItem(CCV_NAME);
		tool.addItem(SCC_NAME);

		//SSLコンボボックス設定
		ssl.addItem("指定なし");
		ssl.addItem("SSL/TLS");
		ssl.addItem("STARTTLS");

		//参照ボタン0
		//作業用dir
		refButton0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					workDirName.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン1
		refButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					newVersion.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン2
		refButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					oldVersion.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン3
		refButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					html.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン4
		refButton4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					csv.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン5
		refButton5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					text.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン6
		refButton6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					accountFile.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン7
		refButton7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					keyFile.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン8
		refButton8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					settingFileName.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン9
		refButton9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					gitCloneDir.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン10
		refButton10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					localtargetDir.setText(chooser.getSelectedFile().toString());
			}
		});

		//参照ボタン11
		refButton11.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					inputsettingFileName.setText(chooser.getSelectedFile().toString());
			}
		});

		// 作業ディレクトリボタン設定
		setWorkDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				workDirName.setEditable(setWorkDir.isSelected());
				refButton0.setEnabled(setWorkDir.isSelected());
			}
		});


		//Gitボタン設定
		gitRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gitURL.setEditable(gitRadio.isSelected());
				gitCloneDir.setEditable(gitRadio.isSelected());
				refButton9.setEnabled(gitRadio.isSelected());
				gitBranch.setEditable(gitRadio.isSelected());
				startDate.setEditable(gitRadio.isSelected());
				endDate.setEditable(gitRadio.isSelected());
				interval.setEditable(gitRadio.isSelected());
				localtargetDir.setEditable(!gitRadio.isSelected());
				checkoutCMD.setEditable(!gitRadio.isSelected());
				checkoutDir.setEditable(!gitRadio.isSelected());
				oldVersion.setEditable(!gitRadio.isSelected());
				newVersion.setEditable(!gitRadio.isSelected());
				refButton1.setEnabled(!gitRadio.isSelected());
				refButton2.setEnabled(!gitRadio.isSelected());
			}
		});

		//ローカルボタン設定
		localRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gitURL.setEditable(!localRadio.isSelected());
				gitCloneDir.setEditable(!localRadio.isSelected());
				refButton9.setEnabled(!localRadio.isSelected());
				gitBranch.setEditable(!localRadio.isSelected());
				startDate.setEditable(!localRadio.isSelected());
				endDate.setEditable(!localRadio.isSelected());
				interval.setEditable(!localRadio.isSelected());
				localtargetDir.setEditable(localRadio.isSelected());
				checkoutCMD.setEditable(!localRadio.isSelected());
				checkoutDir.setEditable(!localRadio.isSelected());
				oldVersion.setEditable(!localRadio.isSelected());
				newVersion.setEditable(!localRadio.isSelected());
				refButton1.setEnabled(!localRadio.isSelected());
				refButton2.setEnabled(!localRadio.isSelected());
			}
		});

		//自動チェックアウトボタン設定
//		autoRadio.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				gitURL.setEditable(!autoRadio.isSelected());
//				gitCloneDir.setEditable(!autoRadio.isSelected());
//				refButton9.setEnabled(!autoRadio.isSelected());
//				startDate.setEditable(!autoRadio.isSelected());
//				endDate.setEditable(!autoRadio.isSelected());
//				interval.setEditable(!autoRadio.isSelected());
//				localtargetDir.setEditable(!autoRadio.isSelected());
//				checkoutCMD.setEditable(autoRadio.isSelected());
//				checkoutDir.setEditable(autoRadio.isSelected());
//				oldVersion.setEditable(!autoRadio.isSelected());
//				newVersion.setEditable(!autoRadio.isSelected());
//				refButton1.setEnabled(!autoRadio.isSelected());
//				refButton2.setEnabled(!autoRadio.isSelected());
//			}
//		});

		//手動チェックアウトボタン設定
//		manualRadio.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				gitURL.setEditable(!manualRadio.isSelected());
//				gitCloneDir.setEditable(!manualRadio.isSelected());
//				refButton9.setEnabled(!manualRadio.isSelected());
//				startDate.setEditable(!manualRadio.isSelected());
//				endDate.setEditable(!manualRadio.isSelected());
//				interval.setEditable(!manualRadio.isSelected());
//				localtargetDir.setEditable(!manualRadio.isSelected());
//				checkoutCMD.setEditable(!manualRadio.isSelected());
//				checkoutDir.setEditable(!manualRadio.isSelected());
//				oldVersion.setEditable(manualRadio.isSelected());
//				newVersion.setEditable(manualRadio.isSelected());
//				refButton1.setEnabled(manualRadio.isSelected());
//				refButton2.setEnabled(manualRadio.isSelected());
//			}
//		});

		//トークン閾値チェックアウトボタン設定
		token.setPreferredSize(new Dimension(40,20));
		tokenThreshold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				token.setEnabled(tokenThreshold.isSelected());
			}
		});

		//RNRチェックアウトボタン設定
		//_rnr.setPreferredSize(new Dimension(40,20));
		//_rnrFilter.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		_rnr.setEnabled(_rnrFilter.isSelected());
		//	}
		//});

		//ウェブチェックボックス設定
		webCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				html.setEditable(webCheck.isSelected());
				refButton3.setEnabled(webCheck.isSelected());
			}
		});

		//CSVチェックボックス設定
		csvCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				csv.setEditable(csvCheck.isSelected());
				refButton4.setEnabled(csvCheck.isSelected());
			}
		});

//		//電子メールチェックボックス設定
//		mailCheck.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				text.setEditable(mailCheck.isSelected());
//				accountFile.setEditable(mailCheck.isSelected());
//				keyFile.setEditable(mailCheck.isSelected());
//				host.setEditable(mailCheck.isSelected());
//				port.setEditable(mailCheck.isSelected());
//				from.setEditable(mailCheck.isSelected());
//				to1.setEditable(mailCheck.isSelected());
//				to2.setEditable(mailCheck.isSelected());
//				to3.setEditable(mailCheck.isSelected());
//				ssl.setEnabled(mailCheck.isSelected());
//				refButton5.setEnabled(mailCheck.isSelected());
//				refButton6.setEnabled(mailCheck.isSelected());
//				refButton7.setEnabled(mailCheck.isSelected());
//			}
//		});

		// ツールコンボボックス設定
		tool.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String toolName = (String)e.getItem();
				if(toolName.equals(CCFX_NAME)) {
					changeLangComboBox(CCFX_NAME);
//				}else if(toolName.equals(CD_NAME)) {
//					changeLangComboBox(CD_NAME);
				}else if(toolName.equals(CCV_NAME)) {
					changeLangComboBox(CCV_NAME);
				} else if (toolName.equals(SCC_NAME)) {
					changeLangComboBox(SCC_NAME);
				} else {
					changeLangComboBox("false");
				}
			}
		});

		//フレーム初期化
		setTitle("設定ツール");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		/*
		 * 基本設定パネル
		 */
		baseSettingPanel.setBorder(BorderFactory.createTitledBorder("基本設定"));
		baseSettingPanel.setLayout(new GridBagLayout());
//		baseSettingPanel.setLayout(new GridBagLayout());

		c.gridy=0; c.gridx=0; baseSettingPanel.add(new JLabel("ユーザ名:"),c);
		c.gridy=0; c.gridx=1; baseSettingPanel.add(userId,c);
		userId.setText("guest");

		c.gridy=1; c.gridx=0; baseSettingPanel.add(new JLabel("プロジェクト名:"),c);
		c.gridy=1; c.gridx=1; baseSettingPanel.add(projectName,c);

		c.gridy=2; c.gridx=0; baseSettingPanel.add(new JLabel("分析名:"),c);
		c.gridy=2; c.gridx=1; baseSettingPanel.add(analysisName,c);


		c.gridy=3; c.gridx=0; baseSettingPanel.add(new JLabel("ツール:"),c);
		c.gridy=3; c.gridx=1; baseSettingPanel.add(tool,c);

		c.gridy=4; c.gridx=0; baseSettingPanel.add(new JLabel("言語:"),c);
		c.gridy=4; c.gridx=1; baseSettingPanel.add(lang,c);

		c.gridy=5; c.gridx=0; baseSettingPanel.add(setWorkDir,c);
		c.gridy=5; c.gridx=1; baseSettingPanel.add(workDirName,c);
		c.gridy=5; c.gridx=2; baseSettingPanel.add(refButton0,c);

		c.gridy=6; c.gridx=0; baseSettingPanel.add(gitRadio,c);

		c.gridy=7; c.gridx=0; baseSettingPanel.add(new JLabel("Git URL(e.g https://CCEvovis.git):"),c);
		c.gridy=7; c.gridx=1; baseSettingPanel.add(gitURL,c);

		c.gridy=8; c.gridx=0; baseSettingPanel.add(new JLabel("Gitブランチ名:"),c);
		c.gridy=8; c.gridx=1; baseSettingPanel.add(gitBranch,c);

		c.gridy=9; c.gridx=0; baseSettingPanel.add(new JLabel("クローン先デイレクトリ :"),c);
		c.gridy=9; c.gridx=1; baseSettingPanel.add(gitCloneDir,c);
		c.gridx=9; c.gridx=2; baseSettingPanel.add(refButton9,c);

		c.gridy=10; c.gridx=0; baseSettingPanel.add(new JLabel("分析開始日(e.g 2019/10/01 -> 20191001):"),c);
		c.gridy=10; c.gridx=1; baseSettingPanel.add(startDate,c);

		c.gridy=11; c.gridx=0; baseSettingPanel.add(new JLabel("分析終了日:"),c);
		c.gridy=11; c.gridx=1; baseSettingPanel.add(endDate,c);

		c.gridy=12; c.gridx=0; baseSettingPanel.add(new JLabel("分析間隔:"),c);
		c.gridy=12; c.gridx=1; baseSettingPanel.add(interval,c);


		c.gridy=13; c.gridx=0; baseSettingPanel.add(localRadio,c);

		c.gridy=14; c.gridx=0; baseSettingPanel.add(new JLabel("分析対象ローカルディレクトリ :"),c);
		c.gridy=14; c.gridx=1; baseSettingPanel.add(localtargetDir,c);
		c.gridx=14; c.gridx=2; baseSettingPanel.add(refButton10,c);

//		c.gridy=12; c.gridx=0; baseSettingPanel.add(autoRadio,c);

//		c.gridy=13; c.gridx=0; baseSettingPanel.add(new JLabel("チェックアウトコマンド:"),c);
//		c.gridy=13; c.gridx=1; baseSettingPanel.add(checkoutCMD,c);
//
//		c.gridy=14; c.gridx=0; baseSettingPanel.add(new JLabel("チェックアウトディレクトリ:"),c);
//		c.gridy=14; c.gridx=1; baseSettingPanel.add(checkoutDir,c);
//		c.gridy=14; c.gridx=2; baseSettingPanel.add(refButton8,c);
//
////		c.gridy=15; c.gridx=0; baseSettingPanel.add(manualRadio,c);
//
//		c.gridy=16; c.gridx=0; baseSettingPanel.add(new JLabel("最新バージョンディレクトリ:"),c);
//		c.gridy=16; c.gridx=1; baseSettingPanel.add(newVersion,c);
//		c.gridy=16; c.gridx=2; baseSettingPanel.add(refButton1,c);
//
//		c.gridy=17; c.gridx=0; baseSettingPanel.add(new JLabel("旧バージョンディレクトリ:"),c);
//		c.gridy=17; c.gridx=1; baseSettingPanel.add(oldVersion,c);
//		c.gridy=17; c.gridx=2; baseSettingPanel.add(refButton2,c);

		c.gridy=18; c.gridx=0; baseSettingPanel.add(tokenThreshold,c);
		c.gridy=18; c.gridx=1; baseSettingPanel.add(token,c);

		c.gridy=19; c.gridx=0; baseSettingPanel.add(overLappingFilter,c);

		/*
		 * クローン設定パネル
		 */
		//cloneSettingPanel.setBorder(new TitledBorder("コードクローン設定"));
		//cloneSettingPanel.setLayout((new GridBagLayout()));
		//c.gridx=0;c.gridy=0; cloneSettingPanel.add(tokenThreshold,c);
		//c.gridx=0;c.gridy=1; _cloneSettingPanel.add(_rnrFilter,c);
		//c.gridx=0;c.gridy=1; cloneSettingPanel.add(overLappingFilter,c);
		//c.gridx=1;c.gridy=0; cloneSettingPanel.add(token,c);
		//c.gridx=1;c.gridy=1; _cloneSettingPanel.add(_rnr,c);


		/*
		 * 選択パネル
		 */
		selectPanel.setBorder(BorderFactory.createTitledBorder("出力選択"));
//		selectPanel.setLayout(new GridBagLayout());
		selectPanel.setLayout(new GridBagLayout());
		c.gridy=0; c.gridx=0; selectPanel.add(webCheck,c);
		c.gridy=1; c.gridx=0; selectPanel.add(webSettingPanel,c);
		c.gridy=2; c.gridx=0; selectPanel.add(csvCheck,c);
		c.gridy=3; c.gridx=0; selectPanel.add(csvSettingPanel,c);
//		c.gridy=4; c.gridx=0; selectPanel.add(mailCheck,c);
//		c.gridy=5; c.gridx=0; selectPanel.add(mailSettingPanel,c);


		/*
		 * ウェブ設定ディレクトリ
		 */
		webSettingPanel.setBorder(BorderFactory.createTitledBorder("ウェブインタフェース設定"));
		webSettingPanel.setLayout(new GridBagLayout());
		c.gridx=0; c.gridy=0; webSettingPanel.add(new JLabel("HTML出力先ディレクトリ："),c);
		c.gridx=1; c.gridy=0; webSettingPanel.add(html,c);
		c.gridx=2; c.gridy=0; webSettingPanel.add(refButton3,c);

		/*
		 * CSV設定ディレクトリ
		 */
		csvSettingPanel.setBorder(BorderFactory.createTitledBorder("CSV出力ファイル設定"));
		csvSettingPanel.setLayout(new GridBagLayout());
		c.gridx=0; c.gridy=0; csvSettingPanel.add(new JLabel("CSV出力先ディレクトリ："),c);
		c.gridx=1; c.gridy=0; csvSettingPanel.add(csv,c);
		c.gridx=2; c.gridy=0; csvSettingPanel.add(refButton4,c);

		/*
		 * 電子メール設定パネル
		 */
//		mailSettingPanel.setBorder(new TitledBorder("電子メール設定"));
//		mailSettingPanel.setLayout((new GridBagLayout()));
//		c.gridx=0; c.gridy=0; mailSettingPanel.add(new JLabel("テキスト出力先ディレクトリ：",SwingConstants.LEFT),c);
//		c.gridx=0; c.gridy=1; mailSettingPanel.add(new JLabel("アカウントファイル：",SwingConstants.LEFT),c);
//		c.gridx=0; c.gridy=2; mailSettingPanel.add(new JLabel("キーファイル："),c);
//		c.gridx=0; c.gridy=3; mailSettingPanel.add(new JLabel("SMTPサーバホスト名："),c);
//		c.gridx=0; c.gridy=4; mailSettingPanel.add(new JLabel("ポート番号："),c);
//		c.gridx=0; c.gridy=5; mailSettingPanel.add(new JLabel("メール送信元："),c);
//		c.gridx=0; c.gridy=6; mailSettingPanel.add(new JLabel("メール送信先1："),c);
//		c.gridx=0; c.gridy=7; mailSettingPanel.add(new JLabel("メール送信先2："),c);
//		c.gridx=0; c.gridy=8; mailSettingPanel.add(new JLabel("メール送信先3："),c);
//		c.gridx=0; c.gridy=9; mailSettingPanel.add(new JLabel("SSL："),c);
//		c.gridx=1; c.gridy=0; mailSettingPanel.add(text,c);
//		c.gridx=1; c.gridy=1; mailSettingPanel.add(accountFile,c);
//		c.gridx=1; c.gridy=2; mailSettingPanel.add(keyFile,c);
//		c.gridx=1; c.gridy=3; mailSettingPanel.add(host,c);
//		c.gridx=1; c.gridy=4; mailSettingPanel.add(port,c);
//		c.gridx=1; c.gridy=5; mailSettingPanel.add(from,c);
//		c.gridx=1; c.gridy=6; mailSettingPanel.add(to1,c);
//		c.gridx=1; c.gridy=7; mailSettingPanel.add(to2,c);
//		c.gridx=1; c.gridy=8; mailSettingPanel.add(to3,c);
//		c.gridx=1; c.gridy=9; mailSettingPanel.add(ssl,c);
//		c.gridx=2; c.gridy=0; mailSettingPanel.add(refButton5,c);
//		c.gridx=2; c.gridy=1; mailSettingPanel.add(refButton6,c);
//		c.gridx=2; c.gridy=2; mailSettingPanel.add(refButton7,c);

		/*
		 * 設定終了パネル
		 */
		endSettingPanel.setLayout((new GridBagLayout()));
		c.gridx=0; c.gridy=0; endSettingPanel.add(new JLabel("設定ファイル名：",SwingConstants.LEFT),c);
		c.gridx=1; c.gridy=0; endSettingPanel.add(settingFileName,c);
		c.gridx=2; c.gridy=0; endSettingPanel.add(refButton8,c);

		/*
		 * 実行設定ファイルの指定
		 */
		inputSettingPanel.setLayout((new GridBagLayout()));
		c.gridx=0;c.gridy=0; inputSettingPanel.add(new JLabel("実行設定ファイル：",SwingConstants.LEFT),c);
		c.gridx=1;c.gridy=0; inputSettingPanel.add(inputsettingFileName,c);
		c.gridx=2;c.gridy=0; inputSettingPanel.add(refButton11,c);
		//フレームに追加
		mainPanel.setLayout((new GridBagLayout()));
		c.anchor = GridBagConstraints.WEST;
		c.gridx=0;c.gridy=0; mainPanel.add(baseSettingPanel,c);
		c.gridx=0;c.gridy=1; mainPanel.add(selectPanel,c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx=0;c.gridy=2; mainPanel.add(endSettingPanel,c);
		c.gridx=0;c.gridy=3; mainPanel.add(generateButton,c);
		c.gridx=0;c.gridy=5; mainPanel.add(message,c);
		c.gridx=0;c.gridy=6; mainPanel.add(inputSettingPanel,c);
		c.gridx=0;c.gridy=7; mainPanel.add(runButton,c);
	    Container contentPane = getContentPane();
	    contentPane.add(mainPanel, BorderLayout.WEST);
		pack();
	    setVisible(true);

	    /*
	     * 設定完了時
	     */
		generateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				generate_config();
			}

		});

	    /*
	     * 設定完了and実行
	     */
		runButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			//	generate_config();
				run_ccevovis(inputsettingFileName.getText());
			}

		});
	}

	private void run_ccevovis(String filename) {
        FileWriter file = null;
		try {
			file = new FileWriter("ccm.bat");
        PrintWriter pw = new PrintWriter(new BufferedWriter(file));

        pw.println("cd /d %~dp0\r\n" +
        		"analyze.jar " + filename);

        pw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		String bat_path = "cmd.exe /c start ccm.bat";

	    try {
	      Runtime.getRuntime().exec(bat_path);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }


	}


	private void generate_config() {

		FileGenerator fg = new FileGenerator();
		if(!fg.init(settingFileName.getText())){ message.setText("設定ファイル名に間違いがあります．"); return;}

		/*
		 * 基本設定
		 */
		if(!fg.write("PROJECT_NAME:",projectName.getText())){	message.setText("プロジェクト名が不明です．"); return;	}

		if(!fg.write("ANALYSIS_NAME:",analysisName.getText())){	message.setText("分析名が不明です．"); return;	}
		fg.write("TOOL:",tool.getSelectedItem().toString());
		fg.write("LANGUAGE:",lang.getSelectedItem().toString());

		//作業ディレクトリの設定する場合
		if(setWorkDir.isSelected()){
			if(!fg.write("WORK_DIR:",workDirName.getText()));
		}

		//Gitから直接cloneして分析を行う場合
		if(gitRadio.isSelected()){
			fg.write("GIT_DIRECT:","true");
			if(!fg.write("GIT_URL:",gitURL.getText())){ message.setText("GitURLが不明です．"); return; }
			if(!fg.write("GIT_BRANCH:",gitBranch.getText())){ message.setText("GitBranchLが不明です．"); return; }
			if(!fg.write("TARGET_DIR:",gitCloneDir.getText())){ message.setText("Gitクローン先が不明です．"); return; }
			if(!fg.write("START_DATE:",startDate.getText())){ message.setText("分析開始日が不明です．"); return; }
			if(!fg.write("END_DATE:",endDate.getText())){ message.setText("分析終了日が不明です．"); return; }
			if(!fg.write("INTERVAL:",interval.getText())){ message.setText("分析間隔が不明です．"); return; }

		}

		//ローカルを対象とした分析を行う場合
		if(localRadio.isSelected()){
			fg.write("LOCAL_TARGET:","true");
			if(!fg.write("TARGET_DIR:",localtargetDir.getText())){ message.setText("チェックアウトコマンドが不明です．"); return; }
		}

		//自動の場合
//		if(autoRadio.isSelected()){
//			fg.write("CHECKOUT:","AUTO");
//			if(!fg.write("CHECKOUT_CMD:",checkoutCMD.getText())){ message.setText("チェックアウトコマンドが不明です．"); return; }
//			if(!fg.write("CHECKOUT_DIR:",checkoutDir.getText())){ message.setText("チェックアウトディレクトリが不明です．"); return; }
//		}

		//手動の場合
//		if(manualRadio.isSelected()){
//			fg.write("CHECKOUT:","MANUAL");
//			if(!fg.write("NEW_VERSION:",newVersion.getText())){ message.setText("最新バージョンディレクトリが不明です．");	 return; }
//			if(!fg.write("OLD_VERSION:",oldVersion.getText())){ message.setText("旧バージョンディレクトリが不明です．"); return; }
//		}

		/*
		 * コードクローン設定
		 */
		//トークン設定
		if(tokenThreshold.isSelected()) fg.write("TOKEN:",token.getValue().toString());
		else fg.write("TOKEN:","50");

		//RNR設定
		//if(_rnrFilter.isSelected()) fg.write("RNR:",_rnr.getValue().toString());
		//else fg.write("RNR:","0");

		//オーバラッピングフィルタリング設定
		if(overLappingFilter.isSelected()) fg.write("OVERLAPPING:","true");
		else fg.write("OVERLAPPING:","false");

		/*
		 * ウェブインタフェース設定
		 */
		if(webCheck.isSelected()){
			fg.write("WEB:","true");
			if(!fg.write("HTML_DIR:",html.getText())){	message.setText("HTML出力先ディレクトリ名が不明です．");	return;	}
		}else{
			fg.write("WEB:","false");
		}

		/*
		 * CSV設定
		 */
		if(csvCheck.isSelected()){
			fg.write("CSV:","true");
			if(!fg.write("CSV_DIR:",csv.getText())){	message.setText("CSV出力先ディレクトリ名が不明です．");	return;	}
		}else{
			fg.write("CSV:","false");
		}
		fg.write("USER_ID:",userId.getText());

		/*
		 * 電子メール設定
		 */
//		if(mailCheck.isSelected()){
//			fg.write("MAIL:","true");
//			if(!fg.write("TEXT_DIR:",text.getText())){ message.setText("テキスト出力先ディレクトリが不明です．"); return; }
//			if(!fg.write("ACCOUNT_FILE:",accountFile.getText())){ message.setText("アカウントファイルが不明です．"); return; }
//			if(!fg.write("KEY_FILE:",keyFile.getText())){ message.setText("キーファイルが不明です．"); return; }
//			if(!fg.write("HOST:",host.getText())){ message.setText("SMTPサーバホスト名が不明です．");	 return; }
//			if(!fg.write("PORT:",port.getText())){ message.setText("ポート番号が不明です．"); return; }
//			if(!fg.write("FROM:",from.getText())){ message.setText("メール送信元が不明です．"); return;	}
//			if(!fg.write("TO1:",to1.getText())){ message.setText("メール送信先が不明です．"); return; }
//			if(!fg.write("TO2:",to2.getText())) fg.write("TO2:","NULL");
//			if(!fg.write("TO3:",to3.getText())){ fg.write("TO3:","NULL");}
//			fg.write("SSL:",ssl.getSelectedItem().toString());
//		}else{
//			fg.write("MAIL:","false");
//		}

		message.setText("設定ファイル生成成功");
		fg.end();
		inputsettingFileName.setText(settingFileName.getText());

	}
}
