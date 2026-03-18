locals {
  name_prefix = "${var.project_name}-${var.environment}"
  secret_name = "${local.name_prefix}-${var.secret_name_suffix}"
}

resource "aws_secretsmanager_secret" "this" {
  name                    = local.secret_name
  description             = var.description
  recovery_window_in_days = var.recovery_window_in_days

  tags = merge(var.tags, {
    Name = local.secret_name
  })
}