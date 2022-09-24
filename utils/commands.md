
テーブルの作成
```
 aws dynamodb create-table --cli-input-json file://utils/users.json --endpoint-url http://localhost:8000
```

テーブル一覧
```
 aws dynamodb describe-table --table-name users --endpoint-url http://localhost:8000
```