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

resource "aws_cloudwatch_metric_alarm" "alb_no_healthy_targets" {
  alarm_name          = "${local.name_prefix}-alb-no-healthy-targets"
  alarm_description   = "Alarm when ALB target group has no healthy targets"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "HealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Average"
  threshold           = 1
  treat_missing_data  = "breaching"

  dimensions = {
    LoadBalancer = var.alb_arn_suffix
    TargetGroup  = var.target_group_arn_suffix
  }

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-alb-no-healthy-targets"
  })
}

resource "aws_cloudwatch_metric_alarm" "sqs_oldest_message_age" {
  alarm_name          = "${local.name_prefix}-sqs-oldest-message-age"
  alarm_description   = "Alarm when oldest SQS message age is too high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = "AWS/SQS"
  period              = 60
  statistic           = "Maximum"
  threshold           = 300
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = var.sqs_queue_name
  }

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-sqs-oldest-message-age"
  })
}

resource "aws_cloudwatch_metric_alarm" "sqs_dlq_has_messages" {
  alarm_name          = "${local.name_prefix}-sqs-dlq-has-messages"
  alarm_description   = "Alarm when DLQ has visible messages"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 60
  statistic           = "Maximum"
  threshold           = 0
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = var.sqs_dlq_name
  }

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-sqs-dlq-has-messages"
  })
}