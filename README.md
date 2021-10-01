# AdNovum Bootstrap Gradle Plugin

Bootstrap your build environment with a custom shell script to downloading build tools and set environment variables.

The generated `initEnv` script can also be executed to run commands in the bootstrapped environment:

```
./initEnv <cmd>
```


## Add bootstrapping to your project

1. [Apply the plugin](https://plugins.gradle.org/plugin/ch.adnovum.bootstrap)
2. Configure the plugin
   ```
   bootstrap {
     bootstrapScriptUrl = 'https://<your-script-url>.sh'
     bootstrapScriptDir = '$APP_HOME'
   }
   ```
3. Execute `gw wrapper`
4. Run your build once (e.g. `gw clean` or `gw assemble`). This will initiate the download of the script you've specified with the `bootstrapScriptUrl` configuration property.
   With the configuration above, the script will be downloaded to the project directory.
5. Commit the script(s) to your Git repository.

From now on, your build is bootstrapped!


## Create your bootstrap script

The Bootstrap script will called from `gradlew` before your Gradle build starts and its standard output will be evaluated.
With this, your script can download any tooling you need and by printing `KEY=VALUE` lines it can also modify environment variables for the build.


### Variations

- If you leave out the `bootstrapScriptDir` configuration parameter, it will be downloaded into a cache folder in your `$HOME` which can be customized with the `bootstrapScriptDir` variable.
- `bootstrapScriptUrl` can also point to a simple filename instead of an URL, for example, `bootstrapScriptUrl = 'myscript.sh'`. In this case you have to add the specified script to the project root folder. 

