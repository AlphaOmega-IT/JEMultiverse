package de.jexcellence.multiverse.command.spawn;

import de.jexcellence.evaluable.section.ACommandSection;
import de.jexcellence.gpeee.interpreter.EvaluationEnvironmentBuilder;

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