package hinaGatling;

public class CreateDefaultTemp {
	// Django用(render)デフォテンプレート
	public Template createForDjango(String title) {
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

		String env = "DEBUG=True\nSECRET_KEY={{AUTO_GENERATE_KEY}}";
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

		template.getBlueprintList().add(new Blueprint("static/仮ファイル", "", false));
		template.getBlueprintList().add(new Blueprint("templates/仮ファイル", "", false));

		// 既存ファイルの書き換えルール
		template.getModifierList().add(new Modifier("config/settings.py", "from pathlib import Path",
				"from pathlib import Path\nfrom dotenv import load_dotenv\nimport os"));
		template.getModifierList()
				.add(new Modifier("config/settings.py", "'DIRS': [],", "'DIRS': [BASE_DIR / 'templates'],"));
		template.getModifierList()
				.add(new Modifier("config/settings.py", "TIME_ZONE = 'UTC'", "TIME_ZONE = 'Asia/Tokyo'"));
		template.getModifierList()
				.add(new Modifier("config/settings.py", "LANGUAGE_CODE = 'en-us'", "LANGUAGE_CODE = 'ja'"));
		template.getModifierList().add(new Modifier("config/settings.py", "STATIC_URL = 'static/'",
				"STATIC_URL = 'static/'\nSTATICFILES_DIRS = [BASE_DIR / 'static']"));
		template.getModifierList()
				.add(new Modifier("config/settings.py", "ALLOWED_HOSTS = []", "ALLOWED_HOSTS = ['*']"));

		String envConfig = "load_dotenv()\n\n" +
				"SECRET_KEY = os.getenv('SECRET_KEY')\n\n" +
				"# SECURITY WARNING: don't run with debug turned on in production!\n" +
				"if os.getenv('DEBUG'):\n" +
				"    DEBUG = os.getenv('DEBUG')\n" +
				"else:\n" +
				"    DEBUG = False";

		template.getModifierList().add(new Modifier("config/settings.py", "DEBUG = True", envConfig));

		return template;
	}
}
