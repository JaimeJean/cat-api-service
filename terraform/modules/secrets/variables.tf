variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "secret_name_suffix" {
  type = string
}

variable "description" {
  type    = string
  default = null
}

variable "recovery_window_in_days" {
  type    = number
  default = 7
}

variable "tags" {
  type = map(string)
}