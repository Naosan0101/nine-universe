-- 複合種族マーフォーク系: 共有イラスト（V138 で attribute 一括更新。初回リリース時の ID 指定は互換のため残す）
UPDATE card_definition SET image_file = 'ドラゴンマーフォーク.PNG' WHERE id IN (77, 78);
UPDATE card_definition SET image_file = '人間マーフォーク.PNG' WHERE id = 79;
