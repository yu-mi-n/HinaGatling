package hinaGatling;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Template {
	private String id;
	private String title;
	private String framework;
	private List<Blueprint> blueprintList;
	private List<String> setupCommand;
	private List<Modifier> modifierList;
	private LocalDateTime lastUpdateDate;

	// コンストラクタ
	public Template() {
		this.blueprintList = new ArrayList<>();
		this.setupCommand = new ArrayList<>();
		this.modifierList = new java.util.ArrayList<>();
	}

	public Template(String title, String framework) {
		this();
		this.id = UUID.randomUUID().toString();
		this.title = title;
		this.framework = framework;
		this.lastUpdateDate = LocalDateTime.now();
	}

	// ゲッターセッター
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFramework() {
		return framework;
	}

	public void setFramework(String framework) {
		this.framework = framework;
	}

	public List<Blueprint> getBlueprintList() {
		return blueprintList;
	}

	public void setBlueprintList(List<Blueprint> blueprintList) {
		this.blueprintList = blueprintList;
	}

	public List<String> getSetupCommand() {
		return setupCommand;
	}

	public void setSetupCommand(List<String> setupCommand) {
		this.setupCommand = setupCommand;
	}

	public List<Modifier> getModifierList() {
		return modifierList;
	}

	public void setModifierList(List<Modifier> modifierList) {
		this.modifierList = modifierList;
	}

	public LocalDateTime getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
}
