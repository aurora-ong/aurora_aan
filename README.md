### Para desplegar utilizar: 

docker-compose up

### Para producir eventos al tópico de comandos ingresar al contenedor de kafka y ejecutar

`kafka-console-producer --bootstrap-server kafka:9092  --topic aurora-aan-commands --property 'parse.key=true' --property 'key.separator=|'`
 

### Para ver los eventos procesados ingresar al contenedor de kafka y ejecutar
  
`kafka-console-consumer --bootstrap-server localhost:9092 --topic aurora-aan-events --property print.timestamp=true --property print.key=true --property print.value=true --from-beginning`
 

### Producir comandos de prueba

`kafka-console-producer --bootstrap-server kafka:9092 --topic aurora-aan-commands --property 'parse.key=true' --property 'key.separator=|' < aan-command-seed-[entidad].json`
