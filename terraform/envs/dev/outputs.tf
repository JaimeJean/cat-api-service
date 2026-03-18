output "vpc_id" {
  value = module.network.vpc_id
}

output "alb_security_group_id" {
  value = module.security.alb_security_group_id
}

output "ecs_security_group_id" {
  value = module.security.ecs_security_group_id
}

output "rds_security_group_id" {
  value = module.security.rds_security_group_id
}

output "ecr_repository_name" {
  value = module.ecr.repository_name
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

output "dynamodb_table_name" {
  value = module.dynamodb.table_name
}

output "dynamodb_table_arn" {
  value = module.dynamodb.table_arn
}

output "sqs_queue_name" {
  value = module.sqs.queue_name
}

output "sqs_queue_url" {
  value = module.sqs.queue_url
}

output "sqs_queue_arn" {
  value = module.sqs.queue_arn
}

output "sqs_dlq_name" {
  value = module.sqs.dlq_name
}

output "sqs_dlq_url" {
  value = module.sqs.dlq_url
}

output "sqs_dlq_arn" {
  value = module.sqs.dlq_arn
}

output "private_db_subnet_ids" {
  value = module.network.private_db_subnet_ids
}

output "rds_identifier" {
  value = module.rds.db_instance_identifier
}

output "rds_endpoint" {
  value = module.rds.db_instance_endpoint
}

output "rds_address" {
  value = module.rds.db_instance_address
}

output "rds_port" {
  value = module.rds.db_instance_port
}

output "rds_db_name" {
  value = module.rds.db_name
}

output "rds_master_user_secret_arn" {
  value = module.rds.master_user_secret_arn
}