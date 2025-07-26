# vault-init-dev.ps1
$VAULT_CONTAINER = "vault"
$VAULT_TOKEN = "root"
$VAULT_ADDR = "http://vault:8200"

# Функция для добавления секретов
# Первый аргумент (ServiceName) - название сервиса, для которого добавен секрет
# Key - ключ секрета
# Value - значение
function Save-Secret {
    param(
        [string]$ServiceName,
        [string]$Key,
        [string]$Value
    )

    docker exec -e VAULT_TOKEN=$VAULT_TOKEN $VAULT_CONTAINER `
        vault kv put "secret/$ServiceName/$Key" value=$Value
}


# Общие секреты
Save-Secret "shared" "POSTGRES_USER" "postgres"
Save-Secret "shared" "POSTGRES_PASSWORD" "postgres"

# Секреты для отдельных сервисов