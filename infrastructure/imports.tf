locals {
  apim_base = "/subscriptions/bd2864ed-4f3e-45ed-9c6a-8d179674bab1/resourceGroups/rg-sps-platform-sbox/providers/Microsoft.ApiManagement/service/sps-api-mgmt-sbox"
}

import {
  to = module.product.azurerm_api_management_product.product
  id = "${local.apim_base}/products/cp-crime-hearing-results"
}

import {
  to = module.product.azurerm_api_management_product_group.access_control_groups["administrators"]
  id = "${local.apim_base}/products/cp-crime-hearing-results/groups/administrators"
}

import {
  to = module.product.azurerm_api_management_product_group.access_control_groups["developers"]
  id = "${local.apim_base}/products/cp-crime-hearing-results/groups/developers"
}

import {
  to = module.product.azurerm_api_management_product_group.access_control_groups["guests"]
  id = "${local.apim_base}/products/cp-crime-hearing-results/groups/guests"
}
