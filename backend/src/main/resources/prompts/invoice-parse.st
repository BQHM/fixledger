你要从家庭设备票据文本中提取设备建档所需字段。

要求：
- 只返回 JSON。
- 不要输出 Markdown。
- 没有把握的字段返回 null。
- 不要生成用户未提供的敏感信息。

返回结构：
{
  "deviceName": "设备名称或 null",
  "purchaseDate": "yyyy-MM-dd 或 null",
  "price": 0,
  "seller": "销售方或 null",
  "suggestedCategory": "建议分类或 null"
}

票据文本：
{{text}}
