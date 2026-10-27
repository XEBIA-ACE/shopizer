module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "21.29.0"

  cluster_name    = var.cluster_name
  cluster_version = var.eks_version

  vpc_id     = aws_vpc.this.id
  subnet_ids = [for s in aws_subnet.private : s.id]

  cluster_endpoint_public_access  = true
  cluster_endpoint_private_access = true

  enable_irsa = true

  cluster_enabled_log_types = var.enable_cluster_log_types

  cloudwatch_log_group_retention_in_days = 30

  eks_managed_node_groups = {
    default = {
      instance_types = [var.instance_type]

      min_size     = var.min_size
      max_size     = var.max_size
      desired_size = var.desired_size

      capacity_type = "ON_DEMAND"

      disk_size = 50

      labels = {
        Environment = var.environment
        ManagedBy   = "ace"
      }

      tags = {
        Name        = "${var.cluster_name}-default-ng"
        ManagedBy   = "ace"
        Environment = var.environment
        Project     = var.project_id
      }
    }
  }

  tags = merge({
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}