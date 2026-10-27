output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.this.id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets"
  value       = [for s in aws_subnet.public : s.id]
}

output "private_subnet_ids" {
  description = "IDs of the private subnets"
  value       = [for s in aws_subnet.private : s.id]
}

output "cluster_name" {
  description = "Name of the EKS cluster"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "Endpoint for EKS control plane"
  value       = module.eks.cluster_endpoint
}

output "cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data required to communicate with the cluster"
  value       = module.eks.cluster_certificate_authority_data
}

output "node_group_role_arn" {
  description = "IAM role ARN of the default managed node group"
  value       = module.eks.eks_managed_node_groups["default"].iam_role_arn
}

output "cluster_security_group_id" {
  description = "EKS cluster security group ID"
  value       = module.eks.cluster_security_group_id
}

output "cluster_autoscaler_iam_role_arn" {
  description = "IAM role ARN used by Cluster Autoscaler"
  value       = module.cluster_autoscaler_iam_role.iam_role_arn
}