terraform {
  backend "s3" {
    bucket       = "itau-case-cat-api-aws-dev-tfstate"
    key          = "envs/dev/terraform.tfstate"
    region       = "us-east-1"
    use_lockfile = true
    encrypt      = true
  }
}