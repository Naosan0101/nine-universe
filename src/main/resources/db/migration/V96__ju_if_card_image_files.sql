-- スタンダードパック2 収録（pack_initial JU / IF）: イラストを「カード名.PNG」に統一
UPDATE card_definition
SET image_file = name || '.PNG'
WHERE id BETWEEN 31 AND 68;
