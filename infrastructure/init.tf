terraform {
  required_version = "1.15.9"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.81.0"
    }
  }
  backend "azurerm" {}
}

provider "azurerm" {
  features {}
}
