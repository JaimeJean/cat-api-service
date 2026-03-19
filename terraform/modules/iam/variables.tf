variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "secrets_arns" {
  type = list(string)
}

variable "sqs_queue_arn" {
  type = string
}

variable "sqs_dlq_arn" {
  type = string
}

variable "dynamodb_table_arn" {
  type = string
}

variable "tags" {
  type = map(string)
}