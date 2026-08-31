package hinaGatling;

import java.util.List;
import java.util.Scanner;

public class Menu {
	private final Scanner scan;
	private TemplateManager templateMana;
	private ProjectBuilder projectBuild;
//	private Deployer deployer;

	// コンストラクタ
	Menu() {
		this.scan = new Scanner(System.in);
		this.templateMana = new TemplateManager();
		this.projectBuild = new ProjectBuilder();
		// this.deployer = new Deployer();
	}

	// メソッド
	public void start() {
		boolean running = true;

		System.out.println(">> HinaGatlingへようこそ <<");
		System.out.println("");

		while (running) {
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
				running = false;
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

		// 登録済みテンプレートの確認
		List<Template> templates = templateMana.loadAll();
		if (templates.isEmpty()) {
			System.out.println("登録されているテンプレートがありません。「2: 登録」から作成してください");
			return;
		}

		// テンプレートの選択
		Template selectedTemplate = selectTemplate();
		if (selectedTemplate == null) {
			return;
		}

		// プロジェクト名の入力
		System.out.print("プロジェクト名（フォルダ名）を入力してください→ ");
		String projectName = scan.nextLine();
		if (projectName.trim().isEmpty()) {
			System.out.println("【Error】プロジェクト名は必須です。処理を中断します。");
			return;
		}

		// 作成先のパスの入力
		System.out.println("作成先の絶対パスを入力してください。");
		System.out.print("（何も入力せずにEnterを押すと「デスクトップ」に作成されます）→ ");
		String targetPath = scan.nextLine();

		// リモートリポジトリURLの入力
		System.out.println("GitHubのリポジトリのURL/SSHを入力してください（例: git@github.com:username/repo.git）");
		System.out.print("（後で手動設定する場合はそのままEnter）→ ");
		String remoteUrl = scan.nextLine();

		projectBuild.build(selectedTemplate, projectName, targetPath, remoteUrl);
	}

	public void create() {
		System.out.println("\n>> テンプレート新規登録を開始します");

		System.out.print("テンプレート名（例: Django標準テンプレート）→ ");
		String title = scan.nextLine();

		System.out.print("フレームワーク（例: django, flask）→ ");
		String framework = scan.nextLine();

		Template template = new Template(title, framework);

		// セットアップコマンドの取得
		System.out.println("構築のためのセットアップコマンドを順番に入力してください");
		System.out.println("（例: python3 -m venv venv / venv/bin/pip install django など(＊入力なしEnterで終了)）");
		while (true) {
			System.out.print("コマンド → ");
			String cmd = scan.nextLine();
			if (cmd.trim().isEmpty()) {
				break;
			}
			template.getSetupCommand().add(cmd);
		}

		// 置き換え文字の取得
		System.out.print("既存ファイルの書き換えルールの確認・更新をしますか？ (y/N): ");
		collectModifiers(template);

		// デプロイ設定（DeployConfig）
		System.out.println("デプロイ設定を入力してください。");
		System.out.print("プラットフォーム（render / vercel / none）→ ");
		String target = scan.nextLine();

		if (target.equals("none") || target.trim().isEmpty()) {
			template.setDeployConfig(new DeployConfig("none", "", ""));
		} else {
			System.out.print("ビルドコマンド（例: pip install -r requirements.txt）→ ");
			String buildCommand = scan.nextLine();

			System.out.print("起動コマンド（例: gunicorn app.wsgi:application）→ ");
			String startCommand = scan.nextLine();

			DeployConfig deployConfig = new DeployConfig(target, buildCommand, startCommand);
			template.setDeployConfig(deployConfig);
		}

		System.out.println("*ファイル設計図（Blueprint）の登録は、更新機能から行ってください");

		// 保存
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
			System.out.println("技術       : " + template.getFramework());
			System.out.println("コマンド数 : " + template.getSetupCommand().size() + " 個");
		}
		System.out.println("--------------------------------------------------");
	}

