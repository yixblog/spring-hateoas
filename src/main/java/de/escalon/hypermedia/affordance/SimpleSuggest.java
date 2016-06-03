package de.escalon.hypermedia.affordance;

public class SimpleSuggest<T> extends SuggestImpl<SuggestObjectWrapper> {

	public SimpleSuggest(SuggestObjectWrapper wrapper, SuggestType type) {
		super(wrapper, type, "id", "text");
	}

	public SimpleSuggest(String text, String value, SuggestType type) {
		super(new SuggestObjectWrapper<String>(text, value, value), type, "id", "text");
	}

	public SimpleSuggest(String text, String svalue, T value, SuggestType type) {
		super(new SuggestObjectWrapper<T>(text, svalue, value), type, "id", "text");
	}

	public static <T> Suggest<SuggestObjectWrapper>[] wrap(T[] values, SuggestType type) {
		@SuppressWarnings("unchecked")
		Suggest<SuggestObjectWrapper>[] suggests = new Suggest[values.length];
		for (int i = 0; i < suggests.length; i++) {
			suggests[i] = new SimpleSuggest<T>(
					new SuggestObjectWrapper<T>(String.valueOf(values[i]), String.valueOf(values[i]), values[i]), type);
		}
		return suggests;
	}

}
