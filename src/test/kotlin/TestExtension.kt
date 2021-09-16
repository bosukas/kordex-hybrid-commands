import com.kotlindiscord.kord.extensions.extensions.Extension
import io.github.qbosst.kordex.commands.hybrid.*
import io.github.qbosst.kordex.entity.EphemeralHybridMessage
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

class TestExtension: Extension() {
    override val name: String get() = "test"

    override suspend fun setup() {
        publicHybridCommand {
            name = "hybrid"
            description = "Tests a hybrid command"

            publicGroupCommand {
                name = "paginator-test"
                description = "Tests the paginator"

                slashCommandSettings {
                    subCommandName = "info"
                    subCommandDescription = "Displays information on what this command is about"
                }

                action {
                    respond { content = "This is a test for the paginator functionality." }
                }

                ephemeralSubCommand {
                    name = "ephemeral"
                    description = "Tests the ephemeral paginator"

                    action {
                        val paginator = respondingPaginator {
                            page { description = "Page 1" }
                            page { description = "Page 2" }
                        }

                        paginator.send()
                    }
                }

                publicSubCommand {
                    name = "public"
                    description = "Tests the public paginator"

                    action {
                        val paginator = respondingPaginator {
                            page { description = "Page 1" }
                            page { description = "Page 2" }
                        }

                        paginator.send()
                    }
                }
            }
        }

        ephemeralHybridCommand {
            name = "hybrid-two"
            description = "Tests another hybrid command"

            slashCommandSettings {
                subCommandName = "hello"
                subCommandDescription = "Greets you!"
            }

            action {
                val msg = respond { content = "Sup!" }

                delay(2000L) // 2 seconds

                msg.edit { content = "It has been two seconds!" }
            }

            ephemeralSubCommand {
                name = "ping"
                description = "Ping command"

                action {
                    val message: EphemeralHybridMessage
                    val timeMs = measureTimeMillis { message = respond { content = "Pinging..." } }
                    message.edit { content = "$timeMs ms" }
                }
            }
        }
    }
}