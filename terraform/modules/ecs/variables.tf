variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "app_image" {
  type = string
}

variable "container_name" {
  type = string
}

variable "app_port" {
  type = number
}

variable "cpu" {
  type = number
}

variable "memory" {
  type = number
}

variable "desired_count" {
  type = number
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "ecs_security_group_id" {
  type = string
}

variable "target_group_arn" {
  type = string
}

variable "execution_role_arn" {
  type = string
}

variable "task_role_arn" {
  type = string
}

variable "log_group_name" {
  type = string
}

variable "db_host" {
  type = string
}

variable "db_port" {
  type = number
}

variable "db_name" {
  type = string
}

variable "db_username" {
  type = string
}

variable "sqs_queue_url" {
  type = string
}

variable "dynamodb_table_name" {
  type = string
}

variable "rds_secret_arn" {
  type = string
}

variable "thecatapi_secret_arn" {
  type = string
}

variable "tags" {
  type = map(string)
}