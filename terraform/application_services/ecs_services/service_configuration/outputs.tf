output "services" {
  description = "The services object containing all configuration"
  value = yamldecode(templatefile("${path.module}/services.yaml.tftpl", {
      APP_TIMEZONE                        = var.app_timezone,
      APP_VERSION                         = var.app_version,
      PRODUCT_THEME                       = var.service["theme"],
      PRODUCT_TYPE                        = var.service["type"],

      THEME_NL                            = contains(local.theme_arr, "NL") ? "true" : "false",

      # First host in the list is the leading domain
      APP_DOMAIN                            = local.app_domain,

      REGISTRY_URL                          = var.ecr_directory == null ? "${var.ecr_repo}/${lower(var.environment)}" : "${var.ecr_repo}/${var.ecr_directory}",

      SERVICE_DISCOVERY_PRIVATE_DNS         = "${lower(local.name_prefix)}-int.local",


  })).services
}

output "flags" {
  description = "The dynamic flags to be exposed"
  value       = [
    {name = "API", key = "SMM_API_HOST", value = local.app_domain},
  ]
}
