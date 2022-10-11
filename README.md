# DDB Practice

DynamoDBに対してCRUD操作を行うシンプルなAPIです。
- Play FrameworkからAWS SDK for JavaでDynamoDBを操作します
- DynamoDBLocalを利用します

## API仕様
[API仕様](doc/openapi.json)

※ [swagger-viewer](https://chrome.google.com/webstore/detail/swagger-viewer/nfmkaonpdmaglhjjlggfhlndofdldfag?hl=ja)をご利用ください

## セットアップ

```
docker compose up -d

// 初回のみテーブル作成を行う
aws dynamodb create-table --cli-input-json file://utils/users.json --endpoint-url http://localhost:8000

sbt run
```



## 参考
[[DynamoDB][Play framework] Specs2とDynamoDBLocalによる結合テスト
](https://dev.classmethod.jp/articles/dynamodbplay-framework-specs-1/)
