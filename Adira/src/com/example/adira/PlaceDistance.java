package com.example.adira;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;

public class PlaceDistance implements Serializable {

	@Key
	public String status;

	@Key
	public List<Rows> rows;

	public static class Rows implements Serializable {
		@Key
		public List<Elements> elements;
	}

	public static class Elements implements Serializable {
		@Key
		public Distance distance;
		@Key
		public Duration duration;
	}

	public static class Distance implements Serializable {
		@Key
		public String text;
	}

	public static class Duration implements Serializable {
		@Key
		public String text;
	}

}
