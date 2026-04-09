############################
# Terragrunt configuration
############################

# Include all settings from the root terraform.tfvars file
include {
  path = find_in_parent_folders()
}

dependencies {
  paths = ["../security", "../databases"]
}

dependency "security" {
  config_path  = "../security"

  mock_outputs = {
    "passwords" = {
      "rds": "myFakePassword"
    }
  }

  mock_outputs_allowed_terraform_commands = ["init", "validate", "plan"]
}

terraform {
  extra_arguments "common_vars" {
    commands = get_terraform_commands_that_need_vars()
  }
}

inputs = {
  ssm_passwords    = dependency.security.outputs.passwords
}
