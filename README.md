# kordex-hybrid-commands

This is a module for the [Kord-Extensions](https://github.com/Kord-Extensions/kord-extensions) command framework that allows for creating 'hybrid commands', which are just commands that translate to both, message and slash commands.
Using hybrid commands will help prevent code duplication as you do not need to write logic twice for both command types.


# Using Hybrid Commands
You can create a new command using this syntax in an extension
```kt
hybridCommand {
    name = "Ping"
    description = "Sends a response back"
    
    action {
        message.reply("Pong")
    }
}
```

The example above is the equivelant of writing...

```kt
command {
    name = "Ping"
    description = "Sends a response back"
    
    action {
        message.reply("Pong")
    }
}

slashCommand {
    name = "Ping"
    description = "Sends a response back"
    
    action {
        message.reply("Pong")
    }
}
```

# Installation

Coming soon...
