variable "aws_region" {
  type        = string
  description = "AWS region"
}

variable "project_name" {
  type        = string
  description = "Project name"
}

variable "environment" {
  type        = string
  description = "Environment name"
}

variable "vpc_cidr" {
  type        = string
  description = "CIDR block for VPC"
}

variable "app_port" {
  type        = number
  description = "Application port"
}

variable "db_port" {
  type        = number
  description = "Database port"
}

variable "dynamodb_table_name_suffix" {
  type        = string
  description = "Suffix for application DynamoDB table name"
}

variable "dynamodb_hash_key" {
  type        = string
  description = "Hash key for application DynamoDB table"
}

variable "dynamodb_ttl_attribute_name" {
  type        = string
  description = "TTL attribute name for application DynamoDB table"
}

variable "sqs_queue_name_suffix" {
  type        = string
  description = "Suffix for the main SQS queue name"
}

variable "sqs_max_receive_count" {
  type        = number
  description = "Max receives before message goes to DLQ"
}

variable "sqs_visibility_timeout_seconds" {
  type        = number
  description = "Visibility timeout for the main queue"
}

variable "db_name" {
  type        = string
  description = "Database name"
}

variable "db_username" {
  type        = string
  description = "Database username"
}

variable "db_instance_class" {
  type        = string
  description = "RDS instance class"
}

variable "db_allocated_storage" {
  type        = number
  description = "RDS allocated storage in GB"
}

variable "thecatapi_secret_name_suffix" {
  type        = string
  description = "Suffix for TheCatAPI secret name"
}

variable "log_retention_in_days" {
  type        = number
  description = "CloudWatch log retention"
}

variable "health_check_path" {
  type        = string
  description = "Health check path for ALB target group"
}

variable "ecs_container_name" {
  type        = string
  description = "ECS container name"
}

variable "ecs_cpu" {
  type        = number
  description = "Fargate CPU units"
}

variable "ecs_memory" {
  type        = number
  description = "Fargate memory in MiB"
}

variable "ecs_desired_count" {
  type        = number
  description = "Desired ECS service count"
}

variable "ses_sender_email" {
  type        = string
  description = "Verified sender email for Amazon SES"
}