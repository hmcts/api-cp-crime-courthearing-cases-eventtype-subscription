module "product" {
  source = "git::https://github.com/hmcts/cnp-module-api-mgmt-product.git?ref=master"

  api_mgmt_rg                   = var.api_mgmt_rg
  api_mgmt_name                 = var.api_mgmt_name
  name                          = var.apim_product.name
  subscription_required         = var.apim_product.subscription_required
  subscriptions_limit           = var.apim_product.subscriptions_limit
  approval_required             = var.apim_product.approval_required
  published                     = var.apim_product.published
  product_access_control_groups = var.apim_product.product_access_control_groups
  product_policy                = ""
}
