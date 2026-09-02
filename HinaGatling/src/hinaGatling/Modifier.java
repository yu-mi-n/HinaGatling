package hinaGatling;

public class Modifier {
	private String path;
	private String targetText;
	private String replacementText;

	public Modifier() {
	}

	public Modifier(String path, String targetText, String replacementText) {
		this.path = path;
		this.targetText = targetText;
		this.replacementText = replacementText;
	}

	// ゲッtーセッター

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTargetText() {
		return targetText;
	}

	public void setTargetText(String targetText) {
		this.targetText = targetText;
	}

	public String getReplacementText() {
		return replacementText;
	}

	public void setReplacementText(String replacementText) {
		this.replacementText = replacementText;
	}

}
