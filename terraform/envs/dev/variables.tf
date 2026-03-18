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