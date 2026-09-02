package hinaGatling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TemplateManager {
	private static final String FILE_PATH = "templates.json";
	private final ObjectMapper mapper;

	public TemplateManager() {
		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new JavaTimeModule());
		this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	// JSONから全てのテンプレートを読み込み
	public List<Template> loadAll() {
		File file = new File(FILE_PATH);

		if (!file.exists()) {
			return new ArrayList<>();
		}

		try {
			// JSONを読み込んで、List<Template>型に変換
			return mapper.readValue(file, new TypeReference<List<Template>>() {
			});
		} catch (IOException e) {
			System.err.println("データの読み込みに失敗しました: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	// IDでテンプレートを検索
	public Template findById(String id) {
		List<Template> templates = loadAll();

		for (Template template : templates) {
			if (template.getId().equals(id)) {
				return template;
			}
		}
		return null;
	}

	// テンプレートをJSONに保存
	public void save(Template newTemplate) {
		List<Template> templates = loadAll();

		// 上書き保存かどうかを確認
		for (int i = 0; i < templates.size(); i++) {
			Template oldTemplate = templates.get(i);
			if (oldTemplate.getId().equals(newTemplate.getId())) {
				templates.remove(i);
				break;
			}
		}

		// データを追加
		templates.add(newTemplate);

		// JSONへ保存
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), templates);
		} catch (IOException e) {
			System.err.println("データの保存に失敗しました: " + e.getMessage());
		}
	}

	// 指定されたIDのテンプレートを削除
	public void delete(String id) {
		List<Template> templates = loadAll();

		boolean isDeleted = false; // 削除フラグ
		for (int i = 0; i < templates.size(); i++) {
			Template template = templates.get(i);
			if (template.getId().equals(id)) {
				templates.remove(i);
				isDeleted = true;
				break;
			}
		}

		if (isDeleted) {
			try {
				mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), templates);
				System.out.println("削除が完了しました。");
			} catch (IOException e) {
				System.err.println("データの保存に失敗しました: " + e.getMessage());
			}
		} else {
			System.out.println("指定されたIDが見つかりませんでした。");
		}
	}
}