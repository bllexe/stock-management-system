terraform {
  backend "s3" {
    bucket         = "stock-management-tf-state"
    key            = "eks/terraform.tfstate"
    region         = "eu-central-1"
    dynamodb_table = "stock-management-tf-locks"
    encrypt        = true
  }
}
