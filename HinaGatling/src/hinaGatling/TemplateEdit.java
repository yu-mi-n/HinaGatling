package hinaGatling;

import java.util.List;
import java.util.Scanner;

public class TemplateEdit {
	public Template selectTemplate(List<Template> templates, Scanner scan) {
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

	// 独自設定ファイルの新規登録
	public void collectBlueprints(Template template, Scanner scan) {
		if (scan.nextLine().equals("y")) {
			template.getBlueprintList().clear();

			System.out.println("独自設定ファイルを順番に入力してください。（未入力Enterで終了）");
			while (true) {
				System.out.print("ファイル名（例: .env, render.yaml）: ");
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
	public void updateBlueprints(Template template, Scanner scan) {
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

	public void collectModifiers(Template template, Scanner scan) {
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

	public void updateSetupCommands(Template template, Scanner scan) {
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

	public void updateModifiers(Template template, Scanner scan) {
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
}
