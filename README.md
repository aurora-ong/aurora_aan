
# AURORA-AAN

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

`docker-compose up`

#### 2. Ejecutar procesador  

```
cd /aan-commands/
./gradlew run
```

#### 3. Ejecutar projector

```
cd /aan-projector/`
./gradlew run
```

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

`docker-compose down`

<small><b>Nota: Esto eliminará la información ingresada previamente.</small></b>

### Contribución

Si deseas contribuir o reportar errores contacta con el responsable del proyecto.

