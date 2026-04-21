-- 用語統一: アッシュ → クリスタル（列名のみ変更、値はそのまま）
ALTER TABLE app_user RENAME COLUMN recycle_ash TO recycle_crystal;
