# kordex-hybrid-commands

---

## What is kordex-hybrid-commands?

This is a third-party module for the [Kord-Extensions](https://github.com/Kord-Extensions/kord-extensions) command framework which introduces 'hybrid commands.'
These commands are capable of translating to both slash and message commands.

---
## Hybrid Commands Quickstart

### Example Hybrid Command
Let's say you want to have a basic 'ping' command for both command types (slash and message). 
Usually with Kord-Extensions you would write these commands out separately.

```kotlin
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
    autoAckType = AutoAckType.PUBLIC
    
    action {
        publicFollowUp {
            content = "Pong"
        }
    }
}
```

Although this works, having 2 commands that have the same functionality can be harder to keep consistent and manage.
Hybrid Commands solves this by combining the slash and message command dsl builder into one.

You can create a hybrid command using the `hybridCommand` dsl builder in the `Extensions` class.
Below is an example of how a hybrid ping command would be written.
```kotlin
hybridCommand {
    name = "Ping"
    description = "Sends a response back"
    
    action {
        publicFollowUp {
            content = "Pong"
        }
    }
}
```

### Arguments

Just like slash and message commands, hybrid commands also has support for arguments.

```kotlin
class AvatarArgs: Arguments() {
    val user by user("user", "The user's avatar you would like to display")
}

hybridCommand(::AvatarArgs) {
    name = "avatar"
    description = "Displays the mentioned user's avatar"
    
    action {
        // etc
    }
}
```

### Sending Responses
Similar to slash commands, hybrid commands have two ways of responding to a message; `publicFollowUp` and `ephemeralFollowUp`.
Hybrid Commands default to an `autoAckType` of `NONE` and interactions are only acked when either a `publicFollowUp` or `ephemeralFollowUp` is sent.

`publicFollowUp` will send a public follow-up message in the case of a slash command, and a normal message in the case of a message command.

`ephemeralFollowUp` will send an ephemeral follow-up in the case of a slash command, however in the case of a message command, a normal message will be sent. Message commands cannot respond ephemerally to a user.

```kotlin
action { 
    publicFollowUp { 
        content = ""
        
        embed {}
        // etc 
    }
        
    ephemeralFollowUp {
        content = ""
        
        embed {}
        // etc
    }
}
```

If your command does a lot of work before sending a message and needs to be acknowledged before, you can do this by either configuring the slash settings `autoAck` attribute (demonstrated in the next section) or acknowledging it yourself.

In the case of acknowledging the interaction yourself, you will need to check if the context is a `SlashCommandContext`, and then ack it yourself since hybrid commands do not have the concept of acking.

```kotlin
if(context is SlashCommandContext<*>) {
    context.ack(false) // public ack
    context.ack(true) // ephemeral ack
}
```


### Specific Command Features

To configure specific command type features, hybrid commands have a `SlashSettings` and `MessageSettigns` object that can be accessed using the `slashSettings` and `messageSettings` builder.

```kotlin
hybridCommand(::AvatarArgs) {
    name = "avatar"

    messageSettings { aliases = arrayOf("av") }
    slashSettings { autoAck = AutoAckType.PUBLIC /* default: NONE */ } 

    action {
        //etc
    }
}
```

### Sub Commands & Group Commands
Hybrid Commands also support subcommands and group commands. The only rule is that a top level command cannot consist of both group commands and subcommands.

With slash commands, you can't have both sub/group commands along with a command action, but hybrid command allows both.
This will work as expected with message commands, however in the case of a top level slash commands with groups/subcommands, the action will not be registered.

There is a workaround for this problem, you can remap the top-level's command action to a sub command, as demonstrated in the example below.
However, keep in mind that this will only work when your top level command consists of only subcommands.
```kotlin
hybridCommand(::RoleViewArgs /* example purpose */) {
    name = "roles"
    description = "Views the mentioned user's roles"
    
    slashSettings { 
        subCommandName = "view"
        // subCommandDescription - sets the command description for the remapped subcommand
    }
    
    // message command will stay as /roles
    // slash command action will be remapped to /roles view
    action {
        // views a users roles
    }
    
    subCommand(::RoleAddArgs /* example purpose */) {
        name = "add"
        description = "Adds the role to the mentioned user"
        
        action {
            // adds role to user
        }
    }
    
    subCommand(::RoleRemoveArgs /* example purpose */) {
        name = "remove"
        description = "Removes the role from the mentioned user"
        
        action {
            // removes role from user
        }
    }
}
```

The same workaround can be applied to group commands.

```kotlin
hybridCommand {
    name = "command-example"
    description = "example-description"
    
    group {
        name = "group-example"
        
        slashSettings { subCommandName = "remapped" }
        
        // group command action remappped to /command-example group-example remapped
        // message command action stays at {prefix}command-example group-example
        action {
            // do stuff
        }
        
        subCommand {
            name = "example subcommand"
            
            action {
                // do stuff
            }
        }
    }
    
    group {
        // another group command
    }
}
```

If you don't want this behaviour, you can just leave the `subCommandName` as null.

## Pagination

Hybrid commands also support button pagination. They are pretty similar (if not identical) to how `MessageButtonPaginator` and `InteractionButtonPaginator` work.

You can create a new paginator by using the `paginator` dsl builder inside an `HybridCommandContext`

```kotlin
hybridCommand {
    name = "paginator-example"
    
    action {
        val paginator = paginator {
            addPage(yourPageInstance)
            
            addPage(anotherPageInstance)
            
            // etc
        }
        
        paginator.send()
    }
}
```

We also provide a convenient dsl page builder extension function to help with creating your paginators. 
With the page dsl, you can create fields in your page.

```kotlin
paginator {
    page {
        title = "Your First Page"
        description = "Description about the first page"
                
        field("Field 1", inline = true) {
            "Example Field"
        }
    }
    
    page {
        title = "Your Second Page"
        colour = DISCORD_BLURPLE
        // etc
    }
}
```

---
## Documentation

Coming soon...

---
## Installation

### Requirements
You will need to have the [Kord-Extensions](https://github.com/Kord-Extensions/kord-extensions) command framework already installed.

### Gradle (Kotlin)
```kotlin
repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
```
---
```kotlin
dependencies {
    implementation("io.github.qbosst:kordex-hybrid-commands:1.0.0-SNAPSHOT")
}
```

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots" }
}
```
---
```groovy
dependencies {
    implementation "io.github.qbosst:kordex-hybrid-commands:1.0.0-SNAPSHOT"
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>s01-snapshots-repo</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
</repositories>
```
---
```xml
<dependency>
    <groupId>io.github.qbosst</groupId>
    <artifactId>kordex-hybrid-commands</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
