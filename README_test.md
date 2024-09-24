# Ejemplo de DAO para JPA y uso de JUNIT (SI-2024, semana 2)

Ejemplo de DAO (_Data Access Object_) genérico para JPA

Ejemplo de uso de JUnit5 para _testing_ simple del DAO JPA


**Nota:** En los equipos de laboratorio es conveniente establecer la variable de entorno JAVA_PATH, para que el comando `mvn` (Maven) compile y ejecute los proyectos siempre con el mismo JDK.

 ```sh
 export JAVA_HOME=/usr/lib/jvm/jdk-20
    
 export PATH=$JAVA_HOME/bin:$PATH
 ```


## DAO JPA Genérico

### Crear interface y clase del DAO

Crear el directorio para el paquete `daos` y copiar los ficheros Java con la definición de interfaces y clases de implementación.
```sh
mkdir -p src/main/java/es/uvigo/mei/pedidos/daos
cd src/main/java/es/uvigo/mei/pedidos/daos

wget https://raw.githubusercontent.com/esei-si-dagss/pedidos-persistencia-24/main/src/main/java/es/uvigo/mei/pedidos/daos/{GenericoDAO,GenericoDAOJPA}.java
wget https://raw.githubusercontent.com/esei-si-dagss/pedidos-persistencia-24/main/src/main/java/es/uvigo/mei/pedidos/daos/{ClienteDAO,ClienteDAOJPA}.java
wget https://raw.githubusercontent.com/esei-si-dagss/pedidos-persistencia-24/main/src/main/java/es/uvigo/mei/pedidos/daos/{PedidoDAO,PedidoDAOJPA,PedidosException}.java

pushd
```

#### Aspectos a revisar
1. Comprobar el interface _GenericoDAO_  con la definición de un DAO genérico (parametrizando la clase de la entidad y la de la clave) y su implementación _GenericoDAOJPA_ (recibe un _EntityManager_ en el constructor)
2. Comprobar los interfaces _ClienteDAO_ y _PedidoDAO_ 
	* Heredan de _GenericoDAO_ especificando los tipos correspondientes de entidad y clave
	* Añaden métodos adicionales específicos para el entidad
3. Revisar las  implementaciones de estos interfaces:
	* _ClienteDAOJPA_: hereda de _GenericoDAOJPA_ e implementa _ClienteDAO_
	* _PedidoDAOJPA_: hereda de _GenericoDAOJPA_ e implementa _PedidoDAO_
4. Comprobar en _PedidoDAOJPA_ la gestión de la relación 1:N con _LineaPedido_



## Uso de JUnit5

### Declarar dependencias en `pom.xml`

#### Añadir dependencia de JUnit5 con _scope_ `test`
```
...
        <dependency>
           <groupId>org.junit.jupiter</groupId>
           <artifactId>junit-jupiter</artifactId>
           <version>5.11.0</version>
           <scope>test</scope>
        </dependency>
...
```

#### Añadir dependencia de la BD en memoria H2 con _scope_ `test` 
```
...	
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.3.232</version>
            <scope>test</scope>
        </dependency>
...
```

**Importante:** La versión del plugin `maven-surefire-plugin` responsable de la fase `test` de MAVEN debe igual o superior a 3.0.0 para poder usar Junit 5 (ver https://maven.apache.org/surefire/maven-surefire-plugin/featurematrix.html)

Añadir al final del `pom.xml`  (dentro de `<build><pluginManagement><plugins>`) la configuración para usar la última versión de este plugin.

```xml
...
  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.2.2</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```


### Crear `persistence.xml` para pruebas con conexión a BD H2.

```sh
mkdir -p src/test/resources/META-INF/
nano src/test/resources/META-INF/persistence.xml
```

Contenido

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd"
             version="3.1">

  <persistence-unit name="pedidos_test_PU" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
       
    <class>es.uvigo.mei.pedidos.entidades.Almacen</class>
    <class>es.uvigo.mei.pedidos.entidades.Articulo</class>
    <class>es.uvigo.mei.pedidos.entidades.ArticuloAlmacen</class>
    <class>es.uvigo.mei.pedidos.entidades.Cliente</class>
    <class>es.uvigo.mei.pedidos.entidades.Familia</class>
    <class>es.uvigo.mei.pedidos.entidades.LineaPedido</class>
    <class>es.uvigo.mei.pedidos.entidades.Pedido</class>
    
    <properties>
      <property name="jakarta.persistence.jdbc.url"
                value="jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=false;" />
      <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver" />
      <property name="jakarta.persistence.schema-generation.database.action" value="drop-and-create"/>
      
      <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
      <property name="hibernate.cache.provider_class" value="org.hibernate.cache.NoCacheProvider"/>
      <property name="hibernate.show_sql" value="true"/>
      <property name="hibernate.format_sql" value="true"/>
    </properties>
  </persistence-unit>
</persistence>
```



### Crear una clase de Test para _GenericoDAO_

```sh
mkdir -p src/test/java/es/uvigo/mei/pedidos/daos/
cd src/test/java/es/uvigo/mei/pedidos/daos/

wget https://raw.githubusercontent.com/esei-si-dagss/pedidos-persistencia-24/main/src/test/java/es/uvigo/mei/pedidos/daos/GenericoDAOJPATest.java

pushd
```

Inspeccionar el código de los tests de la clase _GenericoDAOJPATest_

### Ejecutar los tests

```sh
mvn clean
mvn test
```

