# LoginManager
## ¿Qué es?
LoginManager es un pequeño programa creado para permitir a cualquier persona mantener un seguimiento de sus trabajadores. De esta forma, pueden saber cuándo entran y cuándo salen del trabajo.

Al usar SQL, LoginManager puede mantener un seguimiento organizado y ordenado.

LoginManager es una solución gratuita para pequeñas y medianas empresas que necesitan implementar el registro de jornada que impone el Ministerio de Trabajo de España.

## ¿Cómo se usa?
LoginManager utiliza una interfaz web, de forma que solo hay que conectarse a la página para encontrarse con la interfaz. Ésta es muy simple y directa, con lo que no hay forma de perderse en ella.

### Si se es un trabajador nuevo
Solo hay que poner el DNI. Una vez se ponga (y se pulse fuera de la caja) se deberá poner una contraseña (junto con su confirmación). Es recomendable que esta contraseña no sea utilizada por ningún otro servicio, por si acaso.

Luego de eso solo es necesario pulsar en el botón de entrada o salida. En el caso en que ponga que no se pudieron leer las credenciales, es altamente probable que sea porque el tabajador no está añadido a la tabla `Empleados`. Este error se puede ignorar.

### Si se es un trabajador existente
Solo hace falta poner el DNI y la contraseña, y alguna observación en caso de ser necesario.

## Requisitos
LoginManager necesita el siguiente software para poder funcionar:
- Un dispositivo Windows donde funcionar.
  - *Opcionalmente puede funcionar en Linux si se usa [Wine](https://www.winehq.org/). Su funcionamiento no está garantizado.*
- Java 21 (preferiblemente de [Eclipse Adoptium](https://adoptium.net/es/temurin/releases?version=21&os=any&arch=any)).
- [MySQL](https://dev.mysql.com/).

## Posibles problemas
### Cifrado
Si bien las credenciales están cifradas para hacer más complicado el acceso a estas, el cifrado usado no es el más robusto del mercado. Sin embargo, el descifrado manual de las credenciales supone saber lo que se hace, con lo cual yo ([@RocketSmash9000](https://github.com/RocketSmash9000)) no me hago cargo de posibles errores al manipular indebidamente estos archivos.

### Soporte para Linux
LoginManager puede usarse en un servidor Linux, pero dado que hay muchas formas de instalar paquetes y una gran cantidad de distribuciones, no todas tienen la misma forma de ejecutar MySQL. De esta forma, es altamente complicado crear un programa lo suficientemente robusto como para incluir todas y cada una de las formas de comprobar que MySQL está funcionando. Han habido intentos de crear una conexión con Linux, pero ha demostrado tomar más tiempo del que un único desarrollador puede dar. Es por esta razón que no habrá soporte oficial para Linux.

## Servicios opcionales
### QueryManager
LoginManager no implementa una interfaz para administrar y organizar las entradas y salidas, con lo cual es necesario usar la línea de comandos de MySQL o su interfaz gráfica. Como alternativa, existe QueryManager, el cual permite ver las entradas y salidas de los trabajadores, filtrar por días y trabajador, y administrar y añadir nuevos trabajadores. Esta aplicación es de código cerrado y de pago, con lo cual es necesario contactar conmigo ([@RocketSmash9000](https://github.com/RocketSmash9000)) a través de correo elecrónico (RocketSmash@proton.me) para discutir su precio. El pago es único y garantiza acceso a la versión más reciente a la hora del pago y sus versiones posteriores.

### Instalación
En caso de no querer instalar LoginManager (o QueryManager, o los dos) de forma manual, yo ([@RocketSmash9000](https://github.com/RocketSmash9000)) ofrezco su instalación. En caso de ser de interés, será necesario contactar conmigo a través de correo electrónico (RocketSmash@proton.me).

### Personalización
En caso de necesitar funciones específicas que LoginManager o QueryManager no ofrecen, es posible contactar conmigo por correo electrónico (RocketSmash@proton.me) para crear un programa que se adapte a todas las necesidades. Esta personalización deberá pagarse y su precio se verá alterado según la cantidad de cambios a realizar. Las versiones personalizadas son completamente privadas y no se publicarán en ninguna plataforma.
