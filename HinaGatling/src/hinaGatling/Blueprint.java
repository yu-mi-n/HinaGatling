package hinaGatling;

public class Blueprint {
	private String path;
	private String content;
	private boolean isSecret;

	// コンストラクタ
	public Blueprint() {

	}

	public Blueprint(String path, String content, boolean isSecret) {
		this.path = path;
		this.content = content;
		this.isSecret = isSecret;
	}

//	ゲッターセッター
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getIsSecret() {
		return isSecret;
	}

	public void setIsSecret(Boolean isSecret) {
		this.isSecret = isSecret;
	}
}
