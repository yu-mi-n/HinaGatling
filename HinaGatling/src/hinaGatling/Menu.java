package hinaGatling;

import java.util.List;
import java.util.Scanner;

public class Menu {
	private final Scanner scan;
	private TemplateManager templateMana;
	private TemplateEdit templateEdit;
	private ProjectBuilder projectBuild;
	private CreateDefaultTemp createDefault;

	// コンストラクタ
	Menu() {
		this.scan = new Scanner(System.in);
		this.templateMana = new TemplateManager();
		this.templateEdit = new TemplateEdit();
		this.projectBuild = new ProjectBuilder();
		this.createDefault = new CreateDefaultTemp();
	}

	// メソッド
	public void start() {
		System.out.println(">> HinaGatlingへようこそ <<");
		System.out.println("");

		boolean roop = true;

		while (roop) {
			System.out.println("=================================");
			System.out.println("      HinaGatling Main Menu      ");
			System.out.println("=================================");
			System.out.println("1: プロジェクト構築");
			System.out.println("2: テンプレート登録");
			System.out.println("3: テンプレート一覧表示");
			System.out.println("4: テンプレート更新");
			System.out.println("5: テンプレート削除");
			System.out.println("0: 終了");
			System.out.print("実行する番号を入力してください→");

			String input = scan.nextLine();

			switch (input) {
			case "1":
				execute();
				break;
			case "2":
				create();
				break;
			case "3":
				list();
				break;
			case "4":
				update();
				break;
			case "5":
				delete();
				break;
			case "0":
				System.out.println("HinaGatlingを終了します。お疲れ様でした。");
				roop = false;
				break;
			default:
				System.out.println("【Error】正しい番号（0〜5）を入力してください。");
			}
			System.out.println("");
		}
		scan.close();
	}

	public void execute() {
		System.out.println(">> プロジェクト構築（実行）を開始します");

		List<Template> templates = templateMana.loadAll();
		if (templates.isEmpty()) {
			System.out.println("登録されているテンプレートがありません。「2: 登録」から作成してください");
			return;
		}

		Template selectedTemplate = templateEdit.selectTemplate(templates, scan);
		if (selectedTemplate == null) {
			return;
		}

		System.out.print("プロジェクト名（フォルダ名）を入力してください→ ");
		String projectName = scan.nextLine();
		if (projectName.trim().isEmpty()) {
			System.out.println("【Error】プロジェクト名は必須です。処理を中断します。");
			return;
		}

		System.out.println("作成先の絶対パスを入力してください。");
		System.out.print("（何も入力せずにEnterを押すと「デスクトップ」に作成されます）→ ");
		String targetPath = scan.nextLine();

		System.out.println("GitHubのリポジトリのURL/SSHを入力してください（例: git@github.com:username/repo.git）");
		System.out.print("（後で手動設定する場合はそのままEnter）→ ");
		String remoteUrl = scan.nextLine();

		projectBuild.build(selectedTemplate, projectName, targetPath, remoteUrl);
	}

	public void create() {
		System.out.println(">> テンプレート新規登録を開始します");

		System.out.print("テンプレート名（例: Django標準テンプレート）→ ");
		String title = scan.nextLine();

		// デフォルトテンプレート生成する？
		System.out.print("「Djangoテンプレート(render用)」のデフォルトテンプレートを自動生成しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			Template template = createDefault.createForDjango(title);

			templateMana.save(template);
			System.out.println("-> Django用(render)デフォルトテンプレート「" + title + "」を自動生成し、保存しました");
			return;
		}

		System.out.print("フレームワーク（例: django, flask）→ ");
		String framework = scan.nextLine();

		Template template = new Template(title, framework);

		// セットアップコマンドの取得
		System.out.println("構築のためのセットアップコマンドを順番に入力してください");
		System.out.println("（例: python3 -m venv venv / venv/bin/pip install django など(＊入力なしEnterで終了)）");
		while (true) {
			System.out.print("コマンド → ");
			String command = scan.nextLine();
			if (command.trim().isEmpty()) {
				break;
			}
			template.getSetupCommand().add(command);
		}

		// 独自設定ファイル登録
		System.out.print("独自設定ファイル（render.yamlや.envなど）を登録しますか？ (y/N)→ ");
		templateEdit.collectBlueprints(template, scan);

		// 置き換え文字の取得
		System.out.print("既存ファイルの書き換えルールを登録しますか？ (y/N)→ ");
		templateEdit.collectModifiers(template, scan);

		templateMana.save(template);
		System.out.println("テンプレート「" + title + "」の登録・JSONへ保存完了");
	}

	public void list() {
		System.out.println(">> 登録済みテンプレート一覧:");
		List<Template> templates = templateMana.loadAll();

		if (templates.isEmpty()) {
			System.out.println("登録されているテンプレートはありません。");
			return;
		}

		for (Template template : templates) {
			System.out.println("--------------------------------------------------");
			System.out.println("ID         : " + template.getId());
			System.out.println("タイトル   : " + template.getTitle());
			System.out.println("フレームワーク       : " + template.getFramework());
			System.out.println("コマンド数 : " + template.getSetupCommand().size() + " 個");
		}
		System.out.println("--------------------------------------------------");
	}

	public void update() {
		System.out.println(">> テンプレート更新を開始します");

		List<Template> templates = templateMana.loadAll();
		if (templates.isEmpty()) {
			System.out.println("登録されているテンプレートがありません。「2: 登録」から作成してください");
			return;
		}

		Template template = templateEdit.selectTemplate(templates, scan);
		if (template == null) {
			return;
		}

		System.out.println("※変更しない項目は、何も入力せずにそのままEnterを押してください。");

		System.out.print("新しいタイトル [" + template.getTitle() + "]→ ");
		String newTitle = scan.nextLine();
		if (!newTitle.trim().isEmpty()) {
			template.setTitle(newTitle);
		}

		System.out.print("新しいフレームワーク [" + template.getFramework() + "]→ ");
		String newFramework = scan.nextLine();
		if (!newFramework.trim().isEmpty()) {
			template.setFramework(newFramework);
		}

		System.out.print("セットアップコマンドを再登録しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			templateEdit.updateSetupCommands(template, scan);
		}

		System.out.print("独自設定ファイル(Blueprint)を確認・更新しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			templateEdit.updateBlueprints(template, scan);
		}

		System.out.print("ファイル内容書き換えルールを確認・更新しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			templateEdit.updateModifiers(template, scan);
		}

		templateMana.save(template);
		System.out.println("テンプレートの更新が完了しました。");
	}

	public void delete() {
		System.out.println(">> テンプレート削除を開始します");

		List<Template> templates = templateMana.loadAll();
		if (templates.isEmpty()) {
			System.out.println("登録されているテンプレートがありません。「2: 登録」から作成してください");
			return;
		}

		Template template = templateEdit.selectTemplate(templates, scan);
		if (template == null) {
			return;
		}

		System.out.print("本当に「" + template.getTitle() + "」を削除してもよろしいですか？ (y/N): ");
		if (scan.nextLine().equals("y")) {
			templateMana.delete(template.getId());
		} else {
			System.out.println("削除をキャンセルしました。");
		}
	}
}