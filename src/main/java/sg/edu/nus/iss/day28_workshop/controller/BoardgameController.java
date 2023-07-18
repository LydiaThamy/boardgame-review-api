package sg.edu.nus.iss.day28_workshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.day28_workshop.service.BoardgameService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class BoardgameController {


    @Autowired
    private BoardgameService service;

    @GetMapping(path = "/review/{cId}")
    public ResponseEntity<String> getComment(@PathVariable String cId) {

        Optional<String> result = service.getCommentByCid(cId);

        if (result.isEmpty())
            // return ResponseEntity.badRequest().body("review with ID " + cId + " does not exist");
            return ResponseEntity.status(404).body("review ID " + cId + " does not exist");

        return ResponseEntity.ok().body(result.get());
    }

    @GetMapping("/game/{game_id}")
    public ResponseEntity<String> getGame(@PathVariable String game_id) {
        
        Integer gid;

        try {
            gid = Integer.parseInt(game_id);    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("game ID " + game_id + " must be a number"); 
        }

        Optional<String> result = service.getGameByGid(gid);

        if (result.isEmpty())
            // return ResponseEntity.badRequest().body("game ID " + game_id + " does not exist");
            return ResponseEntity.status(404).body("game ID " + game_id + " does not exist");

        return ResponseEntity.ok().body(result.get());
    }

    @GetMapping("/game/{game_id}/reviews")
    public ResponseEntity<String> getBoardgame(@PathVariable String game_id) {

        Integer gid;

        try {
            gid = Integer.parseInt(game_id);    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("game ID " + game_id + " must be a number"); 
        }

        Optional<String> result = service.getBoardgameByGid(gid);

        if (result.isEmpty())
            // return ResponseEntity.badRequest().body("game ID " + game_id + " does not exist");
            return ResponseEntity.status(404).body("game ID " + game_id + " does not exist");

        return ResponseEntity.ok().body(result.get());
    }

    @GetMapping("/games/lowest")
    public ResponseEntity<String> getLowestReview(@RequestParam(name = "limit", defaultValue = "5") Integer limit, @RequestParam(name = "skip", defaultValue = "0") Integer skip) {
        return ResponseEntity.ok().body(
            service.getLowestReview(limit, skip).toString());
        }

    @GetMapping("/games/highest")
    public ResponseEntity<String> getHighestReview(@RequestParam(name = "limit", defaultValue = "5") Integer limit, @RequestParam(name = "skip", defaultValue = "0") Integer skip) {
        return ResponseEntity.ok().body(
            service.getHighestReview(limit, skip).toString());
        }
    // public ResponseEntity<String> getHighestRatings() {
    //     return ResponseEntity.ok().body(
    //         service.getHighestRatings().toString());
    // }
}
