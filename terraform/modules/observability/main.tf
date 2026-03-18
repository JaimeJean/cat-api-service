locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${local.name_prefix}-app"
  retention_in_days = var.log_retention_in_days

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-ecs-log-group"
  })
}