package de.escalon.hypermedia.spring.xhtml.beans;

import static de.escalon.hypermedia.spring.Path.on;
import static de.escalon.hypermedia.spring.Path.path;

public enum ItemParams {
	ID(path(on(Item.class).getId())), NAME(path(on(Item.class).getName())), AMOUNT(path(on(Item.class).getAmount())), SINGLESUB(
			path(on(Item.class).getSingleSub())), SUBITEMID(path(on(Item.class).getSubItemId())), TYPE(
					path(on(Item.class).getType())), LISTSUBENTITY(path(on(Item.class).getListSubEntity())), MULTIPLE(
							path(on(Item.class).getMultiple())), UNDEFINEDLIST(path(on(Item.class).getUndefinedList()));

	private String path;

	ItemParams(final String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}