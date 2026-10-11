terraform {
  backend "s3" {
    bucket         = "gym-management-tfstate-staging"
    key            = "staging/terraform.tfstate"
    region         = "ap-southeast-1"
    dynamodb_table = "gym-management-tflock-staging"
    encrypt        = true
  }
}
