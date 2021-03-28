# Vault Auth

![Build](https://github.com/davidsteinsland/postgres-vault-auth/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16104-postgresql-vault-auth.svg)](https://plugins.jetbrains.com/plugin/16104-postgresql-vault-auth)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16104-postgresql-vault-auth.svg)](https://plugins.jetbrains.com/plugin/16104-postgresql-vault-auth)

<!-- Plugin description -->
Fetches credentials for a database from Vault.

This plugin assumes that [vault](https://learn.hashicorp.com/tutorials/vault/getting-started-install?in=vault/getting-started) is installed and available.
The currently supported authentication methods are [OIDC](https://www.vaultproject.io/docs/auth/jwt) (web browser flow) and [OKTA](https://www.vaultproject.io/docs/auth/okta)
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Postgres Vault Auth"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/davidsteinsland/postgres-vault-auth/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Configuration
For global configuration see <kbd>Settings/Preferences</kbd> > <kbd>Tools</kbd> > <kbd>Vault</kbd>

See Database Properties for connection specific configurations
  
### Vault SVG Logo

* [Source](https://worldvectorlogo.com/logo/vault-enterprise)
* [Terms of use](https://worldvectorlogo.com/terms-of-use)