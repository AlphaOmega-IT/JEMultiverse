package de.jexcellence.multiverse.command.multiverse;

/**
 * Defines the various actions available to the PMultiverse command,
 * such as creation, deletion, editing, forced creation, etc.
 */
public enum PMultiverseAction {
  /**
   * Represents a request to create a new world.
   */
  CREATE,

  /**
   * Represents a request to delete an existing world.
   */
  DELETE,

  /**
   * Represents a request to edit a world via a custom UI or process.
   */
  EDIT,

  /**
   * Represents a request to force create a world, bypassing typical checks.
   */
  FORCE_CREATION,

  /**
   * Represents a request to display help information.
   */
  HELP,

  /**
   * Represents a request to list all available worlds.
   */
  LIST,

  /**
   * Represents a request to load an existing world.
   */
  LOAD,

  /**
   * Represents a request to teleport to another world.
   */
  TELEPORT
}