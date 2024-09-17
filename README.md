# Ejemplo MAVEN y JPA Básico  (SI-2024, semana 1)

* Ejemplo de creación de proyectos Maven y uso desde línea de comandos
* Ejemplo de mapeo JPA


## 1. PREVIO
### Requisitos previos

* Servidor de BD MySQL
* Maven (versión > 3.5.x)
* (opcional) GIT
* (opcional) IDE Java (Eclipse, Netbeans, IntelliJ, VS Code)

**Nota:** En los equipos de laboratorio, es conveniente establecer la variable de entorno JAVA_PATH, para que el comando `mvn` (Maven) compile y ejecute los proyectos siempre con el mismo JDK.

 ```sh
 export JAVA_HOME=/usr/lib/jvm/openjdk-20
 
 export PATH=$JAVA_HOME/bin:$PATH
 ```

 

Crear BD para los ejemplos

* Crear BD "pruebas_si" en MySQL

```sh
mysql -u root -p    [pedirá la contraseña de MySQL]

mysql> create database pruebas_si;
mysql> create user si@localhost identified by "si";
mysql> grant all privileges on pruebas_si.* to si@localhost;
```

Adicionalmente, puede ser necesario establecer un formato de fecha compatible
```
mysql> set @@global.time_zone = '+00:00';
mysql> set @@session.time_zone = '+00:00';
```



## 2. CREAR Y CONFIGURAR PROYECTO MAVEN
### Crear un proyecto Maven usando el arquetipo `maven-archetype-quickstart` 
```sh
mvn archetype:generate -DgroupId=es.uvigo.mei \
                         -DartifactId=pedidos-persistencia \
                         -Dversion=1.0 \
                         -Dpackage=es.uvigo.mei.pedidos\
                         -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4
                         
cd pedidos-persistencia                         
```

* Comprobar la estructura de directorios creada con `tree pedidos-persistencia` ó `ls -lR pedidos-persistencia`
* Ajustar el archivo `pom.xml`generado (el arquetipo `maven-archetype-quickstart` establece por defecto plugins de versiones algo antiguas)
	1. Ajustar la versión de Java a utilizar a, al menos, Java 11 [la versión de Hibernate utilizada requiere Java 11 o superior]. También puede omitirse la especificación de versiones y usar la que esté instalada por defecto.
    ```xml
      <properties>
        <maven.compiler.target>11</maven.compiler.target>
        <maven.compiler.source>11</maven.compiler.source>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>
    ```
    2. Eliminar el contenido del elemento `<dependencies>` (manteniendo las etiquetas externas que se rellenarán en el siguiente paso)
    3. Eliminar completamente la sección `<build>` (incluyendo contenido y etiquetas externas). Por defecto, se declaran versiones anticuadas de los plugins.
* Eliminar las clases de ejemplo incluidas en el arquetipo maven
```sh
rm src/main/java/es/uvigo/mei/pedidos/App.java

rm src/test/java/es/uvigo/mei/pedidos/AppTest.java
```

## 3. CONFIGURACIÓN PARA JPA
### Declarar las dependencias necesarias en `pom.xml`
1 Declarar el uso de Hibernate como _provider_ JPA (ver. 6.6.0.Final) dentro de `<dependencies>...</dependencies>`

```xml
<project>
   ...
   <dependencies>
      <dependency>
         <groupId>org.hibernate.orm</groupId>
         <artifactId>hibernate-core</artifactId>
         <version>6.6.0.Final</version>
      </dependency>
      ...
   </dependencies>
</project>
```
El resto de dependencias necesarias para `hibernate-core` serán descargadas e instaladas por Maven.

2 Declarar el _connector_ JDBC para MySQL (ver. 9.0.0) dentro de `<dependencies>...</dependencies>`

```xml
<project>
   ...
   <dependencies>
      ...
      <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.0.0</version>
      </dependency>
   </dependencies>
</project>
```

## 4. MODELO E-R
![Modelo E-R](doc/modeloER_pedidos.png?raw=true "Modelo E-R del ejemplo")

## 5. AÑADIR ENTIDADES

