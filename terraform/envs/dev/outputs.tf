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