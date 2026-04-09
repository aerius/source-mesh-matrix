#############################
# COLLECT REMOTE STATE DATA #
#############################

locals {
  theme_arr = split(",", var.service["theme"])
}
