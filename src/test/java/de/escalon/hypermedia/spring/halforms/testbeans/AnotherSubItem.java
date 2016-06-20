package de.escalon.hypermedia.spring.halforms.testbeans;

import java.io.Serializable;

import de.escalon.hypermedia.spring.AffordanceBuilder;
import de.escalon.hypermedia.spring.halforms.testbeans.DummyController.RemoteOptions;

public class AnotherSubItem implements Serializable {

	public static final AnotherSubItem[] VALIDS = { new AnotherSubItem("1", "S" + 1), new AnotherSubItem("2", "S" + 2),
			new AnotherSubItem("3", "S" + 3), new AnotherSubItem("4", "S" + 4) };

	public static final AnotherSubItem INVALID_VALUE = new AnotherSubItem(Integer.MAX_VALUE + "", "S" + Integer.MAX_VALUE);

	private String name;

	private String owner;

	public AnotherSubItem() {

	}

	public AnotherSubItem(final String name, final String owner) {
		this.name = name;
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public static class SubItemSearchableOptions extends RemoteOptions {
		public SubItemSearchableOptions() {
			super(AffordanceBuilder.methodOn(DummyController.class).searchAnother(null), null, "name");
		}
	}
}
