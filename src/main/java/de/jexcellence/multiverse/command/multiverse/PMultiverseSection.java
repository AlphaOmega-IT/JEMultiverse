package de.jexcellence.multiverse.command.multiverse;

import de.jexcellence.evaluable.section.ACommandSection;
import de.jexcellence.gpeee.interpreter.EvaluationEnvironmentBuilder;

/**
 * Defines the configuration section for the PMultiverse command, including
 * environment settings and the command name.
 */
public class PMultiverseSection extends ACommandSection {

	private static final String COMMAND_NAME = "pmultiverse";

	/**
	 * Constructs a new PMultiverseSection with the specified environment builder.
	 *
	 * @param environmentBuilder The environment builder used for command evaluation.
	 */
	public PMultiverseSection(
		final EvaluationEnvironmentBuilder environmentBuilder
	) {
		super(COMMAND_NAME, environmentBuilder);
	}
}