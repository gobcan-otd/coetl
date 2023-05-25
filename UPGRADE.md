# UPGRADE - Proceso de actualización entre versiones

*Para actualizar de una versión a otra es suficiente con actualizar el WAR a la última versión. El siguiente listado presenta aquellos cambios de versión en los que no es suficiente con actualizar y que requieren por parte del instalador tener más cosas en cuenta. Si el cambio de versión engloba varios cambios de versión del listado, estos han de ejecutarse en orden de más antiguo a más reciente.*

*De esta forma, si tuviéramos una instalación en una versión **A.B.C** y quisieramos actualizar a una versión posterior **X.Y.Z** para la cual existan versiones anteriores que incluyan cambios listados en este documento, se deberá realizar la actualización pasando por todas estas versiones antes de poder llegar a la versión deseada.*

*EJEMPLO: Queremos actualizar desde la versión 1.0.0 a la 3.0.0 y existe un cambio en la base de datos en la actualización de la versión 1.0.0 a la 2.0.0.*

*Se deberá realizar primero la actualización de la versión 1.0.0 a la 2.0.0 y luego desde la 2.0.0 a la 3.0.0*

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
