# LoginManager
## ¿Qué es?
LoginManager es un pequeño programa escrito en Java que permite a cualquier persona mantener un seguimiento de sus trabajadores. De esta forma, pueden saber cuándo entran y cuándo salen del trabajo.

Al usar SQL, LoginManager puede mantener un seguimiento organizado y ordenado.

## ¿Cómo se usa?
LoginManager utiliza una interfaz web, de forma que solo hay que conectarse a la página para encontrarse con la interfaz. Ésta es muy simple y directa, con lo que no hay forma de perderse en ella.

### Si se es un trabajador nuevo
Solo hay que poner el DNI. Una vez se ponga (y se pulse fuera de la caja) se deberá poner una contraseña (junto con su confirmación). Es recomendable que esta contraseña no sea utilizada por ningún otro servicio, por si acaso.

Luego de eso solo es necesario pulsar en el botón de entrada o salida. Si no pone que se ha registrado la entrada o salida, sino que aparece algo distinto, es recomendable refrescar la página y volver a rellenar los campos necesarios.

### Si se es un trabajador existente
Solo hace falta poner el DNI y la contraseña, y alguna observación en caso de ser necesario.

## Dependencias
LoginManager necesita lo siguiente para poder funcionar adecuadamente:
- Un dispositivo Windows donde funcionar.
- Java 21 (preferiblemente de [Eclipse Adoptium](https://adoptium.net/es/temurin/releases?version=21&os=any&arch=any)).
- [MySQL](https://dev.mysql.com/).

## Posibles problemas
Si bien las credenciales están cifradas para hacer más complicado el acceso a estas, el cifrado usado no es el más robusto del mercado. Sin embargo, el descifrado manual de las credenciales supone saber lo que se hace, con lo cual yo ([@RocketSmash9000](https://github.com/RocketSmash9000)) no me hago cargo de posibles errores al manipular indebidamente estos archivos.

## Servicios opcionales
LoginManager no implementa una interfaz para administrar y organizar las entradas y salidas, con lo cual es necesario usar la línea de comandos de MySQL o su interfaz gráfica. Como alternativa, existe QueryManager, el cual permite ver las entradas y salidas de los trabajadores, filtrar por días y trabajador, y administrar y añadir nuevos trabajadores. Esta aplicación es de código cerrado y de pago, con lo cual es necesario contactar conmigo ([@RocketSmash9000](https://github.com/RocketSmash9000)) a través de correo elecrónico (RocketSmash@proton.me) para discutir su precio. El pago es único y garantiza acceso únicamente a la versión más reciente en el momento del pago.

En caso de no querer instalar LoginManager (o QueryManager, o los dos) de forma manual, yo ([@RocketSmash9000](https://github.com/RocketSmash9000)) ofrezco su instalación. En caso de ser de interés, será necesario contactar conmigo a través de correo electrónico (RocketSmash@proton.me).
