# UPGRADE - Proceso de actualización entre versiones

*Para actualizar de una versión a otra es suficiente con actualizar el WAR a la última versión. El siguiente listado presenta aquellos cambios de versión en los que no es suficiente con actualizar y que requieren por parte del instalador tener más cosas en cuenta. Si el cambio de versión engloba varios cambios de versión del listado, estos han de ejecutarse en orden de más antiguo a más reciente.*

*De esta forma, si tuviéramos una instalación en una versión **A.B.C** y quisieramos actualizar a una versión posterior **X.Y.Z** para la cual existan versiones anteriores que incluyan cambios listados en este documento, se deberá realizar la actualización pasando por todas estas versiones antes de poder llegar a la versión deseada.*

*EJEMPLO: Queremos actualizar desde la versión 1.0.0 a la 3.0.0 y existe un cambio en la base de datos en la actualización de la versión 1.0.0 a la 2.0.0.*

*Se deberá realizar primero la actualización de la versión 1.0.0 a la 2.0.0 y luego desde la 2.0.0 a la 3.0.0*

## 2.9.5 a 2.9.6

  * Se corrige error que impedía crear ficheros como parámetros en ETLs de Apache Hop.
  * Se corrige error que impedía migrar ETLs de PDI a Apache Hop cuando estas tienen ficheros como parámetros.
  
## 2.9.4 a 2.9.5

  * Se corrige error de ejecución de ETL en CoETL, cuando la plataforma de ejecución es Apache Hop y la ETL tiene varias configuraciones de un mismo grupo en el metadata.
  * Se corrige error de ETL que quedan en estado de ejecución.
  * Se añade un campo de configuración de ETL que permite establecer el nivel de log que debe generarse en la ejecución de la ETL.

## 2.9.3 a 2.9.4

  * Se modifica la ejecución de los jobs de control de estado de ejecución para evitar eliminar la ejecución en la plataforma sin haber cambiado primero el estado de la misma.
  * Se corrige error por el cual el job de Pentaho podía lanzar ETLs de Apache Hop.
  * Se corrige la obtención de emails de usuarios para el envío de aviso de error para que discrimine los usuarios dado de baja.
  * Se parametriza el nivel de log para las ejecuciones de ETLs

## 2.9.2 a 2.9.3

  * Se corrige la asignación del parámetro ETL_RESOURCES en caso de cambiar de plataforma de ejecución.
  * Se realiza trim al código de la ETL para evitar que esta contenga espacios en blanco.

## 2.9.1 a 2.9.2

  * Se corrige la generación de la conección a base de datos en el metadata_json que necesita hop para ejecutar las ETL.
  
## 2.9.0 a 2.9.1

## 2.8.0 a 2.9.0

  * Crear nuevas propiedades en el application.yml:
    - apache-hop.hopFolder: Directorio de instalación de Apoache hop en la máquina
    - apache-hop.variables: XML plantilla de las variables de Apache Hop. Puede usarse la siguiente propuesta: "<variables><variable><name>HOP_AUTO_CREATE_CONFIG</name><value>Y</value></variable><variable><name>HOP_METADATA_FOLDER</name><value>${ETL_RESOURCES}/metadata</value></variable><variable><name>HOP_PROJECT_NAME</name><value>${ETL_CODE}</value></variable><variable><name>PROJECT_HOME</name><value>${HOP_FOLDER}/config/projects/${ETL_CODE}</value></variable></variables>"

## 2.7.1 a 2.8.0

  * Con la nueva versión se requiere de que la infraestructura tenga habilitada un servidor de Apache Hop para la ejecución de ETLs con esta plataforma.

## 2.7.0 a 2.7.1

  * Se realiza una mejora en la definición de la relaciones de entidades entre usuario-rol-organismos.
  * Se normaliza el endpoint de Platino en PRE y EXP.

## 2.6.1 a 2.7.0

  * Se añade la nueva gestión de usuarios por rol-organismo-etl.
  * Se corrigen errores detectados en la configuración de roles.

