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

module "thecatapi_secret" {
  source = "../../modules/secrets"

  project_name = var.project_name
  environment  = var.environment

  secret_name_suffix = var.thecatapi_secret_name_suffix
  description        = "API key for TheCatAPI"
  tags               = local.common_tags
}

module "iam" {
  source = "../../modules/iam"

  project_name = var.project_name
  environment  = var.environment

  secrets_arns = [
    module.thecatapi_secret.secret_arn,
    module.rds.master_user_secret_arn
  ]

  sqs_queue_arn    = module.sqs.queue_arn
  sqs_dlq_arn      = module.sqs.dlq_arn
  dynamodb_table_arn = module.dynamodb.table_arn

  tags = local.common_tags
}

module "observability" {
  source = "../../modules/observability"

  project_name          = var.project_name
  environment           = var.environment
  log_retention_in_days = var.log_retention_in_days
  tags                  = local.common_tags
}

module "alb" {
  source = "../../modules/alb"

  project_name          = var.project_name
  environment           = var.environment
  vpc_id                = module.network.vpc_id
  public_subnet_ids     = module.network.public_subnet_ids
  alb_security_group_id = module.security.alb_security_group_id
  app_port              = var.app_port
  health_check_path     = var.health_check_path
  tags                  = local.common_tags
}

module "ecs" {
  source = "../../modules/ecs"

  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  app_image      = "${module.ecr.repository_url}:latest"
  container_name = var.ecs_container_name
  app_port       = var.app_port
  cpu            = var.ecs_cpu
  memory         = var.ecs_memory
  desired_count  = var.ecs_desired_count

  public_subnet_ids     = module.network.public_subnet_ids
  ecs_security_group_id = module.security.ecs_security_group_id
  target_group_arn      = module.alb.target_group_arn

  execution_role_arn = module.iam.task_execution_role_arn
  task_role_arn      = module.iam.task_role_arn
  log_group_name     = module.observability.ecs_log_group_name

  db_host     = module.rds.db_instance_address
  db_port     = module.rds.db_instance_port
  db_name     = module.rds.db_name
  db_username = var.db_username

  sqs_queue_url       = module.sqs.queue_url
  dynamodb_table_name = module.dynamodb.table_name

  rds_secret_arn      = module.rds.master_user_secret_arn
  thecatapi_secret_arn = module.thecatapi_secret.secret_arn

  tags = local.common_tags
}