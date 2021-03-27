# Plugin Development


## Enable Debug Logs
### Open Debug Log Settings
* *Help > Diagnostic Tools > Debug Log Settings...*
* Input class names to enable 
    * Preface package with `#`
    * Append `:trace` to class name to enable trace level (warning trace level logging emits credentials)
    * Example  
        * ```
            #com.github.davidsteinsland.postgresvault.Vault:trace
            #com.github.davidsteinsland.postgresvault.VaultAuth:trace
          ```
* Find Log File: *Help > Show Log in Finder*

![Debug Log Settings](./img/Debug%20Log%20Settings.png)
![Configure](./img/Configure%20Debug%20Logs.png)