	public void update() {
		System.out.println(">> テンプレート更新を開始します");

		Template template = selectTemplate();
		if (template == null) {
			return;
		}

		System.out.println("※変更しない項目は、何も入力せずにそのままEnterを押してください。");

		// タイトルの更新
		System.out.print("新しいタイトル [" + template.getTitle() + "]→ ");
		String newTitle = scan.nextLine();
		if (!newTitle.trim().isEmpty()) {
			template.setTitle(newTitle);
		}

		// フレームワークの更新
		System.out.print("新しいフレームワーク [" + template.getFramework() + "]→ ");
		String newFramework = scan.nextLine();
		if (!newFramework.trim().isEmpty()) {
			template.setFramework(newFramework);
		}

		// コマンドの更新
		System.out.print("セットアップコマンドを再登録しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			updateSetupCommands(template);
		}

		// modifierの更新
		System.out.print("ファイル内容書き換えルールを再登録しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			updateModifiers(template);
		}

		// 変更の保存
		templateMana.save(template);
		System.out.println("テンプレートの更新が完了しました。");
	}

	public void delete() {
		System.out.println(">> テンプレート削除を開始します");

		Template template = selectTemplate();
		if (template == null) {
			return;
		}

		System.out.print("本当に「" + template.getTitle() + "」を削除してもよろしいですか？ (y/N): ");
		String confirm = scan.nextLine();

		if (confirm.equalsIgnoreCase("y")) {
			templateMana.delete(template.getId());
		} else {
			System.out.println("削除をキャンセルしました。");
		}
	}

	// テンプレート一覧表示して、インデックスで選択させる
	private Template selectTemplate() {
		List<Template> templates = templateMana.loadAll();

		if (templates.isEmpty()) {
			System.out.println("登録されているテンプレートがありません。");
			return null;
		}

		System.out.println("【利用可能なテンプレート一覧】");
		for (int i = 0; i < templates.size(); i++) {
			Template t = templates.get(i);
			System.out.println((i + 1) + ": " + t.getTitle() + " (" + t.getFramework() + ")");
		}

		System.out.print("対象の番号を入力してください (0でキャンセル)→ ");
		String input = scan.nextLine();

		try {
			int index = Integer.parseInt(input);
			if (index == 0) {
				System.out.println("操作をキャンセルしました。");
				return null;
			}

			if (index > 0 && index <= templates.size()) {
				return templates.get(index - 1);
			} else {
				System.out.println("【ERROR】正しい番号を選択してください。");
				return null;
			}
		} catch (NumberFormatException e) {
			System.out.println("【ERROR】数字を入力してください。");
			return null;
		}
	}

	// Modifierの入力
	private void collectModifiers(Template template) {
		if (scan.nextLine().equals("y")) {
			template.getModifierList().clear();

			System.out.println("書き換え情報を順番に入力してください。（Enterで終了）");
			while (true) {
				System.out.print("対象ファイルのパス（例: config/settings.py）: ");
				String path = scan.nextLine();
				if (path.trim().isEmpty()) {
					break;
				}

				System.out.print("置換対象の元の文字列（例: STATIC_URL = 'static/'）: ");
				String targetText = scan.nextLine();

				System.out.println(
						"置換後の新しい文字列を入力してください。(例: STATIC_URL = 'static/'\\nSTATICFILES_DIRS = [BASE_DIR / 'static'])");
				System.out.print("（※改行を入れる場合は \\n と入力してください）: ");
				String replacementText = scan.nextLine();

				replacementText = replacementText.replace("\\n", "\n");

				template.getModifierList().add(new Modifier(path, targetText, replacementText));

				System.out.print("さらにルールを追加しますか？ (y/N): ");
				if (!scan.nextLine().equals("y")) {
					break;
				}
			}
		}
	}

