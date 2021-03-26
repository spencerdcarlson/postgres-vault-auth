# Vault 

## Payload Examples
```bash
VAULT_ADDR=http://localhost vault read postgres/database/creds/readonly -format=json
```
```json
{
  "request_id": "9088c3f9-baa4-0dd9-d75e-9cbaed6a9d43",
  "lease_id": "postgres/black-mamba-follower-database/creds/readonly/iiRBrOadEy3sY9C8aSWiwLjU",
  "lease_duration": 28800,
  "renewable": true,
  "data": {
    "password": "mypassword",
    "username": "myuser"
  },
  "warnings": null
}
```

```bash
$ VAULT_ADDR=http://localhost vault login -method=okta username=myuser password=mypass -format=json
```

```json
{
  "request_id": "834cff79-94a5-a323-87e1-78291f57b8f3",
  "lease_id": "",
  "lease_duration": 0,
  "renewable": false,
  "data": {},
  "warnings": null,
  "auth": {
    "client_token": "mytoken",
    "accessor": "myaccessor",
    "policies": [
      "default",
      "techleads_dea"
    ],
    "token_policies": [
      "default"
    ],
    "identity_policies": null,
    "metadata": {
      "policies": "default",
      "username": "myuser"
    },
    "orphan": true,
    "entity_id": "37b50cc9-37c5-2366-eac3-61a61044cdce",
    "lease_duration": 2764800,
    "renewable": true
  }
}
```

```bash
vault token lookup -format=json
```
```json
{
  "request_id": "476db59a-eb5e-89d7-df86-7986bc8c0ea0",
  "lease_id": "",
  "lease_duration": 0,
  "renewable": false,
  "data": {
    "accessor": "myaccessor",
    "creation_time": 1616781851,
    "creation_ttl": 2764800,
    "display_name": "myuser",
    "entity_id": "37b50cc9-37c5-2366-eac3-61a61044cdce",
    "expire_time": "2021-04-27T18:04:11.395096994Z",
    "explicit_max_ttl": 0,
    "id": "myid",
    "issue_time": "2021-03-26T18:04:11.395109735Z",
    "meta": {
      "policies": "default",
      "username": "myusername"
    },
    "num_uses": 0,
    "orphan": true,
    "path": "auth/okta/login/myuser",
    "policies": [
      "default"
    ],
    "renewable": true,
    "ttl": 2764175,
    "type": "service"
  },
  "warnings": null
}
```