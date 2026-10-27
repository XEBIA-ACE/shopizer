resource "aws_cloudwatch_log_group" "cluster" {
  name              = "/aws/eks/${var.cluster_name}/cluster"
  retention_in_days = 30

  tags = merge({
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

resource "aws_cloudwatch_log_group" "cluster_autoscaler" {
  name              = "/aws/eks/${var.cluster_name}/cluster-autoscaler"
  retention_in_days = 30

  tags = merge({
    ManagedBy   = "ace"
    Environment = var.environment
    Project     = var.project_id
  }, var.tags)
}

resource "helm_release" "cluster_autoscaler" {
  name       = "cluster-autoscaler"
  repository = "https://kubernetes.github.io/autoscaler"
  chart      = "cluster-autoscaler"
  namespace  = "kube-system"
  version    = "9.37.0"

  create_namespace = false

  values = [
    yamlencode({
      rbac = {
        serviceAccount = {
          create = true
          name   = "cluster-autoscaler"
          annotations = {
            "eks.amazonaws.com/role-arn" = module.cluster_autoscaler_iam_role.iam_role_arn
          }
        }
      }
      cloudProvider = "aws"
      awsRegion     = var.region
      autoDiscovery = {
        clusterName = var.cluster_name
      }
      extraArgs = {
        "scan-interval"                             = "10s"
        "skip-nodes-with-local-storage"             = "false"
        "skip-nodes-with-system-pods"               = "false"
        "balance-similar-node-groups"               = "true"
        "skip-nodes-with-system-pods=false"         = "true"
        "stderrthreshold"                           = "info"
        "v"                                         = "4"
        "expander"                                  = "least-waste"
      }
      podAnnotations = {
        "cluster-autoscaler.kubernetes.io/safe-to-evict" = "false"
      }
      serviceMonitor = {
        enabled = false
      }
    })
  ]

  depends_on = [module.eks]

  lifecycle {
    ignore_changes = [metadata]
  }
}