# Hybrid Commands

![Build Status](https://badgen.net/github/checks/qbosst/kordex-hybrid-commands/main?icon=github&label=build) ![Release](https://badgen.net/maven/v/metadata-url/https/s01.oss.sonatype.org/service/local/repositories/releases/content/io/github/qbosst/kordex-hybrid-commands/maven-metadata.xml?icon=maven&label=release&color=blue&scale=1) ![Snapshot](https://badgen.net/maven/v/metadata-url/https/s01.oss.sonatype.org/service/local/repositories/snapshots/content/io/github/qbosst/kordex-hybrid-commands/maven-metadata.xml?icon=maven&label=snapshot&color=orange)

Hybrid commands are commands that can translate to both slash commands and chat commands. This is useful for when you want to create a slash and chat command that have the same functionality.

## Example Use Case

Let's suppose that you have a basic ping command for the command types; slash and chat, that returns a message back to the user when invoked. Usually with Kord-Extensions you would write these commands out separately.

```kotlin
chatCommand {
    name = "Ping"
    description = "Sends a response back"
    
    action {
        message.reply("Pong")
    }
}

publicSlashCommand {
    name = "Ping"
    description = "Sends a response back"
    
    action {
        respond { content = "Pong" }
    }
}
```

Although this works, having 2 commands for the same purpose can be hard to manage and keep consistent. If you decide to change anything about your command, you will have to do this for both commands. Hybrid Commands solves this by combining the slash and chat command dsl builder into one.

You can create a hybrid command using the `pubicHybridCommand` or `ephemeralHybridCommand` dsl builder in the `Extension` class. Our ping command would now be written like this.

```kotlin
publicHybridCommand {
    name = "Ping"
    description = "Sends a response back"
    
    action {
        respond { content = "Pong" }
    }
}
```

## Arguments

Just like slash and chat commands, hybrid commands have support for arguments.
```kotlin
class AvatarArgs: Arguments() {
    val user by user("user", "The user's avatar you would like to display")
}

publicHybridCommand(::AvatarArgs) {
    name = "avatar"
    description = "Displays the mentioned user's avatar"
    
    action { 
        val user = arguments.user
        // display user avatar
    }
}
```

## Public And Ephemeral Hybrid Commands

Similar to slash commands, hybrid commands two dsl builders; `publicHybridCommand` and `ephemeralHybridCommand`. When a hybrid command gets translated into the slash command equivalent, they will use an `autoAckType` of `NONE` and interactions are only acked when either a `publicFollowUp` or `ephemeralFollowUp` is sent.

A `publicHybridCommand` will send a public follow up in the case of a slash command, and a normal reply message in the case of a message command.

A `ephemeralHybridCommand` will send an ephemeral follow up in the case of a slash command, and a **normal** reply in the case of a message command. Message commands **do not** have support for ephemeral messages.

```kotlin
action { 
    publicHybridCommand {
        name = "public"
        description = "A public hybrid command"
        
        action { 
            respond { 
                content = "This is a public hybrid command"
                
                embed {
                    // etc
                }
            } 
        }
    }
    
    ephemeralHybridCommand {
        name = "ephemeral"
        description = "An ephemeral hybrid command"
        
        action {
            respond { 
                content = "This is an ephemeral hybrid command"
                
                embed { 
                    // etc
                }
            } 
        }
    }
}
```

## Specific Command Features

Slash commands and chat commands have some properties that are not shared and therefore could not be included in the Hybrid Commands builder. To configure these properties, hybrid commands have a `SlashCommandSettings` and `ChatCommandSettings` property that can be accessed using the `slashCommandSettings` and `chatCommandSettings` builder.

```kotlin
hybridCommand(::AvatarArgs) {
    name = "avatar"

    chatCommandSettings { aliases = arrayOf("av") }
    slashCommandSettings { guild( /* your guild id */ ) } 

    action {
        // display user avatar
    }
}
```

## Subcommands & Group Commands

Hybrid commands also have support for subcommands and group commands. The only rule is that a top level hybrid command cannot contain both group commands and subcommands.

With slash commands, you can't have both sub/group commands along with a command action, but hybrid commands allows both. This will work as expected with message commands, however in the case of a top level slash command with group/sub commands, the action will not be registered.

In this example, the only **available slash commands** would be
* /roles add
* /roles remove
```kotlin
publicHybridCommand(::RoleViewArgs) {
    name = "roles"
    description = "Views the mentioned user's roles"
    
    // message command will register this action 
    // slash command will not register this action
    action {
        // views a users roles
    }
    
    publicSubCommand(::RoleAddArgs) {
        name = "add"
        description = "Adds the role to the mentioned user"
        
        action {
            // adds role to user
        }
    }
    
    publicSubCommand(::RoleRemoveArgs) {
        name = "remove"
        description = "Removes the role from the mentioned user"
        
        action {
            // removes role from user
        }
    }
}
```

Luckily, there is a workaround for this problem. Hybrid Commands lets you re-map the command action to a slash sub command, using the `slashCommandSettings` method.

**Available slash commands**
* /roles add
* /roles remove
* /roles view
```kotlin
hybridCommand(::RoleViewArgs /* demonstration purpose */) {
    name = "roles"
    description = "Views the mentioned user's roles"
    
    slashCommandSettings { 
       subCommandName = "view"
       // subCommandDescription - if unset, the subcommand will use the top level command's description
    }
    
    // chat command will register this action 
    // slash command will remap this action to /roles view
    action {
        // views a users roles
    }
}
```

The same workaround can be applied to group commands.

```kotlin
publicHybridCommand {
    name = "example"
    description = "example-description"
    
    publicGroupCommand {
        name = "group"
        
        slashCommandSettings { subCommandName = "remapped" }
        
        // group command action re-mappped to /command group remapped
        // chat command action stays at {prefix}command group
        action {
            // do stuff
        }
        
        publicSubCommand {
            name = "subcommand"
            
            action {
                // do stuff
            }
        }
    }
    
    ephmerealGroup {
        // another group command
    }
}
```

If you don't want this behaviour, you can leave the `subCommandName` as `null` (default).

## Pagination

Hybrid commands also support button pagination. They are pretty similar (if not identical) to how `MessageButtonPaginator` and `InteractionButtonPaginator` work.

You can create a paginator by using the `paginator` dsl builder inside the hybrid command `action`

```kotlin
publicHybridCommand {
    name = "paginator-example"
    description = "Sends a paginator"
    
    action {
        val paginator = respondingPaginator {
            page { /* page 1 */ }
            
            page { /* page 2 */ }
            
            // etc
        }
        
        paginator.send()
    }
}
```

## Installation

You can get the latest version by checking the button at the top of this page.
`1.0.0-SNAPSHOT` -> Uses Kord-Extensions `1.4.2-SNAPSHOT`
`1.0.1-SNAPSHOT` -> Uses Kord-Extensions `1.4.3-SNAPSHOT`
`1.0.2-SNAPSHOT` -> Uses Kord-Extensions `1.4.4-SNAPSHOT`
`1.0.3-SNAPSHOT` -> Uses Kord-Extensions `1.5.0-SNAPSHOT`

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
    implementation("io.github.qbosst:kordex-hybrid-commands:{version}")
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
    implementation "io.github.qbosst:kordex-hybrid-commands:{version}"
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
    <version>{version}</version>
</dependency>
```

## Support

If you have any questions, issues, etc, you can ping me on the Kotlin Discord server `q bosst#2456` or create an issue on the github.

Contributions are also welcome!

Thanks!
