package hinaGatling;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder {
	private File projectDir;

	public void build(Template template, String projectName, String path, String remoteUrl) {
		System.out.println("プロジェクト [" + projectName + "] の構築を開始します");

		// プロジェクト作成先のパス指定（未入力の場合はデスクトップ）
		if (path == null || path.trim().isEmpty()) {
			String homePath = System.getProperty("user.home");
			path = homePath + "/Desktop";
		}

		// プロジェクト用の新しいフォルダを作成
		this.projectDir = new File(path, projectName);

		// すでに同名のフォルダ・ファイルが存在する場合は中断
		if (this.projectDir.exists()) {
			System.err.println("【ERROR】指定された場所に、既に「" + projectName + "」が存在します。");
			System.err.println("構築処理を中止しました。");
			return;
		}

		if (!projectDir.exists()) {
			projectDir.mkdir();
		}

		System.out.println("作成場所: " + this.projectDir.getAbsolutePath());

		// コマンドの実行
		System.out.println("[Step1] セットアップコマンドを実行中...");
		boolean isSuccess = executeCommands(template.getSetupCommand());

		if (!isSuccess) {
			System.err.println(" 構築失敗: コマンド実行中にエラーが発生したため、処理を中断しました");
			System.out.println("  -> ロールバックを実行中: 中途半端なプロジェクトフォルダを削除します...");

			if (this.projectDir != null && this.projectDir.exists()) {
				deleteDirectory(this.projectDir);
			}

			System.out.println("  -> 削除が完了しました");
			return;
		}

		// ファイルの生成
		System.out.println("[Step2] 独自設定ファイルの生成中...");
		generate(template.getBlueprintList());

		// ファイル内容の一部書き換え
		System.out.println("[Step3] 既存ファイルの設定書き換えを実行中...");
		modify(template.getModifierList());

		// Gitとセキュリティ設定
		System.out.println("[Step4] セキュリティ設定とGit初期化...");
		setupGitAndSecurity(remoteUrl);

		System.out.println("構築完了: " + projectName);
	}

	public boolean executeCommands(List<String> cmds) {
		if (cmds == null || cmds.isEmpty()) {
			System.out.println("実行するコマンドがありません。");
			return true;
		}

		for (String cmd : cmds) {
			System.out.println("実行: " + cmd);
			try {
				// bashシェルにコマンドを渡す
				ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);

				// プロジェクトフォルダをカレントに
				pb.directory(this.projectDir);

				pb.inheritIO();

				Process process = pb.start();
				int exitCode = process.waitFor();

				if (exitCode != 0) {
					System.err.println("【Warning】コマンドがエラー終了しました。処理を中断します");
					return false;
				}
			} catch (Exception e) {
				System.err.println("コマンドの実行中に例外が発生しました: " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	public void generate(List<Blueprint> files) {
		if (files == null || files.isEmpty()) {
			System.out.println("生成する独自ファイルはありません。");
			return;
		}

		// .gitignore ファイルをさす
		File gitignoreFile = new File(this.projectDir, ".gitignore");

		for (Blueprint blueprint : files) {
			System.out.println("作成: " + blueprint.getPath());

			// this.projectDirを基準に、ファイルの保存場所を決定
			File targetFile = new File(this.projectDir, blueprint.getPath());

			try {
				// ファイルが配置されるフォルダがなければ自動作成
				File parentDir = targetFile.getParentFile();
				if (parentDir != null && !parentDir.exists()) {
					parentDir.mkdirs();
				}

				// 中身の取得
				String content = blueprint.getContent();

				// シークレットキー生成（{{AUTO_GENERATE_KEY}}があったら）
				if (content.contains("{{AUTO_GENERATE_KEY}}")) {
					content = content.replace("{{AUTO_GENERATE_KEY}}", genSkeyForDjango());
					System.out.println("  -> [Security] シークレットキーを動的生成しました");
				}

				// ファイルに書き込み
				try (java.io.FileWriter fw = new java.io.FileWriter(targetFile)) {
					fw.write(content);
				}

				// isSecretがtrueなら、.gitignore に追記
				if (blueprint.getIsSecret()) {
					try (java.io.FileWriter fw = new java.io.FileWriter(gitignoreFile, true)) {
						fw.write(blueprint.getPath() + "\n");
					}
					System.out.println("  -> [Security] 機密ファイルのため、.gitignore に登録しました");
				}

			} catch (Exception e) {
				System.err.println("ファイルの生成に失敗しました (" + blueprint.getPath() + "): " + e.getMessage());
			}
		}
	}

	public void setupGitAndSecurity(String remoteUrl) {
		// 基本的な .gitignore の自動生成
		File gitignoreFile = new File(this.projectDir, ".gitignore");
		List<String> ignoreContents = new ArrayList<String>();

		if (gitignoreFile.exists()) {
			try {
				ignoreContents = Files.readAllLines(gitignoreFile.toPath());
			} catch (Exception e) {
				System.err.println(".gitignoreの読み込みに失敗しました: " + e.getMessage());
			}
		}

		String[] requiredIgnores = {
				"venv/", "__pycache__/", "*.pyc", ".DS_Store", ".history", ".env", "*.env"
		};

		// 差分をチェックして追記
		try (java.io.FileWriter fw = new java.io.FileWriter(gitignoreFile, true)) {
			for (String rule : requiredIgnores) {
				// なかったら追記
				if (!ignoreContents.contains(rule)) {
					fw.write(rule + "\n");
					System.out.println("  -> .gitignore に必須ルールを追加: " + rule);
				}
			}
		} catch (Exception e) {
			System.err.println(".gitignoreの更新に失敗しました: " + e.getMessage());
		}

		// Gitコマンド
		List<String> gitCommands = new java.util.ArrayList<>();
		gitCommands.add("git init");
		gitCommands.add("git add .");
		gitCommands.add("git commit -m \"first commit by HinaGatling\"");

		if (remoteUrl != null && !remoteUrl.trim().isEmpty()) {
			gitCommands.add("git branch -M main");
			gitCommands.add("git remote add origin " + remoteUrl);
			gitCommands.add("git push -u origin main");
			System.out.println("  -> リモートリポジトリへのプッシュ設定が完了しました");
		}

		// コマンド実行
		System.out.println("  -> Gitリポジトリの初期化と初回コミットを実行します");
		boolean isSuccess = executeCommands(gitCommands);

		if (isSuccess) {
			System.out.println("  -> Gitのセットアップが完了しました");
		} else {
			System.err.println("【警告】Gitのセットアップに失敗しました。（MacにGitがインストールされていないか、権限エラーの可能性があります）");
		}
	}

	// 既存ファイルの一部を書き換え
	private void modify(List<Modifier> modifiers) {
		if (modifiers == null || modifiers.isEmpty()) {
			return;
		}

		for (Modifier modifier : modifiers) {
			// ファイルのパスを取得
			File targetFile = new File(this.projectDir, modifier.getPath());

			if (!targetFile.exists()) {
				System.err.println("  【Warning】書き換え対象のファイルが見つかりません: " + modifier.getPath());
				continue;
			}

			try {
				Path path = targetFile.toPath();
				// ファイルの中身を読み込み
				String content = Files.readString(path);

				// 指定された文字列を、新しい文字列に置換
				content = content.replace(modifier.getTargetText(), modifier.getReplacementText());

				// 置換した内容で上書き保存
				Files.writeString(path, content);

				System.out.println("  -> " + modifier.getPath() + " の内容を書き換えました");
			} catch (Exception e) {
				System.err.println("  【ERROR】ファイルの書き換えに失敗しました: " + e.getMessage());
			}
		}
	}

	// プロジェクトの構築がエラーった時、フォルダの中身ごと強制削除するメソッド
	private void deleteDirectory(File file) {
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				// ファイルの中身を削除
				for (File entry : entries) {
					deleteDirectory(entry);
				}
			}
		}
		// 中身が空になったフォルダ・ファイルを削除
		file.delete();
	}

	// ランダムキー生成メソッド(Django用)
	private String genSkeyForDjango() {
		String chars = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*(-_=+)";
		java.util.Random random = new java.util.Random();
		StringBuilder secretKey = new StringBuilder(50);

		for (int i = 0; i < 50; i++) {
			int randomIndex = random.nextInt(chars.length());
			char randomChar = chars.charAt(randomIndex);
			secretKey.append(randomChar);
		}

		return secretKey.toString();
	}
}
