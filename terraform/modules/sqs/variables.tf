variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "queue_name_suffix" {
  type = string
}

variable "max_receive_count" {
  type        = number
  description = "How many times a message can be received before going to DLQ"
  default     = 3
}

variable "message_retention_seconds" {
  type        = number
  description = "Message retention for main queue"
  default     = 345600 # 4 days
}

variable "dlq_message_retention_seconds" {
  type        = number
  description = "Message retention for DLQ"
  default     = 1209600 # 14 days
}

variable "visibility_timeout_seconds" {
  type        = number
  description = "Visibility timeout for main queue"
  default     = 60
}

variable "receive_wait_time_seconds" {
  type        = number
  description = "Long polling wait time"
  default     = 20
}

variable "tags" {
  type = map(string)
}