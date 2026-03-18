module "network" {
  source = "../../modules/network"

  project_name = var.project_name
  environment  = var.environment
  vpc_cidr     = var.vpc_cidr
  tags         = local.common_tags
}

module "security" {
  source = "../../modules/security"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.network.vpc_id
  app_port     = var.app_port
  db_port      = var.db_port
  tags         = local.common_tags
}

module "ecr" {
  source = "../../modules/ecr"

  project_name = var.project_name
  environment  = var.environment
  tags         = local.common_tags
}

module "dynamodb" {
  source = "../../modules/dynamodb"

  project_name      = var.project_name
  environment       = var.environment
  table_name_suffix = var.dynamodb_table_name_suffix
  hash_key          = var.dynamodb_hash_key
  tags              = local.common_tags
}

module "sqs" {
  source = "../../modules/sqs"

  project_name               = var.project_name
  environment                = var.environment
  queue_name_suffix          = var.sqs_queue_name_suffix
  max_receive_count          = var.sqs_max_receive_count
  visibility_timeout_seconds = var.sqs_visibility_timeout_seconds
  tags                       = local.common_tags
}

module "rds" {
  source = "../../modules/rds"

  project_name = var.project_name
  environment  = var.environment

  db_name           = var.db_name
  db_username       = var.db_username
  db_port           = var.db_port
  instance_class    = var.db_instance_class
  allocated_storage = var.db_allocated_storage

  subnet_ids             = module.network.private_db_subnet_ids
  vpc_security_group_ids = [module.security.rds_security_group_id]

  publicly_accessible = false
  skip_final_snapshot = true

  tags = local.common_tags
}