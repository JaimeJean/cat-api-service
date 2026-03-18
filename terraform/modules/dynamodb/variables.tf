variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "table_name_suffix" {
  type = string
}

variable "hash_key" {
  type = string
}

variable "billing_mode" {
  type    = string
  default = "PAY_PER_REQUEST"
}

variable "ttl_attribute_name" {
  type        = string
  description = "TTL attribute name"
  default     = "ttl"
}

variable "enable_ttl" {
  type        = bool
  description = "Enable DynamoDB TTL"
  default     = true
}

variable "tags" {
  type = map(string)
}