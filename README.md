
# AURORA-AAN
- [![Java](https://img.shields.io/badge/Java-17-blue?style=flat-square&logo=java)](https://docs.oracle.com/en/java/)
- [![Docker](https://img.shields.io/badge/Docker-Latest-blue?style=flat-square&logo=docker)](https://www.docker.com/)
- [![Kafka](https://img.shields.io/badge/Apache%20Kafka-Latest-white?style=flat-square&logo=apache-kafka)](https://kafka.apache.org/)
- [![RocksDB](https://img.shields.io/badge/RocksDB-7.10.2-yellow?style=flat-square&logo=rocksdb)](https://rocksdb.org/)
- [![RxJava](https://img.shields.io/badge/RxJava-1.3.8-red?style=flat-square&logo=reactivex)](https://github.com/ReactiveX/RxJava)
- [![Javalin](https://img.shields.io/badge/Javalin-4.6.1-blue?style=flat-square&logo=javalin)](https://javalin.io/)

Proyecto de implementación del Modelo Aurora de la Iniciativa Aurora (https://aurora.ong).     

<b>Responsable del proyecto:</b><br /> 
Pavel Delgado (p.delgadohurtado@gmail.com)


<hr />

### Módulos del proyecto



### Pre-requisitos

- Docker (https://www.docker.com/)
- Java 17 (https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

### Ejecutar proyecto (dev)

#### 1. Desplegar cluster kafka:
Navegar hasta la ruta: `\src\main\java\ong\aurora\aan\projector\ksa_projector\kafka`
aqui se encuentra el `docker-compose.yml` configurado para desplegar el cluster kafka, se debe ejecutar el comando:
`docker compose up -d`

#### 2. Variables de Entorno Necesarias

Asegúrate de configurar las siguientes variables de entorno antes de ejecutar la aplicación:

- **AAN_BLOCKCHAIN_FILE_PATH**: Ruta al archivo de eventos de blockchain. Por ejemplo: `node_data/node0/event_store.log`
- **AAN_COMMAND_PORT**: Puerto para comandos. Por ejemplo: `6000`
- **AAN_NETWORK_PORT**: Puerto de red. Por ejemplo: `4000`
- **AAN_NODE_ID**: ID del nodo. Por ejemplo: `node0`
- **AAN_PROJECTOR_PORT**: Puerto del proyector. Por ejemplo: `8000`
- **AAN_NODE_KEY_PRIVATE**: Clave privada del nodo. Por ejemplo: `MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCuiAqv2JWaTLhL
  JrZVtXh5Cim11yBFkZAvcxVC4CQAErf8w9RENRsLN8bFpIqxj4fkljLyrc7wSv9Y
  +ZhMz6NTRkRzZZev26U7J6l0JELJwGoL131NrB88yNAlnWOqz9PNid/gNEBNDw/p
  LWgs4u33c/N1PPTsKmMqDJHA7eEDVZ9pADSmoczMc7dbWwLilp7Jd3cFxGt6o/xE
  0uaYHGApzi0580xrNLWBNMxe4hWAjDShgDPy8NjsSMJSP89FI6J4rLemhRLBtUYa
  gr+YMm4FH8TcEFSFY3LJOJZESXcS4hv3QOg+a8dqKxwdHcxykEoJnet3cY4Lslgl
  AgBSSOhDAgMBAAECggEAAcU0EHPMPmTPrxT8rmQqvjEWe/v9r89YRewVTETuVYEY
  DjHUalIIuSTJmOSUSCQ5I+Q/FiWPeMFD5c1uhIanajgQb8doaPUyi0p+XemBxXKA
  3dJ0aWZsyrCq9PnEGi9zAlq328TQfA//PYJyr37THKIfUR5zpSn+PNe22BnzIQOb
  qrqsVwoI6DA8LuKKY6TQs3kcMoKdw8HkuN5viPS5oB49y7tQYAxIJMELl8EGfREn
  WhVjeXLwCnvaOD8gIejiXlk0fQ5qobrvYE1yX6t/GzLK3viJci4NN57lJGj8+dgf
  vcFVX1ElTLV7+sK6qMhQOMxX7tD7Tx+ZOcn4+DyrQQKBgQDwrOrYkS6ysAW79E85
  q/b9O1yNdD+FjJJXWaueNoasq6KU/Sv06miYVEkReEdLH3SMiPR6YKD1MMfnN2vb
  YPBQwu/cJazFEorsIsrqdOf9qrk2hZc4ryxMzrn4gT+EEurIDwJMD5LGq8vOynhX
  BSSWC4+5egF+FTKWNaBSLd83/QKBgQC5pPTbNEvz2FveG2XP7W5HXgWe0mEknMeF
  MYeDJpUlq9jGt2/Ki57a5/OqTwpgUBDR+v43AwGP7hWQUg7INv2XZZS9zhENOWsP
  9IpZsh8LoR1cOBz22Qywe+sap7z7ceYjuXjSNptelKm1rq/882Qgtnc/cZG4MWGx
  /yrshWn1PwKBgGVVyUQKTQk7iA6NXVZBC6uv9NYyXhSTDYLt5I6nEqldUwU8W+ex
  UvzgcCuE4y/EPMR3XcixKtRSB7lY4nbqKAU5LtkYm9gWaJvoQlvZgVyTJ4zHTta1
  Gwfz+uWhNeccN/KoLyVrYTd5WkTlhynMpozurUNBFaPKtRP93FT9r4DZAoGBAJEF
  Z7IFvH+UuM9dH7+6freJn2JefjyXrNVDx9SQQ1rWT9WMXuwe/c2L/TFB5Z0vbrRa
  GlTnKEVDe9zt/VYyfw/R7swHOhLWk+g25A0FtHLlPYZY32eGTv9fDl4YQu06IMov
  jU7KEb1k7N5cxGYZzFIxuB37nhYBnYvrBaEW55sJAoGAW+a3wm5WrRJbU9kB5Iis
  lN3gk1GiuYHGapxMTY+EieL78w7PkUg4ML3nYlt6eZ1QNjt1OdnEs7KdrnZm2VVK
  8ff1dZKIbYjPfBQye+CKHCgmhJkpsAU7JWDe0FD4Xdnz6g43IibuwBovXuAg1TtX
  B+ucSDkEhIcmsKf00N/fDSo=`
- **AAN_NODE_KEY_PUBLIC**: Clave pública del nodo. Por ejemplo: `MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArogKr9iVmky4Sya2VbV4
  eQoptdcgRZGQL3MVQuAkABK3/MPURDUbCzfGxaSKsY+H5JYy8q3O8Er/WPmYTM+j
  U0ZEc2WXr9ulOyepdCRCycBqC9d9TawfPMjQJZ1jqs/TzYnf4DRATQ8P6S1oLOLt
  93PzdTz07CpjKgyRwO3hA1WfaQA0pqHMzHO3W1sC4paeyXd3BcRreqP8RNLmmBxg
  Kc4tOfNMazS1gTTMXuIVgIw0oYAz8vDY7EjCUj/PRSOieKy3poUSwbVGGoK/mDJu
  BR/E3BBUhWNyyTiWREl3EuIb90DoPmvHaiscHR3McpBKCZ3rd3GOC7JYJQIAUkjo
  QwIDAQAB`


#### 3. Ejecutar proyecto
Se debe ejecutar la clase principal `ANNCore` ubicada en: `\src\main\java\ong\aurora\aan\core`


### Producir eventos

#### Ejecutar comandos por consola

```
docker-compose exec aan-kafka bash -c "kafka-console-producer --bootstrap-server kafka:9092 --topic aurora-aan-commands --property 'parse.key=true' --property 'key.separator=|'"
```

Los comandos deben ingresarse con el siguiente formato: `key|value`. 

Ejemplos: 
```
dca09761-57a0-40f1-95e7-83f60b0c49ee|{"command_timestamp":"2020-11-23T09:12:00.000Z","command_name":"authorization.consume","command_data":{"authorization_id":"ecc8b131-26b7-4723-8943-984e65f155be"}}
```

<small><b>Nota: Puedes encontrar comandos de prueba en el directorio `/ann-kafka/seed-data/aan-command/`</small></b>. 

#### Visualizar comandos por consola

```
docker-compose exec aan-kafka bash -c "kafka-console-consumer --bootstrap-server kafka:9092 --topic aurora-aan-events --property print.timestamp=true --property print.key=true --property print.value=true --from-beginning"
```

#### Visualizar la blockchain por HTTP

```
curl http://localhost:15001/event/all
```

#### Visualizar registros desde el proyector por HTTP


```
# Visualizar todos los registros para una entidad. Reemplazar [entidad] por el identificador de la entidad.
curl http://localhost:15002/[entidad]/all
```

### Limpieza

#### Reiniciar cluster

`docker compose down -v --remove-orphans`

<small><b>Nota: Esto eliminará la información ingresada previamente.</small></b>

### Contribución

Si deseas contribuir o reportar errores contacta con el responsable del proyecto.

