package sg.edu.nus.iss.day28_workshop.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
// import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import sg.edu.nus.iss.day28_workshop.Utility;
import sg.edu.nus.iss.day28_workshop.repository.BoardgameRepo;

@Service
public class BoardgameService {
    
    @Autowired
    private BoardgameRepo repo;

    public Optional<String> getCommentByCid(String cId) {
        
        Optional<Document> result = repo.getCommentByCid(cId);

        if (result.isEmpty())
            return Optional.empty();

        return Optional.of(
            Utility.toJsonComment(result.get()) // String
            );
    }

    public Optional<String> getGameByGid(Integer gid) {

        Optional<Document> result = repo.getGameByGid(gid);

        if (result.isEmpty())
            return Optional.empty();

        return Optional.of(
            Utility.toJsonGame(result.get()) // String
            );
    }

    public Optional<String> getBoardgameByGid(Integer gid) {
        
        Optional<Document> result = repo.getBoardgameByGid(gid);

        if (result.isEmpty())
            return Optional.empty();

        return Optional.ofNullable(
            Utility.toJsonBoardgame(result.get() // String
                )
            );
    }

    public String getLowestReview(Integer limit, Integer skip) {

        List<JsonObject> result = Utility.toJsonBoardgame(
            repo.getLowestReview(limit, skip));

        JsonArrayBuilder jBuilder = Json.createArrayBuilder();
        
        for (JsonObject s : result)
            jBuilder.add(s);
        
        return Json.createObjectBuilder()
            .add("rating", "lowest")
            .add("games", jBuilder)
            .add("timestamp", new Date().toString())
            .build()
            .toString();

    }
    
    public String getHighestReview(Integer limit, Integer skip) {

        // List<String> result = Utility.toJsonBoardgame(
        //     repo.getHighestRatings());

        List<JsonObject> result = Utility.toJsonBoardgame(
            repo.getHighestReview(limit, skip));

        JsonArrayBuilder jBuilder = Json.createArrayBuilder();
        
        for (JsonObject s : result)
        // for (String s : result)
            jBuilder.add(s);
        
        return Json.createObjectBuilder()
            .add("rating", "highest")
            .add("games", jBuilder)
            .add("timestamp", new Date().toString())
            .build()
            .toString();

        }
        
        // public List<Document> getHighestRatings() {
        //     return repo.getHighestRatings();
        // }

    public String getIndexJson() {
        
        return Json.createObjectBuilder()
                .add("game", "/game/<game_id>")
                .add("review", "/review/<review_id>")
                .add("game with reviews", "game/<game_id>/reviews")
                .add("games with highest review", "/games/highest")
                .add("games with lowest review", "/games/lowest")
                .build()
                .toString();

    }
} 
