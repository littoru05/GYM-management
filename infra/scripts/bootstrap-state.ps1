param(
    [string]$Region = "ap-southeast-1",
    [string]$BucketName = "gym-management-tfstate-staging",
    [string]$TableName = "gym-management-tflock-staging"
)

$ErrorActionPreference = "Continue"

Write-Host "=== Bootstrapping Terraform Remote State in AWS Region: $Region ===" -ForegroundColor Cyan

# 1. Create S3 Bucket
Write-Host "Creating S3 bucket: $BucketName..."
if ($Region -eq "us-east-1") {
    aws s3api create-bucket --bucket $BucketName --region $Region
} else {
    aws s3api create-bucket --bucket $BucketName --region $Region --create-bucket-configuration LocationConstraint=$Region
}

# Enable S3 Versioning
Write-Host "Enabling versioning on $BucketName..."
aws s3api put-bucket-versioning --bucket $BucketName --versioning-configuration Status=Enabled

# Enable S3 Encryption
Write-Host "Enabling encryption on $BucketName..."
aws s3api put-bucket-encryption --bucket $BucketName --server-side-encryption-configuration '{"Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]}'

# Block Public Access
Write-Host "Blocking public access on $BucketName..."
aws s3api put-public-access-block --bucket $BucketName --public-access-block-configuration '{"BlockPublicAcls": true, "IgnorePublicAcls": true, "BlockPublicPolicy": true, "RestrictPublicBuckets": true}'

# 2. Create DynamoDB Table
Write-Host "Creating DynamoDB table: $TableName..."
aws dynamodb create-table `
    --table-name $TableName `
    --attribute-definitions AttributeName=LockID,AttributeType=S `
    --key-schema AttributeName=LockID,KeyType=HASH `
    --billing-mode PAY_PER_REQUEST `
    --region $Region

Write-Host "=== Bootstrap Completed Successfully! ===" -ForegroundColor Green
