locals {
  k8s_namespaces = [
    "api-gateway",
    "authentication-service",
    "config-server",
    "customer-service",
    "eureka-server",
    "inventory-service",
    "order-service",
    "product-service",
    "supplier-service",
  ]
}

resource "kubernetes_namespace" "this" {
  for_each = toset(local.k8s_namespaces)

  metadata {
    name = each.value

    labels = {
      project     = var.project_name
      environment = var.environment
    }
  }
}
