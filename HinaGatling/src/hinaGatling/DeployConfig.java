package hinaGatling;

import java.util.HashMap;
import java.util.Map;

public class DeployConfig {
	private String target; // デプロイ先
	private String buildCommand;
	private String startCommand;
	private Map<String, String> envValue; // 環境変数

	// コンストラクタ
	public DeployConfig() {
		this.envValue = new HashMap<>();
	}

	public DeployConfig(String target, String buildCommand, String startCommand) {
		this();
		this.target = target;
		this.buildCommand = buildCommand;
		this.startCommand = startCommand;
	}

	// ゲッターセッター
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getBuildCommand() {
		return buildCommand;
	}

	public void setBuildCommand(String buildCommand) {
		this.buildCommand = buildCommand;
	}

	public String getStartCommand() {
		return startCommand;
	}

	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}

	public Map<String, String> getEnvValue() {
		return envValue;
	}

	public void setEnvValue(Map<String, String> envValue) {
		this.envValue = envValue;
	}
}
