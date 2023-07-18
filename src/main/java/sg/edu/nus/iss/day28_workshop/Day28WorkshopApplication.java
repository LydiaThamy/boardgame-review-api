package sg.edu.nus.iss.day28_workshop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import sg.edu.nus.iss.day28_workshop.repository.BoardgameRepo;

@SpringBootApplication
public class Day28WorkshopApplication implements CommandLineRunner {

	@Autowired
	private BoardgameRepo bRepo;

	public static void main(String[] args) {
		SpringApplication.run(Day28WorkshopApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println(bRepo.getHighestReview(1, 0));
		System.out.println(bRepo.getLowestReview(5, 0));
	}

}
