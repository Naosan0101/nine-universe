-- カードテキスト: ストーンの数え方「個」→「つ」に統一
UPDATE card_definition SET
    deploy_help = REPLACE(
        REPLACE(
            REPLACE(
                REPLACE(
                    REPLACE(
                        REPLACE(
                            deploy_help,
                            'ストーン1個につき', 'ストーン1つにつき'),
                        'ストーン2個を使用', 'ストーンを2つ使用'),
                    'ストーン1個を使用', 'ストーンを1つ使用'),
                'ストーンを3個', 'ストーンを3つ'),
            'ストーンを2個', 'ストーンを2つ'),
        'ストーンを1個', 'ストーンを1つ'),
    passive_help = REPLACE(
        REPLACE(
            REPLACE(
                REPLACE(
                    REPLACE(
                        REPLACE(
                            passive_help,
                            'ストーン1個につき', 'ストーン1つにつき'),
                        'ストーン2個を使用', 'ストーンを2つ使用'),
                    'ストーン1個を使用', 'ストーンを1つ使用'),
                'ストーンを3個', 'ストーンを3つ'),
            'ストーンを2個', 'ストーンを2つ'),
        'ストーンを1個', 'ストーンを1つ');
