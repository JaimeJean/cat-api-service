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