Crear el directorio para el paquete `entidades` y copiar los ficheros Java con la definición de las entidades (disponibles en [https://github.com/esei-si-dagss/pedidos-persistencia-24/tree/main/src/main/java/es/uvigo/mei/pedidos/entidades](https://github.com/esei-si-dagss/pedidos-persistencia-24/tree/main/src/main/java/es/uvigo/mei/pedidos/entidades))

```sh
mkdir -p src/main/java/es/uvigo/mei/pedidos/entidades

    (por ahora no se crearán tests unitarios con JUnit)

cd src/main/java/es/uvigo/mei/pedidos/entidades

wget https://raw.githubusercontent.com/esei-si-dagss/pedidos-persistencia-24/main/src/main/java/es/uvigo/mei/pedidos/entidades/{Articulo,Almacen,ArticuloAlmacen,ArticuloAlmacenId,Familia,Direccion,Cliente,Pedido,LineaPedido,EstadoPedido}.java

pushd
```

### Aspectos a revisar
1. Salvo en el caso de la entidad `Cliente`, que usa como clave primaria su DNI, las demás entidades usan claves autogeneradas de tipo `GenerationType.IDENTITY` (necesario para  mapear un atributo autoincremental de MySQL)
2. Salvo para la relación entre `Pedido` y su entidad débil `LineaPedido`, nos hemos limitado a relaciones unidireccionales.
    * Para asegurar que ambos extrmos de esa relación bidireccional se mantienen consistentes, se  incluyen los métodos `anadirLineaPedido()` y `anadirLineaPedidoInterno()` en `Pedido`, que se coordinan con el método `setPedido()`de `LineaPedido`para que en todo momento los dos lados de la relación sean correctos.
    * Más detalles en [jpa-implementation-patterns-bidirectional-assocations](https://xebia.com/blog/jpa-implementation-patterns-bidirectional-assocations/) y [Object_corruption,_one_side_of_the_relationship_is_not_updated_after_updating_the_other_side](https://en.wikibooks.org/wiki/Java_Persistence/Relationships#Object_corruption,_one_side_of_the_relationship_is_not_updated_after_updating_the_other_side)
  
3. La relación N:M entre `Articulo` y `Almacen` tiene un atributo propio, `stock`, por lo que se ha modelado esa relación como `@Entity`, empleando dos relaciones unidireccionales `@ManyToOne` hacia `Articulo` y `Almacen`.
    * Para gestionar el campo clave multiatributo se usa la clase auxiliar `ArticuloAlmacenId`, vinculada a la entidad que soporta la relación con atributos mediante la anotación`@IdClass`.
    * Más detalles en [Mapping_a_Join_Table_with_Additional_Columns](https://en.wikibooks.org/wiki/Java_Persistence/ManyToMany#Mapping_a_Join_Table_with_Additional_Columns)

### Configurar Persistence Unit de JPA

Definir fichero `persistence.xml` en `src/main/resources/META-INF`

```
mkdir -p src/main/resources/META-INF
nano src/main/resources/META-INF/persistence.xml
```

Contenido a incluir
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd"
             version="3.1">
  <persistence-unit name="pedidos_PU" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    
    <class>es.uvigo.mei.pedidos.entidades.Almacen</class>
    <class>es.uvigo.mei.pedidos.entidades.Articulo</class>
    <class>es.uvigo.mei.pedidos.entidades.ArticuloAlmacen</class>
    <class>es.uvigo.mei.pedidos.entidades.Cliente</class>
    <class>es.uvigo.mei.pedidos.entidades.Familia</class>
    <class>es.uvigo.mei.pedidos.entidades.LineaPedido</class>
    <class>es.uvigo.mei.pedidos.entidades.Pedido</class>
    
    <properties>
      <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/pruebas_si?serverTimezone=UTC"/>
      <property name="javax.persistence.jdbc.user" value="si"/>
      <property name="javax.persistence.jdbc.password" value="si"/>
      <property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver"/>
      <property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
      
      <property name="hibernate.dialect" value="org.hibernate.dialect.MySQL8Dialect"/>
      <property name="hibernate.cache.provider_class" value="org.hibernate.cache.NoCacheProvider"/>
      <property name="hibernate.show_sql" value="true"/>
      <property name="hibernate.format_sql" value="true"/>
    </properties>
  </persistence-unit>

</persistence>
```



## 6. PROBAR EL PROYECTO
### Añadir clases con "main()" de ejemplo
```sh
cd src/main/java/es/uvigo/mei/pedidos/

wget https://raw.githubusercontent.com/esei-si-dagss/pedidos-persistencia-24/main/src/main/java/es/uvigo/mei/pedidos/Main.java

pushd
```

#### Aspectos a revisar
1. Se crea un _EntityManagerFactory_ al que se le pedirá la creación de los respectivos _EntityManager_ con los que se realizarán las operaciones sobre la base de datos
2. Las operaciones realizadas por el _EntityManager_ sobre la base de datos se realizan dentro de una _transacción_ proporcionada por el _EntityEmanager_.
3. La transación es iniciada con `tx.begin()` y completada con éxito al hacer `tx.commit()`. En caso  de que durante la ejecución de esas acciones se lanze alguna excepcion, se ejecuta `tx.rollback()` para omitir los cambios realizados.
4. Dado que en `persistence.xml` se ha indicado la opción `drop-and-create`, en cada ejecución de estas clases de ejemplo, se eliminan los contenidos de la base de datos y se crea una nueva vacía.

### Ejecutar clase `Main` proporcionada
```sh
mvn package
mvn exec:java -Dexec.mainClass="es.uvigo.mei.pedidos.Main"
```

* Comprobar el estado de la BD: tablas creadas y contenido de las mismas

```
mysql -u si -p   <con contraseña si>

mysql > use pruebas_si;
mysql > show tables;
mysql > describe Articulo;                # los nombres de las tablas pueden ser diferentes
mysql > describe Pedido;
mysql > select * from Articulo;
mysql > select * from Pedido;
mysql > ...

```



## 7. TAREA EXTRA
* Comprobar la importación y uso del proyecto creado desde el IDE Java empleado habitualmente.

## 8. Proyecto final resultante

