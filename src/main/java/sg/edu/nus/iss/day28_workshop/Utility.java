package sg.edu.nus.iss.day28_workshop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import sg.edu.nus.iss.day28_workshop.repository.BoardgameRepo;

public class Utility {

    public static String toJsonGame(Document document) {

        return Json.createObjectBuilder()
                .add("game_id", document.getInteger(BoardgameRepo.F_GID))
                .add("name", document.getString(BoardgameRepo.F_NAME))
                .add("year", document.getInteger(BoardgameRepo.F_YEAR))
                .add("rank", document.getInteger(BoardgameRepo.F_RANKING))
                .add("users_rated", document.getInteger(BoardgameRepo.F_USERS_RATED))
                .add("url", document.getString(BoardgameRepo.F_URL))
                .add("thumbnail", document.getString(BoardgameRepo.F_IMAGE))
                .build()
                .toString();
    }

    public static String toJsonComment(Document document) {

        return Json.createObjectBuilder()
                .add("review_id", document.getString(BoardgameRepo.F_C_ID))
                .add("user", document.getString(BoardgameRepo.F_USER))
                .add("rating", document.getInteger(BoardgameRepo.F_RATING))
                .add("comments", document.getString(BoardgameRepo.F_C_TEXT))
                .add("game_id", document.getInteger(BoardgameRepo.F_GID))
                .build()
                .toString();
    }

    public static String toJsonBoardgame(Document document) {

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (String s: document.getList("reviews", String.class))
            arrayBuilder.add(s);
        
        JsonArray jArr = arrayBuilder.build();

        return Json.createObjectBuilder()
            .add("game_id", document.getInteger(BoardgameRepo.F_GID))
                .add("name", document.getString(BoardgameRepo.F_NAME))
                .add("year", document.getInteger(BoardgameRepo.F_YEAR))
                .add("rank", document.getInteger(BoardgameRepo.F_RANKING))
                .add("average", document.getDouble(BoardgameRepo.F_AVERAGE).intValue())
                .add("users_rated", document.getInteger(BoardgameRepo.F_USERS_RATED))
                .add("url", document.getString(BoardgameRepo.F_URL))
                .add("thumbnail", document.getString(BoardgameRepo.F_IMAGE))
                .add("reviews", jArr)
                .add("timestamp", document.get(BoardgameRepo.F_TIMESTAMP, Date.class).toString())
                .build()
                .toString();
    }

    private static JsonObject toJsonObject(Document doc) {

        return Json.createObjectBuilder()
                   .add("_id", doc.get(BoardgameRepo.F_ID, Document.class).toString())
                   .add("name", doc.getString(BoardgameRepo.F_NAME))
                   .add("rating", doc.getInteger(BoardgameRepo.F_RATING))
                   .add("user", doc.getString(BoardgameRepo.F_USER))
                   .add("comment", doc.getString(BoardgameRepo.F_COMMENT))
                   .add("review_id", doc.getString(BoardgameRepo.F_REVIEW_ID))
                   .build();
    } 

    public static List<JsonObject> toJsonBoardgame(List<Document> documents) {
    // public static List<String> toJsonBoardgame(List<Document> documents) {
        
        // List<String> result = new ArrayList<>();

        // JsonObjectBuilder jBuilder = Json.createObjectBuilder();

        // for (Document doc: documents)
        //     result.add(
        //         jBuilder
        //            .add("_id", doc.get(BoardgameRepo.F_ID, Document.class).toString())
        //            .add("name", doc.getString(BoardgameRepo.F_NAME))
        //            .add("rating", doc.getInteger(BoardgameRepo.F_RATING))
        //            .add("user", doc.getString(BoardgameRepo.F_USER))
        //            .add("comment", doc.getString(BoardgameRepo.F_COMMENT))
        //            .add("review_id", doc.getString(BoardgameRepo.F_REVIEW_ID))
        //            .build().toString()
        //     );
        
        List<JsonObject> result = new ArrayList<>();

        for (Document doc: documents)
            result.add(toJsonObject(doc));

        return result;
    }

}
