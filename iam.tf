data "aws_iam_policy_document" "cluster_autoscaler" {
  statement {
    sid = "AllowClusterAutoscalerScaling"

    actions = [
      "autoscaling:SetDesiredCapacity",
      "autoscaling:TerminateInstanceInAutoScalingGroup",
      "autoscaling:UpdateAutoScalingGroup",
    ]

    resources = ["*"]

    condition {
      test     = "StringEquals"
      variable = "autoscaling:ResourceTag/k8s.io/cluster-autoscaler/enabled"
      values   = ["true"]
    }

    condition {
      test     = "StringEquals"
      variable = "autoscaling:ResourceTag/kubernetes.io/cluster/${var.cluster_name}"
      values   = ["owned"]
    }
  }

  statement {
    sid = "DescribeAutoscaling"

    actions = [
      "autoscaling:DescribeAutoScalingGroups",
      "autoscaling:DescribeAutoScalingInstances",
      "autoscaling:DescribeLaunchConfigurations",
      "autoscaling:DescribeTags",
      "ec2:DescribeLaunchTemplateVersions",
    ]

    resources = ["*"]
  }

  statement {
    sid = "DescribeEC2"

    actions = [
      "ec2:DescribeInstanceTypes",
      "ec2:DescribeInstances",
      "ec2:DescribeLaunchTemplateVersions",
      "ec2:DescribeTags",
    ]

    resources = ["*"]
  }
}

resource "aws_iam_policy" "cluster_autoscaler" {
  name        = "${var.cluster_name}-cluster-autoscaler"
  description = "IAM policy for Kubernetes Cluster Autoscaler on EKS"

  policy = data.aws_iam_policy_document.cluster_autoscaler.json

  tags = merge({
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

module "cluster_autoscaler_iam_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "5.39.1"

  role_name                        = "${var.cluster_name}-cluster-autoscaler"
  attach_cluster_autoscaler_policy = false

  oidc_providers = {
    eks = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:cluster-autoscaler"]
    }
  }

  tags = merge({
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

resource "aws_iam_role_policy_attachment" "cluster_autoscaler" {
  role       = module.cluster_autoscaler_iam_role.iam_role_name
  policy_arn = aws_iam_policy.cluster_autoscaler.arn
}