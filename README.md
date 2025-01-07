# JEMultiverse

JEMultiverse is a comprehensive plugin designed to manage multiple worlds within a Minecraft server environment. It leverages the power of JPA and Hibernate for database interactions and provides a robust API for world management, teleportation, and configuration.

## Features

- **World Management**: Create, delete, and easily manage multiple worlds.
- **Caching**: Utilizes Caffeine for efficient caching of world data.
- **Asynchronous Operations**: Supports asynchronous operations using `CompletableFuture` for non-blocking interactions.
- **Teleportation**: Provides seamless teleportation capabilities between worlds.
- **Configuration Management**: Easily configure and manage world settings through YAML files.
- **User Interface**: A user-friendly UI for world creation and editing is offered.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 16 or higher
- Maven or Gradle for dependency management
- A database supported by Hibernate

### Installation

Add the following dependency to your `pom.xml` if you are using Maven:

```xml
<!-- This will be added soon, for now, fork the project and build it yourself through cleanMavenDeployLocally -->
<dependency>
    <groupId>de.jexcellence.multiverse</groupId>
    <artifactId>jemultiverse</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- or if you are using gradle: -->
implementation("de.jexcellence.multiverse:jemultiverse:1.0.0")
```

### Example hibernate.properties
```yml
# JDBC URL for H2 in-memory database
jakarta.persistence.jdbc.url=jdbc:h2:mem:testdb

# Database credentials
jakarta.persistence.jdbc.user=sa
jakarta.persistence.jdbc.password=

# Database configuration
database.type=h2
database.name=testdb
database.port=3306
database.host=localhost

# Hibernate settings
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.highlight_sql=true

# Schema generation
jakarta.persistence.schema-generation.database.action=update
```

### Using JEMultiverse
## As a Developer: Spigot Paper Adapter
# To integrate JEMultiverse with a Spigot Paper server, you can use the MultiverseAdapter class. This adapter provides methods to interact with the multiverse API.

Example Usage
```java
import de.jexcellence.multiverse.api.MultiverseAdapter;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    private MultiverseAdapter multiverseAdapter;

    @Override
    public void onEnable() {
        multiverseAdapter = new MultiverseAdapter(this);
        // Register the MultiverseAdapter as a service
        getServer().getServicesManager().register(MultiverseAdapter.class, multiverseAdapter, this, ServicePriority.Normal);

        // Example: Access the adapter from another part of the plugin
        MultiverseAdapter adapter = getServer().getServicesManager().load(MultiverseAdapter.class);
        if (adapter != null) {
            // Use the adapter for operations
        }
    }
}
```

### As a Player: Using Commands
## JEMultiverse provides several commands for players to manage and interact with worlds.

## nAvailable Commands
/mv create <world_name> <world_type>: Create a new world.
/mv delete <world_name>: Delete an existing world.
/mv edit <world_name>: Edit a world using the UI.
/mv teleport <world_name>: Teleport to a specified world.
/mv list: List all available worlds.
/mv help: Display help information for commands.

### Customizing Language Files (it also contains an en.yml file)
## JEMultiverse supports custom language files, allowing you to tailor messages and prompts to fit your server's theme or language preferences.
```yml
translations:
  multiverse:
    world_created:
      - "<color:#d3d3d3>Die Welt</color> <bold><color:#ffd700>%world_name%</color></bold> <color:#d3d3d3>wurde erfolgreich erstellt.</color> <color:#00ff00>✔</color>"
    world_deleted:
      - "<color:#d3d3d3>Die Welt</color> <bold><color:#ffd700>%world_name%</color></bold> <color:#d3d3d3>wurde erfolgreich gelöscht.</color> <color:#00ff00>✔</color>"
    teleport_success:
      - "<color:#d3d3d3>Du wurdest erfolgreich teleportiert.</color> <color:#00ff00>✔</color>"
```

### Conclusion
## JEMultiverse provides a powerful and flexible solution for managing multiple worlds in a Minecraft server. Its robust API, user-friendly features, and customization options simplify world management and enhance the gaming experience.
