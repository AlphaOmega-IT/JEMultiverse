package de.jexcellence.multiverse.command.spawn;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

/**
 * Defines the configuration section for the PSpawn command, including
 * environment settings and the command name.
 */
public class PSpawnSection extends ACommandSection {

	private static final String COMMAND_NAME = "pspawn";

	/**
	 * Constructs a new PSpawnSection with the specified environment builder.
	 *
	 * @param environmentBuilder The environment builder for customized evaluations.
	 */
	public PSpawnSection(
		final EvaluationEnvironmentBuilder environmentBuilder
	) {
		super(COMMAND_NAME, environmentBuilder);
	}
}