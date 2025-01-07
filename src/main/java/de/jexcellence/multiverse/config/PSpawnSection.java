package de.jexcellence.multiverse.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class PSpawnSection extends ACommandSection {
	
	private static final String COMMAND_NAME = "spawn";
	
	public PSpawnSection(
			final EvaluationEnvironmentBuilder environmentBuilder
	) {
		super(COMMAND_NAME, environmentBuilder);
	}
}
