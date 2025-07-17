# LoginManager
## ¿Qué es?
LoginManager es un pequeño programa escrito en Java que permite a cualquier persona mantener un seguimiento de sus trabajadores. De esta forma, pueden saber cuándo entran y cuándo salen del trabajo.

Al usar SQL, LoginManager puede mantener un seguimiento organizado y ordenado.

## ¿Cómo se usa?
LoginManager utiliza una interfaz web, de forma que solo hay que conectarse a la página para encontrarse con la interfaz. Ésta es muy simple y directa, con lo que no hay forma de perderse en ella.

Simplemente, hay que insertar el DNI y pulsar en el botón correspondiente según si el trabajador está entrando o saliendo. Esto creará un registro en la base de datos al instante.

## Dependencias
LoginManager necesita lo siguiente para poder funcionar adecuadamente:
- Windows
- Java 21 (preferiblemente de Eclipse Adoptium)
- MySQL

## Posibles problemas
LoginManager no usa encriptación en sus conexiones, con lo que cualquier persona interceptar la información enviada. Sin embargo, este solo admite conexiones desde los dispositivos conectados en la misma red que el dispositivo que corre LoginManager, con lo que en redes cerradas donde todos los dispositivos conectados son de trabajadores, no debería haber problema.

Las credenciales de acceso a la base de datos tampoco está encriptada, pero el archivo que las contiene está únicamente guardado en el ordenador en el que LoginManager está funcionando. Además, este archivo solo se lee una vez. Así que ganar acceso a la base de datos es complicado.
