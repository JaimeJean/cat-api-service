locals {
  name_prefix = "${var.project_name}-${var.environment}"
  table_name  = "${local.name_prefix}-${var.table_name_suffix}"
}

resource "aws_dynamodb_table" "this" {
  name         = local.table_name
  billing_mode = var.billing_mode
  hash_key     = var.hash_key

  attribute {
    name = var.hash_key
    type = "S"
  }

  ttl {
    attribute_name = var.ttl_attribute_name
    enabled        = var.enable_ttl
  }

  point_in_time_recovery {
    enabled = false
  }

  tags = merge(var.tags, {
    Name = local.table_name
  })
}