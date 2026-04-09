locals {
  name_prefix                 = var.environment
  name_ssm_prefix             = "/${lower(var.environment)}"
  app_domain                  = split(",", var.application_host_headers["APP"])[0]
}
