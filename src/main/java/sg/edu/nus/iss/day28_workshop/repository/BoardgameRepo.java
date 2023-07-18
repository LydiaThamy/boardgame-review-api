package sg.edu.nus.iss.day28_workshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
// import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators.Avg;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.ArrayElemAt;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Map;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class BoardgameRepo {

    @Autowired
    private MongoTemplate template;

    public final static String C_GAMES = "games";
    public final static String C_COMMENTS = "comments";

    public final static String F_ID = "_id";
    public final static String F_GID = "gid";
    public final static String F_NAME = "name";
    public final static String F_YEAR = "year";
    public final static String F_RANKING = "ranking";
    public final static String F_USERS_RATED = "users_rated";
    public final static String F_URL = "url";
    public final static String F_IMAGE = "image";
    public final static String F_AVERAGE = "average";
    public final static String F_TIMESTAMP = "timestamp";

    public final static String F_REVIEWS = "reviews";
    public final static String F_REVIEW_ID = "review_id";
    public final static String F_C_ID = "c_id";
    public final static String F_USER = "user";
    public final static String F_RATING = "rating";
    public final static String F_C_TEXT = "c_text";
    public final static String F_COMMENT = "comment";

    public final static String DOT = ".";

    public Optional<Document> getGameByGid(Integer gid) {

        /*
         * db.games.findOne( {
         * "gid": gid
         * })
         */

        Query query = Query.query(
                Criteria.where(F_GID).is(gid));

        query.fields()
                .exclude(F_ID);

        return Optional.ofNullable(
                template.findOne(query, Document.class, C_GAMES));
    }

    public Optional<Document> getCommentByCid(String cId) {

        /*
         * db.comments.findOne( {
         * "c_id": cId
         * })
         */

        Query query = Query.query(
                Criteria.where(F_C_ID).is(cId));

        query.fields()
                .exclude(F_ID);

        return Optional.ofNullable(
                template.findOne(query, Document.class, C_COMMENTS));
    }

    public Optional<Document> getBoardgameByGid(Integer gid) {

        /*
            db.games.aggregate([
                { $match: { gid: NumberInt(5)}},
                { $lookup: 
                    {
                        from: 'comments',
                        localField: 'gid',
                        foreignField: 'gid',
                        pipeline: [{
                            $sort: {'rating' : -1}
                        }],
                        as: 'reviews'}
                },
                { $addFields: {
                    average: { 
                        // $floor: {
                            $avg: "$reviews.rating"    
                            // }                    
                        },
                    reviews: { 
                        $map: {
                            input: "$reviews",
                            as: "review",
                            in: { $concat: [ "/review/", '$$review.c_id'] }
                        }
                    },
                    timestamp: "$$NOW"
                }},
                { $project: { _id: 0} },
            ])
        */

        MatchOperation matchById = Aggregation.match(
            Criteria.where(F_GID).is(gid)
        );

        LookupOperation lookupComments = Aggregation.lookup()
                .from(C_COMMENTS)
                .localField(F_GID)
                .foreignField(F_GID)
                .pipeline(
                    Aggregation.sort(Direction.DESC, F_RATING)
                ).as(F_REVIEWS);
                
        AddFieldsOperation addFields = Aggregation.addFields()
                .addField(F_AVERAGE).withValueOf(
                    // Floor.floorValueOf(
                        Avg.avgOf(F_REVIEWS + DOT + F_RATING)
                    // )
                )
                .addField(F_REVIEWS).withValueOf(
                    Map.itemsOf(F_REVIEWS).as(F_REVIEWS).andApply(
                        StringOperators.Concat.stringValue("/review/").concatValueOf(F_REVIEWS + DOT + F_C_ID)
                    )
                )
                .addField(F_TIMESTAMP).withValueOf(LocalDateTime.now())
                .build();

        ProjectionOperation projectId = Aggregation.project()
            .andExclude(F_ID)
        ;
        
        // UnwindOperation unwindComments = Aggregation.unwind(F_REVIEWS);

        // ProjectionOperation projectCid = Aggregation.project(F_GID, F_NAME, F_YEAR, F_RANKING, F_USERS_RATED, F_URL, F_IMAGE)
        //         .and(
        //             StringOperators.Concat.stringValue("/review/")
        //                 .concatValueOf(F_REVIEWS + "." + F_C_ID)
        //         ).as(F_C_ID);
        
        // GroupOperation groupByGame = Aggregation.group(F_GID, F_NAME, F_YEAR, F_RANKING, F_USERS_RATED, F_URL, F_IMAGE)
        //         .push(
        //             // F_REVIEWS
        //             // F_REVIEWS + "." + F_C_ID
        //             F_C_ID
        //             ).as(F_REVIEWS);
        
        // ProjectionOperation projectFields = Aggregation.project(
        //             F_ID + DOT + F_GID, 
        //             F_ID + DOT + F_NAME,
        //             F_ID + DOT + F_YEAR,
        //             F_ID + DOT + F_RANKING,
        //             F_ID + DOT + F_USERS_RATED,
        //             F_ID + DOT + F_URL,
        //             F_ID + DOT + F_IMAGE,
        //             F_REVIEWS
        //         )
        //         .and(F_ID + DOT + F_RANKING).as("rank")
        //         .andExclude(F_ID);

        Aggregation pipeline = Aggregation
                .newAggregation(matchById, lookupComments, addFields, projectId
                    // , unwindComments, projectCid, groupByGame, projectFields
                );

        List<Document> results = template.aggregate(pipeline, C_GAMES, Document.class).getMappedResults();

        if (results.isEmpty())
            return Optional.empty();
        
        return Optional.of(results.get(0));

    }

    public List<Document> getHighestReview(Integer limit, Integer skip) {

        /*
            db.games.aggregate([
                { $lookup: {
                    from: "comments",
                    localField: "gid",
                    foreignField: "gid",
                    pipeline: [
                        { $sort: {rating : -1}},
                        { $limit: 1},
                        { $project: { _id: 0, c_id: 1, user: 1, rating: 1, c_text: 1}}
                        ],
                    as: "reviews"
                }},
                { $addFields: {
                    _id: "$gid",
                    rating: { $arrayElemAt: [ "$reviews.rating" , 0 ]},
                    user: { $arrayElemAt: [ "$reviews.user" , 0 ]},
                    comment: { $arrayElemAt: [ "$reviews.c_text" , 0 ]},
                    review_id: { $arrayElemAt: [ "$reviews.c_id" , 0 ]},
                }},
                { $project: { name: 1, rating: 1, user: 1, comment: 1, review_id: 1 }},
                { $skip: 5},
                { $limit: 5 }
                
            //    { $project: { gid: 0, year: 0, ranking: 0, users_rated: 0, url: 0, image: 0, reviews: 0 }}
                
            //    { $unwind: "$reviews"},
            //    { $group: {
            //        _id: "$gid",
            //        name: { $push: "$name"},
            //        rating: { $push: "$reviews.rating"},
            //        user: { $push: "$reviews.user"},
            //        comment: { $push: "$reviews.c_text"},
            //        review_id: { $push: "$reviews.c_id"}
            //    }},
            //    { $addFields: {
            //        name: { $arrayElemAt: [ "$name" , 0 ]},
            //        rating: { $arrayElemAt: [ "$rating" , 0 ]},
            //        user: { $arrayElemAt: [ "$user" , 0 ]},
            //        comment: { $arrayElemAt: [ "$comment" , 0 ]},
            //        review_id: { $arrayElemAt: [ "$review_id" , 0 ]},
            //    }}

            ])
         */

         LookupOperation lookupComments = Aggregation.lookup()
                .from(C_COMMENTS)
                .localField(F_GID)
                .foreignField(F_GID)
                .pipeline(
                    Aggregation.sort(Direction.DESC, F_RATING),
                    Aggregation.limit(1),
                    Aggregation.project(F_C_ID, F_USER, F_RATING, F_C_TEXT).andExclude(F_ID)
                )
                .as(F_REVIEWS);
 
        AddFieldsOperation addFields = Aggregation.addFields()
                .addFieldWithValueOf(F_ID, F_GID)
                .addFieldWithValueOf(F_RATING,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_RATING).elementAt(0))
                .addFieldWithValueOf(F_USER,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_USER).elementAt(0))
                .addFieldWithValueOf(F_COMMENT,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_C_TEXT).elementAt(0))
                .addFieldWithValueOf(F_REVIEW_ID,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_C_ID).elementAt(0))
                .build();
        
        ProjectionOperation projectFields = Aggregation.project(F_ID, F_NAME, F_RATING, F_USER, F_COMMENT, F_REVIEW_ID);

        SkipOperation skipResults = Aggregation.skip(skip);

        LimitOperation limitResults = Aggregation.limit(limit);

        Aggregation pipeline = Aggregation.newAggregation(lookupComments, addFields, projectFields, skipResults, limitResults);

        return template.aggregate(pipeline, C_GAMES, Document.class).getMappedResults();
    }

    public List<Document> getLowestReview(Integer limit, Integer skip) {

        /*
            db.games.aggregate([
                { $lookup: {
                    from: "comments",
                    localField: "gid",
                    foreignField: "gid",
                    pipeline: [
                        { $sort: {rating : 1}},
                        { $limit: 1},
                        { $project: { _id: 0, c_id: 1, user: 1, rating: 1, c_text: 1}}
                        ],
                    as: "reviews"
                }},
                { $addFields: {
                    _id: "$gid",
                    rating: { $arrayElemAt: [ "$reviews.rating" , 0 ]},
                    user: { $arrayElemAt: [ "$reviews.user" , 0 ]},
                    comment: { $arrayElemAt: [ "$reviews.c_text" , 0 ]},
                    review_id: { $arrayElemAt: [ "$reviews.c_id" , 0 ]},
                }},
                { $project: { name: 1, rating: 1, user: 1, comment: 1, review_id: 1 }},
                { $skip: 5},
                { $limit: 5 }

            ])
         */

         LookupOperation lookupComments = Aggregation.lookup()
                .from(C_COMMENTS)
                .localField(F_GID)
                .foreignField(F_GID)
                .pipeline(
                    Aggregation.sort(Direction.ASC, F_RATING),
                    Aggregation.limit(1),
                    Aggregation.project(F_C_ID, F_USER, F_RATING, F_C_TEXT).andExclude(F_ID)
                )
                .as(F_REVIEWS);
 
        AddFieldsOperation addFields = Aggregation.addFields()
                .addFieldWithValueOf(F_ID, F_GID)
                .addFieldWithValueOf(F_RATING,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_RATING).elementAt(0))
                .addFieldWithValueOf(F_USER,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_USER).elementAt(0))
                .addFieldWithValueOf(F_COMMENT,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_C_TEXT).elementAt(0))
                .addFieldWithValueOf(F_REVIEW_ID,
                    ArrayElemAt.arrayOf(F_REVIEWS + DOT + F_C_ID).elementAt(0))
                .build();
        
        ProjectionOperation projectFields = Aggregation.project(F_ID, F_NAME, F_RATING, F_USER, F_COMMENT, F_REVIEW_ID);

        SkipOperation skipResults = Aggregation.skip(skip);

        LimitOperation limitResults = Aggregation.limit(limit);

        Aggregation pipeline = Aggregation.newAggregation(lookupComments, addFields, projectFields, skipResults, limitResults);

        return template.aggregate(pipeline, C_GAMES, Document.class).getMappedResults();
    }
}
