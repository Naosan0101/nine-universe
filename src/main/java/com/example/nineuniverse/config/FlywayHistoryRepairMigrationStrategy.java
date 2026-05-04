package com.example.nineuniverse.config;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway が「V1 適用済み」と記録しているのに {@code app_user} が無い不整合があると V2 が失敗する。
 * その場合のみ履歴の 1 / 2 を削除し、マイグレーションを最初からやり直す（中身が空の DB 向け）。
 * <p>
 * その後 {@link Flyway#repair()} で、既に適用済みマイグレーションのファイルをローカルで編集したときの
 * チェックサム不一致を {@code flyway_schema_history} 側に合わせて解消する（起動不能の回避）。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class FlywayHistoryRepairMigrationStrategy {

	private final DataSource dataSource;

	@Bean
	FlywayMigrationStrategy flywayMigrationStrategy() {
		return flyway -> {
			try (Connection c = dataSource.getConnection()) {
				repairIfNeeded(c);
			} catch (SQLException e) {
				String hint = connectionFailureHint(e);
				throw new IllegalStateException(
						"Flyway 事前修復で DB に接続できませんでした。" + hint, e);
			}
			flyway.repair();
			flyway.migrate();
		};
	}

	private static String connectionFailureHint(SQLException root) {
		for (Throwable t = root; t != null; t = t.getCause()) {
			if (t instanceof ConnectException) {
				return " PostgreSQL に TCP で届いていません。spring.datasource.url のホスト・ポート、"
						+ "サーバ上で PostgreSQL が起動しているか、ファイアウォールと pg_hba.conf で接続が許可されているかを確認してください。"
						+ " ローカル開発のみの例: docker compose up -d postgres";
			}
			if (t instanceof SQLException se && "28P01".equals(se.getSQLState())) {
				return " spring.datasource のユーザー名・パスワードが、接続先 DB（VPS 上の PostgreSQL 等）の定義と一致しているか確認してください。"
						+ " 本番・リモートでは環境変数 SPRING_DATASOURCE_USERNAME / SPRING_DATASOURCE_PASSWORD（および URL）で"
						+ " サーバ管理者が設定した値に上書きするのが安全です。"
						+ " ローカル Docker では、初回作成済みのデータボリュームに古いパスワードが残っていると compose の値とずれることがあります。";
			}
			String msg = t.getMessage();
			if (msg != null && (msg.contains("接続が拒絶") || msg.contains("Connection refused"))) {
				return " PostgreSQL に TCP で届いていません。ホスト・ポート、サーバの PostgreSQL 起動、"
						+ "ファイアウォールと pg_hba.conf を確認してください。"
						+ " ローカル開発のみの例: docker compose up -d postgres";
			}
			if (msg != null
					&& (msg.contains("password authentication failed") || msg.contains("パスワード認証"))) {
				return " spring.datasource のユーザー名・パスワードが接続先 DB と一致しているか確認してください。"
						+ " VPS ではサーバ上で作成したロールのパスワードに合わせ、環境変数で上書きしてください。"
						+ " ローカル Docker では古いボリュームと compose のパスワードがずれることがあります。";
			}
		}
		return "";
	}

	private void repairIfNeeded(Connection c) throws SQLException {
		if (!tableExists(c, "flyway_schema_history")) {
			return;
		}
		if (tableExists(c, "app_user")) {
			return;
		}
		if (!historyHasSuccessfulVersion(c, "1")) {
			return;
		}
		int otherTables = countUserTablesExcluding(c, "flyway_schema_history");
		if (otherTables > 0) {
			log.error(
					"Flyway は version 1 を適用済みですが app_user がありません。"
							+ " 他に {} 個のユーザーテーブルがあるため自動修復しません。"
							+ " scripts/reset-springdb-public-schema.sql を実行するか、手でスキーマを整合させてください。",
					otherTables);
			return;
		}
		log.warn(
				"Flyway 履歴に version 1 がありますが app_user がありません。"
						+ " flyway_schema_history から version 1,2 を削除し、マイグレーションを再実行します。");
		try (Statement st = c.createStatement()) {
			st.executeUpdate("DELETE FROM flyway_schema_history WHERE version IN ('1', '2')");
		}
	}

	private static boolean historyHasSuccessfulVersion(Connection c, String version) throws SQLException {
		try (PreparedStatement ps = c.prepareStatement(
				"SELECT 1 FROM flyway_schema_history WHERE version = ? AND success = TRUE LIMIT 1")) {
			ps.setString(1, version);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private static int countUserTablesExcluding(Connection c, String excludeTable) throws SQLException {
		String schema = c.getSchema();
		if (schema == null) {
			schema = "public";
		}
		try (PreparedStatement ps = c.prepareStatement(
				"SELECT COUNT(*) FROM information_schema.tables "
						+ "WHERE table_schema = ? AND table_type = 'BASE TABLE' AND LOWER(table_name) <> LOWER(?)")) {
			ps.setString(1, schema);
			ps.setString(2, excludeTable);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		}
	}

	private static boolean tableExists(Connection c, String tableName) throws SQLException {
		DatabaseMetaData meta = c.getMetaData();
		String schema = c.getSchema();
		String catalog = c.getCatalog();
		try (ResultSet rs = meta.getTables(catalog, schema, tableName, new String[] {"TABLE"})) {
			return rs.next();
		}
	}
}
