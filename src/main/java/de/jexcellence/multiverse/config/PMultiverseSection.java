package de.jexcellence.multiverse.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class PMultiverseSection extends ACommandSection {
	
	private static final String COMMAND_NAME = "multiverse";
	
	public PMultiverseSection(
			final EvaluationEnvironmentBuilder environmentBuilder
	) {
		super(COMMAND_NAME, environmentBuilder);
	}
}
