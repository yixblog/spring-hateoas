package de.escalon.hypermedia.affordance;

import java.util.ArrayList;
import java.util.List;

public class SimpleSuggest<T> extends SuggestImpl<SuggestObjectWrapper<T>> {

	@SuppressWarnings("unchecked")
	public SimpleSuggest(String text, String value, SuggestType type) {
		this(text, value, (T) value, type);
	}

	public SimpleSuggest(String text, String svalue, T value, SuggestType type) {
		this(new SuggestObjectWrapper<T>(text, svalue, value), type);
	}

	public SimpleSuggest(SuggestObjectWrapper<T> wrapper, SuggestType type) {
		super(wrapper, type, SuggestObjectWrapper.ID, SuggestObjectWrapper.TEXT);
	}

	public static <T> List<Suggest<SuggestObjectWrapper<T>>> wrap(T[] values, SuggestType type) {
		List<Suggest<SuggestObjectWrapper<T>>> suggests = new ArrayList<Suggest<SuggestObjectWrapper<T>>>(values.length);
		for (int i = 0; i < values.length; i++) {
			suggests.add(new SimpleSuggest<T>(
					new SuggestObjectWrapper<T>(String.valueOf(values[i]), String.valueOf(values[i]), values[i]), type));
		}
		return suggests;
	}

}
