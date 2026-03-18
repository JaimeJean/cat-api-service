variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "log_retention_in_days" {
  type    = number
  default = 7
}

variable "tags" {
  type = map(string)
}

variable "alb_arn_suffix" {
  type = string
}

variable "target_group_arn_suffix" {
  type = string
}

variable "sqs_queue_name" {
  type = string
}

variable "sqs_dlq_name" {
  type = string
}