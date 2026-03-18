locals {
  name_prefix = "${var.project_name}-${var.environment}"
  queue_name  = "${local.name_prefix}-${var.queue_name_suffix}"
  dlq_name    = "${local.name_prefix}-${var.queue_name_suffix}-dlq"
}

resource "aws_sqs_queue" "main" {
  name                       = local.queue_name
  message_retention_seconds  = var.message_retention_seconds
  visibility_timeout_seconds = var.visibility_timeout_seconds
  receive_wait_time_seconds  = var.receive_wait_time_seconds

  tags = merge(var.tags, {
    Name = local.queue_name
  })
}

resource "aws_sqs_queue" "dlq" {
  name                      = local.dlq_name
  message_retention_seconds = var.dlq_message_retention_seconds

  tags = merge(var.tags, {
    Name = local.dlq_name
  })
}

resource "aws_sqs_queue_redrive_policy" "main" {
  queue_url = aws_sqs_queue.main.id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = var.max_receive_count
  })
}

resource "aws_sqs_queue_redrive_allow_policy" "dlq" {
  queue_url = aws_sqs_queue.dlq.id

  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue"
    sourceQueueArns   = [aws_sqs_queue.main.arn]
  })
}