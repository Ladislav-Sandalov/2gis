package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

@SpringBootApplication
public class DemoApplication {
	private static final String API_KEY = "1";

	public static void main(String[] args) {
		try {
			String query = "банк";
			double lon = 44.002966;
			double lat = 56.321540;
			int radius = 1000;

			// Кодируем параметры
			String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
			String encodedFields = URLEncoder.encode(
					"items.point,items.name,items.contact_groups.contacts,items.schedule,items.description",
					StandardCharsets.UTF_8
			);

			// Формируем URL
			String url = "https://catalog.api.2gis.com/3.0/items"
					+ "?q=" + encodedQuery
					+ "&point=" + lon + "," + lat
					+ "&radius=" + radius
					+ "&key=" + API_KEY
					+ "&fields=" + encodedFields;

			System.out.println("Запрос URL: " + url);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.header("Accept", "application/json")
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// Парсинг ответа
			JSONObject json = new JSONObject(response.body());
			JSONArray items = json.getJSONObject("result").getJSONArray("items");

			System.out.println("\nРезультаты поиска:");
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);

				// Основная информация
				String name = item.optString("name", "Название не указано");
				String description = item.optString("description", "Описание отсутствует");

				// Координаты
				JSONObject point = item.optJSONObject("point");
				String coordinates = (point != null)
						? point.optDouble("lon", 0) + ", " + point.optDouble("lat", 0)
						: "Координаты не доступны";

				// Контакты
				String phone = "Телефон не указан";
				if (item.has("contact_groups.contacts")) {
					JSONArray contacts = item.getJSONArray("contact_groups.contacts");
					for (int j = 0; j < contacts.length(); j++) {
						JSONObject contact = contacts.getJSONObject(j);
						if ("phone".equals(contact.optString("type"))) {
							phone = contact.optString("value", "Телефон не указан");
							break;
						}
					}
				}

				// Время работы
				String schedule = "График работы не указан";
				if (item.has("schedule")) {
					JSONObject scheduleObj = item.getJSONObject("schedule");
					schedule = scheduleObj.optString("comment",
							scheduleObj.optString("text", "График работы не указан"));
				}

				// Вывод информации
				System.out.println("\n" + (i+1) + ". " + name);
				System.out.println("--------------------------------");
				System.out.println("Координаты: " + coordinates);
				System.out.println("Описание: " + description);
				System.out.println("Телефон: " + phone);
				System.out.println("График работы: " + schedule);
			}

		} catch (Exception e) {
			System.err.println("Ошибка: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
