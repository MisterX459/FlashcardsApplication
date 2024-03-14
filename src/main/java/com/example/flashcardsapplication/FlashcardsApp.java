package com.example.flashcardsapplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.*;
class Entry {
	private String word;
	private Map<String, String> translations;
	public Entry(String word) {
		this.word = word;
		this.translations = new HashMap<>();
	}
	public String getWord() {
		return word;
	}

	public Map<String, String> getTranslations() {
		return translations;
	}

	public void addTranslation(String language, String translation) {
		translations.put(language, translation);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Word in english: " + word);
		translations.forEach((lang, trans) -> sb.append(", Translation in ").append(lang).append(": ").append(trans));
		return sb.toString();
	}
}

interface RepositoryInterface {
	void addEntry(Entry entry);
	List<Entry> getData();
}
class EntryRepository implements RepositoryInterface {
	private final List<Entry> entries = new ArrayList<>();

	public void addEntry(Entry entry) {
		entries.add(entry);
	}

	public List<Entry> getData() {
		return entries;
	}
}
@Service
class FileService {
	private final EntryRepository repository;
	public FileService(EntryRepository repository) {
		this.repository = repository;
	}
	public void loadFile(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(";");
				Entry entry = new Entry(parts[0]);
				entry.addTranslation("DE", parts[1]);
				entry.addTranslation("PL", parts[2]);
				repository.addEntry(entry);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}public void writing(String filename) {
		try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
			for (Entry entry : repository.getData()) {
				pw.println(entry.getWord() + ";" + entry.getTranslations().get("DE") + ";" + entry.getTranslations().get("PL"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
@Controller
class FlashcardsController {
	private final EntryRepository entryRepository;
	private final FileService fileService;
	private final Random random;
	public FlashcardsController(EntryRepository entryRepository, FileService fileService) {
		this.entryRepository = entryRepository;
		this.fileService = fileService;
		this.random = new Random();
	}
	public void listAll() {
		entryRepository.getData().forEach(System.out::println);
	}
	public void addNewWord(String word, String translation1, String translation2) {
		Entry entry = new Entry(word);
		entry.addTranslation("DE", translation1);
		entry.addTranslation("PL", translation2);
		entryRepository.addEntry(entry);
		fileService.writing("dictionary.csv");
	}
	public void test() {
		List<Entry> entries = entryRepository.getData();
		if (entries.isEmpty()) {
			System.out.println("Dictionary is empty.");
			return;
		}
		Entry randomEntry = entries.get(random.nextInt(entries.size()));
		Scanner scanner = new Scanner(System.in);
		System.out.println("Translate the word: " + randomEntry.getWord());
		System.out.print("Translation in german: ");
		String translation1 = scanner.nextLine().toLowerCase();
		System.out.print("Translation in polish: ");
		String translation2 = scanner.nextLine().toLowerCase();

		if (translation1.equals(randomEntry.getTranslations().get("DE").toLowerCase()) &&
				translation2.equals(randomEntry.getTranslations().get("PL").toLowerCase())) {
			System.out.println("Correct!");
		} else {
			System.out.println("Incorrect. Correct translations are: " +
					randomEntry.getTranslations().get("DE") + ", " + randomEntry.getTranslations().get("PL"));
		}
	}
}
@Configuration
class AppConfig {
	@Bean
	public EntryRepository entryRepository() {
		return new EntryRepository();
	}
	@Bean
	public FileService fileService(EntryRepository entryRepository) {
		return new FileService(entryRepository);
	}
	@Bean
	public FlashcardsController flashcardsController(EntryRepository entryRepository,FileService fileService) {
		return new FlashcardsController(entryRepository,fileService);
	}
}

@SpringBootApplication
public class FlashcardsApp {
	public static void main(String[] args) {
		SpringApplication.run(FlashcardsApp.class, args);
		EntryRepository entryRepository = new EntryRepository();
		FileService fileService = new FileService(entryRepository);
		FlashcardsController flashcardsController = new FlashcardsController(entryRepository,fileService);
		fileService.loadFile("dictionary.csv");
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("Menu:");
			System.out.println("1.Add a new word");
			System.out.println("2.Display all words");
			System.out.println("3.Start test");
			System.out.println("4.Exit");
			System.out.print("Enter your choice: ");
			int choice = scanner.nextInt();
			scanner.nextLine();
			switch (choice) {
				case 1:
					System.out.print("Enter word: ");
					String word = scanner.nextLine();
					System.out.print("Enter translation in german: ");
					String translationDE = scanner.nextLine();
					System.out.print("Enter translation in polish: ");
					String translationPL = scanner.nextLine();
					flashcardsController.addNewWord(word, translationDE, translationPL);
					break;
				case 2:
					flashcardsController.listAll();
					break;
				case 3:
					flashcardsController.test();
					break;
				case 4:
					System.out.println("Bye");
					System.exit(0);
				default:
					System.out.println("Invalid choice.Try again.");
			}
		}
	}
}
