locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

data "aws_iam_policy_document" "ecs_task_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "task_execution_role" {
  name               = "${local.name_prefix}-ecs-task-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-ecs-task-execution-role"
  })
}

resource "aws_iam_role_policy_attachment" "task_execution_managed_policy" {
  role       = aws_iam_role.task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "task_execution_secrets_access" {
  statement {
    effect = "Allow"

    actions = [
      "secretsmanager:GetSecretValue"
    ]

    resources = var.secrets_arns
  }
}

resource "aws_iam_policy" "task_execution_secrets_access" {
  name   = "${local.name_prefix}-ecs-task-execution-secrets-access"
  policy = data.aws_iam_policy_document.task_execution_secrets_access.json

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-ecs-task-execution-secrets-access"
  })
}

resource "aws_iam_role_policy_attachment" "task_execution_secrets_access" {
  role       = aws_iam_role.task_execution_role.name
  policy_arn = aws_iam_policy.task_execution_secrets_access.arn
}

resource "aws_iam_role" "task_role" {
  name               = "${local.name_prefix}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-ecs-task-role"
  })
}

data "aws_iam_policy_document" "task_app_access" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage",
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:ChangeMessageVisibility",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl"
    ]

    resources = [
      var.sqs_queue_arn,
      var.sqs_dlq_arn
    ]
  }

  statement {
    effect = "Allow"

    actions = [
      "dynamodb:PutItem",
      "dynamodb:GetItem",
      "dynamodb:UpdateItem",
      "dynamodb:DeleteItem",
      "dynamodb:Query",
      "dynamodb:Scan"
    ]

    resources = [var.dynamodb_table_arn]
  }

  statement {
    effect = "Allow"

    actions = [
      "ses:SendEmail",
      "ses:SendRawEmail"
    ]

    resources = ["*"]
  }
}

resource "aws_iam_policy" "task_app_access" {
  name   = "${local.name_prefix}-ecs-task-app-access"
  policy = data.aws_iam_policy_document.task_app_access.json

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-ecs-task-app-access"
  })
}

resource "aws_iam_role_policy_attachment" "task_app_access" {
  role       = aws_iam_role.task_role.name
  policy_arn = aws_iam_policy.task_app_access.arn
}