#!/usr/bin/env bash
set -euo pipefail

REGION="${AWS_REGION:-ap-southeast-1}"
BUCKET_NAME="${STATE_BUCKET_NAME:-gym-management-tfstate-staging}"
TABLE_NAME="${DYNAMODB_TABLE_NAME:-gym-management-tflock-staging}"

echo "=== Bootstrapping Terraform Remote State in AWS Region: ${REGION} ==="

# 1. Create S3 Bucket
echo "Creating S3 bucket: ${BUCKET_NAME}..."
if [ "${REGION}" == "us-east-1" ]; then
    aws s3api create-bucket --bucket "${BUCKET_NAME}" --region "${REGION}" || true
else
    aws s3api create-bucket --bucket "${BUCKET_NAME}" --region "${REGION}" \
        --create-bucket-configuration LocationConstraint="${REGION}" || true
fi

# Enable S3 Bucket Versioning
echo "Enabling versioning on ${BUCKET_NAME}..."
aws s3api put-bucket-versioning --bucket "${BUCKET_NAME}" \
    --versioning-configuration Status=Enabled

# Enable S3 Bucket Encryption
echo "Enabling default encryption (AES256) on ${BUCKET_NAME}..."
aws s3api put-bucket-encryption --bucket "${BUCKET_NAME}" \
    --server-side-encryption-configuration '{
        "Rules": [{
            "ApplyServerSideEncryptionByDefault": {
                "SSEAlgorithm": "AES256"
            }
        }]
    }'

# Block S3 Public Access
echo "Blocking public access on ${BUCKET_NAME}..."
aws s3api put-public-access-block --bucket "${BUCKET_NAME}" \
    --public-access-block-configuration '{
        "BlockPublicAcls": true,
        "IgnorePublicAcls": true,
        "BlockPublicPolicy": true,
        "RestrictPublicBuckets": true
    }'

# 2. Create DynamoDB Table for Locking
echo "Creating DynamoDB table: ${TABLE_NAME}..."
aws dynamodb create-table \
    --table-name "${TABLE_NAME}" \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region "${REGION}" || true

echo "=== Bootstrap Completed Successfully! ==="