	private void updateSetupCommands(Template template) {
		List<String> commands = template.getSetupCommand();

		while (true) {
			System.out.println("【現在のセットアップコマンド一覧】");
			if (commands.isEmpty()) {
				System.out.println("（登録されているコマンドはありません）");
			} else {
				for (int i = 0; i < commands.size(); i++) {
					System.out.println((i + 1) + ": " + commands.get(i));
				}
			}

			System.out.print("操作を選択してください (1〜" + commands.size() + "の番号: 修正, 0: 新規追加, −1: 順番入れ替え, -2: 終了): ");
			String input = scan.nextLine();

			try {
				int choice = Integer.parseInt(input);
				if (choice == -2) {
					break;
				} else if (choice == 0) {
					// 新規追加
					System.out.print("追加するコマンドを入力してください: ");
					String newCommand = scan.nextLine();
					if (!newCommand.trim().isEmpty()) {
						commands.add(newCommand);
						System.out.println("-> コマンドを追加しました。");
					}

				} else if (choice == -1) {
					// 順番入れ替え
					if (commands.size() < 2) {
						System.out.println("【ERROR】コマンドが2つ以上ありません。");
						continue;
					}

					System.out.print("移動したいコマンドの番号 (1〜" + commands.size() + "): ");
					int fromIndex = Integer.parseInt(scan.nextLine()) - 1;

					System.out.print("移動先の番号 (1〜" + commands.size() + "): ");
					int toIndex = Integer.parseInt(scan.nextLine()) - 1;

					if (fromIndex >= 0 && fromIndex < commands.size() && toIndex >= 0 && toIndex < commands.size()) {
						// 入れ替え機能
						String targetCmd = commands.remove(fromIndex);
						commands.add(toIndex, targetCmd);
						System.out.println("-> コマンドの順番を入れ替えました。");
					} else {
						System.out.println("【ERROR】正しい番号を入力してください。");
					}

				} else if (choice > 0 && choice <= commands.size()) {
					// 部分修正機能
					String oldCommand = commands.get(choice - 1);
					System.out.println("※変更しない場合は何も入力せずにEnter");
					System.out.print("新しいコマンド [" + oldCommand + "]: ");

					String newCommand = scan.nextLine();
					if (!newCommand.trim().isEmpty()) {
						commands.set(choice - 1, newCommand);
						System.out.println("-> コマンド (番号 " + choice + ") を更新しました。");
					}

				} else {
					System.out.println("【ERROR】正しい番号を入力してください。");
				}
			} catch (NumberFormatException e) {
				System.out.println("【ERROR】数字を入力してください。");
			}
		}
	}

	// Modifierの更新の方
	private void updateModifiers(Template template) {
		List<Modifier> modifiers = template.getModifierList();

		while (true) {
			System.out.println("【現在の既存ファイル書き換えルール一覧】");
			if (modifiers.isEmpty()) {
				System.out.println("（登録されているルールはありません）");
			} else {
				for (int i = 0; i < modifiers.size(); i++) {
					Modifier modifier = modifiers.get(i);
					System.out.println((i + 1) + ": " + modifier.getPath());
					System.out.println("   [置換対象] " + modifier.getTargetText());
				}
			}

			// 操作の選択
			System.out.print("操作を選択してください (1〜" + modifiers.size() + "の番号: 修正, 0: 新規追加, -1: 終了): ");
			String input = scan.nextLine();

			try {
				int choice = Integer.parseInt(input);

				if (choice == -1) {
					break;
				} else if (choice == 0) {
					// 新規追加
					System.out.print("対象ファイルのパス: ");
					String path = scan.nextLine();
					if (path.trim().isEmpty())
						continue;

					System.out.print("置換対象の元の文字列: ");
					String targetText = scan.nextLine();

					System.out.print("置換後の新しい文字列 (改行は \\n): ");
					String replacementText = scan.nextLine();
					replacementText = replacementText.replace("\\n", "\n");

					modifiers.add(new Modifier(path, targetText, replacementText));
					System.out.println("-> ルールを新規追加しました。");

				} else if (choice > 0 && choice <= modifiers.size()) {
					Modifier modifier = modifiers.get(choice - 1);
					System.out.println("※変更しない項目は何も入力せずにEnter");

					System.out.print("新しいパス [" + modifier.getPath() + "]: ");
					String newPath = scan.nextLine();
					if (!newPath.trim().isEmpty()) {
						modifier.setPath(newPath);
					}

					System.out.print("新しい置換対象 [" + modifier.getTargetText() + "]: ");
					String newTarget = scan.nextLine();
					if (!newTarget.trim().isEmpty()) {
						modifier.setTargetText(newTarget);
					}

					System.out.print("新しい置換後文字列 (改行は \\n): ");
					String newReplacement = scan.nextLine();
					if (!newReplacement.trim().isEmpty()) {
						modifier.setReplacementText(newReplacement.replace("\\n", "\n"));
					}
					System.out.println("-> ルール (番号 " + choice + ") を更新しました。");

				} else {
					System.out.println("【ERROR】正しい番号を入力してください。");
				}
			} catch (NumberFormatException e) {
				System.out.println("【ERROR】数字を入力してください。");
			}
		}
	}
}
