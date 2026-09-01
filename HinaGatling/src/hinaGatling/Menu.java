package hinaGatling;

import java.util.List;
import java.util.Scanner;

public class Menu {
	private final Scanner scan;
	private TemplateManager templateMana;
	private ProjectBuilder projectBuild;

	// コンストラクタ
	Menu() {
		this.scan = new Scanner(System.in);
		this.templateMana = new TemplateManager();
		this.projectBuild = new ProjectBuilder();
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

		List<Template> templates = templateMana.loadAll();
		if (templates.isEmpty()) {
			System.out.println("登録されているテンプレートがありません。「2: 登録」から作成してください");
			return;
		}

		Template selectedTemplate = selectTemplate();
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
			createDefaultTemplate(title);
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
		collectBlueprints(template);

		// 置き換え文字の取得
		System.out.print("既存ファイルの書き換えルールを登録しますか？ (y/N)→ ");
		collectModifiers(template);

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
			updateSetupCommands(template);
		}

		// ★ Blueprintの部分更新サブメニューへの分岐
		System.out.print("独自設定ファイル(Blueprint)を確認・更新しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			updateBlueprints(template);
		}

		System.out.print("ファイル内容書き換えルールを確認・更新しますか？ (y/N)→ ");
		if (scan.nextLine().equals("y")) {
			updateModifiers(template);
		}

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
		if (scan.nextLine().equals("y")) {
			templateMana.delete(template.getId());
		} else {
			System.out.println("削除をキャンセルしました。");
		}
	}

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

	// 独自設定ファイルの新規登録メソッド
	private void collectBlueprints(Template template) {
		if (scan.nextLine().equals("y")) {
			template.getBlueprintList().clear();

			System.out.println("独自設定ファイルを順番に入力してください。（未入力Enterで終了）");
			while (true) {
				System.out.print("ファイルのパス（例: .env, render.yaml）: ");
				String path = scan.nextLine();
				if (path.trim().isEmpty()) {
					break;
				}

				System.out.println("ファイルの中身を入力してください");
				System.out.print("（※改行を入れる場合は \\n と入力してください）: ");
				String content = scan.nextLine();
				content = content.replace("\\n", "\n");

				System.out.print("このファイルは機密情報を含みますか？（yにすると.gitignoreに登録） (y/N): ");
				boolean isSecret = scan.nextLine().equals("y");

				template.getBlueprintList().add(new Blueprint(path, content, isSecret));

				System.out.print("さらにファイルを追加しますか？ (y/N): ");
				if (!(scan.nextLine().equals("y"))) {
					break;
				}
			}
		}
	}

	// 独自設定ファイルの部分更新メソッド
	private void updateBlueprints(Template template) {
		List<Blueprint> blueprints = template.getBlueprintList();

		while (true) {
			System.out.println("【現在の独自設定ファイル(Blueprint)一覧】");
			if (blueprints.isEmpty()) {
				System.out.println("（登録されているファイルはありません）");
			} else {
				for (int i = 0; i < blueprints.size(); i++) {
					Blueprint bp = blueprints.get(i);
					System.out.println((i + 1) + ": " + bp.getPath() + (bp.getIsSecret() ? " [機密]" : ""));
				}
			}

			System.out.print("操作を選択 (1〜" + blueprints.size() + "の番号: 修正, 0: 新規追加, -1: 終了): ");
			String input = scan.nextLine();

			try {
				int choice = Integer.parseInt(input);

				if (choice == -1) {
					break;
				} else if (choice == 0) {
					System.out.print("ファイルのパス: ");
					String path = scan.nextLine();
					if (path.trim().isEmpty())
						continue;

					System.out.print("ファイルの中身 (改行は \\n): ");
					String content = scan.nextLine();
					content = content.replace("\\n", "\n");

					System.out.print("機密情報を含みますか？ (y/N): ");
					String secretInput = scan.nextLine();
					boolean isSecret = scan.nextLine().equals("y");

					blueprints.add(new Blueprint(path, content, isSecret));
					System.out.println("-> ファイルを新規追加しました。");

				} else if (choice > 0 && choice <= blueprints.size()) {
					Blueprint bp = blueprints.get(choice - 1);
					System.out.println("※変更しない項目は何も入力せずにEnter");

					System.out.print("新しいパス [" + bp.getPath() + "]: ");
					String newPath = scan.nextLine();
					if (!newPath.trim().isEmpty()) {
						bp.setPath(newPath);
					}

					System.out.print("新しい中身 (改行は \\n): ");
					String newContent = scan.nextLine();
					if (!newContent.trim().isEmpty()) {
						bp.setContent(newContent.replace("\\n", "\n"));
					}

					System.out.print("機密情報ですか？ 現在:[" + (bp.getIsSecret() ? "y" : "N") + "] (y/N): ");
					String newSecret = scan.nextLine();
					if (!newSecret.trim().isEmpty()) {
						bp.setIsSecret(newSecret.equals("y"));
					}
					System.out.println("-> ファイル (番号 " + choice + ") を更新しました。");

				} else {
					System.out.println("【ERROR】正しい番号を入力してください。");
				}
			} catch (NumberFormatException e) {
				System.out.println("【ERROR】数字を入力してください。");
			}
		}
	}

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
				String continueInput = scan.nextLine();
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

			System.out.print("操作を選択 (1〜" + commands.size() + "の番号: 修正, 0: 新規追加, −1: 順番入れ替え, -2: 終了): ");
			String input = scan.nextLine();

			try {
				int choice = Integer.parseInt(input);
				if (choice == -2) {
					break;
				} else if (choice == 0) {
					System.out.print("追加するコマンドを入力してください: ");
					String newCommand = scan.nextLine();
					if (!newCommand.trim().isEmpty()) {
						commands.add(newCommand);
						System.out.println("-> コマンドを追加しました。");
					}

				} else if (choice == -1) {
					if (commands.size() < 2) {
						System.out.println("【ERROR】コマンドが2つ以上ありません。");
						continue;
					}

					System.out.print("移動したいコマンドの番号 (1〜" + commands.size() + "): ");
					int fromIndex = Integer.parseInt(scan.nextLine()) - 1;

					System.out.print("移動先の番号 (1〜" + commands.size() + "): ");
					int toIndex = Integer.parseInt(scan.nextLine()) - 1;

					if (fromIndex >= 0 && fromIndex < commands.size() && toIndex >= 0 && toIndex < commands.size()) {
						String targetCmd = commands.remove(fromIndex);
						commands.add(toIndex, targetCmd);
						System.out.println("-> コマンドの順番を入れ替えました。");
					} else {
						System.out.println("【ERROR】正しい番号を入力してください。");
					}

				} else if (choice > 0 && choice <= commands.size()) {
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

			System.out.print("操作を選択 (1〜" + modifiers.size() + "の番号: 修正, 0: 新規追加, -1: 終了): ");
			String input = scan.nextLine();

			try {
				int choice = Integer.parseInt(input);

				if (choice == -1) {
					break;
				} else if (choice == 0) {
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

	// django用(render)デフォルトテンプレート生成
	private void createDefaultTemplate(String title) {
		Template template = new Template(title, "django");

		// セットアップコマンド
		template.getSetupCommand().add("python3 -m venv venv");
		template.getSetupCommand().add("venv/bin/pip install --upgrade pip");
		template.getSetupCommand().add("venv/bin/pip install django gunicorn python-dotenv");
		template.getSetupCommand().add("venv/bin/django-admin startproject config .");
		template.getSetupCommand().add("venv/bin/pip freeze > requirements.txt");

		// 独自設定ファイル
		String gitignore = "venv/\n__pycache__/\n*.pyc\ndb.sqlite3\n.env\n.DS_Store";
		template.getBlueprintList().add(new Blueprint(".gitignore", gitignore, false));

		String env = "DEBUG=True\nSECRET_KEY=your_secret_key_here";
		template.getBlueprintList().add(new Blueprint(".env", env, true));

		String renderYaml = "services:\n" +
				"  - type: web\n" +
				"    name: django-app\n" +
				"    env: python\n" +
				"    buildCommand: \"pip install -r requirements.txt\"\n" +
				"    startCommand: \"gunicorn config.wsgi\"\n" +
				"    envVars:\n" +
				"      - key: PYTHON_VERSION\n" +
				"        value: 3.10.0";
		template.getBlueprintList().add(new Blueprint("render.yaml", renderYaml, false));

		// 既存ファイルの書き換えルール
		template.getModifierList()
				.add(new Modifier("config/settings.py", "TIME_ZONE = 'UTC'", "TIME_ZONE = 'Asia/Tokyo'"));
		template.getModifierList()
				.add(new Modifier("config/settings.py", "LANGUAGE_CODE = 'en-us'", "LANGUAGE_CODE = 'ja'"));
		template.getModifierList().add(new Modifier("config/settings.py", "STATIC_URL = 'static/'",
				"STATIC_URL = 'static/'\nSTATICFILES_DIRS = [BASE_DIR / 'static']"));
		template.getModifierList()
				.add(new Modifier("config/settings.py", "ALLOWED_HOSTS = []", "ALLOWED_HOSTS = ['*']"));

		// 保存処理
		templateMana.save(template);
		System.out.println("-> Django用(render)デフォルトテンプレート「" + title + "」を自動生成し、保存しました");
	}
}