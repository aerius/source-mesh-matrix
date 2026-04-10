locals {
  basicinfra_version          = "v1"
  ecr_repo                    = "028339422996.dkr.ecr.eu-west-1.amazonaws.com"

  target_groups = {
    "tg1" = {name = "api", protocol = "HTTP", port = "8080", path = "/api/actuator/health",  matcher = "200-399"},
  }

  listener_rules = {
    "rule1"    = {tg = "tg1", application_type = "APP", path_pattern = "/api/*", cognito = false},
  }

  ecs_ctr_fes_1_instance_type         = "c6a.xlarge"
  ecs_ctr_fes_1_max_instance_size     = "2"
}