## 2.5.0 a 2.6.1

  * Se añade la posibilidad de crear parámetros "FILE" para incluir ficheros csv, txt, json y xml con un tamaño máximo de 50MB.
  * Se corrigen vulnerabilidades críticas de librerías.

## 2.4.1 a 2.5.0

  * Se añade la posibilidad de crear parámetros "FILE" para incluir ficheros csv, txt, json y xml con un tamaño máximo de 50MB.

## 2.4.0 a 2.4.1

  * Se elimina indice duplicado en la tabla usuario sobre el campo login creado en el fichero 00000000000000_initial_schema.xml

  * Las ETL con una ejecución programada sin periodicidad, es decir, la expresión cron no se ejecuta varios días, al tratar de obtener la siguiente fecha de ejecución al ser un valor null da error la conversión del dato.

  * Trim al crear parámetros para controlar que no se creen con espacios en blanco. La página de parámetros globales no tiene paginación y no se ven todos los valores.

## 2.3.3 a 2.4.0

  * Crear nueva propiedad en el application.yml y asignarle el nombre del entorno. La información del entorno en los emails se deshabilita para el entorno PRODUCTION
    - application.enviroment
    
  * Desplegar el .war de la forma habitual

## 2.3.2 a 2.3.3

  * Desplegar el .war de la forma habitual


## 2.3.1 a 2.3.2

  * Desplegar el .war de la forma habitual


## 2.3.0 a 2.3.1

  * Desplegar el .war de la forma habitual


## 2.2.0 a 2.3.0

  * Desplegar el .war de la forma habitual


## 2.1.0 a 2.2.0

  * El despliegue normal del .war realiza los cambios necesarios


## 2.0.1 a 2.1.0

* Se modifica la gestión de permisos para establecerlo de acuerdo al rol y al organismo.
* El despliegue normal del .war realiza los cambios necesarios

## 2.0.0 a 2.0.1

* Dado que ha cambiado la estructura de algunas tablas de la base de datos, la aplicación deja de ser compatible con la versión que actualmente existe.
  * Ahora, la estructura que deberán tener los usuarios para usar la aplicación es diferente, ya que un usuario deberá tener, en función del rol que ocupe, asignado al menos 1 organismo.
  * Se ha eliminado la antigua tabla de usurio_rol

## 1.1.2 a 2.0.0
* Rotura de compatibilidad
** A partir de esta versión de la aplicación se incorpora un repositorio GIT para la obtención de las ETL, se eliminan las propiedades LDAP y se incorporan de parámetros globales para las ETL.

* Cambios en las propiedades del fichero application-env.yml. Son los siguientes:
  * ⚠️ Las nuevas propiedades que sean obligatorias se marcarán con un #Required. Esto significa que deberán tener un valor para que la aplicación funcione correctamente 
  * Se debe eliminar cualquier propiedad referente al LDAP.
    - ldap.url
    - ldap.username
    - ldap.password
    - ldap.base
    - ldap.searchUsersProperty
    
  * Se deben añadir nuevas propiedades referentes a datos de acceso al GIT dónde se alojen las ETL:
    - git.username : FILL_WITH_GIT_USER # Required: GIT owner username for access
    - git.password : FILL_WITH_GIT_PASSWORD # Required: GIT owner password for access
    - git.password : FILL_WITH_BRANCH # Required: GIT ETL branch for ETL execution
    - pentaho.host.mainResourcePrefix : # Required: Pentaho main resources prefix
    
  * Se deben añadir nuevas propiedades para la encriptación y desencriptación de los parámetros de una ETL que sean de tipo password:  
    - aes.secretKeySalt : FILL_SECRET_KEY_SALT # Required: secret key salt fr generated KeySpec
    - aes.secretKeyPassword : FILL_SECRET_KEY_PASSWORD # Required: For example application name COETL_GOBCAN

## 0.0.0 a 1.1.2

* El proceso de actualizaciones entre versiones para versiones anteriores a la 1.1.2 está definido en el fichero README.md.
